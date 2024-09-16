package com.krishna.service;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;

import com.krishna.dto.AccountsCompliantStatusChangeDto;
import com.krishna.security.JwtValidator;

@Service
public class ProjectAccountComplianceServiceImpl implements ProjectAccountComplianceService {
	@Autowired
	JwtValidator validator;

	@Autowired
	MailService mailService;

	@Override
	public Boolean sendMailOnAccountsCompliantStatusChange(String accessToken,
			AccountsCompliantStatusChangeDto accountsCompliantStatusChangeDto) {
		Boolean result = false;
		RestTemplate restTemplate = new RestTemplate();
		final String uri = "http://localhost:9000/api/v1/accountUtility/accountsCompliantStatusChange?projectId="
				+ accountsCompliantStatusChangeDto.getProjectId() + "&comments="
				+ accountsCompliantStatusChangeDto.getComment() + "&issueType="
				+ accountsCompliantStatusChangeDto.getIssueType() + "&compliantStatus="
				+ accountsCompliantStatusChangeDto.getCompliantStatus() + "&callFrom="
				+ accountsCompliantStatusChangeDto.getCallFrom();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + accessToken);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.add("user-agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
		ResponseEntity<HashMap> response = restTemplate.exchange(uri, HttpMethod.GET, entity, HashMap.class);
		Map<String, Object> data = response.getBody();
		Object projectComplianceIssueType = data.get("projectComplianceIssueType");
		Object projectName = data.get("projectName");
		Object projectStatus = data.get("projectStatus");
		Object projectManagerName = data.get("projectManagerName");
		Object reviewEligible = data.get("accountsCompliant");
		Object billingCompliancePerc = data.get("billingCompliancePerc");
		Object projectmanagerEmail = data.get("projectmanagerEmail");
		List<Object> ccList = Arrays.asList(data.get("ccList"));
		String[] ccArray = new String[ccList.size()];
		ccList.forEach(cc -> {
			int i = 0;
			ccArray[i] = cc.toString();
			i++;
		});

		String url = ""/*
						 * grailsApplication.config.grails.codeReviewFeedbackURL+"pId="+project.id+
						 * "&complianceHisoryId="+complianceHistoryId+
						 * "&cname=portfolio&aname=replyByManagerOnPaymentComplianceResponse"
						 */;
		String guidelineUrl = "https://docs.google.com/document/d/1s0gaqW66VXCxM78dxUz9hKHDscecIA0orFfPvkuhkqY/edit#heading=h.u1iprsv409zh";
		LocalDate now = LocalDate.now();
		LocalDate earlier = now.minusMonths(1);

		Month month = earlier.getMonth();
		String subject = "Account Compliance  || " + month;
		String userNames = "AUTOMATED";

		Context context = new Context();
		context.setVariable("projectName", projectName);
		context.setVariable("changedStatus", projectStatus);
		context.setVariable("issueType", projectComplianceIssueType);
		context.setVariable("managerName", projectManagerName);
		context.setVariable("url", url);
		context.setVariable("guidelineUrl", guidelineUrl);
		context.setVariable("month", month);
		context.setVariable("data", accountsCompliantStatusChangeDto.getContent());
		context.setVariable("currentDate", new Date());
		context.setVariable("reviewEligible", reviewEligible);
		context.setVariable("billingCompliancePerc", billingCompliancePerc);
		context.setVariable("actualBilling", accountsCompliantStatusChangeDto.getActualBilling());
		context.setVariable("expectedBilling", accountsCompliantStatusChangeDto.getExpectedBilling());
		context.setVariable("comment", accountsCompliantStatusChangeDto.getComment());
		context.setVariable("differencePerc", accountsCompliantStatusChangeDto.getDifferencePerc());

		try {
			mailService.sendScheduleHtmlMailWithCc(projectmanagerEmail.toString(), subject, context,
					"Accounts-Compliance-Flag-New", ccArray);
			result = true;
		} catch (Exception ex) {
			result = false;
		}
		return result;
	}

}
