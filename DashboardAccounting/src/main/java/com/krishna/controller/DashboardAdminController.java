package com.krishna.controller;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.Interfaces.ICompanyMarginService;
import com.krishna.Interfaces.IPayrollService;
import com.krishna.domain.PayRegister;
import com.krishna.domain.Margin.ProjectExpectedHours;
import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.service.DashboardAdminService;
import com.krishna.service.ProjectMarginService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

/**
 * 
 * @author shivangi
 *
 */
@RestController
public class DashboardAdminController {
	
	@Autowired
	DashboardAdminService dashboardAdminService;
	
	@Autowired
	ICompanyMarginService companyMarginService;
	
	@Autowired
	ProjectMarginService projectMarginService;
	
	@Autowired
	IPayrollService payrollService;
	

	
	@RequestMapping(method=RequestMethod.GET,value="/api/v1/test")
	public String test(@RequestHeader("Authorization")String accessToken) {
		return "Project Invoice Service is Up..........";
	}
	
	@PreAuthorize("hasRole('ROLE_DASHBOARD_ADMIN')")
	@RequestMapping(method=RequestMethod.PUT,value=UrlMappings.SET_ANNUAL_CTC)
	public ResponseEntity<Object> setAnnualCtc(@RequestHeader("Authorization") String accessToken){
		List<PayRegister> payregisters=dashboardAdminService.setAnnualCtc();
		if(!payregisters.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.ADDED_SUCCESSFULLY, payregisters);
		}
		return ResponseHandler.errorResponse(ConstantUtility.EDITING_FAILED, HttpStatus.CONFLICT);
	}
	
	@PreAuthorize("hasRole('ROLE_DASHBOARD_ADMIN')")
	@RequestMapping(method=RequestMethod.PUT,value=UrlMappings.SET_EFFECTIVE_DATE)
	public ResponseEntity<Object> setEffectiveDate(@RequestHeader("Authorization") String accessToken,@RequestParam int userId,@RequestParam long effectiveDate){
		PayRegister payregister=dashboardAdminService.setEffectiveDate(userId,effectiveDate);
		if(payregister!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, payregister);
		}
		return ResponseHandler.errorResponse(ConstantUtility.EDITING_FAILED, HttpStatus.CONFLICT);
	}
	
	@RequestMapping(method=RequestMethod.PUT,value=UrlMappings.FLUSH_CACHES)
	public ResponseEntity<Object> flushCache(@RequestHeader("Authorization") String accessToken){
		companyMarginService.flushDirectCostCache();
		companyMarginService.flushTeamData();
		projectMarginService.flushTotalBuMargins();
	    projectMarginService.flushBuMargins();
	    projectMarginService.flushLifeTimeIndirectCost();
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, null);
	}
	
	@RequestMapping(method=RequestMethod.PUT,value=UrlMappings.FLUSH_TIMESHEET_CACHE)
	public ResponseEntity<Object> flushTimesheetCache(@RequestHeader("Authorization") String accessToken){
		payrollService.flushTimesheetCache();
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, null);
	}
	
	@RequestMapping(method = RequestMethod.PUT, value = UrlMappings.REPUTCOMPANYCACHE)
	public ResponseEntity<Object> reputCompanyCache(@RequestHeader("Authorization") String accessToken) {
		String hasRun = dashboardAdminService.reputCompanyCache(accessToken);
		if (hasRun != null && hasRun.equals("Run successfully")) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Cache updated", hasRun);
		} else if (hasRun != null)
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.UNABLE_TO_FETCH_DATA, hasRun);
		else
			return ResponseHandler.errorResponse(ConstantUtility.UNABLE_TO_FETCH_DATA, HttpStatus.EXPECTATION_FAILED,
					null);
	}
	
	@RequestMapping(method=RequestMethod.POST,value=UrlMappings.SAVE_PROJECT_SNAPSHOT)
	public ResponseEntity<Object> saveProjectSnapshot(@RequestHeader("Authorization") String accessToken,@RequestParam Long projectId){
		ProjectExpectedHours projectExpectedHours=projectMarginService.saveProjectSnapshot(accessToken,projectId,0,0);
		if(projectExpectedHours!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Data Saved", projectExpectedHours);
		}
		return ResponseHandler.errorResponse(ConstantUtility.UNABLE_TO_FETCH_DATA, HttpStatus.EXPECTATION_FAILED, null);
	}
	
	@RequestMapping(method=RequestMethod.POST,value=UrlMappings.SAVE_PROJECT_SNAPSHOT_MONTH)
	public ResponseEntity<Object> saveProjectSnapshotMonthwise(@RequestHeader("Authorization") String accessToken,@RequestParam Long projectId,@RequestParam int month,@RequestParam int year){
		ProjectExpectedHours projectExpectedHours=projectMarginService.saveProjectSnapshot(accessToken,projectId,month,year);
		if(projectExpectedHours!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Data Saved", projectExpectedHours);
		}
		return ResponseHandler.errorResponse(ConstantUtility.UNABLE_TO_FETCH_DATA, HttpStatus.EXPECTATION_FAILED, null);
	}

}
