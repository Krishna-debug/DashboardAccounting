package com.krishna.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.krishna.domain.EmailNotification;
import com.krishna.dto.GetNotificationDto;
import com.krishna.enums.MailStatus;
import com.krishna.enums.MailType;
import com.krishna.repository.MailnotificationRepository;
import com.krishna.security.JwtValidator;
import com.krishna.util.ConstantUtility;

@Service
public class EmailNotificationService {

	@Value("${spring.mail.username}")
	private String fromAddress;

	@Value("${spring.mail.overrides}")
	private String mailOverrides;

	@Autowired
	JwtValidator validator;

	@Autowired
	MailnotificationRepository mailnotificationRepository;

	@PersistenceContext
	private EntityManager entityManager;

	public boolean saveEmailRecord(String toAddress, String subject, String templateName) {
		boolean result = false;
		EmailNotification mailNotification = new EmailNotification();
		List<String> toEmployees = new ArrayList<>();
		toEmployees.add(toAddress);
		mailNotification.setFromMail(fromAddress);
		mailNotification.setMailStatus(MailStatus.SENT);
		mailNotification.setMailType(MailType.GENERIC);
		mailNotification.setSentDate(new Date());
		mailNotification.setSubject(subject);
		mailNotification.setTemplateName(templateName);
		mailNotification.setToEmployees(toEmployees);
		
		mailNotification = mailnotificationRepository.save(mailNotification);
		if (mailnotificationRepository.save(mailNotification) != null)
			result = true;
		return result;
	}

	public boolean saveEmailRecord(String toAddress, String subject, String[] ccArray, String templateName) {
		boolean result = false;
		EmailNotification mailNotification = new EmailNotification();
		List<String> toEmployees = new ArrayList<>();
		toEmployees.add(toAddress);
		List<String> ccEmployees = new ArrayList<>();
		for (String ccMail : ccArray) {
			ccEmployees.add(ccMail);
		}
		mailNotification.setFromMail(fromAddress);
		mailNotification.setMailStatus(MailStatus.SENT);
		mailNotification.setMailType(MailType.GENERIC);
		mailNotification.setSentDate(new Date(0));
		mailNotification.setSubject(subject);
		mailNotification.setTemplateName(templateName);
		mailNotification.setToEmployees(toEmployees);
		mailNotification.setCcEmployees(ccEmployees);
		
		mailNotification = mailnotificationRepository.save(mailNotification);
		if (mailnotificationRepository.save(mailNotification) != null)
			result = true;
		return result;
	}

	public boolean saveEmailRecord(String[] toAddress, String subject, String templateName) {
		boolean result = false;
		EmailNotification mailNotification = new EmailNotification();
		List<String> toEmployees = new ArrayList<>();
		for (String toMail : toAddress) {
			toEmployees.add(toMail);
		}
		mailNotification.setFromMail(fromAddress);
		mailNotification.setMailStatus(MailStatus.SENT);
		mailNotification.setMailType(MailType.GENERIC);
		mailNotification.setSentDate(new java.util.Date());
		mailNotification.setSubject(subject);
		mailNotification.setTemplateName(templateName);
		mailNotification.setToEmployees(toEmployees);
		
		mailNotification = mailnotificationRepository.save(mailNotification);
		if (mailnotificationRepository.save(mailNotification) != null)
			result = true;
		return result;
	}

	public boolean saveEmailRecord(String[] toAddress, String subject, String[] ccArray, String templateName) {
		boolean result = false;
		EmailNotification mailNotification = new EmailNotification();
		List<String> toEmployees = new ArrayList<>();
		for (String toMail : toAddress) {
			toEmployees.add(toMail);
		}
		List<String> ccEmployees = new ArrayList<>();
		for (String ccMail : ccArray) {
			ccEmployees.add(ccMail);
		}
		mailNotification.setFromMail(fromAddress);
		mailNotification.setMailStatus(MailStatus.SENT);
		mailNotification.setMailType(MailType.GENERIC);
		mailNotification.setSentDate(new Date());
		mailNotification.setSubject(subject);
		mailNotification.setTemplateName(templateName);
		mailNotification.setToEmployees(toEmployees);
		mailNotification.setCcEmployees(ccEmployees);
		mailNotification = mailnotificationRepository.save(mailNotification);
		if (mailnotificationRepository.save(mailNotification) != null)
			result = true;
		return result;
	}

	/** For fail mail Notifications */

	public boolean failEmailRecord(String toAddress, String subject, String templateName) {
		boolean result = false;
		EmailNotification mailNotification = new EmailNotification();
		List<String> toEmployees = new ArrayList<>();
		toEmployees.add(toAddress);
		mailNotification.setFromMail(fromAddress);
		mailNotification.setMailStatus(MailStatus.SENT);
		mailNotification.setMailType(MailType.GENERIC);
		mailNotification.setSentDate(new Date());
		mailNotification.setSubject(subject);
		mailNotification.setTemplateName(templateName);
		mailNotification.setToEmployees(toEmployees);
		
		mailNotification = mailnotificationRepository.save(mailNotification);
		if (mailnotificationRepository.save(mailNotification) != null)
			result = true;
		return result;
	}

