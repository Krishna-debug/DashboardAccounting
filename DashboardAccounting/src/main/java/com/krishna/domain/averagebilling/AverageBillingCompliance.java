package com.krishna.domain.averagebilling;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public @Data class AverageBillingCompliance {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	/*ID*/
	private long id;
	/*Month of compliance*/
	private int month;
	/*Year of compliance*/
	private int year;
	/*Project's ID*/
	private long projectId;
	/*Comment*/
	private String comments;
}
