package com.krishna.dto;

import java.util.Date;

import com.krishna.domain.BuExpenses;

public class DeductionResponseDto {
	
	private Long id;

	private Double deductedAmount;

	private String remarks;

	private Date deductionDate;

	private Date deductedOn;

	private String deductedByName;

	private BuExpenses buExpenses;

	
	public BuExpenses getBuExpenses() {
		return buExpenses;
	}

	public void setBuExpenses(BuExpenses buExpenses) {
		this.buExpenses = buExpenses;
	}

	public Double getDeductedAmount() {
		return deductedAmount;
	}

	public void setDeductedAmount(Double deductedAmount) {
		this.deductedAmount = deductedAmount;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Date getDeductionDate() {
		return deductionDate;
	}

	public void setDeductionDate(Date deductionDate) {
		this.deductionDate = deductionDate;
	}

	public Date getDeductedOn() {
		return deductedOn;
	}

	public void setDeductedOn(Date deductedOn) {
		this.deductedOn = deductedOn;
	}

	public String getDeductedByName() {
		return deductedByName;
	}

	public void setDeductedByName(String deductedByName) {
		this.deductedByName = deductedByName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	

}
