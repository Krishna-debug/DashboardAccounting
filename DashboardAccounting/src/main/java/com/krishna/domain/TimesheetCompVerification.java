package com.krishna.domain;

	
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import lombok.Data;
import lombok.ToString;

@Entity
@Table
@Data
@ToString
public class TimesheetCompVerification {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private long userId;
	private Boolean isTimesheetComplianceVerified;
	private boolean isDeleted;
	private int month;
	private int year;
	private String buHeadComment;
	private String resourcingComment;
	private Date creationDate;
}
