package com.krishna.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.Interfaces.LeaveCostPercentageService;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

@RestController
public class LeaveCostPercentageController {
	
	@Autowired
	private LeaveCostPercentageService leaveCostPercentService;
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_ALL_LEAVECOST_PERCENT)
	public ResponseEntity<Object> getAllleaveCost(@RequestHeader("Authorization") String auth){
		List<Map<String, Object>> response = leaveCostPercentService.getAllLeaveCostPercentage();
		if(response.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT,  false, "No data found!", response);
		}
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "Data fetched successfully!", response);
	}
	
	@PreAuthorize("hasRole('ROLE_DASHBOARD_ADMIN')")
	@PostMapping(UrlMappings.ADD_LEAVECOST_PERCENT)
	public ResponseEntity<Object> saveLeaveCost(@RequestParam Double leaveCostPercentage, @RequestHeader("Authorization") String auth){
		Map<String, Object> response = leaveCostPercentService.saveLeaveCostPercentage(leaveCostPercentage, auth);
		if(response.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST,  false, "Internal server error", response);
		}
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "Data saved successfully!", response);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@PutMapping(UrlMappings.UPDATE_LEAVECOST_PERCENT)
	public ResponseEntity<Object> updateLeaveCostData(@RequestParam Double leaveCostPercentage, @RequestHeader("Authorization") String auth, long id){
		Map<String, Object> responseObj = leaveCostPercentService.updateLeaveCostPercentage(leaveCostPercentage, auth, id);
		if(responseObj.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST,  false, "Unable to update record!",responseObj);
		}
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "Data updated successfully!", responseObj);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@DeleteMapping(UrlMappings.DELETE_LEAVECOST_PERCENT)
	public ResponseEntity<Object> deleteLeaveCost(long id, @RequestHeader("Authorization") String auth){
		boolean isDeleted = leaveCostPercentService.deleteLeaveCostPercentage(id, auth);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "Deleted successfully!", isDeleted);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_CURRENT_LEAVE_COST_PERCENT)
	public ResponseEntity<Object> getCurrentLeaveCostPercent(@RequestHeader("Authorization") String accessToken){
		Map<String,Object> currentLeaveCost = leaveCostPercentService.getCurrentLeaveCostPercent();
		if(currentLeaveCost!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK,  false, "Current Leave Cost Percentage Fetched", currentLeaveCost);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, true, "Unable to fetch Leave Cost!", currentLeaveCost);
	}
}
