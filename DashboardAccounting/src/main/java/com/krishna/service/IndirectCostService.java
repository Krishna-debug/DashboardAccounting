package com.krishna.service;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import com.krishna.controller.FeignLegacyInterface;
import com.krishna.controller.IndirectCostController;
import com.krishna.domain.Arrear;
import com.krishna.domain.GradeBasedIndirectCost;
import com.krishna.domain.IndirectCost;
import com.krishna.domain.PayRegister;
import com.krishna.domain.PayRevisions;
import com.krishna.domain.Payroll;
import com.krishna.domain.Margin.BuReserve;
import com.krishna.domain.Margin.BuReserveDeductions;
import com.krishna.domain.Margin.MarginBasis;
import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.dto.IndirectCostDto;
import com.krishna.dto.IndirectCostGradeBasedDTO;
import com.krishna.dto.PayrollPaidUnpaidDto;
import com.krishna.enums.Months;
import com.krishna.repository.BuReserveRepository;
import com.krishna.repository.GradeBasedIndirectCostRepository;
import com.krishna.repository.IndirectCostRepository;
import com.krishna.repository.MarginBasisRepository;
import com.krishna.repository.invoice.ProjectInvoiceRepository;
import com.krishna.repository.payroll.ArrearRepository;
import com.krishna.repository.payroll.PayRegisterRepository;
import com.krishna.repository.payroll.PayRevisionRepository;
import com.krishna.service.util.ConsolidatedService;
import com.krishna.service.util.UtilityService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.EncryptionUtil;


@Service
public class IndirectCostService {
	@Autowired
	IndirectCostRepository costRepository;
	

	@Autowired
	PayRegisterRepository payRegisterRepository;
	
	@Autowired
	PayRevisionRepository payRevisionRepository;
	
	@Autowired
	ProjectInvoiceRepository projectInvoiceRepository;
	
	Logger log=LoggerFactory.getLogger(IndirectCostService.class);
	
	@Autowired
	LoginUtiltiyService loginUtilityService;
	
	@Autowired
	ProjectMarginService projectMarginservice;
	
	@Autowired
	ConsolidatedService consolidatedService;
	
	@Autowired
	EncryptionUtil<Double> encrypter;
	
	@Autowired
	private PayrollTrendsImpl payrollTrendsService;
	
	@Autowired
	private UtilityService utilityService;
	
	@Autowired
	private MarginBasisRepository marginBasisRepository;
	
	@Autowired
	private GradeBasedIndirectCostRepository gradeBasedCostRepository;
	
	@Autowired
	private FeignLegacyInterface feignLegacyInterface;
	
	@Autowired
	ProjectInvoiceService projectInvoiceService;
	
	@Value("${com.oodles.accounts.email}")
	private String accountsMail;
	
	@Value("${env.url}")
	private String environmentUrl;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	EntityManager entityManager;
	
	@Autowired
	CompanyMarginService companyMarginService;
	
	@Autowired
	BuReserveRepository bureserveRepo;
	
	@Autowired
	ArrearRepository arrearRepo;

	
	public IndirectCost addIndirectCost(IndirectCostDto indirectCostDto, Long userId, String accessToken) {
		Optional<IndirectCost> data = costRepository.findByMonthAndYearAndIsDeleted(indirectCostDto.getMonth(), indirectCostDto.getYear(), false);
		if(indirectCostDto.getYear().isEmpty())
			return null;
		IndirectCost indirectCost = new IndirectCost();
		if(!data.isPresent()) {
			BeanUtils.copyProperties(indirectCostDto, indirectCost);
			if(indirectCostDto.getReimbursement()==null)
			indirectCost.setReimbursement(0D);
			indirectCost.setCreatedBy(userId);
			indirectCost.setCreateOn(new Date());
			indirectCost.setIsDeleted(false);
			costRepository.save(indirectCost);
			int month = Month.valueOf(indirectCostDto.getMonth().toString().toUpperCase()).getValue();
			MarginBasis marginBasis = marginBasisRepository.findByMonthAndYear(month,Integer.parseInt(indirectCostDto.getYear()));
			if(marginBasis!=null){
				marginBasis.setIsLTM(true);
				marginBasisRepository.save(marginBasis);
				
			}
			else{
				setMarginBasis(month, Integer.parseInt(indirectCostDto.getYear()), true);

			}
			
			freezeReserve(indirectCostDto.getMonth().toString(), indirectCostDto.getYear());
			sendMailOnUpdatationMargin(indirectCostDto.getMonth(),indirectCostDto.getYear(),accessToken);
			
		}
		return indirectCost;
	}
	
	private void freezeReserve(String month, String year) {
		int monthValue = Month.valueOf(month.toUpperCase()).getValue();
		int previousMonth = Month.of(monthValue).minus(1).getValue();
		int previousYear = YearMonth.of(Integer.parseInt(year), monthValue).minusMonths(1).getYear();
		List<BuReserve> reserves=bureserveRepo.findAllByMonthAndYear(previousMonth,previousYear);
		reserves.stream().forEach(reserve->{
			reserve.setReserveChanged(false);
			bureserveRepo.save(reserve);
			log.debug(": freezeReserve BuName : "+reserve.getBuName());
		});
	}
	
	public void sendMailOnUpdatationMargin(Months month, String year, String accessToken) {
		LocalDate currentDate = LocalDate.now();
		List<String> buisnessVertical = (List<String>) projectInvoiceService.getBusinessVerticals(accessToken);
		for (String buName : buisnessVertical) {
			Map<String, Object> buOwnerDetails = (Map<String, Object>) feignLegacyInterface
					.getBuOwnerInfo(accessToken, buName).get("data");
			if (buOwnerDetails != null) {
				String buOwnerEmail = buOwnerDetails.get("ownerEmail").toString();
				String[] ccArray = new String[1];
				ccArray[0] = accountsMail; 
				Context context = new Context();
				context.setVariable("bu", buName);
				context.setVariable("monthName", month);
				context.setVariable("year",year);
				context.setVariable("buOwner", buOwnerDetails.get("ownerName").toString());
				context.setVariable("currentYear", LocalDate.now().getYear());
				context.setVariable("responseUrl",
						"https://" + environmentUrl + "/#/vertical/"+buName);
				String subject = "Indirect Cost Updated || " + month + " "+year;
				if (!buOwnerEmail.equals("")) {
					mailService.sendScheduleHtmlMailWithCc(buOwnerEmail, subject, context, "Margin-Updation",
							ccArray);
				}
			}
		}
	}
	
	
	
	
	
	public Map<String, Object> getCostData(String accessToken, Double companyCount,IndirectCost indirectCost,String businessVertical,Object workingDays,Double totalAssetCost){
		int month=getMonthNumber(indirectCost.getMonth().getMonths());
		Double infraCost=indirectCost.getInfraCost();
		Double variableCost=indirectCost.getVariableCost();
		Double reimbursementCostTotal = companyMarginService.getReimbursementCost(accessToken, month,
				Integer.parseInt(indirectCost.getYear()));

		Double assetCost = 0D;
		Double reimbursementCost = 0D;
		if (companyCount != 0.0) {
			infraCost = (indirectCost.getInfraCost() / companyCount);
			variableCost = (indirectCost.getVariableCost() / companyCount);
			reimbursementCost = reimbursementCostTotal != null ? (reimbursementCostTotal / companyCount) : 0.00;
			assetCost = totalAssetCost / companyCount;
		}
		Map<String,Object> map=new HashMap<>();
		map.put("employees", Math.round(companyCount*100.0)/100.0);
		map.put("infraCost", Math.round(infraCost*100.0)/100.0);
		map.put(ConstantUtility.TOTAL_INFRA_COST, indirectCost.getInfraCost());
		map.put("variableCost", Math.round(variableCost*100.0)/100.0);
		map.put(ConstantUtility.TOTAL_VARIABLE_COST, indirectCost.getVariableCost());
		map.put("reimbursementCost", Math.round(reimbursementCost*100.0)/100.0);
		map.put(ConstantUtility.TOTAL_REIMBURSMENT_COST,reimbursementCostTotal);
		map.put(ConstantUtility.TOTAL_ASSET_COST, totalAssetCost);
		map.put(ConstantUtility.ASSET_COST, assetCost);
		Double staffCost=getStaffCost(accessToken,month,indirectCost.getYear(),workingDays,ConstantUtility.OPERATIONS_SUPPORT);
		Double staffCostPerSeat=((Math.round(staffCost*100)/100)/companyCount);
		map.put("staffCost", Math.round(staffCostPerSeat)*100.0/100.0);
		map.put("month", indirectCost.getMonth());
		map.put("year", indirectCost.getYear());
		map.put("id", indirectCost.getId());
		Double totalCost=0.0;
		if (businessVertical == null || businessVertical.equals("")) {
			totalCost=staffCostPerSeat+(indirectCost.getInfraCost()/companyCount)+(indirectCost.getVariableCost()/companyCount) + reimbursementCost + assetCost;
		}else {
			Map<String, Object> userListAndCount = feignLegacyInterface.gradeWiseExpectedHours(month, Integer.parseInt(indirectCost.getYear()) );
			Map<String, Double> gradeWiseCosts = utilityService.getGradeWiseCosts(month, Integer.parseInt(indirectCost.getYear()) , accessToken, "", userListAndCount);
			Double buCost=buIndirectCost(accessToken, businessVertical, month, indirectCost.getYear(),gradeWiseCosts);
			totalCost=staffCostPerSeat+(indirectCost.getInfraCost()/companyCount)+(indirectCost.getVariableCost()/companyCount)+ reimbursementCost + assetCost + buCost;
			map.put("buCost",Math.round(buCost*100.0)/100.0);
		}
		map.put(ConstantUtility.TOTAL_COST, Math.round(totalCost*100.0)/100.0);
		double totalCommulativeCost = totalCost * companyCount;
		map.put(ConstantUtility.TOTAL_CUMULATIVE_COST, Math.round(totalCommulativeCost * 100.00) / 100.00);
		return map;
	}
	
