package com.krishna.enums;

public enum MailType {

	GENERIC("Generic");

	private MailType(String mailType) {
		this.mailType = mailType;
	}

	private String mailType;

	public String getMailType() {
		return mailType;
	}

	public void setMailType(String mailType) {
		this.mailType = mailType;
	}

}
