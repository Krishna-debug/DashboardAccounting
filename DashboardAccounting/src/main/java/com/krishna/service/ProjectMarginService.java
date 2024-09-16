package com.krishna.service;

import java.text.DateFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.action.internal.CollectionAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.AtomicDouble;
import com.krishna.controller.FeignLegacyInterface;
import com.krishna.domain.BuSpecificCost;
import com.krishna.domain.GradeBasedIndirectCost;
import com.krishna.domain.IndirectCost;
import com.krishna.domain.PayRegister;
import com.krishna.domain.PayRevisions;
import com.krishna.domain.Payroll;
import com.krishna.domain.UserModel;
import com.krishna.domain.Margin.MarginBasis;
import com.krishna.domain.Margin.ProjectExpectedHours;
import com.krishna.domain.Margin.ProjectSnapshots;
import com.krishna.domain.Margin.TeamExpectedhours;
import com.krishna.domain.Margin.UserSnapshots;
import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.dto.ExcludePayrollDto;
import com.krishna.dto.IndirectCostGradeBasedDTO;
import com.krishna.enums.Months;
import com.krishna.repository.BuSpecificCostRepository;
import com.krishna.repository.GradeBasedIndirectCostRepository;
import com.krishna.repository.IndirectCostRepository;
import com.krishna.repository.MarginBasisRepository;
import com.krishna.repository.ProjectExpectedHoursRepository;
import com.krishna.repository.ProjectSnapshotRepository;
import com.krishna.repository.UserExpectedHoursRepository;
import com.krishna.repository.UserSnapshotsRepository;
import com.krishna.repository.invoice.ProjectInvoiceRepository;
import com.krishna.repository.payroll.PayRegisterRepository;
import com.krishna.repository.payroll.PayRevisionRepository;
import com.krishna.repository.payroll.PayrollRepository;
import com.krishna.security.JwtValidator;
import com.krishna.service.util.ConsolidatedService;
import com.krishna.service.util.UtilityService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.DoubleEncryptDecryptConverter;

@Service
public class ProjectMarginService {

	@Autowired
	PayRegisterRepository payRegisterRepository;

	@Autowired
	IndirectCostService indirectCostService;

	@Autowired
	PayRevisionRepository payRevisionRepository;

	@Autowired
	ProjectInvoiceRepository projectInvoiceRepository;

	@Autowired
	IndirectCostRepository costRepository;

	@Autowired
	ProjectInvoiceService projectInvoiceService;

	@Autowired
	LoginUtiltiyService loginUtilityService;

	@Autowired
	DollarCostServiceImpl dollarCostService;

	@Autowired
	ConsolidatedService consolidatedService;

	@Autowired
	ProjectExpectedHoursRepository expectedHoursRepository;

	@Autowired
	ProjectSnapshotRepository snapshotRepository;
	
	@Autowired
	UserExpectedHoursRepository userExpectedHoursRepo;
	
	@Autowired
	UserSnapshotsRepository userSnapshotsRepository;
	
	@Autowired
	GradeBasedIndirectCostRepository gradeBasedIndirectCostRepository;
	
	@Autowired
	MarginBasisRepository marginBasisRepository;
	
	@Autowired
	PayrollTrendsImpl payrollTrendsService;
	
	@Autowired
	GradeBasedIndirectCostRepository gradeBasedCostRepository;
	
	@Autowired
	UtilityService utilService;
	
	@Autowired
	PayrollRepository payrollRepository;

	Logger log = LoggerFactory.getLogger(ProjectMarginService.class);

	@Autowired
	JwtValidator validator;
	
	@Autowired
	FeignLegacyInterface feignLegacyInterface;

	@Autowired
	BuSpecificCostRepository buSpecificCostRepository;
	
	@Autowired
	EntityManager entityManager;
	
	private static Map<String, Object> GRADE_WISE_FIXED_COST = null;

	@SuppressWarnings("unchecked")
	@Cacheable("currentMonthMargin")
	//Juhi
	public Map<String, Object> getDirectCost(long projectId, int month, int year, String accessToken,
			boolean isAccountUser/* , boolean isGradeWise */) {
		UserModel currentUser = validator.tokenbValidate(accessToken);
		
		Long currentUserRank = 1L;
		if (currentUser != null)
			currentUserRank = currentUser.getRank();
		YearMonth yearMonth = YearMonth.of(year, month);
		Object workingDays = getWorkingDays(accessToken, month, year);
		Map<String, Object> expectedHoursData =feignLegacyInterface.getMarginExpectedHours(accessToken, projectId, month - 1, year);
		List<Map<String,Object>> data = expectedHoursData!=null?
				 (List<Map<String,Object>>) expectedHoursData.get("data"):new ArrayList<>();
		Map<String, Object> projectDetails = (Map<String, Object>) feignLegacyInterface.getProjectDescription(projectId,"",month,year).get("data");
		String projectBusinessVertical = (String) projectDetails.get(ConstantUtility.BUSINESS_VERTICAL);

		List<Object> teamdata = new ArrayList<>();
		Double projectDirectCost = 0.0;
		Double projectInDirectCost = 0.0;
		Map<String, Object> marginData = new HashMap<>();
		marginData = getInvoiceData(month, marginData, year, projectId);
		Double invoiceAmount = (Double) marginData.get(ConstantUtility.INVOICE_AMOUNT_IN_RUPEE);
		Double hourlyIndirectCost=0D;
		Map<String,Double> gradeWiseCosts=null;
		Double buCost=0.0;
		boolean isGradeWise=false;
		MarginBasis marginBasis=marginBasisRepository.findByMonthAndYear(month, year);
		if(marginBasis!=null)
			isGradeWise = marginBasis.getIsGradeWise();
		if(isGradeWise) {
			gradeWiseCosts=utilService.getGradeWiseCostsV2(month, year, accessToken, "", "");
			buCost=indirectCostService.buIndirectCostV3(accessToken, projectBusinessVertical, month, Integer.toString(year),workingDays, gradeWiseCosts);
		}
		else
			hourlyIndirectCost = getHourlyIndirectCost(invoiceAmount, projectBusinessVertical, accessToken, year,month);
		Map<String, Object> managementData = new HashMap<>();
		int teamSize = data.size();
		Map<String,Object> variablePays=utilService.getVariableAmounts(month, year);
		
		Map<String, Object> allCompensation = getAllCompensation(month,year);
		Map<String,Object> incentives = (Map<String, Object>) allCompensation.get("IncentivesResponse");
		Map<String,Object> voluntaryPayAmount = (Map<String, Object>) allCompensation.get("voluntaryPayResponse");
		Map<String,Object> specialAllowanceData = (Map<String, Object>) allCompensation.get("specialAmountResponse");
		Map<String,Object> payDaysData =  (Map<String, Object>) allCompensation.get("PayDaysResponse");

		double totalSpecialAllowanceAmount=0D;
		double totalVoluntaryPayAmount=0D;
		if (hourlyIndirectCost != 0.0 || gradeWiseCosts!=null) {
			for (int i = 0; i < teamSize; i++) {
				Map<String, Object> userData = (Map<String, Object>) data.get(i);
				Object userId = userData.get(ConstantUtility.USER_ID);
				String expectedHours = userData.get(ConstantUtility.EXPECTED_HOURS).toString();
				String mins=expectedHours.split("\\.")[1];
				Double expHours=Double.parseDouble(expectedHours.split("\\.")[0].toString()) ;
				if (Double.parseDouble(mins) != 0D) {
					expHours = expHours + (Double.parseDouble(mins) / 60);
				}
				if(specialAllowanceData.containsKey(userId.toString())) {
					log.info(":::specialAllowanceData:::"+specialAllowanceData.get(userId.toString()).toString());
					log.info(":::padDays  * 8:::"+Double.parseDouble(payDaysData.get(userId.toString()).toString()) *8);
					log.info(":::hr:::"+Double.parseDouble(expHours.toString()));
					
					double specialAllowance=(Double.parseDouble(specialAllowanceData.get(userId.toString()).toString())/(Double.parseDouble(payDaysData.get(userId.toString()).toString()) *8));
					totalSpecialAllowanceAmount=totalSpecialAllowanceAmount+Math.round((specialAllowance*Double.parseDouble(expHours.toString()))*100.0)/100.0;
				}
				if(voluntaryPayAmount.containsKey(userId.toString())) {
					log.info(":::voluntaryPayAmount:::"+voluntaryPayAmount.get(userId.toString()).toString());
					log.info(":::padDays  * 8:::"+Double.parseDouble(payDaysData.get(userId.toString()).toString()) *8);
					log.info(":::hr:::"+Double.parseDouble(expHours.toString()));
					
					double voluntaryPay=(Double.parseDouble(voluntaryPayAmount.get(userId.toString()).toString())/(Double.parseDouble(payDaysData.get(userId.toString()).toString()) *8));
					totalVoluntaryPayAmount=totalVoluntaryPayAmount+Math.round((voluntaryPay*Double.parseDouble(expHours.toString()))*100.0)/100.0;
				}
				
				Long rank = Long.parseLong(userData.get("rank").toString());
				if (isGradeWise) {
					String grade = userData.get(ConstantUtility.GRADE).toString();
					if (gradeWiseCosts != null) {
					if (invoiceAmount==null || invoiceAmount == 0D ) {
							hourlyIndirectCost = gradeWiseCosts.get(grade) / ((Double.parseDouble(workingDays.toString())) * 8);
					}else
							hourlyIndirectCost = (gradeWiseCosts.get(grade) + buCost) / ((Double.parseDouble(workingDays.toString())) * 8);
					}
				}
				Map<String, Object> userSalary = getTeamData(userData, workingDays, yearMonth, hourlyIndirectCost,
						projectBusinessVertical,variablePays,incentives,voluntaryPayAmount,payDaysData);
				Boolean isMarginIncluded = Boolean.parseBoolean(userSalary.get("isMarginIncluded").toString()) ;
				Double userDirectCost = 0.0;
				Double indirectCost = 0.0;
				if (!userSalary.get(ConstantUtility.EMPLOYEE_SALARY).equals("N/A")) {
					userDirectCost = (Double) userSalary.get(ConstantUtility.EMPLOYEE_SALARY);
					indirectCost = (Double) userSalary.get(ConstantUtility.INDIRECT_COST);
				}
				if(isMarginIncluded) {
				projectDirectCost = projectDirectCost + userDirectCost;
				projectInDirectCost = projectInDirectCost + indirectCost;
				}
				if (!isAccountUser && rank < currentUserRank)
					managementData = managementSalary(userSalary, managementData, projectBusinessVertical);
				else
					teamdata.add(userSalary);
			}
			if (managementData.containsKey(ConstantUtility.USER_ID))
				teamdata.add(managementData);
		}
		marginData.put("checking", 0);
		marginData.put("teamData", teamdata);
		marginData.put("projectDirectCost", Math.round(projectDirectCost * 100.0) / 100.0);
		marginData.put("projectIndirectCost", Math.round(projectInDirectCost * 100.0) / 100.0);
		if(marginBasis!=null)
			marginData.put("marginBasis", marginBasis.getIsGradeWise());
		else
			marginData.put("marginBasis", false);
			 Double paymentChargesAmt = (Double) marginData.get("paymentChargesSum");
				
		marginData.put(ConstantUtility.PROJECT_COST,
				Math.round((projectDirectCost + projectInDirectCost) * 100.0) / 100.0);
		marginData.put("projectCostIncPaymentCharges",Math.round((projectDirectCost + projectInDirectCost+paymentChargesAmt) * 100.0) / 100.0);
		marginData.put("specialAllowanceAmount",Math.round(totalSpecialAllowanceAmount * 100.0) / 100.0);
		marginData.put("totalVoluntaryPayAmount", Math.round(totalVoluntaryPayAmount * 100.0) / 100.0);
		marginData = getMarginData(month, marginData, (projectDirectCost + projectInDirectCost), year, projectId);
		return marginData;
	}

	public Double getHourlyIndirectCost(Double invoiceAmount, String projectBusinessVertical, String accessToken, int year, int month) {
		Double hourlyIndirectCost = 0.0;
		if (invoiceAmount!=null && invoiceAmount != 0.0)
			hourlyIndirectCost = indirectCostService.getIndirectCostHourly(accessToken, String.valueOf(year),
					projectBusinessVertical, month);
		else
			hourlyIndirectCost = indirectCostService.getIndirectCostHourly(accessToken, String.valueOf(year), "",
					month);
		return hourlyIndirectCost;
	}

