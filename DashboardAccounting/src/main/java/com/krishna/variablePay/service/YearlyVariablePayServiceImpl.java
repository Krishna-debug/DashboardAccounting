package com.krishna.variablePay.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.krishna.domain.UserModel;
import com.krishna.domain.variablePay.VariablePay;
import com.krishna.domain.variablePay.YearlyVariablePay;
import com.krishna.dto.variablePay.AddYearlyVariablePayDto;
import com.krishna.repository.variablePay.VariablePayRepository;
import com.krishna.repository.variablePay.YearlyVariablePayRepository;
import com.krishna.security.JwtValidator;

@Service
public class YearlyVariablePayServiceImpl implements YearlyVariablePayService {
	
	@Autowired
	YearlyVariablePayRepository yearlyVariablePayRepo;
	
	@Autowired
	JwtValidator validator;
	
	@Autowired
	VariablePayRepository variablePayRepository;

	@Override
	public String addYearlyVariablePay(String accessToken,AddYearlyVariablePayDto addYearlyVariablePayDto) {
			String result="false";
			if(addYearlyVariablePayDto.getPayRegisterId()==null) {
				return result="Please complete the payRegister";
			}else {
				YearlyVariablePay yearlyVariablePayData = yearlyVariablePayRepo.findByUserIdAndYearAndIsDeletedFalse(
						addYearlyVariablePayDto.getUserId(), addYearlyVariablePayDto.getYear());
				UserModel currentUser = validator.tokenbValidate(accessToken);
				if(yearlyVariablePayData==null) {
				YearlyVariablePay yearlyVariablePay=new YearlyVariablePay();
				yearlyVariablePay.setYearlyAmount(addYearlyVariablePayDto.getYearlyAmount());
				yearlyVariablePay.setBuName(addYearlyVariablePayDto.getBuName());
				yearlyVariablePay.setBuId(addYearlyVariablePayDto.getBuId());
				yearlyVariablePay.setCreatedBy(currentUser.getUserId());
				yearlyVariablePay.setCreatedOn(new Date());
				yearlyVariablePay.setUserId(addYearlyVariablePayDto.getUserId());
				yearlyVariablePay.setPayRegisterId(addYearlyVariablePayDto.getPayRegisterId());
				yearlyVariablePay.setYear(addYearlyVariablePayDto.getYear());
				yearlyVariablePayRepo.save(yearlyVariablePay);
				result="true";
			}
			else {
				result="Already Exist";
			}
			return result;
			}
		}

	@Override
	public String updateYearlyVariablePay(String accessToken, Double yearlyAmount, Long id) {
		YearlyVariablePay yearlyVariablePay = yearlyVariablePayRepo.findByIdAndIsDeletedFalse(id);
		String result="false";
		UserModel currentUser = validator.tokenbValidate(accessToken);
		if(yearlyVariablePay!=null) {
			yearlyVariablePay.setYearlyAmount(yearlyAmount);
			yearlyVariablePay.setUpdatedOn(new Date());
			yearlyVariablePay.setUpdatedBy(currentUser.getUserId());
			yearlyVariablePayRepo.save(yearlyVariablePay);
			result="true";

		}
		else {
			result="No Variable Pay Found";
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> getYearlyVariablePay(String accessToken, int year, Long userId) {
		YearlyVariablePay yearlyVariablePay = yearlyVariablePayRepo.findByUserIdAndYearAndIsDeletedFalse(userId, year);
		List<VariablePay> variablePayData = variablePayRepository.findAllByIsDeletedFalseAndYearAndUserId(year,userId);
		Double totalVaribalePay=variablePayData.parallelStream().mapToDouble(pay -> pay.getAmount()).sum();
		List<Map<String,Object>> result=new ArrayList<>();
		if (yearlyVariablePay != null) {
			Map<String, Object> map = new HashMap<>();
			map.put("id", yearlyVariablePay.getId());
			map.put("amount", yearlyVariablePay.getYearlyAmount());
			map.put("amountLeft", yearlyVariablePay.getYearlyAmount()-totalVaribalePay);
			map.put("year", yearlyVariablePay.getYear());
			map.put("buName", yearlyVariablePay.getBuName());
			map.put("userId", yearlyVariablePay.getUserId());
			result.add(map);
			return result;
		}
		return result;
	}

}
