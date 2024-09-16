package com.krishna.enums;

public enum PayslipStatus {

	SAVED("Saved"),

	GENERATED("Generated"),
	
	SENT("Sent");

	private String payslipStatus;

	private PayslipStatus(String payslipStatus) {
		this.payslipStatus = payslipStatus;
	}

	public String getPayslipStatus() {
		return payslipStatus;
	}

}
