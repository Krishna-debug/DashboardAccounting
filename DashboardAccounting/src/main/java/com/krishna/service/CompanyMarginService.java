package com.krishna.service;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.krishna.Interfaces.ICompanyMarginService;
import com.krishna.controller.FeignLegacyInterface;
import com.krishna.domain.Arrear;
import com.krishna.domain.BuSpecificCost;
import com.krishna.domain.IndirectCost;
import com.krishna.domain.LeaveCostPercentage;
import com.krishna.domain.Payroll;
import com.krishna.domain.ReserveSnapShot;
import com.krishna.domain.Margin.BuReserve;
import com.krishna.domain.Margin.BuReserveDeductions;
import com.krishna.domain.Margin.MarginBasis;
import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.domain.variablePay.VariablePay;
import com.krishna.dto.DeductionResponseDto;
import com.krishna.dto.ExcludePayrollDto;
import com.krishna.enums.Months;
import com.krishna.repository.BuReserveDeductionRepository;
import com.krishna.repository.BuReserveRepository;
import com.krishna.repository.BuSpecificCostRepository;
import com.krishna.repository.IndirectCostRepository;
import com.krishna.repository.MarginBasisRepository;
import com.krishna.repository.ReserveSnapShotRepository;
import com.krishna.repository.invoice.ProjectInvoiceRepository;
import com.krishna.repository.payroll.ArrearRepository;
import com.krishna.repository.payroll.LeaveCostPercentageRepository;
import com.krishna.repository.payroll.PayrollRepository;
import com.krishna.repository.variablePay.VariablePayRepository;
import com.krishna.service.invoice.InvoicePipelineService;
import com.krishna.service.util.ConsolidatedService;
import com.krishna.service.util.UtilityService;
import com.krishna.util.ConstantUtility;


@Service
public class CompanyMarginService implements ICompanyMarginService{
	
	@Autowired
	ProjectInvoiceService projectInvoiceService;
	
	@Autowired
	LoginUtiltiyService loginUtilityService;
	
	@Autowired
	ProjectInvoiceRepository projectInvoiceRepository;
	

	@Autowired
	FeignLegacyInterface feignLegacyInterface;
	
	@Autowired
	IndirectCostService indirectCostService;
	
	@Autowired
	IndirectCostRepository costRepository;
	
	@Autowired
	PayrollRepository payrollRepository;
	
	@Autowired
	ProjectMarginService projectMarginservice;
	
	@Autowired
	LeaveCostPercentageRepository leaveCostRepo;
	
	@Autowired
	DollarCostServiceImpl dollarCostService;
	
	@Autowired
	MarginBasisRepository marginBasisRepository;
	
	@Autowired
	BuReserveRepository bureserveRepo;
	
	@Autowired
	UtilityService utilService;
	
	@Autowired
	ArrearRepository arrearRepo;
	
	@Autowired
	BuReserveDeductionRepository deductionRepo;
	
	@Autowired
	ReserveSnapShotRepository reserveSnapShotRepository;
	
	@Autowired
	VariablePayRepository variablePayRepo;
	
	@Autowired
	PayrollService payrollService;

	@Autowired
	BuSpecificCostRepository buSpecificCostRepository;
	
	@Autowired
	InvoicePipelineService invoicePipelineService;
	
	@Autowired
	ConsolidatedService consolidatedService;
	
	@Autowired
	EntityManager entityManager;
	
	Logger log=LoggerFactory.getLogger(CompanyMarginService.class);
	
	@Cacheable("invoices")
	public Map<String, Object> getBuWiseInvoiceTotal(int month, String year, String accessToken) {
		Double invoiceTotalAmount = 0.0;
		Double invoiceTotalAmountInR = 0.0;
		Double invoiceTotalDisputedInR = 0.0;
		Double extInvoiceTotalAmt = 0D;
		Double extInvoiceTotalAmtInr = 0D;
		Double paidTotalInvoice =0D;
		Double paidTotalInvoiceInr = 0D;
		Double dollarexchangeCost=dollarCostService.getAverageDollarCost(month, Integer.parseInt(year));
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		List<Object> invoiceDataList=new ArrayList<>();
		List<Object> businessVerticals = loginUtilityService.objectToListConverter(projectInvoiceService.getBusinessVerticals(accessToken));
		List<Object> invoiceProjectIds=new ArrayList<>();
		List<ProjectInvoice> monthlyInvoices=projectInvoiceRepository.findAllByMonthAndYearAndIsDeleted(monthName, year, false);
		List<ProjectInvoice> monthlyExtInvoice = monthlyInvoices.stream().filter(inv-> inv.getIsInternal().equals(false)).collect(Collectors.toList());
		
		Map<String,Object> projectwiseBuDetails=feignLegacyInterface.getProjectwiseBuDetails(accessToken,"",month,Integer.parseInt(year))!=null?
				(Map<String, Object>)feignLegacyInterface.getProjectwiseBuDetails(accessToken,"",month,Integer.parseInt(year)).get("data"):null;
		for (Object businessVertical : businessVerticals) {
			Double invoiceAmount = 0.0;
			Double invoiceAmountInRupees = 0.0;
			Double invoiceDisputedInR = 0.0;
			Double paymentChargesSum = 0.0;
			int invoiceSize=monthlyInvoices.size();
			
			Map<String,Object> budetails=utilService.getBusinessVerticalDetails(accessToken, businessVertical.toString());
			Map<String, Object> invoiceData = new HashMap<>();
			invoiceData=projectMarginservice.getInternalInvoices(invoiceData, monthName, year, new Long(budetails.get("id").toString()), dollarexchangeCost);
			for(int i=0;i<invoiceSize;i++) {
				if (!businessVertical.equals("")) {
					Map<String,Object> buDetails =  projectwiseBuDetails!=null?(Map<String, Object>) projectwiseBuDetails.get(monthlyInvoices.get(i).getProjectId().toString()):null ;
					String projectBusinessVertical = buDetails!=null?(String)buDetails.get("businessVerticalName"):"NA";
					if(projectBusinessVertical.equals("NA")) {
						Map<String,Object> projectDetails=(Map<String, Object>) feignLegacyInterface.getProjectDescription(monthlyInvoices.get(i).getProjectId(),"",null,null).get("data");
						projectBusinessVertical= projectDetails.get("businessVertical").toString();
					}
					if (projectBusinessVertical.equals(businessVertical)) {
						Double paymentChargesDollar=0D;
						Double paymentChargesRupee=0D;
						if(monthlyInvoices.get(i).getPaymentCharges()!=null && monthlyInvoices.get(i).getInvoiceStatus()!=6) {
							paymentChargesDollar=projectInvoiceService.getPaymentCharges("DOLLAR", monthlyInvoices.get(i));
							paymentChargesRupee=projectInvoiceService.getPaymentCharges("RUPEE", monthlyInvoices.get(i));
						}
						paymentChargesSum=paymentChargesSum+paymentChargesRupee;
						if(monthlyInvoices.get(i).getInvoiceStatus()!=6) {
	
						invoiceProjectIds.add(monthlyInvoices.get(i).getProjectId());
						Double projectInvoice=monthlyInvoices.get(i).getAmountInDollar();

						invoiceAmount=invoiceAmount+projectInvoice;
						invoiceTotalAmount=invoiceTotalAmount+projectInvoice;
						Double projectInvoiceInr=projectInvoice*dollarexchangeCost;
						
						invoiceAmountInRupees=invoiceAmountInRupees+projectInvoiceInr;
						invoiceTotalAmountInR=invoiceTotalAmountInR+projectInvoiceInr;
						if(monthlyInvoices.get(i).getIsInternal().equals(false)){
							extInvoiceTotalAmt = extInvoiceTotalAmt + projectInvoice;
							extInvoiceTotalAmtInr = extInvoiceTotalAmtInr + projectInvoice*dollarexchangeCost;
						}
					}
						if(monthlyInvoices.get(i).getInvoiceStatus()==5)  {
							invoiceDisputedInR = invoiceDisputedInR +monthlyInvoices.get(i).getAmountInDollar()*dollarexchangeCost;
						}
					}
				}
			}
			
			Double internalInvoices=0D;
			Double paidInternalInvoices = 0D;
			Double paidInternalInRupee = 0D;
			if(!businessVertical.toString().equals("Operations Support")){
				internalInvoices=(Double) invoiceData.get("internalInvoices");
				paidInternalInvoices = (Double)invoiceData.get("paidInternalInvoices");
				paidInternalInRupee = (Double)invoiceData.get("paidInternalInRupee");
				paidTotalInvoice = paidTotalInvoice + paidInternalInvoices;
				paidTotalInvoiceInr = paidTotalInvoiceInr + paidInternalInRupee;
			}
			else{
				internalInvoices=(Double) invoiceData.get("receivedinternalInvoices");
				paidTotalInvoice = paidTotalInvoice + (Double)invoiceData.get("paidInternalInvoices");
				paidTotalInvoiceInr = paidTotalInvoiceInr + (Double)invoiceData.get("paidInternalInRupee");

			}

			//invoiceAmount-=paidInternalInvoices;
			//invoiceTotalAmount-=paidInternalInvoices;
			//invoiceAmountInRupees-=paidInternalInRupee;
			//invoiceTotalAmountInR-=paidInternalInRupee;
			invoiceData=getBuWiseInvoiceMap(businessVertical.toString(), invoiceAmount, invoiceAmountInRupees,paymentChargesSum,invoiceDisputedInR);

			invoiceDataList.add(invoiceData);
		}
		Map<String, Object> map=new HashMap<>();
		map.put(ConstantUtility.DOLLAR_EXCHANGE_VALUE, dollarexchangeCost);
		map.put(ConstantUtility.INVOICE_PROJECT_ID, invoiceProjectIds);
		map.put("buWiseInvoice", invoiceDataList);
		map.put("companyTotalRevenue", Double.parseDouble(new DecimalFormat("#.##").format(invoiceTotalAmount)));
		map.put("companyTotalRevenueInr", Double.parseDouble(new DecimalFormat("#.##").format(invoiceTotalAmountInR)));
		map.put("companyRevenue", Double.parseDouble(new DecimalFormat("#.##").format(extInvoiceTotalAmt)));
		map.put("companyRevenueInr", Double.parseDouble(new DecimalFormat("#.##").format(extInvoiceTotalAmtInr)));
		map.put("paidTotalInvoice" , Double.parseDouble(new DecimalFormat("#.##").format(paidTotalInvoice)));
		map.put("paidTotalInvoiceInr" , Double.parseDouble(new DecimalFormat("#.##").format(paidTotalInvoiceInr)));

		return map;
	}
	
	private Map<String, Object> getBuWiseInvoiceMap(String businessVertical,Double invoiceAmount,Double invoiceAmountInRupees,Double paymentChargesSum,Double invoiceDisputedInR){
		Map<String, Object> invoiceData=new HashMap<>();
		invoiceData.put(ConstantUtility.BUSINESS_VERTICAL, businessVertical);
		invoiceData.put(ConstantUtility.INVOICE_AMOUNT, Double.parseDouble(new DecimalFormat("#.##").format(invoiceAmount)));
		invoiceData.put("invoiceAmountInRupees", Double.parseDouble(new DecimalFormat("#.##").format(invoiceAmountInRupees)));
		invoiceData.put("paymentChargesSum", Double.parseDouble(new DecimalFormat("#.##").format(paymentChargesSum)));
		invoiceData.put("invoiceDisputedInR", Double.parseDouble(new DecimalFormat("#.##").format(invoiceDisputedInR)));

		return invoiceData;
	}
	
	@Cacheable("buWiseUsers")
	public List<Object> getCompanywiseData(int month, int year, String accessToken) {
		log.info(".........Fetching Bu Wise Users.........");
		List<Object> companyData = new ArrayList<>();
		Map<String, Object> buWiseUsers = (Map<String, Object>) feignLegacyInterface.getBuUsers( month, year,"All").get("data");
		List<Object> usersList = loginUtilityService.objectToListConverter(buWiseUsers.get("usersData"));
		List<Object> projects = loginUtilityService.objectToListConverter(buWiseUsers.get("projects"));
		List<Object> businessVerticals = loginUtilityService.objectToListConverter( buWiseUsers.get("businessVerticals"));
		int projectSize = projects.size();
		int userSize = usersList.size();
		int verticalsListSize = businessVerticals.size();
		for (int j = 0; j < verticalsListSize; j++) {
			List<Object> teamData = new ArrayList<>();
			Map<String, Object> map = new HashMap<>();
			Map<String, Object> bu = loginUtilityService.objectToMapConverter( businessVerticals.get(j));
			map.put(ConstantUtility.BUSINESS_VERTICAL, bu.get("name"));
			String ownerName="";
			
			if(bu.get("owner")!=null) {
				Long ownerId=Long.parseLong(loginUtilityService.objectToMapConverter(bu.get("owner")).get("id").toString());
				ownerName=(String) ((Map<String, Object>)(feignLegacyInterface.getUserName(accessToken,ownerId)).get("data")).get("name");
			}
			map.put(ConstantUtility.BU_OWNER, ownerName);
			for (int i = 0; i < projectSize; i++) {
				List<Object> obj =  (List<Object>) projects.get(i);
				Integer businessVertical = 0;
				Integer projectId= 0;
				if(obj!=null && obj.get(1).toString()!=null){
					businessVertical = Integer.parseInt(obj.get(1).toString());
					projectId = Integer.parseInt(obj.get(0).toString());
				}
					if (businessVertical.equals(bu.get("id"))) {
						for (int k = 0; k < userSize; k++) {
							if(usersList.get(k)!=null) {
							Integer userProjectId = (Integer) (loginUtilityService.objectToMapConverter( usersList.get(k))).get(ConstantUtility.PROJECT_ID);
							if (projectId.toString().equals(userProjectId.toString())) {
								teamData.add(usersList.get(k));
							}
							}
						}
					}
			}
			map.put("team", teamData);
			companyData.add(map);
		}
		return companyData;
	}
	
