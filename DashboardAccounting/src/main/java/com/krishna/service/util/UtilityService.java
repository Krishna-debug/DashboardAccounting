package com.krishna.service.util;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.krishna.Interfaces.DollarCostService;
import com.krishna.controller.FeignLegacyInterface;
import com.krishna.controller.FeignZuulInterface;
import com.krishna.domain.CostForecasting;
import com.krishna.domain.GradeBasedIndirectCost;
import com.krishna.domain.IndirectCost;
import com.krishna.domain.PayRegister;
import com.krishna.domain.PayRevisions;
import com.krishna.domain.UserModel;
import com.krishna.domain.Margin.AnalyticServer;
import com.krishna.domain.Margin.BuReserve;
import com.krishna.domain.Margin.BuReserveDeductions;
import com.krishna.domain.Margin.OverAllPl;
import com.krishna.domain.Margin.ProjectExpectedHours;
import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.domain.variablePay.VariablePay;
import com.krishna.dto.IndirectCostGradeBasedDTO;
import com.krishna.enums.Months;
import com.krishna.repository.AnalyticRepository;
import com.krishna.repository.BuReserveDeductionRepository;
import com.krishna.repository.BuReserveRepository;
import com.krishna.repository.CostForecastingRepo;
import com.krishna.repository.GradeBasedIndirectCostRepository;
import com.krishna.repository.IndirectCostRepository;
import com.krishna.repository.OverAllPlRepo;
import com.krishna.repository.invoice.ProjectInvoiceRepository;
import com.krishna.repository.payroll.PayRegisterRepository;
import com.krishna.repository.payroll.PayRevisionRepository;
import com.krishna.repository.variablePay.VariablePayRepository;
import com.krishna.security.JwtValidator;
import com.krishna.service.CompanyMarginService;
import com.krishna.service.IndirectCostService;
import com.krishna.service.LoginUtiltiyService;
import com.krishna.service.PayrollTrendsImpl;
import com.krishna.service.ProjectInvoiceService;
import com.krishna.service.ProjectMarginService;
import com.krishna.util.ConstantUtility;

/**
 * Utility Service for All Utility API's we are using in different Modules
 * @author shivangi
 *
 */
@Service
public class UtilityService {
	
	
	@Autowired
	LoginUtiltiyService loginUtilityService;
	
	@Autowired
	PayRevisionRepository payRevisionRepository;
	
	@Autowired
	PayRegisterRepository payRegisterRepository;
	
	@Autowired
	ProjectMarginService projectMarginservice;
	
	@Autowired
	PayrollTrendsImpl payrollTrendsService;
	
	@Autowired
	GradeBasedIndirectCostRepository gradeBasedCostRepository;
	
	@Autowired
	IndirectCostService indirectCostService;
	
	@Autowired
	IndirectCostRepository costRepository;
	
	@Autowired
	DollarCostService dollarCostService;
	
	@Autowired
	FeignLegacyInterface feignLegacyInterface;
	
	@Autowired
	VariablePayRepository variableRespository;
	
	@Autowired
	AnalyticRepository analyticsRepository;
	
	@Autowired
	JwtValidator validator;
	
	@Autowired
	BuReserveRepository buReserveRepo;
	
	@Autowired
	BuReserveDeductionRepository deductionRepo;
	
	@Autowired
	ProjectInvoiceRepository invoiceRepository;

	@Autowired
	FeignZuulInterface feignZuulInterface;
	
	@Autowired
	private CompanyMarginService companyMarginService;
	
	@Autowired
	private OverAllPlRepo overAllPlRepo;
	
	@Autowired
	private ProjectInvoiceService projectInvoiceService;
	
	@Autowired 
	private ConsolidatedService consolidatedService;
	
	@Autowired
	private CostForecastingRepo costForecastingRepo;
	

	/**
	 * Get Laptop Allowance of users owning their own assets
	 * @author shivangi
	 * @param accessToken
	 * @param userIds
	 * @return list of users Along with their L.A.
	 */
	public List<Object> getLaptopAllowanceForAsset(String accessToken, List<Long> userIds) {
		List<Object> userList=new ArrayList<Object>();
		userIds.forEach(id->{
			double laptopAllowance=0.0;
			PayRegister payregister=payRegisterRepository.findAllByUserIdAndIsCurrent(id, true);
			if(payregister!=null) {
				laptopAllowance=payregister.getLaptopAllowance();
			}
			Map<String,Object> map=new HashMap<>();
			map.put("userId", id);
			Map<String,Object> userInfoMap=(Map<String, Object>) feignLegacyInterface.getUserBasicInfo(accessToken,id).get("data");
			map.put("name", userInfoMap.get("fullName"));
			map.put("laptopAllowance", laptopAllowance);
			userList.add(map);
		});
		return userList;
	}
	