	public List<Map<String, Object>> getIndirectCost(String accessToken,String year,String businessVertical){
		List<IndirectCost> indirectCostList = new ArrayList<>();
		List<Map<String, Object>> dataList=new ArrayList<>();
			indirectCostList = costRepository.findByYearAndIsDeleted(year, false);	
			 List<Map<String, Object>> workingDaysData = feignLegacyInterface.getpayrollWorkingDaysList( accessToken , Integer.parseInt(year));
				List<Arrear> arrears= arrearRepo.findAllByCreationYearAndIsReimbursementAndIsArrearIncludedAndIsDeleted(Integer.parseInt(year), true, false,false);	
				List<PayRegister> currentPayregister = payRegisterRepository.findAllByIsCurrent(true);
				List<PayRevisions> payrevisions = payRevisionRepository.findAllByIsDeleted(false);

			indirectCostList.forEach(indirectCost->{
					int month=getMonthNumber(indirectCost.getMonth().getMonths());
					Map<String,Object> data=feignLegacyInterface.getCompanyExpectedHours(accessToken,month,indirectCost.getYear());
					Object companyExpectedHours=data.get(ConstantUtility.DATA);
					Object workingDays = workingDaysData.get(month-1).get(ConstantUtility.WORKING_DAYS);
					Double companyCount=0.0;
					if(Double.parseDouble(companyExpectedHours.toString())!=0.0) {
						companyCount=Double.parseDouble(companyExpectedHours.toString())/(Double.parseDouble(workingDays.toString())*8);
					}
					Double assetCost= 0D;
					Map<String,Object> map=getCostDataV2(accessToken, companyCount, indirectCost, businessVertical, workingDays,
							assetCost,month,arrears,currentPayregister,payrevisions);
					dataList.add(map);
			});
		return dataList;
	}
	public Map<String, Object> getCostDataV2(String accessToken, Double companyCount,IndirectCost indirectCost,String businessVertical,
			Object workingDays,Double totalAssetCost,int month,List<Arrear> arrears,List<PayRegister>currentPayregister,List<PayRevisions> payrevisions){
		Double infraCost=indirectCost.getInfraCost();
		Double variableCost=indirectCost.getVariableCost();
		Double reimbursementCostTotal = companyMarginService.getReimbursementCostV2(accessToken, month,
				Integer.parseInt(indirectCost.getYear()),arrears);

		Double assetCost = 0D;
		Double reimbursementCost = 0D;
		if (companyCount != 0.0) {
			infraCost = (indirectCost.getInfraCost() / companyCount);
			variableCost = (indirectCost.getVariableCost() / companyCount);
			reimbursementCost = reimbursementCostTotal != null ? (reimbursementCostTotal / companyCount) : 0.00;
			assetCost = totalAssetCost / companyCount;
		}
		Map<String,Object> map=new HashMap<>();
		map.put("employees", Math.round(companyCount*100.0)/100.0);
		map.put("infraCost", Math.round(infraCost*100.0)/100.0);
		map.put(ConstantUtility.TOTAL_INFRA_COST, indirectCost.getInfraCost());
		map.put("variableCost", Math.round(variableCost*100.0)/100.0);
		map.put(ConstantUtility.TOTAL_VARIABLE_COST, indirectCost.getVariableCost());
		map.put("reimbursementCost", Math.round(reimbursementCost*100.0)/100.0);
		map.put(ConstantUtility.TOTAL_REIMBURSMENT_COST,reimbursementCostTotal);
		map.put(ConstantUtility.TOTAL_ASSET_COST, totalAssetCost);
		map.put(ConstantUtility.ASSET_COST, assetCost);
		Double staffCost=getStaffCostV2(accessToken,month,indirectCost.getYear(),workingDays,ConstantUtility.OPERATIONS_SUPPORT,currentPayregister,payrevisions);
		Double staffCostPerSeat=((Math.round(staffCost*100)/100)/companyCount);
		map.put("staffCost", Math.round(staffCostPerSeat)*100.0/100.0);
		map.put("month", indirectCost.getMonth());
		map.put("year", indirectCost.getYear());
		map.put("id", indirectCost.getId());
		Double totalCost=0.0;
		if (businessVertical == null || businessVertical.equals("")) {
			totalCost=staffCostPerSeat+(indirectCost.getInfraCost()/companyCount)+(indirectCost.getVariableCost()/companyCount) + reimbursementCost + assetCost;
		}else {
			Map<String, Double> gradeWiseCosts = utilityService.getGradeWiseCostsV2(month, Integer.parseInt(indirectCost.getYear()) , accessToken, "", "");
			Double buCost=buIndirectCostV3(accessToken, businessVertical, month, indirectCost.getYear(),workingDays,gradeWiseCosts);
			totalCost=staffCostPerSeat+(indirectCost.getInfraCost()/companyCount)+(indirectCost.getVariableCost()/companyCount)+ reimbursementCost + assetCost + buCost;
			map.put("buCost",Math.round(buCost*100.0)/100.0);
		}
		map.put(ConstantUtility.TOTAL_COST, Math.round(totalCost*100.0)/100.0);
		double totalCommulativeCost = totalCost * companyCount;
		map.put(ConstantUtility.TOTAL_CUMULATIVE_COST, Math.round(totalCommulativeCost * 100.00) / 100.00);
		return map;
	}
	
	
	
	
	public IndirectCost updateIndirectCost(Double totalInfraCost,Double totalVariableCost, Long costId, Long userId, Double totalReimbursementCost) {
		Optional<IndirectCost> data = costRepository.findById(costId);
		IndirectCost indirectCost = data.get();
		if(data.isPresent()) {
			indirectCost.setInfraCost(totalInfraCost);
			indirectCost.setVariableCost(totalVariableCost);
			indirectCost.setReimbursement(totalReimbursementCost);
			costRepository.save(indirectCost);
		}
		return indirectCost;
	}
	
	public IndirectCost deleteIndirectCost(Long costId) {
		Optional<IndirectCost> indirectCost = costRepository.findById(costId);
		if(indirectCost.isPresent()) {
			indirectCost.get().setIsDeleted(true);
			costRepository.save(indirectCost.get());
			MarginBasis marginBasis = marginBasisRepository.findByMonthAndYear(Month.valueOf(indirectCost.get().getMonth().toString().toUpperCase()).getValue(),Integer.parseInt(indirectCost.get().getYear()));
			if(marginBasis!=null){
				marginBasis.setIsLTM(false);
				marginBasisRepository.save(marginBasis);
				
			}
			return indirectCost.get();
		}
		return null;
	}
	
	public Map<String, List<?>> graphData(String year){
		Map<String, List<?>> graphData = new HashMap<>();
		List<IndirectCost> filterByYear = costRepository.findByYearAndIsDeleted(year, false);
		List<Months> months = Arrays.asList(Months.values());
		List<Double> cost = new ArrayList<>();
		
		if(!filterByYear.isEmpty()) {
			for(int i=0; i<months.size(); i++) {
				Optional<IndirectCost> data = costRepository.findByMonthAndYearAndIsDeleted(months.get(i), year, false);
				if(data.isPresent())
					cost.add(0d);
				else
					cost.add(0d);
			}
				
			graphData.put("months", months);
			graphData.put(ConstantUtility.TOTAL_COST, cost);
			return graphData;
		}
		return null;
	}

	public double getHourlySalary(Object workingDays, PayRegister payregister) {
		double totalAnnualCtc = payregister.getTotalAnnualCtc();
		double dayWiseSalary = (totalAnnualCtc/12) / Double.parseDouble(workingDays.toString());
		double hourlySalary = dayWiseSalary / 8;
		return hourlySalary;
	}
	
	public int getMonthNumber(String monthName) {
	    return Month.valueOf(monthName.toUpperCase()).getValue();
	}
	
	@SuppressWarnings("unchecked")
	public Double getStaffCost(String accessToken, int month, String year, Object workingDays, String businessVertical) {
		Double directCost = 0.0;
		Map<String, Object> staffHours = feignLegacyInterface.getBuWiseProjects(accessToken, businessVertical, month, Integer.parseInt(year));
		List<Object> dataList = (List<Object>) staffHours.get(ConstantUtility.DATA);
		if (!dataList.isEmpty()) {
			List<Object> dataProjects = (List<Object>) ((Map<String, Object>) dataList.get(0)).get(ConstantUtility.PROJECTS);
			if (!dataProjects.isEmpty()) {
				for (int i = 0; i < dataProjects.size(); i++) {
					List<Object> teamData = (List<Object>) ((Map<String, Object>) dataProjects.get(i)).get(ConstantUtility.TEAM_DATA);
					if (!teamData.isEmpty()) {
						for (int j = 0; j < teamData.size(); j++) {
							Map<String, Object> userData = (Map<String, Object>) teamData.get(j);
							double userSalary = getPay(userData, month, Integer.parseInt(year), workingDays,new HashMap<>(),new HashMap<>());
							directCost = directCost + userSalary;
						}
					}
				}
			}
		}
		return directCost;
	}
	@SuppressWarnings("unchecked")
	public Double getStaffCostV2(String accessToken, int month, String year, Object workingDays, String businessVertical,List<PayRegister>currentPayregister,List<PayRevisions> payrevisions) {
		Double directCost = 0.0;
		Map<String, Object> staffHours = feignLegacyInterface.getBuWiseProjects(accessToken, businessVertical, month, Integer.parseInt(year));
		List<Object> dataList = (List<Object>) staffHours.get(ConstantUtility.DATA);
		log.info("::::::::data:::::"+staffHours.get(ConstantUtility.DATA));
		if (!dataList.isEmpty()) {
			List<Object> dataProjects = (List<Object>) ((Map<String, Object>) dataList.get(0)).get(ConstantUtility.PROJECTS);
			if (!dataProjects.isEmpty()) {
				for (int i = 0; i < dataProjects.size(); i++) {
					List<Object> teamData = (List<Object>) ((Map<String, Object>) dataProjects.get(i)).get(ConstantUtility.TEAM_DATA);
					if (!teamData.isEmpty()) {
						for (int j = 0; j < teamData.size(); j++) {
							Map<String, Object> userData = (Map<String, Object>) teamData.get(j);
							double userSalary = getPayV2(userData, month, Integer.parseInt(year), workingDays,new HashMap<>(),new HashMap<>(),currentPayregister,payrevisions);
							directCost = directCost + userSalary;
						}
					}
				}
			}
		}
		return directCost;
	}
	
