package com.krishna.service;

import java.text.DateFormatSymbols;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.krishna.domain.IndirectCost;
import com.krishna.domain.Margin.BuReserve;
import com.krishna.domain.Margin.BuReserveDeductions;
import com.krishna.dto.DeductionResponseDto;
import com.krishna.enums.Months;
import com.krishna.repository.BuReserveDeductionRepository;
import com.krishna.repository.BuReserveRepository;
import com.krishna.repository.IndirectCostRepository;

@Service
public class ReserveServiceImpl {
	
	@Autowired
	DollarCostServiceImpl dollarCostService;
	
	@Autowired
	CompanyMarginService companyMarginService;
	
	@Autowired
	LoginUtiltiyService loginUtilityService;
	
	@Autowired
	BuReserveRepository bureserveRepo;
	
	@Autowired
	BuReserveDeductionRepository deductionRepo;
	
	@Autowired
	IndirectCostRepository costRepository;
	
	Logger log=LoggerFactory.getLogger(ReserveServiceImpl.class);
	
	public Map<String, Object> getBuReserveV1(int month, int year, Map<String, Object> companyMargins,
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
		Map<String,Object> invoiceMap=companyMarginService.getInvoiceMapForReserve(invoiceList);

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

			if(disputedAmountLTM.get(buName + "LTM")!=null){
				buDisputedAmountLTM = disputedAmountLTM.get(buName + "LTM")*avgDollarCostLtm;
				
			}
				
			if(disputedAmountLTM.get(buName+"TotalLTM")!=null)
				buTotalInvoiceAmountLTM = new Double(disputedAmountLTM.get(buName+"TotalLTM").toString())*avgDollarCostLtm;
			log.info("buName+++++++++++"+buName);

			Map<String, Object> invoiceData=(Map<String, Object>) invoiceMap.get(buName);
			log.info("invoiceData+++++++++++"+invoiceData);
            Double invoiceTotalAmount=0D;
            if(invoiceData!=null && invoiceData.containsKey("invoiceAmountInRupees"))
			    invoiceTotalAmount=(Double) invoiceData.get("invoiceAmountInRupees");
				
			Map<String, Object> buMap=new HashMap<>();
			
			Double disputedPercentage = 0D;
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
			
			if(buDisputedAmountLTM!=0 && buTotalInvoiceAmountLTM!=0)
				disputedPercentageLTM = (buDisputedAmountLTM / buTotalInvoiceAmountLTM) * 100;
			if(disputedAmountLTM.get(buName+"monthlyTotalLTM") != null)
				buDisputedLTM = (Math.round(disputedPercentageLTM*100.00)/100.00 * (new Double(disputedAmountLTM.get(buName+"monthlyTotalLTM").toString())*dollarexchangeCost)) / 100;
			
			
			buMap.put("disputedPerc",Math.round( disputedPercentage*100.00)/100.00);
			buMap.put("disputedAmount",Math.round(buDisputed*100.00)/100.00);
			buMap.put("disputedPercLTM",Math.round( disputedPercentageLTM*100.00)/100.00);
			buMap.put("disputedAmountLTM",Math.round(buDisputedLTM*100.00)/100.00);
			BuReserve bureserve=bureserveRepo.findAllByYearAndBuNameAndMonth(year, buName, month);
			Boolean isReserveChanged=false;
			YearMonth lastMonthYearObj=YearMonth.of(LocalDateTime.now().minusMonths(1).getYear(), LocalDateTime.now().minusMonths(1).getMonthValue());
			if(bureserve==null || isReserveChanged) {
				buMap.put("totalMarginPerc", buData!=null && buData.containsKey("marginPerc")? buData.get("marginPerc"):0D);
				buMap.put("totalMargin", buData!=null && buData.containsKey("margin")? new Double(buData.get("margin").toString()):0D);
				if(buDisputedAmount!=0 && buTotalInvoiceAmount!=0)
					disputedPercentage = (buDisputedAmount / buTotalInvoiceAmount) * 100;
				buDisputed = (disputedPercentage*invoiceTotalAmount)/100.00;
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
				bureserve = companyMarginService.setBureserve(month, year, reserveAmount, lastTotal, buName, monthlyIndirectCost);
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
	
	

}