	public PayRegister getMonthsalary(Long userId, int month, int year) {
		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDate lastDay = yearMonth.atEndOfMonth();
		LocalDate firstDay=yearMonth.atDay(1);
		PayRegister currentPayregister = payRegisterRepository.findAllByUserIdAndIsCurrent(userId, true);
		if (currentPayregister != null) {
			if (currentPayregister.getEffectiveDate().toLocalDate().isBefore(lastDay.plusDays(1)) || currentPayregister.getEffectiveDate().toLocalDate().isEqual(lastDay)) {
				return currentPayregister;
			} else {
				currentPayregister=getSalaryFromPayrevisions(userId, lastDay, firstDay);
				return currentPayregister;
			}
		} else {
			return null;
		}
		
	}
	
	public PayRegister getSalaryFromPayrevisions(Long userId, LocalDate lastDay,LocalDate firstDay){
		List<PayRevisions> payrevisions = payRevisionRepository.findAllByUserIdAndIsDeleted(userId,false);
		PayRegister payRegister=null;
		List<PayRevisions> finalpayrevision = new ArrayList<>();
		if (!payrevisions.isEmpty()) {
			payrevisions.forEach(payrev -> {
				LocalDate effectiveFrom=payrev.getEffectiveFrom().toLocalDate();
				LocalDate effectiveTo=payrev.getEffectiveTo().toLocalDate();
				boolean isEffective=projectMarginservice.checkEffectiveDate(firstDay,lastDay,effectiveFrom,effectiveTo);
				if (isEffective) {
					finalpayrevision.add(payrev);
				}
			});
		}
		
		if (!finalpayrevision.isEmpty()) {
			if (finalpayrevision.size() > 1) {
				PayRevisions dataPayrev = finalpayrevision.get(finalpayrevision.size() - 1);
				payRegister = payRegisterRepository.findAllById(dataPayrev.getPayRegister().getId());
			} else {
				PayRevisions dataPayrev = finalpayrevision.get(0);
				payRegister = payRegisterRepository.findAllById(dataPayrev.getPayRegister().getId());
			}
			return payRegister;
		} else {
			return null;
		}
	}
	
	public Map<String,Double> getGradeWiseCosts(int month,int year,String accessToken,String businessVertical, Map<String, Object> userListAndCount ){
		List<String> allGrades = payrollTrendsService.getAllGrades(accessToken);
		Double buCost = 0D;
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		Months monthEnum = Months.valueOf(monthName.toUpperCase());
		IndirectCost monthlyIndirectCost = costRepository.findByYearAndIsDeletedAndMonth(Integer.toString(year), false, monthEnum);
		Map<String,Double> costs=new HashMap<>();
		List<GradeBasedIndirectCost> allCosts=gradeBasedCostRepository.findAllByMonthAndYearAndIsVariable(month,year,false);
		if(monthlyIndirectCost!=null) {
		if(!businessVertical.equals("")) {
			Map<String, Double> gradeWiseCosts=getGradeWiseCosts(month, year, accessToken, "", userListAndCount);
			buCost = indirectCostService.buIndirectCost(accessToken, businessVertical, month, Integer.toString(year),gradeWiseCosts);
		}
		
		double cummulativeFixedCost=0.0;
		for(String grade : allGrades) {
			Map<String, Object> currentGradeUserListAndCount = (Map<String, Object>) userListAndCount.get(grade);
			if(!grade.equals("E1")) {
				List<GradeBasedIndirectCost> filteredObj = allCosts.stream().filter(data->data.getGrade().toString().equals(grade)).collect(Collectors.toList());
				GradeBasedIndirectCost indirectCost = null;
				if(!filteredObj.isEmpty())
					indirectCost = filteredObj.get(0);
				double fixedCost = indirectCost!=null ? indirectCost.getFixedCost() : 0.00;	
				cummulativeFixedCost = cummulativeFixedCost + (fixedCost * (double) currentGradeUserListAndCount.get(ConstantUtility.USER_COUNT));
				costs.put(grade, fixedCost+buCost);
			}
		}
		IndirectCostGradeBasedDTO indirectCostDTO = new IndirectCostGradeBasedDTO();
		Map<String, Object> currentGradeUserListAndCount = (Map<String, Object>) userListAndCount.get("E1");
		indirectCostDTO.setUserCount(Math.round(((double) currentGradeUserListAndCount.get(ConstantUtility.USER_COUNT))*100.00)/100.00);
		indirectCostService.calculateE1FixedCost(month, year, indirectCostDTO, accessToken,cummulativeFixedCost,"");
		costs.put("E1", indirectCostDTO.getFixedCost()+buCost);
		costs.put("buCost", buCost);
		return costs;
		}
		else return null;
	}
	
