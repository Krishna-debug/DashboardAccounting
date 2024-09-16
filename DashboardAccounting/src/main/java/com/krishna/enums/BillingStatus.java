package com.krishna.enums;

public enum BillingStatus {
	
	 PENDING("Pending"),PAID("Paid"),IN_TRANSIT("In Transit");
     private String value;
     
     private BillingStatus(String value) {
 		this.value = value;
     }

 	public String getValue() {
 			return value;
 	}

	public static BillingStatus getbillingStatus(String key) {
		for (BillingStatus keyValues : BillingStatus.values()) {

			if (keyValues.getValue().equals(key)) {
				return keyValues;
			}
		}
		return null;
	}

	public static BillingStatus[] getAllBillingStatus() {
		return BillingStatus.values();
	}

}
