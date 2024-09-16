package com.krishna.resource.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.Interfaces.IConsolidatedService;
import com.krishna.Interfaces.PayrollTrendsService;
import com.krishna.domain.averagebilling.AverageBillingCompliance;
import com.krishna.dto.AverageBillingRateDto;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

@RestController
public class AverageBillingComplianceController {
	
	@Autowired
	private IConsolidatedService consolidatedService;
	
	@Autowired 
	private PayrollTrendsService payrollTrendService;
	
	//@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN','RESOURCING','RESOURCING_ADMIN','BU_HEAD')")
	@PostMapping(UrlMappings.GET_AVERAGE_BILLING)
	public ResponseEntity<Object> getAverageBilling(@RequestHeader("Authorization") String accessToken, @RequestBody AverageBillingRateDto averageBillingRateDto){
		averageBillingRateDto.setMonth(averageBillingRateDto.getMonth()+1);
		List<Object> data=consolidatedService.getAverageBilling(accessToken,averageBillingRateDto,"Token");
		if(data.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.UNABLE_TO_FETCH_DATA, data);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY, data);
	}

	@PostMapping(UrlMappings.GET_LIFETIME_AVERAGE_BILLING)
	public ResponseEntity<Object> getLifetimeAverageBilling(@RequestHeader("Authorization") String accessToken, @RequestBody AverageBillingRateDto averageBillingRateDto){
		averageBillingRateDto.setMonth(averageBillingRateDto.getMonth()+1);
		List<Map<String, Object>> data=consolidatedService.getLifetimeBilling(accessToken,averageBillingRateDto,"Token");
		if(data.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.UNABLE_TO_FETCH_DATA, data);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY, data);
	}
	
	@PostMapping(UrlMappings.GET_BU_AVERAGE_BILLING)
	public ResponseEntity<Object> getAverageBillingForBuDashboard(@RequestHeader("Authorization") String accessToken, @RequestBody AverageBillingRateDto averageBillingRateDto){
		averageBillingRateDto.setMonth(averageBillingRateDto.getMonth()+1);
		List<Object> data=consolidatedService.getAverageBilling(accessToken,averageBillingRateDto,"");
		if(data.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.UNABLE_TO_FETCH_DATA, data);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY, data);
	}

	@PostMapping(UrlMappings.GET_BU_DASHBOARD_AVERAGE_BILLING)
	public ResponseEntity<Object> getAvrgBillingForBuDashboard(@RequestHeader("Authorization") String accessToken, @RequestBody AverageBillingRateDto averageBillingRateDto){
		averageBillingRateDto.setMonth(averageBillingRateDto.getMonth()+1);
		List<Object> data=consolidatedService.getAverageBilling(accessToken,averageBillingRateDto,"");
		if(data.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.UNABLE_TO_FETCH_DATA, data);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY, data);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN','RESOURCING','RESOURCING_ADMIN','BU_HEAD')")
	@GetMapping(UrlMappings.SEND_BILLING_COMPLIANCE_MAIL)
	public ResponseEntity<Object> sendBillingComplianceMail(@RequestHeader("Authorization") String accessToken, @RequestParam int month,@RequestParam int year,@RequestParam Long projectId){
		Boolean mailSent=consolidatedService.sendBillingComplianceMail(accessToken,projectId,month+1,year);
		if(mailSent!=null) {
			if(mailSent)
				return ResponseHandler.generateResponse(HttpStatus.OK, true, "Mail Sent SuccessFully", mailSent);
			else 
				return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unable to send mail", mailSent);
		}
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "No Project Data or Manager Found", mailSent);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN','RESOURCING','RESOURCING_ADMIN','BU_HEAD')")
	@PostMapping(UrlMappings.SEND_BU_BILLING_COMPLIANCE_MAIL)
	public ResponseEntity<Object> sendBillingRateMailToHeads(@RequestHeader("Authorization") String accessToken, @RequestParam int month,@RequestParam int year,@RequestParam String businessVertical){
		Boolean mailSent=consolidatedService.sendBillingRateMailToHeads(accessToken,month+1,year,businessVertical);
		if(mailSent!=null) {
			if(mailSent)
				return ResponseHandler.generateResponse(HttpStatus.OK, true, "Mail Sent SuccessFully", mailSent);
			else 
				return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unable to send mail", mailSent);
		}
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "No Project Data or Manager Found", mailSent);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN','RESOURCING','RESOURCING_ADMIN','BU_HEAD')")
	@PostMapping(UrlMappings.SAVE_AVERAGE_BILLING_COMPLIANCE_COMMENTS)
	public ResponseEntity<Object> saveABComplianceComments(@RequestHeader("Authorization") String accessToken, 
			@RequestParam int month, @RequestParam int year, @RequestParam String comments, @RequestParam Long projectId) {
		AverageBillingCompliance avgBillingRateComp = payrollTrendService.saveAverageBillingCompliance(month+1, year, comments, projectId);
		if(avgBillingRateComp != null) {
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, avgBillingRateComp);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILED, avgBillingRateComp);
		}
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN','RESOURCING','RESOURCING_ADMIN')")
	@PostMapping(UrlMappings.DELETE_COMPLIANCE_COMMENTS)
	public ResponseEntity<Object> deleteBillingComplianceComment(@RequestHeader("Authorization") String accessToken, 
			@RequestParam int month, @RequestParam int year, @RequestParam Long projectId) {
		AverageBillingCompliance avgBillingRateComp = payrollTrendService.saveAverageBillingCompliance(month+1, year, "", projectId);
		if(avgBillingRateComp != null) {
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, avgBillingRateComp);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILED, avgBillingRateComp);
		}
	}


}
