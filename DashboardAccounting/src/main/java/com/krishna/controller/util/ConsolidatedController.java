package com.krishna.controller.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.Interfaces.IConsolidatedService;
import com.krishna.domain.UserModel;
import com.krishna.security.JwtValidator;
import com.krishna.service.AccessUtility.AccessUtilityService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;
	
@RestController
public class ConsolidatedController {
	
	@Autowired
	IConsolidatedService consolidatedService;
	
	@Autowired
	AccessUtilityService utilityService;
	
	@Autowired
	JwtValidator validator;
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_SALARY_RECONCILIATION)
	public ResponseEntity<Object> getUserDataForConsolidatedPage(@RequestHeader("Authorization") String accessToken,@RequestParam int month,@RequestParam int year,@RequestParam String userStatus,@RequestParam(required = false) String businessVertical){
		List<Map<String, Object>> users=consolidatedService.getUsersForSalaryReconcilliation(accessToken,month+1,year,userStatus,businessVertical);
		Map<String,Object> data=consolidatedService.getUserDataForConsolidatedPage(accessToken,month+1,year,userStatus,businessVertical,users);
		if(data==null || ( (data != null) && data.isEmpty()))
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unable to fetch data", data);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY, data);
	}
	
	//@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_USERS_SALARY_RECONCILIATION)
	public ResponseEntity<Object> getUsersForSalaryReconcilliation(@RequestHeader("Authorization") String accessToken,@RequestParam int month,@RequestParam int year,@RequestParam String userStatus,@RequestParam(required = false) String businessVertical){
		UserModel currentLoginUser = validator.tokenbValidate(accessToken);
		if((businessVertical==null || businessVertical.equals("")) && !currentLoginUser.getRoles().contains(ConstantUtility.ROLE_ACCOUNTS) && !currentLoginUser.getRoles().contains(ConstantUtility.ROLE_ACCOUNTS_ADMIN))
			return ResponseHandler.errorResponse(ConstantUtility.NOT_AUTHORIZED, HttpStatus.FORBIDDEN, null);
		consolidatedService.flushUsersCache();
		List<Map<String, Object>> data=consolidatedService.getUsersForSalaryReconcilliation(accessToken,month+1,year,userStatus,businessVertical);
		if(data.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Unable to fetch users For Salary Reconciliation", data);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY, data);
	}
	
	//@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.DIRECT_COST_FORECAST)
	public ResponseEntity<Object> getDirectCostForecast(@RequestHeader("Authorization") String accessToken,@RequestParam int month,@RequestParam int year,@RequestParam(required = false) String businessVertical){
		UserModel currentLoginUser = validator.tokenbValidate(accessToken);
		if((businessVertical==null || businessVertical.equals("")) && !currentLoginUser.getRoles().contains(ConstantUtility.ROLE_ACCOUNTS) && !currentLoginUser.getRoles().contains(ConstantUtility.ROLE_ACCOUNTS_ADMIN))
			return ResponseHandler.errorResponse(ConstantUtility.NOT_AUTHORIZED, HttpStatus.FORBIDDEN, null);
		List<Map<String, Object>> users=consolidatedService.getUsersForSalaryReconcilliation(accessToken,month+1,year,"All",businessVertical);
		Map<String,Object> data=consolidatedService.getDirectCostForecast(accessToken,month+1,year,"All",businessVertical,users);
		if(data==null || ( (data != null) && data.isEmpty()))
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unable to fetch data", data);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY, data);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_SALARY_RECONCILIATION_IC)
	public ResponseEntity<Object> getIndirectCostForSalaryReconciliation(@RequestHeader("Authorization") String accessToken,@RequestParam int month,@RequestParam int year,@RequestParam String userStatus,@RequestParam(required = false) String businessVertical){
		List<Map<String, Object>> users=consolidatedService.getUsersForSalaryReconcilliation(accessToken,month+1,year,userStatus,businessVertical);
		Map<String,Object> data=consolidatedService.getIndirectCostForSalaryReconciliation(accessToken,month+1,year,userStatus,businessVertical,users);
		if(data==null)
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unable to fetch IC for users", data);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY, data);
	}
	
	//@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.INDIRECT_COST_FORECAST)
	public ResponseEntity<Object> getIndirectCostForecast(@RequestHeader("Authorization") String accessToken,@RequestParam int month,@RequestParam int year,@RequestParam(required = false) String businessVertical){
		UserModel currentLoginUser = validator.tokenbValidate(accessToken);
		if((businessVertical==null || businessVertical.equals("")) && !currentLoginUser.getRoles().contains(ConstantUtility.ROLE_ACCOUNTS) && !currentLoginUser.getRoles().contains(ConstantUtility.ROLE_ACCOUNTS_ADMIN))
			return ResponseHandler.errorResponse(ConstantUtility.NOT_AUTHORIZED, HttpStatus.FORBIDDEN, null);
		List<Map<String, Object>> users=consolidatedService.getUsersForSalaryReconcilliation(accessToken,month+1,year,"All",businessVertical);
		Map<String,Object> data=consolidatedService.getIndirectCostForecast(accessToken,month+1,year,"All",businessVertical,users);
		if(data==null)
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unable to fetch IC for users", data);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY, data);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_SALARY_DIFFERENCE)
	public ResponseEntity<Object> getSalaryDifference(@RequestHeader("Authorization") String accessToken,@RequestParam int month,@RequestParam int year){
		Map<String,Object> data=consolidatedService.getSalaryDifference(accessToken,month+1,year);
		if(data==null)
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.UNABLE_TO_FETCH_DATA, data);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY, data);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_USER_SNAPSHOT)
	public ResponseEntity<Object> getUserSnapShot(@RequestHeader("Authorization") String accessToken, @RequestParam Long projectId){
		List<Object> data=consolidatedService.getUserSnapShot(accessToken,projectId);
		if(data.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.UNABLE_TO_FETCH_DATA, data);
			}
		else {
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY, data);
		}
	}
	
	@GetMapping(UrlMappings.GET_PROJECT_BILLING)
	public ResponseEntity<Object> getBillingComplianceProjectData(@RequestHeader("Authorization") String accessToken,
			@RequestParam long projectId, @RequestParam Integer month, @RequestParam Integer year) {
		boolean isAuthorised = utilityService.hasProjectAccess(projectId, accessToken);
		if (isAuthorised) {
			Map<String, Object> data = consolidatedService.getBillingComplianceProjectData(accessToken, projectId, month+1, year);
			if (data == null)
				return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false,
						ConstantUtility.UNABLE_TO_FETCH_DATA, data);
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY,
					data);
		}
		return ResponseHandler.errorResponse("Access Denied", HttpStatus.FORBIDDEN, null);
	}

	@GetMapping(UrlMappings.GET_PROJECT_BILLING_YEARLY)
	public ResponseEntity<Object> getYearlyBillingComplianceProjectData(@RequestHeader("Authorization") String accessToken,
			@RequestParam long projectId, @RequestParam Integer year) {
		
			int month = 0;
			Map<String,Object> lifeTimeAverageBilling=new HashMap<>();
			List<Map<String, Object>> res = new ArrayList<>();
			Double totalBilling=0D;
			while((year.equals(Calendar.getInstance().get(Calendar.YEAR)) && month <= Calendar.getInstance().get(Calendar.MONTH)) ||
			(!year.equals(Calendar.getInstance().get(Calendar.YEAR)) && month < 12)){
				Map<String, Object> data = consolidatedService.getBillingComplianceProjectData(accessToken, projectId, month+1, year);
				res.add(data);
				month++;
				totalBilling= totalBilling + Double.parseDouble(data.get("expectedBillingRate").toString());
			}
			totalBilling=totalBilling/12;
			lifeTimeAverageBilling.put("yearlyData", res);
			lifeTimeAverageBilling.put("lifetimeBilling", totalBilling);
			if (!res.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY,
						lifeTimeAverageBilling);
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false,
						ConstantUtility.UNABLE_TO_FETCH_DATA, res);
	
		
	}

	@GetMapping(UrlMappings.PRO_DASHBOARD_GET_PROJECT_BILLING_YEARLY)
	public ResponseEntity<Object> getYearlyBillingComplianceProjectDataDashboard(@RequestHeader("Authorization") String accessToken,
			@RequestParam long projectId, @RequestParam Integer year) {
		
			int month = 0;
			List<Map<String, Object>> res = new ArrayList<>();
			while((year.equals(Calendar.getInstance().get(Calendar.YEAR)) && month <= Calendar.getInstance().get(Calendar.MONTH)) ||
			(!year.equals(Calendar.getInstance().get(Calendar.YEAR)) && month < 12)){
				Map<String, Object> data = consolidatedService.getBillingComplianceProjectData(accessToken, projectId, month+1, year);
				res.add(data);
				month++;
			}

			if (!res.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY,
			res);
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false,
						ConstantUtility.UNABLE_TO_FETCH_DATA, res);
	
		
	}
	
}
