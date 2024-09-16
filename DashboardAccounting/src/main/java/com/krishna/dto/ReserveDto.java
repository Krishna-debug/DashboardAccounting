package com.krishna.dto;

public class ReserveDto {

	private String buName;
	private Double deductionAmount;
//	private Integer month;
//	private Integer year;
	private String remarks;
	private Long deductedOn;
	private Long expenseType;

	public String getBuName() {
		return buName;
	}

	public void setBuName(String buName) {
		this.buName = buName;
	}

	public Double getDeductionAmount() {
		return deductionAmount;
	}

	public void setDeductionAmount(Double deductionAmount) {
		this.deductionAmount = deductionAmount;
	}

//	public Integer getMonth() {
//		return month;
//	}
//
//	public void setMonth(Integer month) {
//		this.month = month;
//	}
//
//	public Integer getYear() {
//		return year;
//	}
//
//	public void setYear(Integer year) {
//		this.year = year;
//	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Long getDeductedOn() {
		return deductedOn;
	}

	public void setDeductedOn(Long deductedOn) {
		this.deductedOn = deductedOn;
	}

	public Long getExpenseType() {
		return expenseType;
	}

	public void setExpenseType(Long expenseType) {
		this.expenseType = expenseType;
	}

}
