package com.krishna.variablePay.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.dto.variablePay.AddVariablePayDto;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;
import com.krishna.variablePay.service.VariablePayService;

@RestController
public class VariablePayController {
	
	@Autowired
	VariablePayService variablePayService;
	
	@PostMapping(UrlMappings.ADD_VARIABLE_PAY)
	public ResponseEntity<Object> addVariablePay(@RequestHeader("Authorization") String accessToken,
			@RequestBody AddVariablePayDto addVariablePayDto) {
		String res = variablePayService.addVariablePay(accessToken, addVariablePayDto);
		if (res.equals("true")) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, res);
		} else if (res.equals("Already Exist")) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Data Already Exists", res);
		}
		 else if (res.equals("Please complete the payRegister")) {
				return ResponseHandler.generateResponse(HttpStatus.OK, true, "Please complete the payRegister", res);
		 }
		else {
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILURE, res);
		}
	}
	
	@DeleteMapping(UrlMappings.DELETE_VARIABLE_PAY)
	public ResponseEntity<Object> deleteBuReserve(@RequestHeader("Authorization")String accessToken,@RequestParam Long id) {
		Boolean isDeleted = variablePayService.deleteVariablePay(accessToken,id);
		if(isDeleted) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DELETED_SUCCESSFULLY,isDeleted);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILED, isDeleted);
		}
	}
	
	@GetMapping(UrlMappings.GET_VARIBALE_PAY)
	public ResponseEntity<Object> getVariablePay(@RequestHeader("Authorization")String accessToken,@RequestParam int year,@RequestParam Long userId) {
		List<Map<String, Object>> result=variablePayService.getVariablePay(accessToken,year,userId);
		if(!result.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Data Fetched Successfully", result);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "No data available", null);
	}

	@PostMapping(UrlMappings.UPDATE_VARIABLE_PAY)
	public ResponseEntity<Object> updateVariablePay(@RequestHeader("Authorization") String accessToken,
			@RequestBody AddVariablePayDto addVariablePayDto) {
		String res = variablePayService.updateVariablePay(accessToken, addVariablePayDto);
		if (res.equals("true")) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, res);
		} else if (res.equals("Variable Pay Not Found")) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, res);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILURE, res);
		}
	}
	

}
