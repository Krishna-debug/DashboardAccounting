package com.krishna.dto;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.ElementCollection;

import com.krishna.enums.PayRollStatus;

public class PayrollDto {
	
	private long userId;
	
	private double basicPay;

	private double hra;

	private double conveyance;

	private double medicalAllowance;

	private double projectAllowance;

	private double employerPfContribution;

	private double totalMonthlyPay;
	
	private double incentives;

	private double employeePfContribution;
	
	private double tds;
	
	private double healthInsurance;
	
	private double infraDeduction;
	
	private PayRollStatus payrollStatus;
	
	private int payDays;
	
	private int unpaidDays;
	
	private double statutoryMaternityPay;
	
	private boolean isVerified;
	
	@ElementCollection
	private Collection<String> columnEarnings = new ArrayList<>();
	
	@ElementCollection
	private Collection<String> columnDeductions = new ArrayList<>();
	
	private double laptopAllowance;
	
	private Double workFromHomeAllowance;
	
	private double specialAllowance;
	
	private double leaveDeductions;
	
	private double paidLeaveAdditions;
	
	private Double variablePay;
	
	private Double voluntaryPay;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
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

	public void setEmployerPfContribution(double employerPfContribution) {
		this.employerPfContribution = employerPfContribution;
	}

	public double getTotalMonthlyPay() {
		return totalMonthlyPay;
	}

	public void setTotalMonthlyPay(double totalMonthlyPay) {
		this.totalMonthlyPay = totalMonthlyPay;
	}

	public double getIncentives() {
		return incentives;
	}

	public void setIncentives(double incentives) {
		this.incentives = incentives;
	}

	public double getTds() {
		return tds;
	}

	public void setTds(double tds) {
		this.tds = tds;
	}

	public double getHealthInsurance() {
		return healthInsurance;
	}

	public void setHealthInsurance(double healthInsurance) {
		this.healthInsurance = healthInsurance;
	}

	public double getInfraDeduction() {
		return infraDeduction;
	}

	public void setInfraDeduction(double infraDeduction) {
		this.infraDeduction = infraDeduction;
	}

	public PayRollStatus getPayrollStatus() {
		return payrollStatus;
	}

	public void setPayrollStatus(PayRollStatus payrollStatus) {
		this.payrollStatus = payrollStatus;
	}

	public int getPayDays() {
		return payDays;
	}

	public void setPayDays(int payDays) {
		this.payDays = payDays;
	}

	public int getUnpaidDays() {
		return unpaidDays;
	}

	public void setUnpaidDays(int unpaidDays) {
		this.unpaidDays = unpaidDays;
	}

	public double getStatutoryMaternityPay() {
		return statutoryMaternityPay;
	}

	public void setStatutoryMaternityPay(double statutoryMaternityPay) {
		this.statutoryMaternityPay = statutoryMaternityPay;
	}

	public double getEmployeePfContribution() {
		return employeePfContribution;
	}

	public void setEmployeePfContribution(double employeePfContribution) {
		this.employeePfContribution = employeePfContribution;
	}

	public boolean isVerified() {
		return isVerified;
	}

	public void setVerified(boolean isVerified) {
		this.isVerified = isVerified;
	}

	public Collection<String> getColumnEarnings() {
		return columnEarnings;
	}

	public Collection<String> getColumnDeductions() {
		return columnDeductions;
	}

	public void setColumnEarnings(Collection<String> columnEarnings) {
		this.columnEarnings = columnEarnings;
	}

	public void setColumnDeductions(Collection<String> columnDeductions) {
		this.columnDeductions = columnDeductions;
	}

	public double getLaptopAllowance() {
		return laptopAllowance;
	}

	public void setLaptopAllowance(double laptopAllowance) {
		this.laptopAllowance = laptopAllowance;
	}

	public double getLeaveDeductions() {
		return leaveDeductions;
	}

	public void setLeaveDeductions(double leaveDeductions) {
		this.leaveDeductions = leaveDeductions;
	}

	public double getPaidLeaveAdditions() {
		return paidLeaveAdditions;
	}

	public void setPaidLeaveAdditions(double paidLeaveAdditions) {
		this.paidLeaveAdditions = paidLeaveAdditions;
	}

	public Double getWorkFromHomeAllowance() {
		return workFromHomeAllowance;
	}

	public void setWorkFromHomeAllowance(Double workFromHomeAllowance) {
		this.workFromHomeAllowance = workFromHomeAllowance;
	}

	public double getSpecialAllowance() {
		return specialAllowance;
	}

	public void setSpecialAllowance(double specialAllowance) {
		this.specialAllowance = specialAllowance;
	}

	public Double getVariablePay() {
		return variablePay;
	}

	public void setVariablePay(Double variablePay) {
		this.variablePay = variablePay;
	}

	public Double getVoluntaryPay() {
		return voluntaryPay;
	}

	public void setVoluntaryPay(Double voluntaryPay) {
		this.voluntaryPay = voluntaryPay;
	}
	@Override
	public String toString() {
		return "PayrollDto [userId=" + userId + ", basicPay=" + basicPay + ", hra=" + hra + ", conveyance=" + conveyance
				+ ", medicalAllowance=" + medicalAllowance + ", projectAllowance=" + projectAllowance
				+ ", employerPfContribution=" + employerPfContribution + ", totalMonthlyPay=" + totalMonthlyPay
				+ ", incentives=" + incentives + ", employeePfContribution=" + employeePfContribution + ", tds=" + tds
				+ ", healthInsurance=" + healthInsurance + ", infraDeduction=" + infraDeduction + ", payrollStatus="
				+ payrollStatus + ", payDays=" + payDays + ", unpaidDays=" + unpaidDays + ", statutoryMaternityPay="
				+ statutoryMaternityPay + ", isVerified=" + isVerified + ", columnEarnings=" + columnEarnings
				+ ", columnDeductions=" + columnDeductions + ", laptopAllowance=" + laptopAllowance + "]";
	}

}