	public Map<String,Double> getGradeWiseCostsV2(int month,int year,String accessToken,String businessVertical, String source){
		Map<String,Double> gradeWiseCostMap=new HashMap<>();
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		Months monthEnum = Months.valueOf(monthName.toUpperCase());
		List<String> allGrades = payrollTrendsService.getAllGrades(accessToken);
		List<GradeBasedIndirectCost> allCosts = gradeBasedCostRepository.findAllByMonthAndYearAndIsVariable(month,year,false);
		IndirectCost monthlyIndirectCost = costRepository.findByYearAndIsDeletedAndMonth(Integer.toString(year), false, monthEnum);
		int ptr=6;
		YearMonth yearMonth = YearMonth.of(year, month);
		if(source.equals(ConstantUtility.FORECAST)) {
			while(monthlyIndirectCost==null && ptr<=6) {
				yearMonth = yearMonth.minusMonths(1);
				String monthNam = new DateFormatSymbols().getMonths()[yearMonth.getMonthValue() - 1].toString();
				Months monthEnu = Months.valueOf(monthNam.toUpperCase());
				monthlyIndirectCost = costRepository.findByYearAndIsDeletedAndMonth(Integer.toString(yearMonth.getYear()), false, monthEnu);
				ptr--;
			}
		}
		if((monthlyIndirectCost!=null && source.equals("")) || source.equals(ConstantUtility.FORECAST)) {
			double cummulativeFixedCost=0.0;
			Map<String, Object> userListAndCount = feignLegacyInterface.gradeWiseExpectedHours(month, year);
			
			for(String grade : allGrades) {
				Map<String, Object> currentGradeUserListAndCount = (Map<String, Object>) userListAndCount.get(grade);
				if(!grade.equals("E1")) {
					List<GradeBasedIndirectCost> filteredObj = allCosts.stream().filter(data->data.getGrade().toString().equals(grade)).collect(Collectors.toList());
					GradeBasedIndirectCost indirectCost = null;
					if(!filteredObj.isEmpty())
						indirectCost = filteredObj.get(0);
					double fixedCost = indirectCost!=null ? indirectCost.getFixedCost() : 0.00;	
					if(source.equals(ConstantUtility.FORECAST))
						cummulativeFixedCost = cummulativeFixedCost + (fixedCost * (double) currentGradeUserListAndCount.get("forecastedCount"));
					else
						cummulativeFixedCost = cummulativeFixedCost + (fixedCost * (double) currentGradeUserListAndCount.get(ConstantUtility.USER_COUNT));
					gradeWiseCostMap.put(grade, fixedCost);
				}
			}
			IndirectCostGradeBasedDTO indirectCostDTO = new IndirectCostGradeBasedDTO();
			Map<String, Object> currentGradeUserListAndCount = (Map<String, Object>) userListAndCount.get("E1");
			
			if(source.equals(ConstantUtility.FORECAST))
				indirectCostDTO.setUserCount(Math.round(((double) currentGradeUserListAndCount.get("forecastedCount"))*100.00)/100.00);
			else
				indirectCostDTO.setUserCount(Math.round(((double) currentGradeUserListAndCount.get(ConstantUtility.USER_COUNT))*100.00)/100.00);
			indirectCostService.calculateE1FixedCost(month, year, indirectCostDTO, accessToken,cummulativeFixedCost,source);
			gradeWiseCostMap.put("E1", indirectCostDTO.getFixedCost());
			return gradeWiseCostMap;
		}
		else {
			return null;
		}
	}
	
	public Map<String,Object> getBusinessVerticalDetails(String accessToken, String businessVertical) {
		Map<String, Object> buDetails=feignLegacyInterface.getBusinessVerticalDetails(accessToken);
		List<Map<String, Object>> buList=(List<Map<String, Object>>) buDetails.get("data");
		return buList.parallelStream().filter(bu -> (bu.get("name").toString()).equals(businessVertical))
		.findFirst().orElse(null);
	}
	
	public Map<String,Object> getBuInternalInvoices(String accessToken,String businessVertical,int month, int year){
		Map<String,Object> budetails=getBusinessVerticalDetails(accessToken, businessVertical);
		Map<String, Object> invoiceData = new HashMap<>();
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		Double dollarexchangeCost=(Double) dollarCostService.getAverageDollarCost(month, year);
		invoiceData=projectMarginservice.getInternalInvoices(invoiceData, monthName, Integer.toString(year), new Long(budetails.get("id").toString()), dollarexchangeCost);
		return invoiceData;
	}
	
