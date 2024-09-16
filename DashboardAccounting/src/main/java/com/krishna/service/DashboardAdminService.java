package com.krishna.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.krishna.Interfaces.ICompanyMarginService;
import com.krishna.controller.FeignLegacyInterface;
import com.krishna.domain.LeaveCostPercentage;
import com.krishna.domain.PayRegister;
import com.krishna.domain.PayRevisions;
import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.repository.invoice.ProjectInvoiceRepository;
import com.krishna.repository.payroll.LeaveCostPercentageRepository;
import com.krishna.repository.payroll.PayRegisterRepository;
import com.krishna.repository.payroll.PayRevisionRepository;

@Service
public class DashboardAdminService {
	
	@Autowired
	ProjectInvoiceRepository projectInvoiceRepository;
	
	
	@Autowired
	PayRegisterRepository payRegisterRepository;
	
	@Autowired
	PayRevisionRepository payrevisionRepository;
	
	@Autowired
	LeaveCostPercentageRepository leaveCostPercent;
	
	@Autowired
	ICompanyMarginService companyMarginService;
	
	@Autowired
	ProjectMarginService projectMarginService;

	@Autowired
	FeignLegacyInterface feignLegacyInterface;
	
	Logger log=LoggerFactory.getLogger(DashboardAdminService.class);
	


	public List<PayRegister> setAnnualCtc() {
		List<PayRegister> payRegisters=payRegisterRepository.findAllByIsCurrent(true);
		List<LeaveCostPercentage> prevCosts = leaveCostPercent.findAllByIsDeleted(false);
		LeaveCostPercentage leaveCost=prevCosts.get(0);
		payRegisters.forEach(payRegister->{
			double annualCtc = payRegister.getTotalMonthlyPay()*12;
			double salaryExcludeLA = (payRegister.getTotalMonthlyPay() - payRegister.getLaptopAllowance())*12;
			double paidLeaveAmount = (leaveCost.getLeaveCostPercentage() * salaryExcludeLA)/100;
			payRegister.setAnnualCTC(annualCtc);
			payRegister.setTotalAnnualCtc(annualCtc+paidLeaveAmount);
			payRegister.setPaidLeavesAmount(paidLeaveAmount);
			payRegister=payRegisterRepository.saveAndFlush(payRegister);
		});
		List<PayRevisions> payRevisions=payrevisionRepository.findAll();
		if(!payRevisions.isEmpty()) {
		payRevisions.forEach(payrevision->{
			double annualPayrevctc=payrevision.getTotalMonthlyPay()*12;
			double salaryExcludeLA = (payrevision.getTotalMonthlyPay() - payrevision.getLaptopAllowance())*12;
			double paidLeavesPayrevAmount=(leaveCost.getLeaveCostPercentage() * salaryExcludeLA)/100;
			payrevision.setAnnualCTC(annualPayrevctc);
			payrevision.setTotalAnnualCtc(annualPayrevctc+paidLeavesPayrevAmount);
			payrevision=payrevisionRepository.saveAndFlush(payrevision);
			PayRegister payee=payRegisterRepository.findAllById(payrevision.getPayRegister().getId());
			payee.setAnnualCTC(annualPayrevctc);
			payee.setTotalAnnualCtc(annualPayrevctc + paidLeavesPayrevAmount);
			payee.setPaidLeavesAmount(paidLeavesPayrevAmount);
			payee = payRegisterRepository.saveAndFlush(payee);
		});
		}
		return payRegisters;
	}

	public PayRegister setEffectiveDate(int userId, long effectiveDate) {
		PayRegister payRegister=payRegisterRepository.findAllByUserIdAndIsCurrent(userId, true);
		payRegister.setEffectiveDate(new Timestamp(effectiveDate).toLocalDateTime());
		payRegister=payRegisterRepository.save(payRegister);
		return payRegister;
	}
	//@Scheduled(cron = "0 30 3 * * *")
	public String reputCompanyCache(String accessToken) {
		try {
		log.info("------Company Cache Updated--------");
		List<Object> companyProjects = new ArrayList<Object>();
		List<Object> buMarginData = new ArrayList<Object>();
		int year = LocalDateTime.now().getYear();
		int initialMonth = 0;
		int initialYear = 2019;
		int lastMonth = LocalDateTime.now().minusMonths(1).getMonthValue();
		for (int j = year; j > initialYear; j--) {
			if(j<year)
				lastMonth=12;
			for (int i = initialMonth; i < lastMonth; i++) {
				Map<String, Object> invoiceTotal = companyMarginService.getBuWiseInvoiceTotal(i + 1,
						Integer.toString(year),accessToken);
				
				List<Object> buWiseUsers=companyMarginService.getCompanywiseData(i+1,year,accessToken);
				Map<String,Object> directCostTotal=companyMarginService.getDirectCostBuWise(i+1,year,buWiseUsers,invoiceTotal,accessToken);
				Map<String, Object> companyMargins=companyMarginService.getCompanyMargin(accessToken,directCostTotal,invoiceTotal,i+1,year);
				Map<String, Double> disputedAmount=companyMarginService.getAverageDisputedPercentage(year,accessToken);
				Map<String, Double> disputedAmountLTM=companyMarginService.getLTMBuDisputedPercentage(year,i+1,accessToken);
				companyMarginService.getBuReserve(i+1,year,companyMargins,disputedAmount,invoiceTotal,directCostTotal,buWiseUsers,disputedAmountLTM);
				
				List<String> verticals = new ArrayList<String>(
						Arrays.asList("Digital Marketing", "Blockchain", "Artificial Intelligence", "ERP Solution",
								"Oodles Technologies", "Oodles Studio", "Operations Support","DPP"));
				for (String businessVertical : verticals) {
					projectMarginService.getBuTotalMargin(i + 1, Integer.toString(year), businessVertical,accessToken);
					buMarginData = projectMarginService.getBuMargin(businessVertical, i + 1, year, 0,accessToken);
				}
			}
		}
		if (companyProjects.isEmpty() || buMarginData.isEmpty())
			return " ";
		else
			return "Run successfully";
		}catch(Exception e) {
			e.printStackTrace();
			log.debug("reputCompanyCache Exception : "+e.toString());
			return null;
		}
	}
	
}
