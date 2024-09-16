package com.krishna.service;

import static java.util.stream.Collectors.toList;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.util.concurrent.AtomicDouble;
import com.krishna.controller.FeignLegacyInterface;
import com.krishna.domain.BuSpecificCost;
import com.krishna.domain.IndirectCost;
import com.krishna.domain.Payroll;
import com.krishna.domain.ProjectTrendsDTO;
import com.krishna.domain.SecurityDeposit;
import com.krishna.domain.Margin.MarginBasis;
import com.krishna.domain.invoice.InvoiceCycle;
import com.krishna.domain.invoice.InvoiceStatus;
import com.krishna.domain.invoice.PaymentMode;
import com.krishna.domain.invoice.PaymentTerms;
import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.dto.ExcludePayrollDto;
import com.krishna.dto.InvoiceFilterDto;
import com.krishna.dto.UserIdsDto;
import com.krishna.enums.Months;
import com.krishna.repository.BuSpecificCostRepository;
import com.krishna.repository.IndirectCostRepository;
import com.krishna.repository.MarginBasisRepository;
import com.krishna.repository.SecurityDepositRepository;
import com.krishna.repository.invoice.InvoiceCycleRepository;
import com.krishna.repository.invoice.InvoiceStatusRepository;
import com.krishna.repository.invoice.PaymentModeRepository;
import com.krishna.repository.invoice.PaymentTermsRepository;
import com.krishna.repository.invoice.ProjectInvoiceRepository;
import com.krishna.repository.payroll.PayrollRepository;
import com.krishna.security.JwtValidator;
import com.krishna.service.invoice.InvoicePipelineService;
import com.krishna.service.util.UtilityService;
import com.krishna.util.ConstantUtility;

@Service
public class DeliveryTeamService {

	@Autowired
	FeignLegacyInterface feignLegacyInterface;

	@Autowired
	ProjectMarginService projectMarginService;

	@Autowired
	MarginBasisRepository marginBasisRepository;

	@Autowired
	UtilityService utilityService;

	@Autowired
	IndirectCostService indirectCostService;

	@Autowired
	ProjectInvoiceRepository projectInvoiceRepository;

	@Autowired
	InvoiceStatusRepository invoiceStatusRepository;

	@Autowired
	DollarCostServiceImpl dollarCostService;

	@Autowired
	JwtValidator validator;

	@Autowired
	ProjectInvoiceService projectInvoiceService;

	@Autowired
	SecurityDepositRepository securityDepositRepository;

	@Autowired
	SecurityDepositService securityDepositService;
	
	@Autowired
	UtilityService utilService;
	
	@Autowired
	LoginUtiltiyService loginUtilityService;
	
	@Autowired
	private InvoicePipelineService invoicePipelineService;
	
	@Autowired
	public PaymentModeRepository paymentModeRepository;

	@Autowired
	public InvoiceCycleRepository invoiceCycleRepository;

	@Autowired
	public PaymentTermsRepository paymentTermsRepository;
	
	@Autowired
	PayrollRepository payrollRepository;

	@Autowired
	IndirectCostRepository costRepository;
	
	@Autowired
	EntityManager entityManager;

	@Autowired
	BuSpecificCostRepository buSpecificCostRepository;

	Logger log=LoggerFactory.getLogger(DeliveryTeamService.class);

	
	public List<Object> getTeamHeadWiseProjectMargin(String accessToken,int billingRateFilter,Long teamHeadId, String bu, int year,
			int month) {
		List<Object> marginData = new ArrayList<>();
		Map<String, Double> gradeWiseCosts = null;
		Double buCost = 0D;
		Double hourlyIndirectCost = 0D;
		Double nonBillableIndirectCost = 0D;
		Map<String, Object> workingDaysData = (Map<String, Object>) feignLegacyInterface
				.getpayrollWorkingDays(accessToken, month, new Integer(year)).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);

		boolean isGradeWise = false;
		MarginBasis marginBasis = marginBasisRepository.findByMonthAndYear(month, year);
		if (marginBasis != null)
			isGradeWise = marginBasis.getIsGradeWise();
		if (isGradeWise) {
			Map<String, Object> userListAndCount = feignLegacyInterface.gradeWiseExpectedHours(month, year);
			gradeWiseCosts = utilityService.getGradeWiseCosts(month, year, accessToken, "", userListAndCount);
			buCost = indirectCostService.buIndirectCost(accessToken, bu, month, Integer.toString(year),gradeWiseCosts);
		} else {
			hourlyIndirectCost = indirectCostService.getIndirectCostHourly(accessToken, Integer.toString(year), bu,
					month);
			nonBillableIndirectCost = indirectCostService.getIndirectCostHourly(accessToken, Integer.toString(year), "",
					month);
		}
		List<ExcludePayrollDto> payrolls = new ArrayList<>();
		Map<String, Object> allCompensation = projectMarginService.getAllCompensation(month,year);
		
		
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		
		List<ProjectInvoice> overallInvoices = projectInvoiceRepository.findAllByMonthAndYearAndIsDeleted(monthName, Integer.toString(year), false);
		List<Long> ids=overallInvoices.stream().distinct().mapToLong(ProjectInvoice::getProjectId).boxed().collect(Collectors.toList());
		
		List<ProjectInvoice> buInvoices=new ArrayList<>();
		Map<String,Map<String,Object>> projectDatMap=new HashMap<>();
		List<Map<String,Object>> projectDetailsList  = feignLegacyInterface.findProjectDescriptionList(accessToken , ids, month,year);
		for (ProjectInvoice invoice : overallInvoices) {
			Long projectId=invoice.getProjectId();
			Map<String, Object> projectDetails = new HashMap<>();
			if(projectDatMap.containsKey(projectId.toString()))
				projectDetails =projectDatMap.get(projectId.toString());
			else {
				projectDetails = projectDetailsList.stream().filter(pr->pr.get("projectId").toString().equals(invoice.getProjectId().toString())).findFirst().orElse(null);
				projectDatMap.put(invoice.getProjectId().toString(), projectDetails);
			}
			String projectBusinessVertical = (String) projectDetails.get(ConstantUtility.BUSINESS_VERTICAL);
			if (projectBusinessVertical.equals(bu)) {
				buInvoices.add(invoice);
			}			
		}
		List<Long> buInvoiceIds=buInvoices.stream().distinct().mapToLong(ProjectInvoice::getProjectId).boxed().collect(Collectors.toList());
		log.info("invoiceProjects..."+buInvoiceIds);
		
		
		if (hourlyIndirectCost != 0.0 || gradeWiseCosts != null) {
			List<Map<String, Object>> projects = (List<Map<String, Object>>) feignLegacyInterface
					.getProjectListByTeamHeadAndBu(accessToken, teamHeadId, bu, year, month).get("data");
				for (int i = 0; i < projects.size(); i++) {
					Map<String, Object> projectData = projects.get(i);
					Map<String, Object> projectMap = projectMarginService.projectMap(projectData, bu, month, year,
							accessToken, hourlyIndirectCost, nonBillableIndirectCost, gradeWiseCosts, buCost,
							workingDays,marginBasis, payrolls,allCompensation,buInvoices);
					if (!projectMap.isEmpty())
						marginData.add(projectMap);
				}
		}
		
