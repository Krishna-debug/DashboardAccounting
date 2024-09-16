package com.krishna.domain.invoice;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

@Audited
@Table
@Entity
public class ProjectInvoiceItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*ID*/
	private Long id;
	/*User or Resource's Id*/
	private Long userId;
	/*User or Resource's Name*/
	private String userName;
	/*Project Id for invoice*/
	private Long projectId;
	/*Invoice Id for Project*/
	private Long projectInvoiceId;
	/*Actual Hours resource person worked for */
	private Double timesheetHours;
	/*Grade of resource person*/
	private String grade;
	/*Cost per hours of resource*/
	private Double unitCost;
	/*If is Deleted*/
	private Boolean isDeleted;
	/*If is Unit present*/
	private Boolean isUnit;
	/*Description for unit*/
	private String unitDescription;
	/*if IFSD available*/
	private Boolean isIfsd = false;
	
	private Long concernedSplitInvoice;
	/*Adjustment Amount Value*/
	private Double adjustmentAmount=0D;

	public Boolean getIsIfsd() {
		return isIfsd;
	}

	public void setIsIfsd(Boolean isIfsd) {
		this.isIfsd = isIfsd;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Double getTimesheetHours() {
		return timesheetHours;
	}

	public void setTimesheetHours(Double timesheetHours) {
		this.timesheetHours = timesheetHours;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public Double getUnitCost() {
		return unitCost;
	}

	public void setUnitCost(Double unitCost) {
		this.unitCost = unitCost;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public Long getProjectInvoiceId() {
		return projectInvoiceId;
	}

	public void setProjectInvoiceId(Long projectInvoiceId) {
		this.projectInvoiceId = projectInvoiceId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Boolean getIsUnit() {
		return isUnit;
	}

	public void setIsUnit(Boolean isUnit) {
		this.isUnit = isUnit;
	}

	public String getUnitDescription() {
		return unitDescription;
	}

	public void setUnitDescription(String unitDescription) {
		this.unitDescription = unitDescription;
	}
	
	public Long getConcernedSplitInvoice() {
		return concernedSplitInvoice;
	}

	public void setConcernedSplitInvoice(Long concernedSplitInvoice) {
		this.concernedSplitInvoice = concernedSplitInvoice;
	}

	public Double getAdjustmentAmount() {
		return adjustmentAmount;
	}

	public void setAdjustmentAmount(Double adjustmentAmount) {
		this.adjustmentAmount = adjustmentAmount;
	}

	@Override
	public String toString() {
		return "ProjectInvoiceItem [id=" + id + ", userId=" + userId + ", userName=" + userName + ", projectId="
				+ projectId + ", projectInvoiceId=" + projectInvoiceId + ", timesheetHours=" + timesheetHours
				+ ", grade=" + grade + ", unitCost=" + unitCost + ", isDeleted=" + isDeleted + ", isUnit=" + isUnit
				+ ", unitDescription=" + unitDescription + ", isIfsd=" + isIfsd + "]";
	}

}