	public Integer getWorkingDaysBetweenDates(Date start,Date end){
		long diff = end.getTime() - start.getTime();
		int totalNoOfDays=(int) ((diff / (1000*60*60*24))+1);
		int workingDays=totalNoOfDays;
		for(int j=0;j<totalNoOfDays;j++){
			if(start.getDay()==0 || start.getDay()==6){
				workingDays--;
			}
			start=DateUtils.addDays(start, 1);
		}
		return workingDays;
	}
	
	public Map<String,Object> getVariableAmounts(int month,int year){
		List<VariablePay> variablePays=variableRespository.findAllByMonthAndYearAndIsDeleted(month,year,false);
		Map<String,Object> response=new HashMap<>();
		variablePays.forEach(pay->{
			response.put(pay.getUserId().toString(), pay.getAmount());
		});
		return response;
	}

	public Boolean toggleServer(String accessToken, String buName) {
		AnalyticServer server=analyticsRepository.findByBuName(buName);
		if(server!=null) 
			server.setEnable(!server.getEnable());
		else {
			server = new AnalyticServer();
			server.setBuName(buName);
			server.setEnable(true);
		}
		server=analyticsRepository.save(server);
		return server.getEnable();
	}
	
	public Boolean useAnalyticServer(String accessToken, String buName) {
		AnalyticServer server=analyticsRepository.findByBuName(buName);
		UserModel currentUser=validator.tokenbValidate(accessToken);
		boolean useAnalyticServer=false;
		if(server!=null && (currentUser.getRoles().contains("ROLE_ADMIN") && currentUser.getRoles().contains("ROLE_DASHBOARD_ADMIN"))) {
			useAnalyticServer=server.getEnable();
		}
		return useAnalyticServer;
	}

