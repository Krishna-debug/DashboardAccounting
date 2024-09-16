package com.krishna.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.Interfaces.IPayrollService;
import com.krishna.domain.Payroll;
import com.krishna.service.PayRegisterService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

@RestController
public class PayrollNodeController {
	
	@Autowired
	IPayrollService payRollService;
	
	@Autowired
	PayRegisterService payRegisterService;
	
	
	@RequestMapping(method=RequestMethod.GET,value=UrlMappings.GET_BU_DASHBOARD_MONTHWISE_PAYROLLS)
	public ResponseEntity<Object> getBuWisePayrollsForMonth(@RequestHeader("Authorization") String accessToken,
			@RequestParam int month, @RequestParam int year, @RequestParam(required = false) String timesheetCompliance,
			@RequestParam String businessVertical, @RequestParam(required = false) String priority) {
		List<Object> usersData = payRegisterService.getUsersForPayregister(accessToken, "All",
				month + 1, year);
		List<Map<String, Object>> allUsers = usersData.stream().map(obj -> (Map<String, Object>) obj).collect(Collectors.toList());
		List<Long> buUsers = allUsers.stream()
			    .filter(user -> user.get("businessVertical").equals(businessVertical) && user.get("id") != null)
			    .map(user -> Long.parseLong(user.get("id").toString()))
			    .collect(Collectors.toList());
		List<Payroll> payrolls = payRollService.getBuWisePayrollsForMonth(month + 1, year, buUsers, priority,
				businessVertical);
		if (payrolls != null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, payrolls);
		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.OK);
	}
	
	@RequestMapping(method=RequestMethod.GET,value=UrlMappings.GET_BU_DASHBOARD_PAYROLLS)
	public ResponseEntity<Object> getPayrolls(@RequestHeader("Authorization")String accessToken,@RequestParam int month,@RequestParam int year,@RequestParam(required = false) String timesheetCompliance, @RequestParam String businessVertical, @RequestParam(required = false) String priority){
		List<Object> usersData = payRegisterService.getUsersForPayregister(accessToken, "All",
				month + 1, year);
		List<Map<String, Object>> allUsers = usersData.stream().map(obj -> (Map<String, Object>) obj).collect(Collectors.toList());
		List<Long> buUsers = allUsers.stream()
			    .filter(user -> user.get("businessVertical").equals(businessVertical) && user.get("id") != null)
			    .map(user -> Long.parseLong(user.get("id").toString()))
			    .collect(Collectors.toList());
		Map<String,Object> timesheets=payRollService.getPayrollUsersTimesheetByUserList(accessToken, month+1, year);
		List<Payroll> payrolls = payRollService.getBuWisePayrollsForMonth(month + 1, year, buUsers, priority,
				businessVertical);
		List<Object> payrollData= payRollService.getBuWisePayrolls(month+1,year,accessToken,timesheetCompliance,priority,timesheets,payrolls,usersData,buUsers);
		
		if (payrolls != null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, payrollData);
		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = UrlMappings.GET_BU_DASHBOARD_USER_LEAVES_DATA)
	public ResponseEntity<Object> userLeavesdataForPayroll(@RequestHeader("Authorization") String accessToken,@RequestParam int month,@RequestParam int year,@RequestParam long userId) {
		ArrayList<Object> attendanceData = payRollService.userLeavesdataForPayroll(accessToken, userId,month+1,year);
		if (!attendanceData.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, attendanceData);
		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.OK);
	}

}