	public double getPay(Map<String, Object> userData, int month, int year, Object workingDays,Map<String,Object> voluntaryPayData,Map<String,Object> payDaysData) {
		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDate lastDay = yearMonth.atEndOfMonth();
		LocalDate firstDay=yearMonth.atDay(1);
		Object userId = userData.get("userId");
		double userSalary = 0.0;
		Double voluntaryPayAmount=0D;
		if(voluntaryPayData.containsKey(userId.toString())) {
			voluntaryPayAmount=(Double.parseDouble(voluntaryPayData.get(userId.toString()).toString())/(Double.parseDouble(payDaysData.get(userId.toString()).toString()) *8));
		}
		PayRegister currentPayregister = payRegisterRepository.findAllByUserIdAndIsCurrent(Long.parseLong(userId.toString()), true);
		if (currentPayregister != null) {
			if (currentPayregister.getEffectiveDate().toLocalDate().isBefore(lastDay.plusDays(1)) || currentPayregister.getEffectiveDate().toLocalDate().isEqual(lastDay)) {
				double hourlySalary = getHourlySalary(workingDays, currentPayregister);
				double hourlyCompensation = voluntaryPayAmount;
				userSalary = userDirectCost(hourlySalary, userData);
			} else {
				List<PayRevisions> payrevisions = payRevisionRepository.findAllByUserIdAndIsDeleted(Long.parseLong(userId.toString()),false);
				
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
						PayRegister payRegister = payRegisterRepository.findAllById(dataPayrev.getPayRegister().getId());
						double hourlySalary = getHourlySalary(workingDays, payRegister);
						double hourlyCompensation = voluntaryPayAmount;
						userSalary = userDirectCost(hourlySalary, userData);
					} else {
						PayRevisions dataPayrev = finalpayrevision.get(0);
						PayRegister payRegister = payRegisterRepository.findAllById(dataPayrev.getPayRegister().getId());
						double hourlySalary = getHourlySalary(workingDays, payRegister);
						double hourlyCompensation = voluntaryPayAmount;
						userSalary = userDirectCost(hourlySalary, userData);
					}
				} else {
					double hourlySalary = 0.0;
					double hourlyCompensation = voluntaryPayAmount;
					userSalary = userDirectCost(hourlySalary, userData);
				}
			}
		} else {
			double hourlySalary = 0.0;
			double hourlyCompensation = voluntaryPayAmount;
			userSalary = userDirectCost(hourlySalary, userData);
		}
		return userSalary;
	}
	
	public double getPayV2(Map<String, Object> userData, int month, int year, Object workingDays,Map<String,Object> voluntaryPayData,Map<String,Object> payDaysData
			,List<PayRegister>payregisterList,List<PayRevisions> payrevisionsList) {
		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDate lastDay = yearMonth.atEndOfMonth();
		LocalDate firstDay=yearMonth.atDay(1);
		Object userId = userData.get("userId");
		double userSalary = 0.0;
		Double voluntaryPayAmount=0D;
		if(voluntaryPayData.containsKey(userId.toString()))
		{
			voluntaryPayAmount=(Double.parseDouble(voluntaryPayData.get(userId.toString()).toString())/(Double.parseDouble(payDaysData.get(userId.toString()).toString()) *8));
		} 
		PayRegister currentPayregister=payregisterList.stream().filter(pay->String.valueOf(pay.getUserId()).equals(userId.toString()) && pay.isCurrent()==true).findFirst().orElse(null);
		if (currentPayregister != null) {
			if (currentPayregister.getEffectiveDate().toLocalDate().isBefore(lastDay.plusDays(1)) || currentPayregister.getEffectiveDate().toLocalDate().isEqual(lastDay)) {
				double hourlyCompensation = voluntaryPayAmount;
				double hourlySalary = getHourlySalary(workingDays, currentPayregister);
				userSalary = userDirectCost(hourlySalary, userData);
			} else {
				List<PayRevisions>  payrevisions=payrevisionsList.stream().filter(pay->String.valueOf(pay.getUserId()).equals(userId.toString()) && pay.isDeleted()==false).collect(Collectors.toList());

				
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
						PayRegister payRegister = payRegisterRepository.findAllById(dataPayrev.getPayRegister().getId());

						double hourlyCompensation = voluntaryPayAmount;
						double hourlySalary = getHourlySalary(workingDays, payRegister);
						userSalary = userDirectCost(hourlySalary, userData);
					} else {
						PayRevisions dataPayrev = finalpayrevision.get(0);
						PayRegister payRegister = payRegisterRepository.findAllById(dataPayrev.getPayRegister().getId());

						double hourlySalary = getHourlySalary(workingDays, payRegister)+voluntaryPayAmount;
						double hourlyCompensation = voluntaryPayAmount;
						userSalary = userDirectCost(hourlySalary, userData);
					}
				} else {
					double hourlySalary = 0.0;
					double hourlyCompensation = voluntaryPayAmount;
					userSalary = userDirectCost(hourlySalary, userData);
				}
			}
		} else {
			double hourlySalary = 0.0;
			double hourlyCompensation = voluntaryPayAmount;
			userSalary = userDirectCost(hourlySalary, userData);
		}
		return userSalary;
	}
	
	public double userDirectCost(double hourlySalary, Map<String, Object> userData) {
		Double expectedHours =  Double.parseDouble(userData.get(ConstantUtility.EXPECTED_HOURS).toString());
		String mins=expectedHours.toString().split("\\.")[1];
		if (Double.parseDouble(mins) != 0D) {
			expectedHours = Double.parseDouble(userData.get(ConstantUtility.EXPECTED_HOURS).toString().split("\\.")[0]);
			expectedHours = expectedHours + (Double.parseDouble(mins) / 60);
		}
		double employeeSalary = (hourlySalary * expectedHours);
		return employeeSalary;
	}
	
	public Double buIndirectCost (String accessToken,String businessVertical,int month,String year,Map<String,Double> gradeWiseCosts) {
		Double buIndirectCost=0.0;
		List<Long> projectIds=new ArrayList<>();
		Map<String, Object> workingDaysData = (Map<String, Object>) feignLegacyInterface.getpayrollWorkingDays(accessToken, month , Integer.parseInt(year)).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		Map<String, Object> buIds=(Map<String, Object>)feignLegacyInterface.getBuProjectIds(accessToken, businessVertical, month, year).get("data");
		Double buExpectedHours=0.0;
		if(buIds.containsKey(ConstantUtility.BU_HOURS)) {
			buExpectedHours=Double.parseDouble(buIds.get(ConstantUtility.BU_HOURS).toString());
		}
		Integer nonBillableHours=0;
		Integer nonBillableMins=0;
		if(buIds.containsKey(ConstantUtility.PROJECT_IDS_LIST)) {
		List<Object> buProjects=loginUtilityService.objectToListConverter(buIds.get(ConstantUtility.PROJECT_IDS_LIST));
		 String monthName=new DateFormatSymbols().getMonths()[month-1].toString();
		buProjects.forEach(projectId->{
			List<ProjectInvoice> projectInvoice = new ArrayList<>();
			if(businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT)){
				projectInvoice=projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndProjectIdAndIsInternalAndInvoiceStatusNot(monthName, year, false, Long.parseLong(projectId.toString()),false, 6L);

				
			}
			else
				projectInvoice=projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndProjectIdAndInvoiceStatusNot(monthName, year, false, Long.parseLong(projectId.toString()), 6L);
			if(projectInvoice.isEmpty()) {
				projectIds.add(Long.parseLong(projectId.toString()));
			}
		});
		List<Map<String,Object>> teamList=(List<Map<String, Object>>) buIds.get(ConstantUtility.TEAM_LIST);
		 
		for(Long id:projectIds) {

			List<Map<String,Object>> projectFiltered= teamList.stream().filter(data->data.get(ConstantUtility.PROJECT_ID).toString().equals(id.toString())).collect(Collectors.toList());
			List<Object> team= new ArrayList<>();
			Map<String,Object> projectExpectedHours= null;
			if(!projectFiltered.isEmpty()) {
				projectExpectedHours=projectFiltered.get(0);
				team=(List<Object>) projectExpectedHours.get("team");
			}
			for(int i=0;i<team.size();i++) {
				Map<String,Object> userData=loginUtilityService.objectToMapConverter(team.get(i));
				String grade =userData.get(ConstantUtility.GRADE)!=null?userData.get(ConstantUtility.GRADE).toString():"NA";
				String expectedHours=userData.get(ConstantUtility.EXPECTED_HOURS).toString();
				String mins=expectedHours.split("\\.")[1];
				String hours=expectedHours.split("\\.")[0];
				nonBillableHours=nonBillableHours+Integer.parseInt(hours);
				nonBillableMins=nonBillableMins+Integer.parseInt(mins);
				if(nonBillableMins>=60) {
					nonBillableHours=nonBillableHours+1;
					nonBillableMins=nonBillableMins-60;
				}
				Double userSalary=getPay(userData,month,Integer.parseInt(year),workingDays,new HashMap<>(),new HashMap<>());
				Double indirectCostNonBillable=0D;
				if(gradeWiseCosts!=null && grade!=null && gradeWiseCosts.containsKey(grade) && gradeWiseCosts.get(grade)!=null) {
					double hourlyIndirectCost = gradeWiseCosts.get(grade) / ((Double.parseDouble(workingDays.toString())) * 8);
					Double collectiveExpectedHours=Double.parseDouble(expectedHours);
					if (Double.parseDouble(mins) != 0D) {
						collectiveExpectedHours = Double.parseDouble(hours);
						collectiveExpectedHours = collectiveExpectedHours + (Double.parseDouble(mins) / 60);
					}
					indirectCostNonBillable=hourlyIndirectCost * collectiveExpectedHours ;
				}
				buIndirectCost=buIndirectCost+userSalary+indirectCostNonBillable;
				
			}
		}
		Double billableHours=buExpectedHours-Double.parseDouble(nonBillableHours+"."+nonBillableMins);
		Double billableCount=0.0;
		if(billableHours!=0.0) {
			billableCount=billableHours/(Double.parseDouble(workingDays.toString())*8);
			buIndirectCost=Math.round((buIndirectCost/billableCount)*100.0)/100.0;
		}
		else {
				if(businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT))
					buIndirectCost=0.0;
				else {
					billableCount=Double.parseDouble(nonBillableHours+"."+nonBillableMins)/(Double.parseDouble(workingDays.toString())*8);
					buIndirectCost=Math.round((buIndirectCost/billableCount)*100.0)/100.0;
				}
			}
		}
		return buIndirectCost;
	}
	
	//calculate BU Development Cost per seat / buIndirectCost
	public Double buIndirectCostV3(String accessToken,String businessVertical,int month,String year, Object workingDays, Map<String,Double> gradeWiseCosts) {
		Double buIndirectCost=0.0;
		Double buExpectedHours=0.0;
		Integer nonBillableHours=0;
		Integer nonBillableMins=0;
		String monthName=new DateFormatSymbols().getMonths()[month-1].toString();
		Map<String, Object> projectIdsAndTeamInfoBuWise=(Map<String, Object>)feignLegacyInterface.getBuProjectIds(accessToken, businessVertical, month, year).get("data");
		if(projectIdsAndTeamInfoBuWise.containsKey(ConstantUtility.BU_HOURS)) {
			buExpectedHours = Double.parseDouble(projectIdsAndTeamInfoBuWise.get(ConstantUtility.BU_HOURS).toString());
		}
		if(projectIdsAndTeamInfoBuWise.containsKey(ConstantUtility.PROJECT_IDS_LIST)) {
			List<ProjectInvoice> filterProjectInvoices = new ArrayList<>();
			List<Object> objectList=(List<Object>) projectIdsAndTeamInfoBuWise.get(ConstantUtility.PROJECT_IDS_LIST);
			List<Long> buProjectsIds = objectList.stream().map(l -> Long.parseLong(l.toString())).collect(Collectors.toList());
			List<Map<String,Object>> teamList=(List<Map<String, Object>>) projectIdsAndTeamInfoBuWise.get(ConstantUtility.TEAM_LIST);
			List<ProjectInvoice> projectInvoices = projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndProjectIdIn(monthName, year, false, buProjectsIds);
			if(businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT)) {
				filterProjectInvoices = projectInvoices.stream().filter(inv -> (!inv.getIsInternal() && inv.getInvoiceStatus()!=6L)).collect(Collectors.toList());
			}
			else {
				filterProjectInvoices = projectInvoices.stream().filter(inv -> (inv.getInvoiceStatus()!=6L)).collect(Collectors.toList());
			}
			List<Long> invoiceProjectIds =  filterProjectInvoices.stream().distinct().map(inv->inv.getProjectId()).collect(Collectors.toList());
			List<Long> nonBillablesProjectIds = buProjectsIds.stream().filter(id -> !invoiceProjectIds.contains(Long.parseLong(id.toString()))).collect(Collectors.toList());
			for(Long id : nonBillablesProjectIds) {
				List<Map<String,Object>> projectFiltered= teamList.stream().filter(data->data.get(ConstantUtility.PROJECT_ID).toString().equals(id.toString())).collect(Collectors.toList());
				if(!projectFiltered.isEmpty()) {
					 List<Object> teamMap =  (List<Object>) projectFiltered.get(0).get("team");
					 for(int i=0;i<teamMap.size();i++) {
						 Map<String,Object> userData= loginUtilityService.objectToMapConverter(teamMap.get(i));;
						 String grade =userData.get(ConstantUtility.GRADE)!=null?userData.get(ConstantUtility.GRADE).toString():"NA";
						 String expectedHours=userData.get(ConstantUtility.EXPECTED_HOURS).toString();
						 String mins=expectedHours.split("\\.")[1];
						 String hours=expectedHours.split("\\.")[0];
						 nonBillableHours = nonBillableHours + Integer.parseInt(hours);
						 nonBillableMins = nonBillableMins + Integer.parseInt(mins);
						 if(nonBillableMins >= 60) {
								nonBillableHours = nonBillableHours+1;
								nonBillableMins = nonBillableMins-60;
							}
						 Double indirectCostNonBillable=0D;
						 Double userSalary=getPay(userData,month,Integer.parseInt(year),workingDays,new HashMap<>(),new HashMap<>());
						 if(gradeWiseCosts!=null && grade!=null && gradeWiseCosts.containsKey(grade) && gradeWiseCosts.get(grade)!=null) {
								double hourlyIndirectCost = gradeWiseCosts.get(grade) / ((Double.parseDouble(workingDays.toString())) * 8);
								Double collectiveExpectedHours = Double.parseDouble(expectedHours);
								if (Double.parseDouble(mins) != 0D) {
									collectiveExpectedHours = Double.parseDouble(hours);
									collectiveExpectedHours = collectiveExpectedHours + (Double.parseDouble(mins) / 60);
								}
								indirectCostNonBillable=hourlyIndirectCost * collectiveExpectedHours ;
							}
						 buIndirectCost=buIndirectCost+userSalary+indirectCostNonBillable;	
					 }	 
				}
			}
			Double billableHours=buExpectedHours-Double.parseDouble(nonBillableHours+"."+nonBillableMins);
			Double billableCount=0.0;
			if (billableHours != 0.0) {
				billableCount = billableHours / (Double.parseDouble(workingDays.toString()) * 8);
				buIndirectCost = Math.round((buIndirectCost / billableCount) * 100.0) / 100.0;
			} else {
				if (businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT))
					buIndirectCost = 0.0;
				else {
					billableCount = Double.parseDouble(nonBillableHours + "." + nonBillableMins)
							/ (Double.parseDouble(workingDays.toString()) * 8);
					buIndirectCost = Math.round((buIndirectCost / billableCount) * 100.0) / 100.0;
				}
			}
		}
		return buIndirectCost;
	}
	
	public Double buIndirectCostV2(String accessToken,String businessVertical,int month,String year,Object workingDays, Map<String,Double> gradeWiseCosts) {
		Double buIndirectCost=0.0;
		List<Long> projectIds=new ArrayList<>();
		Map<String, Object> buIds=(Map<String, Object>)feignLegacyInterface.getBuProjectIds(accessToken, businessVertical, month, year).get("data");
		Double buExpectedHours=0.0;
		if(buIds.containsKey(ConstantUtility.BU_HOURS)) {
			buExpectedHours=Double.parseDouble(buIds.get(ConstantUtility.BU_HOURS).toString());
		}
		Integer nonBillableHours=0;
		Integer nonBillableMins=0;
		if(buIds.containsKey(ConstantUtility.PROJECT_IDS_LIST)) {
		List<Object> buProjects=loginUtilityService.objectToListConverter(buIds.get(ConstantUtility.PROJECT_IDS_LIST));
		buProjects.forEach(projectId->{
			String monthName=new DateFormatSymbols().getMonths()[month-1].toString();
			List<ProjectInvoice> projectInvoice=projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndProjectIdAndIsInternalAndInvoiceStatusNot(monthName, year, false, Long.parseLong(projectId.toString()), false, 6L);
			if(projectInvoice.isEmpty()) {
				projectIds.add(Long.parseLong(projectId.toString()));
			}
		});
		
		List<Map<String,Object>> teamList=(List<Map<String, Object>>) buIds.get(ConstantUtility.TEAM_LIST);

		for(Long id:projectIds) {
			List<Map<String,Object>> projectFiltered= teamList.stream().filter(data->data.get(ConstantUtility.PROJECT_ID).toString().equals(id.toString())).collect(Collectors.toList());
			List<Object> team= new ArrayList<>();
			Map<String,Object> projectExpectedHours= null;
			if(!projectFiltered.isEmpty()) {
				projectExpectedHours=projectFiltered.get(0);
				team=(List<Object>) projectExpectedHours.get("team");
			}
			for(int i=0;i<team.size();i++) {
				Map<String,Object> userData=loginUtilityService.objectToMapConverter(team.get(i));
				String grade ="NA";
				if(userData.containsKey(ConstantUtility.GRADE) && userData.get(ConstantUtility.GRADE)!=null)
					grade = userData.get(ConstantUtility.GRADE).toString();

				Double userSalary=getPay(userData,month,Integer.parseInt(year),workingDays,new HashMap<>(),new HashMap<>());
				Double indirectCostNonBillable=0D;
				if(gradeWiseCosts!=null && grade!=null && gradeWiseCosts.containsKey(grade) && gradeWiseCosts.get(grade)!=null);
					indirectCostNonBillable=Double.parseDouble(gradeWiseCosts.get(grade).toString()) ;
				buIndirectCost=buIndirectCost+userSalary+indirectCostNonBillable;
				String expectedHours=userData.get(ConstantUtility.EXPECTED_HOURS).toString();
				String mins=expectedHours.split("\\.")[1];
				String hours=expectedHours.split("\\.")[0];
				nonBillableHours=nonBillableHours+Integer.parseInt(hours);
				nonBillableMins=nonBillableMins+Integer.parseInt(mins);
				if(nonBillableMins>=60) {
					nonBillableHours=nonBillableHours+1;
					nonBillableMins=nonBillableMins-60;
				}
			}
		}
		Double billableHours=buExpectedHours-Double.parseDouble(nonBillableHours+"."+nonBillableMins);
		Double billableCount=0.0;
		if(billableHours!=0.0) {
			billableCount=billableHours/(Double.parseDouble(workingDays.toString())*8);
			buIndirectCost=Math.round((buIndirectCost/billableCount)*100.0)/100.0;
		}
		else {
				if(businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT))
					buIndirectCost=0.0;
				else {
					billableCount=Double.parseDouble(nonBillableHours+"."+nonBillableMins)/(Double.parseDouble(workingDays.toString())*8);
					buIndirectCost=Math.round((buIndirectCost/billableCount)*100.0)/100.0;
				}
			}
		}
		return buIndirectCost;
	}
	
	public Map<String,Object> getVerticalProjects(String accessToken,String businessVertical,String month,String year){
		Map<String,Object> staffData=new HashMap<>();
		int monthNum=getMonthNumber(month);
		Map<String, Object> workingDaysData =(Map<String, Object>) feignLegacyInterface.getpayrollWorkingDays(accessToken,monthNum , Integer.parseInt(year)).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		List<Object> dataList=(List<Object>) feignLegacyInterface.getBuWiseProjects(accessToken,businessVertical,monthNum,Integer.parseInt(year)).get(ConstantUtility.DATA);
		List<Object>  staffHours = loginUtilityService.objectToListConverter(dataList);
		List<Object> staffCostData=new ArrayList<>();
		Double totalCost=0.0;
		if(!staffHours.isEmpty()) {
		Map<String,Object> projects=loginUtilityService.objectToMapConverter((staffHours.get(0)));
		List<Object> projectList=loginUtilityService.objectToListConverter(projects.get(ConstantUtility.PROJECTS));
		for(int i=0;i<projectList.size();i++) {
			Map<String,Object> projectData=loginUtilityService.objectToMapConverter(projectList.get(i));
			Map<String,Object> map=new HashMap<>();
			map.put(ConstantUtility.PROJECT_NAME, projectData.get(ConstantUtility.PROJECT_NAME));
			map.put(ConstantUtility.PROJECT_ID, Long.parseLong(projectData.get(ConstantUtility.PROJECT_ID).toString()));
			List<Object> teamData=loginUtilityService.objectToListConverter(projectData.get(ConstantUtility.TEAM_DATA));
			Double projectDirectCost=0.0;
			Integer billableHours=0;
			Integer billableMins=0;
			for(int j=0;j<teamData.size();j++) {
				Map<String,Object> userData=loginUtilityService.objectToMapConverter(teamData.get(j));
				double userSalary=getPay(userData,monthNum,Integer.parseInt(year),workingDays,new HashMap<>(),new HashMap<>());
				projectDirectCost=projectDirectCost+userSalary;
				totalCost=totalCost+userSalary;
				String expectedHours=userData.get(ConstantUtility.EXPECTED_HOURS).toString();
				
				String mins=expectedHours.split("\\.")[1];
				String hours=expectedHours.split("\\.")[0];
				billableHours=billableHours+Integer.parseInt(hours);
				billableMins=billableMins+Integer.parseInt(mins);
				if(billableMins>=60) {
					billableHours=billableHours+1;
					billableMins=billableMins-60;
				}
			}
			Double nonBillableCount=Double.parseDouble(billableHours+"."+billableMins)/(Double.parseDouble(workingDays.toString())*8);
			map.put(ConstantUtility.EXPECTED_HOURS, Double.parseDouble(billableHours+"."+billableMins));
			map.put(ConstantUtility.PROJECT_EMPLOYEE_COUNT,Math.round(nonBillableCount*100.0 )/100.0);
			map.put("directCost", Math.round(projectDirectCost*100.0)/100.0);
			staffCostData.add(map);
			}
		}
		staffData.put("projectsList", staffCostData);
		staffData.put("totalDirectCost", Math.round(totalCost*100.0)/100.0);
		Map<String, Object> data = feignLegacyInterface.getCompanyExpectedHours(accessToken, monthNum, year);
		Object companyExpectedHours = data.get(ConstantUtility.DATA);
		Double companyCount=Double.parseDouble(companyExpectedHours.toString())/(Double.parseDouble(workingDays.toString())*8);
		staffData.put(ConstantUtility.TOTAL_EMPLOYEES, Math.round(companyCount*100.0)/100.0);
		return staffData; 
	}

	public List<Object> getInfraCost(String accessToken, String month, String year) {
		List<Object> dataList=new ArrayList<>();
		Map<String, Object> map = new HashMap<>();
		Months monthObj=Months.valueOf(month);
		int monthNum=getMonthNumber(month);
		IndirectCost indirectCost = costRepository.findByYearAndIsDeletedAndMonth(year, false,monthObj);
		Map<String, Object> data = feignLegacyInterface.getCompanyExpectedHours( accessToken,monthNum, indirectCost.getYear());
		Object companyExpectedHours = data.get(ConstantUtility.DATA);
		Map<String, Object> workingDaysData = (Map<String, Object>) feignLegacyInterface.getpayrollWorkingDays(accessToken, monthNum, Integer.parseInt(year)).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		Double companyCount=0.0;
		Double infraCost=indirectCost.getInfraCost();
		if(Double.parseDouble(companyExpectedHours.toString())!=0.0) {
			companyCount=Double.parseDouble(companyExpectedHours.toString())/(Double.parseDouble(workingDays.toString())*8);
			infraCost=(indirectCost.getInfraCost()/companyCount);
		}
		if (indirectCost!=null) {
			map.put(ConstantUtility.TOTAL_INFRA_COST, Math.round(indirectCost.getInfraCost()*100.0)/100.0);
			map.put(ConstantUtility.TOTAL_EXPECTED_HOURS,  Math.round(Double.parseDouble(companyExpectedHours.toString())*100.0)/100.0);
			map.put(ConstantUtility.TOTAL_EMPLOYEES,  Math.round(companyCount*100.0)/100.0);
			map.put(ConstantUtility.PER_PERSON_HOURS,(Double.parseDouble(workingDays.toString()))*8);
			map.put("perSeatInfraCost", Math.round(infraCost*100.0)/100.0);
			dataList.add(map);
		}
		return dataList;
	}
	
	public List<Object> getVariableCost(String accessToken, String month, String year) {
		List<Object> dataList=new ArrayList<>();
		Map<String, Object> map = new HashMap<>();
		Months monthObj=Months.valueOf(month);
		int monthNum=getMonthNumber(month);
		IndirectCost indirectCost = costRepository.findByYearAndIsDeletedAndMonth(year, false,monthObj);
		Map<String, Object> data = feignLegacyInterface.getCompanyExpectedHours(accessToken, monthNum, indirectCost.getYear());
		Object companyExpectedHours = data.get(ConstantUtility.DATA);
		Map<String, Object> workingDaysData =(Map<String, Object>) feignLegacyInterface.getpayrollWorkingDays(accessToken,monthNum, Integer.parseInt(year)).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		Double companyCount=0.0;
		Double variableCost=indirectCost.getVariableCost();
		if(Double.parseDouble(companyExpectedHours.toString())!=0.0) {
			companyCount=Double.parseDouble(companyExpectedHours.toString())/(Double.parseDouble(workingDays.toString())*8);
			variableCost=(indirectCost.getVariableCost()/companyCount);
		}
		if (indirectCost!=null) {
			map.put(ConstantUtility.TOTAL_VARIABLE_COST, Math.round(indirectCost.getVariableCost()*100.0)/100.0);
			map.put(ConstantUtility.TOTAL_EXPECTED_HOURS,  Math.round(Double.parseDouble(companyExpectedHours.toString())*100.0)/100.0);
			map.put(ConstantUtility.TOTAL_EMPLOYEES,  Math.round(companyCount*100.0)/100.0);
			map.put(ConstantUtility.PER_PERSON_HOURS,(Double.parseDouble(workingDays.toString()))*8);
			map.put("perSeatVariableCost", Math.round(variableCost*100.0)/100.0);
			dataList.add(map);
		}
		return dataList;
	}
	
	public List<Object> getReimbursement(String accessToken, String month, String year) {
		List<Object> dataList=new ArrayList<>();
		Map<String, Object> map = new HashMap<>();
		Months monthObj=Months.valueOf(month);
		int monthNum=getMonthNumber(month);
		IndirectCost indirectCost = costRepository.findByYearAndIsDeletedAndMonth(year, false,monthObj);
		Map<String, Object> data = feignLegacyInterface.getCompanyExpectedHours( accessToken,monthNum, indirectCost.getYear());
		Object companyExpectedHours = data.get(ConstantUtility.DATA);
		Map<String, Object> workingDaysData = (Map<String, Object>) feignLegacyInterface.getpayrollWorkingDays( accessToken,monthNum, Integer.parseInt(year)).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		Double companyCount=0.0;
		
		Double totalReimbursement=companyMarginService.getReimbursementCost(accessToken, monthNum, Integer.parseInt(indirectCost.getYear()));

		Double reimbursement=0.0;
		if(Double.parseDouble(companyExpectedHours.toString())!=0.0) {
			companyCount=Double.parseDouble(companyExpectedHours.toString())/(Double.parseDouble(workingDays.toString())*8);
			reimbursement=totalReimbursement!=null?(totalReimbursement/companyCount):0.00;
		}
		if (indirectCost!=null) {
			map.put(ConstantUtility.TOTAL_REIMBURSMENT_COST, Math.round(totalReimbursement*100.0)/100.0);
			map.put(ConstantUtility.TOTAL_EXPECTED_HOURS,  Math.round(Double.parseDouble(companyExpectedHours.toString())*100.0)/100.0);
			map.put(ConstantUtility.TOTAL_EMPLOYEES,  Math.round(companyCount*100.0)/100.0);
			map.put(ConstantUtility.PER_PERSON_HOURS,(Double.parseDouble(workingDays.toString()))*8);
			map.put("perSeatReimbursement", Math.round(reimbursement*100.0)/100.0);
			dataList.add(map);
		}
		return dataList;
	}
	
	public Map<String,Object> getVerticalCost(String accessToken, String month,String year,String businessVertical){
		Map<String, Object> map = new HashMap<>();
		Double buIndirectCost=0.0;
		Double nonBillableCostSum = 0D;
		int monthNum=getMonthNumber(month);
		List<Long> projectIds=new ArrayList<>();
		Map<String, Object> workingDaysData =(Map<String, Object>) feignLegacyInterface.getpayrollWorkingDays(accessToken, monthNum, Integer.parseInt(year)).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		map.put("businessVertical", businessVertical);
		Map<String, Object> projectData=(Map<String, Object>) feignLegacyInterface.getBuProjectIds(accessToken, businessVertical, monthNum, year).get("data");

		if(projectData.containsKey(ConstantUtility.PROJECT_IDS_LIST)) {
		List<Object> buProjects=loginUtilityService.objectToListConverter(projectData.get(ConstantUtility.PROJECT_IDS_LIST));
		Map<String, Object> userListAndCount = feignLegacyInterface.gradeWiseExpectedHours(monthNum, Integer.parseInt(year) );
		
		Map<String, Double> gradeWiseCosts = utilityService.getGradeWiseCosts(monthNum, Integer.parseInt(year) , accessToken, "", userListAndCount);
		
		Map<String, Object> allCompensation = projectMarginservice.getAllCompensation(monthNum, Integer.parseInt(year));
		Map<String,Object> voluntaryPayData = (Map<String, Object>) allCompensation.get("voluntaryPayResponse");
		Map<String,Object> payDaysData =  (Map<String, Object>) allCompensation.get("PayDaysResponse");

			String buHours = projectData.get(ConstantUtility.BU_HOURS).toString();

		Double buExpectedHours=Double.parseDouble(buHours);
		Double buSize=buExpectedHours/(Double.parseDouble(workingDays.toString())*8);
		map.put("totaBuSize", Math.round(buSize*100.0)/100.0);
		Integer nonBillableHours=0;
		Integer nonBillableMins=0;
		buProjects.forEach(projectId->{
			String monthName=new DateFormatSymbols().getMonths()[monthNum-1].toString();
			List<ProjectInvoice> projectInvoice	= new ArrayList<>();		
			if(businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT)){
				projectInvoice=projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndProjectIdAndIsInternalAndInvoiceStatusNot(monthName, year, false, Long.parseLong(projectId.toString()),false, 6L);
			}
			else
				projectInvoice=projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndProjectIdAndInvoiceStatusNot(monthName, year, false, Long.parseLong(projectId.toString()), 6L);
			if(projectInvoice.isEmpty()) {
				projectIds.add(Long.parseLong(projectId.toString()));
			}
		});
		List<Map<String,Object>> teamList=(List<Map<String, Object>>) projectData.get(ConstantUtility.TEAM_LIST);

		for(Long id:projectIds) {
			List<Map<String,Object>> projectFiltered= teamList.stream().filter(data->data.get(ConstantUtility.PROJECT_ID).toString().equals(id.toString())).collect(Collectors.toList());
			List<Object> team= new ArrayList<>();
			Map<String,Object> projectExpectedHours= null;
			if(!projectFiltered.isEmpty()) {
				projectExpectedHours=projectFiltered.get(0);
				team=(List<Object>) projectExpectedHours.get("team");
			}
			for(int i=0;i<team.size();i++) {
				Map<String,Object> userData=loginUtilityService.objectToMapConverter(team.get(i));
				Double userSalary = 0D;
				if(gradeWiseCosts != null)
					userSalary=getPay(userData,monthNum,Integer.parseInt(year),workingDays,voluntaryPayData,payDaysData);
				Double indirectCostNonBillable=0D;
				String grade ="NA";
				if(userData.containsKey(ConstantUtility.GRADE) && userData.get(ConstantUtility.GRADE)!=null)
					grade = userData.get(ConstantUtility.GRADE).toString();

				String expectedHours=userData.get(ConstantUtility.EXPECTED_HOURS).toString();
				String mins=expectedHours.split("\\.")[1];
				String hours=expectedHours.split("\\.")[0];
				nonBillableHours=nonBillableHours+Integer.parseInt(hours);
				nonBillableMins=nonBillableMins+Integer.parseInt(mins);
				if(nonBillableMins>=60) {
					nonBillableHours=nonBillableHours+1;
					nonBillableMins=nonBillableMins-60;
				}
				if(gradeWiseCosts!=null && grade!=null && gradeWiseCosts.containsKey(grade) && gradeWiseCosts.get(grade)!=null) {
					double hourlyIndirectCost = gradeWiseCosts.get(grade) / ((Double.parseDouble(workingDays.toString())) * 8);
					Double collectiveExpectedHours=Double.parseDouble(expectedHours);
					if (Double.parseDouble(mins) != 0D) {
						collectiveExpectedHours = Double.parseDouble(hours);
						collectiveExpectedHours = collectiveExpectedHours + (Double.parseDouble(mins) / 60);
					}
					indirectCostNonBillable=hourlyIndirectCost * collectiveExpectedHours ;
				}
				
				buIndirectCost=buIndirectCost+userSalary+indirectCostNonBillable;
				nonBillableCostSum = nonBillableCostSum + indirectCostNonBillable;
				
			}
		}
		map.put("buIndirectTotal", Math.round(buIndirectCost*100.0)/100.0);
		Double billableHours=buExpectedHours-Double.parseDouble(nonBillableHours+"."+nonBillableMins);
		Double billableCount=0.0;
		if(billableHours!=0.0) {
			billableCount=billableHours/(Double.parseDouble(workingDays.toString())*8);
			buIndirectCost=Math.round((buIndirectCost/billableCount)*100.0)/100.0;
		}
		else {
			if(businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT))
				buIndirectCost=0.0;
			else {
				billableCount=Double.parseDouble(nonBillableHours+"."+nonBillableMins)/(Double.parseDouble(workingDays.toString())*8);
				buIndirectCost=Math.round((buIndirectCost/billableCount)*100.0)/100.0;
			}
		}
		map.put("buNonBillableCount", (Math.round((Double.parseDouble(nonBillableHours+"."+nonBillableMins)/(Double.parseDouble(workingDays.toString())*8))*100.0)/100.0));
		map.put("buBillableCount", Math.round(billableCount*100.0)/100.0);
		map.put(ConstantUtility.PER_PERSON_HOURS,(Double.parseDouble(workingDays.toString()))*8);
		map.put("buIndirectCost", buIndirectCost);
		map.put("nonBillableCostSum", nonBillableCostSum);
		}
		return map;
	}
	
	public Double getIndirectCostHourly(String accessToken, String year, String businessVertical, int month) {
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		Months monthEnum = Months.valueOf(monthName.toUpperCase());
		Double totalCostHourly=0.0;
		Double assetCost=0D;

		IndirectCost indirectCost = costRepository.findByYearAndIsDeletedAndMonth(year, false, monthEnum);
		if(indirectCost!=null) {
		Map<String, Object> data = feignLegacyInterface.getCompanyExpectedHours( accessToken,month, indirectCost.getYear());
		Object companyExpectedHours = data.get(ConstantUtility.DATA);
		Map<String, Object> workingDaysData = (Map<String, Object>)feignLegacyInterface.getpayrollWorkingDays( accessToken,month, Integer.parseInt(year)).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		Double companyCount=0.0;
		Double staffCostPerSeat=0.0;
		Double infraCost=indirectCost.getInfraCost();
		Double variableCost=indirectCost.getVariableCost();
		Double reimbursementCost=indirectCost.getReimbursement();
		if(Double.parseDouble(companyExpectedHours.toString())!=0.0) {
			companyCount = Double.parseDouble(companyExpectedHours.toString()) / (Double.parseDouble(workingDays.toString()) * 8);
			Double staffCost = getStaffCost(accessToken, month, indirectCost.getYear(), workingDays, ConstantUtility.OPERATIONS_SUPPORT);
			staffCostPerSeat = ((Math.round(staffCost * 100) / 100) / companyCount);
			assetCost = assetCost/companyCount;
			infraCost=(indirectCost.getInfraCost() / companyCount);
			variableCost=(indirectCost.getVariableCost() / companyCount);
			reimbursementCost=(indirectCost.getReimbursement()!=null?indirectCost.getReimbursement() /companyCount:0.00);
			
		}
		Double totalCost = 0.0;
		if (businessVertical.equals("")) {
			totalCost = staffCostPerSeat + infraCost + variableCost+reimbursementCost+ assetCost;
		} else {
			Double buCost = buIndirectCost(accessToken, businessVertical, month, year, null);
			totalCost = staffCostPerSeat + infraCost + variableCost + buCost + reimbursementCost + assetCost;
		}
		totalCostHourly = Math.round(totalCost / (Double.parseDouble(workingDays.toString()) * 8) * 100.0) / 100.0;
		}
		return totalCostHourly;
	}
	
	public Double getIndirectCostHourlyV2(String accessToken, String year, String businessVertical, int month,IndirectCost indirectCost,Object companyExpectedHours,Object workingDays) {
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		Months monthEnum = Months.valueOf(monthName.toUpperCase());
		Double totalCostHourly=0.0;
		Double assetCost=0D;

		if(indirectCost!=null) {
		
		Double companyCount=0.0;
		Double staffCostPerSeat=0.0;
		Double infraCost=indirectCost.getInfraCost();
		Double variableCost=indirectCost.getVariableCost();
		Double reimbursementCost=indirectCost.getReimbursement();
		if(Double.parseDouble(companyExpectedHours.toString())!=0.0) {
			companyCount = Double.parseDouble(companyExpectedHours.toString()) / (Double.parseDouble(workingDays.toString()) * 8);
			Double staffCost = getStaffCost(accessToken, month, indirectCost.getYear(), workingDays, ConstantUtility.OPERATIONS_SUPPORT);
			staffCostPerSeat = ((Math.round(staffCost * 100) / 100) / companyCount);
			assetCost = assetCost/companyCount;
			infraCost=(indirectCost.getInfraCost() / companyCount);
			variableCost=(indirectCost.getVariableCost() / companyCount);
			reimbursementCost=(indirectCost.getReimbursement()!=null?indirectCost.getReimbursement() /companyCount:0.00);
			
		}
		Double totalCost = 0.0;
		if (businessVertical.equals("")) {
			totalCost = staffCostPerSeat + infraCost + variableCost+reimbursementCost+ assetCost;
		} else {
			Map<String, Object> userListAndCount = feignLegacyInterface.gradeWiseExpectedHours(month, Integer.parseInt(indirectCost.getYear()) );
			
			Map<String, Double> gradeWiseCosts = utilityService.getGradeWiseCosts(month, Integer.parseInt(indirectCost.getYear()) , accessToken, "",userListAndCount);
			Double buCost = buIndirectCostV2(accessToken, businessVertical, month, year,workingDays,gradeWiseCosts);
			totalCost = staffCostPerSeat + infraCost + variableCost + buCost + reimbursementCost + assetCost;
		}
		totalCostHourly = Math.round(totalCost / (Double.parseDouble(workingDays.toString()) * 8) * 100.0) / 100.0;
		}
		return totalCostHourly;
	}


	public List< Object> getBuIndirectTotal(String accessToken, String month, String year, String businessVertical) {
		List<Object> buIndirectProjects=new ArrayList<>();
		Double buIndirectCost=0.0;
		int monthNum=getMonthNumber(month);
		List<Long> projectIds=new ArrayList<>();
		Map<String, Object> workingDaysData = (Map<String, Object>) feignLegacyInterface.getpayrollWorkingDays( accessToken,monthNum, Integer.parseInt(year)).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		Map<String, Object>  buProject=(Map<String, Object>) feignLegacyInterface.getBuProjectIds(accessToken, businessVertical, monthNum, year).get("data");
		Map<String, Object> userListAndCount = feignLegacyInterface.gradeWiseExpectedHours(monthNum, Integer.parseInt(year) );
		
		Map<String, Double> gradeWiseCosts = utilityService.getGradeWiseCosts(monthNum, Integer.parseInt(year) , accessToken, "", userListAndCount);
		
		List<Object> buProjects=loginUtilityService.objectToListConverter(buProject.get(ConstantUtility.PROJECT_IDS_LIST));

		buProjects.forEach(projectId->{
			String monthName=new DateFormatSymbols().getMonths()[monthNum-1].toString();

			List<ProjectInvoice> projectInvoice = new ArrayList<>();
			if(businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT))
				projectInvoice=projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndProjectIdAndIsInternalAndInvoiceStatusNot(monthName, year, false, Long.parseLong(projectId.toString()),false,6L);
			else
				projectInvoice=projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndProjectIdAndInvoiceStatusNot(monthName, year, false, Long.parseLong(projectId.toString()),6L);
			
			if(projectInvoice.isEmpty()) {
				projectIds.add(Long.parseLong(projectId.toString()));
			}
		});
		
		Map<String, Object> allCompensation = projectMarginservice.getAllCompensation(monthNum, Integer.parseInt(year));
		Map<String,Object> voluntaryPayData = (Map<String, Object>) allCompensation.get("voluntaryPayResponse");
		Map<String,Object> payDaysData =  (Map<String, Object>) allCompensation.get("PayDaysResponse");
		List<Map<String,Object>> teamList=(List<Map<String, Object>>) buProject.get(ConstantUtility.TEAM_LIST);

		for(Long id:projectIds) {
			Map<String,Object> projectData=new HashMap<>();
			Double projectIndirectCost=0.0;
			Double projectDirectCost=0.0;
			Integer nonBillableHours=0;
			Integer nonBillableMins=0;
			List<Map<String,Object>> projectFiltered= teamList.stream().filter(data->data.get(ConstantUtility.PROJECT_ID).toString().equals(id.toString())).collect(Collectors.toList());
			List<Object> team= new ArrayList<>();
			Map<String,Object> projectExpectedHours= null;
			if(!projectFiltered.isEmpty()) {
				projectExpectedHours=projectFiltered.get(0);
				if (projectExpectedHours != null) {
					team = (List<Object>) projectExpectedHours.get("team");
				    Object projectName=projectExpectedHours.get(ConstantUtility.PROJECT_NAME);
					projectData.put(ConstantUtility.PROJECT_ID, id);
					projectData.put(ConstantUtility.PROJECT_NAME, projectName);
				}
				for(int i=0;i<team.size();i++) {
					Map<String,Object> userData=loginUtilityService.objectToMapConverter(team.get(i));
					String expectedHours=userData.get(ConstantUtility.EXPECTED_HOURS).toString();
					String mins=expectedHours.split("\\.")[1];
					String hours=expectedHours.split("\\.")[0];
					nonBillableHours=nonBillableHours+Integer.parseInt(hours);
					nonBillableMins=nonBillableMins+Integer.parseInt(mins);
					if(nonBillableMins>=60) {
						nonBillableHours=nonBillableHours+1;
						nonBillableMins=nonBillableMins-60;
					}
					String grade ="NA";
					if(userData.containsKey(ConstantUtility.GRADE) && userData.get(ConstantUtility.GRADE)!=null)
						grade = userData.get(ConstantUtility.GRADE).toString();
		
					Double userSalary = 0D;
					if(gradeWiseCosts != null)
						userSalary=getPay(userData,monthNum,Integer.parseInt(year),workingDays,voluntaryPayData,payDaysData);
					Double indirectCostNonBillable=0D;
					if(gradeWiseCosts!=null && grade!=null && gradeWiseCosts.containsKey(grade) && gradeWiseCosts.get(grade)!=null) {
						double hourlyIndirectCost = gradeWiseCosts.get(grade) / ((Double.parseDouble(workingDays.toString())) * 8);
						Double collectiveExpectedHours=Double.parseDouble(expectedHours);
						if (Double.parseDouble(mins) != 0D) {
							collectiveExpectedHours = Double.parseDouble(hours);
							collectiveExpectedHours = collectiveExpectedHours + (Double.parseDouble(mins) / 60);
						}
						indirectCostNonBillable=hourlyIndirectCost * collectiveExpectedHours ;
					}
					buIndirectCost=buIndirectCost+userSalary;
					projectIndirectCost=projectIndirectCost+indirectCostNonBillable;
					projectDirectCost= projectDirectCost+userSalary;
				}
				projectData.put("projectIndirectCost", Math.round(projectIndirectCost*100.0)/100.0);
				projectData.put("projectDirectCost", Math.round(projectDirectCost*100.0)/100.0);
				projectData.put("projectCost", Math.round((projectDirectCost + projectIndirectCost)*100.0)/100.0);
				projectData.put(ConstantUtility.EXPECTED_HOURS, Math.round(Double.parseDouble(nonBillableHours+"."+nonBillableMins)*100.0)/100.0);
				Double nonBillableCount=Double.parseDouble(nonBillableHours+"."+nonBillableMins)/(Double.parseDouble(workingDays.toString())*8);
				projectData.put(ConstantUtility.PROJECT_EMPLOYEE_COUNT,Math.round(nonBillableCount*100.0 )/100.0);
				buIndirectProjects.add(projectData);
			}
		}
		return buIndirectProjects;
	}

	public List<Object> getBuBillableProjects(String accessToken, String businessVertical, String month,String year) {
		List<Object> buIndirectProjects=new ArrayList<>();
		int monthNum=getMonthNumber(month);
		List<Long> projectIds=new ArrayList<>();
		Map<String, Object> workingDaysData = (Map<String, Object>) feignLegacyInterface.getpayrollWorkingDays( accessToken,monthNum, Integer.parseInt(year)).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		Map<String, Object> buProject = (Map<String, Object>) feignLegacyInterface.getBuProjectIds(accessToken, businessVertical, monthNum, year).get("data");
		
		
		List<Object> buProjects=loginUtilityService.objectToListConverter(buProject.get(ConstantUtility.PROJECT_IDS_LIST));
		buProjects.forEach(projectId->{
			String monthName=new DateFormatSymbols().getMonths()[monthNum-1].toString();
			List<ProjectInvoice> projectInvoice = new ArrayList<>();
			if(businessVertical.equals(ConstantUtility.OPERATIONS_SUPPORT))
				projectInvoice=projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndProjectIdAndIsInternalAndInvoiceStatusNot(monthName, year, false, Long.parseLong(projectId.toString()),false, 6L);
			else
				projectInvoice=projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndProjectIdAndInvoiceStatusNot(monthName, year, false, Long.parseLong(projectId.toString()), 6L);
			
			if(!projectInvoice.isEmpty()) {
				projectIds.add(Long.parseLong(projectId.toString()));
			}
		});
		List<Map<String,Object>> teamList=(List<Map<String, Object>>) buProject.get(ConstantUtility.TEAM_LIST);

		for(Long id:projectIds) {
			Map<String,Object> projectData=new HashMap<>();
			Integer billableHours=0;
			Integer billableMins=0;
			List<Map<String,Object>> projectFiltered= teamList.stream().filter(data->data.get(ConstantUtility.PROJECT_ID).toString().equals(id.toString())).collect(Collectors.toList());
			List<Object> team= new ArrayList<>();
			Map<String,Object> projectExpectedHours= null;
			if(!projectFiltered.isEmpty()) {
				projectExpectedHours=projectFiltered.get(0);
				team=(List<Object>) projectExpectedHours.get("team");
			}
			Object projectName=projectExpectedHours.get(ConstantUtility.PROJECT_NAME);
			projectData.put(ConstantUtility.PROJECT_ID, id);
			projectData.put(ConstantUtility.PROJECT_NAME, projectName);
			for(int i=0;i<team.size();i++) {
				Map<String,Object> userData=loginUtilityService.objectToMapConverter(team.get(i));
				String expectedHours=userData.get(ConstantUtility.EXPECTED_HOURS).toString();
				String mins=expectedHours.split("\\.")[1];
				String hours=expectedHours.split("\\.")[0];
				billableHours=billableHours+Integer.parseInt(hours);
				billableMins=billableMins+Integer.parseInt(mins);
				if(billableMins>=60) {
					billableHours=billableHours+1;
					billableMins=billableMins-60;
				}
			}
			projectData.put(ConstantUtility.EXPECTED_HOURS,Double.parseDouble(billableHours+"."+billableMins));
			Double nonBillableCount=Double.parseDouble(billableHours+"."+billableMins)/(Double.parseDouble(workingDays.toString())*8);
			projectData.put(ConstantUtility.PROJECT_EMPLOYEE_COUNT,Math.round(nonBillableCount*100.0 )/100.0);
			buIndirectProjects.add(projectData);
		}
		return buIndirectProjects;
	}
	
	public Double projectCostForOS(String accessToken,int month,int year,Double buExpectedHours,Object workingDays) {
		Map<String,Object> data=feignLegacyInterface.getCompanyExpectedHours(accessToken,month,Integer.toString(year));
		Object companyExpectedHours=data.get(ConstantUtility.DATA);
		Double companyCount=0.0;
		if(Double.parseDouble(companyExpectedHours.toString())!=0.0) 
			companyCount = Double.parseDouble(companyExpectedHours.toString()) / (Double.parseDouble(workingDays.toString()) * 8);
		Double buCount=buExpectedHours/(Double.parseDouble(workingDays.toString())*8);
		String monthName=new DateFormatSymbols().getMonths()[month-1].toString();
		Months monthObj=Months.valueOf(monthName.toUpperCase());
		Double totalCostBu=0.0;
		IndirectCost indirectCost=costRepository.findByYearAndIsDeletedAndMonth(Integer.toString(year), false, monthObj);
		if(indirectCost!=null) {
			totalCostBu=((indirectCost.getInfraCost()/companyCount)+(indirectCost.getVariableCost()/companyCount))*buCount;
		}
		return totalCostBu;
	}
	
	public Double getPerPersonHours(String accessToken,String month,String year) {
		int monthNum=getMonthNumber(month);
		Map<String, Object> workingDaysData = (Map<String, Object>) feignLegacyInterface.getpayrollWorkingDays(accessToken, monthNum, Integer.parseInt(year)).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		return Double.parseDouble(workingDays.toString())*8;
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> getEarnedMonthlyPay(String accessToken, int month, int year, long userId) {
		HashMap<String, Object> response = new HashMap<>();
		Map<String, Object> workingDaysData = (Map<String, Object>) feignLegacyInterface.getpayrollWorkingDays(accessToken, month, year).get("data");
		Map<String,Object> timesheetHour =  (Map<String, Object>) feignLegacyInterface.getTimeSheetHours(accessToken, month, year, userId).get("data");
		List<Map<String,Object>> timesheetHours= (List<Map<String, Object>>) timesheetHour.get("attendanceData");
		Map<String, Object> userTimesheet = (Map<String, Object>) timesheetHours.get(0);
		Double workingDays = Double.parseDouble(workingDaysData.get(ConstantUtility.WORKING_DAYS).toString());
		Double actual_hours = Double.parseDouble(userTimesheet.get(ConstantUtility.ACTUAL_HOURS).toString());
		double hourly_pay = consolidatedService.getPay(userId, month, year, workingDays);
		double earned_pay = (actual_hours * hourly_pay);
		response.put("earnedPay", encrypter.getEncryptedValue(earned_pay));
		response.put(ConstantUtility.WORKING_DAYS, workingDays);
		return response;
	}
	
	/**
	 * <p>Calculates referenced and Fixed Cost based on grades.
	 * See the controller mapping {@linkplain IndirectCostController#
	 * getAllGradeBasedIndirectCost(int, int, String)}.</p>
	 * 
	 * @param month
	 * @param year
	 * @param accessToken
	 * @return {@code List<IndirectCostGradeBasedDTO>}
	 * @see {@linkplain #getGradeIndirectCost(Map, String, int, int)} , {@linkplain GradeBasedIndirectCost}
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getGradeWiseIndirectCost(int month, int year, String accessToken) {
		double referenceCost = 0.00;
		double totalReferenceCost = 0.00;
		double totalCommulativeCost = 0.00;
		Map<String, Object> returnValue = new HashMap<String, Object>();
		List<IndirectCostGradeBasedDTO> indirectCosts = new ArrayList<>();
		double companyCount=0.0;
		List<String> allGrades = payrollTrendsService.getAllGrades(accessToken);
		Map<String, Object> userListAndCount = (Map<String, Object>) feignLegacyInterface.gradeWiseExpectedHours(month, year);
		Double cummulativeFixedCost=0D;
		for(String grade : allGrades) {
			Map<String, Object> currentGradeUserListAndCount = (Map<String, Object>) userListAndCount.get(grade);
			if(!grade.equals("E1")) {
				IndirectCostGradeBasedDTO indirectCostDTO = new IndirectCostGradeBasedDTO();
				indirectCostDTO.setUserCount(Math.round(((double) currentGradeUserListAndCount.get(ConstantUtility.USER_COUNT))*100.00)/100.00);
				indirectCostDTO.setGrade(grade);
				referenceCost = getReferenceCost(userListAndCount, grade, month, year);
				indirectCostDTO.setReferenceCost(Math.round(referenceCost * 100.00) / 100.00);
				indirectCostDTO.setRank(new Long( currentGradeUserListAndCount.get("rank").toString()));
				Optional<GradeBasedIndirectCost> indirectCost = gradeBasedCostRepository.findByGradeAndMonthAndYear(grade, month, year);						
				double fixedCost = indirectCost.isPresent() ? indirectCost.get().getFixedCost() : 0.00;						
				indirectCostDTO.setFixedCost(fixedCost);
				cummulativeFixedCost = cummulativeFixedCost + (fixedCost * (double) currentGradeUserListAndCount.get(ConstantUtility.USER_COUNT));
				indirectCosts.add(indirectCostDTO);
			} 
		}
		IndirectCostGradeBasedDTO indirectCostDTO = new IndirectCostGradeBasedDTO();
		Map<String, Object> currentGradeUserListAndCount = (Map<String, Object>) userListAndCount.get("E1");
		indirectCostDTO.setUserCount(Math.round(((double) currentGradeUserListAndCount.get(ConstantUtility.USER_COUNT))*100.00)/100.00);
		indirectCostDTO.setRank(new Long( currentGradeUserListAndCount.get("rank").toString()));
		indirectCostDTO.setGrade("E1");
		referenceCost = getReferenceCost(userListAndCount, "E1", month, year);
		indirectCostDTO.setReferenceCost(Math.round(referenceCost * 100.00) / 100.00);
		calculateE1FixedCost(month, year, indirectCostDTO, accessToken,cummulativeFixedCost,"");
		totalCommulativeCost = indirectCostDTO.getTotalCommulativeCost();
		cummulativeFixedCost=cummulativeFixedCost+(indirectCostDTO.getFixedCost()*indirectCostDTO.getUserCount());
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
		returnValue.put("indirectCosts", indirectCosts);
		returnValue.put("totalReferenceCost", totalReferenceCost);
		returnValue.put("totalFixedCost", cummulativeFixedCost);
		returnValue.put(ConstantUtility.TOTAL_CUMULATIVE_COST, totalCommulativeCost);
		returnValue.put("companyCount", companyCount);
		return returnValue;
	}
	
	/**
	 * <p>For Calculating Grade E1 fixed Cost.
	 * See {@linkplain #getGradeWiseIndirectCost(int, int, String)}</p>
	 * @param month 
	 * @param year
	 * @param indirectCostDTO
	 * @param source 
	 */
	public void calculateE1FixedCost(int month, int year, IndirectCostGradeBasedDTO indirectCostDTO, String accessToken,double cummulativeFixedCost, String source) {
		double fixedCostSum = 0.00;
		double indirectCost = 0.00;
		double e1VariableCost = 0.00;
		double totalCommulativeCost = 0.00;
		fixedCostSum=cummulativeFixedCost;
		// Get Working Days of Month
		Map<String, Object> workingDaysData = (Map<String, Object>)feignLegacyInterface.getpayrollWorkingDays(accessToken,month, year).get("data");
		Double workingDays = Double.parseDouble(workingDaysData.get(ConstantUtility.WORKING_DAYS).toString());
		// Get overall company Hours
		Map<String, Object> data = feignLegacyInterface.getCompanyExpectedHours(accessToken,month, Integer.toString(year));
		Object companyExpectedHours = data.get(ConstantUtility.DATA);
		Double companyCount = Double.parseDouble(companyExpectedHours.toString())/ (Double.parseDouble(workingDays.toString()) * 8);
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		Months monthEnum = Months.valueOf(monthName.toUpperCase());
		IndirectCost monthlyIndirectCost = costRepository.findByYearAndIsDeletedAndMonth(Integer.toString(year), false, monthEnum);
		int ptr=6;
		YearMonth yearMonth = YearMonth.of(year, month);
		if(source.equals("Forecast")) {
			while(monthlyIndirectCost==null && ptr<=6) {
				yearMonth = yearMonth.minusMonths(1);
				String monthNam = new DateFormatSymbols().getMonths()[yearMonth.getMonthValue() - 1].toString();
				Months monthEnu = Months.valueOf(monthNam.toUpperCase());
				monthlyIndirectCost = costRepository.findByYearAndIsDeletedAndMonth(Integer.toString(yearMonth.getYear()), false, monthEnu);
				ptr--;
			}
		}
		if((monthlyIndirectCost!=null && source.equals("")) || source.equals("Forecast")) {
			Double assetCost=0D;

			Map<String, Object> indirectCostMap = getCostData(accessToken, companyCount, monthlyIndirectCost, "", workingDays, assetCost);
			indirectCost = (double) indirectCostMap.get(ConstantUtility.TOTAL_COST);
			totalCommulativeCost = (double) indirectCostMap.get(ConstantUtility.TOTAL_CUMULATIVE_COST);
			if (indirectCostDTO.getUserCount() != 0) {
				e1VariableCost = ((indirectCost * companyCount) - fixedCostSum) / indirectCostDTO.getUserCount();
			}
		}
		indirectCostDTO.setFixedCost(Math.round(e1VariableCost * 100.00) / 100.00);
		indirectCostDTO.setVariable(true);
		indirectCostDTO.setTotalCommulativeCost(totalCommulativeCost);
		indirectCostDTO.setCompanyCount(companyCount);
	}
	
	/**
	 * <p>Calculates Reference cost for all grades.</p>
	 * @param userListAndCount - Current Grade User list with count
	 * @param grade - Current grade being passed on.
	 * @param month
	 * @param year
	 * @return the calculated reference cost. 
	 * @see {@linkplain #getGradeWiseIndirectCost(int, int, String)}
	 */
	@SuppressWarnings("unchecked")
	public double getReferenceCost(Map<String, Object> userListAndCount, String grade, int month, int year) {
		Map<String, Object> thisGradeUserListAndCount = (Map<String, Object>) userListAndCount.get(grade);
		List<Map<String, Object>> userList = (List<Map<String, Object>>) thisGradeUserListAndCount.get(ConstantUtility.USER_LIST);
		List<Double> salaryData = new ArrayList<>();
		if(!userList.isEmpty()) {
			userList.forEach( userData -> {
				Long userId = Long.parseLong(userData.get("id").toString());
				PayRegister payregister = utilityService.getMonthsalary(userId, month, year); 
				if(payregister != null) {
					salaryData.add(payregister.getTotalMonthlyPay());
				}
				else {
					salaryData.add((double) 0);
				}
			});
		Double min = salaryData.get(0);
		Double max = salaryData.get(salaryData.size() -1);
		Double median = payrollTrendsService.calculateMedian(salaryData);
		return payrollTrendsService.findMedian(salaryData, median, min, max) / 2;
		}
		return 0.00;
	}
	
	/**
	 * <p>Saves the Fixed Cost of given grade for given month
	 * and year. A grade can have single fixed cost for each
	 * month.</p>
	 * @param grade
	 * @param accessToken
	 * @param fixedCost
	 * @param month
	 * @param year
	 * @return the saved grade cost
	 */
	public GradeBasedIndirectCost saveFixedCostOfGrade(String grade, String accessToken, double fixedCost, int month, int year) {
		Optional<GradeBasedIndirectCost> indirectCost = gradeBasedCostRepository.findByGradeAndMonthAndYear(grade, month, year);
		if(indirectCost.isPresent()) {
			indirectCost.get().setFixedCost(fixedCost);
			return gradeBasedCostRepository.save(indirectCost.get());
		} else {
			GradeBasedIndirectCost newFixedCost = new GradeBasedIndirectCost(0, grade, fixedCost, month, year, false);
			if(grade.equals("E1")) {
				newFixedCost.setVariable(true);
			}
			return gradeBasedCostRepository.save(newFixedCost);
		}
	}

	public MarginBasis setMarginBasis(int month, int year, boolean isGradeWise) {
		MarginBasis marginBasis = marginBasisRepository.findByMonthAndYear(month, year);
		try {
			if (marginBasis == null) {
				marginBasis = new MarginBasis();
				marginBasis.setMonth(month);
				marginBasis.setYear(year);
			}
			marginBasis.setIsGradeWise(isGradeWise);
			if (isGradeWise){
				marginBasis.setIsUniform(false);
				marginBasis.setIsLTM(true);
			}
			else{
				marginBasis.setIsUniform(true);
				marginBasis.setIsLTM(false);
			}
			marginBasis = marginBasisRepository.save(marginBasis);
			return marginBasis;
		} 
		catch (Exception e) {
			log.info("Exception while saving is :::::"+e.getLocalizedMessage());
			return null;
		}
	}
	
	public Map<String,Boolean> getMarginBasis(int month, int year) {
		MarginBasis marginBasis = marginBasisRepository.findByMonthAndYear(month, year);
		Map<String,Boolean> res = new HashMap<>();
		if(marginBasis != null ) {
			res.put("IsGradeWise", marginBasis.getIsGradeWise());
			res.put("IsLTM", marginBasis.getIsLTM());
		}
		else{
			res.put("IsGradeWise", false);
			res.put("IsLTM", false);
		}
		return res;
	}
	
	public Map<Integer, Object> getMonthWiseGICByYear(int year) {
		return gradeBasedCostRepository.findAllByYearAndIsVariable(year, false)
				.map(IndirectCostService::monthWiseGICMapper)
				.orElse(new HashMap<>());
	}
	
	public static  Map<Integer, Object> monthWiseGICMapper(List<GradeBasedIndirectCost> gradeBasedCost) {
		Map<Integer, Object> response = new HashMap<>();
		BiFunction<List<GradeBasedIndirectCost>, GradeBasedIndirectCost, Map<String, Object>> 
			monthWithMontlyCost = monthlyGIC();
		
		for(GradeBasedIndirectCost gic : gradeBasedCost) {
			response.put(gic.getMonth(), monthWithMontlyCost.apply(gradeBasedCost, gic));
		}
		return response;
	}

	private static BiFunction<List<GradeBasedIndirectCost>, GradeBasedIndirectCost, Map<String, Object>> monthlyGIC() {
		return (list, singleCost) -> {
			return list.parallelStream().filter(singleOb -> singleOb.getMonth() == singleCost.getMonth())
					.collect(Collectors.toMap(GradeBasedIndirectCost::getGrade, GradeBasedIndirectCost::getFixedCost));
		};
	}

	public List<Object> getAssetCost(String authorization, String month, String year, String businessVertical) {
		List<Object> dataList=new ArrayList<>();
		Map<String, Object> map = new HashMap<>();
		Months monthObj=Months.valueOf(month);
		int monthNum=getMonthNumber(month);
		Map<String, Object> data = feignLegacyInterface.getCompanyExpectedHours( authorization,monthNum, year);
		Object companyExpectedHours = data.get(ConstantUtility.DATA);
		Map<String, Object> workingDaysData = (Map<String, Object>) feignLegacyInterface.getpayrollWorkingDays(authorization, monthNum, Integer.parseInt(year)).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		Double companyCount=0.0;
		Double totalAssetCost=0D;

		Double assetCost=0D;
		if(Double.parseDouble(companyExpectedHours.toString())!=0.0 && totalAssetCost!=0D) {
			companyCount=Double.parseDouble(companyExpectedHours.toString())/(Double.parseDouble(workingDays.toString())*8);
			assetCost=(totalAssetCost/companyCount);
		}
		map.put(ConstantUtility.TOTAL_ASSET_COST, Math.round(totalAssetCost * 100.0) / 100.0);
		map.put(ConstantUtility.TOTAL_EXPECTED_HOURS, Math.round(Double.parseDouble(companyExpectedHours.toString()) * 100.0) / 100.0);
		map.put(ConstantUtility.TOTAL_EMPLOYEES, Math.round(companyCount * 100.0) / 100.0);
		map.put(ConstantUtility.PER_PERSON_HOURS, (Double.parseDouble(workingDays.toString())) * 8);
		map.put(ConstantUtility.ASSET_COST, Math.round(assetCost * 100.0) / 100.0);
		dataList.add(map);
		return dataList;
	}
}
