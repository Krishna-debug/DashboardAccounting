package com.krishna.dto.invoice;

import java.util.Date;

public class ProjectInvoiceGenerateDto {

	Long id;

	Date billingDate;

	Date dueDate;

	long invoiceStatus;

	Date receivedOn;

	double tdsValue;

	private String bankName;

	private Double exchangeRate;

	private Double amount;

	private Double amountInDollar;

	private String generationStatus;

	private String taxable;

	private String placeOfSupply;
	private String cityOfSupply;

	private String payDetails;

	private Boolean isOthersInUrl;

	private String domesticType;

	private Boolean isIfsd;

	private Double adjustmentAmount=0D;
	
	private Long securityDepositeId;
	
	private Boolean isIfsdAdjustment;
	
	private Boolean isKycComplaint;
	private Boolean isDollarCurrency=false;


	public Boolean getIsKycComplaint() {
		return isKycComplaint;
	}

	public void setIsKycComplaint(Boolean isKycComplaint) {
		this.isKycComplaint = isKycComplaint;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getBillingDate() {
		return billingDate;
	}

	public void setBillingDate(Date billingDate) {
		this.billingDate = billingDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public long getInvoiceStatus() {
		return invoiceStatus;
	}

	public void setInvoiceStatus(long invoiceStatus) {
		this.invoiceStatus = invoiceStatus;
	}

	public Date getReceivedOn() {
		return receivedOn;
	}

	public void setReceivedOn(Date receivedOn) {
		this.receivedOn = receivedOn;
	}

	public double getTdsValue() {
		return tdsValue;
	}

	public void setTdsValue(double tdsValue) {
		this.tdsValue = tdsValue;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public Double getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(Double exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Double getAmountInDollar() {
		return amountInDollar;
	}

	public void setAmountInDollar(Double amountInDollar) {
		this.amountInDollar = amountInDollar;
	}

	public String getGenerationStatus() {
		return generationStatus;
	}

	public void setGenerationStatus(String generationStatus) {
		this.generationStatus = generationStatus;
	}

	public String getTaxable() {
		return taxable;
	}

	public void setTaxable(String taxable) {
		this.taxable = taxable;
	}

	public String getPlaceOfSupply() {
		return placeOfSupply;
	}

	public void setPlaceOfSupply(String placeOfSupply) {
		this.placeOfSupply = placeOfSupply;
	}

	public String getPayDetails() {
		return payDetails;
	}

	public void setPayDetails(String payDetails) {
		this.payDetails = payDetails;
	}

	public Boolean getIsOthersInUrl() {
		return isOthersInUrl;
	}

	public void setIsOthersInUrl(Boolean isOthersInUrl) {
		this.isOthersInUrl = isOthersInUrl;
	}

	public String getDomesticType() {
		return domesticType;
	}

	public void setDomesticType(String domesticType) {
		this.domesticType = domesticType;
	}

	public Boolean getIsIfsd() {
		return isIfsd;
	}

	public void setIsIfsd(Boolean isIfsd) {
		this.isIfsd = isIfsd;
	}

	public String getCityOfSupply() {
		return cityOfSupply;
	}

	public void setCityOfSupply(String cityOfSupply) {
		this.cityOfSupply = cityOfSupply;
	}

	public Double getAdjustmentAmount() {
		return adjustmentAmount;
	}

	public void setAdjustmentAmount(Double adjustmentAmount) {
		this.adjustmentAmount = adjustmentAmount;
	}

	public Long getSecurityDepositeId() {
		return securityDepositeId;
	}

	public void setSecurityDepositeId(Long securityDepositeId) {
		this.securityDepositeId = securityDepositeId;
	}

	public Boolean getIsIfsdAdjustment() {
		return isIfsdAdjustment;
	}

	public void setIsIfsdAdjustment(Boolean isIfsdAdjustment) {
		this.isIfsdAdjustment = isIfsdAdjustment;
	}

	public Boolean getIsDollarCurrency() {
		return isDollarCurrency;
	}

	public void setIsDollarCurrency(Boolean isDollarCurrency) {
		this.isDollarCurrency = isDollarCurrency;
	}

}
