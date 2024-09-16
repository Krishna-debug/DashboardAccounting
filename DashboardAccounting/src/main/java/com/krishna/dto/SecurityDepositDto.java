package com.krishna.dto;

import java.util.Date;

public class SecurityDepositDto {
	
	private long id ;
	
	private long leadId;
	
	private long clientId;
	
    private long projectId;
	
	private long managerId;
	
	private double amount;
	
	private String clientName;
	
    private Date receivedOn;
	
	private double amountInDollar;
	
	private String currency;
	
	private double exchangeRate;
	
	private Double paymentCharges = 0D;
	
	private String ifsdStatus;
	
	private String comment;
	
	public long getLeadId() {
		return leadId;
	}

	public void setLeadId(long leadId) {
		this.leadId = leadId;
	}
	
	public long getClientId() {
		return clientId;
	}

	public void setClientId(long clientId) {
		this.clientId = clientId;
	}
	
	public String getIfsdStatus() {
		return ifsdStatus;
	}

	public void setIfsdStatus(String ifsdStatus) {
		this.ifsdStatus = ifsdStatus;
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

	public double getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(double exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public Date getReceivedOn() {
		return receivedOn;
	}

	public void setReceivedOn(Date receivedOn) {
		this.receivedOn = receivedOn;
	}

	public long getProjectId() {
		return projectId;
	}

	public void setProjectId(long projectId) {
		this.projectId = projectId;
	}

	public long getManagerId() {
		return managerId;
	}

	public void setManagerId(long managerId) {
		this.managerId = managerId;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Double getPaymentCharges() {
		return paymentCharges;
	}

	public void setPaymentCharges(Double paymentCharges) {
		this.paymentCharges = paymentCharges;
	}
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
