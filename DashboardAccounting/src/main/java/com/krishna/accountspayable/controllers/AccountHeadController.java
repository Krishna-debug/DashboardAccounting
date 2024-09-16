package com.krishna.accountspayable.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.accountspayable.domain.AccountsHead;
import com.krishna.accountspayable.services.AccountsHeadService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

@RestController
public class AccountHeadController {
	
	@Autowired 
	private AccountsHeadService accountHeadService;
	
	@GetMapping(UrlMappings.GET_ALL_ACCOUNTS_HEAD)
	public ResponseEntity<Object> getAllAccountHead(@RequestHeader("Authorization") String accessToken){
		List<AccountsHead> data = accountHeadService.getAllAccountsHead();
		if(data != null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, data);
		}
		return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, data);
	}
	
	@PostMapping(UrlMappings.SAVE_ACCOUNTS_HEAD)
	public ResponseEntity<Object> saveAccountHead(@RequestParam("accountHeadName") String name, 
			@RequestHeader("Authorization") String accessToken){
		AccountsHead accountsHead = accountHeadService.saveAccountHead(name);
		if(accountsHead != null) {
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, accountsHead);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILED, accountsHead);
	}
	
	@PutMapping(UrlMappings.UPDATE_ACCOUNTS_HEAD)
	public ResponseEntity<Object> updateAccountsHead(@RequestParam("id") long id, @RequestParam("updatedName") String updatedName, 
			@RequestHeader("Authorization") String accessToken){
		AccountsHead updatedData = accountHeadService.updateExisting(id, updatedName);
		if(updatedData != null) {
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, updatedData);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILED, updatedData);
	}
	
	@DeleteMapping(UrlMappings.DELETE_ACCOUNTS_HEAD)
	public ResponseEntity<Object> deleteAccountHead(@RequestHeader("Authorization") String accessToken, @RequestParam("id") long id){
		AccountsHead data = accountHeadService.deleteAccountsHead(id);
		if(data != null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, "");
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILED, "");
	}
}
