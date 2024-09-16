package com.krishna.service.util;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import com.krishna.Interfaces.IConsolidatedService;
import com.krishna.controller.FeignLegacyInterface;
import com.krishna.domain.IndirectCost;
import com.krishna.domain.LeaveCostPercentage;
import com.krishna.domain.PayRegister;
import com.krishna.domain.PayRevisions;
import com.krishna.domain.Payroll;
import com.krishna.domain.Margin.MarginBasis;
import com.krishna.domain.Margin.ProjectSnapshots;
import com.krishna.domain.averagebilling.AverageBillingCompliance;
import com.krishna.domain.averagebilling.BillingCompliancePercentage;
import com.krishna.domain.invoice.PaymentMode;
import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.dto.AverageBillingRateDto;
import com.krishna.enums.Months;
import com.krishna.repository.AverageBillingComplianceRepository;
import com.krishna.repository.BillingCompliancePercentageRepository;
import com.krishna.repository.IndirectCostRepository;
import com.krishna.repository.MarginBasisRepository;
import com.krishna.repository.ProjectSnapshotRepository;
import com.krishna.repository.invoice.PaymentModeRepository;
import com.krishna.repository.invoice.ProjectInvoiceRepository;
import com.krishna.repository.payroll.LeaveCostPercentageRepository;
import com.krishna.repository.payroll.PayRegisterRepository;
import com.krishna.repository.payroll.PayRevisionRepository;
import com.krishna.repository.payroll.PayrollRepository;
import com.krishna.service.IndirectCostService;
import com.krishna.service.LoginUtiltiyService;
import com.krishna.service.MailService;
import com.krishna.service.ProjectMarginService;
import com.krishna.service.AccessUtility.AccessUtilityService;
import com.krishna.service.invoice.InvoicePipelineService;
import com.krishna.util.ConstantUtility;

@Service
public class ConsolidatedService implements IConsolidatedService {

	@Autowired
	LoginUtiltiyService loginUtilityService;

	@Autowired
	PayRegisterRepository payRegisterRepository;

	@Autowired
	IndirectCostService indirectCostService;

	@Autowired
	PayRevisionRepository payRevisionRepository;

	@Autowired
	ProjectMarginService projectMarginservice;

	@Autowired
	PayrollRepository payrollRepository;

	@Autowired
	LeaveCostPercentageRepository leaveCostPercent;

	@Autowired
	ProjectSnapshotRepository snapshotRepository;

	@Autowired
	ProjectInvoiceRepository projectInvoiceRepository;

	@Autowired
	InvoicePipelineService invoicePipelineService;

	@Autowired
	AccessUtilityService accessUtilityService;

	@Autowired
	AverageBillingComplianceRepository averageBillingRateComplRepo;

	@Autowired
	MailService mailService;
	
	@Autowired
	BillingCompliancePercentageRepository compliancePercRepo;
	
	@Autowired
	PaymentModeRepository paymentModeRepository;
	
	@Autowired
	FeignLegacyInterface legacyInterface;
	
	@Autowired
	UtilityService utilService;
	
	@Autowired
	IndirectCostRepository costRepository;
	
	@Autowired
	MarginBasisRepository marginBasisRepository;

	Logger log = LoggerFactory.getLogger(ConsolidatedService.class);
	
	@Autowired
	private FeignLegacyInterface feignLegacyInterface;

	@Override
	@Cacheable("salaryReconciliationUsers")
	public List<Map<String,Object>> getUsersForSalaryReconcilliation(String accessToken, int month, int year, String userStatus,
			String businessVertical) {
		// Fetch month wise Users along with project wise expected Hours and overall
		// hours
		 Map<String, Object> res = legacyInterface.getUserDataForConsolidatedPage(accessToken, month, year, userStatus, businessVertical);
		 if(Objects.nonNull(res)) {
		List<Map<String,Object>> data = (List<Map<String, Object>>) res.get("data");
		return data;
		}
		 else
			 return new ArrayList<>();
	}

	@Override
	public Map<String, Object> getUserDataForConsolidatedPage(String accessToken, int month, int year,
			String userStatus, String businessVertical, List<Map<String, Object>> data) {
		// Fetch month wise Users along with project wise expected Hours and overall
		// hours
		int listSize = data.size();
		// Fetch working days in a month
		Map<String, Object> workingDaysData = (Map<String, Object>) legacyInterface.getpayrollWorkingDays(accessToken,month, year).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);

		List<LeaveCostPercentage> prevCosts = leaveCostPercent.findAllByIsDeleted(false);
		LeaveCostPercentage leaveCost = prevCosts.get(0);
		double leavecostPerc = leaveCost.getLeaveCostPercentage();
		// Get the number of days in that month
		YearMonth yearMonthObject = YearMonth.of(year, month);
		int daysInMonth = yearMonthObject.lengthOfMonth(); 
		
		double salaryTotal = 0;
		double salaryMonthlyTotal = 0;
		double paidSalaryTotal = 0;
		double ctcPaidSalaryTotal = 0;
		double totalLA = 0;
		double totalPaidLeaveAmount = 0;
		for (int i = 0; i < listSize; i++) {
			Map<String, Object> user = loginUtilityService.objectToMapConverter(data.get(i));
			Long userId = Long.parseLong(user.get("id").toString());
//			double hourlySalary = getPay(userId, month, year, workingDays);
			PayRegister payRegister=utilService.getMonthsalary(userId, month, year);
			double hourlySalary = 0.0;
			double hourlyMonthlySalary = 0.0;
			if(payRegister!=null) {
				hourlySalary = indirectCostService.getHourlySalary(workingDays, payRegister);
				hourlyMonthlySalary = indirectCostService.getHourlySalary(daysInMonth, payRegister);
			}
			double overallSalary = hourlySalary * Double.parseDouble(user.get(ConstantUtility.TOTAL_HOURS).toString());
			salaryTotal = salaryTotal + overallSalary;
			double monthlySalary = 0;
			if(user.containsKey("monthlyHours")) 
				monthlySalary = hourlyMonthlySalary * Double.parseDouble(user.get("monthlyHours").toString());
			salaryMonthlyTotal = salaryMonthlyTotal + monthlySalary;
			user.put("overallSalary", overallSalary);
			user.put("monthlySalary", monthlySalary);
			double paidLeaveAmount = 0;
			double salary = 0;
			double salaryWithoutLA = 0;
			double laptopallowance = 0;
			
			Payroll payroll = payrollRepository.findAllByMonthAndUserIdAndYearAndIsDeleted(month, userId, year, false);
			if (payroll != null) {
				user.put("payrollStatus", payroll.getPayRollStatus());

				salary = payroll.getNetPay() + payroll.getTds() + payroll.getHealthInsurance()
						+ payroll.getEmployeePfContribution() + payroll.getInfraDeductions();
				laptopallowance = payroll.getLaptopAllowance();

				paidLeaveAmount = (leavecostPerc * (salary - payroll.getLaptopAllowance())) / 100;

				salaryWithoutLA = salary - payroll.getLaptopAllowance();
				totalLA += payroll.getLaptopAllowance();
			}
			paidSalaryTotal = paidSalaryTotal + salary;
			ctcPaidSalaryTotal = ctcPaidSalaryTotal + (salary + paidLeaveAmount);
			user.put("paidSalary", salary);
			user.put("paidCtc", salary + paidLeaveAmount);
			user.put("salaryDifference", (salary + paidLeaveAmount) - overallSalary);
			user.put("monthlySalaryDifference", (salary + paidLeaveAmount) - monthlySalary);
			user.put("laptopAllowance", laptopallowance);
			user.put("salaryWithoutLA", salaryWithoutLA);
			user.put("paidLeavePercentage", leavecostPerc);
			user.put("paidLeaveAmount", paidLeaveAmount);
			List<Object> projectsList = loginUtilityService.objectToListConverter(user.get(ConstantUtility.PROJECT_WISE_HOURS));
			int projectsListSize = projectsList.size();
			for (int j = 0; j < projectsListSize; j++) {
				Map<String, Object> project = loginUtilityService.objectToMapConverter(projectsList.get(j));
				Double expectedHours=Double.parseDouble(project.get(ConstantUtility.EXPECTED_HOURS).toString());
				String mins=project.get(ConstantUtility.EXPECTED_HOURS).toString().split("\\.")[1];
				if (Double.parseDouble(mins) != 0D) {
					expectedHours = Double.parseDouble(project.get(ConstantUtility.EXPECTED_HOURS).toString().split("\\.")[0]);
					expectedHours = expectedHours + (Double.parseDouble(mins) / 60);
				}
				double projectSalary = hourlySalary * expectedHours;
				project.put("projectSalary", projectSalary);
			}
		}