	@Cacheable("buMargins")
	public List<Object> getBuMargin(String businessVertical, int month, int year, int billingRateFilter, String accessToken) {
		List<Object> marginData = new ArrayList<>();
		
		List<Map<String, Object>> staffHours = (List<Map<String, Object>>)feignLegacyInterface.getBuWiseProjects(accessToken, businessVertical, month, year).get(ConstantUtility.DATA);
		Map<String, Double> gradeWiseCosts =null;
		Double buCost =0D;
		Double hourlyIndirectCost=0D;
		Double nonBillableIndirectCost=0D;
		Map<String, Object> workingDaysData = (Map<String, Object>) feignLegacyInterface.getpayrollWorkingDays(accessToken,month, new Integer(year)).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		
		boolean isGradeWise=false;
		MarginBasis marginBasis=marginBasisRepository.findByMonthAndYear(month, year);
		if(marginBasis!=null)
			isGradeWise = marginBasis.getIsGradeWise();
		if(isGradeWise) {
			Map<String, Object> userListAndCount = feignLegacyInterface.gradeWiseExpectedHours(month, year );
			gradeWiseCosts = utilService.getGradeWiseCosts(month, year, accessToken, "",userListAndCount);
			buCost = indirectCostService.buIndirectCost(accessToken, businessVertical, month, Integer.toString(year),gradeWiseCosts);
		}
		else {
			hourlyIndirectCost = indirectCostService.getIndirectCostHourly(accessToken, Integer.toString(year), businessVertical, month);
			nonBillableIndirectCost = indirectCostService.getIndirectCostHourly(accessToken, Integer.toString(year), "", month);
		}
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		
		List<ProjectInvoice> overallInvoices = projectInvoiceRepository.findAllByMonthAndYearAndIsDeleted(monthName, Integer.toString(year), false);
		List<Long> ids=overallInvoices.stream().distinct().mapToLong(ProjectInvoice::getProjectId).boxed().collect(Collectors.toList());
		
		List<ProjectInvoice> buInvoices=new ArrayList<>();
		Map<String,Map<String,Object>> projectDatMap=new HashMap<>();
		List<Map<String,Object>> projectDetailsList  = feignLegacyInterface.findProjectDescriptionList(accessToken , ids, month,year);
		for (ProjectInvoice invoice : overallInvoices) {
			Long projectId=invoice.getProjectId();
			Map<String, Object> projectDetails = null;
			if(projectDatMap.containsKey(projectId.toString()))
				projectDetails =projectDatMap.get(projectId.toString());
			else {
				projectDetails = projectDetailsList.stream().filter(pr->pr.get("projectId").toString().equals(invoice.getProjectId().toString())).findFirst().orElse(null);
				projectDatMap.put(invoice.getProjectId().toString(), projectDetails);
			}
			if(projectDetails!=null) {
				String projectBusinessVertical = (String) projectDetails.get(ConstantUtility.BUSINESS_VERTICAL);
				if (projectBusinessVertical.equals(businessVertical)) {
					buInvoices.add(invoice);
				}	
			}
					
		}
		List<Long> buInvoiceIds=buInvoices.stream().distinct().mapToLong(ProjectInvoice::getProjectId).boxed().collect(Collectors.toList());
		log.info("invoiceProjects..."+buInvoiceIds);
		
		List<ExcludePayrollDto> payrolls = new ArrayList<>();
		
		Map<String, Object> allCompensation = getAllCompensation(month, year);
		Map<String,Object> voluntaryPayData = (Map<String, Object>) allCompensation.get("voluntaryPayResponse");
		Map<String,Object> specialAllowanceData = (Map<String, Object>) allCompensation.get("specialAmountResponse");
		Map<String,Object> payDaysData =  (Map<String, Object>) allCompensation.get("PayDaysResponse");


		Double dollarexchangeCost=0D;
		
		List<Long> openProjects= new ArrayList<>();
		if (!staffHours.isEmpty() && (hourlyIndirectCost != 0.0 || gradeWiseCosts!=null)) {
			Map<String, Object> projects = loginUtilityService.objectToMapConverter((staffHours.get(0)));
			List<Object> projectList = loginUtilityService.objectToListConverter(projects.get("projects"));
			
			openProjects=projectList.stream().distinct().mapToLong(o->Long.parseLong(((Map<String,Object>)o).get(ConstantUtility.PROJECT_ID).toString())).boxed().collect(Collectors.toList());
			log.info("openProjects..."+openProjects);
			for (int i = 0; i < projectList.size(); i++) {
				Map<String, Object> projectData = loginUtilityService.objectToMapConverter(projectList.get(i));
				Map<String, Object> projectMap = projectMap(projectData, businessVertical,  month,  year,  accessToken,
						 hourlyIndirectCost,  nonBillableIndirectCost, gradeWiseCosts,  buCost, workingDays,marginBasis, 
						 payrolls, allCompensation,buInvoices);
				dollarexchangeCost = dollarCostService.getAverageDollarCost(month, year);
				if(!projectMap.isEmpty())
				marginData.add(projectMap);
			}
		}
		
		if(!openProjects.isEmpty()) {
			Set<Long> closedProjects= new HashSet<Long>();
			closedProjects.addAll(buInvoiceIds);
			if(closedProjects.removeAll(openProjects)) {
				List<Long> closedProjectList=new ArrayList<>();
				closedProjectList.addAll(closedProjects);
				marginData=nonOpenProjectsMap(projectDatMap, buInvoices, closedProjectList, dollarexchangeCost, marginData);
			}
				
		}

		return applyAverageBillingRateFilter(billingRateFilter, marginData);
	}
	
	
	public  Map<String,Object> projectMap(Map<String,Object> project,String businessVertical, int month, int year, String accessToken,
		Double hourlyIndirectCost, Double nonBillableIndirectCost, Map<String, Double> gradeWiseCosts, Double buCost,Object workingDays,
		MarginBasis marginBasis,List<ExcludePayrollDto> payrolls, Map<String, Object> allCompensation, List<ProjectInvoice> overallInvoices){
		
		Map<String, Object> projectMap = getProjectMap(project, businessVertical, month, year, accessToken, hourlyIndirectCost, nonBillableIndirectCost,
				gradeWiseCosts,buCost,workingDays,marginBasis,payrolls,allCompensation,overallInvoices);
		Double invoiceAmountInRupee = Double.parseDouble(projectMap.get("invoiceAmountInRupee").toString());
		Double expectedHours = Double.parseDouble(projectMap.get(ConstantUtility.EXPECTED_HOURS).toString());
		if(invoiceAmountInRupee !=0.00 || expectedHours != 0.00) {
			return projectMap;
		}
		return new HashMap<>();
	}
	
	public List<Object> nonOpenProjectsMap(Map<String,Map<String,Object>> projectDataMap,List<ProjectInvoice> invoices,List<Long> projectIds, Double dollarExchangeCost,List<Object> marginData){
		for (Long project : projectIds) {
			List<ProjectInvoice> closeInvoices=invoices.stream().filter(inv->inv.getProjectId().toString().equals(project.toString())).collect(Collectors.toList());
			Double paymentChargesSum=0D;
			Double invoiceAmount=0D;
			Double invoiceAmountInRupee=0D;
			for (ProjectInvoice invoice : closeInvoices) {
				if(invoice.getInvoiceStatus()!=6) {
					Double paymentChargesDollar=0D;
					Double paymentChargesRupee=0D;
					if(invoice.getPaymentCharges()!=null) {
						paymentChargesDollar=projectInvoiceService.getPaymentCharges("DOLLAR", invoice);
						paymentChargesRupee=projectInvoiceService.getPaymentCharges("RUPEE", invoice);
					}
					paymentChargesSum=paymentChargesSum+paymentChargesRupee;
					invoiceAmount = invoiceAmount + (invoice.getAmountInDollar());
					
					invoiceAmountInRupee = invoiceAmountInRupee + ((invoice.getAmountInDollar() * dollarExchangeCost));
				}
			}
			Map<String,Object> projectData = projectDataMap.get(project.toString());
			Long projectId = Long.parseLong(projectData.get(ConstantUtility.PROJECT_ID).toString());
			String projectName = (String) projectData.get(ConstantUtility.PROJECT_NAME);
			String projectCategory = (String) projectData.get(ConstantUtility.PROJECT_CATEGORY);
			Map<String, Object> projectMap = new HashMap<>();
			projectMap.put(ConstantUtility.PROJECT_ID, projectId);
			projectMap.put("billability", "Billable");
			projectMap.put(ConstantUtility.EXCLUDED_AMOUNT, 0D);
			projectMap.put(ConstantUtility.TOTAL_BU_COST, Math.round(0D * 100.00) / 100.00);
			projectMap.put(ConstantUtility.TOTAL_GRADE_COST, Math.round(0D*100)/100);
			projectMap.put(ConstantUtility.PROJECT_COST, 0D);
			projectMap.put(ConstantUtility.INDIRECT_COST, Math.round(0D * 100.00) / 100.00);
			projectMap.put("specialAllowanceAmount",Math.round(0D * 100.0) / 100.0);
			projectMap.put(ConstantUtility.DIRECT_COST, Math.round(0D * 100.0) / 100.0);
			projectMap.put(ConstantUtility.EXPECTED_HOURS, 0D);
			projectMap.put(ConstantUtility.TOTAL_GRADE_COST, 0D);
			projectMap.put(ConstantUtility.TOTAL_BU_COST, 0D);
			projectMap.put(ConstantUtility.PROJECT_NAME, projectName);
			projectMap.put(ConstantUtility.PROJECT_CATEGORY, projectCategory);
			projectMap.put("paymentCharges", Math.round(paymentChargesSum * 100.0) / 100.0);
			projectMap.put(ConstantUtility.PROJECT_MANAGER_ID, projectData.get(ConstantUtility.PROJECT_MANAGER_ID));
			projectMap.put(ConstantUtility.PROJECT_MANAGER, projectData.get(ConstantUtility.PROJECT_MANAGER));
			projectMap.put("resourcingHours", 0);
			projectMap.put("dollarAmount", dollarExchangeCost);
			projectMap.put(ConstantUtility.INVOICE_AMOUNT, Math.round(invoiceAmount * 100.0) / 100.0);
			projectMap = this.getAverageBillingRate(projectMap);
			projectMap.put("invoiceAmountInRupee", Math.round(invoiceAmountInRupee * 100.0) / 100.0);
			projectMap.put(ConstantUtility.PROJECT_COST, 0);
			Double margin = invoiceAmountInRupee;
			Double marginperc = 0.0;
			if (invoiceAmountInRupee != 0.0)
				marginperc = (margin * 100) / invoiceAmountInRupee;
			projectMap.put("marginInRupee", Math.round(margin * 100.0) / 100.0);
			projectMap.put("marginPercentage", Math.round(marginperc * 100.0) / 100.0);
			marginData.add(projectMap);
		}
		return marginData;
	}

	/**
	 * <p>Filters the ProjectMap According to Average Billing Filter.
	 * Possible averageBillingRateFilter values are 1 2 3 and default
	 * is 0. In case of 1 it returns the data containing averageBilling
	 * Rate value more than $10 per hour and for 2 it returns project
	 * having value between $8 to $10 per hour and for 3 it returns 
	 * project having value less than $8 per hour.</p>
	 * 
	 * @param billingRateFilter
	 * @param marginData
	 * @return the projectData which satisfy the given filiter value.
	 */
	@SuppressWarnings("unchecked")
	 List<Object> applyAverageBillingRateFilter(int billingRateFilter, List<Object> marginData) {
		List<Map<String, Object>> marginDataList = (List<Map<String, Object>>) (List<?>) marginData;
		if(billingRateFilter == 0) {
			return marginData;
		} else if(billingRateFilter == 1) {
			return marginDataList.parallelStream().filter(object -> {
				Double averageBillingRate = Double.parseDouble(object.get(ConstantUtility.AVERAGE_BILLING_RATE).toString());
				if(averageBillingRate > 10.0) {
					return true;
				}
				return false;
			}).collect(Collectors.toList());
		} else if (billingRateFilter == 2) {
			return marginDataList.parallelStream().filter(object -> {
				Double averageBillingRate = Double.parseDouble(object.get(ConstantUtility.AVERAGE_BILLING_RATE).toString());
				if(averageBillingRate >= 8.0 && averageBillingRate <=10) {
					return true;
				}
				return false;
			}).collect(Collectors.toList());
		} else if(billingRateFilter == 3) {
			return marginDataList.parallelStream().filter(object -> {
				Double averageBillingRate = Double.parseDouble(object.get(ConstantUtility.AVERAGE_BILLING_RATE).toString());
				if(averageBillingRate < 8.0 ) {
					return true;
				}
				return false;
			}).collect(Collectors.toList());
		}
		return null;
	}

