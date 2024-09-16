package com.krishna.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Proxy;
import org.hibernate.envers.Audited;
import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.krishna.enums.PFSubscription;
import com.krishna.enums.PayRegisterStatus;
import com.krishna.enums.PayRollStatus;
import com.krishna.util.DoubleEncryptDecryptConverter;

//import lombok.Data;

/**
 * @author shivangi
 * 
 *         The Class Payroll
 * 
 *         Monthly pay Data
 */
@Audited
@Entity
@Table(name = "payRoll")
@JsonIgnoreProperties(ignoreUnknown = false)
@Proxy(lazy = false)
public class Payroll {

	/* The payroll Id */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	/* The User Id */
	private long userId;

	/* The Employee Id */
	@NonNull
	private String employeeId;

	/* The Employee designation */
	private String designation;

	/* The Employee Grade */
	private String grade;

	/* The Employee Department */
	private String department;

	/* The Employee status */
	private String employmentStatus;

	/* The Employee Pan Number */
	private String panNumber;

	/* The Employee UAN */
	private String uan;

	/* The Employee Bank */
	@OneToOne(fetch = FetchType.LAZY)
	private Bank bank;

	/* The account Number */
	private String accountNo;

	/* The Ifsc Code */
	private String ifsc;

	/* The PF Subscription */
	@Enumerated(EnumType.STRING)
	private PFSubscription pfSubscription;

	/* The Employee basic pay */
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double basicPay;

	/* The House Residence allowance */
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double hra;

	/* The Conveyance charges */
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double conveyance;

	/* The Medical Allowance */
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double medicalAllowance;

	/* The Project Allowances */
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double projectAllowance;

	/* The Employer Pf Contribution */
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double employerPfContribution;

	/* Total default monthly pay */
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double totalMonthlyPay;

	/* The Pay effective From */
	private LocalDateTime effectiveDate;

	/* The Pay Register Status */
	@Enumerated(EnumType.STRING)
	private PayRegisterStatus status;

	/* The statutory Maternity Pay */
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double statutoryMaternityPay = 0.0;

	/* The pay effective to */
	private LocalDateTime effectiveTo;

	/* The pay Days */
	private double payDays;

	/* The unpaid Days */
	private double unpaidDays;

	/* The Incentives */
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double incentives;

	/* The Total Arrear */
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double totalArrear;

	/* The Total Reimbursement */
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double totalReimbursement;

	/* The Tds */
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double tds;

	/* The Health Insurance */
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double healthInsurance;

	/* The Infra Deductions */
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double infraDeductions;

	/* The Net Pay */
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double netPay;

	/* The Payroll Status */
	@Enumerated(EnumType.STRING)
	PayRollStatus payRollStatus;

	/* The Payroll Month */
	private int month;

	/* The Payroll Year */
	private int year;

	/* The Payroll generator */
	private long generatedBy;

	/* The Employee ProjectNames */
	@ElementCollection
	private Collection<String> projectNames = new ArrayList<>();

	/* The Employee ProjectIds */
	@ElementCollection
	private Collection<Long> projectIds = new ArrayList<>();

	/* The Payroll Generation Date */
	private LocalDateTime generatedOn;

	/* The Payroll Modification Date */
	private LocalDateTime modifiedOn;

	/* The Payroll Modifier */
	private long modifiedBy;

	/* The Payroll Employee PF Contribution */
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double employeePfContribution;

	/* The Payroll Arrear */
	@OneToMany
	@JsonIgnore
	public Set<Arrear> arrear;

	private boolean isVerified;

	private boolean isBasicPayChanged;

	private boolean isHraChanged;

	private boolean isConveyanceChanged;

	private boolean isMedicalChanged;

	private boolean isProjectAllowanceChanged;

	private boolean isEmployerPfContributionChanged;

	private boolean isProcessed;

	private String payrollComment;
	
	private String buApprovalComment;

	private Boolean buPayrollApproval;

	@ElementCollection
	@JsonIgnore
	private Collection<String> columnEarnings = new ArrayList<>();

	@ElementCollection
	@JsonIgnore
	private Collection<String> columnDeductions = new ArrayList<>();