	public Map<String,Map<String,Double> > syncOlapData(Integer month, Integer year) {
		String monthName=new DateFormatSymbols().getMonths()[month-1].toString();
		List<ProjectInvoice> monthlyInvoices = invoiceRepository.findAllByMonthAndYearAndIsDeletedAndIsInternalAndInvoiceStatusNot(
				monthName, year.toString(), false, false, 6L);
		
		List<Long> projectIds=monthlyInvoices.stream().distinct().map(ProjectInvoice::getProjectId).collect(Collectors.toList());
		Map<String,Map<String,Double> > responseMap=new HashMap<>();
		Map<String,Double> dollarInvoiceMap=new HashMap<>();
		Map<String,Double> rupeeInvoiceMap=new HashMap<>();
		Double dollarexchangeCost=(Double) dollarCostService.getAverageDollarCost(month, year);
		projectIds.forEach(id->{
			List<ProjectInvoice> filteredInvoices=monthlyInvoices.stream().filter(inv->inv.getProjectId().toString().equals(id.toString())).collect(Collectors.toList()); 
			Double invoiceInDollar=filteredInvoices.stream().collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar));
			Double invoiceInRupee=dollarexchangeCost*invoiceInDollar;
			if(!dollarInvoiceMap.containsKey(id.toString()))
				dollarInvoiceMap.put(id.toString(), invoiceInDollar);
			if(!rupeeInvoiceMap.containsKey(id.toString()))
				rupeeInvoiceMap.put(id.toString(), invoiceInRupee);
		});
		Map<String,Double> dollarMap=new HashMap<>();
		dollarMap.put("dollarCost", dollarexchangeCost);
		responseMap.put("dollarInvoiceMap", dollarInvoiceMap);
		responseMap.put("rupeeInvoiceMap", rupeeInvoiceMap);
		responseMap.put("dollarCost", dollarMap);
		return responseMap;
	}
	
	@Cacheable(value ="getInternalInvoices", key="{ #businessVertical, #monthName, #year }")
	public Map<String,Object> getInternalInvoices(String accessToken,String businessVertical,String monthName,String year, Double dollarexchangeCost){
		Map<String,Object> budetails=getBusinessVerticalDetails(accessToken, businessVertical);
		Map<String, Object> buMargin =new HashMap<>();
		buMargin = projectMarginservice.getInternalInvoices(buMargin, monthName, year, new Long(budetails.get("id").toString()),
				dollarexchangeCost);
		return buMargin;
	}
	
	public List<BuReserve> syncReserveData() {
		List<BuReserve> reserves= buReserveRepo.findAll();
		return reserves;
	}

	public List<BuReserveDeductions> syncDeductionData() {
		List<BuReserveDeductions> reserves= deductionRepo.findAll();
		return reserves;
	}

	public Map<String,Double> syncSalaryOlapData(Integer month, Integer year) {
		List<PayRegister> currentPayregisters=payRegisterRepository.findAllByIsCurrent(true);
		List<Long> userIds= currentPayregisters.stream().distinct().map(PayRegister::getUserId).collect(Collectors.toList());
		List<PayRevisions> allPayrevisions = payRevisionRepository.findAllByIsDeleted(false);
		Map<String,Double> directCostMap=new HashMap<>();
		
		Object res= feignZuulInterface.getTestTokenFromZuul();
        String accessToken="";
        if(res != null)
			accessToken =(String) (( Map<String,String>) res).get("accesstoken");
		Map<String, Object> workingDaysData = (Map<String, Object>) feignLegacyInterface.getpayrollWorkingDays(accessToken ,month , year).get("data");
		Double workingDays = Double.parseDouble(workingDaysData.get(ConstantUtility.WORKING_DAYS).toString()) ;
		userIds.forEach(userId->{
			PayRegister pay= getMonthsalaryV1(userId, month, year, currentPayregisters, allPayrevisions);
			if(pay!=null)
				directCostMap.put(userId.toString(), ((pay.getTotalAnnualCtc()/12)/(workingDays*8)));
			else
				directCostMap.put(userId.toString(), 0D);
		});
		return directCostMap;
	}
	
	public PayRegister getMonthsalaryV1(Long userId, int month, int year,List<PayRegister> currentPayregisters,List<PayRevisions> allPayrevisions) {
		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDate lastDay = yearMonth.atEndOfMonth();
		LocalDate firstDay=yearMonth.atDay(1);
		List<PayRegister> userCurrentPayregister=currentPayregisters.stream().filter(pay-> Long.toString(pay.getUserId()).equals(userId.toString())).collect(Collectors.toList());
		PayRegister currentPayregister = null;
		if(!userCurrentPayregister.isEmpty())
			currentPayregister=userCurrentPayregister.get(0);
		if (currentPayregister != null) {
			if (currentPayregister.getEffectiveDate().toLocalDate().isBefore(lastDay.plusDays(1)) || currentPayregister.getEffectiveDate().toLocalDate().isEqual(lastDay)) {
				return currentPayregister;
			} else {
				currentPayregister=getSalaryFromPayrevisionsV1(userId, lastDay, firstDay, allPayrevisions);
				return currentPayregister;
			}
		} else {
			return null;
		}
		
	}
	
	public PayRegister getSalaryFromPayrevisionsV1(Long userId, LocalDate lastDay,LocalDate firstDay,List<PayRevisions> allPayrevisions){
		List<PayRevisions> payrevisions = allPayrevisions.stream().filter(pay-> Long.toString(pay.getUserId()).equals(userId.toString())).collect(Collectors.toList());
		PayRegister payRegister=null;
		List<PayRevisions> finalpayrevision = new ArrayList<>();
		if (!payrevisions.isEmpty()) {
			payrevisions.forEach(payrev -> {
				LocalDate effectiveFrom=payrev.getEffectiveFrom().toLocalDate();
				LocalDate effectiveTo=payrev.getEffectiveTo().toLocalDate();
				boolean isEffective=projectMarginservice.checkEffectiveDate(firstDay,lastDay,effectiveFrom,effectiveTo);
				if (isEffective) {
					finalpayrevision.add(payrev);
				}
			});
		}
		
		if (!finalpayrevision.isEmpty()) {
			if (finalpayrevision.size() > 1) {
				PayRevisions dataPayrev = finalpayrevision.get(finalpayrevision.size() - 1);
				payRegister = payRegisterRepository.findAllById(dataPayrev.getPayRegister().getId());
			} else {
				PayRevisions dataPayrev = finalpayrevision.get(0);
				payRegister = payRegisterRepository.findAllById(dataPayrev.getPayRegister().getId());
			}
			return payRegister;
		} else {
			return null;
		}
	}

	public Map<String, Double> syncICData(Integer month, Integer year,String projectBusinessVertical) {
		Map<String, Double> response=new HashMap<>();
		Object res= feignZuulInterface.getTestTokenFromZuul();
		String accessToken="";
		if(res != null)
			accessToken =(String) (( Map<String,String>) res).get("accesstoken");
		List<String> verticals = new ArrayList<String>(
				Arrays.asList("Digital Marketing", "Blockchain", "Artificial Intelligence", "ERP Solution",
						"Oodles Technologies", "Oodles Studio", "Operations Support","DPP"));
		Map<String, Object> userListAndCount = feignLegacyInterface.gradeWiseExpectedHours(month, year);

		for(int i=0;i<verticals.size();i++) {
			Map<String, Double> gradeWiseCosts=getGradeWiseCosts(month, year, accessToken, "",userListAndCount);
			Double buCost=indirectCostService.buIndirectCost(accessToken, projectBusinessVertical, month, Integer.toString(year),gradeWiseCosts);
			response.put(verticals.get(i), buCost);
		}
		return response;
	}
	
	public Boolean saveAllOverallPLData() {
		Object res= feignZuulInterface.getTestTokenFromZuul();
		String accessToken = "";
		if(res != null)
			accessToken =(String) (( Map<String,String>) res).get("accesstoken");
		int currentMonth = 5;
		int currentYear = 2020;

        int yearLimit = 2023;
		while(currentYear<=yearLimit) {
			if(currentMonth==12) {
				currentMonth = 1;
			    currentYear = currentYear + 1;
			}
			else {
				currentMonth = currentMonth + 1;
			}
			Map<String,Object> overallPLData = companyMarginService.getCompanyPL(accessToken, currentMonth, currentYear);
			Map<String,Object> salaryDifference = consolidatedService.getSalaryDifference(accessToken, currentMonth, currentYear);
			OverAllPl overAllPl  = overAllPlRepo.findByMonthAndYear(currentMonth,currentYear);
			
			Map<String, Object> invoiceTotal=companyMarginService.getBuWiseInvoiceTotal(currentMonth,Integer.toString(currentYear),accessToken);
			List<Object> buWiseUsers=companyMarginService.getCompanywiseData(currentMonth,currentYear,accessToken);
			Map<String,Object> directCostTotal=companyMarginService.getDirectCostBuWise(currentMonth,currentYear,buWiseUsers,invoiceTotal,accessToken);
			
			Object totalInvoiceAmount=invoiceTotal.get("companyTotalRevenueInr");
			Object totalProjectCost=directCostTotal.get("companyTotalCost");
			Double totalMargin=0.0;
			totalMargin=Double.parseDouble(totalInvoiceAmount.toString())-Double.parseDouble(totalProjectCost.toString());

			overAllPl = overAllPl != null ? overAllPl : new OverAllPl();
			overAllPl.setMonth(currentMonth);
			overAllPl.setYear(currentYear);
			overAllPl.setTotalProfitAndLossBuWise(totalMargin);
			overAllPl = mapOverAllPlData(overAllPl,overallPLData,salaryDifference);
			overAllPlRepo.save(overAllPl);
		}
		return true;
	}
			
	public Boolean saveOverallPLData() {
		Object res= feignZuulInterface.getTestTokenFromZuul();
		String accessToken = "";
		if(res != null)
			accessToken =(String) (( Map<String,String>) res).get("accesstoken");
		int currentMonth = new Date().getMonth()+1;
		int currentYear = new Date().getYear()+1900;

		while(currentMonth!=0) {
			Map<String,Object> overallPLData = companyMarginService.getCompanyPL(accessToken, currentMonth, currentYear);
			OverAllPl overAllPl  = overAllPlRepo.findByMonthAndYear(currentMonth,currentYear);
			Map<String,Object> salaryDifference = consolidatedService.getSalaryDifference(accessToken, currentMonth, currentYear);
			overAllPl = overAllPl != null ? overAllPl : new OverAllPl();
			Map<String, Object> invoiceTotal=companyMarginService.getBuWiseInvoiceTotal(currentMonth,Integer.toString(currentYear),accessToken);
			List<Object> buWiseUsers=companyMarginService.getCompanywiseData(currentMonth,currentYear,accessToken);
			Map<String,Object> directCostTotal=companyMarginService.getDirectCostBuWise(currentMonth,currentYear,buWiseUsers,invoiceTotal,accessToken);
			
			Object totalInvoiceAmount=invoiceTotal.get("companyTotalRevenueInr");
			Object totalProjectCost=directCostTotal.get("companyTotalCost");
			Double totalMargin=0.0;
			totalMargin=Double.parseDouble(totalInvoiceAmount.toString())-Double.parseDouble(totalProjectCost.toString());

			overAllPl.setMonth(currentMonth);
			overAllPl.setYear(currentYear);
			overAllPl.setTotalProfitAndLossBuWise(totalMargin);

			overAllPl = mapOverAllPlData(overAllPl,overallPLData,salaryDifference);
			overAllPlRepo.save(overAllPl);
			currentMonth = currentMonth - 1;
		}
		return true;
	}
	
	public OverAllPl mapOverAllPlData(OverAllPl overAllPl, Map<String,Object> overallPLData, Map<String,Object> salaryDifference) {
		Function<Object,Double> convertInDouble = value -> {return value != null ? Double.valueOf(String.valueOf(value)) : 0.0D;};
		overAllPl.setEffectiveRevenueDollar(convertInDouble.apply(overallPLData.get("invoiceAmountInDollar")));
		overAllPl.setEffectiveDollarValue(convertInDouble.apply(overallPLData.get("dollarAmount")));
		overAllPl.setEffectiveRevenue(convertInDouble.apply(overallPLData.get("invoiceAmount")));
		overAllPl.setPaymentCharges(convertInDouble.apply(overallPLData.get("paymentChargesSum")));
		overAllPl.setInfraCost(convertInDouble.apply(overallPLData.get("infraCost")));
		overAllPl.setVariableCost(convertInDouble.apply(overallPLData.get("variableCost")));
		overAllPl.setReimbursementAndBonusCost(convertInDouble.apply(overallPLData.get("reimbursementCost")));
		overAllPl.setBuSpecificCost(convertInDouble.apply(overallPLData.get("buSpecifciCost")));
		overAllPl.setVariablePay(convertInDouble.apply(overallPLData.get("variablePay")));
		overAllPl.setVoluntaryPay(convertInDouble.apply(overallPLData.get("voluntaryPay")));
		overAllPl.setTotalIndirectCost(convertInDouble.apply(overallPLData.get("indirectCost")));
		overAllPl.setTotalSalary(convertInDouble.apply(overallPLData.get("payrollAmount")));
		overAllPl.setTotalSalaryBuffer(convertInDouble.apply(overallPLData.get("totalCtcSalary")));
		overAllPl.setTotalCost(convertInDouble.apply(overallPLData.get("totalCost")));
		overAllPl.setTotalProfitAndLoss(convertInDouble.apply(overallPLData.get("profitAmount")));
		overAllPl.setTotalMargin(convertInDouble.apply(overallPLData.get("maginPerc")));
		overAllPl.setNetMargin(convertInDouble.apply(overallPLData.get("netMarginPerc")));
		overAllPl.setYtdDisputed(convertInDouble.apply(overallPLData.get("disputedPercYtd")));
		overAllPl.setLtmDisputed(convertInDouble.apply(overallPLData.get("disputedPercLTM")));
		overAllPl.setNetProfitAndLoss(convertInDouble.apply(overallPLData.get("netMargin")));
		overAllPl.setSalaryDifference(convertInDouble.apply(salaryDifference.get("totalSalaryDifference")));
        return overAllPl;
	}
	
	public  Map<String,Object> getOverAllPlYearWise(Integer year){
		Integer currentMonth = new Date().getMonth()+1;
		List<OverAllPl> overAllPlData = overAllPlRepo.findAllByYearAndMonthLessThan(year,currentMonth);
		Function<Object,Double> convertInDouble = value -> {return value != null && !value.equals("null") ? Double.valueOf(String.valueOf(value)) : 0.0D;};
		BiFunction<Object,Object,Double> addValues = (value,newValue) -> {return convertInDouble.apply(value)+convertInDouble.apply(newValue);};
		Map<String,Object> response= new HashMap<String,Object>();
        for(OverAllPl overAllPl : overAllPlData) {
            
        	response.put("invoiceAmountInDollar", addValues.apply(response.get("invoiceAmountInDollar"), overAllPl.getEffectiveRevenueDollar()));
        	response.put("dollarAmount", addValues.apply(response.get("dollarAmount"), overAllPl.getEffectiveDollarValue()));
        	response.put("invoiceAmount", addValues.apply(response.get("invoiceAmount"), overAllPl.getEffectiveRevenue()));
        	response.put("paymentChargesSum", addValues.apply(response.get("paymentChargesSum"), overAllPl.getPaymentCharges()));
        	response.put("infraCost", addValues.apply(response.get("infraCost"), overAllPl.getInfraCost()));
        	response.put("variableCost", addValues.apply(response.get("variableCost"), overAllPl.getVariableCost()));
        	response.put("reimbursementCost", addValues.apply(response.get("reimbursementCost"), overAllPl.getReimbursementAndBonusCost()));
        	response.put("buSpecifciCost", addValues.apply(response.get("buSpecifciCost"), overAllPl.getBuSpecificCost()));
        	response.put("variablePay", addValues.apply(response.get("variablePay"), overAllPl.getVariablePay()));
        	response.put("voluntaryPay", addValues.apply(response.get("voluntaryPay"), overAllPl.getVoluntaryPay()));
        	response.put("indirectCost", addValues.apply(response.get("indirectCost"), overAllPl.getTotalIndirectCost()));
        	response.put("payrollAmount", addValues.apply(response.get("payrollAmount"), overAllPl.getTotalSalary()));
        	response.put("totalCtcSalary", addValues.apply(response.get("totalCtcSalary"), overAllPl.getTotalSalaryBuffer()));
        	response.put("totalCost", addValues.apply(response.get("totalCost"), overAllPl.getTotalCost()));
        	response.put("profitAmount", addValues.apply(response.get("profitAmount"), overAllPl.getTotalProfitAndLoss()));
        	response.put("totalSalaryDifference", addValues.apply(response.get("totalSalaryDifference"), overAllPl.getSalaryDifference()));
        	response.put("netMargin", addValues.apply(response.get("netMargin"), overAllPl.getNetProfitAndLoss()));
        	response.put("totalMargin", addValues.apply(response.get("totalMargin"), overAllPl.getTotalProfitAndLossBuWise()));

        }
        
    	response.put("maginPerc", (convertInDouble.apply(String.valueOf(response.get("invoiceAmount"))) -convertInDouble.apply(String.valueOf(response.get("totalCost"))))/convertInDouble.apply(String.valueOf(response.get("invoiceAmount"))) * 100);
    	response.put("dollarAmount",convertInDouble.apply(String.valueOf(response.get("dollarAmount"))) / (currentMonth-1));

        return response;
	}
	
	public Map<String,Object> getYearWiseDisputeData(Long year){
		Function<Object,Double> convertInDouble = value -> {return value != null && !value.equals("null") ? Double.valueOf(String.valueOf(value)) : 0.0D;};
		BiFunction<Object,Object,Double> addValues = (value,newValue) -> {return convertInDouble.apply(value)+convertInDouble.apply(newValue);};
		Object res= feignZuulInterface.getTestTokenFromZuul();
		String accessToken = "";

		if(res != null)
			accessToken =(String) (( Map<String,String>) res).get("accesstoken");

		Integer yearMonth = new Date().getMonth();
		Integer currentMonth = new Date().getMonth(); 
	    Integer currentYear = new Date().getYear()+1900;
		Map<String,Object> response = new HashMap<String,Object>();
		Map<String, Object> disputedDataYTD = projectInvoiceService.getAverageDisputedPercentage(year,"",accessToken);
		while(currentMonth!=0) {
		Map<String, Object> disputedDataLTM = projectInvoiceService.getLTMDisputedPercentage(Long.valueOf(currentYear),"",accessToken,currentMonth);
		response.put("averageDisputedPercentageLTMForYear",addValues.apply(response.get("averageDisputedPercentageLTMForYear"), disputedDataLTM.get("averageDisputedPercentageLTM")));
	        currentMonth = currentMonth - 1;
		}
		response.put("disputedPercLTM", Double.parseDouble(new DecimalFormat("#.##").format(convertInDouble.apply(response.get("averageDisputedPercentageLTMForYear"))/(yearMonth*100 ) * 100)));
		response.put("disputedPercYtd", disputedDataYTD.get("averageDisputedPercentage"));
		return response;
	}
	
	
	public Boolean setUserCostForecastingData(String accessToken){
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
				saveCostForeCastSnapshot(accessToken, Long.parseLong(proId.toString()), LocalDateTime.now().minusMonths(1).getMonthValue(), LocalDateTime.now().minusMonths(1).getYear());
			}
		}
		return true;
	}
	
	public void saveCostForeCastSnapshot(String accessToken, Long projectId, int month , int year){
		Map<String, Object> marginMap = null;
		Date today = new Date();
		marginMap = projectMarginservice.getDirectCost(projectId,month,
				year, accessToken, true);
		
		List<Object> team=(List<Object>) marginMap.get("teamData");
		int teamSize=team.size();
		
		for(int i=0;i<teamSize;i++) {
		Map<String,Object> userData=(Map<String, Object>) team.get(i);
		Long userId=new Long( userData.get("userId").toString());

		CostForecasting costForecasting = new CostForecasting();
			
		costForecasting.setMonth(month);
		costForecasting.setYear(year);
		costForecasting.setProjectId(projectId);
		costForecasting.setUserId(userId);
		costForecasting.setCreationDate(today);
		costForecasting.setArchived(false);
		costForecasting.setExpectedHours(Double.parseDouble(userData.get("expectedHours").toString()));
		costForecasting.setHourlySalary(Double.parseDouble(userData.get("perDaySalary").toString()));
		costForecasting.setMonthlyHours(Double.parseDouble(userData.get("monthly_hours").toString()));
		costForecasting.setDirectCost(Double.parseDouble(userData.get("employeeSalary").toString()));
		costForecasting.setIndirectCost(Double.parseDouble(userData.get("indirectCost").toString()));
		costForecasting.setForecastedHours(Double.parseDouble(userData.get("forecasted_hours").toString()));
		costForecasting.setResourcingHours(Double.parseDouble(userData.get("resourcing_hours").toString()));
		costForecasting.setForecastedAccountingHours(Double.parseDouble(userData.get("forecasted_accounting_hours").toString()));
		costForecasting = costForecastingRepo.save(costForecasting);
	}
	}
	
	public List<CostForecasting> getCostForecastingData(String accessToken){
		List<CostForecasting> response = costForecastingRepo.findAllByIsArchivedFalse();
		return response;
	}
	
}
