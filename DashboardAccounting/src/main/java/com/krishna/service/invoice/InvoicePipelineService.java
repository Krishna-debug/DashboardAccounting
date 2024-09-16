package com.krishna.service.invoice;

import java.sql.Timestamp;
import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.krishna.Interfaces.IInvoicePipelineService;
import com.krishna.controller.FeignLegacyInterface;
import com.krishna.domain.ExpectedBillingRate;
import com.krishna.domain.Payroll;
import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.repository.ExpectedBillingRepository;
import com.krishna.repository.invoice.ProjectInvoiceRepository;

@Service
public class InvoicePipelineService implements IInvoicePipelineService {


	@Autowired
	ProjectInvoiceRepository projectInvoiceRepository;

	@Autowired
	ExpectedBillingRepository expectedBillingRepo;

	@Value("${env.url}")
	private String environmentUrl;
	
	@Autowired
	private FeignLegacyInterface feignLegacyInterface;
	
	@Autowired
	EntityManager entityManager;
	
	@Override
	public List<Object> getInvoicePipeline(int month, int year, String projectType, String businessVerticals) {
		Map<String,Object> dataMap = feignLegacyInterface.getProjectsForInvoicePipeline(month, year, projectType);
		List<Map<String,Object>> projectList=new ArrayList<>();
		if(dataMap!=null && dataMap.get("data")!=null)
			projectList = (List<Map<String, Object>>) dataMap.get("data");
		int projectListSize = projectList.size();
		List<Object> resultProjectList = new ArrayList<>();
		Map<String, Double> billingRateMap = getBillingRates(month, year);
		for (int i = 0; i < projectListSize; i++) {
			Map<String, Object> projectData = (Map<String, Object>) projectList.get(i);
			projectData = preparePipelineResponse(projectData, businessVerticals, billingRateMap, month, year);
			if(projectData.containsKey("expectedBilling"))
				resultProjectList.add(projectData);
		}
		return resultProjectList;
	}
	
	public Map<String, Object> preparePipelineResponse(Map<String, Object> projectData, String businessVerticals,
			Map<String, Double> billingRateMap, int month, int year) {
		Double expectedBilling = 0D;
		Double forecastedBilling = 0D;
		String bu = projectData.get("businessVertical").toString();
		if (businessVerticals == null || businessVerticals.equals("") || businessVerticals.equals(bu)) {
			Long projectId = new Long(projectData.get("projectId").toString());
			List<ProjectInvoice> invoices = projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndProjectIdAndInvoiceStatusNot(
							new DateFormatSymbols().getMonths()[month - 1].toString(), Integer.toString(year), false,
							projectId, 6L);
			double invoiceAmount = invoices.stream().collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar));
			projectData.put("invoiceAmount", invoiceAmount);
			projectData.put("expectedHours", projectData.get("resourcingHours"));
			projectData.put("forecastedHours", projectData.get("forecastedHours"));
			
			List<Object> teamList = (List<Object>) projectData.get("team");
			int teamListSize = teamList.size();
			for (int j = 0; j < teamListSize; j++) {
				Map<String, Object> teamData = (Map<String, Object>) teamList.get(j);
				String grade = teamData.get("grade") != null ? teamData.get("grade").toString() : "NA";
				Double expectedHours = Double.parseDouble(teamData.get("resourcingHours").toString());
				Double forecastedHours = Double.parseDouble(teamData.get("forecastedHours").toString());
				Double rateValue = billingRateMap.get(grade);
				System.out.println(" :::::::projectId = " + projectId);
				System.out.println("forecastedHours " + forecastedHours + " rateValue " + rateValue);
				if (rateValue != null) {
					expectedBilling = expectedBilling + (rateValue * expectedHours);
					forecastedBilling = forecastedBilling + (rateValue * forecastedHours);
					teamData.put("expectedBillingRate", rateValue);
					teamData.put("expectedBilling", Math.round((rateValue * expectedHours) * 100.00) / 100.00);
					teamData.put("forecastedBilling", Math.round((rateValue * forecastedHours) * 100.00) / 100.00);
				} else {
					teamData.put("expectedBillingRate", 0);
					teamData.put("expectedBilling", 0);
					teamData.put("forecastedBilling",0);
				}
				teamData.put("resourcingHours", expectedHours);
				teamData.put("forecastedHours", forecastedHours);
			}
			projectData.put("expectedBilling", expectedBilling);
			projectData.put("forcastedBilling", forecastedBilling);
			projectData.put("billingDifference", expectedBilling - invoiceAmount);
			projectData.put("forcastedBillingDifference", forecastedBilling - invoiceAmount);
		}
		return projectData;
	}

	public Map<String, Double> getBillingRates(int month, int year) {
		Query q = entityManager.createNativeQuery("select grade,billing_rate from expected_billing_rate where month=:month and year=:year");
		q.setParameter("month", month);
		q.setParameter("year", year);
		List<Object []> billingsRates = q.getResultList(); 
		if(billingsRates.isEmpty()) {
			
			q= entityManager.createNativeQuery("select grade,billing_rate from expected_billing_rate where month=:month and year=:year");
			q.setParameter("month", month-1);
			q.setParameter("year", year);
			billingsRates = q.getResultList(); 
		}
		Map<String, Double> rateMap = new HashMap<String, Double>();
		for (Object[] billingRate : billingsRates) {
			rateMap.put(billingRate[0].toString(), Double.parseDouble(billingRate[1].toString()) );
		}
		return rateMap;
	}

	@Cacheable(value="actualHours",key="{#month, #year}")
	public List<Object> getActualHoursForInvoicePipeline(int month, int year, String accessToken) {
		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDate lastDay = yearMonth.atEndOfMonth().plusDays(1);
		LocalDate firstDay = yearMonth.atDay(1);
		Timestamp lastTimestamp = Timestamp.valueOf(lastDay.atStartOfDay());
		Timestamp firstTimestamp = Timestamp.valueOf(firstDay.atStartOfDay());
		Calendar cal = Calendar.getInstance();
	    cal.setTimeInMillis(firstTimestamp.getTime());
	    cal.add(Calendar.HOUR, 5);
	    cal.add(Calendar.MINUTE, 30);
	    long start = cal.getTimeInMillis();
		long end = lastTimestamp.getTime();
		List<Object> actualHoursList = new ArrayList<>();
		final String uri = "https://" + environmentUrl
				+ "/zuul/dashboard_node/api/projectWiseApplicationHours?startDate=" + start + "&endDate=" + end;
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.add("user-agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
		ResponseEntity<HashMap> response = restTemplate.exchange(uri, HttpMethod.GET, entity, HashMap.class);
		Map<String, Object> result = response.getBody();
		Map<String, Object> data = (HashMap<String, Object>) result.get("data");
		List<String> keys = new ArrayList<String>(data.keySet());
		for (int i = 0; i < keys.size(); i++) {
			String projectId = (String) keys.get(i);
			List<Object> hoursList = (List<Object>) data.get(keys.get(i));
			Integer seconds  = Integer.parseInt(hoursList.get(0).toString());
			Integer actualHours = 0;
			Integer actualMins = 0;
			if (seconds!=null) {
				actualHours = seconds / 3600;
                actualMins = (seconds % 3600) / 60;
                }
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("projectId", projectId);
			resultMap.put("actualHours", actualHours + "." + actualMins);
			actualHoursList.add(resultMap);
		}
		return actualHoursList;
	}

	//@Scheduled(cron="0 0 0/12 1/1 * ?", zone="IST")
	@CacheEvict(cacheNames = "actualHours", allEntries = true)
       public void flushActualHoursForInvoicePipeline(){
	
	}

}
