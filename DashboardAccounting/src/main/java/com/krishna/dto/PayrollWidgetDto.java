package com.krishna.dto;

public class PayrollWidgetDto {

	long processedCount = 0;

	long pendingCount = 0;

	long verfiedCount = 0;

	long fileprocessedCount = 0;

	long onholdCount = 0;

	long timesheetIssueCount = 0;

	double totalVerifiedNetPay = 0d;

	double totalOnholdNetPay = 0d;

	double totalFileProcessedNetPay = 0d;

	double totalProcessedNetPay = 0d;

	double totalPendingNetPay = 0d;

	double timeSheetIssueNetPay = 0d;
	
	double attendanceIssueNetPay = 0d;
	
	long attendanceIssueCount = 0;

	public long getProcessedCount() {
		return processedCount;
	}

	public void setProcessedCount(long processedCount) {
		this.processedCount = processedCount;
	}

	public long getPendingCount() {
		return pendingCount;
	}

	public void setPendingCount(long pendingCount) {
		this.pendingCount = pendingCount;
	}

	public long getVerfiedCount() {
		return verfiedCount;
	}

	public void setVerfiedCount(long verfiedCount) {
		this.verfiedCount = verfiedCount;
	}

	public long getFileprocessedCount() {
		return fileprocessedCount;
	}

	public void setFileprocessedCount(long fileprocessedCount) {
		this.fileprocessedCount = fileprocessedCount;
	}

	public long getOnholdCount() {
		return onholdCount;
	}

	public void setOnholdCount(long onholdCount) {
		this.onholdCount = onholdCount;
	}

	public double getTotalVerifiedNetPay() {
		return totalVerifiedNetPay;
	}

	public void setTotalVerifiedNetPay(double totalVerifiedNetPay) {
		this.totalVerifiedNetPay = totalVerifiedNetPay;
	}

	public double getTotalOnholdNetPay() {
		return totalOnholdNetPay;
	}

	public void setTotalOnholdNetPay(double totalOnholdNetPay) {
		this.totalOnholdNetPay = totalOnholdNetPay;
	}

	public double getTotalFileProcessedNetPay() {
		return totalFileProcessedNetPay;
	}

	public void setTotalFileProcessedNetPay(double totalFileProcessedNetPay) {
		this.totalFileProcessedNetPay = totalFileProcessedNetPay;
	}

	public double getTotalProcessedNetPay() {
		return totalProcessedNetPay;
	}

	public long getTimesheetIssueCount() {
		return timesheetIssueCount;
	}

	public void setTimesheetIssueCount(long timesheetIssueCount) {
		this.timesheetIssueCount = timesheetIssueCount;
	}

	public double getTimeSheetIssueNetPay() {
		return timeSheetIssueNetPay;
	}

	public void setTimeSheetIssueNetPay(double timeSheetIssueNetPay) {
		this.timeSheetIssueNetPay = timeSheetIssueNetPay;
	}

	public void setTotalProcessedNetPay(double totalProcessedNetPay) {
		this.totalProcessedNetPay = totalProcessedNetPay;
	}

	public double getTotalPendingNetPay() {
		return totalPendingNetPay;
	}

	public void setTotalPendingNetPay(double totalPendingNetPay) {
		this.totalPendingNetPay = totalPendingNetPay;
	}

	public double getAttendanceIssueNetPay() {
		return attendanceIssueNetPay;
	}

	public void setAttendanceIssueNetPay(double attendanceIssueNetPay) {
		this.attendanceIssueNetPay = attendanceIssueNetPay;
	}

	public long getAttendanceIssueCount() {
		return attendanceIssueCount;
	}

	public void setAttendanceIssueCount(long attendanceIssueCount) {
		this.attendanceIssueCount = attendanceIssueCount;
	}
	

}
