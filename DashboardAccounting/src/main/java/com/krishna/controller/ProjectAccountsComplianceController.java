package com.krishna.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.dto.AccountsCompliantStatusChangeDto;
import com.krishna.service.ProjectAccountComplianceService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

@RestController
public class ProjectAccountsComplianceController {

	@Autowired
	ProjectAccountComplianceService projectAccountComplianceService;

	@PostMapping(UrlMappings.SEND_MAIL_ON_ACCOUNTS_COMPLIANT_STATUS_CHANGE)

	public ResponseEntity<Object> sendMailOnAccountsCompliantStatusChange(
			@RequestHeader("Authorization") String accessToken,
			AccountsCompliantStatusChangeDto accountsCompliantStatusChangeDto) {
		Boolean result = projectAccountComplianceService.sendMailOnAccountsCompliantStatusChange(accessToken,
				accountsCompliantStatusChangeDto);
		if (result == true)
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, result);
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILED, null);

	}
}