	public boolean failEmailRecord(String toAddress, String subject, String[] ccArray, String templateName) {
		boolean result = false;
		EmailNotification mailNotification = new EmailNotification();
		List<String> toEmployees = new ArrayList<>();
		toEmployees.add(toAddress);
		List<String> ccEmployees = new ArrayList<>();
		for (String ccMail : ccArray) {
			ccEmployees.add(ccMail);
		}
		mailNotification.setFromMail(fromAddress);
		mailNotification.setMailStatus(MailStatus.SENT);
		mailNotification.setMailType(MailType.GENERIC);
		mailNotification.setSentDate(new Date());
		mailNotification.setSubject(subject);
		mailNotification.setTemplateName(templateName);
		mailNotification.setToEmployees(toEmployees);
		mailNotification.setCcEmployees(ccEmployees);
		if (mailnotificationRepository.save(mailNotification) != null)
			result = true;
		return result;
	}

	public boolean failEmailRecord(String[] toAddress, String subject, String templateName) {
		boolean result = false;
		EmailNotification mailNotification = new EmailNotification();
		List<String> toEmployees = new ArrayList<>();
		for (String toMail : toAddress) {
			toEmployees.add(toMail);
		}
		mailNotification.setFromMail(fromAddress);
		mailNotification.setMailStatus(MailStatus.SENT);
		mailNotification.setMailType(MailType.GENERIC);
		mailNotification.setSentDate(new Date());
		mailNotification.setSubject(subject);
		mailNotification.setTemplateName(templateName);
		mailNotification.setToEmployees(toEmployees);
		
		mailNotification = mailnotificationRepository.save(mailNotification);
		if (mailnotificationRepository.save(mailNotification) != null)
			result = true;
		return result;
	}

	public boolean failEmailRecord(String[] toAddress, String subject, String[] ccArray, String templateName) {
		boolean result = false;
		EmailNotification mailNotification = new EmailNotification();
		List<String> toEmployees = new ArrayList<>();
		for (String toMail : toAddress) {
			toEmployees.add(toMail);
		}
		List<String> ccEmployees = new ArrayList<>();
		for (String ccMail : ccArray) {
			ccEmployees.add(ccMail);
		}
		mailNotification.setFromMail(fromAddress);
		mailNotification.setMailStatus(MailStatus.SENT);
		mailNotification.setMailType(MailType.GENERIC);
		mailNotification.setSentDate(new Date());
		mailNotification.setSubject(subject);
		mailNotification.setTemplateName(templateName);
		mailNotification.setToEmployees(toEmployees);
		mailNotification.setCcEmployees(ccEmployees);
		
		mailNotification = mailnotificationRepository.save(mailNotification);
		if (mailnotificationRepository.save(mailNotification) != null)
			result = true;
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Object> getMailNotification(String accessToken,GetNotificationDto getNotificationDto) {
		Map<String,Object> res=new HashMap<>();
		ArrayList<Map<String, Object>> MailNotification = new ArrayList<>();
		List<EmailNotification> mailNotification = new ArrayList<>();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<EmailNotification> query = criteriaBuilder.createQuery(EmailNotification.class);
		Root<EmailNotification> root = query.from(EmailNotification.class);
		if (getNotificationDto.getStartDate()!=null && getNotificationDto.getEndDate()!=null) {
			query.select(root);
			query.where(criteriaBuilder.between(root.get(ConstantUtility.SENT_DATE), new Date(getNotificationDto.getStartDate()),
					new Date(getNotificationDto.getEndDate())));
			query.orderBy(criteriaBuilder.desc(root.get(ConstantUtility.SENT_DATE)));
		}

		Query q = entityManager.createQuery(query);
		Integer offset = 0;
		if (Integer.parseInt(getNotificationDto.getOffset()) != 0)
			offset = offset / 10;
		q.setFirstResult(offset * Integer.parseInt(getNotificationDto.getMaxSize()));
		q.setMaxResults(Integer.parseInt(getNotificationDto.getMaxSize()));

		Query total = entityManager.createQuery(query);
		mailNotification = q.getResultList();

		Iterator<EmailNotification> itr = mailNotification.iterator();
		while (itr.hasNext()) {
			Map<String, Object> mailNotificationMap = new HashMap<>();
			EmailNotification mails = itr.next();
			mailNotificationMap.put("id", mails.getId());
			mailNotificationMap.put(ConstantUtility.SENT_DATE, mails.getSentDate());
			mailNotificationMap.put("fromEmployee", mails.getFromMail());
			mailNotificationMap.put("toEmployees", mails.getToEmployees());
			mailNotificationMap.put("subject", mails.getSubject());
			mailNotificationMap.put("ccEmployees", mails.getCcEmployees());
			mailNotificationMap.put("status", mails.getMailStatus());
			mailNotificationMap.put("type", mails.getMailType());
			mailNotificationMap.put("totalCount", total.getResultList().size());
			MailNotification.add(mailNotificationMap);
		}
		res.put("totalCount", MailNotification.size());

		res.put("mailNofication", MailNotification);

		return res;
		}

}
