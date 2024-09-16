package com.krishna.dto;

import java.util.List;

public class AverageBillingRateDto {
	
	private int month;
	private int year;
	private List<String> billingRateFilter;
	private List<String> resourceHourFilter;
	private List<String> differencePercFilter;
	public int getMonth() {
		return month;
	}
	public void setMonth(int month) {
		this.month = month;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public List<String> getBillingRateFilter() {
		return billingRateFilter;
	}
	public void setBillingRateFilter(List<String> billingRateFilter) {
		this.billingRateFilter = billingRateFilter;
	}
	public List<String> getResourceHourFilter() {
		return resourceHourFilter;
	}
	public void setResourceHourFilter(List<String> resourceHourFilter) {
		this.resourceHourFilter = resourceHourFilter;
	}
	public List<String> getDifferencePercFilter() {
		return differencePercFilter;
	}
	public void setDifferencePercFilter(List<String> differencePercFilter) {
		this.differencePercFilter = differencePercFilter;
	}
	
}
