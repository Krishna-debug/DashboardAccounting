package com.krishna.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.dto.EmailTemplateDto;
import com.krishna.dto.EmailTemplateResponseDto;
import com.krishna.service.EmailTemplateService;
import com.krishna.util.UrlMappings;

import io.swagger.annotations.ApiOperation;

/**
 * 
 * @author shivangi
 *
 */

@RestController
public class EmailTemplatesController {

	@Autowired
	EmailTemplateService emailTemplateService;

	public static final Logger LOGGER = LoggerFactory.getLogger(EmailTemplatesController.class);

	@ApiOperation(value = "Create new email template")
	@PostMapping(UrlMappings.CREATE_EMAIL_TEMPLATE)
	public ResponseEntity<Object> createEmailTemplate(@RequestHeader("Authorization") String accessToken,
			@RequestBody EmailTemplateDto emailTemplateDto) {
		LOGGER.info("email template controller calls");
		return emailTemplateService.createEmailTemplate(emailTemplateDto);

	}

	@ApiOperation(value = "Get all the email template dashboard have in db")
	@GetMapping(UrlMappings.CREATE_EMAIL_TEMPLATE)
	public EmailTemplateResponseDto getEmailTemplates()

	{
		return emailTemplateService.getEmailTemplates();

	}

	@ApiOperation(value = "Get email template by Id")
	@GetMapping(UrlMappings.CREATE_EMAIL_TEMPLATE + "/{templateId}")
	public EmailTemplateResponseDto getEmailTemplateById(@PathVariable("templateId") Long id) {
		return emailTemplateService.getEmailTemplateById(id);
	}

	@ApiOperation(value = "Delete email template")
	@PostMapping(UrlMappings.CREATE_EMAIL_TEMPLATE + "/{templateId}")
	public EmailTemplateResponseDto deleteEmailTemplate(@RequestHeader("Authorization") String accessToken,
			@PathVariable("templateId") Long id) {

		return emailTemplateService.deleteEmailTemplate(id);

	}

	@ApiOperation(value = "Update email template dashboard have in db")
	@PutMapping(UrlMappings.CREATE_EMAIL_TEMPLATE)
	public EmailTemplateResponseDto updateEmailTemplate(@RequestHeader("Authorization") String accessToken,
			@RequestBody EmailTemplateDto emailTemplateDto) {
		LOGGER.info("controller calls");
		return emailTemplateService.updateEmailTemplate(emailTemplateDto);

	}

}
