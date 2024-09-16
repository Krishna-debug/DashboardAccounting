package com.krishna.service;

import org.springframework.stereotype.Service;

import com.krishna.dto.AccountsCompliantStatusChangeDto;

@Service
public interface ProjectAccountComplianceService {

	Boolean sendMailOnAccountsCompliantStatusChange(String accessToken,
			AccountsCompliantStatusChangeDto accountsCompliantStatusChangeDto);

}
