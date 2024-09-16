package com.krishna.domain;
//package com.oodles.domain;
//
//import java.util.Date;
//import java.util.List;
//
//import javax.persistence.Column;
//import javax.persistence.ElementCollection;
//import javax.persistence.Entity;
//import javax.persistence.EnumType;
//import javax.persistence.Enumerated;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.Table;
//
//import com.oodles.enums.MailNotificationStatus;
//import com.oodles.enums.MailType;
//
//@Entity
//@Table
//public class MailNotification {
//
//	@Id
//	@GeneratedValue(strategy = GenerationType.AUTO)
//	private Long id;
//
//	private String fromEmployee;
//
//	private String subject;
//
//	private Date sentDate;
//	
//	private long sentBy;
//
//	@Column
//	@ElementCollection(targetClass = String.class)
//	private List<String> toEmployees;
//
//	@Column
//	@ElementCollection(targetClass = String.class)
//	private List<String> ccEmployees;
//
//	@Enumerated(EnumType.STRING)
//	private MailType type;
//
//	@Enumerated(EnumType.STRING)
//	private MailNotificationStatus status;
//
//	public Long getId() {
//		return id;
//	}
//
//	public void setId(Long id) {
//		this.id = id;
//	}
//
//	public String getFromEmployee() {
//		return fromEmployee;
//	}
//
//	public void setFromEmployee(String fromEmployee) {
//		this.fromEmployee = fromEmployee;
//	}
//
//	public String getSubject() {
//		return subject;
//	}
//
//	public void setSubject(String subject) {
//		this.subject = subject;
//	}
//
//	public Date getSentDate() {
//		return sentDate;
//	}
//
//	public void setSentDate(Date sentDate) {
//		this.sentDate = sentDate;
//	}
//
//	public List<String> getToEmployees() {
//		return toEmployees;
//	}
//
//	public void setToEmployees(List<String> toEmployees) {
//		this.toEmployees = toEmployees;
//	}
//
//	public List<String> getCcEmployees() {
//		return ccEmployees;
//	}
//
//	public void setCcEmployees(List<String> ccEmployees) {
//		this.ccEmployees = ccEmployees;
//	}
//
//	public MailType getType() {
//		return type;
//	}
//
//	public void setType(MailType type) {
//		this.type = type;
//	}
//
//	public MailNotificationStatus getStatus() {
//		return status;
//	}
//
//	public void setStatus(MailNotificationStatus status) {
//		this.status = status;
//	}
//
//	public long getSentBy() {
//		return sentBy;
//	}
//
//	public void setSentBy(long sentBy) {
//		this.sentBy = sentBy;
//	}
//
//	@Override
//	public String toString() {
//		return "MailNotification [id=" + id + ", fromEmployee=" + fromEmployee + ", subject=" + subject + ", sentDate="
//				+ sentDate + ", sentBy=" + sentBy + ", toEmployees=" + toEmployees + ", ccEmployees=" + ccEmployees
//				+ ", type=" + type + ", status=" + status + "]";
//	}
//
//}
//
