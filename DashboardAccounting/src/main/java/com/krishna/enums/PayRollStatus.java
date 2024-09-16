package com.krishna.enums;

public enum PayRollStatus {

	ONHOLD("On Hold"),

	PENDING("Pending"),
	
	VERIFIED("Verified"),
	
	FILEPROCESSED("File Processed"),
	
	TIMESHEET_ISSUE("Timesheet Issue"),
	
	ATTENDANCE_ISSUE("Attendance Issue"),
	
	PROCESSED("Processed");

	private String payRollStatus;

	private PayRollStatus(String payRollStatus) {
		this.payRollStatus = payRollStatus;
	}

	public String getPayRollStatus() {
		return payRollStatus;
	}

}
