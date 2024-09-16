package com.krishna.schedulers;

import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.DateUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.krishna.Interfaces.IConsolidatedService;
import com.krishna.controller.FeignLegacyInterface;
import com.krishna.domain.ExpectedBillingRate;
import com.krishna.domain.GradeBasedIndirectCost;
import com.krishna.domain.IndirectCost;
import com.krishna.domain.Margin.MarginBasis;
import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.enums.Months;
import com.krishna.repository.ExpectedBillingRepository;
import com.krishna.repository.GradeBasedIndirectCostRepository;
import com.krishna.repository.IndirectCostRepository;
import com.krishna.repository.MarginBasisRepository;
import com.krishna.repository.invoice.ProjectInvoiceRepository;
import com.krishna.service.BuReserveServiceImpl;
import com.krishna.service.IndirectCostService;
import com.krishna.service.MailService;
import com.krishna.service.ProjectInvoiceService;
import com.krishna.service.ProjectMarginService;
import com.krishna.service.util.ConsolidatedService;
import com.krishna.util.ConstantUtility;

@Component
public class Scheduler {
	
	@Autowired
	BuReserveServiceImpl buReserveServiceImpl;
	
	@Autowired
	FeignLegacyInterface feignLegacyInterface;

	@Autowired
	ProjectInvoiceRepository projectInvoiceRepository;
	
	@Autowired
	MailService mailService;
	
	@Autowired
	ProjectMarginService projectMarginService;
	
	@Autowired
	ProjectInvoiceService projectInvoiceService;
	
	@Autowired
	GradeBasedIndirectCostRepository gradeBasedIcRepo;
	
	@Autowired ExpectedBillingRepository expectedBillingRepo;
	
	@Autowired
	IConsolidatedService consolidateService;
	
	@Autowired
	MarginBasisRepository marginBasisRepository;
	
	@Autowired
	IndirectCostService indirectCostService;
	
	private static Logger log = LoggerFactory.getLogger(Scheduler.class);
	
	@Autowired
	IndirectCostRepository costRepository;
	

	

//	@Scheduled(cron = "0 0 2 * * MON",zone="IST")
	//@Scheduled(cron = "0 0 2 25 * ?",zone="IST")
	public void accountsCompliantStatusChange(String accessToken) {
		try {
		LocalDate today = LocalDate.now();
		int previousMonth = LocalDate.now().minusMonths(1).getMonthValue();
		String monthName = new DateFormatSymbols().getMonths()[previousMonth-1].toString();
		Months monthEnum = Months.valueOf(monthName.toUpperCase());
		IndirectCost monthIndirectCost = costRepository.findByYearAndIsDeletedAndMonth(Integer.toString(today.getYear()),
				false, monthEnum);
		
		
		int thisYear = (today.getMonthValue() == 1) ? today.minusYears(1).getYear() : today.getYear();

		List<ProjectInvoice> allInvoiceList = projectInvoiceRepository.findAllByIsDeletedAndIsInternal(false, false);
		List<ProjectInvoice> invoices = allInvoiceList.stream().filter(inv -> inv.getMonth().equals(monthName) && inv.getYear().equals(String.valueOf(thisYear))).collect(Collectors.toList());
		List<Long> projectIdList = invoices.stream().mapToLong(invoice -> invoice.getProjectId()).boxed().collect(Collectors.toList());
		HashSet<Long> uniqueProjectIdList = new HashSet<Long>(projectIdList);
		for(Long projectId : uniqueProjectIdList) {
			String comment=null;
			String compliantType=null;
			String data=null;
			List<ProjectInvoice> allInvoices = allInvoiceList.stream().filter(inv -> inv.getProjectId().equals(projectId)).collect(Collectors.toList());
			List<ProjectInvoice> unpaidOrDueInvoices = this.checkPendingOrUnPaidInvoices(projectId, monthName, thisYear,allInvoices);
			if(!unpaidOrDueInvoices.isEmpty()) {
				String invoiceIds="";
				for(ProjectInvoice inv:unpaidOrDueInvoices) {
					invoiceIds=invoiceIds+inv.getId()+",";
				}
				log.info("Project Id {} has been marked as account compliant due to unpaid/Due invoices {} : ", projectId, invoiceIds);
				comment="Marked non compliant by the system for Payment_Issues  as the invoices("+invoiceIds+") is/are Pending.";
				compliantType="Payment_Issues";
				data= invoiceIds;
				feignLegacyInterface.accountsCompliantStatusChange(projectId, comment, false, compliantType, accessToken, data, "Cron");
			}
			else {
					if (monthIndirectCost != null) {
						Double margin = (Double) projectMarginService.getDirectCost(projectId, previousMonth, thisYear, accessToken, true).get("marginPercentage");
						if (margin < 20) {
							log.info("ProjectId {} has been found for Direct Cost Compliant", projectId);
							comment = "Marked non compliant by the system for Margin_issues, as the current margin("+ margin + ") is less than 20%.";
							compliantType = "Margin_Issues";
							data = margin.toString();

							feignLegacyInterface.accountsCompliantStatusChange(projectId, comment, false, compliantType, accessToken, data, "Cron");
						}
					}
					Map<String,Object> averageBillingCompliance=consolidateService.getBillingComplianceProjectData(accessToken, projectId, previousMonth, thisYear);
				Boolean averageBillingNonCompliant=false;
				if(averageBillingCompliance!=null && averageBillingCompliance.containsKey(ConstantUtility.COMPLIANT_SMALL_STRING) )
					averageBillingNonCompliant=new Boolean(averageBillingCompliance.get(ConstantUtility.COMPLIANT_SMALL_STRING).toString());
				if(!averageBillingNonCompliant) {
					if(averageBillingCompliance!=null && averageBillingCompliance .containsKey(ConstantUtility.COMPLIANT_SMALL_STRING)) {
						Double diffPerc=new Double(averageBillingCompliance.get("differencePerc").toString());
						Double compliancePerc=new Double(averageBillingCompliance.get("compliantPerc").toString());
						Double averageBillingRate= new Double(averageBillingCompliance.get("averageBillingRate").toString());
						Double expectedBillingRate = new Double(averageBillingCompliance.get("expectedBillingRate").toString());
						Double lifetimeAverageBillingRate = new Double(averageBillingCompliance.get("lifetimeAverageBillingRate").toString());
						comment="Marked non compliant by the system for Avg_Billing_Rate, as the current billing Rate("+ diffPerc+") is less than "+compliancePerc+".";
						compliantType="Avg_Billing_Rate";
						data = diffPerc.toString()+"!"+(Math.round(averageBillingRate*100.00)/100.00)+"!"+(Math.round(expectedBillingRate*100.00)/100.00)+"!"+(Math.round(lifetimeAverageBillingRate*100.00)/100.00);

						feignLegacyInterface.accountsCompliantStatusChange(projectId, comment, false, compliantType, accessToken, data, "Cron");
					}
				}
				else {
					comment="Marked account's compliant by System.";
					compliantType="";

					feignLegacyInterface.accountsCompliantStatusChange(projectId, comment, true, compliantType, accessToken, data, "Cron");
				}
			}
		}
		
		}catch(Exception e) {
			e.printStackTrace();
			log.debug("accountsCompliantStatusChange Exception : "+e.toString());	
		}
	}

