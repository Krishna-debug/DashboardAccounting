package com.krishna.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import com.krishna.Auditing.ConferenceRevisionListener;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@RevisionEntity(ConferenceRevisionListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RevInfo {

	@Id
	@GeneratedValue
	@RevisionNumber
	/*ID*/
	private int id;

	@RevisionTimestamp
	/*TImeStamp in milli seconds*/	
	private long timestamp;
	/*ID of the auditor*/
	private Long auditorId;
	/*Name of the auditor*/
	private String auditorName;
	
	private String auditorEmail;
}
