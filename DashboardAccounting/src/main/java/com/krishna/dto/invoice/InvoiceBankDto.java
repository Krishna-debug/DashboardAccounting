package com.krishna.dto.invoice;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


public class InvoiceBankDto {

	private String name;

	private String paymentRoute;

	private String accountNumber;

	private String ifscCode;

	private String swiftCode;

	private String bankAddress;

	private String paymentRemittanceDescription;

	private String paymentPurpose;
	
	@NotNull @NotEmpty
	private String beneficiaryName;

	private String routingNumber;

	public String getRoutingNumber() {
		return routingNumber;
	}

	public void setRoutingNumber(String routingNumber) {
		this.routingNumber = routingNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPaymentRoute() {
		return paymentRoute;
	}

	public void setPaymentRoute(String paymentRoute) {
		this.paymentRoute = paymentRoute;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getIfscCode() {
		return ifscCode;
	}

	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}

	public String getSwiftCode() {
		return swiftCode;
	}

	public void setSwiftCode(String swiftCode) {
		this.swiftCode = swiftCode;
	}

	public String getBankAddress() {
		return bankAddress;
	}

	public void setBankAddress(String bankAddress) {
		this.bankAddress = bankAddress;
	}

	public String getPaymentRemittanceDescription() {
		return paymentRemittanceDescription;
	}

	public void setPaymentRemittanceDescription(String paymentRemittanceDescription) {
		this.paymentRemittanceDescription = paymentRemittanceDescription;
	}

	public String getPaymentPurpose() {
		return paymentPurpose;
	}

	public void setPaymentPurpose(String paymentPurpose) {
		this.paymentPurpose = paymentPurpose;
	}

	public String getBeneficiaryName() {
		return beneficiaryName;
	}

	public void setBeneficiaryName(String beneficiaryName) {
		this.beneficiaryName = beneficiaryName;
	}
	
}