	private boolean isAttendanceVerified;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double laptopAllowance;

	private boolean isLaptopAllowanceChanged;

	private boolean isSmpChanged;

	private Boolean isMarginIncluded = true;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double leaveDeductions;

	private boolean isDeleted = false;

	private boolean isPriority = false;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double paidLeaveAdditions;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private Double specialAllowance;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private Double workFromHomeAllowance;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double variablePayAmount;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double voluntaryPayAmount;

	public boolean isPriority() {
		return isPriority;
	}

	public void setPriority(boolean isPriority) {
		this.isPriority = isPriority;
	}

	public long getId() {
		return id;
	}

	public Set<Arrear> getArrear() {
		return arrear;
	}

	public void setArrear(Set<Arrear> arrear) {
		this.arrear = arrear;
	}

	public void setTotalArrear(double totalArrear) {
		this.totalArrear = totalArrear;
	}

	public void setId(long id) {
		this.id = id;
	}

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

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getEmploymentStatus() {
		return employmentStatus;
	}

	public void setEmploymentStatus(String employmentStatus) {
		this.employmentStatus = employmentStatus;
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

	public LocalDateTime getEffectiveTo() {
		return effectiveTo;
	}

	public void setEffectiveTo(LocalDateTime effectiveTo) {
		this.effectiveTo = effectiveTo;
	}

	public double getPayDays() {
		return payDays;
	}

	public void setPayDays(double payDays) {
		this.payDays = payDays;
	}

	public double getUnpaidDays() {
		return unpaidDays;
	}

	public void setUnpaidDays(double unpaidDays) {
		this.unpaidDays = unpaidDays;
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

	public double getInfraDeductions() {
		return infraDeductions;
	}

	public void setInfraDeductions(double infraDeductions) {
		this.infraDeductions = infraDeductions;
	}

	public double getNetPay() {
		return netPay;
	}

	public void setNetPay(double netPay) {
		this.netPay = netPay;
	}

	public PayRollStatus getPayRollStatus() {
		return payRollStatus;
	}

	public void setPayRollStatus(PayRollStatus payRollStatus) {
		this.payRollStatus = payRollStatus;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public long getGeneratedBy() {
		return generatedBy;
	}

	public void setGeneratedBy(long generatedBy) {
		this.generatedBy = generatedBy;
	}

	public LocalDateTime getGeneratedOn() {
		return generatedOn;
	}

	public Collection<String> getProjectNames() {
		return projectNames;
	}

	public void setProjectNames(Collection<String> projectNames) {
		this.projectNames = projectNames;
	}

	public Collection<Long> getProjectIds() {
		return projectIds;
	}

	public void setProjectIds(Collection<Long> projectIds) {
		this.projectIds = projectIds;
	}

	public void setGeneratedOn(LocalDateTime generatedOn) {
		this.generatedOn = generatedOn;
	}

	public LocalDateTime getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(LocalDateTime modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	public long getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(long modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public double getEmployeePfContribution() {
		return employeePfContribution;
	}

	public void setEmployeePfContribution(double employeePfContribution) {
		this.employeePfContribution = employeePfContribution;
	}

	public double getTotalArrear() {
		return totalArrear;
	}

	public boolean isVerified() {
		return isVerified;
	}

	public void setVerified(boolean isVerified) {
		this.isVerified = isVerified;
	}

	public boolean isBasicPayChanged() {
		return isBasicPayChanged;
	}

	public boolean isHraChanged() {
		return isHraChanged;
	}

	public boolean isConveyanceChanged() {
		return isConveyanceChanged;
	}

	public boolean isMedicalChanged() {
		return isMedicalChanged;
	}

	public boolean isProjectAllowanceChanged() {
		return isProjectAllowanceChanged;
	}

	public boolean isEmployerPfContributionChanged() {
		return isEmployerPfContributionChanged;
	}

	public void setBasicPayChanged(boolean isBasicPayChanged) {
		this.isBasicPayChanged = isBasicPayChanged;
	}

	public void setHraChanged(boolean isHraChanged) {
		this.isHraChanged = isHraChanged;
	}

	public void setConveyanceChanged(boolean isConveyanceChanged) {
		this.isConveyanceChanged = isConveyanceChanged;
	}

	public void setMedicalChanged(boolean isMedicalChanged) {
		this.isMedicalChanged = isMedicalChanged;
	}

	public void setProjectAllowanceChanged(boolean isProjectAllowanceChanged) {
		this.isProjectAllowanceChanged = isProjectAllowanceChanged;
	}

	public void setEmployerPfContributionChanged(boolean isEmployerPfContributionChanged) {
		this.isEmployerPfContributionChanged = isEmployerPfContributionChanged;
	}

	public boolean isProcessed() {
		return isProcessed;
	}

	public void setProcessed(boolean isProcessed) {
		this.isProcessed = isProcessed;
	}

	public String getPayrollComment() {
		return payrollComment;
	}

	public void setPayrollComment(String payrollComment) {
		this.payrollComment = payrollComment;
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

	public boolean isAttendanceVerified() {
		return isAttendanceVerified;
	}

	public void setAttendanceVerified(boolean isAttendanceVerified) {
		this.isAttendanceVerified = isAttendanceVerified;
	}

	public double getLaptopAllowance() {
		return laptopAllowance;
	}

	public void setLaptopAllowance(double laptopAllowance) {
		this.laptopAllowance = laptopAllowance;
	}

	public boolean isLaptopAllowanceChanged() {
		return isLaptopAllowanceChanged;
	}

	public boolean isSmpChanged() {
		return isSmpChanged;
	}

	public void setLaptopAllowanceChanged(boolean isLaptopAllowanceChanged) {
		this.isLaptopAllowanceChanged = isLaptopAllowanceChanged;
	}

	public void setSmpChanged(boolean isSmpChanged) {
		this.isSmpChanged = isSmpChanged;
	}

	public double getLeaveDeductions() {
		return leaveDeductions;
	}

	public void setLeaveDeductions(double leaveDeductions) {
		this.leaveDeductions = leaveDeductions;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public double getPaidLeaveAdditions() {
		return paidLeaveAdditions;
	}

	public void setPaidLeaveAdditions(double paidLeaveAdditions) {
		this.paidLeaveAdditions = paidLeaveAdditions;
	}

	public double getTotalReimbursement() {
		return totalReimbursement;
	}

	public void setTotalReimbursement(double totalReimbursement) {
		this.totalReimbursement = totalReimbursement;
	}

	public Boolean getIsMarginIncluded() {
		return isMarginIncluded;
	}

	public void setIsMarginIncluded(Boolean isMarginIncluded) {
		this.isMarginIncluded = isMarginIncluded;
	}

	public Double getSpecialAllowance() {
		return specialAllowance;
	}

	public void setSpecialAllowance(Double specialAllowance) {
		this.specialAllowance = specialAllowance;
	}

	public Double getWorkFromHomeAllowance() {
		return workFromHomeAllowance;
	}

	public void setWorkFromHomeAllowance(Double workFromHomeAllowance) {
		this.workFromHomeAllowance = workFromHomeAllowance;
	}

	public Double getVariablePayAmount() {
		return variablePayAmount;
	}

	public void setVariablePayAmount(Double variablePayAmount) {
		this.variablePayAmount = variablePayAmount;
	}

	
	@Override
	public String toString() {
		return "Payroll [ variablePay=" + variablePayAmount + "]";
	}

	public Double getVoluntaryPayAmount() {
		return voluntaryPayAmount;
	}

	public void setVoluntaryPayAmount(Double voluntaryPayAmount) {
		this.voluntaryPayAmount = voluntaryPayAmount;
	}

	public Boolean getBuPayrollApproval() {
		return buPayrollApproval;
	}

	public void setBuPayrollApproval(Boolean buPayrollApproval) {
		this.buPayrollApproval = buPayrollApproval;
	}

	public String getBuApprovalComment() {
		return buApprovalComment;
	}

	public void setBuApprovalComment(String buApprovalComment) {
		this.buApprovalComment = buApprovalComment;
	}
	
}