		return projectMarginService.applyAverageBillingRateFilter(billingRateFilter, marginData);		
		
	}

	public List<HashMap<String, Object>> getTeamHeadWiseDataLineChart(String accessToken, Long teamHeadId, String bu,
			String year) {
		List<String> months = Stream.of("January", "February", "March", "April", "May", "June", "July", "August",
				"September", "October", "November", "December").collect(Collectors.toList());
		ArrayList<HashMap<String, Object>> result = new ArrayList<>();
		double previousAmount = 0;
		List<Long> projectIds=new ArrayList<>();
		List<Map<String, Object>> projects = (List<Map<String, Object>>) feignLegacyInterface
				.getTeamHeadWiseProjectList(accessToken, teamHeadId, bu).get("data");
		for (Map<String, Object> project : projects) {
			projectIds.add(Long.parseLong(project.get("projectId").toString()));
		}
		List<ProjectInvoice> yearLyInvoices = projectInvoiceRepository
				.findAllByProjectIdInAndYearAndIsDeletedAndIsInternal(projectIds, year, false, false);
		String status1Name = invoiceStatusRepository.findById(1l).getStatusName();
		String status2Name = invoiceStatusRepository.findById(2l).getStatusName();
		String status3Name = invoiceStatusRepository.findById(3l).getStatusName();
		String status4Name = invoiceStatusRepository.findById(4l).getStatusName();
		String status5Name = invoiceStatusRepository.findById(5l).getStatusName();
		
		for (String month : months) {
			HashMap<String, Object> res = new HashMap<>();
			res.put("month", month);
			res.put(ConstantUtility.BUSINESS_VERTICAL, bu);
			List<ProjectInvoice> projectInvoices = 
					yearLyInvoices.stream().filter(invoice->invoice.getMonth().equals(month)).collect(toList());
				double status1Amount=projectInvoices.stream().filter(inv-> Long.toString(inv.getInvoiceStatus()).equals("1") ).collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
				double status2Amount=projectInvoices.stream().filter(inv-> Long.toString(inv.getInvoiceStatus()).equals("2") ).collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
				double status3Amount=projectInvoices.stream().filter(inv-> Long.toString(inv.getInvoiceStatus()).equals("3") ).collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
				double status4Amount=projectInvoices.stream().filter(inv-> Long.toString(inv.getInvoiceStatus()).equals("4") ).collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
				double status5Amount=projectInvoices.stream().filter(inv-> Long.toString(inv.getInvoiceStatus()).equals("5") ).collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
				double totalAmount = projectInvoices.stream().collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar));

				double differencePerc = 0;
				if (totalAmount != 0) {

					if (previousAmount != 0)
						differencePerc = ((totalAmount - previousAmount) / previousAmount) * 100;
					else
						differencePerc = ((totalAmount - previousAmount) / totalAmount) * 100;
				}
				previousAmount = totalAmount;
				res.put(status1Name, status1Amount);
				res.put(status2Name, status2Amount);
				res.put(status3Name, status3Amount);
				res.put(status4Name, status4Amount);
				res.put(status5Name, status5Amount);
				res.put("total", totalAmount);
				res.put("differencePerc", differencePerc);
				result.add(res);
		}
		return result;
	}

	public Map<String, Object> getTeamHeadWiseAverageDisputedInvoicePercentage(String accessToken, Long teamHeadId,
			String bu, String year) {
		Map<String, Object> response = new LinkedHashMap<>();
		Double totalAmmont = 0.0;
		Double disputedAmmout = 0.0;
		Double disputedPercentage = 0.0;
		Double totalAmmontLTM = 0.0;
		Double disputedAmmoutLTM = 0.0;
		Double disputedPercentageLTM = 0.0;
		List<Map<String, Object>> projects = (List<Map<String, Object>>) feignLegacyInterface
				.getTeamHeadWiseProjectList(accessToken, teamHeadId, bu).get("data");
		List<Long> projectList = projects.stream().map(pro ->Long.parseLong(pro.get("projectId").toString())).collect(Collectors.toList());
		List<ProjectInvoice> projectInvoiceList = projectInvoiceRepository.findAllByProjectIdInAndYearAndIsDeleted(projectList, year, false);
		List<ProjectInvoice> projectInvoiceListLtm = projectInvoiceRepository.findAllByProjectIdInAndYearAndIsDeleted(projectList, String.valueOf(Long.parseLong(year)-1), false);
		List<ProjectInvoice> totalInvoiceList = new ArrayList<>();
		List<ProjectInvoice> totalInvoices = new ArrayList<>();

		totalInvoiceList.addAll(projectInvoiceList);
		totalInvoiceList.addAll(projectInvoiceListLtm);
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(new DateTime().minusMonths(12).toDate());
		
		for(ProjectInvoice inv : totalInvoiceList){
			Calendar invCal = Calendar.getInstance();
			invCal.setTime(date);
			invCal.set(Calendar.YEAR,Integer.parseInt(inv.getYear()));
			invCal.set(Calendar.MONTH,Month.valueOf(inv.getMonth().toUpperCase()).getValue()-1);
			if(invCal.getTime().before(cal.getTime()) && invCal.getTime().after(cal1.getTime()) ){
				totalInvoices.add(inv);
			}
		}
		for (Map<String, Object> project : projects) {
			List<ProjectInvoice> projectInvoices = projectInvoiceRepository
					.findByProjectIdAndYearAndIsDeleted(
							Long.parseLong(project.get("projectId").toString()), year, false);
			for (ProjectInvoice invoice : totalInvoices) {
				if(invoice.getYear().equals(year)){
					totalAmmont = totalAmmont + invoice.getAmountInDollar();
					if (invoice.getInvoiceStatus() == 5)
						disputedAmmout = disputedAmmout + invoice.getAmountInDollar();
				}
				totalAmmontLTM = totalAmmontLTM + invoice.getAmountInDollar();
				if (invoice.getInvoiceStatus() == 5)
						disputedAmmoutLTM = disputedAmmoutLTM + invoice.getAmountInDollar();
			}
		}
		disputedPercentage = (disputedAmmout / totalAmmont) * 100;
		disputedPercentageLTM = (disputedAmmoutLTM / totalAmmontLTM) * 100;
		response.put("averageDisputedPercentage", Math.round(disputedPercentage * 100.0) / 100.0);
		response.put("averageDisputedPercentageLTM", Math.round(disputedPercentageLTM * 100.0) / 100.0);

		Double dollarexchangeCost = dollarCostService.getAverageDollarCost(
				LocalDateTime.now().minusMonths(1).getMonthValue(), LocalDateTime.now().minusMonths(1).getYear());
		response.put("disputedAmount", disputedAmmout * dollarexchangeCost);
		response.put("disputedAmountInDollars", disputedAmmout);
		response.put("disputedAmountLTM", disputedAmmoutLTM * dollarexchangeCost);
		response.put("disputedAmountInDollarsLTM", disputedAmmoutLTM);
		return response;

	}

	public List<Map<String, Object>> getTeamHeadWiseProjects(String accessToken, Long teamHeadId, String bu) {
		List<Map<String, Object>> result = (List<Map<String, Object>>) feignLegacyInterface
				.getTeamHeadWiseProjectList(accessToken, teamHeadId, bu).get("data");
		return result;

	}

	public Map<String, Object> getInvoiceData(String authorization, String year, String month,
			List<Map<String, Object>> allProjectDetails, Long teamHeadId,String businessVertical,String currencyType) {
		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object> response = new HashMap<>();
		List<Map<String,Object>> allProjectData = getTeamHeadWiseProjects(authorization,teamHeadId,businessVertical);
		List<ProjectInvoice> projectInvoices = new ArrayList<>();
		allProjectData.forEach(project -> {
			if (month.equals("")) {
				projectInvoices .addAll(projectInvoiceRepository.findAllByProjectIdAndYearAndIsDeleted(Long.parseLong(project.get("projectId").toString()),year, false));
			} else {
				projectInvoices .addAll(projectInvoiceRepository.findByProjectIdAndMonthAndYearAndIsDeleted(Long.parseLong(project.get("projectId").toString()),month, year, false));
			}
		});
		List<Long> createdIdList = new ArrayList<>();
		projectInvoices.forEach(invoice -> {
			createdIdList.add(invoice.getCreatorId());
		});
		UserIdsDto uids = new UserIdsDto();
		uids.setUserIds(createdIdList);
		
		List<Map<String, Object>> userNameList=(List<Map<String, Object>>) feignLegacyInterface.getUserNameList(authorization, uids).get("data");
		List<InvoiceCycle> billingCycleList = invoiceCycleRepository.findAll();
		List<InvoiceStatus> invoiceStatusList = invoiceStatusRepository.findAll();
		List<PaymentMode> paymentModeList = paymentModeRepository.findAll();
		List<PaymentTerms> paymentTermsList = paymentTermsRepository.findAll();
		InvoiceFilterDto invoiceFilterDto=new InvoiceFilterDto();
		invoiceFilterDto.setMonth(month);
		invoiceFilterDto.setYear(year);
		invoiceFilterDto.setBusinessVertical(businessVertical);
		invoiceFilterDto.setCurrencyType(currencyType);
		projectInvoices.forEach(invoice -> {
			HashMap<String, Object> res = new HashMap<>();
			res = projectInvoiceService.getData(res, invoice, allProjectDetails,userNameList,paymentTermsList,paymentModeList,invoiceStatusList,billingCycleList,null);
			result.add(res);
		});
		Map<String, Object> widgetsData = projectInvoiceService.getWidgetsData(projectInvoices,invoiceFilterDto);
		response.put("invoiceList", result);
		response.put("totalCount", result.size());
		response.put("widgetList", widgetsData);

		return response;
	}

	public Map<String, Object> getFilterWiseInvoiceData(String authorization, InvoiceFilterDto invoiceFilterDto, List<Map<String, Object>> allProjectDetails) {
		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object> response = new HashMap<>();
		List<Map<String,Object>> allProjectData = getTeamHeadWiseProjects(authorization,invoiceFilterDto.getTeamHeadId(),invoiceFilterDto.getBusinessVertical());
		List<ProjectInvoice> projectInvoices = new ArrayList<>();
		List<Long> ProjectIds = allProjectData.stream().map(project ->Long.parseLong(project.get("projectId").toString())).collect(Collectors.toList());
		projectInvoices = projectInvoiceRepository.findAllByProjectIdInDateRangeFilterIsDeleted(ProjectIds, new Date(invoiceFilterDto.getFromDate()), new Date(invoiceFilterDto.getToDate()), false);
		List<Long> createdIdList = new ArrayList<>();
		projectInvoices.forEach(invoice -> {
			createdIdList.add(invoice.getCreatorId());
		});
		UserIdsDto uids = new UserIdsDto();
		uids.setUserIds(createdIdList);
		
		List<Map<String, Object>> userNameList=(List<Map<String, Object>>) feignLegacyInterface.getUserNameList(authorization, uids).get("data");
		List<InvoiceCycle> billingCycleList = invoiceCycleRepository.findAll();
		List<InvoiceStatus> invoiceStatusList = invoiceStatusRepository.findAll();
		List<PaymentMode> paymentModeList = paymentModeRepository.findAll();
		List<PaymentTerms> paymentTermsList = paymentTermsRepository.findAll();

		projectInvoices.forEach(invoice -> {
			HashMap<String, Object> res = new HashMap<>();
			res = projectInvoiceService.getData(res, invoice, allProjectDetails,userNameList,paymentTermsList,paymentModeList,invoiceStatusList,billingCycleList,null);
			result.add(res);
		});
		Map<String, Object> widgetsData = projectInvoiceService.getWidgetsData(projectInvoices,invoiceFilterDto);
		response.put("invoiceList", result);
		response.put("totalCount", result.size());
		response.put("widgetList", widgetsData);

		return response;
	}

	
	public Map<String, Object> getTeamHeadWiseIfsdData(String accessToken,  Long teamHeadId, String businessVertical, Integer year) {
		List<Object> result=new ArrayList<>();
		List<Map<String,Object>>projects=(List<Map<String, Object>>) feignLegacyInterface.getTeamHeadWiseProjectList(accessToken, teamHeadId, businessVertical).get("data");
		List<Long> projectIds=new ArrayList<>();
		List<Long> leadIds = new ArrayList<>();
		projects.forEach(project->{
			Long projectId=Long.parseLong(project.get("projectId").toString()) ;
			if(!project.get("leadId").equals("NA")) {
				Long leadId = Long.parseLong(project.get("leadId").toString()) ;
				leadIds.add(leadId);
			}
			projectIds.add(projectId);
		});
		Map<String, Object> listAllUsers = (Map<String, Object>) feignLegacyInterface.getUserNameAndDob(accessToken);
		List<Map<String, Object>> userInfo = (List<Map<String, Object>>) listAllUsers.get(ConstantUtility.DATA_);
		List<SecurityDeposit> security = new ArrayList<>();
		if(year!=null){
			 security = !leadIds.isEmpty()?securityDepositRepository.findAllByLeadIdInAndYearAndIsDeleted(leadIds, year, false): Collections.EMPTY_LIST;
		}else {
			 security = !leadIds.isEmpty()?securityDepositRepository.findAllByLeadIdInAndIsDeleted(leadIds, false): Collections.EMPTY_LIST;		
		}
		AtomicDouble paidAmount=new AtomicDouble(0);
		AtomicDouble unpaidAmount=new AtomicDouble(0);
		AtomicDouble totalAmount=new AtomicDouble(0);
		AtomicDouble adjustedAmount=new AtomicDouble(0);
		AtomicDouble availableAmount=new AtomicDouble(0);
		AtomicDouble adjustedAmountInDoller=new AtomicDouble(0);
		List<Map<String, Object>> allProjectData = projectInvoiceService.getProjectDetails(accessToken).stream().filter(e->projectIds.contains(Long.parseLong(e.get("id").toString()))).collect(Collectors.toList());
		Query query = entityManager.createNativeQuery("select * from project_invoice where security_deposite_id is not null", ProjectInvoice.class);
		List<ProjectInvoice> invoiceData = query.getResultList();
		security.stream().forEach(securityDeposit->{
			Map<String, Object> projectData = allProjectData.stream().filter(project -> (project.get("leadId")!=null ? project.get("leadId").toString().equals(String.valueOf(securityDeposit.getLeadId())) : false || project.get("id").toString().equals(String.valueOf(securityDeposit.getProjectId())))).findAny().orElse(null);
			Map<String, Object> createdBy = userInfo.stream().filter(userdata -> userdata.get("userId").toString().equals(String.valueOf(securityDeposit.getCreatedBy()))).findAny().orElse(null);
			Map<String, Object> manager = userInfo.stream().filter(userdata -> userdata.get("userId").toString().equals(String.valueOf(securityDeposit.getManagerId()))).findAny().orElse(null);
			Map<String, Object> response = securityDepositService.getData(securityDeposit, businessVertical, projectData, manager, createdBy, invoiceData);
			if (!response.isEmpty()) {
				result.add(response);
				Double ifsdAmount=Double.parseDouble(response.get("amountInDollar").toString());
				Double adjustedValue=Double.parseDouble(response.get("adjustmentSum").toString());
				adjustedAmount.getAndAdd(adjustedValue);
				totalAmount.getAndAdd(ifsdAmount);
				adjustedAmountInDoller.getAndAdd(Double.parseDouble(response.get("adjustmentAmountInDoller").toString()));

				availableAmount.getAndAdd(ifsdAmount-adjustedValue);
				if(response.get("ifsdStatus").toString().equals("UNPAID"))
					unpaidAmount.getAndAdd(ifsdAmount);
				else
					paidAmount.getAndAdd(ifsdAmount);
			}
			});
		Map<String, Object> resultMap=new HashMap<>();
		resultMap.put("ifsdData", result);
		resultMap.put("totalAmount",Double.parseDouble(new DecimalFormat("#.##").format(totalAmount)));
		resultMap.put("unpaidAmount", unpaidAmount);
		resultMap.put("paidAmount", paidAmount);
		resultMap.put("availableAmount", availableAmount);
		resultMap.put("adjustedAmount", adjustedAmount);
		resultMap.put("adjustedAmountInDoller", adjustedAmountInDoller);
		return resultMap;
	}

	public Map<String, Object> getInvoiceTrends(String authorization, Long teamHeadId, String businessVertical,
			int month, String year) throws NumberFormatException, Exception {
		List<Map<String, Object>> projects = (List<Map<String, Object>>) feignLegacyInterface
				.getTeamHeadWiseProjectList(authorization, teamHeadId, businessVertical).get("data");
		List<Long> projectIds = new ArrayList<>();
		projects.forEach(project -> {
			Long projectId = Long.parseLong(project.get("projectId").toString());
			projectIds.add(projectId);
		});
		String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
		
		String previousMonth = Month.of(month).minus(1).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
		double ammountsInDoller = 0;
		double previousMonthAmmount = 0;
		double trends = 0;
		double trendsAmount = 0;
		List<ProjectTrendsDTO> projectTrends = new ArrayList<ProjectTrendsDTO>();

		for (Long projectId : projectIds) {
			ProjectTrendsDTO trend = projectInvoiceService.trendsOnProjectBasis(projectId, businessVertical, monthName,
					previousMonth, Long.parseLong(year));

			ammountsInDoller += trend.getProvidedMonthTotalAmmount();
			previousMonthAmmount += trend.getPreviousMonthTotalAmmount();
			if(!trend.getProjectName().equals(""))
				projectTrends.add(trend);
		}
		Collections.sort(projectTrends, new SortByTrend());
		if (previousMonthAmmount != 0) {
			trendsAmount = ammountsInDoller - previousMonthAmmount;
			trends = ((ammountsInDoller - previousMonthAmmount) / previousMonthAmmount) * 100;
			trends = (double) Math.round(trends * 100) / 100;
		} else {
			trendsAmount = ammountsInDoller - previousMonthAmmount;
			trends = 100D;
		}
		Map<String, Object> response = new HashMap<>();
		response.put(ConstantUtility.BUSINESS_VERTICAL, businessVertical);
		response.put("overallTotalAmmount", (double) Math.round(ammountsInDoller * 100) / 100);
		response.put("overallpreviousMonthTotalAmmount", (double) Math.round(previousMonthAmmount * 100) / 100);
		response.put("overAlltrend", trends + "%");
		response.put("projectWiseTrends", projectTrends);
		response.put("overallTrendAmount", Math.round(trendsAmount * 100.0) / 100.0);
		return response;
	}

	public List<Object> getInvoicePipeline(String accessToken,int month, int year, String projectType, String businessVertical,
			Long teamHeadId) {
		Map<String,Object> dataMap = feignLegacyInterface
				.getDeliveryTeamInvoicePipeline(accessToken,month, year, projectType, teamHeadId, businessVertical);
		List<Map<String,Object>> projectList=new ArrayList<>();
		if(dataMap!=null && dataMap.get("data")!=null)
			projectList = (List<Map<String, Object>>) dataMap.get("data");
		int projectListSize = projectList.size();
		List<Object> resultProjectList = new ArrayList<>();
		Map<String, Double> billingRateMap = invoicePipelineService.getBillingRates(month, year);
		for (int i = 0; i < projectListSize; i++) {
			Map<String, Object> projectData = (Map<String, Object>) projectList.get(i);
			projectData = invoicePipelineService.preparePipelineResponse(projectData, businessVertical, billingRateMap, month, year);
			if(projectData.containsKey("expectedBilling"))
				resultProjectList.add(projectData);
		}
		return resultProjectList;
	}

	public Map<String, Object> getTotalMargin(int month, String year, String businessVertical, Long teamHeadId,String accessToken) {
		Map<String, Object> buMargin = new HashMap<>();
			
		Double invoiceTotalAmount = 0.0;
		Double totalSpecialAllowance = 0.0;
		Double totalVoluntaryPayAmount = 0.0;	
		Double invoiceTotalAmountInRupee = 0.0;
		Double totalProjectCost = 0.0;
		Double totalMargin = 0.0;
		Double totalMarginPerc = 0.0;
		Double paymentChargesSum = 0.0;
		Double billableDirectCost = 0.0;
		Double nonBillableIC = 0.0;
		Double billableIC = 0.0;
		Double disputedAmount = 0.0;
		Double totalHours=0D;
		List<Object> billableDirectProjects = new ArrayList<>();
		List<Object> billableIndirectProjects = new ArrayList<>();
		List<Object> nonbillableProjects = new ArrayList<>();
		Map<String, Double> gradeWiseCosts = null;
		Double hourlyIndirectCost=0D;
		Double nonBillableIndirectCost=0D;
		Double buCost=0.0;
		Double internalInvoices =0D;
		Double overallInvoices = 0D;
		Double overallInvoicesInRupee = 0D;
		Double yearlyDisputedAmount=0D;
		Double yearlyTotalAmount=0D;
		Double excludedAmount=0.0D;
		boolean isGradeWise=false;
		boolean isLTM=false;
		Map<String, Object> workingDaysData = (Map<String, Object>)feignLegacyInterface.getpayrollWorkingDays(accessToken,month, new Integer(year)).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		MarginBasis marginBasis=marginBasisRepository.findByMonthAndYear(month, Integer.parseInt(year));
		if(marginBasis!=null){
			isGradeWise = marginBasis.getIsGradeWise();
			isLTM = marginBasis.getIsLTM();
		}
		if(isGradeWise) {
			Map<String, Object> userListAndCount = feignLegacyInterface.gradeWiseExpectedHours(month, Integer.parseInt(year));
			gradeWiseCosts = utilService.getGradeWiseCosts(month, Integer.parseInt(year), accessToken, "",userListAndCount);
			buCost = indirectCostService.buIndirectCost(accessToken, businessVertical, month, year,gradeWiseCosts);
		}
		else {
			hourlyIndirectCost = indirectCostService.getIndirectCostHourly(accessToken, year, businessVertical, month);
			nonBillableIndirectCost = indirectCostService.getIndirectCostHourly(accessToken, year, "", month);
		}
		List<Map<String,Object>> allProjectData = projectInvoiceService.getProjectDetails(accessToken);
		List<ExcludePayrollDto> payrolls = new ArrayList<>();
		Map<String, Object> allCompensation = projectMarginService.getAllCompensation(month, Integer.parseInt(year));
		Map<String,Object> voluntaryPayData = (Map<String, Object>) allCompensation.get("voluntaryPayResponse");
		Map<String,Object> specialAllowanceData = (Map<String, Object>) allCompensation.get("specialAmountResponse");
		Map<String,Object> payDaysData =  (Map<String, Object>) allCompensation.get("PayDaysResponse");
		
		Double dollarexchangeCost = dollarCostService.getAverageDollarCost(month, Integer.parseInt(year));
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		List<Map<String, Object>> projects = (List<Map<String, Object>>) feignLegacyInterface
				.getProjectListByTeamHeadAndBu(accessToken, teamHeadId, businessVertical,  Integer.parseInt(year), month).get("data");
		List<Long> projectIds=projects.stream().map(pro->Long.parseLong(pro.get(ConstantUtility.PROJECT_ID).toString())).collect(Collectors.toList());
		List<ProjectInvoice> yearlyInvoice = new ArrayList<>();
		if(businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT))
			yearlyInvoice = projectInvoiceRepository.findAllByYearAndIsDeletedAndIsInternal(year.toString(),
				false, false);
		else
			yearlyInvoice = projectInvoiceRepository.findAllByYearAndIsDeleted(year.toString(),
				false);
		yearlyDisputedAmount = yearlyInvoice.stream()
	            .filter(inv -> projectIds.contains(Long.valueOf(inv.getProjectId().toString())) && Long.toString(inv.getInvoiceStatus()).equals("5"))
	            .collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
		 yearlyTotalAmount = yearlyInvoice.stream()
	            .filter(inv -> projectIds.contains(Long.valueOf(inv.getProjectId().toString())))
	            .collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
		
		if (nonBillableIndirectCost != 0.0 || gradeWiseCosts!=null) {
			List<ProjectInvoice> monthlyInvoices = new ArrayList<>();
			if(businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT))
				monthlyInvoices = projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndIsInternalAndProjectIdIn(monthName, year, false,false,projectIds);
			else
				monthlyInvoices = projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndProjectIdIn(monthName, year, false,projectIds);
			List<Long> invoiceIds = new ArrayList<>();
			if (!monthlyInvoices.isEmpty()) {
				for (ProjectInvoice invoice : monthlyInvoices) {
					Map<String, Object> projectDetails = projectInvoiceService.getProjectDetail(allProjectData, invoice.getProjectId());
					String projectBusinessVertical ="UnAssigned";;
					if(projectDetails!=null)
						projectBusinessVertical = (String) projectDetails.get(ConstantUtility.BUSINESS_VERTICAL);
					
					if (!businessVertical.equals("")) {
						if (projectBusinessVertical.equals(businessVertical)) {
							Double paymentChargesDollar=0D;
							Double paymentChargesRupee=0D;
							if(invoice.getPaymentCharges()!=null && invoice.getInvoiceStatus()!=6) {
								paymentChargesDollar=projectInvoiceService.getPaymentCharges("DOLLAR", invoice);
								paymentChargesRupee=projectInvoiceService.getPaymentCharges("RUPEE", invoice);
							}
							paymentChargesSum=paymentChargesSum+paymentChargesRupee;
							if(invoice.getInvoiceStatus()!=6 || invoice.getInvoiceStatus()!=7) {
								invoiceTotalAmount = invoiceTotalAmount +invoice.getAmountInDollar();
								invoiceTotalAmountInRupee = invoiceTotalAmountInRupee + ((invoice.getAmountInDollar() * dollarexchangeCost));
							}
							if (invoice.getInvoiceStatus() == 5L)
								disputedAmount = disputedAmount + invoice.getAmountInDollar();
							if (invoiceIds.isEmpty() || !invoiceIds.contains(Long.valueOf(invoice.getProjectId()))) {
								invoiceIds.add(Long.valueOf(invoice.getProjectId()));
							}
						}
					}
				}
			}
			if (!projects.isEmpty()) {
				for (int i = 0; i < projects.size(); i++) {
					Map<String, Object> projectMap = new HashMap<>();
					Map<String, Object> projectData = loginUtilityService.objectToMapConverter(projects.get(i));
					Long projectId = Long.parseLong(projectData.get(ConstantUtility.PROJECT_ID).toString());
					String projectName = (String) projectData.get(ConstantUtility.PROJECT_NAME);
					Double projectCost = 0.0;
					Double directCost = 0.0;
					Double indirectCost = 0.0;
					if (invoiceIds.contains(projectId)) {
						projectMap = projectMarginService.getProjectCost(projectData, month, Integer.parseInt(year), accessToken, projectMap,
								hourlyIndirectCost,gradeWiseCosts,buCost,workingDays,marginBasis, payrolls,voluntaryPayData,specialAllowanceData,payDaysData);
						indirectCost = (Double) projectMap.get(ConstantUtility.INDIRECT_COST);
						billableIC = billableIC + indirectCost;
						Double totalBuCost = (Double) projectMap.get(ConstantUtility.TOTAL_BU_COST);
						Double totalGradeCost = Double.valueOf((projectMap.get(ConstantUtility.TOTAL_GRADE_COST).toString()));
						billableIndirectProjects = projectMarginService.getProjectMap(indirectCost, billableIndirectProjects, projectName,totalBuCost,totalGradeCost);
						directCost = (Double) projectMap.get(ConstantUtility.DIRECT_COST);
						billableDirectCost = billableDirectCost + directCost;
						billableDirectProjects =projectMarginService.getProjectsMap(directCost, billableDirectProjects, projectName);
					} else {
						if(!invoiceIds.isEmpty()) {
							double nonbillableIc=0;
							projectMap = projectMarginService.getProjectCost(projectData, month, Integer.parseInt(year), accessToken, projectMap,
								nonBillableIndirectCost,null,nonbillableIc,workingDays,marginBasis,payrolls,voluntaryPayData,specialAllowanceData,payDaysData);
						}
						else
							projectMap = projectMarginService.getProjectCost(projectData, month, Integer.parseInt(year), accessToken, projectMap,
									hourlyIndirectCost,gradeWiseCosts,buCost,workingDays,marginBasis, payrolls,voluntaryPayData,specialAllowanceData,payDaysData);
						indirectCost = (Double) projectMap.get(ConstantUtility.INDIRECT_COST);
						nonBillableIC = nonBillableIC + indirectCost;
						nonbillableProjects = projectMarginService.getProjectsMap(indirectCost, nonbillableProjects, projectName);
					}
					projectCost = indirectCost + directCost;
					totalProjectCost = totalProjectCost + projectCost;
					totalHours=projectMarginService.getExpectedHours(new Double(projectMap.get(ConstantUtility.EXPECTED_HOURS).toString()), totalHours);
					totalSpecialAllowance = totalSpecialAllowance+(Double) projectMap.get("specialAllowanceAmount");
					totalVoluntaryPayAmount = totalVoluntaryPayAmount + (Double)projectMap.get("voluntaryPayAmount");	
					excludedAmount=excludedAmount+Double.parseDouble(projectMap.get(ConstantUtility.EXCLUDED_AMOUNT).toString());
				}
			}
			overallInvoices = invoiceTotalAmount + internalInvoices;
			overallInvoicesInRupee = invoiceTotalAmountInRupee + (internalInvoices * dollarexchangeCost);
			
		}
		Map<String,Object> internalMap = new HashMap<>();
		Double receivedInternalInvoices = 0D;
		Double receivedInternalInRupee = 0D;

		Map<String,Object> budetails=utilService.getBusinessVerticalDetails(accessToken, businessVertical);
		internalMap = projectMarginService.getInternalInvoices(internalMap, String.valueOf(month), year, new Long(budetails.get("id").toString()), dollarexchangeCost);
		
		receivedInternalInvoices = Double.parseDouble(internalMap.get("receivedinternalInvoices").toString());
		receivedInternalInRupee = Double.parseDouble(internalMap.get("receivedinternalInRupee").toString());

		if(!buMargin.containsKey("receivedinternalInvoices")) {
			buMargin.put("receivedinternalInvoices",0D);
			buMargin.put("receivedinternalInRupee", 0D);
			buMargin.put("paidInternalInvoices", 0D);
			buMargin.put("paidInternalInRupee", 0D);
			buMargin.put("internalInvoices", 0D);
			buMargin.put("internalInvoicesInRupee", 0D);
		}
			buMargin.put(ConstantUtility.DOLLAR_EXCHANGE_VALUE, dollarexchangeCost);
			buMargin.put("overallInvoices", overallInvoices);
			buMargin.put("overallInvoicesInRupee", overallInvoicesInRupee);
			buMargin.put(ConstantUtility.INVOICE_AMOUNT, Math.round((invoiceTotalAmount - receivedInternalInvoices) * 100.0) / 100.0);			buMargin.put("totalProjectCostExcPaymentCharges", totalProjectCost);
			buMargin.put("paymentChargesAmt", paymentChargesSum);
			buMargin.put("specialAllowanceAmount", totalSpecialAllowance);
			buMargin.put("voluntaryPayAmount", totalVoluntaryPayAmount);
			buMargin.put("invoiceAmountInRupee", Math.round((invoiceTotalAmountInRupee -receivedInternalInRupee) * 100.0) / 100.0);			buMargin.put(ConstantUtility.EXCLUDED_AMOUNT,excludedAmount);
			buMargin.put("totalProjectCost", totalProjectCost+paymentChargesSum+totalSpecialAllowance+totalVoluntaryPayAmount);
			Double buSize=0D;
			if(new Double(workingDays.toString())!=0 && totalHours!=0)
				buSize=(totalHours/(new Double(workingDays.toString())*8));
			buMargin.put("buAvergeGic",0);
			if(buSize!=0)
				buMargin.put("buAvergeGic", Math.round(((billableIC+nonBillableIC)/buSize)*100.00)/100.00);
			buMargin.put("buSize", Math.round(buSize*100.00)/100.00);
			buMargin.put("totalIndirectCost", Math.round((billableIC+nonBillableIC)*100.00)/100.00);
			buMargin.put("billableIC", billableIC);
			buMargin.put("excludedAmount", excludedAmount);
			buMargin.put("billableICProjects", billableIndirectProjects);
			buMargin.put("billableDirectCost", billableDirectCost);
			buMargin.put("billableDirectProjects", billableDirectProjects);
			buMargin.put("nonBillableIC", nonBillableIC);
			buMargin.put("nonBillableICProjects", nonbillableProjects);
			double disputedPercentageYearly = (yearlyDisputedAmount / yearlyTotalAmount) * 100;
			Double avgDisputedAmountInRupee=((disputedPercentageYearly*overallInvoicesInRupee)/100);
			buMargin.put("disputedAmount", yearlyDisputedAmount);
			buMargin.put("disputedAmountRupee", avgDisputedAmountInRupee);
			buMargin.put("disputedPerc", Math.round(disputedPercentageYearly * 100.0) / 100.0);
			totalMargin = (overallInvoicesInRupee - (totalProjectCost+paymentChargesSum+totalSpecialAllowance+totalVoluntaryPayAmount));
			buMargin.put("totalMargin", Math.round(totalMargin * 100.0) / 100.0);
			if (overallInvoicesInRupee != 0.0)
				totalMarginPerc = (totalMargin * 100) / overallInvoicesInRupee;
			buMargin.put("totalMarginPerc", Math.round(totalMarginPerc * 100.0) / 100.0);
			buMargin.put("isLTM", isLTM);
			return buMargin;
	}
	public Map<String, Object> getCumulativeCost(Integer month, String year, String businessVertical,
			List<Map<String, Object>> deliveryTeam, String accessToken) {
		Map<String, Object> cumulativeResponse = new HashMap<>();
			Double hourlyIndirectCost = 0D;
		Double nonBillableIndirectCost = 0D;
		Map<String, Double> gradeWiseCosts = null;
		Double buCost = 0.0;

		boolean isGradeWise = false;
		Map<String, Object> workingDaysData = (Map<String, Object>) feignLegacyInterface
				.getpayrollWorkingDays(accessToken, month, new Integer(year)).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		MarginBasis marginBasis = marginBasisRepository.findByMonthAndYear(month, Integer.parseInt(year));
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		IndirectCost monthIndirectCost = costRepository.findByYearAndIsDeletedAndMonth(year,false, Months.valueOf(monthName.toUpperCase()));
		if(monthIndirectCost == null)
		log.info( " indirect cost is null ");
				//return null;

		if (marginBasis != null)
			isGradeWise = marginBasis.getIsGradeWise();
		if (isGradeWise) {
			Map<String, Object> userListAndCount = feignLegacyInterface.gradeWiseExpectedHours(month, Integer.parseInt(year));
			gradeWiseCosts = utilService.getGradeWiseCosts(month, Integer.parseInt(year), accessToken, "", userListAndCount);
			buCost = indirectCostService.buIndirectCost(accessToken, businessVertical, month, year,gradeWiseCosts);
		} else {
			hourlyIndirectCost = indirectCostService.getIndirectCostHourly(accessToken, year, businessVertical, month);
			nonBillableIndirectCost = indirectCostService.getIndirectCostHourly(accessToken, year, "", month);
		}
		Double dollarexchangeCost = dollarCostService.getAverageDollarCost(month, Integer.parseInt(year));
		
		List<Map<String, Object>> allProjectData = projectInvoiceService.getProjectDetails(accessToken);

		List<Object> responseList = new ArrayList<>();
		Double totalRevenueInDollar = 0D;
		Double totalRevenueInRupee = 0D;
		Double totalCost = 0D;
		Double effectiveDollarCost = 0D;
		Double margin = 0D;
		Double marginPerc = 0D;
		Double totalExcludedAmount=0.0D;				
		Map<String, Object> buMap = new HashMap<>();
		if (month != null && year != null) {
			buMap = (Map<String, Object>) feignLegacyInterface
					.monthwiseProjectBUData(accessToken, month, Integer.parseInt(year)).get("data");
		}
		AtomicReference<Map<String, Object>> value = new AtomicReference<>(buMap);
		
		List<ProjectInvoice> overallYearlyInvoices = new ArrayList<>();
		if(businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT))
			overallYearlyInvoices = projectInvoiceRepository.findAllByYearAndIsDeletedAndIsInternal(year,false, false);
		else
			overallYearlyInvoices = projectInvoiceRepository.findAllByYearAndIsDeleted(year, false);
		List<ProjectInvoice> overallPreviousYrInvoices = new ArrayList<>();
		overallPreviousYrInvoices = projectInvoiceRepository.findAllByYearAndIsDeletedAndIsInternalAndInvoiceStatusNot(String.valueOf((Integer.parseInt(year)-1)),false, false,6L);
		List<ProjectInvoice> totalYearlyInvoices = new ArrayList<>();
		totalYearlyInvoices.addAll(overallPreviousYrInvoices);
		List<ProjectInvoice> overallExtInvoices = overallYearlyInvoices.stream().filter(inv-> inv.getIsInternal().equals(false) && !inv.getInvoiceStatus().equals(6L)).collect(Collectors.toList());
		totalYearlyInvoices.addAll(overallExtInvoices);

		List<ProjectInvoice> overallMonthlyInvoices = overallYearlyInvoices.stream()
				.filter(pro -> pro.getMonth().equals(monthName)).collect(Collectors.toList());
		List<ProjectInvoice> businessVerticalInvoices = new ArrayList<>();
		overallMonthlyInvoices.stream().forEach(invoice -> {
			String projectBusinessVertical = "UnAssigned";
			if (value != null && value.get() != null && value.get().containsKey(invoice.getProjectId().toString())) {
				projectBusinessVertical = value.get().get(invoice.getProjectId().toString()).toString();
			} else {
				Map<String, Object> projectDetails = projectInvoiceService.getProjectDetail(allProjectData,
						invoice.getProjectId());
				if (projectDetails != null)
					projectBusinessVertical = (String) projectDetails.get(ConstantUtility.BUSINESS_VERTICAL);
			}
			if (!businessVertical.equals("") && projectBusinessVertical.equals(businessVertical)) {
				businessVerticalInvoices.add(invoice);
			}
		});
		Double buTotalInvoiceInDollar = businessVerticalInvoices.stream().filter(inv->inv.getInvoiceStatus()!=6L)
				.collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
		Double buTotalInvoiceInRupee = (buTotalInvoiceInDollar * dollarexchangeCost);
		Map<String,Object> totalMap=new HashMap<>();
		Map<String,Object> budetails=utilService.getBusinessVerticalDetails(accessToken, businessVertical);
		totalMap = projectMarginService.getInternalInvoices(totalMap, monthName, year, new Long(budetails.get("id").toString()),
				dollarexchangeCost);
		List<ProjectInvoice> receivedInternalInvoicesList = new ArrayList<>();
		if(businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT)){
			
			receivedInternalInvoicesList=projectInvoiceRepository.findAllByRaisedToBuAndMonthAndYearAndIsDeletedAndIsInternal(new Long(budetails.get("id").toString()),monthName,year,false, true);
		}
		Date date1 = new Date();
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(date1);
		if(month != null){
			startCal.set(Calendar.MONTH, month-1);
		}
		startCal.set(Calendar.DAY_OF_MONTH, startCal.getActualMaximum(Calendar.DATE));
		Calendar prevCal = Calendar.getInstance();
		prevCal.setTime(startCal.getTime());
		prevCal.add(Calendar.MONTH, -11);
		prevCal.set(Calendar.DAY_OF_MONTH, 1);
		List<ProjectInvoice> ltmInvoices = new ArrayList<>();
		for(ProjectInvoice inv : totalYearlyInvoices){
			Calendar invCal = Calendar.getInstance();
			invCal.setTime(date1);
			invCal.set(Calendar.YEAR,Integer.parseInt(inv.getYear()));
			invCal.set(Calendar.MONTH,Month.valueOf(inv.getMonth().toUpperCase()).getValue()-1);
			invCal.set(Calendar.DAY_OF_MONTH, 15);
			if(invCal.getTime().before(startCal.getTime()) && invCal.getTime().after(prevCal.getTime()) ){
				ltmInvoices.add(inv);
			}
		}
		List<Long> deliveryTeamHeadIdList = deliveryTeam.stream().map(d-> Long.parseLong(d.get("teamHeadId").toString())).collect(Collectors.toList());

		Map<Long,Object> projectList = loginUtilityService.objectToLongMapConverter( feignLegacyInterface
					.getProjectListByTeamHeadListAndBu(accessToken, deliveryTeamHeadIdList, businessVertical,
							Integer.parseInt(year), month)
					.get("data"));
		

		
		for (int k = 0; k < deliveryTeam.size(); k++) {
			Double excludedAmount=0.0D;	
			Double invoiceTotalAmount = 0.0;
			Double invoiceTotalAmountInRupee = 0.0;
			Double totalProjectCost = 0.0;
			Double paymentChargesSum = 0.0;
			Double totalSpecialAllowance = 0.0;
			Double totalVoluntaryPayAmount =0.0;
			Double totalMargin = 0.0;
			Double totalMarginPerc = 0.0;
			Double billableDirectCost = 0.0;
			Double nonBillableIC = 0.0;
			Double billableIC = 0.0;
			Double disputedAmount = 0.0;
			Double totalHours = 0D;
			List<Object> billableDirectProjects = new ArrayList<>();
			List<Object> billableIndirectProjects = new ArrayList<>();
			List<Object> nonbillableProjects = new ArrayList<>();
			Double receivedinternalInvoices = 0D;
			Double overallInvoices = 0D;
			Double overallInvoicesInRupee = 0D;
			Double overallInvoicesLTM = 0D;
			Double overallInvoicesInRupeeLTM = 0D;
			Map<String, Object> buMargin = new HashMap<>();
			Long deliveryTeamId = Long.parseLong(deliveryTeam.get(k).get("teamHeadId").toString());
			
			
			List<Map<String, Object>> projects = (List<Map<String, Object>>)projectList.get(deliveryTeamId);
			


			List<Map<String, Object>> deliveryTeamProjects=(List<Map<String, Object>>) deliveryTeam.get(k).get("projects");
			List<Long> projectIds = deliveryTeamProjects.stream().map(pro -> Long.parseLong(pro.get(ConstantUtility.PROJECT_ID).toString()))
					.collect(Collectors.toList());
			List<ProjectInvoice> yearlyInvoices = overallYearlyInvoices.stream().filter(inv->projectIds.contains(inv.getProjectId())).collect(Collectors.toList());
			List<ProjectInvoice> ltmProjectWiseInvoice = ltmInvoices.stream().filter(inv->projectIds.contains(inv.getProjectId())).collect(Collectors.toList());


			List<ProjectInvoice> monthlyInvoices = businessVerticalInvoices.stream().filter(inv->projectIds.contains(inv.getProjectId())).collect(Collectors.toList());
			List<ProjectInvoice> monthlyInvoiceschecking = overallMonthlyInvoices.stream().filter(inv->projectIds.contains(inv.getProjectId())).collect(Collectors.toList());

			Double monthlyAmount=monthlyInvoiceschecking.stream().collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
			List<ProjectInvoice> monthlyInvoiceLTM = overallExtInvoices.stream().filter(inv->projectIds.contains(inv.getProjectId()) && inv.getMonth().equals(monthName)).collect(Collectors.toList());
			Double monthlyRevenueLTM = monthlyInvoiceLTM.stream().collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar));

			Double totalAmount=yearlyInvoices.stream().collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
			disputedAmount=yearlyInvoices.stream().filter(pro->pro.getInvoiceStatus()==5).collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
			Double totalAmountLTM=ltmProjectWiseInvoice.stream().collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
			Double disputedAmountLTM=ltmProjectWiseInvoice.stream().filter(pro->pro.getInvoiceStatus()==5).collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
	

			List<Long> invoiceIds = new ArrayList<>();
			if (!monthlyInvoices.isEmpty()) {
				for (ProjectInvoice invoice : monthlyInvoices) {
							Double paymentChargesDollar = 0D;
							Double paymentChargesRupee = 0D;
							if (invoice.getPaymentCharges() != null && invoice.getInvoiceStatus() != 6) {
								paymentChargesDollar = projectInvoiceService.getPaymentCharges("DOLLAR",
										invoice);
								paymentChargesRupee = projectInvoiceService.getPaymentCharges("RUPEE", invoice);
							}
							paymentChargesSum=paymentChargesSum+paymentChargesRupee;
							if (invoice.getInvoiceStatus() != 6) {
								invoiceTotalAmount = invoiceTotalAmount + invoice.getAmountInDollar();
								invoiceTotalAmountInRupee = invoiceTotalAmountInRupee + (invoice.getAmountInDollar() * dollarexchangeCost);
							}
							if (invoiceIds.isEmpty()
									|| !invoiceIds.contains(Long.valueOf(invoice.getProjectId()))) {
								invoiceIds.add(Long.valueOf(invoice.getProjectId()));
							}

				}
			}
			List<ExcludePayrollDto> payrolls = new ArrayList<>();
			Map<String, Object> allCompensation = projectMarginService.getAllCompensation(month, Integer.parseInt(year));
			Map<String,Object> voluntaryPayData = (Map<String, Object>) allCompensation.get("voluntaryPayResponse");
			Map<String,Object> specialAllowanceData = (Map<String, Object>) allCompensation.get("specialAmountResponse");
			Map<String,Object> payDaysData =  (Map<String, Object>) allCompensation.get("PayDaysResponse");
		
			if (nonBillableIndirectCost != 0.0 || gradeWiseCosts != null) {
					for (int i = 0; i < projects.size(); i++) {
						Map<String, Object> projectMap = new HashMap<>();
						Map<String, Object> projectData = loginUtilityService.objectToMapConverter(projects.get(i));
						Long projectId = Long.parseLong(projectData.get(ConstantUtility.PROJECT_ID).toString());
						String projectName = (String) projectData.get(ConstantUtility.PROJECT_NAME);
						Double projectCost = 0.0;
						Double directCost = 0.0;
						Double indirectCost = 0.0;
						Double projectSpecialAll = 0.0;	
						if (invoiceIds.contains(projectId)) {
							projectMap = projectMarginService.getProjectCost(projectData, month, Integer.parseInt(year),
									accessToken, projectMap, hourlyIndirectCost, gradeWiseCosts, buCost, workingDays, 
									marginBasis, payrolls,voluntaryPayData, specialAllowanceData,payDaysData);
							indirectCost = (Double) projectMap.get(ConstantUtility.INDIRECT_COST);
							billableIC = billableIC + indirectCost;
							Double totalBuCost = (Double) projectMap.get(ConstantUtility.TOTAL_BU_COST);
							Double totalGradeCost = Double.valueOf((projectMap.get(ConstantUtility.TOTAL_GRADE_COST).toString()));
							billableIndirectProjects =projectMarginService. getProjectMap(indirectCost, billableIndirectProjects, projectName,totalBuCost,totalGradeCost);

							
							directCost = (Double) projectMap.get(ConstantUtility.DIRECT_COST);
							billableDirectCost = billableDirectCost + directCost;
							billableDirectProjects = projectMarginService.getProjectsMap(directCost,
									billableDirectProjects, projectName);
						} else {
							if (!invoiceIds.isEmpty()) {
								double nonbillableIc = 0;
								if(businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT)){
								projectMap = projectMarginService.getProjectCost(projectData, month,
										Integer.parseInt(year), accessToken, projectMap, nonBillableIndirectCost,
										gradeWiseCosts, nonbillableIc, workingDays,marginBasis,payrolls,
										voluntaryPayData, specialAllowanceData,payDaysData);
								}
								else
								projectMap = projectMarginService.getProjectCost(projectData, month,
										Integer.parseInt(year), accessToken, projectMap, nonBillableIndirectCost,
										null, nonbillableIc, workingDays,marginBasis,payrolls,
										voluntaryPayData, specialAllowanceData,payDaysData);
						
							} else {
								if(businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT))

								projectMap = projectMarginService.getProjectCost(projectData, month,
										Integer.parseInt(year), accessToken, projectMap, hourlyIndirectCost,
										gradeWiseCosts, 0D/*buCost*/, workingDays,marginBasis,payrolls,
										voluntaryPayData, specialAllowanceData,payDaysData);
								else
								projectMap = projectMarginService.getProjectCost(projectData, month,
										Integer.parseInt(year), accessToken, projectMap, hourlyIndirectCost,
										null, 0D/*buCost*/, workingDays,marginBasis,payrolls,
										voluntaryPayData, specialAllowanceData,payDaysData);

							}
							indirectCost = (Double) projectMap.get(ConstantUtility.INDIRECT_COST);
							nonBillableIC = nonBillableIC + indirectCost;
							nonbillableProjects = projectMarginService.getProjectsMap(indirectCost, nonbillableProjects,
									projectName);
						}
						excludedAmount = excludedAmount+ Double.parseDouble(projectMap.get("excludedAmount").toString());
						totalExcludedAmount = totalExcludedAmount+Double.parseDouble(projectMap.get("excludedAmount").toString());
						projectCost = indirectCost + directCost;
						totalProjectCost = totalProjectCost + projectCost;
						totalHours = projectMarginService.getExpectedHours(
								new Double(projectMap.get(ConstantUtility.EXPECTED_HOURS).toString()), totalHours);
						totalSpecialAllowance = totalSpecialAllowance+(Double) projectMap.get("specialAllowanceAmount");
						totalVoluntaryPayAmount = totalVoluntaryPayAmount + (Double)projectMap.get("voluntaryPayAmount");	
					}
				}
				

			if(businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT)){
				List<ProjectInvoice> receivedInvoiceList = receivedInternalInvoicesList.stream().filter(inv->projectIds.contains(inv.getProjectId())).collect(Collectors.toList());
				receivedinternalInvoices = receivedInvoiceList.stream().collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar));
				overallInvoices = invoiceTotalAmount + receivedinternalInvoices;
				overallInvoicesInRupee = invoiceTotalAmountInRupee + (receivedinternalInvoices * dollarexchangeCost);
			}
			else{
				overallInvoices = invoiceTotalAmount;
				overallInvoicesInRupee = invoiceTotalAmountInRupee;
			}

			Double yearlyDisputedAmt = 0D;
			Double yearlyRevenueInr = 0D;
			Double yearlyDisputedAmtInr = 0D;


			for (ProjectInvoice inv : yearlyInvoices) {
				
					Calendar cal = Calendar.getInstance();
					try {
						Date date = new SimpleDateFormat("MMMM", Locale.ENGLISH).parse(inv.getMonth());
						cal.setTime(date);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


					Double dollarexCost = dollarCostService.getAverageDollarCost(cal.get(Calendar.MONTH) + 1,
							Integer.parseInt(year));
				if (inv.getInvoiceStatus()== 5) {
					yearlyDisputedAmtInr = yearlyDisputedAmtInr + inv.getAmountInDollar() * dollarexCost;
					yearlyDisputedAmt = yearlyDisputedAmt + inv.getAmountInDollar();
				}
				if (inv.getInvoiceStatus()!= 6L){
				yearlyRevenueInr = yearlyRevenueInr + inv.getAmountInDollar() * dollarexCost;
				}
				yearlyRevenueInr = yearlyRevenueInr + inv.getAmountInDollar() * dollarexCost;

			}
			totalProjectCost=totalProjectCost+paymentChargesSum+totalSpecialAllowance+totalVoluntaryPayAmount;
			if(monthIndirectCost == null){
				totalProjectCost = 0D;
			}
			buMargin.put(ConstantUtility.DOLLAR_EXCHANGE_VALUE, dollarexchangeCost);
			effectiveDollarCost=dollarexchangeCost;
			buMargin.put("overallInvoices", overallInvoices);
			totalRevenueInDollar=totalRevenueInDollar+overallInvoices;
			buMargin.put("overallInvoicesInRupee", overallInvoicesInRupee);
			totalRevenueInRupee=totalRevenueInRupee+overallInvoicesInRupee;
			buMargin.put("totalProjectCost", totalProjectCost);
			if(monthIndirectCost != null)
				buMargin.put("totalProjectCostExcPaymentCharges", totalProjectCost-paymentChargesSum);
			else
				buMargin.put("totalProjectCostExcPaymentCharges",0);
			buMargin.put("paymentChargesSum", paymentChargesSum);
			buMargin.put("yearlyRevenue", totalAmount);
			buMargin.put("yearlyRevenueInr", yearlyRevenueInr);
			buMargin.put("yearlyDisputedAmount", yearlyDisputedAmt);
			buMargin.put("yearlyDisputedAmtInr", yearlyDisputedAmtInr);





			totalCost=totalCost+totalProjectCost;
			Double disputedPerc=Math.round(((disputedAmount * 100.0) / totalAmount) * 100.0) / 100.0;
			Double disputedPercLTM=Math.round(((disputedAmountLTM * 100.0) / totalAmountLTM) * 100.0) / 100.0;
			Double disputedAmountValue=((disputedPerc*overallInvoices)/100);
			Double disputedAmountRupee=((disputedPerc*overallInvoicesInRupee)/100);
			Double disputedAmountValueLTM = ((disputedPercLTM* monthlyRevenueLTM)/100);
			Double disputedAmountRupeeLTM = ((disputedPercLTM* monthlyRevenueLTM * dollarexchangeCost)/100);
			buMargin.put("disputedAmount", disputedAmountValue);
			buMargin.put("disputedPerc", Math.round(((disputedAmount * 100.0) / totalAmount) * 100.0) / 100.0);
			buMargin.put("disputedAmountInRupees", disputedAmountRupee);
			buMargin.put("disputedAmountLTM", disputedAmountValueLTM);
			buMargin.put("disputedPercLTM", Math.round(disputedPercLTM * 100.0) / 100.0);
			buMargin.put("disputedAmountInRupeesLTM", disputedAmountRupeeLTM);
			buMargin.put("totalAmountLTM", totalAmountLTM);
			buMargin.put("billableIC", billableIC);
			buMargin.put("billableICProjects", billableIndirectProjects);
			buMargin.put("billableDirectCost", billableDirectCost);
			buMargin.put("specialAllowanceAmount", totalSpecialAllowance);
			buMargin.put("voluntaryPayAmount",totalVoluntaryPayAmount);
			buMargin.put("billableDirectProjects", billableDirectProjects);
			buMargin.put("nonBillableIC", nonBillableIC);
			buMargin.put("nonBillableICProjects", nonbillableProjects);
			buMargin.put("excludedAmount",excludedAmount);
			if(monthIndirectCost != null){
				buMargin.put("totalIndirectCost", Math.round((billableIC + nonBillableIC) * 100.00) / 100.00);
				totalMargin = (overallInvoicesInRupee - totalProjectCost);
				buMargin.put("totalMargin", Math.round(totalMargin * 100.0) / 100.0);
				if (overallInvoicesInRupee != 0.0)
					totalMarginPerc = (totalMargin * 100) / overallInvoicesInRupee;
				buMargin.put("totalMarginPerc", Math.round(totalMarginPerc * 100.0) / 100.0);
			}else {
				buMargin.put("totalIndirectCost", 0);
				buMargin.put("totalMargin", 0);
				buMargin.put("totalMarginPerc", 0);

			}
			buMargin.put("deliveryTeam", deliveryTeam.get(k).get("teamName").toString());
			responseList.add(buMargin);
		}
		
		Double internalInvoices = 0D;
		Double internalInvoicesInr = 0D;
		Double overallInvoices = totalRevenueInDollar;
		Double overallInvoicesInRupee = totalRevenueInRupee ;
		Double paidInternalInvoice = 0D;
		Double paidInternalInRupee = 0D;
		Double receivedInvoice = 0D;
		Double receivedInRupee = 0D;
		Double buSpCost = 0D;
		List<Double> buSpecificCosts = buSpecificCostRepository.findAmountByYearAndMonthAndBusinessVerticalAndDeleted(Integer.parseInt(year), month, businessVertical, false);
		buSpCost = buSpecificCosts.stream().collect(Collectors.summingDouble(val -> val));
		if (responseList.size() > 1) {
			internalInvoices = new Double(totalMap.get("internalInvoices").toString());
			internalInvoicesInr = (internalInvoices * dollarexchangeCost);
			paidInternalInvoice = new Double(totalMap.get("paidInternalInvoices").toString());
			paidInternalInRupee = new Double(totalMap.get("paidInternalInRupee").toString());
			
			if (!businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT)) {
				overallInvoices = overallInvoices - paidInternalInvoice;
				overallInvoicesInRupee = overallInvoicesInRupee - paidInternalInRupee;
			}
			else{
				receivedInvoice  = new Double(totalMap.get("receivedinternalInvoices").toString());
				receivedInRupee  = new Double(totalMap.get("receivedinternalInRupee").toString());
				overallInvoices = overallInvoices + internalInvoices;
				overallInvoicesInRupee = overallInvoicesInRupee + (internalInvoices * dollarexchangeCost);
			}
		}
		totalMap.put("excludedAmount", totalExcludedAmount);
		totalMap.put("internalInvoicesInr", internalInvoicesInr);
		totalMap.put("internalInvoices", internalInvoices);
		totalMap.put("openProjectsTotalRevenueInDollar", totalRevenueInDollar);
		totalMap.put("openProjectsTotalRevenueInRupeeOpen", totalRevenueInRupee);
		if (!businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT)) {
			totalMap.put("totalRevenueInDollar", buTotalInvoiceInDollar - receivedInvoice);
			totalMap.put("totalRevenueInRupee", buTotalInvoiceInRupee - receivedInRupee);
			totalMap.put("overallInvoices", buTotalInvoiceInDollar );
			totalMap.put("overallInvoicesInRupee", buTotalInvoiceInRupee);
			if(monthIndirectCost != null){
			totalMap.put("margin", ((buTotalInvoiceInRupee) - (totalCost + paidInternalInRupee)));
			if (totalRevenueInRupee != 0.0)
				totalMap.put("marginPerc", (((buTotalInvoiceInRupee ) - (totalCost + paidInternalInRupee)) * 100)
						/ (buTotalInvoiceInRupee));
			}
			else{
				totalMap.put("margin", 0);
				totalMap.put("marginPerc",0);
			}
		} else {
			totalMap.put("totalRevenueInDollar", buTotalInvoiceInDollar );
			totalMap.put("totalRevenueInRupee", buTotalInvoiceInRupee );
			totalMap.put("overallInvoices", buTotalInvoiceInDollar + receivedInvoice);
			totalMap.put("overallInvoicesInRupee", buTotalInvoiceInRupee + (receivedInvoice * dollarexchangeCost));
			if(monthIndirectCost != null){
			totalMap.put("margin", ((buTotalInvoiceInRupee + (receivedInvoice * dollarexchangeCost)) - (totalCost + paidInternalInRupee)));
			if (totalRevenueInRupee != 0.0)
				totalMap.put("marginPerc",
						(((buTotalInvoiceInRupee + (receivedInvoice * dollarexchangeCost)) - (totalCost + paidInternalInRupee)) * 100)
								/ (buTotalInvoiceInRupee + (receivedInvoice * dollarexchangeCost)));
			}
			else{
				totalMap.put("margin", 0);
				totalMap.put("marginPerc",0);
			}
		}
		totalMap.put("openProjectsOverallInvoiceInDollar", overallInvoices);
		totalMap.put("openProjectsOverallInvoicesInRupee", overallInvoicesInRupee);
		totalMap.put("buSpecificCost", buSpCost);
		if(monthIndirectCost != null){
			totalMap.put("totalCost", totalCost);
			totalMap.put("totalCostWithPaidRev", totalCost + paidInternalInRupee + buSpCost);
		}
		else{
			totalMap.put("totalCost", 0D);
			totalMap.put("totalCostWithPaidRev", 0D);

		}

		totalMap.put("effectiveDollarCost", effectiveDollarCost);
		totalMap.put("responseList", responseList);
		
			

		return totalMap;
	}

	public List<Map<String, Object>> getDeliveryHeadWiseYTD(String accessToken, String year,
			List<Map<String, Object>> deliveryTeamData) {
		List<Map<String,Object>> result=new ArrayList<>();
		List<ProjectInvoice> overallYearlyInvoices = projectInvoiceRepository
				.findAllByYearAndIsDeletedAndInvoiceStatusNot(year, false, 6L);
		for (Map<String, Object> delivery : deliveryTeamData) {
			Map<String, Object> response = new HashMap<>();
			Double totalAmount = 0.0;
			Double disputedAmount = 0.0;
			Double disputedPercentage = 0.0;
			List<Object> projects = (List<Object>) delivery.get("projects");
			for (Object proj : projects) {
				List<ProjectInvoice> projectInvoices = overallYearlyInvoices.stream()
						.filter(inv -> inv.getProjectId().toString().equals(proj.toString()))
						.collect(Collectors.toList());
				totalAmount = totalAmount + projectInvoices.stream()
						.collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
				disputedAmount = disputedAmount + projectInvoices.stream().filter(pro -> pro.getInvoiceStatus() == 5)
						.collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
			}
			disputedPercentage = (disputedAmount / totalAmount) * 100;
			response.put("averageDisputedPercentage", Math.round(disputedPercentage * 100.0) / 100.0);
			Double dollarexchangeCost = dollarCostService.getAverageDollarCost(
					LocalDateTime.now().minusMonths(1).getMonthValue(), LocalDateTime.now().minusMonths(1).getYear());
			response.put("disputedAmount", Math.round((disputedAmount*dollarexchangeCost)*100)/100);
			response.put("disputedAmountInDollars", Math.round(disputedAmount*100)/100 );
			response.put("totalAmountInRupee", Math.round((totalAmount*dollarexchangeCost)*100)/100);
			response.put("totalAmountInDollar",  Math.round(totalAmount*100)/100);
			response.put("deliveryTeamId", delivery.get("deliveryTeamId"));
			response.put("deliveryTeamName", delivery.get("deliveryTeamName"));
			response.put("teamHeadId", delivery.get("teamHeadId"));
			response.put("teamHeadName", delivery.get("teamHeadName"));
			result.add(response);
		}
		
		return result;
	}

	public List<Map<String,Object>> getYTDBifurcation(String accessToken, String bu,String year, Long teamHeadId, String month){
		List<Map<String,Object>> result = new ArrayList<>();
		List<Map<String, Object>> projectsDataList=(List<Map<String, Object>>) feignLegacyInterface.getDeliveryTeam(accessToken, bu, Integer.parseInt(month), Integer.parseInt(year)).get("data");
		List<Long> projectIdList = new ArrayList<>();
		List<Map<String,Object>> projectList = new ArrayList<>();

		for(Map<String, Object> val : projectsDataList){
			if(val.get("teamHeadId").toString().equals(teamHeadId.toString())){
				projectList = (List<Map<String, Object>>) val.get("projects");
				projectIdList = projectList.stream().map(pro -> Long.parseLong(pro.get(ConstantUtility.PROJECT_ID).toString())).collect(Collectors.toList());
				break;
			}
		}
		List<ProjectInvoice> overallYearlyInvoices = projectInvoiceRepository.findAllByYearAndIsDeletedAndInvoiceStatus(year, false, 5L);
		for(Long id : projectIdList){
			Double disputedAmount = 0D;
			Double disputedAmountInr = 0D;
			String projectName= null;
			Map<String,Object> res = new HashMap<>();
			List<ProjectInvoice> projectYearlyInvoice =  overallYearlyInvoices.stream().filter(inv-> inv.getProjectId().equals(id)).collect(Collectors.toList());
			for(ProjectInvoice inv : projectYearlyInvoice){
				Calendar cal = Calendar.getInstance();
				try {
					Date date = new SimpleDateFormat("MMMM", Locale.ENGLISH).parse(inv.getMonth());
					cal.setTime(date);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				Double dollarexchangeCost = dollarCostService.getAverageDollarCost(cal.get(Calendar.MONTH)+1, Integer.parseInt(year));
				disputedAmountInr = disputedAmountInr + inv.getAmountInDollar()*dollarexchangeCost;
				disputedAmount = disputedAmount + inv.getAmountInDollar();


			}
			for(Map<String,Object> map : projectList){
				if(map.get(ConstantUtility.PROJECT_ID).toString().equals(id.toString())){
					projectName = map.get(ConstantUtility.PROJECT_NAME).toString();
					break;
				}
			}
			if(!disputedAmount.equals(0D)){
				res.put("projectId", id);
				res.put("projectName", projectName);
				res.put("disputedAmount", disputedAmount);
				res.put("disputedAmountInr", disputedAmountInr);

				result.add(res);
			}
			
		}

		return result;
	}

	public List<Map<String,Object>> getLTMBifurcation(String accessToken, String bu,String year, Long teamHeadId, String month){
		List<Map<String,Object>> result = new ArrayList<>();
		List<Map<String, Object>> projectsDataList=(List<Map<String, Object>>) feignLegacyInterface.getDeliveryTeam(accessToken, bu, Integer.parseInt(month), Integer.parseInt(year)).get("data");
		List<Long> projectIdList = new ArrayList<>();
		List<Map<String,Object>> projectList = new ArrayList<>();
		for(Map<String, Object> val : projectsDataList){
			if(val.get("teamHeadId").toString().equals(teamHeadId.toString())){
				projectList = (List<Map<String, Object>>) val.get("projects");
				projectIdList = projectList.stream().map(pro -> Long.parseLong(pro.get(ConstantUtility.PROJECT_ID).toString())).collect(Collectors.toList());
				break;
			}
		}
		List<String> yearList = new ArrayList<>();
		yearList.add(year);
		yearList.add(String.valueOf(Integer.parseInt(year)-1));
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if(month != null){
			cal.set(Calendar.MONTH, Integer.parseInt(month));
		}
		cal.set(Calendar.YEAR, Integer.parseInt(year.toString()));
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DATE));
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(cal.getTime());
		cal1.add(Calendar.MONTH, -11);
		cal1.set(Calendar.DAY_OF_MONTH, 1);
		List<ProjectInvoice> overallYearlyInvoices = projectInvoiceRepository.findAllByIsDeletedAndIsInternalAndInvoiceStatusAndYearIn(false, false, 5L,yearList);
		List<ProjectInvoice> ltmInvoices = new ArrayList<>();
		for(ProjectInvoice inv : overallYearlyInvoices){
			Calendar invCal = Calendar.getInstance();
			invCal.setTime(date);
			invCal.set(Calendar.YEAR,Integer.parseInt(inv.getYear()));
			invCal.set(Calendar.MONTH,Month.valueOf(inv.getMonth().toUpperCase()).getValue()-1);
			invCal.set(Calendar.DAY_OF_MONTH, 15);
			if(invCal.getTime().before(cal.getTime()) && invCal.getTime().after(cal1.getTime()) ){
				ltmInvoices.add(inv);
			}
		}
		for(Long id : projectIdList){
			Double disputedAmountLTM = 0D;
			Double disputedAmountLTMInr = 0D;
			String projectName= null;
			Map<String,Object> res = new HashMap<>();
			List<ProjectInvoice> projectYearlyInvoice =  ltmInvoices.stream().filter(inv-> inv.getProjectId().equals(id)).collect(Collectors.toList());

			for(ProjectInvoice inv : projectYearlyInvoice){
				Calendar calInv = Calendar.getInstance();
				try {
					Date dateInv = new SimpleDateFormat("MMMM", Locale.ENGLISH).parse(inv.getMonth());
					calInv.setTime(dateInv);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				Double dollarexchangeCost = dollarCostService.getAverageDollarCost(calInv.get(Calendar.MONTH)+1, Integer.parseInt(year));
				disputedAmountLTMInr = disputedAmountLTMInr + inv.getAmountInDollar()*dollarexchangeCost;
				disputedAmountLTM = disputedAmountLTM + inv.getAmountInDollar();


			}
			for(Map<String,Object> map : projectList){
				if(map.get(ConstantUtility.PROJECT_ID).toString().equals(id.toString())){
					projectName = map.get(ConstantUtility.PROJECT_NAME).toString();
					break;
				}
			}
			if(!disputedAmountLTM.equals(0D)){
				res.put("projectId", id);
				res.put("projectName", projectName);
				res.put("disputedAmountLTM", disputedAmountLTM);
				res.put("disputedAmountLTMInr", disputedAmountLTMInr);

				result.add(res);
			}
			
		}

		return result;
	}


}