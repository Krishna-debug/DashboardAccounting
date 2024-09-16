package com.krishna.service;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.krishna.domain.EmailTemplate;
import com.krishna.repository.EmailTemplateRepository;
import com.krishna.util.ConstantUtility;

@Service
public class MailService {

	/**
	 * @author shivangi
	 */
	@Autowired
	private JavaMailSender sender;

	@Autowired
	private TemplateEngine templateEngine;

	@Value("${spring.mail.username}")
	private String fromAddress;

	@Value("${spring.mail.overrides}")
	private String mailOverrides;

	@Autowired
	private Environment environment;

	@Autowired
	private EmailTemplateRepository emailTempRepo;
	
	@Autowired
	private EmailNotificationService emailnotificationService;
	
	@Value("${com.oodles.sesFromAddress}")
	private String mailFromSes;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Async
	public void sendScheduleHtmlMail(String toAddress, String subject, Context context, String templatename) {
		try {
			String env[] = environment.getActiveProfiles();
			for (String currentEnvironment : env) {
				if (currentEnvironment.equals(ConstantUtility.DEVELOPMENT)) {
					toAddress = mailOverrides;
				}
				if (currentEnvironment.equals(ConstantUtility.LOCAL_DEVELOPMENT)) {
					toAddress = mailOverrides;
				}
				if (currentEnvironment.equals(ConstantUtility.STAGING)) {
					toAddress = mailOverrides;
				}
			}
			String html = templateEngine.process(templatename, context);
			MimeMessage mail = sender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mail, true);
//			mail.setFrom(new InternetAddress(fromAddress));
			mail.setFrom(mailFromSes);
			helper.setTo(toAddress);
			helper.setSubject(subject);
			helper.setText(html, true);
			helper.setBcc(mailFromSes);
			sender.send(mail);
			emailnotificationService.saveEmailRecord(toAddress, subject , templatename);
		} catch (Exception e) {
			emailnotificationService.failEmailRecord(toAddress, subject , templatename);
			LOGGER.error(String.format(ConstantUtility.PROBLEM_WITH_SENDING_EMAIL_TO_ERROR_MESSAGE, toAddress,
					e.getMessage()));
			LOGGER.error(ConstantUtility.EMAIL_SEND_ERROR, e);

		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Async
	public void sendScheduleHtmlMailWithCc(String toAddress, String subject, Context context, String templatename,
			String[] ccArray) {
		try {

			String env[] = environment.getActiveProfiles();
			for (String currentEnvironment : env) {

				if (currentEnvironment.equals(ConstantUtility.DEVELOPMENT)) {
					toAddress = mailOverrides;
					Arrays.fill(ccArray, mailOverrides);
				}
				if (currentEnvironment.equals(ConstantUtility.LOCAL_DEVELOPMENT)) {
					toAddress = mailOverrides;
					Arrays.fill(ccArray, mailOverrides);
				}
				if (currentEnvironment.equals(ConstantUtility.STAGING)) {
					toAddress = mailOverrides;
					Arrays.fill(ccArray, mailOverrides);
				}
			}

			String html = templateEngine.process(templatename, context);
			MimeMessage mail = createMail(toAddress, subject, ccArray, html);
			
			emailnotificationService.saveEmailRecord(toAddress, subject  , ccArray, templatename);
		} catch (Exception e) {
			emailnotificationService.failEmailRecord(toAddress, subject , ccArray, templatename);
			LOGGER.error(String.format(ConstantUtility.PROBLEM_WITH_SENDING_EMAIL_TO_ERROR_MESSAGE, toAddress,
					e.getMessage()));
			LOGGER.error(ConstantUtility.EMAIL_SEND_ERROR, e);

		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Async
	public void sendScheduleHtmlMailWithCcAndAttachmentFile(String toAddress, String subject, Context context, String templatename,
			String[] ccArray, FileSystemResource file) {
		try {

			String env[] = environment.getActiveProfiles();
			for (String currentEnvironment : env) {

				if (currentEnvironment.equals(ConstantUtility.DEVELOPMENT)) {
					toAddress = mailOverrides;
					Arrays.fill(ccArray, mailOverrides);
				}
				if (currentEnvironment.equals(ConstantUtility.LOCAL_DEVELOPMENT)) {
					toAddress = mailOverrides;
					Arrays.fill(ccArray, mailOverrides);
				}
				if (currentEnvironment.equals(ConstantUtility.STAGING)) {
					toAddress = mailOverrides;
					Arrays.fill(ccArray, mailOverrides);
				}
			}

			String html = templateEngine.process(templatename, context);
			MimeMessage mail = createMailWithAttachment(toAddress, subject, ccArray, html, context, file);
			File tempFile = new File(Paths.get(file.getFilename()).toString());
			tempFile.delete();
			emailnotificationService.saveEmailRecord(toAddress, subject  , ccArray, templatename);
		} catch (Exception e) {
			emailnotificationService.failEmailRecord(toAddress, subject , ccArray, templatename);
			LOGGER.error(String.format(ConstantUtility.PROBLEM_WITH_SENDING_EMAIL_TO_ERROR_MESSAGE, toAddress,
					e.getMessage()));
			LOGGER.error(ConstantUtility.EMAIL_SEND_ERROR, e);

		}
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Async
	public void sendScheduleHtmlMails(String[] toAddress, String subject, Context context, String templatename) {
		try {

			String env[] = environment.getActiveProfiles();

			for (String currentEnvironment : env) {
				if (currentEnvironment.equals(ConstantUtility.DEVELOPMENT)) {
					Arrays.fill(toAddress, mailOverrides);
				}
				if (currentEnvironment.equals(ConstantUtility.LOCAL_DEVELOPMENT)) {
					Arrays.fill(toAddress, mailOverrides);
				}
				if (currentEnvironment.equals(ConstantUtility.STAGING)) {
					Arrays.fill(toAddress, mailOverrides);
				}
			}
			String html = templateEngine.process(templatename, context);
			MimeMessage mail = sender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mail, true);
//			mail.setFrom(new InternetAddress(fromAddress));
			mail.setFrom(mailFromSes);
			helper.setTo(toAddress);
			helper.setSubject(subject);
			helper.setBcc(mailFromSes);
			helper.setText(html, true);
			sender.send(mail);
			emailnotificationService.saveEmailRecord(toAddress, subject ,templatename);
		} catch (Exception e) {
			emailnotificationService.failEmailRecord(toAddress, subject ,templatename);
			LOGGER.error(String.format(ConstantUtility.PROBLEM_WITH_SENDING_EMAIL_TO_ERROR_MESSAGE, toAddress,
					e.getMessage()));
			LOGGER.error(ConstantUtility.EMAIL_SEND_ERROR, e);

		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Async
	public void sendScheduleCommonHtmlMailWithCc(String[] toAddress, String subject, Context context,
			String templatename, String[] ccArray) {
		try {

			String env[] = environment.getActiveProfiles();
			for (String currentEnvironment : env) {
				if (currentEnvironment.equals(ConstantUtility.DEVELOPMENT)) {
					Arrays.fill(toAddress, mailOverrides);
					Arrays.fill(ccArray, mailOverrides);
				}
				if (currentEnvironment.equals(ConstantUtility.LOCAL_DEVELOPMENT)) {
					Arrays.fill(toAddress, mailOverrides);
					Arrays.fill(ccArray, mailOverrides);
				}
				if (currentEnvironment.equals(ConstantUtility.STAGING)) {
					Arrays.fill(toAddress, mailOverrides);
					Arrays.fill(ccArray, mailOverrides);
				}
			}

			String html = templateEngine.process(templatename, context);
			MimeMessage mail = sender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mail, true);
//			mail.setFrom(new InternetAddress(fromAddress));
			mail.setFrom(mailFromSes);
			helper.setTo(toAddress);
			helper.setSubject(subject);
			helper.setCc(ccArray);
			helper.setBcc(mailFromSes);
			helper.setText(html, true);
			sender.send(mail);
			emailnotificationService.saveEmailRecord(toAddress, subject , ccArray, templatename);
		} catch (Exception e) {
			emailnotificationService.failEmailRecord(toAddress, subject , ccArray, templatename);
			LOGGER.error(String.format(ConstantUtility.PROBLEM_WITH_SENDING_EMAIL_TO_ERROR_MESSAGE, toAddress,
					e.getMessage()));
			LOGGER.error(ConstantUtility.EMAIL_SEND_ERROR, e);

		}
	}

	/** Used when email templates are stored in Database*/
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Async
	public void sendScheduleHtmlMail(String toAddress, String subject, Map<String, Object> context,
			String templatename) {
		try {
			String env[] = environment.getActiveProfiles();
			for (String currentEnvironment : env) {
				if (currentEnvironment.equals(ConstantUtility.DEVELOPMENT)) {
					toAddress = mailOverrides;
				}
				if (currentEnvironment.equals(ConstantUtility.LOCAL_DEVELOPMENT)) {
					toAddress = mailOverrides;
				}
				if (currentEnvironment.equals(ConstantUtility.STAGING)) {
					toAddress = mailOverrides;
				}
			}
			EmailTemplate emailTemplate = emailTempRepo.findAllByTemplateNameAndIsDeleted(templatename, false);
			String html = emailTemplate.getTemplateData();
			StrSubstitutor sub = new StrSubstitutor(context);
			String html2 = sub.replace(html);
			MimeMessage mail = sender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mail, true);
//			mail.setFrom(new InternetAddress(fromAddress));
			mail.setFrom(mailFromSes);
			helper.setTo(toAddress);
			helper.setSubject(subject);
			helper.setText(html2, true);
			helper.setBcc(mailFromSes);
			sender.send(mail);
			emailnotificationService.saveEmailRecord(toAddress, subject , templatename);
		} catch (Exception e) {
			emailnotificationService.failEmailRecord(toAddress, subject , templatename);
			LOGGER.error(String.format(ConstantUtility.PROBLEM_WITH_SENDING_EMAIL_TO_ERROR_MESSAGE, toAddress,
					e.getMessage()));
			LOGGER.error(ConstantUtility.EMAIL_SEND_ERROR, e);

		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Async
	public void sendScheduleCommonHtmlMailWithCc(String[] toAddress, String subject, Map<String, Object> context,
			String templatename, String[] ccArray) {
		try {

			String env[] = environment.getActiveProfiles();
			for (String currentEnvironment : env) {
				if (currentEnvironment.equals(ConstantUtility.DEVELOPMENT)) {
					Arrays.fill(toAddress, mailOverrides);
					Arrays.fill(ccArray, mailOverrides);
				}
				if (currentEnvironment.equals(ConstantUtility.LOCAL_DEVELOPMENT)) {
					Arrays.fill(toAddress, mailOverrides);
					Arrays.fill(ccArray, mailOverrides);
				}
				if (currentEnvironment.equals(ConstantUtility.STAGING)) {
					Arrays.fill(toAddress, mailOverrides);
					Arrays.fill(ccArray, mailOverrides);
				}
			}

			EmailTemplate emailTemplate = emailTempRepo.findAllByTemplateNameAndIsDeleted(templatename, false);
			String html = emailTemplate.getTemplateData();
			StrSubstitutor sub = new StrSubstitutor(context);
			String html2 = sub.replace(html);
			MimeMessage mail = sender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mail, true);
//			mail.setFrom(new InternetAddress(fromAddress));
			mail.setFrom(mailFromSes);
			helper.setTo(toAddress);
			helper.setSubject(subject);
			helper.setCc(ccArray);
			helper.setText(html2, true);
			helper.setBcc(mailFromSes);
			sender.send(mail);
			emailnotificationService.saveEmailRecord(toAddress, subject , ccArray, templatename);
		} catch (Exception e) {
			emailnotificationService.failEmailRecord(toAddress, subject , ccArray, templatename);
			LOGGER.error(String.format(ConstantUtility.PROBLEM_WITH_SENDING_EMAIL_TO_ERROR_MESSAGE, toAddress,
					e.getMessage()));
			LOGGER.error(ConstantUtility.EMAIL_SEND_ERROR, e);

		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@Async
	public void sendScheduleHtmlMails(String[] toAddress, String subject, Map<String, Object> context,
			String templatename) {
		try {

			String env[] = environment.getActiveProfiles();

			for (String currentEnvironment : env) {
				if (currentEnvironment.equals(ConstantUtility.DEVELOPMENT)) {
					for (String toAdd : toAddress) {
						LOGGER.info(ConstantUtility.TO_ADDRESS, toAdd);
					}
					Arrays.fill(toAddress, mailOverrides);
				}
				if (currentEnvironment.equals(ConstantUtility.LOCAL_DEVELOPMENT)) {
					for (String toAdd : toAddress) {
						LOGGER.info(ConstantUtility.TO_ADDRESS, toAdd);
					}
					Arrays.fill(toAddress, mailOverrides);
				}
				if (currentEnvironment.equals(ConstantUtility.STAGING)) {
					for (String toAdd : toAddress) {
						LOGGER.info(ConstantUtility.TO_ADDRESS, toAdd);
					}
					Arrays.fill(toAddress, mailOverrides);
				}
			}
			EmailTemplate emailTemplate = emailTempRepo.findAllByTemplateNameAndIsDeleted(templatename, false);
			String html = emailTemplate.getTemplateData();
			StrSubstitutor sub = new StrSubstitutor(context);
			String html2 = sub.replace(html);
			MimeMessage mail = sender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mail, true);
//			mail.setFrom(new InternetAddress(fromAddress));
			mail.setFrom(mailFromSes);
			helper.setTo(toAddress);
			helper.setSubject(subject);
			helper.setBcc(mailFromSes);
			helper.setText(html2, true);

			sender.send(mail);
			emailnotificationService.saveEmailRecord(toAddress, subject , templatename);
		} catch (Exception e) {
			emailnotificationService.failEmailRecord(toAddress, subject , templatename);
			LOGGER.error(String.format(ConstantUtility.PROBLEM_WITH_SENDING_EMAIL_TO_ERROR_MESSAGE, toAddress,
					e.getMessage()));
			LOGGER.error(ConstantUtility.EMAIL_SEND_ERROR, e);

		}
	}

	public MimeMessage createMail(String toAddress, String subject, String[] ccArray, String html)
			throws MessagingException {
		MimeMessage mail = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mail, true);
//		mail.setFrom(new InternetAddress(fromAddress));
		mail.setFrom(mailFromSes);
		helper.setTo(toAddress);
		//System.out.println("toAddress= " + toAddress.toString());
		helper.setSubject(subject);
		if(ccArray != null && ccArray.length > 0)
			helper.setCc(ccArray);
			//System.out.println(" ccArray = " + Arrays.deepToString(ccArray));
		helper.setText(html, true);
		helper.setBcc(mailFromSes);
		sender.send(mail);
		return mail;
	}

	public MimeMessage createMailWithAttachment(String toAddress, String subject, String[] ccArray, String html,Context context, FileSystemResource file)
			throws MessagingException {
		MimeMessage mail = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mail, true);
		helper.addAttachment("OODLES_INV-"+context.getVariable("invoiceId")+".pdf", file);
//		mail.setFrom(new InternetAddress(fromAddress));
		mail.setFrom(mailFromSes);
		helper.setTo(toAddress);
		helper.setSubject(subject);
		if(ccArray != null && ccArray.length > 0)
			helper.setCc(ccArray);
		helper.setText(html, true);
		helper.setBcc(mailFromSes);
		sender.send(mail);
		return mail;
	}
}
