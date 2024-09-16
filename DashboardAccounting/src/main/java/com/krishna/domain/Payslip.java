package com.krishna.domain;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.springframework.lang.NonNull;

import com.krishna.enums.PayslipStatus;

@Audited
@Entity
@Table(name = "payslip")
public class Payslip {

	/* The payslip Id */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	/* The User Id */
	private long userId;

	/*Id of Payroll*/
	private long payrollId;
	/*Date of Modification of Payslip*/
	private LocalDateTime modificationDate;
	/*Date of Generation of Payslip*/
	private LocalDateTime generationDate;
	/*Month of Payslip*/
	private int payslipMonth;
	/*Year of Payslip*/
	private int payslipYear;

	@Enumerated(EnumType.STRING)
	/*Status of Payslip-> SAVE,GENERATED,SEND */
	private PayslipStatus payslipStatus;
	/*user id of paslip generator*/
	private long generatedBy;
	/*User Id of Payslip Modifier*/
	private long modifiedBy;
	/*If Payslip is updated*/
	private boolean isUpdated;
	/*If is Archieved*/
	private boolean isArchived;

	public long getId() {
		return id;
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

	public long getPayrollId() {
		return payrollId;
	}

	public void setPayrollId(long payrollId) {
		this.payrollId = payrollId;
	}

	public LocalDateTime getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(LocalDateTime modificationDate) {
		this.modificationDate = modificationDate;
	}

	public LocalDateTime getGenerationDate() {
		return generationDate;
	}

	public void setGenerationDate(LocalDateTime generationDate) {
		this.generationDate = generationDate;
	}

	public int getPayslipMonth() {
		return payslipMonth;
	}

	public void setPayslipMonth(int payslipMonth) {
		this.payslipMonth = payslipMonth;
	}

	public int getPayslipYear() {
		return payslipYear;
	}

	public void setPayslipYear(int payslipYear) {
		this.payslipYear = payslipYear;
	}

	public PayslipStatus getPayslipStatus() {
		return payslipStatus;
	}

	public void setPayslipStatus(PayslipStatus payslipStatus) {
		this.payslipStatus = payslipStatus;
	}

	public long getGeneratedBy() {
		return generatedBy;
	}

	public void setGeneratedBy(long generatedBy) {
		this.generatedBy = generatedBy;
	}

	public long getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(long modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public boolean isUpdated() {
		return isUpdated;
	}

	public void setUpdated(boolean isUpdated) {
		this.isUpdated = isUpdated;
	}

	public boolean isArchived() {
		return isArchived;
	}

	public void setArchive(boolean isArchive) {
		this.isArchived = isArchive;
	}
	
	@Override
	public String toString() {
		return "Payslip [id=" + id + ", userId=" + userId + ", payrollId=" + payrollId + ", modificationDate="
				+ modificationDate + ", generationDate=" + generationDate + ", payslipMonth=" + payslipMonth
				+ ", payslipYear=" + payslipYear + ", payslipStatus=" + payslipStatus + ", generatedBy=" + generatedBy
				+ ", modifiedBy=" + modifiedBy + "]";
	}

}
