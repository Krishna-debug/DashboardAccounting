package com.krishna.enums;

public enum InvoiceGenerationStatus {
	
	PENDING("Pending"), INDRAFT("In_Draft"),GENERATED("Generated");

	private String invoiceGenerationStatus;

	public String getInvoiceGenerationStatus() {
		return invoiceGenerationStatus;
	}

	private InvoiceGenerationStatus(String invoiceGenerationStatus) {
		this.invoiceGenerationStatus = invoiceGenerationStatus;
	}

}
