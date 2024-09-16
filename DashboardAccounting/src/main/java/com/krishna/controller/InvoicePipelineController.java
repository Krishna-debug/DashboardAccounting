package com.krishna.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.Interfaces.IInvoicePipelineService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

@RestController
public class InvoicePipelineController {
	
	@Autowired
	IInvoicePipelineService invoicePipelineService;
	

	
	
	@GetMapping(UrlMappings.GET_INVOICE_PIPELINE)
	public ResponseEntity<Object> getInvoicePipeline(@RequestHeader String authorization,@RequestParam int month,@RequestParam int year, @RequestParam String projectType, @RequestParam String businessVerticals) {
		

		List<Object> projects=invoicePipelineService.getInvoicePipeline(month+1,year,projectType,businessVerticals);
		if(!projects.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Projects Fetched Successfully", projects);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unbale to fetch Projects", projects);
	}

	@GetMapping(UrlMappings.GET_BU_INVOICE_PIPELINE)
	public ResponseEntity<Object> getBuInvoicePipeline(@RequestHeader String authorization,@RequestParam int month,@RequestParam int year, @RequestParam String projectType, @RequestParam String businessVerticals) {
		

		List<Object> projects=invoicePipelineService.getInvoicePipeline(month+1,year,projectType,businessVerticals);
		if(!projects.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Projects Fetched Successfully", projects);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unbale to fetch Projects", projects);
	}
	
	@GetMapping("api/v1/invoicePipeline/getActualHoursForInvoicePipeline")
	public ResponseEntity<Object> getActualHoursForInvoicePipeline(@RequestHeader String authorization,@RequestParam int month,@RequestParam int year) {

		List<Object> projects=invoicePipelineService.getActualHoursForInvoicePipeline(month+1,year,authorization);
		if(!projects.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Actual Hours Fetched Successfully", projects);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unbale to fetch Actual Hours", projects);
	}

	@GetMapping(UrlMappings.GET_ACTUAL_HRS_INVOICE_PIPELINE)
	public ResponseEntity<Object> getBuActualHoursForInvoicePipeline(@RequestHeader String authorization,@RequestParam int month,@RequestParam int year) {

		List<Object> projects=invoicePipelineService.getActualHoursForInvoicePipeline(month+1,year,authorization);
		if(!projects.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Actual Hours Fetched Successfully", projects);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unbale to fetch Actual Hours", projects);
	}

	@PreAuthorize("hasAnyRole('ROLE_DASHBOARD_ADMIN')")
	@PutMapping(UrlMappings.FLUSH_ACTUAL_HOURS)
	public ResponseEntity<Object> flushActualHoursForInvoicePipeline(@RequestHeader String authorization){
		invoicePipelineService.flushActualHoursForInvoicePipeline();
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, null);
	}

}
