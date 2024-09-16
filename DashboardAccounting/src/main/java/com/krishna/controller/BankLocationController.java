package com.krishna.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.domain.BankLocation;
import com.krishna.service.BankLocationService;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

/*
 * @author Pankaj Garg
 * 
 * This is Controller class for bank location CRUD.
 * */
@RestController
public class BankLocationController {

	@Autowired BankLocationService bankLocationService;
	
	
	@GetMapping(UrlMappings.GET_ALL_BANK_LOCATIONS)
	public ResponseEntity<Object> getAllBankLocations(@RequestHeader("Authorization") String accessToken ){
		List<BankLocation> response = bankLocationService.getAllBankLocations();
		if(!response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK,true, "Data Fetched Successfully",response);
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Expectation Failed", null);
	}

	@GetMapping(UrlMappings.GET_BU_ALL_BANK_LOCATIONS)
	public ResponseEntity<Object> getBuAllBankLocations(@RequestHeader("Authorization") String accessToken ){
		List<BankLocation> response = bankLocationService.getAllBankLocations();
		if(!response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK,true, "Data Fetched Successfully",response);
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Expectation Failed", null);
	}
	
	@GetMapping(UrlMappings.GET_BANK_LOCATION)
	public ResponseEntity<Object> getAllBankLocation(@RequestHeader("Authorization") String accessToken,long id ){
		BankLocation response = bankLocationService.getBankLocation(id);
		if(response !=null)
			return ResponseHandler.generateResponse(HttpStatus.OK,true, "Data Fetched Successfully",response);
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Data does not exists", null);
	}
	
	
	@PostMapping(UrlMappings.ADD_BANK_LOCATION)
	public ResponseEntity<Object> addBankLocation(@RequestHeader("Authorization") String accessToken, @RequestParam String location,@RequestParam(required=false) Long id ){
		BankLocation response = bankLocationService.addBankLocation(location,id);
		if(response!=null)
			return ResponseHandler.generateResponse(HttpStatus.CREATED,true, "Bank Location Added Successfully",response);
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Bank Location Already Exists", null);
	}
	
	@DeleteMapping(UrlMappings.DELETE_BANK_LOCATION)
	public ResponseEntity<Object> editBankLocation(@RequestHeader("Authorization") String accessToken,@RequestParam long id){
		boolean response = bankLocationService.deleteBankLocation(id);
		if(response)
			return ResponseHandler.generateResponse(HttpStatus.CREATED,true, "Bank Location Deleted Successfully",response);
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Bank Location is in Use", response);
	}
	
	
	
		
}
