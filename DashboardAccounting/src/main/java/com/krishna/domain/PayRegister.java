package com.krishna.domain;

import java.time.LocalDateTime;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;
import org.springframework.lang.NonNull;

import com.krishna.enums.PFSubscription;
import com.krishna.enums.PayRegisterStatus;
import com.krishna.util.DoubleEncryptDecryptConverter;

/**
 * @author shivangi
 *
 */
@Audited
@Entity
@Table(name = "payRegister")
public class PayRegister {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*ID*/
	private long id;

	/*User's Id*/
	private long userId;

	@NonNull
	/*Id of Employee*/
	private String employeeId;
	/*PAN Number of employee*/
	private String panNumber;
	/*UAN number of employee*/
	private String uan;

	
	/*Bank Object from Bank Table*/
	@OneToOne(fetch = FetchType.LAZY)
	private Bank bank;
	/*Account number of an Employee*/
	private String accountNo;
	/*IFSC Code of employee's Bank*/
	private String ifsc;

	@Enumerated(EnumType.STRING)
	/*If PF subscription is Availed or not -> Yes or No*/
	private PFSubscription pfSubscription;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*Basic Payor Salary of an employee*/
	private double basicPay;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*House Rent allowance for an Employee*/
	private double hra;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*Amount of conveyance Amount*/
	private double conveyance;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*Amount of medical allowance*/
	private double medicalAllowance;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*Amoun tof Project Allowance*/
	private double projectAllowance;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*Amount of Employer's side PF contribution*/
	private double employerPfContribution;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*Total monthly Pay to empkloyee*/
	private double totalMonthlyPay;
	/*Start Date for changes if any*/
	private LocalDateTime effectiveDate;
	/*Date of creation*/
	private LocalDateTime creationDate;
	/*Last updated on*/
	private LocalDateTime lastUpdatedOn;
	/*User id of creator*/
	private long createdBy;

	@Enumerated(EnumType.STRING)
	/*If Payment completed or not -> COMPLETED or INCOMPLETED*/
	private PayRegisterStatus status;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*Amount for Maternity Pay*/
	private double statutoryMaternityPay = 0.0;
	/*If employee is currently present or not*/
	private boolean isCurrent = true;
	/*Effective to Date*/
	private LocalDateTime effectiveTo;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*Amoun tof Laptop Allowance*/
	private double laptopAllowance;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*Annual Cost to Company*/
	private double annualCTC;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*Total Annual Cost to Company*/
	private double totalAnnualCtc;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*Amount for Paid Leaves*/
	private double paidLeavesAmount;
	/*Comment on Pay*/
	private String comment;
	
	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
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

	public Bank getBank() {
		return bank;
	}

	public void setBank(Bank bank) {
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

	public PFSubscription getPfSubscription() {
		return pfSubscription;
	}

	public void setPfSubscription(PFSubscription pfSubscription) {
		this.pfSubscription = pfSubscription;
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

	public LocalDateTime getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(LocalDateTime effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public LocalDateTime getLastUpdatedOn() {
		return lastUpdatedOn;
	}

	public void setLastUpdatedOn(LocalDateTime lastUpdatedOn) {
		this.lastUpdatedOn = lastUpdatedOn;
	}

	public long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(long createdBy) {
		this.createdBy = createdBy;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	public boolean isCurrent() {
		return isCurrent;
	}

	public void setCurrent(boolean isCurrent) {
		this.isCurrent = isCurrent;
	}

	public LocalDateTime getEffectiveTo() {
		return effectiveTo;
	}

	public void setEffectiveTo(LocalDateTime effectiveTo) {
		this.effectiveTo = effectiveTo;
	}

	public double getLaptopAllowance() {
		return laptopAllowance;
	}

	public void setLaptopAllowance(double laptopAllowance) {
		this.laptopAllowance = laptopAllowance;
	}

	public double getAnnualCTC() {
		return annualCTC;
	}

	public void setAnnualCTC(double annualCTC) {
		this.annualCTC = annualCTC;
	}

	public double getTotalAnnualCtc() {
		return totalAnnualCtc;
	}

	public void setTotalAnnualCtc(double totalAnnualCtc) {
		this.totalAnnualCtc = totalAnnualCtc;
	}

	public double getPaidLeavesAmount() {
		return paidLeavesAmount;
	}

	public void setPaidLeavesAmount(double paidLeavesAmount) {
		this.paidLeavesAmount = paidLeavesAmount;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "PayRegister [id=" + id + ", userId=" + userId + ", employeeId=" + employeeId + ", panNumber="
				+ panNumber + ", uan=" + uan + ", bank=" + bank + ", accountNo=" + accountNo + ", ifsc=" + ifsc
				+ ", pfSubscription=" + pfSubscription + ", basicPay=" + basicPay + ", hra=" + hra + ", conveyance="
				+ conveyance + ", medicalAllowance=" + medicalAllowance + ", projectAllowance=" + projectAllowance
				+ ", employerPfContribution=" + employerPfContribution + ", totalMonthlyPay=" + totalMonthlyPay
				+ ", effectiveDate=" + effectiveDate + ", creationDate=" + creationDate + ", lastUpdatedOn="
				+ lastUpdatedOn + ", createdBy=" + createdBy + ", status=" + status + ", statutoryMaternityPay="
				+ statutoryMaternityPay + ", isCurrent=" + isCurrent + ", effectiveTo=" + effectiveTo
				+ ", laptopAllowance=" + laptopAllowance + "]";
	}

}
