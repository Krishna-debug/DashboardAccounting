package com.krishna.enums;

public enum PayStatus {

	PAID("Paid"), UNPAID("Unpaid"), CANCELLED("Cancelled") ;

	private String payStatus;

	public String getInvoiceGenerationStatus() {
		return payStatus;
	}

	private PayStatus(String payStatus) {
		this.payStatus = payStatus;
	}
	
	public static PayStatus[] getSecurityDepositePayStatus(){
		 return PayStatus.values();
	 }
   public static PayStatus getSecurityDepositeEnumStatus(String key){
		for(PayStatus keyValues:PayStatus.values()) {
			if(keyValues.getInvoiceGenerationStatus().equals(key)) 
				return keyValues;
		}
		return null;
	}

}
