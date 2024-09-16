package com.krishna.service;

import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krishna.Interfaces.DollarCostService;
import com.krishna.domain.DollarCost;
import com.krishna.domain.UserModel;
import com.krishna.dto.DollarCostDTO;
import com.krishna.repository.invoice.DollarCostRepository;
import com.krishna.security.JwtValidator;
import com.krishna.util.ConstantUtility;

@Service
public class DollarCostServiceImpl implements DollarCostService {
	
	private ObjectMapper mapper;
	private DollarCostRepository dollarCostRepository;
	private JwtValidator validator;
	
	public DollarCostServiceImpl() {}
	
	@Autowired
	public DollarCostServiceImpl(ObjectMapper mapper, DollarCostRepository dollarCostRepository, JwtValidator validator) {
		this.mapper = mapper;
		this.dollarCostRepository = dollarCostRepository;
		this.validator = validator;
	}

	@Override
	public Map<String, Object> getAllCost(Integer year) {
		List<DollarCost> allCost=new ArrayList<>();
		if(year!=null) {
			 allCost = dollarCostRepository.findAllByYearAndIsDeletedOrderByYearDescMonthDesc(year,false);
		}
		else {
			 allCost = dollarCostRepository.findAllByIsDeletedOrderByYearDescMonthDesc(false);
		}
		return generateResponse(allCost);
	}

	@Override
	public Map<String, Object> saveDollarCost(DollarCostDTO requestBody, String accessToken) {
		UserModel currentUser = validator.tokenbValidate(accessToken);
		
		DollarCost existing = dollarCostRepository.findByMonthAndIsDeletedAndYear(requestBody.getMonth() + 1, false, requestBody.getYear());
		if(existing == null) {
			DollarCost entity = mapper.convertValue(requestBody, DollarCost.class);
			entity.setDeleted(false);
			entity.setCreatedBy(currentUser.getUserId());
			entity.setCreationDate(LocalDate.now());
			entity.setMonth(requestBody.getMonth() + 1);
			DollarCost savedEntity = dollarCostRepository.save(entity);
			return generateResponse(savedEntity);
		}else {
			return new HashMap<>();
		}
	}

	@Override
	public Map<String, Object> updateDollarCost(DollarCostDTO requestBody, Long id, String accessToken) {
		UserModel currentUser = validator.tokenbValidate(accessToken);
		Optional<DollarCost> data = dollarCostRepository.findById(id);
		data.ifPresent( entity ->{
			entity.setCost(requestBody.getCost());
			entity.setLastUpdatedDate(LocalDate.now());
			entity.setLastUpdatedBy(currentUser.getUserId());
			
			dollarCostRepository.saveAndFlush(entity);
		});
		return generateResponse(data.get());
	}

	@Override
	public void deleteDollarCost(Long id, String accessToken) {
		UserModel currentUser = validator.tokenbValidate(accessToken);
		Optional<DollarCost> data = dollarCostRepository.findById(id);
		data.ifPresent( entity ->{
			entity.setDeleted(true);
			entity.setLastUpdatedDate(LocalDate.now());
			entity.setLastUpdatedBy(currentUser.getUserId());
			
			dollarCostRepository.saveAndFlush(entity);
		});
	}
	
	private Map<String, Object> generateResponse(List<DollarCost> dollorCost){
		List<Map<String , Object>> allCost = new ArrayList<>();
		Map<String, Object> response = new HashMap<>();
		if(dollorCost != null) {
			
			dollorCost.forEach( cost ->{
				String monthName=new DateFormatSymbols().getMonths()[cost.getMonth()-1].toString();
				Map<String, Object> returnValue = new HashMap<>();
				returnValue.put("id", cost.getId());
				returnValue.put("dollarCost", cost.getCost());
				returnValue.put(ConstantUtility.MONTH, monthName);
				returnValue.put(ConstantUtility.YEAR, cost.getYear());
				allCost.add(returnValue);
			});
			response.put("allData", allCost);
			return response;
		}
			response.put("message", "No data found!");
			return response;
	}
	
	private Map<String, Object> generateResponse(DollarCost cost){
		Map<String, Object> returnValue = new HashMap<>();
		
		if(cost != null) {
			String monthName=new DateFormatSymbols().getMonths()[cost.getMonth()-1].toString();
			returnValue.put("id", cost.getId());
			returnValue.put("dollarCost", cost.getCost());
			returnValue.put(ConstantUtility.MONTH, monthName);
			returnValue.put(ConstantUtility.YEAR, cost.getYear());
			return returnValue;
		}else {
			returnValue.put("message", "No Data found");
			return returnValue;
		}
	}

	@Override
	public Map<String, Object> getCostByMonthAndYear(int month, int year) {
		return generateResponse(getCurrentDollarCostOrLastEntry(month, year));
	}

