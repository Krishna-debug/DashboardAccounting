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

import com.krishna.accountspayable.domain.HsnCode;
import com.krishna.accountspayable.services.HsnCodeService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

@RestController
public class HsnCodeController {
	
	@Autowired 
	private HsnCodeService hsnCodeService;
	
	@GetMapping(UrlMappings.GET_ALL_HSN_CODES)
	public ResponseEntity<Object> getAllHsnCode(@RequestHeader("Authorization") String accessToken){
		List<HsnCode> data = hsnCodeService.getAllHsnCodes();
		if(data != null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, data);
		}
		return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, data);
	}
	
	@PostMapping(UrlMappings.SAVE_HSN_CODE)
	public ResponseEntity<Object> saveHsnCode(@RequestParam("hsnCode") String hsnCode, 
			@RequestHeader("Authorization") String accessToken){
		HsnCode code = hsnCodeService.saveHSNCode(hsnCode);
		if(code != null) {
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, code);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILED, code);
	}
	
	@PutMapping(UrlMappings.UPDATE_HSN_CODE)
	public ResponseEntity<Object> updateHsnCode(@RequestParam("id") long id, @RequestParam("updatedHsnCode") String updatedHsnCode, 
			@RequestHeader("Authorization") String accessToken){
		HsnCode updatedData = hsnCodeService.updateHsnCode(id, updatedHsnCode);
		if(updatedData != null) {
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, updatedData);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILED, updatedData);
	}
	
	@DeleteMapping(UrlMappings.DELETE_HSN_CODE)
	public ResponseEntity<Object> deleteHsnCode(@RequestHeader("Authorization") String accessToken, @RequestParam("id") long id){
		HsnCode data = hsnCodeService.deleteHsnData(id);
		if(data != null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, "");
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILED, "");
	}
}
