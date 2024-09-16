package com.krishna.enums;

public enum Months {
	JANUARY("January"), FEBRUARY("February"), MARCH("March"), APRIL("April"), MAY("May"), JUNE("June"), 
	JULY("July"), AUGUST("August"), SEPTEMBER("September"), OCTOBER("October"), NOVEMBER("November"), 
	DECEMBER("December");
	
	private String value;
	
	private Months(String value) {
		this.value = value;
	}
	
	public String getMonths() {
		return value;
	}
}