	@CacheEvict(cacheNames="invoices", allEntries=true)
	public void flushInvoicesCache() { }
	
	//@Scheduled(cron = "0 0 6 * * *")
	@CacheEvict(cacheNames="projectCost", allEntries=true)
	public void flushDirectCostCache() { }
	
	//@Scheduled(cron = "0 0 5 * * *")
	@CacheEvict(cacheNames="buWiseUsers", allEntries=true)
	public void flushTeamData() { }

	
	
	@Cacheable(value="projectCost",key="{#month, #year}")
	public Map<String, Object> getDirectCostBuWise(int month, int year, List<Object> buWiseUsers, Map<String, Object> buWiseInvoiceData,String accessToken) {
		List<Object> dataList = new ArrayList<>();
		Double companyTotalCost = 0.0;
		Double specialAmountAmount = 0D;
		Double voluntaryPayAmount = 0D;
		List<Object> projectIds = loginUtilityService.objectToListConverter(buWiseInvoiceData.get(ConstantUtility.INVOICE_PROJECT_ID));
		List<Map<String, Object>> invoicesList = (List<Map<String, Object>>) buWiseInvoiceData.get("buWiseInvoice");
		MarginBasis marginBasis = marginBasisRepository.findByMonthAndYear(month, year);
		boolean isGradeWise = marginBasis != null ? marginBasis.getIsGradeWise() : false;
		boolean isLTM = marginBasis != null ? marginBasis.getIsLTM() : false;

		int totalBu = buWiseUsers.size();
		Map<String, Double> nonbillableGradeWiseCosts = null;
		Map<String, Object> userListAndCount = feignLegacyInterface.gradeWiseExpectedHours(month, year);

		if (isGradeWise) {
			nonbillableGradeWiseCosts = utilService.getGradeWiseCosts(month, year, accessToken, "",userListAndCount);
		}
		Map<String, Object> workingDaysData = (Map<String, Object>) feignLegacyInterface.getpayrollWorkingDays(accessToken, month, year).get("data");
		Object workingDays = workingDaysData.get("workingDays");
		Map<String, Object> excludedAmountData = new HashMap<>();
		List<BuSpecificCost> buSpecificCosts = buSpecificCostRepository.findAllByYearAndMonthAndDeleted(year,month,false);
		
		Map<String, Object> allCompensation = projectMarginservice.getAllCompensation(month,year);
		
		
		for (int i = 0; i < totalBu; i++) {
			if ((isGradeWise && nonbillableGradeWiseCosts != null) || !isGradeWise) {
				Map<String, Object> data = loginUtilityService.objectToMapConverter(buWiseUsers.get(i));
				List<Object> teamList = loginUtilityService.objectToListConverter(data.get("team"));
				Map<String, Object> invMap = invoicesList.stream().filter(inv -> inv.get("businessVertical").toString().equals(data.get(ConstantUtility.BUSINESS_VERTICAL).toString())).findAny().orElse(null);
				Map<String, Object> projectCost = getCompanyProjectCost(teamList, month, year, accessToken, (data.get(ConstantUtility.BUSINESS_VERTICAL).toString()), 
						projectIds, nonbillableGradeWiseCosts, invMap, workingDays, excludedAmountData,allCompensation);
				String businessVertical = projectCost.get(ConstantUtility.BUSINESS_VERTICAL).toString();
				// asset Cost
				Double assetCost = 0D;
				//buSpecifci cost
				Double buSpCost = 0D;
				if(buSpecificCosts!= null){
					buSpCost = buSpecificCosts.stream().filter(val -> val.getBusinessVertical().equals(businessVertical)).collect(Collectors.summingDouble(BuSpecificCost::getAmount));
				}
				companyTotalCost = companyTotalCost + (Double) projectCost.get(ConstantUtility.PROJECT_COST) + assetCost + buSpCost;
				

				projectCost.put(ConstantUtility.PROJECT_COST, Double.parseDouble(new DecimalFormat("#.##").format((Double) projectCost.get(ConstantUtility.PROJECT_COST) + assetCost + buSpCost)));
				projectCost.put(ConstantUtility.BU_OWNER, (data.get(ConstantUtility.BU_OWNER).toString()));
				projectCost.put("isInternal", false);
				projectCost.put("buSpecificCost", buSpCost);
				Map<String, Object> internalInvoices = utilService.getBuInternalInvoices(accessToken,businessVertical, month, year);
				projectCost.put("internalInvoice", internalInvoices.get("paidInternalInvoices"));
				projectCost.put("internalInvoiceInRupee", internalInvoices.get("paidInternalInRupee"));
				Double finalProjectCost = new Double(projectCost.get(ConstantUtility.PROJECT_COST).toString()) + new Double(internalInvoices.get("paidInternalInRupee").toString());
				projectCost.put(ConstantUtility.PROJECT_COST, Math.round(finalProjectCost * 100.00) / 100.00);
				companyTotalCost = companyTotalCost + new Double(internalInvoices.get("paidInternalInRupee").toString());

				if (businessVertical.equals("Operations Support")) {
							projectCost.put("isInternal", true);
				}
				dataList.add(projectCost);
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put("voluntaryPayAmount", Math.round((Double.parseDouble(voluntaryPayAmount.toString())) * 100.0) / 100.0);
		map.put("specialAllowanceAmount", Math.round((Double.parseDouble(specialAmountAmount.toString())) * 100.0) / 100.0);
		map.put("buWiseTotalCost", dataList);
		map.put("companyTotalCost", Double.parseDouble(new DecimalFormat("#.##").format(companyTotalCost)));
		map.put("isLTM", isLTM);
		return map;
	}

	public Map<String, Double> getBuWiseLtmDisputedPercent(Integer year, Integer month, String accessToken){
		Map<String, Double> responseMap = new LinkedHashMap<>();
		
		Double totalAmountLTM = 0.0;
		Double disputedAmountLTM = 0.0;
		Double disputedPercentageLTM = 0.0;

		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if(month != null){
			cal.set(Calendar.MONTH, month-1);
		}
		cal.set(Calendar.YEAR, Integer.parseInt(year.toString()));
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DATE));
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(cal.getTime());
		cal1.add(Calendar.MONTH, -11);
		cal1.set(Calendar.DAY_OF_MONTH, 1);
		
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		List<ProjectInvoice> invoices = projectInvoiceRepository.findAllByYearAndIsDeletedAndIsInternalAndInvoiceStatusNot(year.toString(),
				false, false, 6L);
		List<ProjectInvoice> previousInvoices = projectInvoiceRepository.findAllByYearAndIsDeletedAndIsInternalAndInvoiceStatusNot(String.valueOf(year-1),
				false, false, 6L);
		Map<String,Object> projectwiseBuDetails=(Map<String, Object>) feignLegacyInterface.getProjectwiseBuDetails(accessToken,"",null,null).get("data");
		for(Map.Entry<String, Object> entry : projectwiseBuDetails.entrySet()){
			
		}

		List<ProjectInvoice> ltmInvoices = new ArrayList<>();
		List<ProjectInvoice> totalInvoices = new ArrayList<>();
		totalInvoices.addAll(invoices);
		totalInvoices.addAll(previousInvoices);
		for(ProjectInvoice inv : totalInvoices){
			Calendar invCal = Calendar.getInstance();
			invCal.setTime(date);
			invCal.set(Calendar.YEAR,Integer.parseInt(inv.getYear()));
			invCal.set(Calendar.MONTH,Month.valueOf(inv.getMonth().toUpperCase()).getValue()-1);
			invCal.set(Calendar.DAY_OF_MONTH, 15);
			if(invCal.getTime().before(cal.getTime()) && invCal.getTime().after(cal1.getTime()) ){
				ltmInvoices.add(inv);
			}
		}
		Map<String, Integer> monthMap=new HashMap<>();


		for(ProjectInvoice invoice : ltmInvoices){
			Double buAmountLTM= 0D;
			Double totalBuAmountLTM = 0D;
			Double monthlyBuTotalLTM = 0D;

			Integer monthnum=0;
			if(monthMap.containsKey(invoice.getMonth().toString()))
				monthnum = monthMap.get(invoice.getMonth().toString());
			else {
				monthnum=loginUtilityService.getMonthNumber(invoice.getMonth());
				monthMap.put(invoice.getMonth(), monthnum);
			}

			Map<String,Object> projectBuMap = ((Map<String,Object>)projectwiseBuDetails.get(invoice.getProjectId().toString()));
			String projectBusinessVertical = "UnAssigned";
			if(projectBuMap != null)
				projectBusinessVertical =  projectBuMap.get("businessVerticalName").toString();
			if (invoice.getInvoiceStatus() == 5) {
				if(responseMap.containsKey(projectBusinessVertical+"LTM"))	{
					
					buAmountLTM = (Double) responseMap.get(projectBusinessVertical+"LTM");
				}
				
				responseMap.put(projectBusinessVertical + "LTM", buAmountLTM+invoice.getAmountInDollar());
				totalAmountLTM = totalAmountLTM + invoice.getAmountInDollar();
				
			}
			if(responseMap.containsKey(projectBusinessVertical+"TotalLTM"))	{
				
				totalBuAmountLTM = (Double) responseMap.get(projectBusinessVertical+"TotalLTM");
			}
			responseMap.put(projectBusinessVertical+"TotalLTM", totalBuAmountLTM+invoice.getAmountInDollar());
			if (monthMap.get(invoice.getMonth()).equals(month)) {
				if (responseMap.containsKey(projectBusinessVertical + "monthlyTotalLTM")) {
					monthlyBuTotalLTM = (Double) responseMap.get(projectBusinessVertical + "monthlyTotalLTM");
				}
				responseMap.put(projectBusinessVertical + "monthlyTotalLTM",
						monthlyBuTotalLTM + invoice.getAmountInDollar());
			}
		}
		List<Map<String,Object>> avgDollarCost = new ArrayList<>();
		Double avgCost = 1D;
		avgDollarCost = dollarCostService.getMonthWiseAverageDollarCost(cal1.getTime(), cal.getTime());
		for(Map<String,Object> map : avgDollarCost){
			if(map.containsKey("AverageCost")){
				avgCost=Double.parseDouble(map.get("AverageCost").toString());
				break;
			}
			else
				continue;
		}
		responseMap.put("totalAmountLTM", totalAmountLTM);
		responseMap.put("AverageDollarCost", avgCost);
		return responseMap;


	}

