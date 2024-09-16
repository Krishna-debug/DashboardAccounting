package com.krishna.service;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import com.krishna.controller.FeignLegacyInterface;
import com.krishna.domain.BuExpenses;
import com.krishna.domain.IndirectCost;
import com.krishna.domain.UserModel;
import com.krishna.domain.Margin.BuReserve;
import com.krishna.domain.Margin.BuReserveDeductions;
import com.krishna.domain.Margin.MarginBasis;
import com.krishna.dto.DeductionResponseDto;
import com.krishna.dto.ReserveDto;
import com.krishna.enums.Months;
import com.krishna.repository.BUExpensesRepo;
import com.krishna.repository.BuReserveDeductionRepository;
import com.krishna.repository.BuReserveRepository;
import com.krishna.repository.IndirectCostRepository;
import com.krishna.repository.MarginBasisRepository;
import com.krishna.repository.invoice.ProjectInvoiceRepository;
import com.krishna.security.JwtValidator;

@Service
public class BuReserveServiceImpl implements BuReserveService {

	@Autowired
	BuReserveRepository buReserveRepository;

	@Autowired
	ProjectInvoiceRepository projectInvoiceRepo;

	@Autowired
	JwtValidator validator;

	@Autowired
	BuReserveDeductionRepository deductionRepo;

	@Autowired
	CompanyMarginService companyMarginService;

	@Autowired
	IndirectCostRepository costRepository;

	@Autowired
	FeignLegacyInterface feignLegacyInterface;
	
	@Autowired
	DollarCostServiceImpl dollarCostService;

	@Autowired
	MailService mailService;

	@Autowired
	EntityManager entityManager;

	@Autowired
	ProjectInvoiceService projectInvoiceService;
	

	@Value("${com.oodles.accounts.email}")
	private String accountsMail;

	@Value("${env.url}")
	private String environmentUrl;
	
	@Autowired
	private BUExpensesRepo buExpensesRepo;
	
	@Autowired
	private ProjectMarginService projectMarginService;

	@Autowired
	private MarginBasisRepository marginBasisRepository;

	private static Logger log = LoggerFactory.getLogger(BuReserveServiceImpl.class);

	@Override
	public Boolean updateDeductedAmount(String accessToken, ReserveDto reserveDto) {
		Boolean result = false;
		UserModel currentUser = validator.tokenbValidate(accessToken);
		BuReserveDeductions deductions = new BuReserveDeductions();
		deductions = setDeductionData(deductions, reserveDto);
		deductions.setDeductedBy(currentUser.getUserId());
		deductions.setDeductedByName(currentUser.getEmpName());
		deductions.setIsDeleted(false);
		Optional<BuExpenses> buExpenseData = buExpensesRepo.findById(reserveDto.getExpenseType());
		if(buExpenseData.isPresent()){
			BuExpenses buExpense=buExpenseData.get();
		 deductions.setBuExpenses(buExpense);
		}
		try {
			deductions = deductionRepo.save(deductions);
			if (deductions != null) {
				result = true;
			}
		} catch (Exception e) {
			result = false;
			throw e;
		}
		return result;
	}

