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
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;
import org.springframework.lang.NonNull;

import com.krishna.enums.PFSubscription;
import com.krishna.enums.PayRegisterStatus;
import com.krishna.util.DoubleEncryptDecryptConverter;

/**
 * To log the revisions in pay of employee
 * 
 * @author shivangi
 * 
 */
@Audited
@Entity
@Table(name = "payRevisions")
public class PayRevisions {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*ID*/
	private long id;

	/*User's Id*/
	private long userId;
	/*User's PAN NUmber*/
	private String panNumber;
	/*USer's UAN Number*/
	private String uan;

	@OneToOne
	/*Bank Object from Bank Table*/
	private Bank bank;
	/*User's Account Number*/
	private String accountNo;
	/*User's IFSC code*/
	private String ifsc;

	@Enumerated(EnumType.STRING)
	/*PF Subscription ENUM-> YES or NO*/
	private PFSubscription pfSubscription;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*Base Salary Amount*/
	private double basicPay;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*House and Rent Allowance Value*/
	private double hra;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*Convanyence Allowance Value*/
	private double conveyance;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*Medical Allowance Amount*/
	private double medicalAllowance;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*Project Allowance Value*/
	private double projectAllowance;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*PF amount paid by employer*/
	private double employerPfContribution;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*Total Monthly Pay Amount*/
	private double totalMonthlyPay;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*Maternity Leave Amount*/
	private double statutoryMaternityPay;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*Annual Cost to Company Value*/
	private double annualCTC;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*Total Annual Cost to Company for a employee*/
	private double totalAnnualCtc;
	/*Start date of changes in salary*/
	private LocalDateTime effectiveFrom;
	/*Date for end date in salary*/
	private LocalDateTime effectiveTo;
	/*Date of changes made*/
	private LocalDateTime updatedOn;
	/*ID of user changes by*/
	private long updatedBy;
	/*Name of User Changes made by*/
	private String updatedByUserName;
	/*Pay Register Status ENUM->COMPLETED or INCOMPLETED*/
	private PayRegisterStatus status;

