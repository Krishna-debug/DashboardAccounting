package com.krishna.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.krishna.enums.MailStatus;
import com.krishna.enums.MailType;

@Entity
@Table(name = "email_Notification")
public class EmailNotification {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	/*ID*/
	private Long id;
	/*Sender's Mail*/
	private String fromMail;
	/*Subject of Mail*/
	private String subject;
	/*Date of sneding E-mail*/
	private Date sentDate;
	/*Sender's user Id*/
	private long sentBy;

	@Column
	@ElementCollection(targetClass = String.class)
	/*List of all reciever's Email Id*/
	private List<String> toEmployees;

	@Column
	@ElementCollection(targetClass = String.class)
	/*List of all cc reciever's Email Id*/
	private List<String> ccEmployees;

	@Enumerated(EnumType.STRING)
	/*Type of Mail -> generic*/
	private MailType mailType;

	@Enumerated(EnumType.STRING)
	/*Mail Status-> send or failed*/
	private MailStatus mailStatus;
	/*Tempelate used for Emil Name*/
	private String templateName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSubject() {
		return subject;
	}

	public Date getSentDate() {
		return sentDate;
	}

	public long getSentBy() {
		return sentBy;
	}

	public List<String> getToEmployees() {
		return toEmployees;
	}

	public List<String> getCcEmployees() {
		return ccEmployees;
	}

	public MailType getMailType() {
		return mailType;
	}

	public MailStatus getMailStatus() {
		return mailStatus;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}

	public void setSentBy(long sentBy) {
		this.sentBy = sentBy;
	}

	public void setToEmployees(List<String> toEmployees) {
		this.toEmployees = toEmployees;
	}

	public void setCcEmployees(List<String> ccEmployees) {
		this.ccEmployees = ccEmployees;
	}

	public void setMailType(MailType mailType) {
		this.mailType = mailType;
	}

	public void setMailStatus(MailStatus mailStatus) {
		this.mailStatus = mailStatus;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getFromMail() {
		return fromMail;
	}

	public void setFromMail(String fromMail) {
		this.fromMail = fromMail;
	}

	@Override
	public String toString() {
		return "EmailNotification [id=" + id + ", subject=" + subject + ", sentDate=" + sentDate
				+ ", sentBy=" + sentBy + ", toEmployees=" + toEmployees + ", ccEmployees=" + ccEmployees + ", mailType="
				+ mailType + ", mailStatus=" + mailStatus + ", templateName=" + templateName + "]";
	}

}