		totalPaidLeaveAmount = (leavecostPerc * (salaryTotal - totalLA)) / 100;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("salaryData", data);
		map.put(ConstantUtility.SALARY_TOTAL, salaryTotal);
		map.put("salaryMonthlyTotal", salaryMonthlyTotal);
		map.put("paidSalaryTotal", paidSalaryTotal);
		map.put("ctcPaidSalaryTotal", ctcPaidSalaryTotal);
		map.put("totalSalaryDifference", ctcPaidSalaryTotal - salaryTotal);
		map.put("monthSalaryDifference", ctcPaidSalaryTotal - salaryMonthlyTotal);
		map.put("totalSalaryWithoutLA", paidSalaryTotal - totalLA);
		map.put("totalPaidLeaveAmount", totalPaidLeaveAmount);
		map.put("totalLA", totalLA);
		map.put("leaveCostPercentage", leavecostPerc);
		return map;
	}
	
	@Override
	public Map<String, Object> getDirectCostForecast(String accessToken, int month, int year,
			String userStatus, String businessVertical, List<Map<String, Object>> data) {
		// Fetch month wise Users along with project wise expected Hours and overall
		// hours
		int listSize = data.size();
		// Fetch working days in a month
		Map<String, Object> workingDaysData = (Map<String, Object>) legacyInterface.getpayrollWorkingDays(accessToken,month, year).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);

		List<LeaveCostPercentage> prevCosts = leaveCostPercent.findAllByIsDeleted(false);
		LeaveCostPercentage leaveCost = prevCosts.get(0);
		double leavecostPerc = leaveCost.getLeaveCostPercentage();
		// Get the number of days in that month
		
		double salaryTotal = 0;
		double totalLA = 0;
		double totalPaidLeaveAmount = 0;
		Integer projectHours=0;
		Integer projectMins=0;
		for (int i = 0; i < listSize; i++) {
			Map<String, Object> user = loginUtilityService.objectToMapConverter(data.get(i));
			Long userId = Long.parseLong(user.get("id").toString());
			PayRegister payRegister=utilService.getMonthsalary(userId, month, year);
			double hourlySalary = 0.0;
			if(payRegister!=null) {
				log.info("workingDays..."+workingDays);
				hourlySalary = indirectCostService.getHourlySalary(workingDays, payRegister);
			}
			String userExpectedHours = user.get(ConstantUtility.FORECASTED_ACCOUNTING_HOURS).toString();
			String userMins=userExpectedHours.split("\\.")[1];
			String hours=userExpectedHours.split("\\.")[0];
			projectHours=projectHours+Integer.parseInt(hours);
			projectMins=projectMins+Integer.parseInt(userMins);
			if(projectMins>=60) {
				projectHours=projectHours+1;
				projectMins=projectMins-60;
			}
			double overallSalary = hourlySalary * Double.parseDouble(user.get(ConstantUtility.FORECASTED_ACCOUNTING_HOURS).toString());
			salaryTotal = salaryTotal + overallSalary;
			user.put("overallSalary", overallSalary);
			user.put(ConstantUtility.FORECASTED_HOURS, user.get(ConstantUtility.FORECASTED_ACCOUNTING_HOURS).toString());
			List<Object> projectsList = loginUtilityService.objectToListConverter(user.get(ConstantUtility.PROJECT_WISE_HOURS));
			int projectsListSize = projectsList.size();
			for (int j = 0; j < projectsListSize; j++) {
				Map<String, Object> project = loginUtilityService.objectToMapConverter(projectsList.get(j));
				Double expectedHours=project.get(ConstantUtility.FORECASTED_ACCOUNTING_HOURS)!=null ? Double.parseDouble(project.get(ConstantUtility.FORECASTED_ACCOUNTING_HOURS).toString()) : 0.0D;
				String mins=project.get(ConstantUtility.FORECASTED_ACCOUNTING_HOURS)!=null ? project.get(ConstantUtility.FORECASTED_ACCOUNTING_HOURS).toString().split("\\.")[1] : "0";
				if (Double.parseDouble(mins) != 0D) {
					expectedHours = project.get(ConstantUtility.FORECASTED_ACCOUNTING_HOURS)!=null ? Double.parseDouble(project.get(ConstantUtility.FORECASTED_ACCOUNTING_HOURS).toString().split("\\.")[0]) : 0.0D;
					expectedHours = expectedHours + (Double.parseDouble(mins) / 60);
				}
				double projectSalary = hourlySalary * expectedHours;
				project.put("projectSalary", projectSalary);
				project.put(ConstantUtility.FORECASTED_HOURS,project.get(ConstantUtility.FORECASTED_ACCOUNTING_HOURS)!=null?  project.get(ConstantUtility.FORECASTED_ACCOUNTING_HOURS).toString() : 0.0D);
			}
		}

		totalPaidLeaveAmount = (leavecostPerc * (salaryTotal - totalLA)) / 100;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("salaryData", data);
		map.put(ConstantUtility.SALARY_TOTAL, salaryTotal);
		map.put("totalPaidLeaveAmount", totalPaidLeaveAmount);
		map.put("totalLA", totalLA);
		map.put("leaveCostPercentage", leavecostPerc);
		map.put(ConstantUtility.TOTAL_HOURS, projectHours+"."+projectMins);
		Double monthlyWorkingDays=Double.parseDouble(workingDays.toString());
		map.put("workingDays",monthlyWorkingDays);
		map.put("totalCount", Double.parseDouble(projectHours+"."+projectMins)/(monthlyWorkingDays*8));
		return map;
	}

	public double getPay(Long userId, int month, int year, Object workingDays) {
		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDate lastDay = yearMonth.atEndOfMonth();
		LocalDate firstDay = yearMonth.atDay(1);
		double userSalary = 0.0;
		PayRegister currentPayregister = payRegisterRepository.findAllByUserIdAndIsCurrent(userId, true);
		if (currentPayregister != null) {
			if (currentPayregister.getEffectiveDate().toLocalDate().isBefore(lastDay.plusDays(1))
					|| currentPayregister.getEffectiveDate().toLocalDate().isEqual(lastDay)) {
				userSalary = indirectCostService.getHourlySalary(workingDays, currentPayregister);
			} else {
				userSalary = getSalaryFromPayrevisions(userId, userSalary, lastDay, firstDay, workingDays);
			}
		} else {
			userSalary = 0.0;
		}
		return userSalary;
	}

	public double getSalaryFromPayrevisions(Long userId, double userSalary, LocalDate lastDay, LocalDate firstDay,
			Object workingDays) {
		List<PayRevisions> payrevisions = payRevisionRepository.findAllByUserIdAndIsDeleted(userId, false);

		List<PayRevisions> finalpayrevision = new ArrayList<>();
		if (!payrevisions.isEmpty()) {
			payrevisions.forEach(payrev -> {
				LocalDate effectiveFrom = payrev.getEffectiveFrom().toLocalDate();
				LocalDate effectiveTo = payrev.getEffectiveTo().toLocalDate();
				boolean isEffective = projectMarginservice.checkEffectiveDate(firstDay, lastDay, effectiveFrom,
						effectiveTo);
				if (isEffective) {
					finalpayrevision.add(payrev);
				}
			});
		}

		if (!finalpayrevision.isEmpty()) {
			if (finalpayrevision.size() > 1) {
				PayRevisions dataPayrev = finalpayrevision.get(finalpayrevision.size() - 1);
				PayRegister payRegister = payRegisterRepository.findAllById(dataPayrev.getPayRegister().getId());
				userSalary = indirectCostService.getHourlySalary(workingDays, payRegister);
			} else {
				PayRevisions dataPayrev = finalpayrevision.get(0);
				PayRegister payRegister = payRegisterRepository.findAllById(dataPayrev.getPayRegister().getId());
				userSalary = indirectCostService.getHourlySalary(workingDays, payRegister);
			}
		} else {
			userSalary = 0.0;
		}
		return userSalary;
	}

	
	@Override
	public Map<String, Object> getSalaryDifference(String accessToken, int month, int year) {
		List<Map<String,Object>> data = (List<Map<String, Object>>) legacyInterface.getUserDataForConsolidatedPage(accessToken, month, year, "All", "").get("data");
		int listSize = data.size();
		// Fetch working days in a month
		Map<String, Object> workingDaysData = (Map<String, Object>) legacyInterface.getpayrollWorkingDays(accessToken,month, year).get("data");
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		List<LeaveCostPercentage> prevCosts = leaveCostPercent.findAllByIsDeleted(false);
		LeaveCostPercentage leaveCost = prevCosts.get(0);
		Double leavecostPerc = leaveCost.getLeaveCostPercentage();

		double salaryTotal = 0;
		double paidSalaryTotal = 0;
		double ctcPaidSalaryTotal = 0;
		for (int i = 0; i < listSize; i++) {
			Map<String, Object> user = loginUtilityService.objectToMapConverter(data.get(i));
			Long userId = Long.parseLong(user.get("id").toString());
			double hourlySalary = getPay(userId, month, year, workingDays);
			double overallSalary = hourlySalary * Double.parseDouble(user.get(ConstantUtility.TOTAL_HOURS).toString());
			salaryTotal = salaryTotal + overallSalary;
			double paidLeaveAmount = 0;
			double salary = 0;
			
			Payroll payroll = payrollRepository.findAllByMonthAndUserIdAndYearAndIsDeleted(month, userId, year, false);

			if (payroll != null) {
				salary = payroll.getNetPay() + payroll.getTds() + payroll.getHealthInsurance()
						+ payroll.getEmployeePfContribution() + payroll.getInfraDeductions();
				paidLeaveAmount = (leavecostPerc * (salary - payroll.getLaptopAllowance())) / 100;
			}
			paidSalaryTotal = paidSalaryTotal + salary;
			ctcPaidSalaryTotal = ctcPaidSalaryTotal + (salary + paidLeaveAmount);
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put(ConstantUtility.SALARY_TOTAL, salaryTotal);
		map.put("ctcPaidSalaryTotal", ctcPaidSalaryTotal);
		map.put("totalSalaryDifference", ctcPaidSalaryTotal - salaryTotal);
		return map;
	}

	@Override
	public List<Object> getUserSnapShot(String accessToken, Long projectId) {
		Map<String,Object> data = (Map<String,Object>)legacyInterface.getMarginExpectedHours(accessToken, projectId,
				LocalDateTime.now().minusMonths(1).getMonthValue(), LocalDateTime.now().minusMonths(1).getYear());
		List<Object> userList = new ArrayList<>();
		if(Objects.nonNull(data)) {
			List<Map<String,Object>> result = (List<Map<String,Object>>) data.get("data");
		int teamSize = result.size();
		for (int i = 0; i < teamSize; i++) {
			Map<String, Object> userData = (Map<String, Object>) result.get(i);
			Object userId = userData.get(ConstantUtility.USER_ID);
			Object userName = userData.get("name");
			Object expectedHours = userData.get(ConstantUtility.EXPECTED_HOURS);
			Map<String, Object> user = new HashMap<>();
			user.put(ConstantUtility.USER_ID, userId);
			user.put("name", userName);
			for (int j = 0; j < 2; j++) {
				Map<String, Object> userDatas = new HashMap<>();
				userDatas.put(ConstantUtility.EXPECTED_HOURS, Double.parseDouble(expectedHours.toString()));
				userDatas.put("userSalary", 8900);
				if (j == 0) {
					user.put("today", userDatas);
				} else
					user.put("yesterday", userDatas);
			}
			userList.add(user);
		}
		}
		return userList;
	}

	
	@Override
	public Map<String, Object> getIndirectCostForSalaryReconciliation(String accessToken, int month, int year,
			String userStatus, String businessVertical, List<Map<String,Object>> data) {
		int listSize = data.size();
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		Map<String, Double> buCosts = null;
		Double hourlyIndirectCost = 0D;
		Double totalCost = 0D;
		Months monthEnum = Months.valueOf(monthName.toUpperCase());
		IndirectCost indirectCost = costRepository.findByYearAndIsDeletedAndMonth(String.valueOf(year), false, monthEnum);
		Boolean isGradeWise=false;
		MarginBasis marginBasis=marginBasisRepository.findByMonthAndYear(month, year);
		if(marginBasis!=null)
			isGradeWise = marginBasis.getIsGradeWise();
		if(indirectCost!=null) {
		Map<String, Object> companyExpectedHours = feignLegacyInterface.getCompanyExpectedHours(accessToken,month, indirectCost.getYear());
		Map<String, Object> workingDaysData = (Map<String, Object>)feignLegacyInterface.getpayrollWorkingDays( accessToken,month, Integer.parseInt(String.valueOf(year))).get("data");
		Object expectedHours = companyExpectedHours.get(ConstantUtility.DATA);
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		Map<String,Double> gradeWiseCosts=null;
		Double buCost=0.0;
		if(isGradeWise) {
			gradeWiseCosts=utilService.getGradeWiseCostsV2(month, year, accessToken, "","");
			if(businessVertical==null || businessVertical.equals(""))
				buCosts = getBUCostForGradewise(accessToken, month, year,indirectCost, expectedHours, workingDays, gradeWiseCosts);
			else {
				buCost = indirectCostService.buIndirectCostV3(accessToken, businessVertical, month, Integer.toString(year),workingDays,gradeWiseCosts);
			}
		}
		else {
			hourlyIndirectCost = indirectCostService.getIndirectCostHourlyV2(accessToken, String.valueOf(year),
						businessVertical, month, indirectCost, expectedHours, workingDays);
		}
		List<ProjectInvoice> totalInvoiceList = projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndIsInternal(
						monthName, Integer.toString(year), false, false);

		for (int i = 0; i < listSize; i++) {
			Map<String, Object> user = loginUtilityService.objectToMapConverter(data.get(i));
			String grade= user.get(ConstantUtility.GRADE).toString();
			List<Object> projectsList = loginUtilityService.objectToListConverter(user.get(ConstantUtility.PROJECT_WISE_HOURS));
			int projectsListSize = projectsList.size();
			String previousBU = "";
			double userIC = 0;
			for (int j = 0; j < projectsListSize; j++) {
				Map<String, Object> project = loginUtilityService.objectToMapConverter(projectsList.get(j));
				Long projectId = new Long(project.get(ConstantUtility.PROJECT_ID).toString());
				String bu = project.get(ConstantUtility.BUSINESS_VERTICAL).toString();
				List<ProjectInvoice> invoices = totalInvoiceList.stream().filter(inv -> inv.getProjectId().equals(projectId)).collect(Collectors.toList());
				Double invoiceAmount = 0D;
				for (ProjectInvoice invoice : invoices) {
					invoiceAmount = invoiceAmount + invoice.getAmountInDollar();
				}
				if(isGradeWise)
					hourlyIndirectCost= (gradeWiseCosts.get(grade)) / ((Double.parseDouble(workingDays.toString())) * 8);
					
				Double expHours=Double.parseDouble(project.get(ConstantUtility.EXPECTED_HOURS).toString());
				String mins=expHours.toString().split("\\.")[1];
				if(Double.parseDouble(mins)!=0D) {
					expHours = Double.parseDouble(project.get(ConstantUtility.EXPECTED_HOURS).toString().split("\\.")[0]);
					expHours= expHours+(Double.parseDouble(mins)/60);
				}
				double projectCost = hourlyIndirectCost * expHours;
				userIC = userIC + projectCost;

				project.put("projectIc", Math.round(projectCost * 100.00) / 100.00);
				if (Objects.nonNull(bu) )
					previousBU = bu;
			}
			totalCost = totalCost + userIC;
			user.put("userIC", Math.round(userIC * 100.00) / 100.00);
		}
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("indirecCosts", data);
		map.put("verificationkey", 0D);
		map.put("totalIndirectCost", totalCost);
		return map;
	}
	
	@Override
	public Map<String, Object> getIndirectCostForecast(String accessToken, int month, int year,
			String userStatus, String businessVertical, List<Map<String,Object>> data) {
		int listSize = data.size();
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		Map<String, Double> buCosts = null;
		Double hourlyIndirectCost = 0D;
		Double totalCost = 0D;
		Months monthEnum = Months.valueOf(monthName.toUpperCase());
		IndirectCost indirectCost = costRepository.findByYearAndIsDeletedAndMonth(String.valueOf(year), false, monthEnum);
		Boolean isGradeWise=false;
		MarginBasis marginBasis=marginBasisRepository.findByMonthAndYear(month, year);
		if(marginBasis!=null)
			isGradeWise = marginBasis.getIsGradeWise();
//		if(indirectCost!=null) {
		Map<String, Object> companyExpectedHours = feignLegacyInterface.getCompanyExpectedHours(accessToken,month, String.valueOf(year));
		Map<String, Object> workingDaysData = (Map<String, Object>)feignLegacyInterface.getpayrollWorkingDays( accessToken,month, Integer.parseInt(String.valueOf(year))).get("data");
		Object expectedHours = companyExpectedHours.get(ConstantUtility.DATA);
		Object workingDays = workingDaysData.get(ConstantUtility.WORKING_DAYS);
		Map<String,Double> gradeWiseCosts=null;
		Double buCost=0.0;
		if(isGradeWise) {
			gradeWiseCosts=utilService.getGradeWiseCostsV2(month, year, accessToken, "","Forecast");
//			if(businessVertical==null || businessVertical.equals(""))
//				buCosts = getBUCostForGradewise(accessToken, month, year,indirectCost, expectedHours, workingDays, gradeWiseCosts);
//			else {
//				buCost = indirectCostService.buIndirectCostV3(accessToken, businessVertical, month, Integer.toString(year),workingDays,gradeWiseCosts);
//				//hourlyIndirectCost = Math.round(buCost / (Double.parseDouble(workingDays.toString()) * 8) * 100.0) / 100.0;
//			}
		}
		else {
			hourlyIndirectCost = indirectCostService.getIndirectCostHourlyV2(accessToken, String.valueOf(year),
						businessVertical, month, indirectCost, expectedHours, workingDays);
		}
		List<ProjectInvoice> totalInvoiceList = projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndIsInternal(
						monthName, Integer.toString(year), false, false);

		for (int i = 0; i < listSize; i++) {
			Map<String, Object> user = loginUtilityService.objectToMapConverter(data.get(i));
			String grade= user.get(ConstantUtility.GRADE).toString();
			List<Object> projectsList = loginUtilityService.objectToListConverter(user.get(ConstantUtility.PROJECT_WISE_HOURS));
			int projectsListSize = projectsList.size();
			String previousBU = "";
			double userIC = 0;
			for (int j = 0; j < projectsListSize; j++) {
				Map<String, Object> project = loginUtilityService.objectToMapConverter(projectsList.get(j));
				Long projectId = new Long(project.get(ConstantUtility.PROJECT_ID).toString());
				String bu = project.get(ConstantUtility.BUSINESS_VERTICAL).toString();
				List<ProjectInvoice> invoices = totalInvoiceList.stream().filter(inv -> inv.getProjectId().equals(projectId)).collect(Collectors.toList());
				Double invoiceAmount = 0D;
				for (ProjectInvoice invoice : invoices) {
					invoiceAmount = invoiceAmount + invoice.getAmountInDollar();
				}
				if(isGradeWise)
					hourlyIndirectCost= (gradeWiseCosts.get(grade)) / ((Double.parseDouble(workingDays.toString())) * 8);
				Double expHours=project.get(ConstantUtility.FORECASTED_ACCOUNTING_HOURS)!=null ? Double.parseDouble(project.get(ConstantUtility.FORECASTED_ACCOUNTING_HOURS).toString()) : 0.0D;
// 				Double expHours=Double.parseDouble(project.get(ConstantUtility.FORECASTED_HOURS).toString());


				String mins=expHours.toString().split("\\.")[1];
				if(Double.parseDouble(mins)!=0D) {
					expHours = project.get(ConstantUtility.FORECASTED_ACCOUNTING_HOURS)!=null ? Double.parseDouble(project.get(ConstantUtility.FORECASTED_ACCOUNTING_HOURS).toString().split("\\.")[0]) : 0.0D;
					expHours= expHours+(Double.parseDouble(mins)/60);
				}
				double projectCost = hourlyIndirectCost * expHours;
				userIC = userIC + projectCost;
				log.info("hourlyIndirectCost.."+hourlyIndirectCost);
				log.info("expHours.."+expHours);
				log.info("projectCost.."+projectCost);
				project.put("projectIc", Math.round(projectCost * 100.00) / 100.00);
				if (Objects.nonNull(bu) )
					previousBU = bu;
			}
			totalCost = totalCost + userIC;
			user.put("userIC", Math.round(userIC * 100.00) / 100.00);
		}
//		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("indirecCosts", data);
		map.put("verificationkey", 0D);
		map.put("totalIndirectCost", totalCost);
		return map;
	}

	@Override
	@CacheEvict(cacheNames = "salaryReconciliationUsers", allEntries = true)
	public void flushUsersCache() {
	}

	private Map<String, Double> getBUCosts(String accessToken, int month, int year,IndirectCost indirectCost,Object expectedHours, Object workingDays) {
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
		Map<String, Double> costs = new HashMap<>();
		for (Object bu : buList) {
			Double hourlyCost = indirectCostService.getIndirectCostHourlyV2(accessToken, String.valueOf(year),
					bu.toString(), month,indirectCost, expectedHours, workingDays);
			costs.put(bu.toString(), hourlyCost);
		}
		Double hourlyCost = indirectCostService.getIndirectCostHourlyV2(accessToken, String.valueOf(year), "", month,indirectCost, expectedHours, workingDays);
		costs.put("", hourlyCost);
		return costs;
	}
	
	private Map<String, Double> getBUCostForGradewise(String accessToken, int month, int year,IndirectCost indirectCost,Object expectedHours, Object workingDays, Map<String, Double> gradeWiseCosts) {
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
		Map<String, Double> costs = new HashMap<>();
		for (Object bu : buList) {
			Double buCost = indirectCostService.buIndirectCostV3(accessToken, bu.toString(), month, Integer.toString(year),workingDays,gradeWiseCosts);
			costs.put(bu.toString(), buCost);
		}
		return costs;
	}

	@Override
	public List<Object> getAverageBilling(String accessToken, AverageBillingRateDto averageBillingRateDto, String source) {
		Map<String,Object> projectsListData = new HashMap<>();
		if(source.equals("Token")) {
			projectsListData = (Map<String, Object>) legacyInterface.getProjectsForInvoicePipeline(averageBillingRateDto.getMonth(), averageBillingRateDto.getYear(), "External");
		}
		else {
		    projectsListData = (Map<String, Object>) legacyInterface.getProjectsForInvoicePipeline(averageBillingRateDto.getMonth(), averageBillingRateDto.getYear(), "All");
		}
		List<Map<String,Object>> projectsList = new ArrayList<>();
		if(projectsListData != null)
			projectsList = (List<Map<String, Object>>) projectsListData.get("data");
		List<Object> filteredProjectList = new ArrayList<>();
		int projectListSize = projectsList.size();
		String monthName = new DateFormatSymbols().getMonths()[averageBillingRateDto.getMonth() - 1].toString();
		Map<String, Double> billingRateMap = invoicePipelineService.getBillingRates(averageBillingRateDto.getMonth(), averageBillingRateDto.getYear());
		List<String> accessibleBu = new ArrayList<>();
		if(source.equals("Token"))
			accessibleBu = accessUtilityService.buAccess(accessToken);
		else
			accessibleBu.add("All");
		List<ProjectInvoice> invoiceList = projectInvoiceRepository
				.findAllByMonthAndYearAndIsDeletedAndInvoiceStatusNot(monthName, Integer.toString(averageBillingRateDto.getYear()), false, 6L);
		
		List<PaymentMode> paymentModeList = paymentModeRepository.findAll();
		for (int i = 0; i < projectListSize; i++) {
			Map<String, Object> map = (Map<String, Object>) projectsList.get(i);
			Long projectId = new Long(map.get(ConstantUtility.PROJECT_ID).toString());
			Double expectedHours = new Double(map.get(ConstantUtility.RESOURCING_HOURS).toString());
			Double invoiceAmount = 0D;
			Double totalInvoiceAmt =0D;
			String bu = map.get(ConstantUtility.BUSINESS_VERTICAL).toString();
			List<ProjectInvoice> invoices = invoiceList.stream().filter(inv->inv.getProjectId().toString().equals(projectId.toString())).collect(Collectors.toList());
			if (!invoices.isEmpty())
				invoiceAmount = invoices.stream().collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar));
			map.put(ConstantUtility.INVOICE_AMOUNT, invoiceAmount);
			
			map.put("comments", getAvgBillingComplianceComment(projectId, averageBillingRateDto.getMonth(), averageBillingRateDto.getYear()));
			double averageBillingRate = 0;
			if (invoiceAmount != 0D && expectedHours != 0D)
				averageBillingRate = Math.round(invoiceAmount / expectedHours);
			map.put(ConstantUtility.AVERAGE_BILLING_RATE, averageBillingRate);
			if(map.get(ConstantUtility.PAYMENT_MODE)!=null) {
				List<PaymentMode> filteredList = paymentModeList.stream().filter(pay->pay.getId().toString().equals(map.get(ConstantUtility.PAYMENT_MODE).toString())).collect(Collectors.toList());
				PaymentMode paymentMode = null;
				if(!filteredList.isEmpty())
					paymentMode = filteredList.get(0);
				if(paymentMode!=null)
					map.put(ConstantUtility.PAYMENT_MODE, paymentMode.getPaymentModeType());
			}
			else {
				map.put(ConstantUtility.PAYMENT_MODE, "NA");
			}
			List<Object> teamList = (List<Object>) map.get("team");
			double expectedBilling = getExpectedBillingRate(teamList, billingRateMap, ConstantUtility.BILLING_COMPLIANCE);
			map.put(ConstantUtility.EXPECTED_BILLING, expectedBilling);
			double expectedBillingRate = 0;

			if (expectedBilling != 0 && expectedHours != 0)
				expectedBillingRate = expectedBilling / expectedHours;
			map.put(ConstantUtility.EXPECTED_BILLING_RATE, expectedBillingRate);
			boolean filterDifference = false;
			
			if (expectedBillingRate != 0)
			{
				filterDifference = getDifferenceFilteredResult(averageBillingRateDto.getDifferencePercFilter(),
						Math.round((((averageBillingRate - expectedBillingRate) / expectedBillingRate) * 100) * 100.00) / 100.00);
				map.put("billingDifferencePerc", Math.round((((averageBillingRate - expectedBillingRate) / expectedBillingRate) * 100) * 100.00) / 100.00);
			
			}else {
				map.put("billingDifferencePerc", 0.0);
				filterDifference = getDifferenceFilteredResult(averageBillingRateDto.getDifferencePercFilter(),0.00);
			}
			boolean filterResourcingHours = gethoursFilteredResult(averageBillingRateDto.getResourceHourFilter(), expectedHours);
			boolean filterResult = getAverageFilteredResult(Math.round(invoiceAmount / expectedHours), averageBillingRateDto.getBillingRateFilter());
			if (filterResult && filterResourcingHours && filterDifference) {
				if (accessibleBu.contains(bu) || accessibleBu.contains("All"))
					filteredProjectList.add(map);
			}
		}
		return filteredProjectList;
	}

	@Override
	public List<Map<String,Object>> getLifetimeBilling(String accessToken, AverageBillingRateDto averageBillingRateDto, String source){
		Map<String,Object> projectsListData = new HashMap<>();
		if(source.equals("Token")) {
			projectsListData = (Map<String, Object>) legacyInterface.getLifetimeProjectDetails(accessToken,"External",averageBillingRateDto.getYear(),averageBillingRateDto.getMonth());
		}
		else {
		    projectsListData = (Map<String, Object>) legacyInterface.getLifetimeProjectDetails(accessToken, "All",averageBillingRateDto.getYear(),averageBillingRateDto.getMonth()+1);
		}
		List<Map<String,Object>> projectsList = new ArrayList<>();
		if(projectsListData != null)
			projectsList = (List<Map<String, Object>>) projectsListData.get("data");
			Calendar startCal = Calendar.getInstance();
			startCal.set(Calendar.YEAR, 2021);
			startCal.set(Calendar.MONTH, Calendar.JANUARY);
			startCal.set(Calendar.DAY_OF_MONTH, 15);
			startCal.set(Calendar.MILLISECOND, 0);
			startCal.set(Calendar.SECOND, 0);
			startCal.set(Calendar.MINUTE, 0);
			startCal.set(Calendar.HOUR_OF_DAY, 0);
			Calendar endCal = Calendar.getInstance();
			endCal.set(Calendar.YEAR, averageBillingRateDto.getYear());
			endCal.set(Calendar.MONTH, averageBillingRateDto.getMonth());
			endCal.set(Calendar.DAY_OF_MONTH, 15);
			endCal.set(Calendar.MILLISECOND, 0);
			endCal.set(Calendar.SECOND, 0);
			endCal.set(Calendar.MINUTE, 0);
			endCal.set(Calendar.HOUR_OF_DAY, 0);
			Map<Date,Map<String, Double>> billingRateMapTotal = new HashMap<>();
			do{
				Map<String, Double> billingRateMap = invoicePipelineService.getBillingRates(startCal.get(Calendar.MONTH)+1, startCal.get(Calendar.YEAR));
				billingRateMapTotal.put(startCal.getTime(), billingRateMap);
				startCal.add(Calendar.MONTH, 1);

			}while(startCal.getTime().before(endCal.getTime()) && !startCal.getTime().equals(endCal.getTime()));

			


		List<Map<String,Object>> filteredProjectList = new ArrayList<>();
		int projectListSize = projectsList.size();
		List<ProjectInvoice> totalProjectInvoice = projectInvoiceRepository.findAllByIsDeletedAndInvoiceStatusNot(false,6L);
		List<ProjectInvoice> totalInvoiceList = totalProjectInvoice.stream().filter(inv->
		 Integer.parseInt(inv.getYear()) < averageBillingRateDto.getYear() || ((Integer.valueOf(inv.getYear()).equals(averageBillingRateDto.getYear())) 
		 && (loginUtilityService.getMonthNumber( inv.getMonth()) <= Integer.valueOf(averageBillingRateDto.getMonth())))).collect(Collectors.toList());
		
		for (int i = 0; i < projectListSize; i++) {
			Map<String, Object> innerMap = new HashMap<>();
			Map<String, Object> map = (Map<String, Object>) projectsList.get(i);
			Long projectId = new Long(map.get(ConstantUtility.PROJECT_ID).toString());
			Double expectedHours = new Double(map.get(ConstantUtility.RESOURCING_HOURS).toString());
			Double totalInvoiceAmt =0D;
			Calendar projectStartCal = Calendar.getInstance();
			if(map.get("startDate") != null){
				Date projectStDate =new Date( StringtoDateConvert(map.get("startDate").toString(),"yyyy-MM-dd hh:mm:ss.SSS"));
				projectStartCal.setTime(projectStDate);

			}
			
			projectStartCal.set(Calendar.DAY_OF_MONTH, 15);
			projectStartCal.set(Calendar.MILLISECOND, 0);
			projectStartCal.set(Calendar.SECOND, 0);
			projectStartCal.set(Calendar.MINUTE, 0);
			projectStartCal.set(Calendar.HOUR_OF_DAY, 0);
			

			List<ProjectInvoice> totalInvoiceByProject = totalInvoiceList.stream().filter(inv->inv.getProjectId().toString().equals(projectId.toString())).collect(Collectors.toList());
			if (Objects.nonNull(totalInvoiceByProject) && !totalInvoiceByProject.isEmpty())
				totalInvoiceAmt = totalInvoiceByProject.stream().collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar));
			//map.put(ConstantUtility.TOTAL_INVOICE_AMOUNT, totalInvoiceAmt);
			Double averageBillingRate = 0D;
			if (totalInvoiceAmt != 0D && expectedHours != 0D)
				averageBillingRate = totalInvoiceAmt / expectedHours;
			innerMap.put("LifetimeAverageBillingRate", Math.round( averageBillingRate*100.00)/100.00);
			innerMap.put("projectId", projectId);
			innerMap.put("totalInvoiceAmt", Math.round(totalInvoiceAmt*100.0)/100.0);
			innerMap.put("resourcingHours", Math.round(expectedHours*100.0)/100.0);
			
			Map<String,List<Object>> dateTeamMap = (Map<String,List<Object>>) map.get("team");
			
			
			Map<String, List<Object>> updatedDateMap = new HashMap<>();
			for(Map.Entry<String,List<Object>> entry : dateTeamMap.entrySet()){
				
				Calendar tempCal = Calendar.getInstance();
					SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);

        		try {
            		Date tempDate = sdf.parse(entry.getKey().toString());
					tempCal.setTime(tempDate);

        		} catch (ParseException e) {
           			e.printStackTrace();
        		}

					
				
				tempCal.set(Calendar.MILLISECOND, 0);
				tempCal.set(Calendar.SECOND, 0);
				tempCal.set(Calendar.MINUTE, 0);
				tempCal.set(Calendar.HOUR_OF_DAY, 0);
				Date newDate = tempCal.getTime();
				updatedDateMap.put(newDate.toString(), entry.getValue());
				
			}
			
			Double expectedBillingTotal = 0D;
			for(Map.Entry<Date,Map<String,Double>> entry : billingRateMapTotal.entrySet() ){
				if(entry.getKey().equals(projectStartCal.getTime()) || entry.getKey().after(projectStartCal.getTime()))
					expectedBillingTotal = expectedBillingTotal + getExpectedBillingRate(updatedDateMap.get(entry.getKey().toString()), entry.getValue(), ConstantUtility.BILLING_COMPLIANCE);


			}
			innerMap.put("totalExpectedBilling", Math.round(expectedBillingTotal*100.0)/100.0);
			double expectedBillingRate = 0;

			if (expectedBillingTotal != 0 && expectedHours != 0)
				expectedBillingRate = expectedBillingTotal / expectedHours;
			innerMap.put("LifetimeExpectedBillingRate",  Math.round(expectedBillingRate*100.0)/100.0);
			filteredProjectList.add(innerMap);
			
		}
		return filteredProjectList;
		
	
	}

	private  Date setToMidnight(Date date) {
        // Convert Date to LocalDate
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate localDate = instant.atZone(zoneId).toLocalDate();

        // Set time to midnight (00:00:00)
        LocalDateTime midnight = localDate.atStartOfDay();

        // Convert LocalDateTime back to Date
        return Date.from(midnight.atZone(zoneId).toInstant());
    }

	public Long StringtoDateConvert(String toDate,String format) {
		Long parseDate=0l;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(format);
			
			String s[]=toDate.split("T");
			if(s.length==2)
			parseDate= dateFormat.parse(s[0] +" "+s[1].substring(0, s[1].length()-5)).getTime();
			else
			parseDate= dateFormat.parse(s[0]).getTime();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return parseDate;
	}



	private boolean gethoursFilteredResult(List<String> resourceHourFilter, Double expectedHours) {
		if (resourceHourFilter.isEmpty())
			return true;
		else if (resourceHourFilter.contains("0 to 50")) {
			if (expectedHours < 50)
				return true;
		} else if (resourceHourFilter.contains("50 to 100")) {
			if (expectedHours >= 50 && expectedHours < 100)
				return true;
		} else if (resourceHourFilter.contains("More than 100")) {
			if (expectedHours >= 100)
				return true;
		} else
			return true;
		return false;
	}

	private boolean getDifferenceFilteredResult(List<String> differencePercFilter, Double differencePerc) {
		if (differencePercFilter.isEmpty())
			return true;
		else if (differencePercFilter.contains("Greater than 0")) {
			if (differencePerc > 0)
				return true;
		} else if (differencePercFilter.contains("-30 to 0")) {
			if (differencePerc <= 0 && differencePerc > -30)
				return true;
		} else if (differencePercFilter.contains("-30 to -60")) {
			if (differencePerc <= -30 && differencePerc > -60)
				return true;
		} else if (differencePercFilter.contains("-60 to -90")) {
			if (differencePerc <= -60 && differencePerc > -90)
				return true;
		} else if (differencePercFilter.contains("Above -90")) {
			if (differencePerc <= -90)
				return true;
		} else
			return false;
		return false;
	}

	private String getAvgBillingComplianceComment(Long projectId, int month, int year) {
		return averageBillingRateComplRepo.findByProjectIdAndMonthAndYear(projectId, month, year)
				.map(AverageBillingCompliance::getComments).orElse("");
	}

	public boolean getAverageFilteredResult(Long averageRate, List<String> billingRateFilter) {
		if (billingRateFilter.isEmpty())
			return true;
		else if (billingRateFilter.contains("More than $10")) {
			if (averageRate > 10.0)
				return true;
		} else if (billingRateFilter .contains("Between $8 to $10")) {
			if (averageRate <= 10.0 && averageRate >= 8.0)
				return true;
		} else if (billingRateFilter.contains("Below $8")) {
			if (averageRate < 8.0)
				return true;
		} else
			return true;
		return false;
	}

	public Double getExpectedBillingRate(List<Object> teamList, Map<String, Double> billingRateMap, String source) {
		int teamListSize = 0;
		if(teamList != null)
			teamListSize = teamList.size();
		double expectedBilling = 0;
		for (int j = 0; j < teamListSize; j++) {
			Map<String, Object> teamData = (Map<String, Object>) teamList.get(j);
			String grade = teamData != null
					? teamData.get(ConstantUtility.GRADE) != null ? teamData.get(ConstantUtility.GRADE).toString()
							: "NA"
					: "NA";
			Double expectedHours = 0D;
			if (source.equals(ConstantUtility.BILLING_COMPLIANCE))
				expectedHours = teamData != null ? teamData.get(ConstantUtility.RESOURCING_HOURS) != null
						? Double.parseDouble(teamData.get(ConstantUtility.RESOURCING_HOURS).toString())
						: 0.0 : 0.0;
			else
				expectedHours = teamData != null ? teamData.get(ConstantUtility.FORECASTED_HOURS) != null
						? Double.parseDouble(teamData.get(ConstantUtility.FORECASTED_HOURS).toString())
						: 0.0 : 0.0;

			Double rateValue = billingRateMap.get(grade);
			if (teamData != null) {
				if (rateValue != null) {
					expectedBilling = expectedBilling + (rateValue * expectedHours);
					teamData.put(ConstantUtility.EXPECTED_BILLING_RATE, rateValue);
					teamData.put(ConstantUtility.EXPECTED_BILLING,
							Math.round((rateValue * expectedHours) * 100.00) / 100.00);
				} else {
					teamData.put(ConstantUtility.EXPECTED_BILLING_RATE, 0);
					teamData.put(ConstantUtility.EXPECTED_BILLING, 0);
				}
			}
		}
		return expectedBilling;
	}

	public Boolean sendBillingComplianceMail(String accessToken, Long projectId, int month, int year) {
		Map<String, Object> projectData = (Map<String, Object>) legacyInterface.getProjectDataForComplianceMail(accessToken, month, year,
				projectId).get("data");
		Boolean mailSent = null;
		List<Object> projects = new ArrayList<>();
		if (projectData.containsKey(ConstantUtility.RESOURCING_HOURS)) {
			Map<String, Double> billingRateMap = invoicePipelineService.getBillingRates(month, year);
			List<Object> teamList = (List<Object>) projectData.get("team");
			double expectedBilling = getExpectedBillingRate(teamList, billingRateMap, ConstantUtility.BILLING_COMPLIANCE);
			double expectedHours = new Double(projectData.get(ConstantUtility.RESOURCING_HOURS).toString());
			String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
			List<ProjectInvoice> invoices = projectInvoiceRepository
					.findAllByMonthAndYearAndIsDeletedAndProjectIdAndIsInternal(monthName, Integer.toString(year), false, projectId, false);
			double invoiceAmount = 0;
			if (!invoices.isEmpty())
				invoiceAmount = invoices.stream().collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar));
			double averageBillingRate = 0;
			double expectedBillingRate = 0;
			if (invoiceAmount != 0D && expectedHours != 0D)
				averageBillingRate = Math.round(invoiceAmount / expectedHours);
			if (expectedBilling != 0 && expectedHours != 0)
				expectedBillingRate = expectedBilling / expectedHours;
			expectedBillingRate = 10;
			if (averageBillingRate < expectedBillingRate) {
				mailSent = sendMailForBillingRateCompliance(projectData, month, year, projects);
			} else
				mailSent = true;

		}
		return mailSent;
	}

	private Boolean sendMailForBillingRateCompliance(Map<String, Object> projectData, int month, int year,
			List<Object> projects) {
		if (projectData.containsKey(ConstantUtility.BU_OWNER)) {
			String monthAndYear = Month.of(month).name() + " " + year;
			Context context = new Context();
			String buOwnerMail = projectData.get(ConstantUtility.BU_OWNER_MAIL).toString();
			String buOwner = projectData.get(ConstantUtility.BU_OWNER).toString();
			context.setVariable("userName", buOwner);
			context.setVariable("projects", projects);
			context.setVariable("monthAndYear", monthAndYear);
			context.setVariable("currentYear", LocalDateTime.now().getYear());
			context.setVariable(ConstantUtility.BUSINESS_VERTICAL, projectData.get(ConstantUtility.BUSINESS_VERTICAL));
			Map<String, Object> valueMap = new HashMap<>();
			valueMap.put("userName", projectData.get(ConstantUtility.MANAGER));
			valueMap.put("projectName", projectData.get("projectName"));
			valueMap.put("projects", projects);
			valueMap.put("monthAndYear", monthAndYear);
			String subject = "Billing Rate Compliance || " + monthAndYear + " || "
					+ projectData.get(ConstantUtility.BUSINESS_VERTICAL);
			try {
				String cc[] = { "resourcing@oodles.io" };
				mailService.sendScheduleHtmlMailWithCc(buOwnerMail, subject, context, "Billing-Compliance-Bu", cc);
				return true;
			} catch (Exception e) {
				log.info(e.getStackTrace().toString());
				return false;
			}
		}
		return null;
	}

	public Boolean sendBillingRateMailToHeads(String accessToken, int month, int year, String businessVertical) {
		List<Map<String,Object>> projectsList = (List<Map<String, Object>>) legacyInterface.getProjectsForInvoicePipeline(month, year, "External").get("data");
		int projectListSize = projectsList.size();
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		Map<String, Double> billingRateMap = invoicePipelineService.getBillingRates(month, year);
		List<Object> projects = new ArrayList<>();
		int j = 0;
		String buOwner = "";
		String buOwnerMail = "";
		for (int i = 0; i < projectListSize; i++) {
			Map<String, Object> map = (Map<String, Object>) projectsList.get(i);
			Long projectId = new Long(map.get(ConstantUtility.PROJECT_ID).toString());
			Double expectedHours = new Double(map.get(ConstantUtility.RESOURCING_HOURS).toString());
			Double totalExpectedHours = new Double(map.get(ConstantUtility.TOTAL_RESOURCING_HOURS).toString());
			Double invoiceAmount = 0D;
			Double totalInvoiceAmount = 0D;
			String bu = map.get(ConstantUtility.BUSINESS_VERTICAL).toString();
			if (bu.equals(businessVertical)) {
				if (buOwner.equals("")) {
					buOwner = map.get(ConstantUtility.BU_OWNER).toString();
					buOwnerMail = map.get(ConstantUtility.BU_OWNER_MAIL).toString();
				}
				List<ProjectInvoice> invoiceList = projectInvoiceRepository.findAllByProjectIdAndIsDeletedAndInvoiceStatusNot(projectId, false, 6L);
				List<ProjectInvoice> invoices = invoiceList.stream().filter(inv -> inv.getMonth().equals(monthName) && inv.getYear().equals( Integer.toString(year))).collect(Collectors.toList());
				if (!invoices.isEmpty())
					invoiceAmount = invoices.stream()
							.collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar));
				if(!invoiceList.isEmpty()) {
					totalInvoiceAmount = invoiceList.stream().collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar));
				}
				Map<String, Object> projectMap = new HashMap<>();
				projectMap.put("i", ++j);
				projectMap.put("name", map.get("name"));
				projectMap.put(ConstantUtility.MANAGER, map.get(ConstantUtility.MANAGER));
				projectMap.put("bu", bu);
				projectMap.put("hours", expectedHours);
				if (invoiceAmount != 0D && expectedHours != 0D)
					projectMap.put("avgBilling", "$" + Math.round(invoiceAmount / expectedHours));
				else
					projectMap.put("avgBilling", "$" + 0);
				double expectedBillingRate = getExpectedBillingRate((List<Object>) map.get("team"), billingRateMap, ConstantUtility.BILLING_COMPLIANCE);
				projectMap.put("expBilling", "$" + 0);
				if (expectedBillingRate != 0D && expectedHours != 0D)
					projectMap.put("expBilling",
							"$" + (Math.round((expectedBillingRate / expectedHours) * 100.00) / 100.00));
				if(totalExpectedHours !=0 && totalInvoiceAmount != 0)
					projectMap.put(ConstantUtility.LIFETIME_AVERAGE_BILLING_RATE, "$" + (Math.round((totalInvoiceAmount / totalExpectedHours) * 100.00) / 100.00));
				else
					projectMap.put(ConstantUtility.LIFETIME_AVERAGE_BILLING_RATE, "$" + 0);
				projects.add(projectMap);
			}
		}
		Map<String, Object> projectData = new HashMap<>();
		projectData.put(ConstantUtility.BU_OWNER, buOwner);
		projectData.put(ConstantUtility.BU_OWNER_MAIL, buOwnerMail);
		projectData.put(ConstantUtility.BUSINESS_VERTICAL, businessVertical);
		if (!projects.isEmpty() && !buOwner.equals(""))
			return sendMailForBillingRateCompliance(projectData, month, year, projects);
		else
			return false;
	}
	
	@Override
	public Map<String, Object> getBillingComplianceProjectData(String accessToken, Long projectId, int month, int year) {
		Map<String, Object> proData = (Map<String, Object>)legacyInterface.getProjectDataForComplianceMail(accessToken, month, year,
				projectId);
		Map<String, Object> data=new HashMap<>();
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();

		 if(Objects.nonNull(proData)) {	
			 Map<String, Object> projectData = (Map<String, Object>) proData.get("data");
		if (projectData.containsKey(ConstantUtility.RESOURCING_HOURS)) {
			Map<String, Double> billingRateMap = invoicePipelineService.getBillingRates(month, year);
			List<Object> teamList = (List<Object>) projectData.get("team");
			double expectedBilling = getExpectedBillingRate(teamList, billingRateMap, ConstantUtility.BILLING_COMPLIANCE);
			data.put("team", teamList);
			data.put(ConstantUtility.EXPECTED_BILLING, expectedBilling);
			double expectedHours = new Double(projectData.get(ConstantUtility.RESOURCING_HOURS).toString());
			double totalExpectedHours = new Double(projectData.get(ConstantUtility.TOTAL_RESOURCING_HOURS).toString());
			data.put(ConstantUtility.RESOURCING_HOURS, expectedHours);
			if(projectData.get(ConstantUtility.PAYMENT_MODE)!=null) {
				PaymentMode paymentMode = paymentModeRepository.findById(new Long(projectData.get(ConstantUtility.PAYMENT_MODE).toString()));
				data.put(ConstantUtility.PAYMENT_MODE, paymentMode.getPaymentModeType());
			}
			else
				data.put(ConstantUtility.PAYMENT_MODE, "NA");
			List<ProjectInvoice> invoiceList = projectInvoiceRepository.findAllByProjectIdAndIsDeletedAndInvoiceStatusNot(projectId, false, 6L);
			List<ProjectInvoice> invoices = invoiceList.stream().filter(inv -> inv.getMonth().equals(monthName) && inv.getYear().equals( Integer.toString(year))).collect(Collectors.toList());
			double invoiceAmount = 0;
			double totalInvoiceAmount = 0;
			if(!invoiceList.isEmpty()) {
				totalInvoiceAmount = invoiceList.stream().collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar));
			}
			if (!invoices.isEmpty())
				invoiceAmount = invoices.stream().collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar));
			data.put(ConstantUtility.INVOICE_AMOUNT, invoiceAmount);
			double averageBillingRate = 0;
			double expectedBillingRate = 0;
			double lifetimeAverageBillingRate = 0;
			if (invoiceAmount != 0D && expectedHours != 0D)
				averageBillingRate = Math.round(invoiceAmount / expectedHours);
			if (expectedBilling != 0 && expectedHours != 0)
				expectedBillingRate = expectedBilling / expectedHours;
			if(totalExpectedHours !=0 && totalInvoiceAmount != 0)
				lifetimeAverageBillingRate = Math.round((totalInvoiceAmount / totalExpectedHours) *100.00)/100.00 ;
			data.put(ConstantUtility.AVERAGE_BILLING_RATE, averageBillingRate);
			data.put(ConstantUtility.EXPECTED_BILLING_RATE, expectedBillingRate);
			data.put(ConstantUtility.LIFETIME_AVERAGE_BILLING_RATE, lifetimeAverageBillingRate);
			double differencePerc=0;
			if(expectedBillingRate!=0)
				differencePerc=((averageBillingRate - expectedBillingRate) / expectedBillingRate) * 100;
			data.put(ConstantUtility.DIFFERENCE_PERC, Math.round(differencePerc*100.00)/100.00);
			BillingCompliancePercentage compliancePercObj=compliancePercRepo.findByIsArchive(false);
			double compliancePerc=0;
			if(compliancePercObj!=null) {
				compliancePerc=compliancePercObj.getCompliancePerc();
			}
			data.put(ConstantUtility.COMPLIANT_PERC, compliancePerc);
			if(expectedHours!=0) {
				if (differencePerc < compliancePerc) 
					data.put(ConstantUtility.COMPLIANT_SMALL_STRING, false);
				else
					data.put(ConstantUtility.COMPLIANT_SMALL_STRING, true);
			}
			else
				data.put(ConstantUtility.COMPLIANT_SMALL_STRING, true);
				data.put(ConstantUtility.MONTH, monthName);
			return data;
		}
		 }
		 else{
			data.put(ConstantUtility.EXPECTED_BILLING, 0D);
			data.put(ConstantUtility.RESOURCING_HOURS, 0D);
			data.put(ConstantUtility.PAYMENT_MODE, "NA");
			data.put(ConstantUtility.INVOICE_AMOUNT, 0D);
			data.put(ConstantUtility.AVERAGE_BILLING_RATE, 0D);
			data.put(ConstantUtility.EXPECTED_BILLING_RATE, 0D);
			data.put(ConstantUtility.DIFFERENCE_PERC, 0D);
			data.put(ConstantUtility.COMPLIANT_PERC, 0D);
			data.put(ConstantUtility.MONTH, monthName);
			data.put(ConstantUtility.COMPLIANT_SMALL_STRING, true);
			data.put("team", null);

		 }
		return data;
	}

	@Override
	public List<Map<String, Object>> getYearlyBillingComplianceProjectData(String accessToken, Long projectId, int year) {
		Map<String, Object> proDataMap = (Map<String, Object>)legacyInterface.getYearlyProjectDataForComplianceMail(accessToken, year,
		projectId);
		List<Map<String, Object>> res = new ArrayList<>();
		List<ProjectInvoice> invoicesList = projectInvoiceRepository
							.findAllByYearAndIsDeletedAndProjectIdAndInvoiceStatusNot(Integer.toString(year), false, projectId, 6L);
		 if (Objects.nonNull(proDataMap)) {
			Map<Integer, Object> projectDataMap = loginUtilityService.objectToIntMapConverter(proDataMap
			.get("data"));

			for (Map.Entry<Integer, Object> entry : projectDataMap.entrySet()) {
				Map<String, Object> data=new HashMap<>();

				Map<String, Object> projectData = loginUtilityService.objectToMapConverter( entry.getValue());
				int month = entry.getKey();

				if (projectData.containsKey(ConstantUtility.RESOURCING_HOURS)) {
					Map<String, Double> billingRateMap = invoicePipelineService.getBillingRates(month, year);
					List<Object> teamList = (List<Object>) projectData.get("team");
					double expectedBilling = getExpectedBillingRate(teamList, billingRateMap, ConstantUtility.BILLING_COMPLIANCE);
					data.put("team", teamList);
					data.put(ConstantUtility.EXPECTED_BILLING, expectedBilling);
					double expectedHours = new Double(projectData.get(ConstantUtility.RESOURCING_HOURS).toString());
					data.put(ConstantUtility.RESOURCING_HOURS, expectedHours);
					if (projectData.get(ConstantUtility.PAYMENT_MODE) != null) {
						PaymentMode paymentMode = paymentModeRepository
								.findById(new Long(projectData.get(ConstantUtility.PAYMENT_MODE).toString()));
						data.put(ConstantUtility.PAYMENT_MODE, paymentMode.getPaymentModeType());
					} else
						data.put(ConstantUtility.PAYMENT_MODE, "NA");
					String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
					List<ProjectInvoice> invoices = invoicesList.stream().filter(inv-> inv.getMonth().equals(monthName)).collect(Collectors.toList());
					double invoiceAmount = 0;
					if (!invoices.isEmpty())
						invoiceAmount = invoices.stream()
								.collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar));
					data.put(ConstantUtility.INVOICE_AMOUNT, invoiceAmount);
					double averageBillingRate = 0;
					double expectedBillingRate = 0;
					if (invoiceAmount != 0D && expectedHours != 0D)
						averageBillingRate = Math.round(invoiceAmount / expectedHours);
					if (expectedBilling != 0 && expectedHours != 0)
						expectedBillingRate = expectedBilling / expectedHours;
					data.put(ConstantUtility.AVERAGE_BILLING_RATE, averageBillingRate);
					data.put(ConstantUtility.EXPECTED_BILLING_RATE, expectedBillingRate);
					double differencePerc = 0;
					if (expectedBillingRate != 0)
						differencePerc = ((averageBillingRate - expectedBillingRate) / expectedBillingRate) * 100;
					data.put(ConstantUtility.DIFFERENCE_PERC, Math.round(differencePerc * 100.00) / 100.00);
					BillingCompliancePercentage compliancePercObj = compliancePercRepo.findByIsArchive(false);
					double compliancePerc = 0;
					if (compliancePercObj != null) {
						compliancePerc = compliancePercObj.getCompliancePerc();
					}
					data.put(ConstantUtility.COMPLIANT_PERC, compliancePerc);
					if (expectedHours != 0) {
						if (differencePerc < compliancePerc)
							data.put(ConstantUtility.COMPLIANT_SMALL_STRING, false);
						else
							data.put(ConstantUtility.COMPLIANT_SMALL_STRING, true);
					} else
						data.put(ConstantUtility.COMPLIANT_SMALL_STRING, true);
					data.put(ConstantUtility.MONTH, monthName);
					res.add(data);
				}
			}
		}
		return res;
	}

}
