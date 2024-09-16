package com.krishna.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.Interfaces.IPayrollService;
import com.krishna.Interfaces.IPayslipService;
import com.krishna.domain.Payslip;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

/**
 * 
 * @author shivangi
 *
 *The Payslip Controller
 */
@RestController
public class PayslipController {
	
	Logger log=LoggerFactory.getLogger(PayrollController.class);
	
	@Autowired
	IPayslipService payslipService;
	
	@Autowired
	IPayrollService payRollService;
	
	@RequestMapping(method = RequestMethod.GET, value = UrlMappings.GET_PAYSLIP)
	ResponseEntity<Object> getPayslip(@RequestHeader("Authorization") String accessToken,@RequestParam long payrollId,@RequestParam int month,@RequestParam int year) {
		Map<String, Object> payslipData = payslipService.getPayslip(payrollId,month+1,year);
		if (payslipData != null)
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.FETCHED_PAYSLIP_SUCCESSFULLY,payslipData);
		else
			return ResponseHandler.errorResponse(ConstantUtility.UNABLE_FETCH_PAYSLIP, HttpStatus.EXPECTATION_FAILED);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = UrlMappings.GENERATE_PAYSLIPS)
	ResponseEntity<Object> generatePayslip(@RequestHeader("Authorization") String accessToken, @RequestParam int month,@RequestParam int year,@RequestParam List<Integer> payrollIds) {
		List<Payslip> payslipList = payslipService.generatePayslip(accessToken, month+1, year,payrollIds);
		payRollService.flushPayrollCache();
		if (!payslipList.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, "Payslips Generated SuccessFully! ",payslipList);
		else
			return ResponseHandler.errorResponse("Unable to generate Payslips", HttpStatus.EXPECTATION_FAILED);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = UrlMappings.SEND_PAYSLIP)
	ResponseEntity<Object> sendPayslip(@RequestHeader("Authorization") String accessToken, @RequestParam int month,@RequestParam int year,@RequestParam long userId) {
		boolean mailSent = payslipService.sendPayslip(month+1,year,userId,accessToken);
		if (mailSent)
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, "Mail Sent SuccessFully! ",mailSent);
		else
			return ResponseHandler.errorResponse("Unable to generate Payslips", HttpStatus.EXPECTATION_FAILED);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = UrlMappings.CHANGE_PAYROLL_STATUS_ON_EXPORT)
	ResponseEntity<Object> changePayrollStatusOnExport(@RequestHeader("Authorization") String accessToken,@RequestParam List<Integer> payrollIds) {
		Map<String, Object> payrolls = payslipService.changePayrollStatusOnExport(accessToken,payrollIds);
		Object exportedPayrolls=payrolls.get("alreadyExported");
		Object newlyExported=payrolls.get("newlyExported");
		payRollService.flushPayrollCache();
		if(exportedPayrolls!=null)
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, "Payslips of few users have already been Exported! ",exportedPayrolls);
		if (newlyExported!=null)
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, "Payslips Exported SuccessFully! ",payrolls);
		else
			return ResponseHandler.errorResponse("Unable to export Payslips", HttpStatus.EXPECTATION_FAILED);
	}
	
}
