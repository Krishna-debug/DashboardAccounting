package com.krishna.enums;

/**
 * The Enum PFSubscription
 * @author shivangi
 *
 */
public enum PFSubscription {
	
	YES("Yes"),
	NO("No");
	
	private String pfSubscription;

	public String getPfSubscription() {
		return pfSubscription;
	}

	private PFSubscription(String pfSubscription) {
		this.pfSubscription = pfSubscription;
	}

}
