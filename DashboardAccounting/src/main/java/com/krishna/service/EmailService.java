package com.krishna.service;

import java.util.Date;
import java.util.Properties;
import java.util.Set;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.krishna.controller.EmailTemplatesController;
import com.krishna.domain.Templatebcc;
import com.krishna.domain.Templatecc;

@Service
public class EmailService {

	public static final Logger logger = LoggerFactory.getLogger(EmailTemplatesController.class);

	public boolean sendEmail(String to, String subject, String messageText, String from, Set<Templatebcc> bcc,
			Set<Templatecc> cc, String password) {
		try {
			String host = "smtp.gmail.com";
			String port = "465";

			// Getting the Session Object

			Properties property = setproperties(host, port);

			// Getting Session

			Authenticator auth = new Authenticator() {
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(from, password);
				}
			};
			Session session = Session.getInstance(property, auth);
			session.setDebug(true);

			// Composing the message

			MimeMessage message = composeMessage(session, to, from, bcc, cc, subject);

			MimeMultipart multipart = new MimeMultipart("related");
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(messageText, "text/html ; charset=UTF-8");

			multipart.addBodyPart(messageBodyPart);
			message.setContent(multipart, "text/html");
			try {
				Transport.send(message);
			} catch (Exception e) {
				logger.info(" " + e);
			}
			logger.info("message sent successfully");
		} catch (MessagingException e) {
			logger.info(" " + e);
			return false;
		}
		return true;
	}

	/*
	 * Getting the Session Object
	 */
	public Properties setproperties(String host, String port) {

		Properties property = new Properties();

		property.put("mail.smtp.host", host);
		property.put("mail.smtp.port", port);
		property.put("mail.smtp.starttls.enable", "true");
		property.put("mail.smtp.auth", "true");
		property.put("mail.smtp.socketFactory.port", port);
		property.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		property.put("mail.smtp.socketFactory.fallback", "false");
		return property;
	}

	/*
	 * Compose message for Mail
	 */
	public MimeMessage composeMessage(Session session, String to, String from, Set<Templatebcc> bcc, Set<Templatecc> cc,
			String subject) throws AddressException, MessagingException {

		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		for (Templatecc templatecc : cc) {
			message.addRecipient(Message.RecipientType.CC, new InternetAddress(templatecc.getCcMail()));

		}
		for (Templatebcc templatebcc : bcc) {
			message.addRecipient(Message.RecipientType.BCC, new InternetAddress(templatebcc.getBccMail()));
		}
		message.setSubject(subject);
		message.setSentDate(new Date());

		return message;
	}

}
