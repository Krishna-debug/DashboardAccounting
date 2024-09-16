package com.krishna.dto;

import com.krishna.enums.Months;

public class IndirectCostDto {
	private String year;

	private Months month;

	private Double variableCost;

	private Double infraCost;

	private Double reimbursement;

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