	//@Scheduled(cron = "0 0 10 * * FRI", zone = "IST")
	public void sendMailOnDeductedAmount(String accessToken) {
		try {
			LocalDate currentDate1 = LocalDate.now();
			Date currentDate = localDateToDate(currentDate1);
			LocalDate preDate1 = currentDate1.minusDays(7);
			Date preDate = localDateToDate(preDate1);

			Boolean result = false;
			List<String> buisnessVertical = (List<String>) projectInvoiceService.getBusinessVerticals(accessToken);
			for (String buName : buisnessVertical) {
				List<BuReserveDeductions> buDeductions = deductionRepo.findByDeductedOnAndBuNameAndIsDeleted(preDate,
						currentDate, buName, false);
				Map<String, Object> buOwnerDetails = (Map<String, Object>) feignLegacyInterface
						.getBuOwnerInfo(accessToken, buName).get("data");
				if (buOwnerDetails != null) {
					String buOwnerEmail = buOwnerDetails.get("ownerEmail").toString();
					List<Map<String, Object>> deductionList = new ArrayList<>();
					SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
					List<String> ccList = new ArrayList<>();
					ccList.add(accountsMail);
					buDeductions.forEach(ded -> {
						Map<String, Object> res = new HashMap<>();
						res.put("deductedBy", ded.getDeductedByName());
						res.put("deductedAmount", ded.getDeductedAmount());
						res.put("deductedOn", formatter.format(ded.getDeductedOn()));
						res.put("remarks", ded.getRemarks());
						deductionList.add(res);
					});
					Context context = new Context();
					context.setVariable("bu", buName);
					context.setVariable("buOwner", buOwnerDetails.get("ownerName").toString());
					context.setVariable("data", deductionList);
					context.setVariable("currentYear", LocalDate.now().getYear());
					context.setVariable("responseUrl",
							"https://" + environmentUrl + "/#/vertical/" + buName + "/BU Reserve");
					String subject = "Reserve Deduction || " + buName;
					if (!buOwnerEmail.equals("") && !deductionList.isEmpty()) {
						mailService.sendScheduleHtmlMailWithCc(buOwnerEmail, subject, context,
								"Reserve-Deducted-Amount", ccList.stream().toArray(String[]::new));
						result = true;
					}
				}
			}
		} catch (Exception e) {
			log.debug("sendMailOnDeductedAmount Exception : " + e.toString());
		}
	}

