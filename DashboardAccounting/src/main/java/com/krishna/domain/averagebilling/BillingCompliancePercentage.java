package com.krishna.domain.averagebilling;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

@Entity
@Table
@Audited
public class BillingCompliancePercentage {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*UD*/
	private long id;
	/*Percentage of Billing Compliance*/
	private Double compliancePerc;
	/*If biling compliance is archieved*/
	private Boolean isArchive;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Double getCompliancePerc() {
		return compliancePerc;
	}

	public void setCompliancePerc(Double compliancePerc) {
		this.compliancePerc = compliancePerc;
	}

	public Boolean getIsArchive() {
		return isArchive;
	}

	public void setIsArchive(Boolean isArchive) {
		this.isArchive = isArchive;
	}
	
}
