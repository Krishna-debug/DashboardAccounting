package com.krishna.variablePay.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.krishna.dto.variablePay.AddYearlyVariablePayDto;

@Service
public interface YearlyVariablePayService {

	String addYearlyVariablePay(String accessToken,AddYearlyVariablePayDto addYearlyVariablePayDto);

	String updateYearlyVariablePay(String accessToken, Double yearlyAmount, Long id);

	List<Map<String, Object>> getYearlyVariablePay(String accessToken, int year, Long userId);

}