	public List<ProjectInvoice> checkPendingOrUnPaidInvoices(Long projectId, String monthName, int thisYear, List<ProjectInvoice> allInvoices) {
		int monthNum = Month.valueOf(monthName.toUpperCase()).getValue();
		List<ProjectInvoice> pendingOrCrossedDueInvoices = allInvoices.stream()
				.filter(invoice -> ((invoice.getInvoiceStatus() == 1 || invoice.getInvoiceStatus() == 4) && 
						(monthNum<LocalDate.now().getMonthValue() && (Integer.parseInt(invoice.getYear())<=LocalDate.now().getYear()))
						&& (DateUtils.addDays(invoice.getDueDate(), 10).before(new Date()) || DateUtils.addDays(invoice.getDueDate(), 10).equals(new Date()))))
				.collect(Collectors.toList());
		return pendingOrCrossedDueInvoices;
	}

	//@Scheduled(cron = "0 0 5 2 * *")
	public void carryForwardPreviousMonthGIC() {
		try {
		java.time.LocalDateTime now = java.time.LocalDateTime.now().minusMonths(1);
		int month = now.getMonthValue();
		int year = now.getYear();
		java.time.YearMonth yearMonth = java.time.YearMonth.of(year, month);
		yearMonth = yearMonth.minusMonths(1);
		int previousMonthValue = yearMonth.getMonthValue();
		int previousMonthYear = yearMonth.getYear();
		log.info("  carryForwardPreviousMonthGIC cron hit for month = " + previousMonthValue);
		
		List<GradeBasedIndirectCost> lastMonthgbIC = gradeBasedIcRepo.
				findAllByMonthAndYearAndIsVariable(previousMonthValue, previousMonthYear, false);
		lastMonthgbIC.forEach(lastMonthCost -> {
			Optional<GradeBasedIndirectCost> currentMonthEntry  = gradeBasedIcRepo.
					findByGradeAndMonthAndYear(lastMonthCost.getGrade(), month, year);
			if(!currentMonthEntry.isPresent()) {
				GradeBasedIndirectCost currentMonthCost = new GradeBasedIndirectCost(0, lastMonthCost.getGrade(), 
						lastMonthCost.getFixedCost(), month, year, false);
				gradeBasedIcRepo.save(currentMonthCost);
			}
		});
		MarginBasis marginBasis=marginBasisRepository.findByMonthAndYear(month, year);
		if(marginBasis==null) {
			marginBasis = indirectCostService.setMarginBasis(month, year, true);
		}
		else{
			marginBasis.setIsGradeWise(true);
			marginBasis.setIsUniform(false);
		}
		marginBasisRepository.save(marginBasis);
	}catch(Exception e) {
		log.debug("carryForwardPreviousMonthGIC Exception : "+e.toString());	
	}
	}
	