	public Map<String, Double> getBuWiseAvgDisputedPercent(Integer year, String accessToken){
		Map<String, Double> responseMap = new LinkedHashMap<>();
		Double totalAmount = 0.0;
		Double disputedAmmout = 0.0;
		Double disputedPercentage = 0.0;
		Map<String, Integer> monthMap=new HashMap<>();

		
		List<ProjectInvoice> invoices = projectInvoiceRepository.findAllByYearAndIsDeletedAndIsInternalAndInvoiceStatusNot(year.toString(),
				false, false, 6L);
		
		Map<String,Object> projectwiseBuDetails=(Map<String, Object>) feignLegacyInterface.getProjectwiseBuDetails(accessToken,"",null,null).get("data");
		for (ProjectInvoice invoice : invoices){
			Integer monthnum=0;
			Double buAmount= 0D;
			Double totalBuAmount = 0D;

			if(monthMap.containsKey(invoice.getMonth().toString()))
				monthnum = monthMap.get(invoice.getMonth().toString());
			else {
				monthnum=loginUtilityService.getMonthNumber(invoice.getMonth());
				monthMap.put(invoice.getMonth(), monthnum);
			}
			Map<String,Object> projectBuMap = ((Map<String,Object>)projectwiseBuDetails.get(invoice.getProjectId().toString()));
			String projectBusinessVertical = "UnAssigned";
			if(projectBuMap != null)
				projectBusinessVertical =  projectBuMap.get("businessVerticalName").toString();

			
			if (invoice.getInvoiceStatus() == 5) {
				if(responseMap.containsKey(projectBusinessVertical))	{
					buAmount=(Double) responseMap.get(projectBusinessVertical);
				}
				responseMap.put(projectBusinessVertical, buAmount+invoice.getAmountInDollar());
				totalAmount = totalAmount + invoice.getAmountInDollar();
			}
			if(responseMap.containsKey(projectBusinessVertical+"Total"))	{
				totalBuAmount=(Double) responseMap.get(projectBusinessVertical+"Total");
			}
			responseMap.put(projectBusinessVertical+"Total", totalBuAmount+invoice.getAmountInDollar());
		}
		Calendar fromCal = Calendar.getInstance();
		fromCal.set(Calendar.YEAR, Integer.parseInt(year.toString()));
		fromCal.set(Calendar.MONTH, Calendar.JANUARY);
		Calendar toCal = Calendar.getInstance();
		toCal.set(Calendar.YEAR, Integer.parseInt(year.toString()));
		toCal.set(Calendar.MONTH, Calendar.DECEMBER);
		List<Map<String,Object>> avgDollarCost = new ArrayList<>();
		Double avgCost = 1D;
		avgDollarCost = dollarCostService.getMonthWiseAverageDollarCost(fromCal.getTime(), toCal.getTime());
		for(Map<String,Object> map : avgDollarCost){
			if(map.containsKey("AverageCost")){
				avgCost=Double.parseDouble(map.get("AverageCost").toString());
				break;
			}
			else
				continue;
		}
		responseMap.put("totalAmount", totalAmount);
		responseMap.put("AverageDollarCost", avgCost);
		return responseMap;

	}


	
	
	public Map<String,Object> getCompanyMargin(String accessToken, Map<String, Object> directCostTotal, Map<String, Object> invoiceTotal,int month,int year) {
		Double totalMargin=0.0;
		Double totalMarginPerc=0.0;
		Double disputedAmount = 0D;
		Double disputedAmountLTM = 0D;
		Double netMargin = 0D;
		Map<String,Object> ytdMap=projectInvoiceService.getAverageDisputedPercentage(new Long(year), "",accessToken);
		Map<String,Object> ltmMap=projectInvoiceService.getLTMDisputedPercentage(new Long(year), "",accessToken,month);
		Map<String, Double> disputedAmtLtmMap=getBuWiseLtmDisputedPercent(year,month,accessToken);
		Map<String, Double> disputedAmtYtdMap=getBuWiseAvgDisputedPercent(year,accessToken);
		Double dollarexchangeCost=dollarCostService.getAverageDollarCost(LocalDateTime.now().minusMonths(1).getMonthValue(), LocalDateTime.now().minusMonths(1).getYear());
		Double avgDollarCostLtm = disputedAmtLtmMap.get("AverageDollarCost");
		Double avgDollarCostYtd = disputedAmtYtdMap.get("AverageDollarCost");




		
		List<Object> projectCostList=loginUtilityService.objectToListConverter(directCostTotal.get("buWiseTotalCost"));
		int projectCostListSize=projectCostList.size();
		List<Object> invoiceList=loginUtilityService.objectToListConverter(invoiceTotal.get("buWiseInvoice"));
		int invoiceListSize=invoiceList.size();
		Map<String,Object> invoiceMap=getInvoiceMapForReserve(invoiceList);
		Object totalInvoiceAmount=invoiceTotal.get("companyTotalRevenueInr");
		Object totalExtInvoiceAmount=invoiceTotal.get("companyRevenueInr");
		Object totalProjectCost=directCostTotal.get("companyTotalCost");
		Boolean isLTM = Boolean.valueOf(directCostTotal.get("isLTM").toString());
		List<Object> buWiseMargin=new ArrayList<>();
		String monthName = new DateFormatSymbols().getMonths()[month- 1].toString();
		Months monthEnum = Months.valueOf(monthName.toUpperCase());
		IndirectCost monthlyIndirectCost = costRepository.findByYearAndIsDeletedAndMonth(Integer.toString(year), false, monthEnum);
		
		for(int i=0;i<projectCostListSize;i++) {
			Map<String,Object> projectCostObj=loginUtilityService.objectToMapConverter(projectCostList.get(i)) ;
			Object projectCostBu=projectCostObj.get(ConstantUtility.BUSINESS_VERTICAL);
			Object projectCost=projectCostObj.get(ConstantUtility.PROJECT_COST);
			Double buDisputedAmountLTM=0D;
			Double buTotalInvoiceAmountLTM=0D;
			Double disputedPercentageLTM=0D;
			Double buMonthlyDisputedAmount = 0D;
			Double buDisputedAmountYtd=0D;
			Double buTotalInvoiceAmountYtd=0D;
			
			for(int j=0;j<invoiceListSize;j++) {
				Map<String,Object> invoiceObj=loginUtilityService.objectToMapConverter(invoiceList.get(j));
				Object invoiceBu=invoiceObj.get(ConstantUtility.BUSINESS_VERTICAL);
				Map<String, Object> invoiceData=(Map<String, Object>) invoiceMap.get(projectCostBu);

				if(projectCostBu.toString().equals(invoiceBu.toString())) {
					Object invoiceAmountInr=invoiceObj.get("invoiceAmountInRupees");
					Double margin=0.0;
					Double marginperc=0.0;
					if(Double.parseDouble(invoiceAmountInr.toString())!=0.0 && monthlyIndirectCost!=null) {
						margin=Double.parseDouble(invoiceAmountInr.toString())-Double.parseDouble(projectCost.toString());
						marginperc=(margin*100)/Double.parseDouble(invoiceAmountInr.toString());
					}
					if(disputedAmtLtmMap.get(projectCostBu + "LTM")!=null){
						buDisputedAmountLTM = disputedAmtLtmMap.get(projectCostBu + "LTM")*avgDollarCostLtm;
					}
					if(disputedAmtLtmMap.get(projectCostBu+"TotalLTM")!=null)
						buTotalInvoiceAmountLTM = new Double(disputedAmtLtmMap.get(projectCostBu+"TotalLTM").toString())*avgDollarCostLtm;
					if(buDisputedAmountLTM!=0 && buTotalInvoiceAmountLTM!=0)
						disputedPercentageLTM = (buDisputedAmountLTM / buTotalInvoiceAmountLTM) * 100;
					if(disputedAmtLtmMap.get(projectCostBu+"monthlyTotalLTM") != null)
						buMonthlyDisputedAmount = (disputedPercentageLTM * (new Double(disputedAmtLtmMap.get(projectCostBu+"monthlyTotalLTM").toString())*dollarexchangeCost)) / 100;	
					if(disputedAmtYtdMap.get(projectCostBu)!=null)
						buDisputedAmountYtd = disputedAmtYtdMap.get(projectCostBu)*avgDollarCostYtd;
					if(disputedAmtYtdMap.get(projectCostBu+"Total")!=null)
						buTotalInvoiceAmountYtd = new Double(disputedAmtYtdMap.get(projectCostBu+"Total").toString())*avgDollarCostYtd;
						Double disputedPercentageYtd = 0D;
					if(buDisputedAmountYtd!=0 && buTotalInvoiceAmountYtd!=0)
							disputedPercentageYtd = (buDisputedAmountYtd / buTotalInvoiceAmountYtd) * 100;
					disputedPercentageYtd = Math.round(disputedPercentageYtd * 100.00) / 100.00;
					Double invoiceTotalAmount=0D;
					if(invoiceData!=null && invoiceData.containsKey("invoiceAmountInRupees"))
						invoiceTotalAmount=(Double) invoiceData.get("invoiceAmountInRupees");
					
					
					Double buDisputedYtd = (disputedPercentageYtd*invoiceTotalAmount)/100.00;

					
					Map<String,Object> map=new HashMap<>();
					map.put(ConstantUtility.BUSINESS_VERTICAL, projectCostBu);
					map.put("margin",Math.round( margin*100.00)/100.00);
					map.put("marginPerc", Math.round(marginperc*100.00)/100.00);
					map.put("ltmDisputedPerc",disputedPercentageLTM);
					map.put("ytdDisputedPerc",disputedPercentageYtd);
					map.put("ltmDisputedAmount",buMonthlyDisputedAmount);
					map.put("ytdDisputedAmount",buDisputedYtd);
					if(isLTM){
						map.put("netMargin", margin - buMonthlyDisputedAmount);
						map.put("netMarginPerc", Math.round((marginperc - disputedPercentageLTM)*100.0)/100.0);

					}
					else{
						map.put("netMargin", margin - buDisputedYtd);
						map.put("netMarginPerc",Math.round((marginperc - disputedPercentageYtd)*100.0)/100.0);
					}
					


					buWiseMargin.add(map);
				}
			}
		}
		Map<String,Object> marginMap=new HashMap<>();
		marginMap.put("buWiseMargin", buWiseMargin);
		if(monthlyIndirectCost!=null) {
			totalMargin=Double.parseDouble(totalInvoiceAmount.toString())-Double.parseDouble(totalProjectCost.toString());
			totalMarginPerc=totalMargin/Double.parseDouble(totalInvoiceAmount.toString())*100;
		}
		marginMap.put("totalMargin", Double.parseDouble(new DecimalFormat("#.##").format(totalMargin)));
		marginMap.put("totalMarginPerc", Math.round(totalMarginPerc*100.0)/100.0);
		if(totalMargin!=0) {
			disputedAmount = (new Double(ytdMap.get("averageDisputedPercentage").toString()) * Double.parseDouble(totalExtInvoiceAmount.toString()))/100;
			disputedAmountLTM = (new Double(ltmMap.get("averageDisputedPercentageLTM").toString()) * (new Double(ltmMap.get("monthlyExtInvoiceTotal").toString())))/100;
			if(isLTM)
				netMargin = totalMargin-disputedAmountLTM;
			else
				netMargin = totalMargin-disputedAmount;

		}
		marginMap.put("disputedAmountYtd", disputedAmount);
		marginMap.put("disputedAmountLTM", disputedAmountLTM);
		marginMap.put("disputedPercYtd", new Double(ytdMap.get("averageDisputedPercentage").toString()));
		marginMap.put("disputedPercLTM", new Double(ltmMap.get("averageDisputedPercentageLTM").toString()));
		marginMap.put("netMargin", netMargin);
		if(isLTM)
			marginMap.put("netMarginPerc", totalMarginPerc- new Double(ltmMap.get("averageDisputedPercentageLTM").toString()));
		else
			marginMap.put("netMarginPerc", totalMarginPerc- new Double(ytdMap.get("averageDisputedPercentage").toString()));
		return marginMap;
	}
	
	
	private Map<String, Object> getCompanyProjectCost(List<Object> teamList, int month, int year, String accessToken, String businessVertical, 
			List<Object> projectInvoice, Map<String, Double> nonbillableGradeWiseCosts, Map<String, Object> invMap, Object workingDays,
			Map<String, Object> excludedAmountData, Map<String, Object> allCompensation) {
		Map<String, Object> projectMap = new HashMap<>();
		Integer projectHours = 0;
		Integer projectMins = 0;
		Double directCost = 0.0;
		Double indirectCost = 0.0;
		Double projectCost = 0.0;
		Double overallDirectCost = 0.0;
		Double hourlyIndirectCost = 0.0;
		Double nonBillableHourlyIndirectCost = 0.0;
		double totalSpecialAllowanceAmount = 0D;
		double totalVoluntaryPayAmount =0D;
		double totalExcludedAmount=0.0D;
		int teamSize = teamList.size();
		
		Map<String,Object> voluntaryPayData = (Map<String, Object>) allCompensation.get("voluntaryPayResponse");
		Map<String,Object> specialAllowanceData = (Map<String, Object>) allCompensation.get("specialAmountResponse");
		Map<String,Object> payDaysData =  (Map<String, Object>) allCompensation.get("PayDaysResponse");
		
		Boolean isGradeWise = false;
		MarginBasis marginBasis = marginBasisRepository.findByMonthAndYear(month, year);
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		Months monthEnum = Months.valueOf(monthName.toUpperCase());
		IndirectCost indirectCostObject = costRepository.findByYearAndIsDeletedAndMonth(String.valueOf(year), false, monthEnum);
		Map<String, Object> companyExpectedHours = new HashMap<>();
		if(indirectCostObject!=null) 
			companyExpectedHours = feignLegacyInterface.getCompanyExpectedHours(accessToken,month, indirectCostObject.getYear());
		Double buCost = 0D;
		if (marginBasis != null)
			isGradeWise = marginBasis.getIsGradeWise();
		if (isGradeWise) {
			buCost = indirectCostService.buIndirectCost(accessToken, businessVertical, month, Integer.toString(year),nonbillableGradeWiseCosts);
		} else {
			Object expectedHours = companyExpectedHours.get(ConstantUtility.DATA);
			hourlyIndirectCost = indirectCostService.getIndirectCostHourlyV2(accessToken, Integer.toString(year), businessVertical, month,indirectCostObject,expectedHours,workingDays);
			nonBillableHourlyIndirectCost = indirectCostService.getIndirectCostHourlyV2(accessToken, Integer.toString(year), "", month,indirectCostObject,expectedHours,workingDays);
		}
		boolean hasBillableProjects = false;
		if (!teamList.isEmpty()) {
			for (int j = 0; j < teamSize; j++) {
				Map<String, Object> userData = loginUtilityService.objectToMapConverter(teamList.get(j));
				String expectedHours = userData.get(ConstantUtility.EXPECTED_HOURS).toString();
				String grade ="NA";
				if(userData.containsKey("grade") && userData.get("grade")!=null)
					grade = userData.get("grade").toString();
				Long projId = Long.parseLong(userData.get(ConstantUtility.PROJECT_ID).toString());
				String mins = expectedHours.split("\\.")[1];
				String hours = expectedHours.split("\\.")[0];
				projectHours = projectHours + Integer.parseInt(hours);
				Object userId = userData.get("userId");
				projectMins = projectMins + Integer.parseInt(mins);
				if (projectMins >= 60) {
					projectHours = projectHours + 1;
					projectMins = projectMins - 60;
				}
				if (isGradeWise ) {
					Double billablecost = (nonbillableGradeWiseCosts.get(grade)) + buCost;
					hourlyIndirectCost = billablecost / (Double.parseDouble(workingDays.toString()) * 8);
					Double nonbillableCost = (nonbillableGradeWiseCosts.get(grade));
					nonBillableHourlyIndirectCost = nonbillableCost / (Double.parseDouble(workingDays.toString()) * 8);
				}
				double userSalary = indirectCostService.getPay(userData, month, year, workingDays, voluntaryPayData,payDaysData);
				Double expHours = Double.parseDouble(expectedHours.split("\\.")[0].toString());
				if (Double.parseDouble(mins) != 0D) {
					expHours = expHours + (Double.parseDouble(mins) / 60);
				}
				if(specialAllowanceData.containsKey(userId.toString())) {
					double specialAllowance=(Double.parseDouble(specialAllowanceData.get(userId.toString()).toString())/(Double.parseDouble(payDaysData.get(userId.toString()).toString()) *8));
					totalSpecialAllowanceAmount=totalSpecialAllowanceAmount+Math.round((specialAllowance*Double.parseDouble(expHours.toString()))*100.0)/100.0;
				}
				
				if(voluntaryPayData.containsKey(userId.toString())) {
					double voluntaryPay=(Double.parseDouble(voluntaryPayData.get(userId.toString()).toString())/(Double.parseDouble(payDaysData.get(userId.toString()).toString()) *8));
					totalVoluntaryPayAmount=totalVoluntaryPayAmount+Math.round((voluntaryPay*Double.parseDouble(expHours.toString()))*100.0)/100.0;
				}
				
				boolean salaryIncluded=true;
				if(excludedAmountData.containsKey(userId.toString()))
					salaryIncluded = false;
				if (projectInvoice.contains(projId) && !businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT)) {
					hasBillableProjects = true;
					if(salaryIncluded) {
						directCost = directCost + userSalary;
						indirectCost = indirectCost + Math.round((hourlyIndirectCost * Double.parseDouble(expHours.toString())) * 100.0) / 100.0;
					}
					else
						totalExcludedAmount = totalExcludedAmount + (userSalary+(hourlyIndirectCost * Double.parseDouble(expHours.toString())));
				} else {
					if (!businessVertical.equals("Operations Support")) {
						if(salaryIncluded) 
							overallDirectCost = overallDirectCost + userSalary;
					}
					if(businessVertical.equals("Operations Support"))
						if(salaryIncluded) 
							indirectCost = indirectCost + Math.round((nonBillableHourlyIndirectCost * Double.parseDouble(expHours.toString())) * 100.0) / 100.0;
					if(!salaryIncluded) 
						totalExcludedAmount = totalExcludedAmount + (userSalary+(nonBillableHourlyIndirectCost * Double.parseDouble(expHours.toString())));
				}
			}

		}
		if (!hasBillableProjects) {
			directCost = overallDirectCost;
		}
		Double paymentChargesSum = 0D;
		if (invMap != null && invMap.containsKey("paymentChargesSum"))
			paymentChargesSum = Double.parseDouble(invMap.get("paymentChargesSum").toString());
		projectCost = indirectCost + directCost + paymentChargesSum + totalSpecialAllowanceAmount+totalVoluntaryPayAmount;
		projectMap.put(ConstantUtility.BUSINESS_VERTICAL, businessVertical);
		projectMap.put(ConstantUtility.PROJECT_COST, projectCost);
		
