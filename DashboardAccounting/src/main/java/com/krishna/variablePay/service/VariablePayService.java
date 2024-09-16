package com.krishna.variablePay.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.krishna.dto.variablePay.AddVariablePayDto;

@Service
public interface VariablePayService {

	String addVariablePay(String accessToken,AddVariablePayDto addVariablePayDto);

	Boolean deleteVariablePay(String accessToken, Long id);

	List<Map<String, Object>> getVariablePay(String accessToken,int year,Long userId);

	String updateVariablePay(String accessToken, AddVariablePayDto addVariablePayDto);

}
