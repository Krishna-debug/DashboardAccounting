package com.krishna.domain.Margin;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.krishna.domain.BuExpenses;

@Entity
@Table
@Audited
public class BuReserveDeductions {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	/*ID*/
	private Long id;
	/*Business unit Name*/
	private String buName;
	/*Amount deducted*/
	private Double deductedAmount;
	/*Comment by bu head*/
	private String remarks;
	/*Date for Deduction to perform*/
	private Date deductionDate;
	/*date of deduction creation*/
	private Date deductedOn;
	/*User's Id performing deduction*/
	private Long deductedBy;
	/*Name of the user performing deduction*/
	private String deductedByName;
	/*Amount in BU's previous rReserve*/
	private Double previousReserve;
	/*Amount available in Reserve*/
	private Double availableReserve;
	/*Month of Entry */
	private Integer month;
	/*Year of Entry*/
	private Integer year;
	/*If is Deleted*/
	private Boolean isDeleted;

	@OneToOne
	private BuExpenses buExpenses;
	
	public BuExpenses getBuExpenses() {
		return buExpenses;
	}

	public void setBuExpenses(BuExpenses buExpenses) {
		this.buExpenses = buExpenses;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBuName() {
		return buName;
	}

	public void setBuName(String buName) {
		this.buName = buName;
	}

	public Double getDeductedAmount() {
		return deductedAmount;
	}

	public void setDeductedAmount(Double deductedAmount) {
		this.deductedAmount = deductedAmount;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Date getDeductedOn() {
		return deductedOn;
	}

	public void setDeductedOn(Date deductedOn) {
		this.deductedOn = deductedOn;
	}

	public Long getDeductedBy() {
		return deductedBy;
	}

	public void setDeductedBy(Long deductedBy) {
		this.deductedBy = deductedBy;
	}

	public String getDeductedByName() {
		return deductedByName;
	}

	public void setDeductedByName(String deductedByName) {
		this.deductedByName = deductedByName;
	}

	public Double getPreviousReserve() {
		return previousReserve;
	}

	public void setPreviousReserve(Double previousReserve) {
		this.previousReserve = previousReserve;
	}

	public Double getAvailableReserve() {
		return availableReserve;
	}

	public void setAvailableReserve(Double availableReserve) {
		this.availableReserve = availableReserve;
	}

	public Date getDeductionDate() {
		return deductionDate;
	}

	public void setDeductionDate(Date deductionDate) {
		this.deductionDate = deductionDate;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
}