		projectMap.put("projectCostExcPaymentCharge", Double.parseDouble(new DecimalFormat("#.##").format(projectCost - paymentChargesSum)));
		projectMap.put("excludedAmount", Math.round(totalExcludedAmount * 100.0) / 100.0);
		projectMap.put("specialAllowanceAmount", Math.round(totalSpecialAllowanceAmount * 100.0) / 100.0);
		projectMap.put("voluntaryPayAmount", Math.round(totalVoluntaryPayAmount * 100.0) / 100.0);
		projectMap.put("paymentChargesSum", Double.parseDouble(new DecimalFormat("#.##").format(paymentChargesSum)));
		projectMap.put(ConstantUtility.INDIRECT_COST, Math.round(indirectCost * 100.0) / 100.0);
		projectMap.put("directCost", Math.round(directCost * 100.0) / 100.0);
		projectMap.put(ConstantUtility.EXPECTED_HOURS, projectHours + "." + projectMins);
		return projectMap;
	}
	
	@Override
	public Map<String,Object> getCompanyPL(String accessToken,int month,int year){
		Map<String,Object> companyMargins=new HashMap<>();
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		Months monthEnum = Months.valueOf(monthName.toUpperCase());
		Double indirectCost=0.0;
		Double infraCost=0.0;
		Double variableCost=0.0;
		Double reimbursementCost=0.0;
		Double excludedMarginAmount = 0.0D;
		IndirectCost monthlyIndirectCost = costRepository.findByYearAndIsDeletedAndMonth(Integer.toString(year), false, monthEnum);
		

		if(monthlyIndirectCost!=null) {
			infraCost=monthlyIndirectCost.getInfraCost();
			variableCost=monthlyIndirectCost.getVariableCost();
			indirectCost=monthlyIndirectCost.getVariableCost()+monthlyIndirectCost.getInfraCost()+monthlyIndirectCost.getReimbursement();
			companyMargins.put("infraCost", infraCost);
			companyMargins.put("variableCost", variableCost);
			List<Arrear> arrears= arrearRepo.findAllByCreationMonthAndCreationYearAndIsReimbursementAndIsArrearIncludedAndIsDeleted(month, year, true, false,false);
			if(!arrears.isEmpty())
				reimbursementCost= arrears.parallelStream().mapToDouble(arrear -> arrear.getArrearAmount()).sum();
			List<VariablePay> variablePays=variablePayRepo.findAllByMonthAndYearAndIsDeleted(month, year, false);
			Double varibalePaidAmount=variablePays.parallelStream().mapToDouble(pay -> pay.getAmount()).sum();
			Map<String, Object> allCompensation = projectMarginservice. getAllCompensation(month, year);
			Map<String,Object> voluntaryPayData = (Map<String, Object>) allCompensation.get("voluntaryPayResponse");
			Map<String,Object> specialAllowanceData = (Map<String, Object>) allCompensation.get("specialAmountResponse");
			Double voluntaryPayAmount= Double.parseDouble(voluntaryPayData.get("voluntaryPayAmount").toString());
			companyMargins.put("voluntaryPay", Math.round(voluntaryPayAmount*100.00)/100.00 );
			companyMargins.put("variablePay", varibalePaidAmount);
			companyMargins.put("specialAllowanceAmount",Math.round((Double.parseDouble(specialAllowanceData.get("specialAllowanceAmount").toString())) * 100.0) / 100.0);
			companyMargins.put("reimbursementCost", reimbursementCost);
			indirectCost=monthlyIndirectCost.getVariableCost()+monthlyIndirectCost.getInfraCost()+reimbursementCost;
			companyMargins.put(ConstantUtility.INDIRECT_COST, indirectCost);
			companyMargins=getInvoiceAndPayrollForCompanyPL(month, year, companyMargins);
			Double paymentCharges=Double.parseDouble(companyMargins.get("paymentChargesSum").toString());
			List<BuSpecificCost> buSpecificCosts = buSpecificCostRepository.findAllByYearAndMonthAndDeleted(year,month,false);
			Double buSpCost = 0D;
			if(buSpecificCosts != null){
				buSpCost = buSpecificCosts.stream().collect(Collectors.summingDouble(BuSpecificCost::getAmount));
			}
			Double totalCost=(double) companyMargins.get(ConstantUtility.TOTAL_COST)+infraCost+variableCost+reimbursementCost+paymentCharges + buSpCost;
			
			companyMargins.put(ConstantUtility.TOTAL_COST, totalCost);
			Double invoiceAmount=  (double) companyMargins.get(ConstantUtility.INVOICE_AMOUNT);
			Double profitAmount=invoiceAmount-totalCost;
			companyMargins.put("profitAmount", profitAmount);
			companyMargins.put("buSpecifciCost", buSpCost);
			Double maginPerc=(profitAmount*100)/Double.parseDouble(companyMargins.get(ConstantUtility.INVOICE_AMOUNT).toString());
			companyMargins.put("maginPerc", Math.round(maginPerc*100.0)/100.0);
			companyMargins=getNetMargin(Double.parseDouble(companyMargins.get(ConstantUtility.INVOICE_AMOUNT).toString()), profitAmount, maginPerc, companyMargins, year,accessToken,month);
			companyMargins.put("excludedMargin", excludedMarginAmount);
		}
		return companyMargins;
	}
	
	private Map<String,Object> getNetMargin(Double totalInvoiceAmount,Double totalMargin,Double totalMarginPerc, Map<String,Object> marginMap,int year, String accessToken,Integer month){
		Double disputedAmount = 0D;
		Double disputedAmountLTM = 0D;
		Double netMargin = 0D;
		Boolean isLTM = false;

		Map<String,Object> ytdMap=projectInvoiceService.getAverageDisputedPercentage(new Long(year), "",accessToken);
		Map<String,Object> ltmMap=projectInvoiceService.getLTMDisputedPercentage(new Long(year), "",accessToken,month);
		MarginBasis marginBasis=marginBasisRepository.findByMonthAndYear(month, year);
		if(marginBasis != null)
			isLTM = marginBasis.getIsLTM();
		
		if(totalMargin!=0) {
			disputedAmount = (new Double(ytdMap.get("averageDisputedPercentage").toString()) * totalInvoiceAmount)/100;
			disputedAmountLTM = (new Double(ltmMap.get("averageDisputedPercentageLTM").toString()) * totalInvoiceAmount)/100;

			if(isLTM)
				netMargin = totalMargin-disputedAmountLTM;
			else
				netMargin = totalMargin-disputedAmount;
		}



		marginMap.put("disputedAmountYtd", disputedAmount);
		marginMap.put("disputedPercYtd", new Double(ytdMap.get("averageDisputedPercentage").toString()));
		marginMap.put("disputedAmountLTM", disputedAmountLTM);
		marginMap.put("disputedPercLTM", new Double(ltmMap.get("averageDisputedPercentageLTM").toString()));
		marginMap.put("totalYtdRevenue", new Double(ytdMap.get("totalAmount").toString()));
		marginMap.put("totalLtmRevenue", new Double(ltmMap.get("totalAmountLTM").toString()));
		marginMap.put("disputedYtdRevenue", new Double(ytdMap.get("disputedAmount").toString()));
		marginMap.put("disputedLtmRevenue", new Double(ltmMap.get("disputedAmountLTM").toString()));

		marginMap.put("netMargin", netMargin);
		if(isLTM)
			marginMap.put("netMarginPerc", totalMarginPerc- new Double(ltmMap.get("averageDisputedPercentageLTM").toString()));
		else
			marginMap.put("netMarginPerc", totalMarginPerc- new Double(ytdMap.get("averageDisputedPercentage").toString()));

		return marginMap;
	}
	
	private Map<String,Object> getInvoiceAndPayrollForCompanyPL(int month,int year,Map<String,Object> companyMargins){
		Double invoiceAmountInr=0.0;
		Double invoiceAmountInDollar=0.0;
		Double dollarexchangeCost=0.0;
		Double paymentChargesSum = 0.0;
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		
		List<ProjectInvoice> invoices=projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndIsInternal(monthName, Integer.toString(year), false, false);
		if(!invoices.isEmpty()) {
			dollarexchangeCost=dollarCostService.getAverageDollarCost(month, year);
				
				for(ProjectInvoice invoice:invoices) {
					Double paymentChargesDollar=0D;
					Double paymentChargesRupee=0D;
					if(invoice.getPaymentCharges()!=null && invoice.getInvoiceStatus()!=6) {
						paymentChargesDollar=projectInvoiceService.getPaymentCharges("DOLLAR", invoice);
						paymentChargesRupee=projectInvoiceService.getPaymentCharges("RUPEE", invoice);
					}
					paymentChargesSum=paymentChargesSum+paymentChargesRupee;
					if(invoice.getInvoiceStatus()!=6) {
					invoiceAmountInDollar=invoiceAmountInDollar+invoice.getAmountInDollar();
					invoiceAmountInr=invoiceAmountInr+(invoice.getAmountInDollar()*dollarexchangeCost);
					}
				}

		}
		companyMargins.put("paymentChargesSum", paymentChargesSum);
		companyMargins.put("dollarAmount", dollarexchangeCost);
		companyMargins.put(ConstantUtility.INVOICE_AMOUNT, invoiceAmountInr);
		companyMargins.put("invoiceAmountInDollar", invoiceAmountInDollar);
		
		Double payrollAmount=0.0;
		Double deductionAmount=0.0;
		Double totalLaptopAllowance=0.0;
		List<Payroll> payrolls=payrollRepository.findAllByMonthAndYearAndIsDeletedFalse(month, year);
		if(!payrolls.isEmpty()) {
			payrollAmount=payrolls.parallelStream().mapToDouble(payroll -> payroll.getNetPay()).sum();
			deductionAmount = payrolls.parallelStream().mapToDouble(payroll -> payroll.getTds() + payroll.getHealthInsurance()
							+ payroll.getEmployeePfContribution() + payroll.getInfraDeductions() /*+ payroll.getLeaveDeductions()*/).sum();
			totalLaptopAllowance=payrolls.parallelStream().mapToDouble(payroll -> payroll.getLaptopAllowance()).sum();
		}
		double salary=(payrollAmount+deductionAmount);
		double salaryWithoutLA=(payrollAmount+deductionAmount)-totalLaptopAllowance;
		companyMargins.put("payrollAmount", salary);
		companyMargins.put("payrollAmountExclLA", salaryWithoutLA);
		companyMargins.put("totalLaptopAllowance", totalLaptopAllowance);
		List<LeaveCostPercentage> prevCosts = leaveCostRepo.findAllByIsDeleted(false);
		LeaveCostPercentage leaveAmountPerc=prevCosts.get(0);
		double bufferPay=(((payrollAmount+deductionAmount)-totalLaptopAllowance)*leaveAmountPerc.getLeaveCostPercentage())/100;
		companyMargins.put("bufferPay", bufferPay);
		companyMargins.put("leaveAmountPerc", leaveAmountPerc.getLeaveCostPercentage());
		companyMargins.put("totalCtcSalary", salary+bufferPay);
		companyMargins.put(ConstantUtility.TOTAL_COST, salary+ bufferPay);
		return companyMargins;
	}

	@Override
	public Map<String, Object> getTotalCostDivision(String accessToken, int month, int year, String businessVertical, Map<String, Object> invoiceTotal) {
		List<Object> projectInvoice = loginUtilityService.objectToListConverter(invoiceTotal.get(ConstantUtility.INVOICE_PROJECT_ID));
		List<Object> projectsList = loginUtilityService.objectToListConverter(feignLegacyInterface.getBuWiseProjects(accessToken, businessVertical, month, year).get("data"));
		Double hourlyBillableIndirectCost = 0D;
		Double hourlyNonBillableIndirectCost=0D;
		List<Object> billableProjects = new ArrayList<>();
		List<Object> nonBillableProjects = new ArrayList<>();
		Double billableCost=0.0;
		Double nonBillableCost=0.0;
		Double totalBuSpecificCost = 0D;
		Map<String, Double> gradeWiseCosts =null;
		Double buCost=0.0;
		Map<String, Object> workingDaysData = (Map<String, Object>) feignLegacyInterface.getpayrollWorkingDays(accessToken,month, year).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		
		boolean isGradeWise=false;
		MarginBasis marginBasis=marginBasisRepository.findByMonthAndYear(month, year);
		List<BuSpecificCost> buSpecificCost = buSpecificCostRepository.findAllByYearAndMonthAndBusinessVerticalAndDeleted(year, month, businessVertical, false);
		if(marginBasis!=null)
			isGradeWise = marginBasis.getIsGradeWise();
		
		Map<String, Object> userListAndCount = feignLegacyInterface.gradeWiseExpectedHours(month, year);

		if(isGradeWise) {
			gradeWiseCosts = utilService.getGradeWiseCosts(month, year, accessToken, "",userListAndCount);
			buCost = indirectCostService.buIndirectCost(accessToken, businessVertical, month, Integer.toString(year),gradeWiseCosts);
		}
		else {
			hourlyBillableIndirectCost = indirectCostService.getIndirectCostHourly(accessToken, Integer.toString(year),businessVertical, month);
			hourlyNonBillableIndirectCost=indirectCostService.getIndirectCostHourly(accessToken, Integer.toString(year),"", month);
		}
		if(buSpecificCost != null){
			totalBuSpecificCost = buSpecificCost.stream().collect(Collectors.summingDouble(BuSpecificCost::getAmount));
		}
		if (!projectsList.isEmpty()) {
			Map<String, Object> projects = loginUtilityService.objectToMapConverter((projectsList.get(0)));
			List<Object> projectList = loginUtilityService.objectToListConverter(projects.get("projects"));
			int listSize=projectList.size();
			Boolean hasBillableProjects=false;
			for (int i = 0; i < listSize; i++) {
				Map<String,Object> projectData=loginUtilityService.objectToMapConverter(projectList.get(i));
				Long projectId=Long.parseLong(projectData.get(ConstantUtility.PROJECT_ID).toString());
				if(projectInvoice.contains(projectId)) {
					hasBillableProjects=true;
					break;
				}
			}

			List<ExcludePayrollDto> payrolls = new ArrayList<>();
			Map<String, Object> allCompensation = projectMarginservice.getAllCompensation(month,year);
			Map<String,Object> voluntaryPayData = (Map<String, Object>) allCompensation.get("voluntaryPayResponse");
			Map<String,Object> specialAllowanceData = (Map<String, Object>) allCompensation.get("specialAmountResponse");
			Map<String,Object> payDaysData =  (Map<String, Object>) allCompensation.get("PayDaysResponse");
			for (int i = 0; i < listSize; i++) {
				Map<String,Object> projectData=loginUtilityService.objectToMapConverter(projectList.get(i));
				Long projectId=Long.parseLong(projectData.get(ConstantUtility.PROJECT_ID).toString());
				String projectName=(String) projectData.get("projectName");
				Map<String,Object> projectMap=new HashMap<>();
				projectMap.put(ConstantUtility.PROJECT_ID, projectId);
				projectMap.put("projectName", projectName);
				if((projectInvoice.contains(projectId) || !hasBillableProjects) && !businessVertical.equals("Operations Support")) {
					if(hasBillableProjects) {
						projectMap=projectMarginservice.getProjectCost(projectData, month, year, accessToken, projectMap, 
								hourlyBillableIndirectCost,gradeWiseCosts,buCost,workingDays,marginBasis, payrolls, voluntaryPayData, specialAllowanceData,payDaysData);
						Double projectCost=(Double) projectMap.get(ConstantUtility.PROJECT_COST);
						billableCost=billableCost+projectCost;
						billableProjects.add(projectMap);
					}
					else {
						projectMap=projectMarginservice.getProjectCost(projectData, month, year, accessToken, projectMap, 
								hourlyBillableIndirectCost,gradeWiseCosts,0D,workingDays,marginBasis, payrolls, voluntaryPayData, specialAllowanceData,payDaysData);
						Double projectCost=(Double) projectMap.get(ConstantUtility.PROJECT_COST);
						billableCost=billableCost+projectCost;
						billableProjects.add(projectMap);
					}
				}
				else {
					if(businessVertical.equals("Operations Support")){
						projectMap=getNonBillableProjectCost(projectData, month, year, accessToken, projectMap, hourlyNonBillableIndirectCost,gradeWiseCosts,workingDays);
					}
					else
						projectMap=getNonBillableProjectCost(projectData, month, year, accessToken, projectMap, hourlyNonBillableIndirectCost,null,workingDays);
					Double projectCost=(Double) projectMap.get(ConstantUtility.PROJECT_COST);
					nonBillableCost=nonBillableCost+projectCost;
					nonBillableProjects.add(projectMap);
				}
			}
		}
		return getTotalCostMap(billableProjects, nonBillableProjects, billableCost, nonBillableCost,totalBuSpecificCost);
	}
	
	private Map<String, Object> getTotalCostMap(List<Object> billableProjects,List<Object> nonBillableProjects,Double billableCost,Double nonBillableCost, Double totalBuSpecificCost){
		Map<String, Object> totalCostMap=new HashMap<>();
		totalCostMap.put("billableProjects", billableProjects);
		totalCostMap.put("nonBillableProjects", nonBillableProjects);
		totalCostMap.put("billableCost", billableCost);
		totalCostMap.put("nonBillableCost", nonBillableCost);
		totalCostMap.put("totalBuSpecificCost", totalBuSpecificCost);
		return totalCostMap;
	}
	
	private Map<String,Object> getNonBillableProjectCost(Map<String,Object> projectData,int month,int year,String accessToken,Map<String,Object> projectMap,Double hourlyIndirectCost, Map<String, Double> gradeWiseCosts, Object workingDays){
		List<Object> teamData = loginUtilityService.objectToListConverter(projectData.get("teamData")) ;
		Integer projectHours=0;
		Integer projectMins=0;
		Double indirectCost=0.0;
		boolean isGradeWise=false;
		MarginBasis marginBasis=marginBasisRepository.findByMonthAndYear(month, year);
		if(marginBasis!=null)
			isGradeWise = marginBasis.getIsGradeWise();
		if (!teamData.isEmpty()) {
			for (int j = 0; j < teamData.size(); j++) {
				Map<String, Object> userData = loginUtilityService.objectToMapConverter(teamData.get(j));
				String expectedHours = userData.get(ConstantUtility.EXPECTED_HOURS).toString();
				String grade=userData.get("grade").toString();
				String mins=expectedHours.split("\\.")[1];
				String hours=expectedHours.split("\\.")[0];
				projectHours=projectHours+Integer.parseInt(hours);
				projectMins=projectMins+Integer.parseInt(mins);
				if(projectMins>=60) {
					projectHours=projectHours+1;
					projectMins=projectMins-60;
				}
				if(isGradeWise && gradeWiseCosts!=null) 
					hourlyIndirectCost=(gradeWiseCosts.get(grade))/(Double.parseDouble(workingDays.toString())*8);
				
				if (Double.parseDouble(mins) != 0D) {
					expectedHours = Double.toString(Double.parseDouble(hours) + (Double.parseDouble(mins) / 60));
				}
				indirectCost=indirectCost+Math.round((hourlyIndirectCost*Double.parseDouble(expectedHours))*100.0)/100.0;
				
			}
		}
		projectMap.put(ConstantUtility.PROJECT_COST, indirectCost);
		projectMap.put(ConstantUtility.INDIRECT_COST, Math.round(indirectCost*100.0)/100.0);
		projectMap.put("directCost", 0.0);
		projectMap.put(ConstantUtility.EXPECTED_HOURS, Double.parseDouble(projectHours+"."+projectMins));
		return projectMap;
	}
	
	//@Scheduled(cron = "0 0 3 * * *",zone="IST")
	public List<ReserveSnapShot> buReserveCrone(String accessToken) {
		try {
		log.info("Entering Reserve Snapshot Cron Service...");
		LocalDate date = LocalDate.now().minusMonths(1);
		int currentMonth = date.getMonthValue();
		int currentYear=date.getYear();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		Date yesterday = calendar.getTime();
		log.info("Current Month..."+currentMonth);
		log.info("Current Year..."+currentYear);
		List<ReserveSnapShot> resultList=new ArrayList<>();
		Map<String, Object> invoiceTotal=getBuWiseInvoiceTotal(currentMonth,Integer.toString(currentYear),accessToken);
		log.info("Fetched invoiceTotal...");
		List<Object> buWiseUsers=getCompanywiseData(currentMonth,currentYear,accessToken);
		log.info("Fetched buWiseUsers...");
		Map<String,Object> directCostTotal=getDirectCostBuWise(currentMonth,currentYear,buWiseUsers,invoiceTotal,accessToken);
		log.info("Fetched directCostTotal...");
			log.info("Fetched accessTokenData...");
		Map<String, Object> companyMargins=getCompanyMargin(accessToken,directCostTotal,invoiceTotal,currentMonth,currentYear);
		log.info("Fetched companyMargins...");
		Map<String, Double> disputedAmount=getAverageDisputedPercentage(currentYear,accessToken);
		log.info("Fetched disputedAmount...");
		Map<String, Double> disputedAmountLTM=getLTMBuDisputedPercentage(currentYear,currentMonth,accessToken);
		log.info("Fetched disputedAmountLTM...");
		Map<String, Object> response = getBuReserve(currentMonth, currentYear,companyMargins,disputedAmount,invoiceTotal,directCostTotal,buWiseUsers,disputedAmountLTM);
		log.info("Fetched bu REserve response...");
		List<Map<String, Object>>buList=(List<Map<String, Object>>) response.get("buWiseMargin");
		

		for(int i=0;i<buList.size();i++) {
		Map<String, Object> buMap=buList.get(i);
		ReserveSnapShot reserveSnapShot=new ReserveSnapShot();
		ReserveSnapShot preSnap = null;
		List<ReserveSnapShot> preSnaps = reserveSnapShotRepository.findAllByBuNameAndMonthAndYearAndCreationDate(buMap.get("businessVertical").toString(),
				currentMonth,currentYear,yesterday);
		if (!preSnaps.isEmpty())
			preSnap = preSnaps.get(preSnaps.size() - 1);
		if (preSnap == null || !preSnap.getMonthlyReserveAmount().toString().equals(buMap.get("reservedAmount").toString()))
			reserveSnapShot.setChanged(true);
		else
			reserveSnapShot.setChanged(false);
		
		reserveSnapShot.setBuName(buMap.get("businessVertical").toString());
		reserveSnapShot.setMonth(currentMonth);
		reserveSnapShot.setYear(currentYear);
		reserveSnapShot.setTotalReserve(Double.parseDouble(buMap.get("reservedAmount").toString()));
		reserveSnapShot.setIsDeleted(false);
		Date creationDate = DateUtils.truncate(new Date(), Calendar.DATE);

		reserveSnapShot.setCreationDate(creationDate);
		reserveSnapShot.setArchived(false);
		reserveSnapShot.setYtdDisputed(Double.parseDouble(buMap.get("disputedAmount").toString()));
		reserveSnapShot.setYtdDisputedPerc(Double.parseDouble(buMap.get("disputedPerc").toString()));
		
		reserveSnapShot.setNetMargin(Double.parseDouble(buMap.get("netMargin").toString()));
		reserveSnapShot.setNetMarginPerc(Double.parseDouble(buMap.get("netMarginPerc").toString()));

		reserveSnapShot.setTotalMargin(Double.parseDouble(buMap.get("totalMargin").toString()));
		reserveSnapShot.setTotalMarginPerc(Double.parseDouble(buMap.get("totalMarginPerc").toString()));

		reserveSnapShot.setDeductedAmount(Double.parseDouble(buMap.get("deductedAmount").toString()));
		reserveSnapShot.setMonthlyReserveAmount(Double.parseDouble(buMap.get("buReserve").toString()));
		reserveSnapShotRepository.save(reserveSnapShot);
		resultList.add(reserveSnapShot);
		}
		return resultList;
		}catch(Exception e) {
			e.printStackTrace();
			log.debug("buReserveCrone Exception : "+e.toString());
			return null;
		}
	}

	@Override
	public Map<String, Object> getBuReserve(int month, int year, Map<String, Object> companyMargins,
			Map<String, Double> disputedAmount, Map<String, Object> invoiceTotal,Map<String, Object> directCostTotal,
			List<Object> buWiseUsers,Map<String, Double> disputedAmountLTM) {
		Map<String, Object> response=new HashMap<>();

		Double dollarexchangeCost=dollarCostService.getAverageDollarCost(month, year);
		
		Double avgDollarCostYtd = disputedAmount.get("AverageDollarCost");
		Double avgDollarCostLtm = disputedAmountLTM.get("AverageDollarCost");
		List<Object> buMarginsList=(List<Object>) companyMargins.get("buWiseMargin");
		int listSize=buWiseUsers.size();
		List<Object> buList=new ArrayList<>();
		List<Object> invoiceList= (List<Object>) invoiceTotal.get("buWiseInvoice");
		Map<String,Object> invoiceMap=getInvoiceMapForReserve(invoiceList);

		double totalReserveAmount=0;
		double overallReserve=0;
		
		List<Object> buDirectCostList=loginUtilityService.objectToListConverter(directCostTotal.get("buWiseTotalCost"));
		Double companyTotalCost=Double.parseDouble(directCostTotal.get("companyTotalCost").toString());
		Boolean isLTM = Boolean.valueOf(directCostTotal.get("isLTM").toString());
		for(int i=0;i<listSize;i++) {
			Map<String, Object> buwiseUserData=(Map<String, Object>) buWiseUsers.get(i);
			String buName=buwiseUserData.get("businessVertical").toString();
			Double buDisputedAmount=0D;
			Double buTotalInvoiceAmount=0D;
			Double buDisputedAmountLTM=0D;
			Double buTotalInvoiceAmountLTM=0D;
			Map<String,Object> costObj=(Map<String, Object>) buDirectCostList.stream()
					.filter(currentObject -> ((Map<String,Object>) currentObject).get("businessVertical").toString().equals(buName)).findFirst().orElse(null);
			Map<String,Object> buData =  (Map<String, Object>) buMarginsList.stream()
					.filter(currentObject -> ((Map<String,Object>) currentObject).get("businessVertical").toString().equals(buName)).findFirst().orElse(null);
			
			Double totalCost=0D;
			if(costObj!=null && costObj.containsKey("projectCost")) 
				totalCost= Double.parseDouble( costObj.get("projectCost").toString());
			if(disputedAmount.get(buName)!=null)
				buDisputedAmount = disputedAmount.get(buName)*avgDollarCostYtd;
			if(disputedAmount.get(buName+"Total")!=null)
				buTotalInvoiceAmount = new Double(disputedAmount.get(buName+"Total").toString())*avgDollarCostYtd;

			if(disputedAmountLTM.get(buName + "LTM")!=null)
				buDisputedAmountLTM = disputedAmountLTM.get(buName + "LTM")*avgDollarCostLtm;
				
			if(disputedAmountLTM.get(buName+"TotalLTM")!=null)
				buTotalInvoiceAmountLTM = new Double(disputedAmountLTM.get(buName+"TotalLTM").toString())*avgDollarCostLtm;

			Map<String, Object> invoiceData=(Map<String, Object>) invoiceMap.get(buName);
            Double invoiceTotalAmount=0D;
            if(invoiceData!=null && invoiceData.containsKey("invoiceAmountInRupees"))
			    invoiceTotalAmount=(Double) invoiceData.get("invoiceAmountInRupees");
				
			Map<String, Object> buMap=new HashMap<>();
			buMap.put("totalMarginPerc", buData!=null && buData.containsKey("marginPerc")? buData.get("marginPerc"):0D);
			buMap.put("totalMargin", buData!=null && buData.containsKey("margin")? buData.get("margin") :0D);
			Double disputedPercentage = 0D;
			if(buDisputedAmount!=0 && buTotalInvoiceAmount!=0)
				disputedPercentage = (buDisputedAmount / buTotalInvoiceAmount) * 100;
			buMap.put("disputedPerc",Math.round( disputedPercentage*100.00)/100.00);
			Double disputedPercentageLTM = 0D;
			if(buDisputedAmountLTM!=0 && buTotalInvoiceAmountLTM!=0)
				disputedPercentageLTM = (buDisputedAmountLTM / buTotalInvoiceAmountLTM) * 100;
			buMap.put("disputedPercLTM",Math.round( disputedPercentageLTM*100.00)/100.00);
			double buDisputed = (disputedPercentage*invoiceTotalAmount)/100.00;
			buMap.put("disputedAmount",Math.round(buDisputed*100.00)/100.00);
			double buDisputedLTM=0;
			if(disputedAmountLTM.get(buName+"monthlyTotalLTM") != null){
				buDisputedLTM = ( Math.round(disputedPercentageLTM*100.00)/100.00 * (new Double(disputedAmountLTM.get(buName+"monthlyTotalLTM").toString())*dollarexchangeCost))/100.00;
			}
			buMap.put("disputedAmountLTM",Math.round(buDisputedLTM*100.00)/100.00);
			buMap.put("surplusReserve",0);
			buMap.put("targetReserve",Math.round((totalCost*3)*100.00)/100.00);
			double netMarginPerc = 0.0;
			Double netMargin = 0D;
			if (buData != null && buData.containsKey("marginPerc") && isLTM) {
				netMarginPerc = new Double(buData.get("marginPerc").toString()) - disputedPercentageLTM;
				Double buMonthlyDisputedAmount=0D;
				if(disputedAmountLTM.get(buName+"monthlyTotalLTM") != null)
					buMonthlyDisputedAmount = (Math.round(disputedPercentageLTM*100.00)/100.00 * (new Double(disputedAmountLTM.get(buName+"monthlyTotalLTM").toString())*dollarexchangeCost)) / 100;
				netMargin = new Double(buData.get("margin").toString()) - buMonthlyDisputedAmount;
			}
			else if (buData != null && buData.containsKey("marginPerc")) {
				netMarginPerc = new Double(buData.get("marginPerc").toString()) - disputedPercentage;
				Double buMonthlyDisputedAmount = (disputedPercentage * invoiceTotalAmount) / 100;
				netMargin = new Double(buData.get("margin").toString()) - buMonthlyDisputedAmount;
			}
			buMap.put("netMarginPerc", Math.round(netMarginPerc * 100.00) / 100.00);
			buMap.put("netMargin", Math.round(netMargin * 100.00) / 100.00);
			List<Map<String,Object>> monthwiseList=new ArrayList<>();
			Double lastTotal=0D;
			double totalDeduction=0;
			List<BuReserve> reserves=bureserveRepo.findAllByBuName(buName);
			Collections.sort(reserves, Comparator.comparingInt(BuReserve ::getYear));
			Collections.sort(reserves, Comparator.comparingInt(BuReserve ::getMonth).reversed());
			int lastMonth=LocalDateTime.now().minusMonths(1).getMonthValue();
			int lastYear=LocalDateTime.now().minusMonths(1).getYear();
			YearMonth currMonth=YearMonth.of(year, month);
			if(month>=LocalDateTime.now().getMonthValue() && year>=LocalDateTime.now().getYear()) {
				lastMonth=LocalDateTime.now().getMonthValue();
				lastYear=LocalDateTime.now().getYear();
			}
			if(year<LocalDateTime.now().minusMonths(1).getYear()) {
				lastMonth=12;
				lastYear=year;
			}
			YearMonth initialYearMonth=YearMonth.of(2021, 6);
			YearMonth lastMonthObj=YearMonth.of(lastYear, lastMonth);
			double carryForwardReserve = 0D;
			for(int j=0;j<reserves.size();j++) {
				YearMonth yearMonthObj=YearMonth.of(reserves.get(j).getYear(), reserves.get(j).getMonth());
				if (yearMonthObj.isBefore(lastMonthObj) || yearMonthObj.equals(lastMonthObj)) {
					if(!yearMonthObj.equals(currMonth)) {
						if(yearMonthObj.isAfter(initialYearMonth)) {
						lastTotal = lastTotal+reserves.get(j).getMonthlyReserveAmount();
						Map<String, Object> data = new HashMap<>();
						String monthLength =  String.format("%02d",reserves.get(j).getMonth());
						String monthName = new DateFormatSymbols().getMonths()[reserves.get(j).getMonth() - 1].toString();
						String element = reserves.get(j).getYear().toString() + monthLength;
						carryForwardReserve = carryForwardReserve + reserves.get(j).getMonthlyReserveAmount();
						data.put("month", monthName);
						data.put("year", reserves.get(j).getYear());
						data.put("monthlyReserveAmount", reserves.get(j).getMonthlyReserveAmount());
						data.put("deduction", reserves.get(j).getDeductedAmount());
						data.put("additionalData",Double.parseDouble(element));
						monthwiseList.add(data);
						Collections.reverse(monthwiseList);
						}
					}
					
				}
			}
			buMap.put("carryForwardReserve", carryForwardReserve);
			
			List<DeductionResponseDto> deductions=new ArrayList<>();
			List<BuReserveDeductions> buDeductions=deductionRepo.findAllByBuNameAndIsDeleted(buName,false);
			totalDeduction=buDeductions.stream().collect(Collectors.summingDouble(BuReserveDeductions::getDeductedAmount));
			buDeductions.forEach(deduction->{
				if(deduction.getMonth()==month && deduction.getYear()==year) {
					DeductionResponseDto responseObj=new DeductionResponseDto();
					BeanUtils.copyProperties(deduction, responseObj);
					deduction.setBuExpenses(responseObj.getBuExpenses());
					deductions.add(responseObj);
				}
			});
			buMap.put("deductions",deductions);
			double availableReserve=0;
			double buReservePerc=0;
			double reserveAmount=0;
			BuReserve bureserve=bureserveRepo.findAllByYearAndBuNameAndMonth(year, buName, month);
			Boolean isReserveChanged=false;
			YearMonth lastMonthYearObj=YearMonth.of(LocalDateTime.now().minusMonths(1).getYear(), LocalDateTime.now().minusMonths(1).getMonthValue());
			if(bureserve==null || bureserve.isReserveChanged()) {
				buMap.put("totalMarginPerc", buData!=null && buData.containsKey("marginPerc")? buData.get("marginPerc"):0D);
				buMap.put("totalMargin", buData!=null && buData.containsKey("margin")? new Double(buData.get("margin").toString()):0D);
				if(buDisputedAmount!=0 && buTotalInvoiceAmount!=0)
					disputedPercentage = (buDisputedAmount / buTotalInvoiceAmount) * 100;
				buDisputed = (disputedPercentage*invoiceTotalAmount)/100.00;
				if(buDisputedAmountLTM!=0 && buTotalInvoiceAmountLTM!=0)
					disputedPercentageLTM = (buDisputedAmountLTM / buTotalInvoiceAmountLTM) * 100;
				if(disputedAmountLTM.get(buName+"monthlyTotalLTM") != null)
					buDisputedLTM = (Math.round(disputedPercentageLTM*100.00)/100.00 * (new Double(disputedAmountLTM.get(buName+"monthlyTotalLTM").toString())*dollarexchangeCost)) / 100;
				buMap.put("surplusReserve",0);
				buMap.put("targetReserve",Math.round((totalCost*3)*100.00)/100.00);
				
				if (buData != null && buData.containsKey("marginPerc") && isLTM) {
					netMarginPerc = new Double(buData.get("marginPerc").toString()) - disputedPercentageLTM;
					Double buMonthlyDisputedAmount = 0D;
					if(disputedAmountLTM.get(buName+"monthlyTotalLTM") != null)
						buMonthlyDisputedAmount = (Math.round(disputedPercentageLTM*100.00)/100.00 * (new Double(disputedAmountLTM.get(buName+"monthlyTotalLTM").toString())*dollarexchangeCost)) / 100;

					netMargin = new Double(buData.get("margin").toString()) - buMonthlyDisputedAmount;
				}
				else if (buData != null && buData.containsKey("marginPerc")) {
					netMarginPerc = new Double(buData.get("marginPerc").toString()) - disputedPercentage;
					Double buMonthlyDisputedAmount = (disputedPercentage * invoiceTotalAmount) / 100;
					netMargin = new Double(buData.get("margin").toString()) - buMonthlyDisputedAmount;
				}
				buMap.put("netMarginPerc", Math.round(netMarginPerc * 100.00) / 100.00);
				buMap.put("netMargin", Math.round(netMargin * 100.00) / 100.00);
				isReserveChanged=true;
			}
			else {
				buMap.put("totalMarginPerc", bureserve!=null ? bureserve.getMarginPerc():0D);
				buMap.put("totalMargin", bureserve!=null ? Math.round(bureserve.getMargin() * 100.00) / 100.00 :0D);
				disputedPercentage = bureserve.getDisputedPerc();
				buDisputed = bureserve.getDisputedAmount();
				if(buDisputedAmountLTM!=0 && buTotalInvoiceAmountLTM!=0)
					disputedPercentageLTM = (buDisputedAmountLTM / buTotalInvoiceAmountLTM) * 100;
				if(disputedAmountLTM.get(buName+"monthlyTotalLTM") != null)
					buDisputedLTM = (Math.round(disputedPercentageLTM*100.00)/100.00 * (new Double(disputedAmountLTM.get(buName+"monthlyTotalLTM").toString())*dollarexchangeCost)) / 100;
				
				buMap.put("disputedAmount",Math.round(bureserve.getDisputedAmount()*100.00)/100.00);
				buMap.put("disputedAmountLTM",Math.round(buDisputedLTM*100.00)/100.00);
				buMap.put("surplusReserve",0);
				buMap.put("targetReserve",Math.round((totalCost*3)*100.00)/100.00);
				if(isLTM){
					netMarginPerc = new Double(bureserve.getMarginPerc().toString()) - disputedPercentageLTM;
					Double buMonthlyDisputedAmount = 0D;
					if(disputedAmountLTM.get(buName+"monthlyTotalLTM") != null)
						buMonthlyDisputedAmount = (Math.round(disputedPercentageLTM*100.00)/100.00 * (new Double(disputedAmountLTM.get(buName+"monthlyTotalLTM").toString())*dollarexchangeCost)) / 100;
					netMargin = new Double(bureserve.getMargin().toString()) - buMonthlyDisputedAmount;

				}
				else{
					netMarginPerc = new Double(bureserve.getMarginPerc().toString()) - disputedPercentage;
					Double buMonthlyDisputedAmount = (disputedPercentage * invoiceTotalAmount) / 100;
					netMargin = new Double(bureserve.getMargin().toString()) - buMonthlyDisputedAmount;

				}	
		
				buMap.put("netMarginPerc", Math.round(netMarginPerc * 100.00) / 100.00);
				buMap.put("netMargin", Math.round(netMargin * 100.00) / 100.00);
				
			}
			buMap.put("disputedPerc",Math.round( disputedPercentage*100.00)/100.00);
			buMap.put("disputedAmount",Math.round(buDisputed*100.00)/100.00);
			buMap.put("disputedPercLTM",Math.round( disputedPercentageLTM*100.00)/100.00);
			buMap.put("disputedAmountLTM",Math.round(buDisputedLTM*100.00)/100.00);

			log.info("isReserveChanged..."+isReserveChanged);
			String monthName = new DateFormatSymbols().getMonths()[month- 1].toString();
			Months monthEnum = Months.valueOf(monthName.toUpperCase());
			IndirectCost monthlyIndirectCost = costRepository.findByYearAndIsDeletedAndMonth(Integer.toString(year), false, monthEnum);
			buReservePerc=netMarginPerc-10;
			if(monthlyIndirectCost!=null)
				reserveAmount=(buReservePerc* invoiceTotalAmount)/100;
			totalReserveAmount=totalReserveAmount+reserveAmount;
			lastTotal=lastTotal+reserveAmount;
				
			if(isReserveChanged || (lastMonthYearObj.equals(currMonth))) {
				bureserve = setBureserve(month, year, reserveAmount, lastTotal, buName,monthlyIndirectCost);
			}
			Map<String, Object> data = new HashMap<>();
			String monthLength =  String.format("%02d",month);
			String element = bureserve.getYear().toString() + monthLength;
			data.put("additionalData",Double.parseDouble(element));
			data.put("month", monthName);
			data.put("monthlyReserveAmount", 0);
			data.put("deduction", 0);
			buMap.put("deductedAmount", 0);
			buMap.put("remarks", "");
			availableReserve=lastTotal-totalDeduction;
			buMap.put("buReservePerc", Math.round(buReservePerc*100.00)/100.00);
			buMap.put("invoiceTotalAmount", Math.round(invoiceTotalAmount*100.00)/100.00);
			buMap.put("buReserve", Math.round(reserveAmount*100.00)/100.00);
			
			if(bureserve!=null) {
				data.put("monthlyReserveAmount",monthlyIndirectCost!=null? bureserve.getMonthlyReserveAmount():0D);
				data.put("deduction", bureserve.getDeductedAmount());
				data.put("year", bureserve.getYear());
				buMap.put("deductedAmount", Math.round(bureserve.getDeductedAmount()*100.00)/100.00);
				buMap.put("remarks", bureserve.getRemarks());
				if (isReserveChanged || (lastMonthYearObj.equals(currMonth))) {
					bureserve.setRevenue(invoiceTotalAmount);
					bureserve.setDisputedAmount(buDisputed);
					bureserve.setDisputedPerc(disputedPercentage);
					bureserve.setTotalCost(totalCost);
					bureserve.setTargetReserve(totalCost*3);
					if (buData != null && buData.containsKey("marginPerc") && monthlyIndirectCost!=null) {
						bureserve.setMargin(Double.parseDouble(buData.get("margin").toString()));
						bureserve.setMarginPerc(Double.parseDouble(buData.get("marginPerc").toString()));
					}
					else {
						bureserve.setMargin(0D);
						bureserve.setMarginPerc(0D);
					}
					bureserve.setSurplusReserve(0D);
					if (availableReserve > (totalCost * 3))
						bureserve.setSurplusReserve(availableReserve - (totalCost * 3));
					bureserveRepo.save(bureserve);
				}
			}
			monthwiseList.add(data);
			Collections.sort(monthwiseList, (m1, m2)->new Double(m1.get("additionalData").toString()).compareTo(new Double(m2.get("additionalData").toString())));
			Collections.reverse(monthwiseList);
			buMap.put("monthwiseList", monthwiseList);
			buMap.put("reservedAmount", 0);
			if(availableReserve>0) {
				overallReserve= overallReserve+availableReserve;
			buMap.put("reservedAmount", Math.round(availableReserve * 100.00) / 100.00);
			}
			if(availableReserve>(totalCost*3))
				buMap.put("surplusReserve",Math.round((availableReserve-(totalCost*3))*100.00)/100.00);
			buMap.put("businessVertical", buName);
			buList.add(buMap);
		}
		response.put("buWiseMargin", buList);
		response.put("totalReserveAmount", Math.round(totalReserveAmount*100.00)/100.00);
		response.put("overallReserve", Math.round(overallReserve*100.00)/100.00);
		response.put("overallTargetReserve", 0);
		response.put("overallSurplusReserve", Math.round((companyTotalCost*3)*100.00)/100.00);
		if(overallReserve>(companyTotalCost*3))
			response.put("overallSurplusReserve", Math.round((overallReserve-(companyTotalCost*3))*100.00)/100.00);
		return response;
	}


	
	public Map<String,Object> getInvoiceMapForReserve(List<Object> buInvoiceList){
		int listSize=buInvoiceList.size();
		Map<String,Object> invoiceMap=new HashMap<>();
		for(int i=0;i<listSize;i++) {
			Map<String,Object> buData=(Map<String, Object>) buInvoiceList.get(i);
			invoiceMap.put(buData.get("businessVertical").toString(), buData);
		}
		return invoiceMap;
	}
	
	@Cacheable("ytdBuDisputedPerc")
	@Override
	public Map<String, Double> getAverageDisputedPercentage(Integer year, String accessToken) {
		Double totalAmount = 0.0;
		Map<String, Double> responseMap=new HashMap<>();
		List<ProjectInvoice> invoices = projectInvoiceRepository.findAllByYearAndIsDeletedAndIsInternalAndInvoiceStatusNot(year.toString(),
				false, false,6L);
		Map<String, Integer> monthMap=new HashMap<>();
		Map<Long, String> projectMap=new HashMap<>();
		List<Long> projectIds = invoices.stream().map(inv -> inv.getProjectId()).collect(Collectors.toList());
		
		List<Map<String,Object>> projectDetailList  = feignLegacyInterface.findProjectDescriptionList(accessToken , projectIds, null, year);
		for (ProjectInvoice invoice : invoices) {
			Integer monthnum=0;
			if(monthMap.containsKey(invoice.getMonth().toString()))
				monthnum = monthMap.get(invoice.getMonth().toString());
			else {
				monthnum=loginUtilityService.getMonthNumber(invoice.getMonth());
				monthMap.put(invoice.getMonth(), monthnum);
			}
			String projectBusinessVertical = "UnAssigned";
			if(projectMap.containsKey(invoice.getProjectId())) {
				projectBusinessVertical = projectMap.get(invoice.getProjectId());
			}
			else {
				for(Map<String,Object> map : projectDetailList){
//					if(map.get(ConstantUtility.PROJECT_ID).toString().equals("1436")) {
//						System.out.println(":::::::::::::::::::"+map);
//					}
					if(map.get(ConstantUtility.PROJECT_ID).toString().equals(invoice.getProjectId().toString())){
//						Map<Integer,Object> monthBuMap = loginUtilityService.objectToIntMapConverter(map.get("monthBuMap"));
//						if (!monthBuMap.isEmpty()){
//						Map<String,Object> buDetailMap = loginUtilityService.objectToMapConverter(monthBuMap.get(monthnum));
//						if(buDetailMap != null && !buDetailMap.isEmpty())
							projectBusinessVertical = map.get("businessVertical").toString();
//						}
					}
				}
				projectMap.put(invoice.getProjectId(), projectBusinessVertical);
			}
			Double buAmount= 0D;
			Double totalBuAmount = 0D;
			if(invoice.getInvoiceStatus() != 6 && !invoice.getIsInternal()) {
			if (invoice.getInvoiceStatus() == 5) {
				if(responseMap.containsKey(projectBusinessVertical))	{
					buAmount=(Double) responseMap.get(projectBusinessVertical);
				}
				responseMap.put(projectBusinessVertical, buAmount+invoice.getAmountInDollar());
				totalAmount = totalAmount + invoice.getAmountInDollar();
			}
			if(responseMap.containsKey(projectBusinessVertical+"Total"))	{
				totalBuAmount=(Double) responseMap.get(projectBusinessVertical+"Total");
			}
			responseMap.put(projectBusinessVertical+"Total", totalBuAmount+invoice.getAmountInDollar());
//			if(projectBusinessVertical.equals("ERP Solution")) {
//				System.out.print(":::::::::::::"+projectBusinessVertical);
//				System.out.println(":::::::::::::Status:::::::::::::"+invoice.getInvoiceStatus());
//				System.out.println(":::::::::::::disputedAmount:::::::::::::"+responseMap.get(projectBusinessVertical));
//				System.out.println(":::::::::::::total:::::::::::::"+responseMap.get(projectBusinessVertical+"Total"));
//			}
		}
		}
		Calendar fromCal = Calendar.getInstance();
		fromCal.set(Calendar.YEAR, Integer.parseInt(year.toString()));
		fromCal.set(Calendar.MONTH, Calendar.JANUARY);
		Calendar toCal = Calendar.getInstance();
		toCal.set(Calendar.YEAR, Integer.parseInt(year.toString()));
		toCal.set(Calendar.MONTH, Calendar.DECEMBER);
		List<Map<String,Object>> avgDollarCost = new ArrayList<>();
		Double avgCost = 1D;
		avgDollarCost = dollarCostService.getMonthWiseAverageDollarCost(fromCal.getTime(), toCal.getTime());
		for(Map<String,Object> map : avgDollarCost){
			if(map.containsKey("AverageCost")){
				avgCost=Double.parseDouble(map.get("AverageCost").toString());
				break;
			}
			else
				continue;
		}
		responseMap.put("totalAmount", totalAmount);
		responseMap.put("AverageDollarCost", avgCost);


		return responseMap;
	}

	@Cacheable(value="ltmBuDisputedPerc", key="{#year, #month}")
	@Override
	public Map<String, Double> getLTMBuDisputedPercentage(Integer year,Integer month,String accessToken) {
		Double totalAmountLTM =0.0;
		Map<String, Double> responseMap=new HashMap<>();
		List<ProjectInvoice> totalInvoices = new ArrayList<>();
		List<String> yearList = new ArrayList<>();
		yearList.add(year.toString());
		yearList.add(String.valueOf(year-1));
		
		totalInvoices = projectInvoiceRepository.findAllByIsDeletedAndIsInternalAndInvoiceStatusNotAndYearIn(
				false,false,6L,yearList);
		List<Long> projectIds = totalInvoices.stream().map(inv -> inv.getProjectId()).collect(Collectors.toList());
		List<Map<String,Object>> projectDetailCurrentYr  = feignLegacyInterface.findProjectDescriptionList(accessToken , projectIds, null, year);
		List<Map<String,Object>> projectDetailPreviousYr  = feignLegacyInterface.findProjectDescriptionList(accessToken , projectIds, null, year-1);
		Map<String, Integer> monthMap=new HashMap<>();
		Map<Long, String> projectMap=new HashMap<>();
		List<ProjectInvoice> totalInvoicesList = new ArrayList<>();

		
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if(month != null){
			cal.set(Calendar.MONTH, month-1);
		}
		cal.set(Calendar.YEAR,year);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DATE));
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(cal.getTime());
		cal1.add(Calendar.MONTH, -11);
		cal1.set(Calendar.DAY_OF_MONTH, 1);
		
		
		for(ProjectInvoice inv : totalInvoices){
			Calendar invCal = Calendar.getInstance();
			invCal.setTime(date);
			invCal.set(Calendar.YEAR,Integer.parseInt(inv.getYear()));
			invCal.set(Calendar.MONTH,Month.valueOf(inv.getMonth().toUpperCase()).getValue()-1);
			invCal.set(Calendar.DAY_OF_MONTH, 15);
			if(invCal.getTime().before(cal.getTime()) && invCal.getTime().after(cal1.getTime()) ){
				totalInvoicesList.add(inv);
			}
		}
		
		for (ProjectInvoice invoice : totalInvoicesList) {
			Integer monthnum=0;
			if(monthMap.containsKey(invoice.getMonth().toString()))
				monthnum = monthMap.get(invoice.getMonth().toString());
			else {
				monthnum=loginUtilityService.getMonthNumber(invoice.getMonth());
				monthMap.put(invoice.getMonth(), monthnum);
			}

			String projectBusinessVertical = "UnAssigned";
			if(projectMap.containsKey(invoice.getProjectId())) {
				projectBusinessVertical = projectMap.get(invoice.getProjectId());
			}
			else {
				if(Integer.valueOf(invoice.getYear()).equals(year)){
					for(Map<String,Object> map : projectDetailCurrentYr){
						if(map.get(ConstantUtility.PROJECT_ID).toString().equals(invoice.getProjectId().toString())){
//							Map<Integer,Object> monthBuMap = loginUtilityService.objectToIntMapConverter(map.get("monthBuMap"));
//							if (!monthBuMap.isEmpty()){
//							Map<String,Object> buDetailMap = loginUtilityService.objectToMapConverter(monthBuMap.get(monthnum));
//							if(buDetailMap != null && !buDetailMap.isEmpty())
//								projectBusinessVertical = (String)buDetailMap.get("name");
//							}
							projectBusinessVertical = map.get("businessVertical").toString();

						}
					}
				}
				else{
					for(Map<String,Object> map : projectDetailPreviousYr){
						if(map.get(ConstantUtility.PROJECT_ID).toString().equals(invoice.getProjectId().toString())){
							
							Map<Integer,Object> monthBuMap = loginUtilityService.objectToIntMapConverter( map.get("monthBuMap"));
							
//							if (!monthBuMap.isEmpty()) {
//								Map<String, Object> buDetailMap = loginUtilityService
//										.objectToMapConverter(monthBuMap.get(monthnum));
//								if(buDetailMap != null && !buDetailMap.isEmpty())
//									projectBusinessVertical = (String) buDetailMap.get("name");
//								
//							}
							projectBusinessVertical = map.get("businessVertical").toString();

						}
					}
				}
				
				projectMap.put(invoice.getProjectId(), projectBusinessVertical);

			}
			
			Double buAmountLTM= 0D;
			Double totalBuAmountLTM = 0D;
			Double monthlyBuTotalLTM = 0D;
			if (invoice.getInvoiceStatus() == 5) {
				if(responseMap.containsKey(projectBusinessVertical+"LTM"))	{
					
					buAmountLTM = (Double) responseMap.get(projectBusinessVertical+"LTM");
				}
				
				responseMap.put(projectBusinessVertical + "LTM", buAmountLTM+invoice.getAmountInDollar());
				totalAmountLTM = totalAmountLTM + invoice.getAmountInDollar();
				
			}
			if(responseMap.containsKey(projectBusinessVertical+"TotalLTM"))	{
				
				totalBuAmountLTM = (Double) responseMap.get(projectBusinessVertical+"TotalLTM");
			}
			responseMap.put(projectBusinessVertical+"TotalLTM", totalBuAmountLTM+invoice.getAmountInDollar());
			if (monthMap.get(invoice.getMonth()).equals(month)) {
				if (responseMap.containsKey(projectBusinessVertical + "monthlyTotalLTM")) {
					monthlyBuTotalLTM = (Double) responseMap.get(projectBusinessVertical + "monthlyTotalLTM");
				}
				responseMap.put(projectBusinessVertical + "monthlyTotalLTM",
						monthlyBuTotalLTM + invoice.getAmountInDollar());
			}
		}
		List<Map<String,Object>> avgDollarCost = new ArrayList<>();
		Double avgCost = 1D;
		avgDollarCost = dollarCostService.getMonthWiseAverageDollarCost(cal1.getTime(), cal.getTime());
		for(Map<String,Object> map : avgDollarCost){
			if(map.containsKey("AverageCost")){
				avgCost=Double.parseDouble(map.get("AverageCost").toString());
				break;
			}
			else
				continue;
		}
		responseMap.put("totalAmountLTM", totalAmountLTM);
		responseMap.put("AverageDollarCost", avgCost);
		return responseMap;
	}
	
	@Override
	public Map<String, Object> getIndirectCostDivision(String accessToken, int month, int year, Long projectId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public BuReserve setBureserve(int month,int year,double reserveAmount,double totalAmount,String buName, IndirectCost indirectCost) {
		BuReserve buReserve=bureserveRepo.findAllByYearAndBuNameAndMonth(year, buName, month);
		if(buReserve==null && indirectCost!=null) {
			buReserve = new BuReserve();
			buReserve.setReserveChanged(true);
		}
		if (buReserve != null) {
			buReserve.setMonth(month);
			buReserve.setYear(year);
			buReserve.setBuName(buName);
			buReserve.setMonthlyReserveAmount(reserveAmount);
			buReserve.setTotalReserve(totalAmount);
			bureserveRepo.save(buReserve);
		}
		return buReserve;
	}
	
	public Double getReimbursementCost(String accessToken,int month,int year) {
		List<Arrear> arrears= arrearRepo.findAllByCreationMonthAndCreationYearAndIsReimbursementAndIsArrearIncludedAndIsDeleted(month, year, true, false,false);
		Double reimbursementCost=0.0;
		if(!arrears.isEmpty())
			reimbursementCost= arrears.parallelStream().mapToDouble(arrear -> arrear.getArrearAmount()).sum();
		return reimbursementCost;
	
	}
	public Double getReimbursementCostV2(String accessToken,int month,int year,List<Arrear> arrearsList) {
		List<Arrear> arrears=arrearsList.stream().filter(arr-> String.valueOf(arr.getCreationMonth()).equals(String.valueOf(month))).collect(Collectors.toList());
		Double reimbursementCost=0.0;
		if(!arrears.isEmpty())
			reimbursementCost= arrears.parallelStream().mapToDouble(arrear -> arrear.getArrearAmount()).sum();
		return reimbursementCost;
	
	}

	@Override
	public List<Map<String, Object>> getYearlyRevenueForecast(String accessToken, Integer year,
			String businessVertical) {
		List<Map<String, Object>> responseList=new ArrayList<>();
		Map<String,Object>  yearlyData=(Map<String, Object>) feignLegacyInterface.getYearlyProjects(accessToken,year,businessVertical).get("data");
		for(int month=1;month<=12;month++) {
			List<Map<String,Object>>  monthlyData=(List<Map<String, Object>>) yearlyData.get(String.valueOf(month));
			String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
			Double monthlyExpectedCost=0D;
			for(int i=0;i<monthlyData.size();i++) {
				if(monthlyData.get(i)!=null && !monthlyData.get(i).isEmpty() && monthlyData.get(i).containsKey("team")){
					Map<String,Object> projectdata=(Map<String, Object>) monthlyData.get(i);
					List<Object> teamList=(List<Object>) monthlyData.get(i).get("team");

					Long projectId=Long.parseLong(projectdata.get(ConstantUtility.PROJECT_ID).toString());
					Boolean isInternal=Boolean.parseBoolean(projectdata.get("isInternal").toString()) ;
					Boolean isBillable=true;
					if(isInternal)
						isBillable=checkIsBillable(projectId, month, year);
					if (!isInternal || (isInternal && isBillable)) {
						Map<String, Double> billingRateMap = invoicePipelineService.getBillingRates(month, year);
						Double expectedBilling = consolidatedService.getExpectedBillingRate(teamList, billingRateMap,
								"forecastedRevenue");
								System.out.println("::::::::projectId " + projectId + " monthName " + monthName);
								System.out.println(" billingRateMap= " + billingRateMap.toString());
								System.out.println(" expectedBilling " + expectedBilling);
						projectdata.put("expectedBilling", expectedBilling);
						monthlyExpectedCost = monthlyExpectedCost + Math.round(expectedBilling * 100.00) / 100.00;
					}
				}
			}
			Map<String,Object> responseMap=new HashMap<>();
			responseMap.put("month", monthName);
			responseMap.put("forecastedRevenue", monthlyExpectedCost);
			responseList.add(responseMap);
		}
		return responseList;
	}
	
	private Boolean checkIsBillable(Long projectId,Integer month,Integer year) {
		Query query = entityManager.createNativeQuery("select id from project_invoice where project_id=:projectId and month=:month and year=:year");
		query.setParameter("month", month);
		query.setParameter("year", year);
		query.setParameter("projectId", projectId);
		List<Long> monthlyInvoice = query.getResultList();
		if (!monthlyInvoice.isEmpty())
			return true;
		return false;
		
	}
}
