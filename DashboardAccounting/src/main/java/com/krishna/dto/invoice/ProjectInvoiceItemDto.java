package com.krishna.dto.invoice;

public class ProjectInvoiceItemDto {

	private Long userId;

	private String userName;

	private Long projectId;

	private Long projectInvoiceId;

	private Double timesheetHours;

	private String grade;

	private Double unitCost;

	private Double totalCost;

	private String unitDescription;

	private Boolean isIfsd;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Long getProjectInvoiceId() {
		return projectInvoiceId;
	}

	public void setProjectInvoiceId(Long projectInvoiceId) {
		this.projectInvoiceId = projectInvoiceId;
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

	public Double getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(Double totalCost) {
		this.totalCost = totalCost;
	}

	public String getUnitDescription() {
		return unitDescription;
	}

	public void setUnitDescription(String unitDescription) {
		this.unitDescription = unitDescription;
	}

	public Boolean getIsIfsd() {
		return isIfsd;
	}

	public void setIsIfsd(Boolean isIfsd) {
		this.isIfsd = isIfsd;
	}

}
