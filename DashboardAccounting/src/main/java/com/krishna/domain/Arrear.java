package com.krishna.domain;

import java.time.LocalDateTime;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;
import org.springframework.lang.NonNull;

import com.krishna.util.DoubleEncryptDecryptConverter;

@Audited
@Entity
@Table(name = "arrear")
public class Arrear {

	/* The Arrear Id */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	/* The User Id */
	private long userId;

	/* The Employee Id */
	@NonNull
	private String employeeId;

	/* The payroll Id */
	private long payrollId;

	/* The Arrear Amount */
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	@NotNull
	private double arrearAmount;

	/* The Arrear Month */
	private int arrearMonth;

	/* The Arrear Year */
	private int year;

	/* The Arrear Comments */
	private String arrearComment;

	/* The Arrear creation Date */
	private LocalDateTime creationDate;

	/* The Arrear creator */
	private long createdBy;

	/* The Arrear modification Date */
	private LocalDateTime modificationDate;

	/* The Arrear modifier */
	private long modifiedBy;

	/* The Arrear Creation Month */
	private int creationMonth;

	/* The Arrear Creation Year */
	private int creationYear;

	/* The Arrear included in payroll */
	private boolean isArrearIncluded;
	
	/* The Reimbursement in payroll */
	private Boolean isReimbursement;
	
	/* The Arrear deleted */
	private Boolean isDeleted=false;

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

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public long getPayrollId() {
		return payrollId;
	}

	public void setPayrollId(long payrollId) {
		this.payrollId = payrollId;
	}

	public double getArrearAmount() {
		return arrearAmount;
	}

	public void setArrearAmount(double arrearAmount) {
		this.arrearAmount = arrearAmount;
	}

	public int getArrearMonth() {
		return arrearMonth;
	}

	public void setArrearMonth(int arrearMonth) {
		this.arrearMonth = arrearMonth;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getArrearComment() {
		return arrearComment;
	}

	public void setArrearComment(String arrearComment) {
		this.arrearComment = arrearComment;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(long createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(LocalDateTime modificationDate) {
		this.modificationDate = modificationDate;
	}

	public long getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(long modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public int getCreationMonth() {
		return creationMonth;
	}

	public void setCreationMonth(int creationMonth) {
		this.creationMonth = creationMonth;
	}

	public int getCreationYear() {
		return creationYear;
	}

	public void setCreationYear(int creationYear) {
		this.creationYear = creationYear;
	}

	public boolean isArrearIncluded() {
		return isArrearIncluded;
	}

	public void setArrearIncluded(boolean isArrearIncluded) {
		this.isArrearIncluded = isArrearIncluded;
	}
	
	public Boolean getIsReimbursement() {
		return isReimbursement;
	}

	public void setIsReimbursement(Boolean isReimbursement) {
		this.isReimbursement = isReimbursement;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	@Override
	public String toString() {
		return "Arrear [id=" + id + ", userId=" + userId + ", employeeId=" + employeeId + ", payrollId=" + payrollId
				+ ", arrearAmount=" + arrearAmount + ", arrearMonth=" + arrearMonth + ", year=" + year
				+ ", arrearComment=" + arrearComment + ", creationDate=" + creationDate + ", createdBy=" + createdBy
				+ ", modificationDate=" + modificationDate + ", modifiedBy=" + modifiedBy + ", creationMonth="
				+ creationMonth + ", creationYear=" + creationYear + ", isArrearIncluded=" + isArrearIncluded + "]";
	}

}
