package com.krishna.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

import com.krishna.enums.Months;

@Audited
@Entity
public class IndirectCost {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	/*ID*/
	private Long id;

	@NotEmpty
	/*Year in String*/
	private String year;

	@NotNull
	@Enumerated(EnumType.STRING)
	/*Month in String like -> JANUARY,FEBUARY etc*/
	private Months month;

	@Column(name = "variable_cost")
	/*Variable Cost */
	private Double variableCost;

	@Column(name = "infra_cost")
	/*Cost from Infra team*/
	private Double infraCost;
	/*Reimbursement Amount*/
	private Double reimbursement=0D;

	@Column(name = "is_deleted")
	/*If is deleted*/
	private boolean isDeleted;
	/*Created By User's Id*/
	Long createdBy;
	/*Date of creation*/
	Date createOn;
	/*Modified By user's Id*/
	Long lastModifiedBy;
	/*Date of Last Modification Date*/
	Date LastModifiedOn;

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreateOn() {
		return createOn;
	}

	public void setCreateOn(Date createOn) {
		this.createOn = createOn;
	}

	public Long getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(Long lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Date getLastModifiedOn() {
		return LastModifiedOn;
	}

	public void setLastModifiedOn(Date lastModifiedOn) {
		LastModifiedOn = lastModifiedOn;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public Months getMonth() {
		return month;
	}

	public void setMonth(Months month) {
		this.month = month;
	}

	public Double getVariableCost() {
		return variableCost;
	}

	public void setVariableCost(Double variableCost) {
		this.variableCost = variableCost;
	}

	public Double getInfraCost() {
		return infraCost;
	}

	public void setInfraCost(Double infraCost) {
		this.infraCost = infraCost;
	}

	public Double getReimbursement() {
		return reimbursement;
	}

	public void setReimbursement(Double reimbursement) {
		this.reimbursement = reimbursement;
	}

}