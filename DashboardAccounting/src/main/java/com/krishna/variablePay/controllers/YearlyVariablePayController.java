package com.krishna.variablePay.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.dto.variablePay.AddYearlyVariablePayDto;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;
import com.krishna.variablePay.service.YearlyVariablePayService;

@RestController
public class YearlyVariablePayController {

	@Autowired
	YearlyVariablePayService yearlyVariablePayService;

	@PostMapping(UrlMappings.ADD_YEARLY_VARIABLE_PAY)
	public ResponseEntity<Object> addYearlyVariablePay(@RequestHeader("Authorization") String accessToken,
			@RequestBody AddYearlyVariablePayDto addYearlyVariablePayDto) {
		String res = yearlyVariablePayService.addYearlyVariablePay(accessToken, addYearlyVariablePayDto);
		if (res.equals("true")) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, res);
		} else if (res.equals("Already Exist")) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Data Already Exists", res);
		} else if (res.equals("Please complete the payRegister")) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Please complete the payRegister", res);
		}
		else {
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILURE, res);
		}
	}

	@PostMapping(UrlMappings.UPDATE_YEARLY_VARIABLE_PAY)
	public ResponseEntity<Object> updateYearlyVariablePay(@RequestHeader("Authorization") String accessToken,
			@RequestParam Double yearlyAmount, @RequestParam Long id) {
		String res = yearlyVariablePayService.updateYearlyVariablePay(accessToken, yearlyAmount, id);
		if (res.equals("true")) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, res);
		} else if (res.equals("No Variable Pay Found")) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "No Variable Pay Found", res);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILURE, res);
		}
	}
	
	@GetMapping(UrlMappings.GET_YEARLY_VARIABLE_PAY)
	public ResponseEntity<Object> getYearlyVariablePay(@RequestHeader("Authorization")String accessToken,@RequestParam int year,@RequestParam Long userId) {
		List<Map<String, Object>> result=yearlyVariablePayService.getYearlyVariablePay(accessToken,year,userId);
		if(!result.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Data Fetched Successfully", result);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "No data Available", null);
	}

}
