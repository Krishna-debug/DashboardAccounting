package com.krishna.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.Interfaces.PayrollTrendsService;
import com.krishna.domain.ExpectedBillingRate;
import com.krishna.domain.averagebilling.AverageBillingCompliance;
import com.krishna.schedulers.Scheduler;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

@RestController
public class PayrollTrendsController {

	@Autowired private PayrollTrendsService payrollTrends;
	@Autowired private Scheduler scheduler;
	
	@GetMapping(UrlMappings.GET_PAYROL_TRENDS)
	public ResponseEntity<Object> getPayrollTrends(@RequestHeader("Authorization") String accessToken, int month, int year, String businessVerticle){
		List<Object> object = payrollTrends.getPayrollTrends(accessToken, month+1, year, businessVerticle);
		if(object != null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true,"Fetched Payroll Trends", object);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unable to get payroll trends", object);
	}
	
	@GetMapping(UrlMappings.GET_MIN_MAX_SALARY_OF_GIVEN_GRADE)
	public ResponseEntity<Object> getMinAndMaxSalaryOfGrade(@RequestHeader("Authorization") String token, @RequestParam String grade){
		Map<String, Object> object = payrollTrends.getMinAndMaxSalaryOfGivenGrade(grade, token);
		if(object != null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true,"Fetched Data", object);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unable to get Data", object);
	}
	
	@PostMapping(UrlMappings.SAVE_EXPECTED_BILLING_RATE)
	public ResponseEntity<Object> saveExpectedBillingRate(@RequestHeader("Authorization") String token, @RequestParam String grade,@RequestParam int month,@RequestParam int year,@RequestParam double billingRate){
		ExpectedBillingRate billingRateObj = payrollTrends.saveExpectedBillingRate(grade, token,month+1,year,billingRate);
		if(billingRateObj != null) {
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true,"Saved Successfully", billingRateObj);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unable to save Data", billingRateObj);
	}
	
	@PreAuthorize("hasAnyRole('ROLE_DASHBOARD_ADMIN')")
	@PostMapping(UrlMappings.CARRY_FORWARD_BILLING_RATE)
	public ResponseEntity<Object> carryForwardBillingRate(@RequestHeader("Authorization") String token, @RequestParam int month,@RequestParam int year){
		scheduler.carryForwardBillingRateForTesting(month+1,year);
		return ResponseHandler.generateResponse(HttpStatus.CREATED, true,"Carry Forward Successfully", "");
	}

	@PreAuthorize("hasAnyRole('ROLE_DASHBOARD_ADMIN','ROLE_ANONYMOUS')")
	@PostMapping(UrlMappings.CARRY_FORWARD_BILLING_RATE_CRON)
	public ResponseEntity<Object> carryForwardBillingRateCron(@RequestHeader("Authorization") String token){
		scheduler.carryForwardBillingRate();
		return ResponseHandler.generateResponse(HttpStatus.CREATED, true,"Carry Forward Successfully", "");
	}
	
}
