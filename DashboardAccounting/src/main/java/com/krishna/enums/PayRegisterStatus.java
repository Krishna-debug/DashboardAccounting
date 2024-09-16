package com.krishna.enums;

public enum PayRegisterStatus {

	COMPLETE("Complete"), INCOMPLETE("Incomplete");

	private String payRegisterStatus;

	public String getPfSubscription() {
		return payRegisterStatus;
	}

	private PayRegisterStatus(String payRegisterStatus) {
		this.payRegisterStatus = payRegisterStatus;
	}

}