	private Map<String, Object> getProjectMap(Map<String, Object> projectData,String businessVertical, int month, int year, String accessToken,
			Double hourlyIndirectCost, Double nonBillableIndirectCost, Map<String, Double> gradeWiseCosts, Double buCost,Object workingDays, 
			MarginBasis marginBasis,List<ExcludePayrollDto> payrolls, Map<String, Object> allCompensation, List<ProjectInvoice> overallInvoices) {
		Map<String,Object> voluntaryPayData = (Map<String, Object>) allCompensation.get("voluntaryPayResponse");
		Map<String,Object> specialAllowanceData = (Map<String, Object>) allCompensation.get("specialAmountResponse");
		Map<String,Object> payDaysData =  (Map<String, Object>) allCompensation.get("PayDaysResponse");
		Map<String, Object> projectMap = new HashMap<>();
		Long projectId = Long.parseLong(projectData.get(ConstantUtility.PROJECT_ID).toString());
		String projectName = (String) projectData.get(ConstantUtility.PROJECT_NAME);
		String projectCategory = (String) projectData.get(ConstantUtility.PROJECT_CATEGORY);
		Double indirectCost = 0.0;
		Double directCost = 0.0;
		Double specialAllowance = 0.0;
		Double voluntaryPayAmount = 0.0;
		Double projectCost = 0.0;
		Double invoiceAmount = 0.0;
		Double invoiceAmountInRupee = 0.0;
		Double dollarexchangeCost = 0.0;
		List<ProjectInvoice> projectInvoice = overallInvoices.stream().filter(invoice->invoice.getProjectId().toString().equals(projectId.toString())).collect(Collectors.toList());
		Double assetCost=0D;
		Double paymentChargesSum = 0.0;
		
		if (!projectInvoice.isEmpty()) {
			dollarexchangeCost = dollarCostService.getAverageDollarCost(month, year);
			projectMap = getProjectCost(projectData, month, year, accessToken, projectMap, hourlyIndirectCost,gradeWiseCosts,buCost,
					workingDays,marginBasis, payrolls,voluntaryPayData, specialAllowanceData,payDaysData);
			indirectCost = (Double) projectMap.get(ConstantUtility.INDIRECT_COST);
			directCost = (Double) projectMap.get(ConstantUtility.DIRECT_COST);
			specialAllowance=specialAllowance+(Double) projectMap.get("specialAllowanceAmount");
			projectCost = indirectCost + directCost ;
			for (ProjectInvoice invoice : projectInvoice) {
				if(invoice.getInvoiceStatus()!=6) {
					Double paymentChargesDollar=0D;
					Double paymentChargesRupee=0D;
					if(invoice.getPaymentCharges()!=null) {
						paymentChargesDollar=projectInvoiceService.getPaymentCharges("DOLLAR", invoice);
						paymentChargesRupee=projectInvoiceService.getPaymentCharges("RUPEE", invoice);
					}
					paymentChargesSum=paymentChargesSum+paymentChargesRupee;
					invoiceAmount = invoiceAmount + (invoice.getAmountInDollar());
					
					invoiceAmountInRupee = invoiceAmountInRupee + ((invoice.getAmountInDollar() * dollarexchangeCost));
				}
			}
			projectMap.put("billability", "Billable");
		} else {
			double costForBillable=0;
			projectMap = getProjectCost(projectData, month, year, accessToken, projectMap, nonBillableIndirectCost,gradeWiseCosts,costForBillable,
					workingDays,marginBasis, payrolls,voluntaryPayData, specialAllowanceData,payDaysData);
			indirectCost = (Double) projectMap.get(ConstantUtility.INDIRECT_COST);
			directCost = (Double) projectMap.get(ConstantUtility.DIRECT_COST);
			specialAllowance=specialAllowance+(Double) projectMap.get("specialAllowanceAmount");
			voluntaryPayAmount=voluntaryPayAmount + (Double)projectMap.get("voluntaryPayAmount");
			projectCost = indirectCost + directCost ;
			projectMap.put("billability", "Non-Billable");
		}
		
		
		projectMap.put(ConstantUtility.PROJECT_ID, projectId);
		projectMap.put(ConstantUtility.PROJECT_NAME, projectName);
		projectMap.put(ConstantUtility.PROJECT_CATEGORY, projectCategory);
		projectMap.put("paymentCharges", Math.round(paymentChargesSum * 100.0) / 100.0);
		projectMap.put(ConstantUtility.PROJECT_MANAGER_ID, projectData.get(ConstantUtility.PROJECT_MANAGER_ID));
		projectMap.put(ConstantUtility.PROJECT_MANAGER, projectData.get(ConstantUtility.PROJECT_MANAGER));
		projectMap.put("resourcingHours", new Double(projectData.get("resourcingHours").toString()));
		projectMap.put("dollarAmount", dollarexchangeCost);
		projectMap.put(ConstantUtility.INVOICE_AMOUNT, Math.round(invoiceAmount * 100.0) / 100.0);
		projectMap = this.getAverageBillingRate(projectMap);
		projectMap.put("invoiceAmountInRupee", Math.round(invoiceAmountInRupee * 100.0) / 100.0);
		projectMap.put(ConstantUtility.PROJECT_COST, Math.round((projectCost+paymentChargesSum+specialAllowance+voluntaryPayAmount) * 100.0) / 100.0);
		Double margin = invoiceAmountInRupee - (projectCost+paymentChargesSum+specialAllowance+voluntaryPayAmount);
		Double marginperc = 0.0;
		if (invoiceAmountInRupee != 0.0)
			marginperc = (margin * 100) / invoiceAmountInRupee;
		projectMap.put("marginInRupee", Math.round(margin * 100.0) / 100.0);
		projectMap.put("marginPercentage", Math.round(marginperc * 100.0) / 100.0);
		return projectMap;
	}

	private Map<String, Object> getAverageBillingRate(Map<String, Object> projectMap) {
		double resourcingHours =0;
		if(projectMap.containsKey("resourcingHours"))
			resourcingHours = Double.parseDouble(projectMap.get("resourcingHours").toString());
		double invoiceAmount = Double.parseDouble(projectMap.get(ConstantUtility.INVOICE_AMOUNT).toString());
		double averageBillingRate = 0.00;
		if(resourcingHours!= 0.00) {
			averageBillingRate = invoiceAmount / resourcingHours; 
		}
		projectMap.put(ConstantUtility.AVERAGE_BILLING_RATE, Math.round(averageBillingRate));
		return projectMap;
	}

	public Map<String, Object> getProjectCost(Map<String, Object> projectData, int month, int year, String accessToken,
			Map<String, Object> projectMap, Double hourlyIndirectCost, Map<String, Double> gradeWiseCosts, Double buCost,Object workingDays, MarginBasis marginBasis, List<ExcludePayrollDto> payrolls, Map<String, Object> voluntaryPayData, Map<String, Object> specialAllowanceData,Map<String,Object> payDaysData) {
		List<Object> teamData = loginUtilityService.objectToListConverter(projectData.get("teamData"));
		Double directCost = 0.0;
		Integer projectHours = 0;
		Integer projectMins = 0;
		Double indirectCost = 0.0;
		Double totalGradeCost=0.0;
		Double totalBuCost=0.0;
		boolean isGradeWise=false;
		Double totalSpecialAllowance=0D;
		Double totalVoluntaryPayAmount=0D;
		Double excludedDirectAmount=0.0D;
		Double excludedIndirectAmount=0.0D;
		if(marginBasis!=null)
			isGradeWise = marginBasis.getIsGradeWise();
		if (!teamData.isEmpty()) {
			for (int j = 0; j < teamData.size(); j++) {
				Map<String, Object> userData = loginUtilityService.objectToMapConverter(teamData.get(j));
				String grade=userData.get("grade").toString();
				Object userId = userData.get("userId");
				ExcludePayrollDto payroll =null;
						
				List<ExcludePayrollDto> filteredPayrolls = payrolls.stream().filter(p->userId.toString().equals(Long.toString(p.getUserId()) )).collect(Collectors.toList());
				if(!filteredPayrolls.isEmpty())
					payroll= filteredPayrolls.get(0);
				double userSalary = indirectCostService.getPay(userData, month, year, workingDays,voluntaryPayData,payDaysData);
				if(payroll==null || (payroll!=null && payroll.getIsMarginIncluded())) {
					directCost = directCost + userSalary;
				}
				if(payroll!=null &&  !payroll.getIsMarginIncluded()) {
					excludedDirectAmount = excludedDirectAmount+userSalary;
				}
				String expectedHours = userData.get(ConstantUtility.EXPECTED_HOURS).toString();
				
				String mins = expectedHours.split("\\.")[1];
				String hours = expectedHours.split("\\.")[0];
				projectHours = projectHours + Integer.parseInt(hours);
				projectMins = projectMins + Integer.parseInt(mins);
				if (projectMins >= 60) {
					projectHours = projectHours + 1;
					projectMins = projectMins - 60;
				}
				Double hourlyGradeCost=0D;
				Double hourlyBuCost=0D;
				if(isGradeWise && gradeWiseCosts!=null) {
					hourlyGradeCost= gradeWiseCosts.get(grade)/(Double.parseDouble(workingDays.toString())*8);
					hourlyBuCost = buCost/(Double.parseDouble(workingDays.toString())*8);
					hourlyIndirectCost=(gradeWiseCosts.get(grade)+buCost)/(Double.parseDouble(workingDays.toString())*8);
				}
				if (Double.parseDouble(mins) != 0D) {
					expectedHours = Double.toString(Double.parseDouble(hours) + (Double.parseDouble(mins) / 60));
				}
				if(payroll==null || (payroll!=null && payroll.getIsMarginIncluded())) {
					indirectCost = indirectCost + (hourlyIndirectCost * Double.parseDouble(expectedHours));
					totalBuCost = totalBuCost +  (hourlyBuCost * Double.parseDouble(expectedHours));
					totalGradeCost = totalGradeCost + (hourlyGradeCost * Double.parseDouble(expectedHours));
					
				}
				if(payroll!=null && !payroll.getIsMarginIncluded()) {
					excludedIndirectAmount = excludedIndirectAmount+(hourlyIndirectCost * Double.parseDouble(expectedHours));
				}
				
				if(specialAllowanceData.containsKey(userId.toString())) {
					double specialAllowance=(Double.parseDouble(specialAllowanceData.get(userId.toString()).toString())/(Double.parseDouble(payDaysData.get(userId.toString()).toString()) *8));
					totalSpecialAllowance=totalSpecialAllowance+Math.round((specialAllowance*Double.parseDouble(expectedHours.toString()))*100.0)/100.0;
					
				}
				
				if(voluntaryPayData.containsKey(userId.toString())) {
					double voluntaryPay=(Double.parseDouble(voluntaryPayData.get(userId.toString()).toString())/(Double.parseDouble(payDaysData.get(userId.toString()).toString()) *8));
					totalVoluntaryPayAmount=totalVoluntaryPayAmount+Math.round((voluntaryPay*Double.parseDouble(expectedHours.toString()))*100.0)/100.0;
				}
					
			}
		}
		Double projectCost = indirectCost + directCost;
		projectMap.put(ConstantUtility.EXCLUDED_AMOUNT, excludedIndirectAmount+excludedDirectAmount);
		projectMap.put(ConstantUtility.TOTAL_BU_COST, Math.round(totalBuCost * 100.00) / 100.00);
		projectMap.put(ConstantUtility.TOTAL_GRADE_COST, Math.round(totalGradeCost*100)/100);
		projectMap.put(ConstantUtility.PROJECT_COST, projectCost);
		projectMap.put(ConstantUtility.INDIRECT_COST, Math.round(indirectCost * 100.00) / 100.00);
		projectMap.put("specialAllowanceAmount",Math.round(totalSpecialAllowance * 100.0) / 100.0);
		projectMap.put("voluntaryPayAmount",Math.round(totalVoluntaryPayAmount * 100.0) / 100.0);
		projectMap.put(ConstantUtility.DIRECT_COST, Math.round(directCost * 100.0) / 100.0);
		projectMap.put(ConstantUtility.EXPECTED_HOURS, Double.parseDouble(projectHours + "." + projectMins));
		projectMap.put(ConstantUtility.TOTAL_GRADE_COST, totalGradeCost);
		projectMap.put(ConstantUtility.TOTAL_BU_COST, totalBuCost);
		return projectMap;
	}

	public Map<String, Object> userSalary(double hourlySalary, Map<String, Object> userData, Double indirectCost,
			String projectBusinessVertical, String profileStatus) {
		Object userId = userData.get(ConstantUtility.USER_ID);
		Object expectedHours = userData.get(ConstantUtility.EXPECTED_HOURS);
		Double expHours=Double.parseDouble(userData.get(ConstantUtility.EXPECTED_HOURS).toString());
		String mins=expHours.toString().split("\\.")[1];
		if(Double.parseDouble(mins)!=0D) {
			expHours = Double.parseDouble(userData.get(ConstantUtility.EXPECTED_HOURS).toString().split("\\.")[0]);
			expHours= expHours+(Double.parseDouble(mins)/60);
		}
		Object userName = userData.get("name");
		Map<String, Object> user = new HashMap<>();
		user.put(ConstantUtility.USER_ID, userId);
		user.put("name", userName);
		user.put(ConstantUtility.GRADE, userData.get(ConstantUtility.GRADE));
		user.put(ConstantUtility.PROFILE_STATUS, profileStatus);
		double employeeSalary = (hourlySalary * Double.parseDouble(expHours.toString()));
		user.put(ConstantUtility.EMPLOYEE_SALARY, Math.round(employeeSalary * 100.0) / 100.0);
		user.put(ConstantUtility.EXPECTED_HOURS, Double.parseDouble(expectedHours.toString()));
		user.put("perDaySalary", Math.round(indirectCost*100.00)/100.00);
		
		user.put("forecasted_hours",userData.get("forecasted_hours"));
		user.put("resourcing_hours", userData.get("resourcing_hours"));
		user.put("forecasted_accounting_hours", userData.get("forecasted_accounting_hours"));
		user.put("monthly_hours",userData.get("monthly_hours"));

		user.put(ConstantUtility.INDIRECT_COST,
				Math.round((indirectCost * Double.parseDouble(expHours.toString())) * 100.0) / 100.0);
		user.put(ConstantUtility.BUSINESS_VERTICAL, projectBusinessVertical);
		user.put("isMarginIncluded",true);
		return user;
	}