	@NotNull
	@ManyToOne
	/*Pay Register Object form PayRegidter Table*/
	private PayRegister payRegister;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	/*Laptop Allowance Amount*/
	private double laptopAllowance;
	/*If PAN number is changed*/
	private boolean isPanChanged;
	/*If UAN number is Changes*/
	private boolean isUanChanged;
	/*If Bank Information changed*/
	private boolean isBankChanged;
	/*If account number is changed*/
	private boolean isAccountNumberChanged;
	/*If IFSC code changed*/
	private boolean isIFSCChanged;
	/*If Employee's PF Subscription changed*/
	private boolean isPFSubscriptionChanged;
	/*If Basic Pay is Changed*/
	private boolean isBasicPayChanged;
	/*If House Rent Allowance value is changed*/
	private boolean isHraChanged;
	/*If Conveyance Amount is changed*/
	private boolean isConveyanceChanged;
	/*If a=Medical allowance value is changed*/
	private boolean isMedicalChanged;
	/*IF Project Allowance Amount is changed*/
	private boolean isProjectAllowanceChanged;
	/*If Employer's PF Contribution value is changed*/
	private boolean isEmployerPfContributionChanged;
	/*If Total Monthly Pay Value is changed*/
	private boolean isTotalMonthlyPayChanged;
	/*If Maternity Pay Value  is changed*/
	private boolean isSmpChanged;
	/*If Laptop allowance value is changed*/
	private boolean isLaptopAllowanceChanged;
	/*If Annual CTC is Changed*/
	private boolean isAnnualCtcChanged;
	/*If total Annual CTC value is changed*/
	private boolean isTotalAnnualCtcChanged;
	/*If Start date is changed*/
	private boolean isEffectiveDateChanged;
	/*If is Deleted*/
	private boolean isDeleted;
	/*Deleted By user's User Id*/
	private Long deletedBy;
	/*Comment by user who made changes*/
	private String comment;

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public LocalDateTime getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(LocalDateTime updatedOn) {
		this.updatedOn = updatedOn;
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

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(long updatedBy) {
		this.updatedBy = updatedBy;
	}

	public String getUpdatedByUserName() {
		return updatedByUserName;
	}

	public void setUpdatedByUserName(String updatedByUserName) {
		this.updatedByUserName = updatedByUserName;
	}

	public LocalDateTime getEffectiveFrom() {
		return effectiveFrom;
	}

	public void setEffectiveFrom(LocalDateTime effectiveFrom) {
		this.effectiveFrom = effectiveFrom;
	}

	public LocalDateTime getEffectiveTo() {
		return effectiveTo;
	}

	public void setEffectiveTo(LocalDateTime effectiveTo) {
		this.effectiveTo = effectiveTo;
	}

	public PayRegister getPayRegister() {
		return payRegister;
	}

	public void setPayRegister(PayRegister payRegister) {
		this.payRegister = payRegister;
	}

	public PayRegisterStatus getStatus() {
		return status;
	}

	public void setStatus(PayRegisterStatus status) {
		this.status = status;
	}

	public boolean isPanChanged() {
		return isPanChanged;
	}

	public boolean isUanChanged() {
		return isUanChanged;
	}

	public boolean isBankChanged() {
		return isBankChanged;
	}

	public boolean isAccountNumberChanged() {
		return isAccountNumberChanged;
	}

	public boolean isIFSCChanged() {
		return isIFSCChanged;
	}

	public boolean isPFSubscriptionChanged() {
		return isPFSubscriptionChanged;
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

	public boolean isTotalMonthlyPayChanged() {
		return isTotalMonthlyPayChanged;
	}

	public void setPanChanged(boolean isPanChanged) {
		this.isPanChanged = isPanChanged;
	}

	public void setUanChanged(boolean isUanChanged) {
		this.isUanChanged = isUanChanged;
	}

	public void setBankChanged(boolean isBankChanged) {
		this.isBankChanged = isBankChanged;
	}

	public void setAccountNumberChanged(boolean isAccountNumberChanged) {
		this.isAccountNumberChanged = isAccountNumberChanged;
	}

	public void setIFSCChanged(boolean isIFSCChanged) {
		this.isIFSCChanged = isIFSCChanged;
	}

	public void setPFSubscriptionChanged(boolean isPFSubscriptionChanged) {
		this.isPFSubscriptionChanged = isPFSubscriptionChanged;
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

	public void setTotalMonthlyPayChanged(boolean isTotalMonthlyPayChanged) {
		this.isTotalMonthlyPayChanged = isTotalMonthlyPayChanged;
	}

	public double getStatutoryMaternityPay() {
		return statutoryMaternityPay;
	}

	public void setStatutoryMaternityPay(double statutoryMaternityPay) {
		this.statutoryMaternityPay = statutoryMaternityPay;
	}

	public boolean isSmpChanged() {
		return isSmpChanged;
	}

	public void setSmpChanged(boolean isSmpChanged) {
		this.isSmpChanged = isSmpChanged;
	}

	public double getLaptopAllowance() {
		return laptopAllowance;
	}

	public boolean isLaptopAllowanceChanged() {
		return isLaptopAllowanceChanged;
	}

	public void setLaptopAllowance(double laptopAllowance) {
		this.laptopAllowance = laptopAllowance;
	}

	public void setLaptopAllowanceChanged(boolean isLaptopAllowanceChanged) {
		this.isLaptopAllowanceChanged = isLaptopAllowanceChanged;
	}

	public double getAnnualCTC() {
		return annualCTC;
	}

	public double getTotalAnnualCtc() {
		return totalAnnualCtc;
	}

	public boolean isAnnualCtcChanged() {
		return isAnnualCtcChanged;
	}

	public boolean isTotalAnnualCtcChanged() {
		return isTotalAnnualCtcChanged;
	}

	public void setAnnualCTC(double annualCTC) {
		this.annualCTC = annualCTC;
	}

	public void setTotalAnnualCtc(double totalAnnualCtc) {
		this.totalAnnualCtc = totalAnnualCtc;
	}

	public void setAnnualCtcChanged(boolean isAnnualCtcChanged) {
		this.isAnnualCtcChanged = isAnnualCtcChanged;
	}

	public void setTotalAnnualCtcChanged(boolean isTotalAnnualCtcChanged) {
		this.isTotalAnnualCtcChanged = isTotalAnnualCtcChanged;
	}

	public boolean isEffectiveDateChanged() {
		return isEffectiveDateChanged;
	}

	public void setEffectiveDateChanged(boolean isEffectiveDateChanged) {
		this.isEffectiveDateChanged = isEffectiveDateChanged;
	}

	public Long getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(Long deletedBy) {
		this.deletedBy = deletedBy;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "PayRevisions [id=" + id + ", userId=" + userId + ", panNumber=" + panNumber + ", uan=" + uan + ", bank="
				+ bank + ", accountNo=" + accountNo + ", ifsc=" + ifsc + ", pfSubscription=" + pfSubscription
				+ ", basicPay=" + basicPay + ", hra=" + hra + ", conveyance=" + conveyance + ", medicalAllowance="
				+ medicalAllowance + ", projectAllowance=" + projectAllowance + ", employerPfContribution="
				+ employerPfContribution + ", totalMonthlyPay=" + totalMonthlyPay + ", statutoryMaternityPay="
				+ statutoryMaternityPay + ", effectiveFrom=" + effectiveFrom + ", effectiveTo=" + effectiveTo
				+ ", updatedOn=" + updatedOn + ", updatedBy=" + updatedBy + ", updatedByUserName=" + updatedByUserName
				+ ", status=" + status + ", payRegister=" + payRegister + ", laptopAllowance=" + laptopAllowance
				+ ", isPanChanged=" + isPanChanged + ", isUanChanged=" + isUanChanged + ", isBankChanged="
				+ isBankChanged + ", isAccountNumberChanged=" + isAccountNumberChanged + ", isIFSCChanged="
				+ isIFSCChanged + ", isPFSubscriptionChanged=" + isPFSubscriptionChanged + ", isBasicPayChanged="
				+ isBasicPayChanged + ", isHraChanged=" + isHraChanged + ", isConveyanceChanged=" + isConveyanceChanged
				+ ", isMedicalChanged=" + isMedicalChanged + ", isProjectAllowanceChanged=" + isProjectAllowanceChanged
				+ ", isEmployerPfContributionChanged=" + isEmployerPfContributionChanged + ", isTotalMonthlyPayChanged="
				+ isTotalMonthlyPayChanged + ", isSmpChanged=" + isSmpChanged + ", isLaptopAllowanceChanged="
				+ isLaptopAllowanceChanged + "]";
	}

}
