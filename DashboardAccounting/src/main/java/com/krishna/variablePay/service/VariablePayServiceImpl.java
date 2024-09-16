package com.krishna.variablePay.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.krishna.domain.UserModel;
import com.krishna.domain.variablePay.VariablePay;
import com.krishna.dto.variablePay.AddVariablePayDto;
import com.krishna.repository.variablePay.VariablePayRepository;
import com.krishna.security.JwtValidator;

@Service
public class VariablePayServiceImpl implements VariablePayService {
	
	@Autowired
	VariablePayRepository variablePayRepo;
	
	@Autowired
	JwtValidator validator;

	@Override
	public String addVariablePay(String accessToken,AddVariablePayDto addVariablePayDto) {
		String result="false";
		if(addVariablePayDto.getPayRegisterId()==null) {
			return result="Please complete the payRegister";
		}
		else {
		int month = addVariablePayDto.getMonth()+1;
		VariablePay variablePayData = variablePayRepo.findByUserIdAndMonthAndYearAndIsDeletedFalse(addVariablePayDto.getUserId(),
				month,addVariablePayDto.getYear());
		UserModel currentUser = validator.tokenbValidate(accessToken);
		
		if(variablePayData==null) {
			VariablePay variablePay=new VariablePay();
			variablePay.setAmount(addVariablePayDto.getAmount());
			variablePay.setBuName(addVariablePayDto.getBuName());
			variablePay.setBuId(addVariablePayDto.getBuId());
			variablePay.setCreatedBy(currentUser.getUserId());
			variablePay.setCreatedOn(new Date());
			variablePay.setUserId(addVariablePayDto.getUserId());
			variablePay.setPayRegisterId(addVariablePayDto.getPayRegisterId());
			variablePay.setMonth(month);
			variablePay.setYear(addVariablePayDto.getYear());
			variablePayRepo.save(variablePay);
			result="true";
		}
		else {
			result="Already Exist";
		}
		return result;
		}
	}
	public String updateVariablePay(String accessToken,AddVariablePayDto addVariablePayDto) {
		VariablePay variablePay = variablePayRepo.findByIdAndIsDeletedFalse(addVariablePayDto.getId());
		UserModel currentUser = validator.tokenbValidate(accessToken);
		String result="false";
		if(variablePay!=null) {
		variablePay.setAmount(addVariablePayDto.getAmount());
		variablePay.setUpdatedOn(new Date());
		variablePay.setUpdatedBy(currentUser.getUserId());
		variablePayRepo.save(variablePay);
		result="true";
		}
		else {
			result="Variable Pay Not Found";
		}
		return result;
		
	}

	@Override
	public Boolean deleteVariablePay(String accessToken, Long id) {
		VariablePay variablePay = variablePayRepo.findByIdAndIsDeletedFalse(id);
		Boolean result=false;
		if(variablePay!=null) {
			if(!variablePay.getIsIncludeInPayroll()) {
			variablePay.setIsDeleted(true);
			variablePayRepo.save(variablePay);
			result=true;
			}
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> getVariablePay(String accessToken,int year,Long userId) {
		List<VariablePay> variablePayData = variablePayRepo.findAllByIsDeletedFalseAndYearAndUserId(year,userId);
		List<Map<String,Object>> result=new ArrayList<>();
		variablePayData.forEach(variablePay->{
			Map<String,Object> map=new HashMap<>();
			map.put("id", variablePay.getId());
			map.put("amount", variablePay.getAmount());
			map.put("month", variablePay.getMonth()-1);
			map.put("year", variablePay.getYear());
			map.put("buName", variablePay.getBuName());
			map.put("isIncludedInPayroll",variablePay.getIsIncludeInPayroll());
			result.add(map);
		});
		return result;
	}

}
