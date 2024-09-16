package com.krishna.dto;

import java.util.Date;

public class InvoiceFilterDto {

	private String month;

	private String year;

	private String aging;

	private String businessVertical;

	private boolean isInternal;
	
	private String currencyType;
	
	private Long teamHeadId;
	private Long fromDate;
	private Long toDate;

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getAging() {
		return aging;
	}

	public void setAging(String aging) {
		this.aging = aging;
	}

	public String getBusinessVertical() {
		return businessVertical;
	}

	public void setBusinessVertical(String businessVertical) {
		this.businessVertical = businessVertical;
	}

	public boolean isInternal() {
		return isInternal;
	}

	public void setInternal(boolean isInternal) {
		this.isInternal = isInternal;
	}

	public String getCurrencyType() {
		return currencyType;
	}

	public void setCurrencyType(String currencyType) {
		this.currencyType = currencyType;
	}

	public Long getToDate() {
		return toDate;
	}

	public void setToDate(Long toDate) {
		this.toDate = toDate;
	}

	public Long getFromDate() {
		return fromDate;
	}

	public void setFromDate(Long fromDate) {
		this.fromDate = fromDate;
	}

	public Long getTeamHeadId() {
		return teamHeadId;
	}

	public void setTeamHeadId(Long teamHeadId) {
		this.teamHeadId = teamHeadId;
	}


}