	public void carryForwardPreviousMonthGICForTesting(int month,int year) {
		java.time.YearMonth yearMonth = java.time.YearMonth.of(year, month);
		yearMonth = yearMonth.minusMonths(1);
		int previousMonthValue = yearMonth.getMonthValue();
		int previousMonthYear = yearMonth.getYear();
		List<GradeBasedIndirectCost> lastMonthgbIC = gradeBasedIcRepo.
				findAllByMonthAndYearAndIsVariable(previousMonthValue, previousMonthYear, false);
		lastMonthgbIC.forEach(lastMonthCost -> {
			Optional<GradeBasedIndirectCost> currentMonthEntry  = gradeBasedIcRepo.
					findByGradeAndMonthAndYear(lastMonthCost.getGrade(), month, year);
			if(!currentMonthEntry.isPresent()) {
				GradeBasedIndirectCost currentMonthCost = new GradeBasedIndirectCost(0, lastMonthCost.getGrade(), 
						lastMonthCost.getFixedCost(), month, year, false);
				gradeBasedIcRepo.save(currentMonthCost);
			}
		});
		MarginBasis marginBasis=marginBasisRepository.findByMonthAndYear(month, year);
		if(marginBasis==null) {
			marginBasis = indirectCostService.setMarginBasis(month, year, true);
		}
		else{
			marginBasis.setIsGradeWise(true);
			marginBasis.setIsUniform(false);
		}
		marginBasisRepository.save(marginBasis);
	}

	//@Scheduled(cron = "0 0 15 1 * ?",zone="IST")
	public void carryForwardBillingRate() {
		log.debug("cron is working");
		try {
			log.debug("cron is in try");
		java.time.LocalDateTime now = java.time.LocalDateTime.now();
		int month = now.getMonthValue();
		int year = now.getYear();
		YearMonth yearMonth=YearMonth.of(year, month);
		List<ExpectedBillingRate> currentMonthRates=expectedBillingRepo.findAllByMonthAndYear(month,year);
		List<ExpectedBillingRate> billingRates=expectedBillingRepo.findAllByMonthAndYear(yearMonth.minusMonths(1).getMonthValue(),yearMonth.minusMonths(1).getYear());
		if(!billingRates.isEmpty() && currentMonthRates.isEmpty()) {
			log.debug("cron is in if");
			for(ExpectedBillingRate billingRate:billingRates) {
				log.debug("cron is in for");
				ExpectedBillingRate billingRateObj = new ExpectedBillingRate();
				billingRateObj.setGrade(billingRate.getGrade());
				billingRateObj.setMonth(month);
				billingRateObj.setYear(year);
				billingRateObj.setBillingRate(billingRate.getBillingRate());
				expectedBillingRepo.saveAndFlush(billingRateObj);
				currentMonthRates.add(billingRateObj);
			}
		}
	}catch(Exception e) {
		log.debug("accountsCompliantStatusChange Exception : "+e.toString());	
	}
	}
	
	public void carryForwardBillingRateForTesting(int month,int year) {
		YearMonth yearMonth=YearMonth.of(year, month);
		List<ExpectedBillingRate> currentMonthRates=expectedBillingRepo.findAllByMonthAndYear(month,year);
		List<ExpectedBillingRate> billingRates=expectedBillingRepo.findAllByMonthAndYear(yearMonth.minusMonths(1).getMonthValue(),yearMonth.minusMonths(1).getYear());
		if(!billingRates.isEmpty() && currentMonthRates.isEmpty()) {
			for(ExpectedBillingRate billingRate:billingRates) {
				ExpectedBillingRate billingRateObj = new ExpectedBillingRate();
				billingRateObj.setGrade(billingRate.getGrade());
				billingRateObj.setMonth(month);
				billingRateObj.setYear(year);
				billingRateObj.setBillingRate(billingRate.getBillingRate());
				expectedBillingRepo.saveAndFlush(billingRateObj);
				currentMonthRates.add(billingRateObj);
			}
		}
	}
	
	
	public void carryForwardReservePercentage(@RequestHeader("Authorization") String accessToken) {
		buReserveServiceImpl.carryForwardReservePercentage(accessToken);
	}
	
}
