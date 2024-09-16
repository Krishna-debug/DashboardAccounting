package com.krishna.dto;

import com.krishna.enums.PFSubscription;
import com.krishna.enums.PayRegisterStatus;

public class PayRegisterDto {

	private long userId;

	private String panNumber;

	private String uan;

	private long bank;

	private String accountNo;

	private String ifsc;

	private PFSubscription pfSubscription;

	private double basicPay;

	private double hra;

	private double conveyance;

	private double medicalAllowance;

	private double projectAllowance;

	private double employerPfContribution;

	private double totalMonthlyPay;

	private long effectiveDate;

	private PayRegisterStatus status;

	private double statutoryMaternityPay;

	private double laptopAllowance;
	
	private String comment;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getPanNumber() {
		return panNumber;
	}

	public void setPanNumber(String panNumber) {
		this.panNumber = panNumber;
	}

	public String getUan() {
		return uan;
	}

	public void setUan(String uan) {
		this.uan = uan;
	}

	public long getBank() {
		return bank;
	}

	public void setBank(long bank) {
		this.bank = bank;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
	}

	public String getIfsc() {
		return ifsc;
	}

	public void setIfsc(String ifsc) {
		this.ifsc = ifsc;
	}

	public double getBasicPay() {
		return basicPay;
	}

	public void setBasicPay(double basicPay) {
		this.basicPay = basicPay;
	}

	public double getHra() {
		return hra;
	}

	public void setHra(double hra) {
		this.hra = hra;
	}

	public double getConveyance() {
		return conveyance;
	}

	public void setConveyance(double conveyance) {
		this.conveyance = conveyance;
	}

	public double getMedicalAllowance() {
		return medicalAllowance;
	}

	public void setMedicalAllowance(double medicalAllowance) {
		this.medicalAllowance = medicalAllowance;
	}

	public double getProjectAllowance() {
		return projectAllowance;
	}

	public void setProjectAllowance(double projectAllowance) {
		this.projectAllowance = projectAllowance;
	}

	public double getEmployerPfContribution() {
		return employerPfContribution;
	}

	public void setEmployerPfContribution(double employeePfContribution) {
		this.employerPfContribution = employeePfContribution;
	}

	public double getTotalMonthlyPay() {
		return totalMonthlyPay;
	}

	public void setTotalMonthlyPay(double totalMonthlyPay) {
		this.totalMonthlyPay = totalMonthlyPay;
	}

	public PFSubscription getPfSubscription() {
		return pfSubscription;
	}

	public void setPfSubscription(PFSubscription pfSubscription) {
		this.pfSubscription = pfSubscription;
	}

	public long getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(long effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public PayRegisterStatus getStatus() {
		return status;
	}

	public void setStatus(PayRegisterStatus status) {
		this.status = status;
	}

	public double getStatutoryMaternityPay() {
		return statutoryMaternityPay;
	}

	public void setStatutoryMaternityPay(double statutoryMaternityPay) {
		this.statutoryMaternityPay = statutoryMaternityPay;
	}

	public double getLaptopAllowance() {
		return laptopAllowance;
	}

	public void setLaptopAllowance(double laptopAllowance) {
		this.laptopAllowance = laptopAllowance;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
