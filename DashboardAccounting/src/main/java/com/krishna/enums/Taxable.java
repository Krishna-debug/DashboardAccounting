package com.krishna.enums;

public enum Taxable {

	YES("Yes"), NO("No");

	private String taxable;

	public String getTaxable() {
		return taxable;
	}

	public void setTaxable(String taxable) {
		this.taxable = taxable;
	}

	private Taxable(String taxable) {
		this.taxable = taxable;
	}

}