	public DollarCost getCurrentDollarCostOrLastEntry(int month, int year) {
		DollarCost currentOrLast =null;
		while(currentOrLast ==null && !(month < 1) && !(month >12) && !(year<2010)) {
			currentOrLast = dollarCostRepository.findByMonthAndIsDeletedAndYear(month, false, year);
			YearMonth yearMonth = YearMonth.of(year, month);
			year=yearMonth.minusMonths(1).getYear();
			month=yearMonth.minusMonths(1).getMonthValue();
		}
			return currentOrLast;
	}
	
	public Double getAverageDollarCost(int month, int year) {
		Double dollarCost = 0.0;
		int rollingMonth = month - 1;
		DollarCost currentMonthDollar = getCurrentDollarCostOrLastEntry(month, year);
		if(rollingMonth==0) {
			rollingMonth = 12;
			year=year-1;
		}
		if (currentMonthDollar != null) {
			DollarCost previousMonthDollar = null;
			for (int i = 0; i < 6; i++) {
				Double exchangeRate=0.0;
				previousMonthDollar = getCurrentDollarCostOrLastEntry(rollingMonth, year);
				if(previousMonthDollar!=null)
					exchangeRate=previousMonthDollar.getCost();
				dollarCost = dollarCost + exchangeRate;
				YearMonth yearMonth = YearMonth.of(year, rollingMonth);
				rollingMonth = yearMonth.minusMonths(1).getMonthValue();
				year = yearMonth.minusMonths(1).getYear();
			}
			dollarCost = dollarCost / 6;
			if (dollarCost > currentMonthDollar.getCost())
				dollarCost = currentMonthDollar.getCost();
		}
		return Math.round(dollarCost*100.0)/100.0;
	}

	@Override
	public List<Map<String,Object>> getMonthWiseAverageDollarCost(Date fromDate, Date toDate){
		List<Map<String,Object>> response = new ArrayList<>();
		
		Calendar toCal = Calendar.getInstance();
		toCal.setTime(toDate);
		Calendar fromCal = Calendar.getInstance();
		fromCal.setTime(fromDate);
		
		
		Double costSum = 0D;
		Double count = 0D;
		while(!fromCal.getTime().equals(toCal.getTime()) && fromCal.getTime().before(toCal.getTime())){
			Map<String, Object> map = new HashMap<>();
			Double dollarCost = getAverageDollarCost(fromCal.get(Calendar.MONTH)+1, fromCal.get(Calendar.YEAR));
			costSum = costSum + dollarCost;
			map.put("DollarCost", dollarCost);
			map.put(ConstantUtility.MONTH,fromCal.get(Calendar.MONTH)+1);
			map.put(ConstantUtility.YEAR,fromCal.get(Calendar.YEAR));
			response.add(map);
			fromCal.add(Calendar.MONTH,1);
			fromCal.set(Calendar.DAY_OF_MONTH, 1);
			count++;

		}
		Map<String,Object> map = new HashMap<>();
		if(count != 0)
			map.put("AverageCost",(costSum/count));
		response.add(map);
		return response;

	}

	@Override
	public ArrayList<Object> getLast6MonthAverage() {
		ArrayList<Object> response = new ArrayList<>();
		YearMonth current = YearMonth.now();
		for(int i=0; i < 6; i++) {
			Map<String, Object> returnValue = new HashMap<>();
			current = current.minusMonths(1);
			String monthName=new DateFormatSymbols().getMonths()[current.getMonthValue()-1].toString();
			Double dollarCost = getAverageDollarCost(current.getMonthValue(), current.getYear());
			returnValue.put("cost", Math.round(dollarCost *100.0)/100.0);
			returnValue.put(ConstantUtility.MONTH, monthName);
			returnValue.put(ConstantUtility.YEAR, current.getYear());
			response.add(returnValue);
		}
		return response;
	}
	
	@Override
	public Double getDollarCostForConversion(int month, int year) {
		Double dollarCost = 0.0;
		DollarCost currentMonthDollar = getCurrentDollarCostOrLastEntry(month, year);
		YearMonth thisYearMonth = YearMonth.of(year, month);
		int rollingMonth = thisYearMonth.minusMonths(1).getMonthValue();
		year = thisYearMonth.getYear();
		if (currentMonthDollar != null) {
			DollarCost previousMonthDollar = null;
			for (int i = 0; i < 6; i++) {
				Double exchangeRate=0.0;
				previousMonthDollar = getCurrentDollarCostOrLastEntry(rollingMonth, year);
				if(previousMonthDollar==null)
					return 0D;
				else
					exchangeRate=previousMonthDollar.getCost();
				dollarCost = dollarCost + exchangeRate;
				YearMonth yearMonth = YearMonth.of(year, rollingMonth);
				rollingMonth = yearMonth.minusMonths(1).getMonthValue();
				year = yearMonth.minusMonths(1).getYear();
			}
			dollarCost = dollarCost / 6;
			if (dollarCost > currentMonthDollar.getCost())
				dollarCost = currentMonthDollar.getCost();
		}
		return Math.round(dollarCost*100.0)/100.0;
	}


}