	public Map<String, Object> managementSalary(Map<String, Object> userData, Map<String, Object> newMap,
			String projectBusinessVertical) {
		Object expectedHours = userData.get(ConstantUtility.EXPECTED_HOURS);
		Object employeeSalary = userData.get(ConstantUtility.EMPLOYEE_SALARY);
		Object indirectCost = userData.get(ConstantUtility.INDIRECT_COST);
		newMap.put(ConstantUtility.USER_ID, 0);
		newMap.put("name", "Management");
		newMap.put(ConstantUtility.GRADE, "N/A");
		newMap.put(ConstantUtility.PROFILE_STATUS, "N/A");
		if (!newMap.containsKey(ConstantUtility.EMPLOYEE_SALARY))
			newMap.put(ConstantUtility.EMPLOYEE_SALARY, Double.parseDouble(employeeSalary.toString()));
		else {
			Object currentManagementSalary = newMap.get(ConstantUtility.EMPLOYEE_SALARY);
			Double salary = Double.parseDouble(currentManagementSalary.toString())
					+ Double.parseDouble(employeeSalary.toString());
			newMap.put(ConstantUtility.EMPLOYEE_SALARY, salary);
		}
		if (!newMap.containsKey(ConstantUtility.EXPECTED_HOURS))
			newMap.put(ConstantUtility.EXPECTED_HOURS, Double.parseDouble(expectedHours.toString()));
		else {
			Object currentExpectedHours = newMap.get(ConstantUtility.EXPECTED_HOURS);
			String newExpectedHours = getHours(expectedHours, currentExpectedHours);
			newMap.put(ConstantUtility.EXPECTED_HOURS, Double.parseDouble(newExpectedHours));
		}
		if (!newMap.containsKey(ConstantUtility.INDIRECT_COST))
			newMap.put(ConstantUtility.INDIRECT_COST, indirectCost);
		else {
			Object currentIndirectCost = newMap.get(ConstantUtility.INDIRECT_COST);
			newMap.put(ConstantUtility.INDIRECT_COST,
					Double.parseDouble(indirectCost.toString()) + Double.parseDouble(currentIndirectCost.toString()));
		}
		newMap.put(ConstantUtility.BUSINESS_VERTICAL, projectBusinessVertical);
		return newMap;
	}

	public String getHours(Object previousHours, Object currentHours) {
		Integer hours = Integer.parseInt(currentHours.toString().split("\\.")[0]);
		Integer mins = Integer.parseInt(currentHours.toString().split("\\.")[1]);
		Integer newHours = Integer.parseInt(previousHours.toString().split("\\.")[0]) + hours;
		Integer newMins = Integer.parseInt(previousHours.toString().split("\\.")[1]) + mins;
		return newHours + "." + newMins;
	}

	public Map<String, Object> getTeamData(Map<String, Object> userData, Object workingDays, YearMonth yearMonth,
			Double hourlyIndirectCost, String projectBusinessVertical, Map<String, Object> variablePays, Map<String, Object> incentives,
			Map<String, Object>voluntaryPayData,Map<String,Object> payDaysData) {
		LocalDate lastDay = yearMonth.atEndOfMonth();
		LocalDate firstDay = yearMonth.atDay(1);
		Map<String, Object> userSalary = new HashMap<>();
		Object userId = userData.get(ConstantUtility.USER_ID);
		Double variablePay=0D;
		Double incentive=0D;
		Double voluntaryPayAmount=0D;
		
		if(variablePays.containsKey(userId.toString()))
			variablePay=(Double.parseDouble(variablePays.get(userId.toString()).toString())/(Double.parseDouble(workingDays.toString()) *8)); 
		if(incentives.containsKey(userId.toString()))
			incentive=(Double.parseDouble(incentives.get(userId.toString()).toString())/(Double.parseDouble(workingDays.toString()) *8)); 
		/*
		 * voluntaryPay replace working day -> paid days 
		 */
		if(voluntaryPayData.containsKey(userId.toString()))
			voluntaryPayAmount=(Double.parseDouble(voluntaryPayData.get(userId.toString()).toString())/(Double.parseDouble(payDaysData.get(userId.toString()).toString()) *8)); 
		
			PayRegister currentPayregister = payRegisterRepository
				.findAllByUserIdAndIsCurrent(Long.parseLong(userId.toString()), true);
		Payroll payRoll = payrollRepository.findByMonthAndUserIdAndYear(yearMonth.getMonthValue(),
				Long.parseLong(userId.toString()), yearMonth.getYear());
		if (currentPayregister != null) {

				if (currentPayregister.getEffectiveDate().toLocalDate().isBefore(lastDay.plusDays(1))
						|| currentPayregister.getEffectiveDate().toLocalDate().isEqual(lastDay)) {
					double hourlyCompensation = voluntaryPayAmount;
					double hourlySalary = (indirectCostService.getHourlySalary(workingDays, currentPayregister))+variablePay+incentive;
					userSalary = userSalary(hourlySalary, userData, hourlyIndirectCost, projectBusinessVertical,
							currentPayregister.getStatus().toString());
				} else {
					List<PayRevisions> payrevisions = payRevisionRepository
							.findAllByUserIdAndIsDeleted(Long.parseLong(userId.toString()), false);
					List<PayRevisions> finalpayrevision = new ArrayList<>();
					if (!payrevisions.isEmpty()) {
						payrevisions.forEach(payrev -> {
							LocalDate effectiveFrom = payrev.getEffectiveFrom().toLocalDate();
							LocalDate effectiveTo = payrev.getEffectiveTo().toLocalDate();
							boolean isEffective = checkEffectiveDate(firstDay, lastDay, effectiveFrom, effectiveTo);
							if (isEffective) {
								finalpayrevision.add(payrev);
							}
						});
					}
					if (!finalpayrevision.isEmpty()) {
						if (finalpayrevision.size() > 1) {
							PayRevisions dataPayrev = finalpayrevision.get(finalpayrevision.size() - 1);
							PayRegister payRegister = payRegisterRepository
									.findAllById(dataPayrev.getPayRegister().getId());
							double hourlyCompensation =voluntaryPayAmount;
							double hourlySalary = (indirectCostService.getHourlySalary(workingDays, payRegister))+variablePay+incentive;
							userSalary = userSalary(hourlySalary, userData, hourlyIndirectCost, projectBusinessVertical,
									currentPayregister.getStatus().toString());
						} else {
							PayRevisions dataPayrev = finalpayrevision.get(0);
							PayRegister payRegister = payRegisterRepository
									.findAllById(dataPayrev.getPayRegister().getId());
							double hourlyCompensation =voluntaryPayAmount;
							double hourlySalary = (indirectCostService.getHourlySalary(workingDays, payRegister))+variablePay+incentive;
							userSalary = userSalary(hourlySalary, userData, hourlyIndirectCost, projectBusinessVertical,
									currentPayregister.getStatus().toString());
						}
					} else {
						double hourlySalary = 0.0+variablePay+incentive;
						double hourlyCompensation = voluntaryPayAmount;
						userSalary = userSalary(hourlySalary, userData, hourlyIndirectCost, projectBusinessVertical,
								currentPayregister.getStatus().toString());
					}
				}

		} else {
			Object userName = userData.get("name");
			Object expectedHours = userData.get(ConstantUtility.EXPECTED_HOURS);
			userSalary.put(ConstantUtility.USER_ID, userId);
			userSalary.put("name", userName);
			userSalary.put(ConstantUtility.GRADE, userData.get(ConstantUtility.GRADE));
			userSalary.put(ConstantUtility.EMPLOYEE_SALARY, "N/A");
			userSalary.put(ConstantUtility.EXPECTED_HOURS, Double.parseDouble(expectedHours.toString()));
			
			userSalary.put("forecasted_hours",userData.get("forecasted_hours"));
			userSalary.put("resourcing_hours", userData.get("resourcing_hours"));
			userSalary.put("forecasted_accounting_hours", userData.get("forecasted_accounting_hours"));
			userSalary.put("monthly_hours",userData.get("monthly_hours"));

			userSalary.put(ConstantUtility.INDIRECT_COST, "N/A");
			userSalary.put(ConstantUtility.PROFILE_STATUS, "INCOMPLETE");
			userSalary.put(ConstantUtility.BUSINESS_VERTICAL, projectBusinessVertical);
		}
		userSalary.put("isMarginIncluded", payRoll != null ? payRoll.getIsMarginIncluded() : true);
		return userSalary;
	}


	public boolean checkEffectiveDate(LocalDate firstDay, LocalDate lastDay, LocalDate effectiveFrom,
			LocalDate effectiveTo) {
		boolean isEffective = false;
		if (effectiveFrom.isBefore(firstDay) && effectiveTo.isBefore(firstDay) || effectiveFrom.isAfter(lastDay))
			isEffective = false;
		else
			isEffective = true;
		return isEffective;
	}

	public Map<String, Object> getBuIndirectCost(String businessVertical, int month, int year, String accessToken) {
		Map<String, Object> map = new HashMap<>();
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		Months monthObj = Months.valueOf(monthName.toUpperCase());
		Map<String, Object> data = feignLegacyInterface.getCompanyExpectedHours(accessToken,month, Integer.toString(year));
		
		Object companyExpectedHours = data.get(ConstantUtility.DATA);
		Map<String, Object> workingDaysData = (Map<String, Object>)feignLegacyInterface.getpayrollWorkingDays(accessToken,month, year).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		Double companyCount = 0.0;
		Double infraCost = 0.0;
		Double variableCost = 0.0;
		Double reimbursementCost = 0.0;
		Double staffCostPerSeat = 0.0;
		Double totalCost = 0.0;
		Double buCost = 0.0;
		IndirectCost indirectCost = costRepository.findByYearAndIsDeletedAndMonth(Integer.toString(year), false,
				monthObj);
		if (indirectCost != null) {
			infraCost = indirectCost.getInfraCost();
			variableCost = indirectCost.getVariableCost();
			reimbursementCost = indirectCost.getReimbursement();
			if (Double.parseDouble(companyExpectedHours.toString()) != 0.0) {
				companyCount = Double.parseDouble(companyExpectedHours.toString())
						/ (Double.parseDouble(workingDays.toString()) * 8);
				infraCost = (indirectCost.getInfraCost() / companyCount);
				variableCost = (indirectCost.getVariableCost() / companyCount);
				reimbursementCost = (indirectCost.getReimbursement() / companyCount);
			}
			Double staffCost = indirectCostService.getStaffCost(accessToken, month, indirectCost.getYear(), workingDays,
					"Operations Support");
			staffCostPerSeat = ((Math.round(staffCost * 100) / 100) / companyCount);
			Map<String, Object> userListAndCount = feignLegacyInterface.gradeWiseExpectedHours(month, Integer.parseInt(indirectCost.getYear()) );
			
			Map<String, Double> gradeWiseCosts=utilService.getGradeWiseCosts(month, year, accessToken, "", userListAndCount);
			buCost = indirectCostService.buIndirectCost(accessToken, businessVertical, month, Integer.toString(year),gradeWiseCosts);
			totalCost = staffCostPerSeat + (indirectCost.getInfraCost() / companyCount)
					+ (indirectCost.getVariableCost() / companyCount) + (indirectCost.getReimbursement() / companyCount) + buCost;
		}
		map.put("employees", Math.round(companyCount * 100.0) / 100.0);
		map.put("infraCost", Math.round(infraCost * 100.0) / 100.0);
		map.put("variableCost", Math.round(variableCost * 100.0) / 100.0);
		map.put("staffCost", Math.round(staffCostPerSeat) * 100 / 100);
		map.put("buCost", Math.round(buCost * 100.0) / 100.0);
		map.put("reimbursementCost", Math.round(reimbursementCost * 100.0) / 100.0);
		map.put("totalCost", Math.round(totalCost * 100.0) / 100.0);
		return map;
	}

	private Map<String, Object> getInvoiceData(int month, Map<String, Object> marginData, int year, Long projectId) {
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		List<ProjectInvoice> projectInvoice = projectInvoiceRepository
				.findAllByMonthAndYearAndIsDeletedAndProjectId(monthName, Integer.toString(year), false, projectId);
		Double invoiceAmount = 0D;
		Double invoiceAmountInRupee = 0.0;
		Double paymentChargesSum = 0.0;
		Double dollarexchangeCost = dollarCostService.getAverageDollarCost(month, year);
		Boolean hasInternalInvoices=false;
		if (!projectInvoice.isEmpty()) {
			for (ProjectInvoice invoice : projectInvoice) {
				if(!hasInternalInvoices && invoice.getIsInternal())
					hasInternalInvoices=true;
				Double paymentChargesDollar=0D;
				Double paymentChargesRupee=0D;
				if(invoice.getPaymentCharges()!=null && invoice.getInvoiceStatus()!=6) {
					paymentChargesDollar=projectInvoiceService.getPaymentCharges("DOLLAR", invoice);
					paymentChargesRupee=projectInvoiceService.getPaymentCharges("RUPEE", invoice);
				}
				paymentChargesSum=paymentChargesSum+paymentChargesRupee;
				if(invoice.getInvoiceStatus()!=6){
					invoiceAmount = invoiceAmount +invoice.getAmountInDollar();
					invoiceAmountInRupee = invoiceAmountInRupee +(invoice.getAmountInDollar() * dollarexchangeCost);
				}
			}
		}
		marginData.put("paymentChargesSum", paymentChargesSum);
		marginData.put("hasInternalInvoices", hasInternalInvoices);
		marginData.put(ConstantUtility.DOLLAR_EXCHANGE_VALUE, dollarexchangeCost);
		marginData.put(ConstantUtility.INVOICE_AMOUNT, Math.round(invoiceAmount * 100.0) / 100.0);
		marginData.put(ConstantUtility.INVOICE_AMOUNT_IN_RUPEE, Math.round(invoiceAmountInRupee * 100.0) / 100.0);
		return marginData;
	}

