package com.krishna.dto.invoice;

public class InvoiceSourceAddDto {

	private String companyName;

	private boolean isArchived;

	private String gstNumber;

	private String companyPAN;

	private String address;

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public boolean isArchived() {
		return isArchived;
	}

	public void setArchived(boolean isArchived) {
		this.isArchived = isArchived;
	}

	public String getGstNumber() {
		return gstNumber;
	}

	public void setGstNumber(String gstNumber) {
		this.gstNumber = gstNumber;
	}

	public String getCompanyPAN() {
		return companyPAN;
	}

	public void setCompanyPAN(String companyPAN) {
		this.companyPAN = companyPAN;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
