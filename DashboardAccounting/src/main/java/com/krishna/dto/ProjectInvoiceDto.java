package com.krishna.dto;

import java.util.Date;

public class ProjectInvoiceDto {

	Long id;
	
	Long leadId;
	
	String project;

	double amount;

	double amountInDollar;

	String currency;

	long invoiceCycleId;

	long modeOfPaymentId;

	long paymentTermsId;

	Date billingDate;

	Date dueDate;

	String comment;

	String manager;

	String clientName;

	double exchangeRate;

	long invoiceStatusId;

	Date receivedOn;

	String month;

	String year;

	Long projectId;

	long managerId;

	double tdsValue;

	private Boolean isInternal;

	private Long raisedFromBu;

	private Long raisedToBu;

	private Long bankId;

	private String invoiceType;

	private Long fromDate;

	private Long toDate;

	private String placeOfSupply;

	private Boolean isIfsd;

	private Boolean isMilestone;

	private Double paymentCharges = 0D;

	private Boolean importItems;

	private Double cgst;

	private Double sgst;

	private Double igst;

	private String splitType;

	private String cityOfSupply;
	
	private Double adjustmentAmount=0D;
	
	private Long securityDepositeId;
	
	private String ifsdStatus;
	
	private Boolean isKycComplaint;

	private long bankLocationId;

	private Double waivedOffAmount;
	
	private String payingEntityName;
	
	private String currencyRecevied;
	
	private Double recievedAmount=0D;
	
	
	public long getLeadId() {
		return leadId;
	}

	public void setLeadId(long leadId) {
		this.leadId = leadId;
	}

	public long getBankLocationId() {
		return bankLocationId;
	}

	public void setBankLocationId(long bankLocationId) {
		this.bankLocationId = bankLocationId;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public Date getReceivedOn() {
		return receivedOn;
	}

	public void setReceivedOn(Date receivedOn) {
		this.receivedOn = receivedOn;
	}

	public long getInvoiceStatusId() {
		return invoiceStatusId;
	}

	public void setInvoiceStatusId(long invoiceStatusId) {
		this.invoiceStatusId = invoiceStatusId;
	}

	public double getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(double exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public long getInvoiceCycleId() {
		return invoiceCycleId;
	}

	public void setInvoiceCycleId(long invoiceCycleId) {
		this.invoiceCycleId = invoiceCycleId;
	}

	public long getModeOfPaymentId() {
		return modeOfPaymentId;
	}

	public void setModeOfPaymentId(long modeOfPaymentId) {
		this.modeOfPaymentId = modeOfPaymentId;
	}

	public Date getBillingDate() {
		return billingDate;
	}

	public void setBillingDate(Date billingDate) {
		this.billingDate = billingDate;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public double getAmountInDollar() {
		return amountInDollar;
	}

	public void setAmountInDollar(double amountInDollar) {
		this.amountInDollar = amountInDollar;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Long getProjectId() {
		return projectId;
	}

	public long getManagerId() {
		return managerId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public void setManagerId(long managerId) {
		this.managerId = managerId;
	}

	public double getTdsValue() {
		return tdsValue;
	}

	public void setTdsValue(double tdsValue) {
		this.tdsValue = tdsValue;
	}

	public long getPaymentTermsId() {
		return paymentTermsId;
	}

	public void setPaymentTermsId(long paymentTermsId) {
		this.paymentTermsId = paymentTermsId;
	}

	public Boolean getIsInternal() {
		return isInternal;
	}

	public void setIsInternal(Boolean isInternal) {
		this.isInternal = isInternal;
	}

	public Long getRaisedFromBu() {
		return raisedFromBu;
	}

	public void setRaisedFromBu(Long raisedFromBu) {
		this.raisedFromBu = raisedFromBu;
	}

	public Long getRaisedToBu() {
		return raisedToBu;
	}

	public void setRaisedToBu(Long raisedToBu) {
		this.raisedToBu = raisedToBu;
	}

	public Long getBankId() {
		return bankId;
	}

	public void setBankId(Long bankId) {
		this.bankId = bankId;
	}

	public String getInvoiceType() {
		return invoiceType;
	}

	public void setInvoiceType(String invoiceType) {
		this.invoiceType = invoiceType;
	}

	public Long getFromDate() {
		return fromDate;
	}

	public void setFromDate(Long fromDate) {
		this.fromDate = fromDate;
	}

	public Long getToDate() {
		return toDate;
	}

	public void setToDate(Long toDate) {
		this.toDate = toDate;
	}

	public String getPlaceOfSupply() {
		return placeOfSupply;
	}

	public void setPlaceOfSupply(String placeOfSupply) {
		this.placeOfSupply = placeOfSupply;
	}

	public Boolean getIsIfsd() {
		return isIfsd;
	}

	public void setIsIfsd(Boolean isIfsd) {
		this.isIfsd = isIfsd;
	}

	public Boolean getIsMilestone() {
		return isMilestone;
	}

	public void setIsMilestone(Boolean isMilestone) {
		this.isMilestone = isMilestone;
	}

	public Double getPaymentCharges() {
		return paymentCharges;
	}

	public void setPaymentCharges(Double paymentCharges) {
		this.paymentCharges = paymentCharges;
	}

	public Boolean getImportItems() {
		return importItems;
	}

	public void setImportItems(Boolean importItems) {
		this.importItems = importItems;
	}

	public Double getCgst() {
		return cgst;
	}

	public void setCgst(Double cgst) {
		this.cgst = cgst;
	}

	public Double getSgst() {
		return sgst;
	}

	public void setSgst(Double sgst) {
		this.sgst = sgst;
	}

	public Double getIgst() {
		return igst;
	}

	public void setIgst(Double igst) {
		this.igst = igst;
	}

	public String getSplitType() {
		return splitType;
	}

	public void setSplitType(String splitType) {
		this.splitType = splitType;
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
		securityDepositeId = securityDepositeId;
	}

	public String getIfsdStatus() {
		return ifsdStatus;
	}

	public void setIfsdStatus(String ifsdStatus) {
		this.ifsdStatus = ifsdStatus;
	}

	public Boolean getIsKycComplaint() {
		return isKycComplaint;
	}

	public void setIsKycComplaint(Boolean isKycComplaint) {
		this.isKycComplaint = isKycComplaint;
	}

	public Double getWaivedOffAmount() {
		return waivedOffAmount;
	}

	public void setWaivedOffAmount(Double waivedOffAmount) {
		this.waivedOffAmount = waivedOffAmount;
	}

	public String getPayingEntityName() {
		return payingEntityName;
	}

	public void setPayingEntityName(String payingEntityName) {
		this.payingEntityName = payingEntityName;
	}

	public String getCurrencyRecevied() {
		return currencyRecevied;
	}

	public void setCurrencyRecevied(String currencyRecevied) {
		this.currencyRecevied = currencyRecevied;
	}

	public Double getRecievedAmount() {
		return recievedAmount;
	}

	public void setRecievedAmount(Double recievedAmount) {
		this.recievedAmount = recievedAmount;
	}
	
	
	
}