	private Map<String, Object> getMarginData(int month, Map<String, Object> marginData, Double projectCost, int year,
			Long projectId) {
		Double invoiceAmountInRupee = (Double) marginData.get(ConstantUtility.INVOICE_AMOUNT_IN_RUPEE);
		Double margin = 0.0;
		Double marginPercentage = 0.0;
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		Months monthEnum = Months.valueOf(monthName.toUpperCase());
		IndirectCost monthIndirectCost = costRepository.findByYearAndIsDeletedAndMonth(Integer.toString(year), false,
				monthEnum);
		if (monthIndirectCost != null) {
			margin = invoiceAmountInRupee - projectCost;
			if (invoiceAmountInRupee != 0)
				marginPercentage = ((Math.round(margin * 100.0) / 100.0) * 100) / invoiceAmountInRupee;
		}
		marginData.put("projectMargin", Math.round(margin * 100.0) / 100.0);
		marginData.put("marginPercentage", Math.round(marginPercentage * 100.0) / 100.0);
		return marginData;
	}
	
	@Cacheable("buTotalMargins")
	public Map<String, Object> getBuTotalMargin(int month, String year, String businessVertical, String accessToken) {
		Map<String, Object> buMargin = new HashMap<>();
		boolean isGradeWise=false;
		boolean isLTM=false;
		Double buCost=0.0;
		Double hourlyIndirectCost=0D;
		Double nonBillableIndirectCost=0D;
		Double invoiceTotalAmount = 0.0;
		Double invoiceTotalAmountInRupee = 0.0;
		Double paymentChargesAmt=0D;
		Double disputedAmount = 0.0;
		Double billableIC = 0.0;
		Double nonBillableIC = 0.0;
		Double billableDirectCost = 0.0;
		Double excludedAmount=0.0D;
		Double totalProjectCost = 0.0;
		Double totalHours=0D;
		Double specialAllowance=0D;
		Double voluntaryPayAmount=0D;
		Double internalInvoices =0D;
		Double overallInvoices = 0D;
		Double overallInvoicesInRupee = 0D;
		Double totalMargin = 0.0;
		Double totalMarginPerc = 0.0;
		Double buSpCost = 0D;
		Map<String, Double> gradeWiseCosts = null;
		List<Object> billableDirectProjects = new ArrayList<>();
		List<Object> billableIndirectProjects = new ArrayList<>();
		List<Object> nonbillableProjects = new ArrayList<>();
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		Map<String, Object> workingDaysData = (Map<String, Object>)feignLegacyInterface.getpayrollWorkingDays(accessToken,month, new Integer(year)).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		MarginBasis marginBasis=marginBasisRepository.findByMonthAndYear(month, Integer.parseInt(year));
		if(marginBasis!=null){
			isGradeWise = marginBasis.getIsGradeWise();
			isLTM = marginBasis.getIsLTM();
		}
		if(isGradeWise) {
			gradeWiseCosts = utilService.getGradeWiseCostsV2(month, Integer.parseInt(year), accessToken, "","");
			buCost = indirectCostService.buIndirectCostV3(accessToken, businessVertical, month, year, workingDays, gradeWiseCosts); //BU Development per seat Cost
		} else {
			hourlyIndirectCost = indirectCostService.getIndirectCostHourly(accessToken, year, businessVertical, month);
			nonBillableIndirectCost = indirectCostService.getIndirectCostHourly(accessToken, year, "", month);
		}
		List<Map<String, Object>> staffHours =(List<Map<String, Object>>) feignLegacyInterface.getBuWiseProjects(accessToken, businessVertical, month, Integer.parseInt(year)).get(ConstantUtility.DATA);
		Map<String, Object> projects = staffHours.get(0);
		List<Long> projectIds=new ArrayList<>();
		if(projects.containsKey("projects")) {
			List<Map<String,Object>> projectLists = (List<Map<String,Object>>) projects.get("projects");
			projectIds=projectLists.stream().map(pro->Long.parseLong( pro.get(ConstantUtility.PROJECT_ID).toString())).collect(Collectors.toList());
		}
		Double dollarexchangeCost = dollarCostService.getAverageDollarCost(month, Integer.parseInt(year));
		if (nonBillableIndirectCost != 0.0 || gradeWiseCosts!=null) {
			
			
			List<ProjectInvoice> monthlyInvoices = new ArrayList<>();
			if(businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT))
				monthlyInvoices = projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndIsInternal(monthName, year, false,false);
			else
				monthlyInvoices = projectInvoiceRepository.findAllByMonthAndYearAndIsDeleted(monthName, year, false);
			List<Long> ids =  monthlyInvoices.stream().map(e-> e.getProjectId()).collect(Collectors.toList());
			List<Map<String,Object>> projectDetailsList  = feignLegacyInterface.findProjectDescriptionList(accessToken , ids, month,Integer.parseInt(year));
			List<Long> invoiceIds = new ArrayList<>();
			if (!monthlyInvoices.isEmpty()) {
				for (ProjectInvoice invoice : monthlyInvoices) {
					Map<String, Object> projectDetails = projectDetailsList.stream().filter(pr->pr.get("projectId").toString().equals(invoice.getProjectId().toString())).findFirst().orElse(null);
					String projectBusinessVertical = (String) projectDetails.get(ConstantUtility.BUSINESS_VERTICAL);
					if (!businessVertical.equals("")) {
						if (projectIds.contains(invoice.getProjectId()) || projectBusinessVertical.equals(businessVertical)) {
							Double paymentChargesRupee=0D;
							if(invoice.getPaymentCharges()!=null && invoice.getInvoiceStatus()!=6) {
								paymentChargesRupee=projectInvoiceService.getPaymentCharges("RUPEE", invoice);
							}
							  paymentChargesAmt = paymentChargesAmt+paymentChargesRupee;
							if(invoice.getInvoiceStatus()!=6) {
								invoiceTotalAmount = invoiceTotalAmount +invoice.getAmountInDollar();
								invoiceTotalAmountInRupee = invoiceTotalAmountInRupee +(invoice.getAmountInDollar() * dollarexchangeCost);
								if (invoiceIds.isEmpty() || !invoiceIds.contains(Long.valueOf(invoice.getProjectId()))) {
									invoiceIds.add(Long.valueOf(invoice.getProjectId()));
								}
							}
							if (invoice.getInvoiceStatus() == 5L)
								disputedAmount = disputedAmount + invoice.getAmountInDollar();
						}
					}
				}
			}
			List<ExcludePayrollDto> payrolls = new ArrayList<>();
			
			Map<String, Object> allCompensation = getAllCompensation(month, Integer.parseInt(year));
			Map<String,Object> voluntaryPayData = (Map<String, Object>) allCompensation.get("voluntaryPayResponse");
			Map<String,Object> specialAllowanceData = (Map<String, Object>) allCompensation.get("specialAmountResponse");
			Map<String,Object> payDaysData =  (Map<String, Object>) allCompensation.get("PayDaysResponse");
			
			//** Calculation projects DC and IC
		
			if (!staffHours.isEmpty()) {
				List<Object> projectList =  (List<Object>) projects.get("projects");
				for (int i = 0; i < projectList.size(); i++) {
					Double projectCost = 0.0;
					Double directCost = 0.0;
					Double indirectCost = 0.0;
					Double specialAllowanceProj = 0.0;
					Double voluntaryPayAmountProj = 0.0;
					Map<String, Object> projectMap = new HashMap<>();
					Map<String, Object> projectData = loginUtilityService.objectToMapConverter(projectList.get(i));
					Long projectId = Long.parseLong(projectData.get(ConstantUtility.PROJECT_ID).toString());
					String projectName = (String) projectData.get(ConstantUtility.PROJECT_NAME);
					if (invoiceIds.contains(projectId)) {
						projectMap = getProjectCost(projectData, month, Integer.parseInt(year),accessToken, projectMap,
								hourlyIndirectCost,gradeWiseCosts,buCost,workingDays,marginBasis, payrolls, voluntaryPayData, specialAllowanceData,payDaysData);
						indirectCost = (Double) projectMap.get(ConstantUtility.INDIRECT_COST);
						billableIC = billableIC + indirectCost;
						Double totalBuCost = (Double) projectMap.get(ConstantUtility.TOTAL_BU_COST);
						Double totalGradeCost = Double.valueOf((projectMap.get(ConstantUtility.TOTAL_GRADE_COST).toString()));
						billableIndirectProjects = getProjectMap(indirectCost, billableIndirectProjects, projectName,totalBuCost,totalGradeCost);
						directCost = (Double) projectMap.get(ConstantUtility.DIRECT_COST);
						billableDirectCost = billableDirectCost + directCost;
						billableDirectProjects = getProjectsMap(directCost, billableDirectProjects, projectName);
					}else {
						if(!invoiceIds.isEmpty()) {
							double nonbillableIc=0;
							projectMap = getProjectCost(projectData, month, Integer.parseInt(year), accessToken, projectMap,
									nonBillableIndirectCost,null,nonbillableIc,workingDays,marginBasis, payrolls, voluntaryPayData, specialAllowanceData,payDaysData);
						}
						else {
							projectMap = getProjectCost(projectData, month, Integer.parseInt(year), accessToken, projectMap,
									0D,gradeWiseCosts,buCost,workingDays,marginBasis, payrolls, voluntaryPayData, specialAllowanceData,payDaysData);
						}
						
						indirectCost = (Double) projectMap.get(ConstantUtility.INDIRECT_COST);
						nonBillableIC = nonBillableIC + indirectCost;
						nonbillableProjects = getProjectsMap(indirectCost, nonbillableProjects, projectName);						
					}
					excludedAmount = excludedAmount+ Double.parseDouble(projectMap.get("excludedAmount").toString());
					projectCost = indirectCost + directCost;
					totalProjectCost = totalProjectCost + projectCost;
					totalHours = getExpectedHours(new Double(projectMap.get(ConstantUtility.EXPECTED_HOURS).toString()), totalHours);
					specialAllowanceProj = specialAllowanceProj + (Double)projectMap.get("specialAllowanceAmount");
					specialAllowance = specialAllowance + specialAllowanceProj;
					voluntaryPayAmountProj = voluntaryPayAmountProj + (Double)projectMap.get("voluntaryPayAmount");
					voluntaryPayAmount = voluntaryPayAmount + voluntaryPayAmountProj;
				}
			}
			Map<String,Object> budetails=utilService.getBusinessVerticalDetails(accessToken, businessVertical);
			buMargin = getInternalInvoices(buMargin, monthName, year, new Long(budetails.get("id").toString()),
					dollarexchangeCost);
			internalInvoices = new Double(buMargin.get("internalInvoices").toString());
			Double paidInternalInvoices = 0D;
			Double paidInternalInRupee = 0D;
			Double receivedInternalInvoices = 0D;
			Double receivedInternalInRupee = 0D;
			paidInternalInvoices = Double.parseDouble(buMargin.get("paidInternalInvoices").toString());
			paidInternalInRupee = Double.parseDouble(buMargin.get("paidInternalInRupee").toString());
			receivedInternalInvoices = Double.parseDouble(buMargin.get("receivedinternalInvoices").toString());
			receivedInternalInRupee = Double.parseDouble(buMargin.get("receivedinternalInRupee").toString());

			if (!businessVertical.equals("Operations Support")) {
				
				overallInvoices = invoiceTotalAmount;
				overallInvoicesInRupee = invoiceTotalAmountInRupee ;
				invoiceTotalAmount = invoiceTotalAmount - receivedInternalInvoices;
				invoiceTotalAmountInRupee = invoiceTotalAmountInRupee - receivedInternalInRupee;
			}else{
				overallInvoices = invoiceTotalAmount + receivedInternalInvoices;	
				overallInvoicesInRupee = invoiceTotalAmountInRupee + (receivedInternalInvoices * dollarexchangeCost);
			}
			List<BuSpecificCost> buSpecificCosts = buSpecificCostRepository.findAllByYearAndMonthAndBusinessVerticalAndDeleted(Integer.parseInt(year), month, businessVertical, false);
			if(buSpecificCosts != null){
				buSpCost = buSpecificCosts.stream().collect(Collectors.summingDouble(BuSpecificCost::getAmount));
			}
			totalProjectCost = totalProjectCost + paidInternalInRupee + buSpCost;
		}
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
			buMargin.put(ConstantUtility.INVOICE_AMOUNT, Math.round(invoiceTotalAmount * 100.0) / 100.0);
			buMargin.put("invoiceAmountInRupee", Math.round(invoiceTotalAmountInRupee * 100.0) / 100.0);
			buMargin.put("totalProjectCost", totalProjectCost+paymentChargesAmt+specialAllowance+voluntaryPayAmount);
			buMargin.put("totalProjectCostExcPaymentCharges", totalProjectCost);
			buMargin.put("paymentChargesAmt", paymentChargesAmt);

