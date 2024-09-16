package com.krishna.enums;

public enum MailStatus {

	SENT("Sent"),

	FAILED("Failed");

	private String mailStatus;

	private MailStatus(String mailStatus) {
		this.mailStatus = mailStatus;
	}

	public String getMailStatus() {
		return mailStatus;
	}

	public void setMailStatus(String mailStatus) {
		this.mailStatus = mailStatus;
	}

}
