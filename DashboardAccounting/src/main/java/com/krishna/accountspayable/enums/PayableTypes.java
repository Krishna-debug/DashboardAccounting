package com.krishna.accountspayable.enums;

public enum PayableTypes {
	ADHOC("adhoc"), RECURRING("recurring");
	
	private String value;
	
	PayableTypes(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
}