			Double buSize=0D;
			if(new Double(workingDays.toString())!=0 && totalHours!=0)
				buSize=(totalHours/(new Double(workingDays.toString())*8));
			buMargin.put("buAvergeGic",0);
			if(buSize!=0)
				buMargin.put("buAvergeGic", Math.round(((billableIC+nonBillableIC)/buSize)*100.00)/100.00);
			buMargin.put("buSize", Math.round(buSize*100.00)/100.00);
			buMargin.put("totalIndirectCost", Math.round((billableIC+nonBillableIC)*100.00)/100.00);
			buMargin.put("billableIC", billableIC);
			buMargin.put("billableICProjects", billableIndirectProjects);
			buMargin.put("billableDirectCost", billableDirectCost);
			buMargin.put("billableDirectProjects", billableDirectProjects);
			buMargin.put("nonBillableIC", nonBillableIC);
			buMargin.put("nonBillableICProjects", nonbillableProjects);
			buMargin.put("excludedAmount", excludedAmount);
			buMargin.put("specialAllowanceAmount",specialAllowance);
			buMargin.put("voluntaryPayAmount", voluntaryPayAmount);
			buMargin.put("disputedAmount", disputedAmount);
			buMargin.put("disputedPerc", Math.round(((disputedAmount * 100.0) / invoiceTotalAmount) * 100.0) / 100.0);
			totalMargin = (overallInvoicesInRupee - (totalProjectCost+paymentChargesAmt+specialAllowance+voluntaryPayAmount));
			buMargin.put("totalMargin", Math.round(totalMargin * 100.0) / 100.0);
			if (overallInvoicesInRupee != 0.0)
				totalMarginPerc = (totalMargin * 100) / overallInvoicesInRupee;
			buMargin.put("totalMarginPerc", Math.round(totalMarginPerc * 100.0) / 100.0);
			buMargin.put("isLTM", isLTM);
			buMargin.put("BuSpecificCost", buSpCost);
			return buMargin;
	}
	
	
	public Map<String,Object> getInternalInvoices(Map<String,Object> buMargin,String month,String year,Long buId, Double dollarexchangeCost){
		List<ProjectInvoice> projectInvoices = projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndIsInternal(month, year, false, true );
		List<ProjectInvoice> receivedInternalInvoices = projectInvoices.stream().filter(inv->inv.getRaisedToBu().equals(buId)).collect(Collectors.toList());
		Double receivedInvoices = receivedInternalInvoices.parallelStream().collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar));
		buMargin.put("receivedinternalInvoices", receivedInvoices);
		buMargin.put("receivedinternalInRupee", receivedInvoices * dollarexchangeCost);
		List<ProjectInvoice> paidInternalInvoices=projectInvoices.stream().filter(inv->inv.getRaisedFromBu().equals(buId)).collect(Collectors.toList());
		Double paidInvoices = paidInternalInvoices.parallelStream().collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar));
		buMargin.put("paidInternalInvoices", paidInvoices);
		buMargin.put("paidInternalInRupee", paidInvoices * dollarexchangeCost);
		buMargin.put("internalInvoices", receivedInvoices-paidInvoices);
		buMargin.put("internalInvoicesInRupee", (receivedInvoices-paidInvoices) * dollarexchangeCost);
		return buMargin;
	}
	
	public Map<String,Object> getInternalInvoicesV1(Map<String,Object> buMargin,String month,String year,Long buId, Double dollarexchangeCost){
		List<ProjectInvoice> receivedInternalInvoices=projectInvoiceRepository.findAllByRaisedToBuAndMonthAndYearAndIsDeletedAndIsInternal(buId,month,year,false, true);
		Double receivedInvoices = receivedInternalInvoices.parallelStream().collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar));
		buMargin.put("receivedinternalInvoices", receivedInvoices);
		buMargin.put("receivedinternalInRupee", receivedInvoices * dollarexchangeCost);
		List<ProjectInvoice> paidInternalInvoices=projectInvoiceRepository.findAllByRaisedFromBuAndMonthAndYearAndIsDeletedAndIsInternal(buId,month,year,false, true);
		Double paidInvoices = paidInternalInvoices.parallelStream().collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar));
		buMargin.put("paidInternalInvoices", paidInvoices);
		buMargin.put("paidInternalInRupee", paidInvoices * dollarexchangeCost);
		buMargin.put("internalInvoices", receivedInvoices-paidInvoices);
		buMargin.put("internalInvoicesInRupee", (receivedInvoices-paidInvoices) * dollarexchangeCost);
		return buMargin;
	}

	List<Object> getProjectsMap(Double cost, List<Object> projectsList, String projectName) {
		Map<String, Object> map = new HashMap<>();
		map.put(ConstantUtility.PROJECT_NAME, projectName);
		map.put("cost", cost);
		projectsList.add(map);
		return projectsList;
	}
	
	Double getExpectedHours(Double expectedHours, Double totalHours) {
		int projectHours = new Integer(totalHours.toString().split("\\.")[0]);
		int projectMins = new Integer(totalHours.toString().split("\\.")[1]);
		String mins = expectedHours.toString().split("\\.")[1];
		String hours = expectedHours.toString().split("\\.")[0];
		projectHours = projectHours + Integer.parseInt(hours);
		projectMins = projectMins + Integer.parseInt(mins);
		if (projectMins >= 60) {
			projectHours = projectHours + 1;
			projectMins = projectMins - 60;
		}
		totalHours=new Double(projectHours+"."+projectMins);
		return totalHours;
	}

	private Object getWorkingDays(String accessToken, int month, int year) {
		Map<String, Object> workingDaysData =(Map<String, Object>) feignLegacyInterface.getpayrollWorkingDays(accessToken,month, year).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		return workingDays;
	}

	//@Scheduled(cron = "0 0 6 * * *")
	@CacheEvict(cacheNames = "buMargins", allEntries = true)
	public void flushTotalBuMargins() {
	}

	//@Scheduled(cron = "0 0 5 * * *")
	@CacheEvict(value= {"buTotalMargins","gradeBasedBuDashboard"}, allEntries = true)
	public void flushBuMargins() {
	}

	@Cacheable("lifetimeInvoices")
	public Map<String, Object> getLifeTimeInvoices(int year, long projectId) {
		List<ProjectInvoice> invoices = projectInvoiceRepository.findByProjectIdAndYearAndIsDeletedAndIsInternal(projectId,
				Integer.toString(year), false, false);
		int month = 12;
		if (year == LocalDateTime.now().getYear())
			month = LocalDateTime.now().getMonthValue();
		Map<String, Object> map = new HashMap<>();
		for (int i = 0; i < month - 1; i++) {
			String monthName = new DateFormatSymbols().getMonths()[i].toString();
			Double dollarexchangeCost = dollarCostService.getAverageDollarCost(i + 1, year);
			Double invoiceAmount = 0.0;
			for (ProjectInvoice inv : invoices) {
				if (inv.getMonth().equals(monthName)) {
					if(inv.getInvoiceStatus()!=6) {
						double dollarAmount = inv.getAmountInDollar();
						invoiceAmount = invoiceAmount + (dollarexchangeCost * dollarAmount);
					}
				}
			}
			map.put(Integer.toString(i + 1), invoiceAmount);
		}
		return map;
	}

	@Cacheable("lifeTimeResources")
	public Map<String, Object> getLifeTimeRersources(int year, long projectId,String accessToken) {
			
		Map<String, Object> resources = (Map<String, Object>) feignLegacyInterface.getMonthlyResources(accessToken,year, projectId).get("data");
		return resources;
	}

	@Cacheable("lifeTimeLeaves")
	public Map<String, Object> getResourcesAttendance(int year, Map<String, Object> resources, long projectId, String accessToken) {
		
		int month = 12;
		if (year == LocalDateTime.now().getYear())
			month = LocalDateTime.now().getMonthValue();
		Map<String, Object> map = new HashMap<>();
		for (int i = 0; i < month - 1; i++) {
			List<Object> data = (List<Object>) resources.get(Integer.toString(i + 1));
			List<Object> userAtt = new ArrayList<>();
			for (Object user : data) {

				Map<String, Object> attendance = feignLegacyInterface.getMonthlyLeaveData(accessToken,year,
						Long.parseLong(user.toString()), i + 1, projectId);
				Map<String, Object> monthwiseLeaves = new HashMap<>();
				monthwiseLeaves.put(user.toString(), attendance.get("data"));
				userAtt.add(monthwiseLeaves);
			}

			map.put(Integer.toString(i + 1), userAtt);
		}
		return map;
	}

	@Cacheable("lifeTimeExpectedHours")
	public Map<String, Object> getLifeTimeExpectedHours(int year, Map<String, Object> resources, long projectId, String accessToken) {

		int month = 12;
		if (year == LocalDateTime.now().getYear())
			month = LocalDateTime.now().getMonthValue();
		Map<String, Object> map = new HashMap<>();
		for (int i = 0; i < month - 1; i++) {
			List<Object> data = (List<Object>) resources.get(Integer.toString(i + 1));
			
			
			List<Map<String,Object>> expectedHours = (List<Map<String, Object>>) feignLegacyInterface.getTeamExpectedHours(accessToken,year, data, i + 1, projectId).get("data");

			int listSize = expectedHours.size();
			List<Object> expectedHoursData = new ArrayList<>();
			for (int j = 0; j < listSize; j++) {
				Map<String, Object> d = (Map<String, Object>) expectedHours.get(j);
				expectedHoursData.add(d);
			}
			map.put(Integer.toString(i + 1), expectedHoursData);
		}
		return map;
	}

	@Cacheable("lifeTimeIndirectCost")
	public Map<String, Object> getLifeTimeIndirectCost(String accessToken, int year, long projectId,
			Map<String, Object> invoices) {
		int month = 12;
		if (year == LocalDateTime.now().getYear())
			month = LocalDateTime.now().getMonthValue();
		Map<String, Object> projectDetails = (Map<String, Object>) feignLegacyInterface.getProjectDescription(projectId,"",null,null).get("data");

		String projectBusinessVertical = (String) projectDetails.get(ConstantUtility.BUSINESS_VERTICAL);
		Map<String, Object> map = new HashMap<>();
		for (int i = 0; i < month - 1; i++) {
			Double invoiceAmount = (Double) invoices.get(Integer.toString(i + 1));
			Double hourlyIndirectCost = getHourlyIndirectCost(invoiceAmount, projectBusinessVertical, accessToken, year,
					i + 1);
			map.put(Integer.toString(i + 1), hourlyIndirectCost);
		}
		return map;
	}

	public Map<String, Object> getLifeTimeMargin(String accessToken, int year, long projectId,
			Map<String, Object> invoices, Map<String, Object> expectedHours, Map<String, Object> indirectCost,
			Map<String, Object> currentMonthMarginData) {
		int month = 12;
		if (year == LocalDateTime.now().getYear())
			month = LocalDateTime.now().getMonthValue();
		Double overallInvoice = 0.0;
		Double overallProjectCost = 0.0;
		Double overallMargin = 0.0;
		int initializingMonth = 0;
		Map<String, Object> projectDetails = (Map<String, Object>) feignLegacyInterface.getProjectDescription(projectId,"",null,null).get("data");

		String projectBusinessVertical = (String) projectDetails.get(ConstantUtility.BUSINESS_VERTICAL);
		if (year == 2020)
			initializingMonth = 4; // Since we have started calculating margins from May 2020
		List<Object> listData = new ArrayList<>();
		for (int i = initializingMonth; i < month - 1; i++) {
			
			Double monthlyMargin = 0.0;
			Double monthlyMarginPerc = 0.0;
			Double monthlyIndirectCost = 0.0;
			Double monthlyDirectCost = 0.0;
			Double invoiceAmount = (Double) invoices.get(Integer.toString(i + 1));
			String monthName = new DateFormatSymbols().getMonths()[i].toString();
			Months monthEnum = Months.valueOf(monthName.toUpperCase());
			IndirectCost monthIndirectCost = costRepository.findByYearAndIsDeletedAndMonth(Integer.toString(year),
					false, monthEnum);
			Map<String,Double> gradeWiseCosts=null;
			Double buCost=0.0;
			boolean isGradeWise=false;
			MarginBasis marginBasis=marginBasisRepository.findByMonthAndYear(i+1, year);
			if(marginBasis!=null)
				isGradeWise = marginBasis.getIsGradeWise();
			if(isGradeWise) {
				Map<String, Object> userListAndCount = feignLegacyInterface.gradeWiseExpectedHours(i+1, year );
				gradeWiseCosts=utilService.getGradeWiseCosts(i+1, year, accessToken, "", userListAndCount);
				buCost=indirectCostService.buIndirectCost(accessToken, projectBusinessVertical, i+1, Integer.toString(year),gradeWiseCosts);
			}
			if (monthIndirectCost != null) {
				Object workingDays = getWorkingDays(accessToken, (i + 1), year);
				Double hourlyIndirectCost = 0D;
				if(!isGradeWise)
					hourlyIndirectCost = (Double) indirectCost.get(Integer.toString(i + 1));
				List<Object> resourceData = (List<Object>) expectedHours.get(Integer.toString(i + 1));
				int listSize = resourceData.size();
				for (int j = 0; j < listSize; j++) {
					Map<String, Object> data = (Map<String, Object>) resourceData.get(j);
					Double hourlyDirectCost = consolidatedService.getPay(Long.parseLong(data.get("userId").toString()),
							month, year, workingDays);
					monthlyDirectCost = monthlyDirectCost
							+ (hourlyDirectCost * Double.parseDouble(data.get("expectedHours").toString()));
					if(isGradeWise) {
						String grade = data.get(ConstantUtility.GRADE).toString();
						Double gradeCost=0D;
						if(gradeWiseCosts.get(grade)!=null)
							gradeCost=gradeWiseCosts.get(grade);
						if(invoiceAmount==0.0)
							hourlyIndirectCost = gradeCost/((Double.parseDouble(workingDays.toString())) * 8);
						else
							hourlyIndirectCost = (gradeCost+buCost)/((Double.parseDouble(workingDays.toString())) * 8);
					}
					monthlyIndirectCost = monthlyIndirectCost
							+ (hourlyIndirectCost * Double.parseDouble(data.get("expectedHours").toString()));
				}
				overallInvoice = overallInvoice + invoiceAmount;
				overallProjectCost = overallProjectCost + (monthlyIndirectCost + monthlyDirectCost);
				monthlyMargin = invoiceAmount - (monthlyIndirectCost + monthlyDirectCost);
				monthlyMarginPerc = (monthlyMargin * 100) / invoiceAmount;
				overallMargin = overallMargin + monthlyMargin;
				Map<String, Object> monthMap = new HashMap<>();
				monthMap.put("month", monthName);
				monthMap.put("margin", monthlyMargin);
				monthMap.put("marginPerc", monthlyMarginPerc);
				listData.add(monthMap);
			}
		}
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		Months monthEnum = Months.valueOf(monthName.toUpperCase());
		IndirectCost monthIndirectCost = costRepository.findByYearAndIsDeletedAndMonth(Integer.toString(year),
				false, monthEnum);
		Double overallMarginPerc = 0.0;
		if(monthIndirectCost!=null) {
		Double currentMonthInvoice = (Double) currentMonthMarginData.get(ConstantUtility.INVOICE_AMOUNT_IN_RUPEE);
		Double currentMonthMargin = (Double) currentMonthMarginData.get("projectMargin");
		Double currentMonthMarginPerc = (Double) currentMonthMarginData.get("marginPercentage");
		overallInvoice = overallInvoice + currentMonthInvoice;
		overallMargin = overallMargin + currentMonthMargin;
		
		Map<String, Object> monthMap = new HashMap<>();
		monthMap.put("month", monthName);
		monthMap.put("margin", currentMonthMargin);
		monthMap.put("marginPerc", currentMonthMarginPerc);
		listData.add(monthMap);
		}
		if (overallInvoice != 0.0 && overallMargin > 0.0) {
			overallMarginPerc = (overallMargin * 100) / overallInvoice;
		}
		Map<String, Object> marginMap = new HashMap<>();
		marginMap.put("overallMargin", overallMargin);
		marginMap.put("overallMarginPer", overallMarginPerc);
		marginMap.put("marginList", listData);
		return marginMap;
	}

	public void flushcaches() {
		flushLifeTimeInvoices();
		flushLifeTimeExpectedHours();
		flushLifeTimeResources();
	}

	@CacheEvict(cacheNames = "lifetimeInvoices", allEntries = true)
	public void flushLifeTimeInvoices() {
	}

	@CacheEvict(cacheNames = "lifeTimeExpectedHours", allEntries = true)
	public void flushLifeTimeExpectedHours() {
	}

	//@Scheduled(cron = "0 0 6 * * *")
	@CacheEvict(cacheNames = "lifeTimeIndirectCost", allEntries = true)
	public void flushLifeTimeIndirectCost() {
	}

	@CacheEvict(cacheNames = "lifeTimeResources", allEntries = true)
	public void flushLifeTimeResources() {
	}

	@CacheEvict(cacheNames = "currentMonthMargin", allEntries = true)
	public void flushcurrentMonthMargin() {
	}
	
	//@Scheduled(cron = "0 0 22 * * *",zone="IST")
	public void saveProjectSnapshotAuto(String accessToken) {
		try {
		List<Object> buList = new ArrayList<>();
		buList.add("Digital Marketing");
		buList.add("Blockchain");
		buList.add("Artificial Intelligence");
		buList.add("ERP Solution");
		buList.add("Oodles Technologies");
		buList.add("Oodles Studio");
		buList.add("Operations Support");
		buList.add("Unassigned");
		buList.add("DPP");
		
				int buListSize = buList.size();
		for (int i = 0; i < buListSize; i++) {
			List<Map<String, Object>> projects =(List<Map<String, Object>>) feignLegacyInterface.getBuProjectsForsnapshots(accessToken,buList.get(i).toString()).get("data");
			int projectListSize = projects.size();
			for (int j = 0; j < projectListSize; j++) {
				Map<String, Object> projectMap = (Map<String, Object>) projects.get(j);
				Object proId = projectMap.get("id");
				saveProjectSnapshot(accessToken, Long.parseLong(proId.toString()), LocalDateTime.now().minusMonths(1).getMonthValue(), LocalDateTime.now().minusMonths(1).getYear());
			}
		}
	}catch(Exception e) {
		e.printStackTrace();
		log.debug("saveProjectSnapshotAuto Exception : "+e.toString());	
	}
	}

	public ProjectExpectedHours saveProjectSnapshot(String accessToken, Long projectId, int month , int year) {
		Map<String, Object> marginMap = null;
		ProjectExpectedHours projectExpectedHours = null;
		if(month==0 && year==0) {
			marginMap = getDirectCost(projectId, LocalDateTime.now().minusMonths(1).getMonthValue(),
				LocalDateTime.now().minusMonths(1).getYear(), accessToken, true);
			projectExpectedHours = expectedHoursRepository.findByProjectIdAndMonthAndYear(projectId,
				LocalDateTime.now().minusMonths(1).getMonthValue(), LocalDateTime.now().minusMonths(1).getYear());
		}
		else {
			marginMap = getDirectCost(projectId,month,
					year, accessToken, true);
				projectExpectedHours = expectedHoursRepository.findByProjectIdAndMonthAndYear(projectId,
					month, year);
		}
		if (projectExpectedHours == null) {
			projectExpectedHours = new ProjectExpectedHours();
			projectExpectedHours.setProjectId(projectId);
			projectExpectedHours.setArchived(false);
			projectExpectedHours.setMonth(LocalDateTime.now().minusMonths(1).getMonthValue());
			projectExpectedHours.setYear(LocalDateTime.now().minusMonths(1).getYear());
		}
		setProjectSnapshots(projectId, marginMap);
		projectExpectedHours.setProjectCost(new Double(marginMap.get("projectDirectCost").toString())
				+ new Double(marginMap.get("projectIndirectCost").toString()));
		projectExpectedHours.setDirectCost(new Double(marginMap.get("projectDirectCost").toString()));
		projectExpectedHours.setIndirectCost(new Double(marginMap.get("projectIndirectCost").toString()));
		projectExpectedHours.setInvoiceAmountInRupees(
				new Double(marginMap.get(ConstantUtility.INVOICE_AMOUNT_IN_RUPEE).toString()));
		projectExpectedHours.setInvoiceAmount(new Double(marginMap.get(ConstantUtility.INVOICE_AMOUNT).toString()));
		projectExpectedHours.setMargin(new Double(marginMap.get("projectMargin").toString()));
		projectExpectedHours.setMarginPerc(new Double(marginMap.get("marginPercentage").toString()));
		projectExpectedHours = expectedHoursRepository.saveAndFlush(projectExpectedHours);
		saveTeamExpectedHours(marginMap,projectId,LocalDateTime.now().minusMonths(1).getMonthValue(),LocalDateTime.now().minusMonths(1).getYear());
		return projectExpectedHours;
	}
	
	public List<TeamExpectedhours> saveTeamExpectedHours(Map<String, Object> marginMap,Long projectId,int month,int year) {
		List<Object> team=(List<Object>) marginMap.get("teamData");
		int teamSize=team.size();
		List<TeamExpectedhours> teamData=new ArrayList<>();
		List<TeamExpectedhours> teamhoursList =userExpectedHoursRepo.findAllByProjectIdAndMonthAndYear(projectId,month,year);
		
		for(int i=0;i<teamSize;i++) {
			Map<String,Object> userData=(Map<String, Object>) team.get(i);
			Long userId=new Long( userData.get("userId").toString());
			TeamExpectedhours teamhours = null;
			for(TeamExpectedhours teamhrs : teamhoursList){
				if(teamhrs.getUserId().equals(userId))
					teamhours = teamhrs;

			}
			
			if(teamhours==null) {
				teamhours=new TeamExpectedhours();
				teamhours.setExpectedHours(Double.parseDouble(userData.get("expectedHours").toString()));
				teamhours.setHourlySalary(Double.parseDouble(userData.get("perDaySalary").toString()));
				teamhours.setMonth(month);
				teamhours.setYear(year);
				teamhours.setProjectId(projectId);
				teamhours.setUserId(userId);
				teamhours.setDirectCost(Double.parseDouble(userData.get("employeeSalary").toString()));
				teamhours.setIndirectCost(Double.parseDouble(userData.get("indirectCost").toString()));
				teamhours=userExpectedHoursRepo.saveAndFlush(teamhours);
				setUserSnapshots(projectId, userId, userData);
			}
		}
		return teamData;
	}

	public ProjectSnapshots setProjectSnapshots(Long projectId, Map<String, Object> marginMap) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date today = calendar.getTime();
		ProjectSnapshots preSnap = null;
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		Date yesterday = calendar.getTime();
		List<ProjectSnapshots> preSnaps = snapshotRepository.findAllByProjectIdAndMonthAndYearAndCreationDate(projectId,
				LocalDateTime.now().minusMonths(1).getMonthValue(), LocalDateTime.now().minusMonths(1).getYear(),
				yesterday);
		if (!preSnaps.isEmpty())
			preSnap = preSnaps.get(preSnaps.size() - 1);
		ProjectSnapshots newSnap = new ProjectSnapshots();
		if (preSnap == null || !preSnap.getMargin().toString().equals(marginMap.get("projectMargin").toString()))
			newSnap.setChanged(true);
		else
			newSnap.setChanged(false);
		newSnap.setArchived(false);
		newSnap.setMonth(LocalDateTime.now().minusMonths(1).getMonthValue());
		newSnap.setYear(LocalDateTime.now().minusMonths(1).getYear());
		newSnap.setCreationDate(today);
		newSnap.setProjectId(projectId);
		newSnap.setProjectCost(new Double(marginMap.get("projectDirectCost").toString())
				+ new Double(marginMap.get("projectIndirectCost").toString()));
		newSnap.setDirectCost(new Double(marginMap.get("projectDirectCost").toString()));
		newSnap.setIndirectCost(new Double(marginMap.get("projectIndirectCost").toString()));
		newSnap.setInvoiceAmountInRupees(new Double(marginMap.get(ConstantUtility.INVOICE_AMOUNT_IN_RUPEE).toString()));
		newSnap.setInvoiceAmount(new Double(marginMap.get(ConstantUtility.INVOICE_AMOUNT).toString()));
		newSnap.setMargin(new Double(marginMap.get("projectMargin").toString()));
		newSnap.setMarginPerc(new Double(marginMap.get("marginPercentage").toString()));
		newSnap = snapshotRepository.saveAndFlush(newSnap);
		return newSnap;
	}
	
	public UserSnapshots setUserSnapshots(Long projectId,Long userId, Map<String, Object> userData) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date today = calendar.getTime();
		UserSnapshots preSnap = null;
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		Date yesterday = calendar.getTime();
		List<UserSnapshots> preSnaps = userSnapshotsRepository.findAllByProjectIdAndMonthAndYearAndCreationDateAndUserId(projectId,
				LocalDateTime.now().minusMonths(1).getMonthValue(), LocalDateTime.now().minusMonths(1).getYear(),
				yesterday,userId);
		if (!preSnaps.isEmpty())
			preSnap = preSnaps.get(preSnaps.size() - 1);
		UserSnapshots newSnap = new UserSnapshots();
		if (preSnap == null || !preSnap.getExpectedHours().toString().equals(userData.get("expectedHours").toString()) || !preSnap.getHourlySalary().toString().equals(userData.get("perDaySalary").toString()))
			newSnap.setChanged(true);
		else
			newSnap.setChanged(false);
		newSnap.setArchived(false);
		newSnap.setMonth(LocalDateTime.now().minusMonths(1).getMonthValue());
		newSnap.setYear(LocalDateTime.now().minusMonths(1).getYear());
		newSnap.setCreationDate(today);
		newSnap.setProjectId(projectId);
		newSnap.setUserId(new Long(userData.get("userId").toString()));
		newSnap.setCreationDate(today);
		newSnap.setExpectedHours(Double.parseDouble(userData.get("expectedHours").toString()));
		newSnap.setHourlySalary(Double.parseDouble(userData.get("perDaySalary").toString()));
		newSnap.setDirectCost(Double.parseDouble(userData.get("employeeSalary").toString()));
		newSnap.setIndirectCost(Double.parseDouble(userData.get("indirectCost").toString()));
		newSnap = userSnapshotsRepository.saveAndFlush(newSnap);
		return newSnap;
	}

	
	
	@Cacheable(value="gradeBasedBuDashboard",key="{#month, #year, #businessVertical}")
	public Map<String, Object> getGradeBasedIndirectCostBuMargin(int month, int year, String accessToken, String businessVertical) {
		double totalReferenceCost = 0.00;
		double totalCommulativeCost = 0.00;
		Map<String, Object> returnValue = new HashMap<String, Object>();
		List<IndirectCostGradeBasedDTO> indirectCosts = new ArrayList<>();
		Double companyCount=0.0;
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		
		Map<String,Object> buCostMap= indirectCostService.getVerticalCost(accessToken, monthName, Integer.toString(year), businessVertical);
		Double buCost = (Double) buCostMap.get("buIndirectCost");
		Double billableCount = (Double) buCostMap.get("buBillableCount");
		Double nonBillableCostSum = 0D;
		if(!businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT))
			nonBillableCostSum = (Double) buCostMap.get("nonBillableCostSum");
		
		List<String> allGrades = payrollTrendsService.getAllGrades(accessToken);
	
		Map<String, Object> userListAndCount = feignLegacyInterface.gradeWiseExpectedHours(month, year);
		Double cummulativeFixedCost=0D;
		double referenceCost = indirectCostService.getReferenceCost(userListAndCount, "E1", month, year);
		List<GradeBasedIndirectCost> indirectCostList = gradeBasedCostRepository.findAllByGradeInAndMonthAndYear(allGrades, month, year);
		for(String grade : allGrades) {
			if(!grade.equals("E1")) {
				GradeBasedIndirectCost indirectCost = indirectCostList.stream().filter(i->i.getGrade().equals(grade)).findAny().orElse(null);
				IndirectCostGradeBasedDTO indirectCostDTO = setGradeBasedResponse(grade, userListAndCount, month, year, cummulativeFixedCost, accessToken, buCost, referenceCost, indirectCost);
				cummulativeFixedCost=cummulativeFixedCost+(indirectCostDTO.getFixedCost()*indirectCostDTO.getUserCount());
				indirectCosts.add(indirectCostDTO);
			} 
		}
		GradeBasedIndirectCost indirectCost = indirectCostList.stream().filter(i->i.getGrade().equals("E1")).findAny().orElse(null);
		IndirectCostGradeBasedDTO indirectCostDTO = setGradeBasedResponse("E1", userListAndCount, month, year, cummulativeFixedCost, accessToken, buCost, referenceCost, indirectCost);
		totalCommulativeCost = indirectCostDTO.getTotalCommulativeCost();
		companyCount = Math.round(indirectCostDTO.getCompanyCount() * 100.00) / 100.00;
		indirectCosts.add(indirectCostDTO);
		Comparator<IndirectCostGradeBasedDTO> compareByRank=(IndirectCostGradeBasedDTO g1,IndirectCostGradeBasedDTO g2) -> g1.getRank().compareTo(g2.getRank());
		Collections.sort(indirectCosts,compareByRank);
		totalReferenceCost = indirectCosts.stream().collect(Collectors.summingDouble(IndirectCostGradeBasedDTO::getReferenceCost));
		MarginBasis marginBasis = marginBasisRepository.findByMonthAndYear(month, year);
		if(marginBasis!=null)
			returnValue.put("marginBasis", marginBasis.getIsGradeWise());
		else
			returnValue.put("marginBasis", false);
		returnValue.put("indirectCosts", removeHigherGrades(indirectCosts));
		returnValue.put("totalReferenceCost", totalReferenceCost);
		returnValue.put("totalFixedCost", cummulativeFixedCost);
		returnValue.put("totalCommulativeCost", totalCommulativeCost);
		returnValue.put("companyCount", companyCount);
		returnValue.put("ccCost", buCost);
		returnValue.put("totalCcCost", buCost*billableCount);
		returnValue.put("nonBillableCostSum", nonBillableCostSum);
		return returnValue;
	}
	
	private IndirectCostGradeBasedDTO setGradeBasedResponse(String grade, Map<String,Object> userListAndCount,int month,int year,Double cummulativeFixedCost,String accessToken,Double buCost, Double referenceCost, GradeBasedIndirectCost indirectCost) {
		Map<String, Object> currentGradeUserListAndCount = (Map<String, Object>) userListAndCount.get(grade);
		IndirectCostGradeBasedDTO indirectCostDTO = new IndirectCostGradeBasedDTO();
		indirectCostDTO.setUserCount(Math.round(((double) currentGradeUserListAndCount.get("userCount"))*100.00)/100.00);
		indirectCostDTO.setGrade(grade);
		indirectCostDTO.setReferenceCost(Math.round(referenceCost * 100.00) / 100.00);
		indirectCostDTO.setRank(new Long( currentGradeUserListAndCount.get("rank").toString()));
		if(grade.equals("E1")) {
			indirectCostService.calculateE1FixedCost(month, year, indirectCostDTO, accessToken,cummulativeFixedCost,"");
			indirectCostDTO.setBuCost(indirectCostDTO.getFixedCost()+buCost);
			cummulativeFixedCost=cummulativeFixedCost+(indirectCostDTO.getFixedCost()*indirectCostDTO.getUserCount());
		}
		else {
			double fixedCost = indirectCost !=null ? indirectCost.getFixedCost() : 0.00;						
			indirectCostDTO.setFixedCost(fixedCost);
			indirectCostDTO.setBuCost(fixedCost+buCost);
			cummulativeFixedCost = cummulativeFixedCost + (fixedCost * (double) currentGradeUserListAndCount.get("userCount"));
		}
		return indirectCostDTO;
	}
	
	private List<IndirectCostGradeBasedDTO> removeHigherGrades(List<IndirectCostGradeBasedDTO> indirectCosts) {
		return indirectCosts.parallelStream().filter(
				cost -> !cost.getGrade().equals("V") 
						&& !cost.getGrade().equals("C")
						&& !cost.getGrade().equals("D")
						&& !cost.getGrade().equals("NA")
				).collect(Collectors.toList());
	}

	public Map<String, Object> getLifeTimeGradewiseCost(String accessToken, int year) {
		
		return null;
	}
	
	public Map<String,Object> getUicPerSeat(String accessToken,int month,int year){
		Map<String, Object> data = feignLegacyInterface.getCompanyExpectedHours(accessToken,month, Integer.toString(year));

		Object companyExpectedHours = data.get(ConstantUtility.DATA);
		Map<String, Object> workingDaysData = (Map<String, Object>)feignLegacyInterface.getpayrollWorkingDays(accessToken,month, year).get("data");

		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		
		Double companyCount=0.0;
		if(Double.parseDouble(companyExpectedHours.toString())!=0.0) {
			companyCount=Double.parseDouble(companyExpectedHours.toString())/(Double.parseDouble(workingDays.toString())*8);
		}
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		Months monthEnum = Months.valueOf(monthName.toUpperCase());
		IndirectCost monthIndirectCost = costRepository.findByYearAndIsDeletedAndMonth(Integer.toString(year),
				false, monthEnum);
		Map<String,Object> costMap= new HashMap<>();
		if(monthIndirectCost!=null) {
			Double assetCost= 0D;
			costMap=indirectCostService.getCostData(accessToken, companyCount, monthIndirectCost, "", workingDays,assetCost);
		}
		return costMap;
	}
	
	

	

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> overallBusinessAmount(String accessToken, String bu) {
		List<Map<String, Object>> listOfProjects = (List<Map<String, Object>>) feignLegacyInterface.getBuWiseProjectIds(accessToken, bu).get("data");
		List<Map<String, Object>> result = new ArrayList<>();
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
		Root<ProjectInvoice> pi = query.from(ProjectInvoice.class);
		query.multiselect(pi.get("projectId"),pi.get("amountInDollar"));
		List<Object[]> list = entityManager.createQuery(query).getResultList();
		listOfProjects.forEach(project -> {
			Map<String, Object> res = new HashMap<>();
			List<Object[]> invoices = list.stream()
					.filter(p -> (p[0].toString()).equals(project.get("projectId").toString())).collect(Collectors.toList());
		
			Double totalAmount = 0.0;
			for (Object[] invoice : invoices) {
				totalAmount = totalAmount + Double.parseDouble(invoice[1].toString());
			}
			res.put("projectId", Long.parseLong(project.get("projectId").toString()));
			res.put("invoiceAmount", Math.round(totalAmount * 100.0) / 100.0);
			result.add(res);
		});
		return result;
	}
	

	 
		public Map<String, Object> getAllCompensation(int month, int year) {
			Query q = entityManager.createNativeQuery(
					"select user_id,special_allowance,voluntary_pay_amount,incentives,pay_days,unpaid_days from pay_roll where month=:month and year=:year and is_deleted=false");
			q.setParameter("month", month);
			q.setParameter("year", year);
			List<Object[]> payrollObj = q.getResultList();
			DoubleEncryptDecryptConverter converter = new DoubleEncryptDecryptConverter();
			Map<String, Object> response = new HashMap<>();
			Map<String, Object> voluntaryPayResponse = new HashMap<>();
			List<Object[]> payrolls = payrollObj.stream().filter(pay -> converter.convertToEntityAttribute(pay[2].toString()) != 0D)
					.collect(Collectors.toList());
			Double voluntaryPayAmount= payrolls.stream()
					.mapToDouble(pay -> converter.convertToEntityAttribute(pay[2].toString())).sum();
			
			payrolls.stream().forEach(payrol -> {
				voluntaryPayResponse.put(payrol[0].toString(),
						converter.convertToEntityAttribute(payrol[2].toString()));
			});
			voluntaryPayResponse.put("voluntaryPayAmount", voluntaryPayAmount);
			
			Map<String, Object> specialAmountResponse = new HashMap<>();
			double specialAllAmount = payrollObj.stream()
					.mapToDouble(pay -> converter.convertToEntityAttribute(pay[1].toString())).sum();
			List<Object[]> filteredPayrolls = payrollObj.stream()
					.filter(pay -> converter.convertToEntityAttribute(pay[1].toString()) != 0D).collect(Collectors.toList());
			filteredPayrolls.forEach(payrol -> {
				specialAmountResponse.put(payrol[0].toString(),
						converter.convertToEntityAttribute(payrol[1].toString()));
			});
			specialAmountResponse.put("specialAllowanceAmount", specialAllAmount);
			
			Map<String, Object> incentives = new HashMap<>();
			List<Object[]> filteredIncentives = payrollObj.stream()
					.filter(pay -> converter.convertToEntityAttribute(pay[3].toString()) != 0D).collect(Collectors.toList());
			filteredIncentives.forEach(payrol -> {
				incentives.put(payrol[0].toString(), converter.convertToEntityAttribute(payrol[3].toString()));
			});

			YearMonth yearMonth=YearMonth.of(year, month);
			LocalDate firstDay=yearMonth.atDay(1);
			int lengthOfMonth=yearMonth.lengthOfMonth();
			LocalDate itrdate=firstDay;
			AtomicInteger weekendCount=new AtomicInteger();
			weekendCount.set(0);
			for(int i=1;i<=lengthOfMonth;i++) {
				if(itrdate.getDayOfWeek()==DayOfWeek.SATURDAY || itrdate.getDayOfWeek()==DayOfWeek.SUNDAY)
					weekendCount.getAndIncrement();
				itrdate=itrdate.plusDays(1);
				
			}
			
			Map<String, Object> paidDays = new HashMap<>();
			payrollObj.forEach(payrol -> {
				int userPaidDays=(int) Double.parseDouble(payrol[4].toString());
				int userDays=(int) (Double.parseDouble(payrol[4].toString())+Double.parseDouble(payrol[5].toString()));
				if(userDays==lengthOfMonth)
					paidDays.put(payrol[0].toString(), Double.parseDouble(payrol[4].toString())-weekendCount.get() );
				else {
					LocalDate loopDate=firstDay;
					AtomicInteger paidWeekendCount=new AtomicInteger();
					paidWeekendCount.set(0);
					for(int i=1;i<=userPaidDays;i++) {
						if(loopDate.getDayOfWeek()==DayOfWeek.SATURDAY || loopDate.getDayOfWeek()==DayOfWeek.SUNDAY)
							paidWeekendCount.getAndIncrement();
						loopDate=loopDate.plusDays(1);
						
					}
					paidDays.put(payrol[0].toString(), Double.parseDouble(payrol[4].toString())-paidWeekendCount.get() );
					
				}
			});
			
			
			
			response.put("IncentivesResponse", incentives);
			response.put("voluntaryPayResponse", voluntaryPayResponse);
			response.put("specialAmountResponse", specialAmountResponse);
			response.put("PayDaysResponse", paidDays);
			return response;
		}
 
		List<Object> getProjectMap(Double cost, List<Object> projectsList, String projectName, Double projectDevCost, Double buIndirectCost) {
			Map<String, Object> map = new HashMap<>();
			map.put(ConstantUtility.PROJECT_NAME, projectName);
			map.put("cost", cost);
			map.put(ConstantUtility.TOTAL_BU_COST, projectDevCost);
			map.put(ConstantUtility.TOTAL_GRADE_COST, buIndirectCost);
			projectsList.add(map);
			return projectsList;
		}
	 
}
