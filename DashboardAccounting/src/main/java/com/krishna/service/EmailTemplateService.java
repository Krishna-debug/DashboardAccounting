package com.krishna.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.krishna.controller.EmailTemplatesController;
import com.krishna.domain.EmailTemplate;
import com.krishna.dto.EmailTemplateDto;
import com.krishna.dto.EmailTemplateResponseDto;
import com.krishna.dto.SendEmailTemplate;
import com.krishna.repository.EmailTemplateRepository;
import com.krishna.util.ResponseHandler;

/**
 * 
 * @author shivangi
 *
 */

@Service
public class EmailTemplateService {

	@Autowired
	EmailTemplateRepository emailTempRepo;

	@Autowired
	EmailService emailService;

	public static final Logger LOGGER = LoggerFactory.getLogger(EmailTemplatesController.class);
	public static final String SUCCESS = "success";
	public static final String FAILURE = "failure";

	/**
	 * Create new email template.
	 * 
	 * @param emailTemplateDto
	 * @return ResponseEntity<Object>
	 */
	public ResponseEntity<Object> createEmailTemplate(EmailTemplateDto emailTemplateDto) {
		String templateName = emailTemplateDto.getTemplateName();
		LOGGER.info("email search duplicate value : {}", templateName);
		EmailTemplate emailTemplate = emailTempRepo.findAllByTemplateNameAndIsDeleted(templateName, false);
		if (emailTemplate != null)
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,
					"Sorry!! Duplicate email template name not allowed", null);

		emailTemplate = new EmailTemplate(templateName, emailTemplateDto.getTemplateData());
		LOGGER.info("email template saving in db ");
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "EmailTemplate created successfully",
				emailTempRepo.save(emailTemplate));
	}

	/**
	 * 
	 * @return
	 */
	public EmailTemplateResponseDto getEmailTemplates1() {

		LOGGER.info("fetching email template");
		Iterable<EmailTemplate> allTemplates = emailTempRepo.findAllByIsDeleted(false);
		ArrayList<EmailTemplate> content = new ArrayList<>();
		for (EmailTemplate template : allTemplates) {
			content.add(template);
		}

		return new EmailTemplateResponseDto(content, SUCCESS, "EmailTemplates fetched Successfully");
	}

	public EmailTemplateResponseDto getEmailTemplateById1(Long id) {

		return new EmailTemplateResponseDto(emailTempRepo.findOneByIdAndIsDeleted(id, false), SUCCESS,
				"EmailTemplate fetched Successfully");
	}

	public EmailTemplateResponseDto deleteEmailTemplate1(Long id) {

		EmailTemplate emailTemplate = emailTempRepo.findAllById(id);
		emailTemplate.setDeleted(true);
		emailTempRepo.save(emailTemplate);
		return new EmailTemplateResponseDto(null, SUCCESS, "EmailTemplate deleted Successfully");
	}

	public EmailTemplateResponseDto updateEmailTemplate1(EmailTemplateDto emailTemplateDto) {

		Long id = emailTemplateDto.getId();

		String data = emailTemplateDto.getTemplateData();
		String name = emailTemplateDto.getTemplateName();

		if (id != null && id != 0)
			return new EmailTemplateResponseDto(null, FAILURE, "Id is not valid");

		EmailTemplate emailTemplate = emailTempRepo.findAllById(id);
		if (id != null && id != 0)
			emailTemplate.setId(id);
		if (emailTemplate == null)
			return new EmailTemplateResponseDto(null, FAILURE, "Id is not valid");

		if (data != null)
			emailTemplate.setTemplateData(data);
		if (name != null)
			emailTemplate.setTemplateName(name);

		return new EmailTemplateResponseDto(emailTempRepo.save(emailTemplate), SUCCESS,
				"EmailTemplate updated successfully");

	}

	public EmailTemplateResponseDto getEmailTemplates() {

		Iterable<EmailTemplate> allTemplates = emailTempRepo.findAllByIsDeleted(false);
		ArrayList<EmailTemplate> content = new ArrayList<>();
		for (EmailTemplate template : allTemplates) {
			content.add(template);
		}

		return new EmailTemplateResponseDto(content, SUCCESS, "EmailTemplates fetched Successfully");
	}

	public EmailTemplateResponseDto getEmailTemplateById(Long id) {

		return new EmailTemplateResponseDto(emailTempRepo.findOneByIdAndIsDeleted(id, false), SUCCESS,
				"EmailTemplate fetched Successfully");
	}

	public EmailTemplateResponseDto deleteEmailTemplate(Long id) {

		EmailTemplate emailTemplate = emailTempRepo.findAllById(id);
		emailTemplate.setDeleted(true);
		emailTempRepo.save(emailTemplate);
		return new EmailTemplateResponseDto(null, SUCCESS, "EmailTemplate deleted Successfully");
	}

	public EmailTemplateResponseDto updateEmailTemplate(EmailTemplateDto emailTemplateDto) {

		EmailTemplate emailTemplate = emailTempRepo.findAllById(emailTemplateDto.getId());
		if (emailTemplateDto.getId() != null && emailTemplateDto.getId() != 0)
			emailTemplate.setId(emailTemplateDto.getId());
		Long id = emailTemplateDto.getId();
		String data = emailTemplateDto.getTemplateData();
		String name = emailTemplateDto.getTemplateName();
		if (id != null && id != 0)
			emailTemplate.setId(id);
		if (data != null)
			emailTemplate.setTemplateData(data);
		if (name != null)
			emailTemplate.setTemplateName(name);
		emailTempRepo.save(emailTemplate);

		return new EmailTemplateResponseDto(emailTempRepo.save(emailTemplate), SUCCESS,
				"EmailTemplate updated successfully");
	}

	public HashMap<String, Object> sendEmailTemplate(SendEmailTemplate sendEmailTemplate) {
		HashMap<String, Object> res = new HashMap<>();
		EmailTemplate emailTemplate = emailTempRepo.findAllById(sendEmailTemplate.getTemplateId());
		LOGGER.info("email = " + emailTemplate);

		res.put("message", "message sent successfully");
		return res;
	}

}