	public static final java.util.Date localDateToDate(LocalDate localdate) {
		return Date.from(localdate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	private BuReserveDeductions setDeductionData(BuReserveDeductions deductions, ReserveDto reserveDto) {
		deductions.setBuName(reserveDto.getBuName());
		deductions.setRemarks(reserveDto.getRemarks());
		deductions.setDeductedAmount(reserveDto.getDeductionAmount());
		deductions.setDeductionDate(new Date(reserveDto.getDeductedOn()));
		deductions.setMonth(new Date(reserveDto.getDeductedOn()).getMonth() + 1);
		deductions.setYear(new Date(reserveDto.getDeductedOn()).getYear() + 1900);
		deductions.setDeductedOn(new Date());
		BuReserve buReserve = buReserveRepository.findAllByYearAndBuNameAndMonth(
				LocalDateTime.now().minusMonths(1).getYear(), reserveDto.getBuName(),
				LocalDateTime.now().minusMonths(1).getMonthValue());

		if (buReserve != null) {
			deductions.setAvailableReserve(buReserve.getTotalReserve() - reserveDto.getDeductionAmount());
			deductions.setPreviousReserve(buReserve.getTotalReserve());
			buReserve.setTotalReserve(buReserve.getTotalReserve() - reserveDto.getDeductionAmount());
			try {
				buReserve = buReserveRepository.saveAndFlush(buReserve);
			} catch (Exception e) {
				throw e;
			}
		}
		BuReserve monthReserve = buReserveRepository.findAllByYearAndBuNameAndMonth(
				new Date(reserveDto.getDeductedOn()).getYear() + 1900, reserveDto.getBuName(),
				new Date(reserveDto.getDeductedOn()).getMonth() + 1);

		if (monthReserve == null) {
			monthReserve = new BuReserve();
			monthReserve.setMonth(new Date(reserveDto.getDeductedOn()).getMonth() + 1);
			monthReserve.setYear(new Date(reserveDto.getDeductedOn()).getYear() + 1900);
			monthReserve.setBuName(reserveDto.getBuName());
			monthReserve.setMonthlyReserveAmount(0D);
			monthReserve.setTotalReserve(0D);
		}
		monthReserve.setDeductedAmount(monthReserve.getDeductedAmount() + reserveDto.getDeductionAmount());
		monthReserve.setRemarks(reserveDto.getRemarks());
		try {
			monthReserve = buReserveRepository.saveAndFlush(monthReserve);
		} catch (Exception e) {
			throw e;
		}

		return deductions;
	}

	@Override
	public Map<String, Object> getBuWiseReserve(String accessToken, String buName, int year) {
		Map<String, Object> resultMap = new HashMap<>();
		List<Map<String, Object>> result = new ArrayList<>();
		int initialMonth = 1;
		Double reserveAmount = 0.00;
		Double deductedAmount = 0.00;
		double totalDebited = 0.00;
		double totalCredited = 0.00;
		double totalAvailable = 0.00;
		double surplus = 0.00;
		double target = 0.00;
		double lastCreditedAmount = 0.00;
		double totalCost = 0.00;
		String monthName = null;
		int lstMonth = LocalDateTime.now().getMonthValue();
		if (year == 2021) {
			initialMonth = 7;
			lstMonth = 12;
		} else if (year <= 2020) {
			initialMonth = 0;
			lstMonth = -1;
		}else if( year < LocalDateTime.now().getYear()){
			lstMonth = 12;
		}
		
		int surplusMonth = lstMonth;
		int pointerMonth = lstMonth;
		IndirectCost indirectCost = null;
		while (lstMonth > 0) {
			String monthObj = new DateFormatSymbols().getMonths()[lstMonth - 1].toString();
			Months monthEnum = Months.valueOf(monthObj.toUpperCase());
			indirectCost = costRepository.findByYearAndIsDeletedAndMonth(Integer.toString(year), false, monthEnum);
			if (indirectCost != null) {
				surplusMonth = lstMonth;
				break;
			}
			lstMonth--;
		}
		
		List<MarginBasis> marginBasis = marginBasisRepository.findByYear(year);
		for (int i = initialMonth; i <= pointerMonth; i++) {
			Map<String, Object> map = new HashMap<>();
			map.put("netMargin", 0.00);
			BuReserve bu = buReserveRepository.findAllByYearAndBuNameAndMonth(year, buName, i);
			Map<String, Object> responseLTM = projectInvoiceService.getLTMDisputedPercentage(new Long(year),buName,accessToken,i);
			final int month = i;
			Optional<Boolean> isLTMOptional = marginBasis.stream().filter(val-> val.getMonth().equals(month)).map(MarginBasis :: getIsLTM).findAny();
			Boolean isLTM = false;
			if(isLTMOptional.isPresent())
				isLTM = isLTMOptional.get();


			if (bu != null) {
				monthName = new DateFormatSymbols().getMonths()[bu.getMonth() - 1].toString();
				if ( bu.getDeductedAmount() != null)
					deductedAmount = bu.getDeductedAmount();
				map.put("monthName", monthName);
				map.put("deductedAmount", deductedAmount);
//				map.put("reserveAmount", reserveAmount);
				map.put("totalMargin", bu.getMargin() != null && ((month!=pointerMonth && year==LocalDateTime.now().getYear())|| year<LocalDateTime.now().getYear()) ? bu.getMargin() : 0.00);
				map.put("ytdDisputed", bu.getDisputedAmount() != null &&  ((month!=pointerMonth && year==LocalDateTime.now().getYear())|| year<LocalDateTime.now().getYear()) ? bu.getDisputedAmount() : 0.00);
				map.put("revenue", bu.getRevenue() != null && ((month!=pointerMonth && year==LocalDateTime.now().getYear())|| year<LocalDateTime.now().getYear()) ? bu.getRevenue() : 0.00);
				map.put("netMarginYtd", 0.00);
				map.put("netMargin", 0.00);
				map.put("bookedReserve", 0.00);
				map.put("isLTM", isLTM);
				Double disputedAmountLTM = (new Double(responseLTM.get("averageDisputedPercentageLTM").toString()) * new Double(responseLTM.get("monthlyExtInvoiceTotal").toString()))/100;
				map.put("ltmDisputed",disputedAmountLTM);
				if (((month!=pointerMonth && year==LocalDateTime.now().getYear())|| year<LocalDateTime.now().getYear()) && bu.getMargin() != null && bu.getDisputedAmount() != null && bu.getMargin() != 0.0D) {
//					if (reserveAmount > 0) {
						map.put("netMarginYtd", bu.getMargin() - bu.getDisputedAmount());
						if(!isLTM)
							map.put("netMargin", bu.getMargin() - bu.getDisputedAmount());

//					}
				}
				if(((month!=pointerMonth && year==LocalDateTime.now().getYear())|| year<LocalDateTime.now().getYear()) && isLTM && bu.getMargin() != null && disputedAmountLTM != null && bu.getMargin() != 0.0D){
					map.put("netMargin", bu.getMargin() - disputedAmountLTM);
				}

				if (((month!=pointerMonth && year==LocalDateTime.now().getYear())|| year<LocalDateTime.now().getYear()) && bu.getRevenue() != null && bu.getRevenue() != 0)
					map.put("bookedReserve", Math.round(((bu.getRevenue()) / 10) * 100.00) / 100.00);
				if(((month!=pointerMonth && year==LocalDateTime.now().getYear())|| year<LocalDateTime.now().getYear())) {
					reserveAmount = (bu.getMargin()-(isLTM?disputedAmountLTM:bu.getDisputedAmount()))-(bu.getRevenue()/10);
					map.put("availableAmount", reserveAmount - deductedAmount);
					totalAvailable = totalAvailable + (reserveAmount - deductedAmount);
				}
				else {
					map.put("availableAmount", 0 - deductedAmount);
					totalAvailable = totalAvailable + (0 - deductedAmount);
				}
				totalDebited = totalDebited + deductedAmount;
				if((month!=pointerMonth && year==LocalDateTime.now().getYear())|| year<LocalDateTime.now().getYear()) {
				totalCredited = totalCredited+(new Double(map.get("netMargin").toString()) - new Double(map.get("bookedReserve").toString()));
				}
				//				if (i == surplusMonth) {
//					lastCreditedAmount = reserveAmount;
//					if (indirectCost != null) {
//						surplus = bu.getSurplusReserve();
//						target = bu.getTargetReserve();
//						totalCost = bu.getTotalCost();
//					}
//				}
				List<DeductionResponseDto> deductions = new ArrayList<>();
				map.put("reserveAmount", ((month!=pointerMonth && year==LocalDateTime.now().getYear())|| year<LocalDateTime.now().getYear())?new Double(map.get("netMargin").toString()) - new Double(map.get("bookedReserve").toString()):0);
				List<BuReserveDeductions> buDeductions = deductionRepo.findAllByMonthAndYearAndBuNameAndIsDeleted(i,
						year, buName, false);
				buDeductions.forEach(deduction -> {
					DeductionResponseDto responseObj = new DeductionResponseDto();					
					BeanUtils.copyProperties(deduction, responseObj);
					deduction.setBuExpenses(responseObj.getBuExpenses());
					deductions.add(responseObj);
				});
				map.put("deductions", deductions);
				map.put("remarks", bu.getRemarks());
				result.add(map);
				
			} else {
				monthName = new DateFormatSymbols().getMonths()[i - 1].toString();
				map.put("monthName", monthName);
				map.put("deductedAmount", 0.00);
				map.put("reserveAmount", 0.00);
				map.put("availableAmount", 0.00);
				map.put("deductions", new ArrayList<>());
				map.put("remarks", "");
				map.put("surplusReserve", 0.00);
				map.put("targetReserve", 0.00);
				map.put("netMargin", 0.00);
				result.add(map);
			}

		}
		resultMap.put("reserveData", result);
		resultMap.put("reserveTotal", totalCredited);
		resultMap.put("debitedReserve", totalDebited);
		Double carryFrwrdReserve = 0D;
		BuReserve bu = buReserveRepository.findAllByYearAndBuNameAndMonth(year - 1, buName, 12);
		if (bu != null) {
//			List<BuReserveDeductions> buDeductions = deductionRepo.findAllByBuNameAndIsDeletedAndYear(buName, false,
//					year - 1);
//			double totalDeduction = buDeductions.stream()
//					.collect(Collectors.summingDouble(BuReserveDeductions::getDeductedAmount));
			totalAvailable = totalAvailable + bu.getTotalReserve();
			carryFrwrdReserve = bu.getTotalReserve();
		}
		// resultMap.put("totalAvailable", 0);
		// if(totalAvailable>0)
		resultMap.put("totalAvailable", totalAvailable);
		resultMap.put("surplusReserve", 0);
		if (totalAvailable > (totalCost * 3))
			resultMap.put("surplusReserve", Math.round((totalAvailable - (totalCost*3))*100.00)/100.00);
		resultMap.put("targetReserve", target);
		resultMap.put("lastCreditedAmount", lastCreditedAmount);
		resultMap.put("totalCost", totalCost);
		resultMap.put("carryForwardAmount", carryFrwrdReserve);
		return resultMap;
	}

	@Override
	public Boolean deleteBuReserve(String accessToken, Long id) {
		Boolean isDeleted = false;
		BuReserveDeductions bu = deductionRepo.findById(id).get();
		if (!bu.equals(null)) {
			bu.setIsDeleted(true);
			BuReserveDeductions dataSaved = deductionRepo.save(bu);
			if (!dataSaved.equals(null)) {
				isDeleted = true;
			}
			BuReserve buReserve = buReserveRepository.findAllByYearAndBuNameAndMonth(bu.getYear(), bu.getBuName(),
					bu.getMonth());
			double deductedAmount = buReserve.getDeductedAmount();
			if(buReserve!=null) {
				List<BuReserveDeductions> deductions= deductionRepo.findAllByMonthAndYearAndBuNameAndIsDeleted(buReserve.getMonth(), bu.getYear(), bu.getBuName(), false);
				double totalDeduction = deductions.stream()
						.collect(Collectors.summingDouble(BuReserveDeductions::getDeductedAmount));
				buReserve.setDeductedAmount(totalDeduction);
			}
				
			buReserveRepository.saveAndFlush(buReserve);
		}
		return isDeleted;
	}

	@Override
	public Boolean updateRemarks(String accessToken, Long id, String remarks) {
		Boolean resetRemarks = false;
		BuReserveDeductions bu = deductionRepo.findById(id).get();
		if (!bu.equals(null)) {
			bu.setRemarks(remarks);
			;
			BuReserveDeductions dataSaved = deductionRepo.save(bu);
			if (!dataSaved.equals(null)) {
				resetRemarks = true;
			}
		}

		return resetRemarks;
	}

	/*
	 * @author pankaj
	 * This cron is used to check if there is any disputed invoice in
	 * the previous day and update the buReserve of that invoice Runs at 1 AM
	 * everyday
	 */
		
	//@Scheduled(cron = "0 30 13 ? * *", zone = "IST")
	@SuppressWarnings({ "unchecked", "removal" })
	public Boolean carryForwardReservePercentage(String accessToken) {
		try {
			companyMarginService.flushInvoicesCache();
			projectInvoiceService.flushYtdPerc();
			projectMarginService.flushTotalBuMargins();
		    projectMarginService.flushBuMargins();
		    projectInvoiceService.flushProjectDetailsCache();
			List<String> verticals = new ArrayList<String>(
					Arrays.asList("Digital Marketing", "Blockchain", "Artificial Intelligence", "ERP Solution",
							"Oodles Technologies", "Oodles Studio", "Operations Support"));
			for (int i = LocalDate.now().getYear(); i >= 2021; i--) {
				Map<String, Double> disputedAmount = companyMarginService.getAverageDisputedPercentage(i,accessToken);
				for (int j = 0; j < 12; j++) {
					Map<String, Object> invoiceTotal = companyMarginService.getBuWiseInvoiceTotal(j + 1,
							Integer.toString(i),accessToken);
					Double dollarexchangeCost = dollarCostService.getAverageDollarCost(j + 1, i);

					for (int k = 0; k < verticals.size(); k++) {
						String buName = verticals.get(k);
						Double buDisputedAmount = 0D;
						Double buTotalInvoiceAmount = 0D;
						Double disputedPercentage = 0D;

						if (disputedAmount.get(buName) != null)
							buDisputedAmount = disputedAmount.get(buName) * dollarexchangeCost;

						if (disputedAmount.get(buName + "Total") != null)
							buTotalInvoiceAmount = new Double(disputedAmount.get(buName + "Total").toString())
									* dollarexchangeCost;
//System.out.println("::::::::buName::::::::::"+buName);
//System.out.println("::::::::month::::::::::"+(j+1));
//System.out.println("::::::::year::::::::::"+i);
//
//System.out.println("::::::::buDisputedAmount::::::::::"+buDisputedAmount);

						if (buDisputedAmount != 0 && buTotalInvoiceAmount != 0) {
							disputedPercentage =  (buDisputedAmount / buTotalInvoiceAmount) * 100;
							
							disputedPercentage =  Math.round(disputedPercentage * 100) / 100.0;
						}
//						System.out.println("::::::::disputedPercentage::::::::::"+disputedPercentage);

						List<Object> invoiceList = (List<Object>) invoiceTotal.get("buWiseInvoice");
						Map<String, Object> invoiceMap = companyMarginService.getInvoiceMapForReserve(invoiceList);
						Map<String, Object> invoiceData = (Map<String, Object>) invoiceMap.get(buName);
						Double invoiceTotalAmount = (Double) invoiceData.get("invoiceAmountInRupees");
						Double invoiceDisputedInR = (Double) invoiceData.get("invoiceDisputedInR");
//						System.out.println("::::::::invoiceTotalAmount::::::::::"+invoiceTotalAmount);

						if(invoiceTotalAmount != 0.0D) {
						double buDisputed = (disputedPercentage * invoiceTotalAmount) / 100.00;
						BuReserve buReserve = buReserveRepository.findAllByYearAndBuNameAndMonth(i, buName, j + 1);
						if (buReserve != null) {
							Double margin=invoiceTotalAmount - buReserve.getTotalCost();
							Double marginperc=(margin*100)/invoiceTotalAmount;
							buReserve.setRevenue(invoiceTotalAmount);
							buReserve.setMargin(margin);
							buReserve.setMarginPerc(marginperc);
							buReserve.setDisputedAmount(buDisputed);
							buReserve.setDisputedPerc(disputedPercentage);
							buReserve.setMonthlyReserveAmount((margin-buDisputed)-(invoiceTotalAmount/10));
							buReserveRepository.save(buReserve);
						}
						}
					}

				}

			}
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void correctData() {
		List<String> verticals = new ArrayList<String>(
				Arrays.asList("Digital Marketing", "Blockchain", "Artificial Intelligence", "ERP Solution",
						"Oodles Technologies", "Oodles Studio", "Operations Support", "DPP"));
		
		for(String bu:verticals) {
			int initialMonth=8;
			Double lastAmount=0D;
			BuReserve buReserve=buReserveRepository.findAllByYearAndBuNameAndMonth(2021, bu, 7);
			if(buReserve!=null) {
				List<BuReserveDeductions> buDeductions = deductionRepo.findAllByMonthAndYearAndBuNameAndIsDeleted(7,2021,bu, false);
				double totalDeduction = buDeductions.stream()
						.collect(Collectors.summingDouble(BuReserveDeductions::getDeductedAmount));
				lastAmount=buReserve.getMonthlyReserveAmount()-totalDeduction;
				buReserve.setTotalReserve(lastAmount);
				buReserveRepository.save(buReserve);
			}
			for(int i=2021;i<=2023;i++) {
				if(i>2021)
					initialMonth=1;
				for(int j=initialMonth;j<=12;j++) {
					BuReserve monthlyReserve=buReserveRepository.findAllByYearAndBuNameAndMonth(i, bu, j);
					if(monthlyReserve!=null) {
						List<BuReserveDeductions> buDeductions = deductionRepo.findAllByMonthAndYearAndBuNameAndIsDeleted(j,i,bu, false);
						Double totalDeduction = buDeductions.stream()
								.collect(Collectors.summingDouble(BuReserveDeductions::getDeductedAmount));
						
						Double data=(monthlyReserve.getMonthlyReserveAmount()-totalDeduction);
						lastAmount=lastAmount+data;
						monthlyReserve.setTotalReserve(lastAmount);
						buReserveRepository.save(monthlyReserve);
					}
				}
			}
			
		}
		
	}
}

