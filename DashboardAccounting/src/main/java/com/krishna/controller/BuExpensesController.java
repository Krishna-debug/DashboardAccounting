package com.krishna.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.Interfaces.IBuExpensesService;
import com.krishna.domain.BuExpenses;
import com.krishna.dto.BuExpensesDto;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

@RestController
public class BuExpensesController {

	@Autowired
	IBuExpensesService buExpensesService;
	
	@GetMapping(value= UrlMappings.GET_ALL_DEDUCTION_TYPE)
	public ResponseEntity<Object> getExpensesReasons(@RequestHeader("Authorization") String accessToken){
		List<Object> response = buExpensesService.getExpensesType();
		if(response!=null) 
			return ResponseHandler.generateResponse(HttpStatus.OK,true, "Data Fetched Successfully",response);
		else 
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Expectation Failed", null);
	}
	
	
	@PostMapping(value = UrlMappings.ADD_DEDUCTION_TYPE)
	public ResponseEntity<Object> addExpenseType(@RequestHeader("Authorization") String accessToken,
			@RequestParam String expenseType) {
		BuExpenses response = buExpensesService.addExpenseType(expenseType);
		if (response != null)
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.ADDED_SUCCESSFULLY,response);
		else
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false, ConstantUtility.ALREADY_EXIST, null);
	}
	
	
	@DeleteMapping(value=UrlMappings.DELETE_DEDUCTION_TYPE)
	public ResponseEntity<Object> deleteExpenseType(@RequestHeader("Authorization") String accessToken,@RequestParam Long id){
		boolean response = buExpensesService.deleteExpenseType(id);
		if (response)
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Data Deleted Successfully", response);
		else
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false,"Already in Use", null);
	}
	
	@PutMapping(value=UrlMappings.EDIT_DEDUCTION_TYPE)
	public ResponseEntity<Object> editExpenseType(@RequestHeader("Authorization") String accessToken,@RequestBody BuExpensesDto dto){
		BuExpenses response = buExpensesService.editExpenseType(dto);
		if(response!=null)
		return ResponseHandler.generateResponse(HttpStatus.OK,true, "Data Edited Successfully",response);
		else
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false, ConstantUtility.NON_NULL, null);
	}
}