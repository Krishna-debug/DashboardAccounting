package com.krishna.dto;

public class PayslipDto {
	
	private double basicPay;

	private double hra;

	private double conveyance;

	private double medicalAllowance;

	private double projectAllowance;
	
	private double incentives;
	
	private double tds;
	
	private double healthInsurance;
	
	private double infraDeduction;
	
	private int payDays;
	
	private int unpaidDays;
	
	private Double workFromHomeAllowance;
	
	private Double specialAllowance;

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

	public Double getSpecialAllowance() {
		return specialAllowance;
	}

	public Double getWorkFromHomeAllowance() {
		return workFromHomeAllowance;
	}

	public void setWorkFromHomeAllowance(Double workFromHomeAllowance) {
		this.workFromHomeAllowance = workFromHomeAllowance;
	}

	public void setSpecialAllowance(Double specialAllowance) {
		this.specialAllowance = specialAllowance;
	}

}
