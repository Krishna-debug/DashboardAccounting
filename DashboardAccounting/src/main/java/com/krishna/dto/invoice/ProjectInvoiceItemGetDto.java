package com.krishna.dto.invoice;

public class ProjectInvoiceItemGetDto {

	private Long id;

	private Long userId;

	private String userName;

	private Long projectId;

	private Long projectInvoiceId;

	private double timesheetHours;

	private String grade;

	private double unitCost;

	private Double totalCost;

	private Double totalCostDollar;
	
	private String unitDescription;
	
	private Double adjustmentAmount=0D;

	private Long concernedSplitInvoice;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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

	public Double getTotalCostDollar() {
		return totalCostDollar;
	}

	public void setTotalCostDollar(Double totalCostDollar) {
		this.totalCostDollar = totalCostDollar;
	}

	public String getUnitDescription() {
		return unitDescription;
	}

	public void setUnitDescription(String unitDescription) {
		this.unitDescription = unitDescription;
	}

	public Double getAdjustmentAmount() {
		return adjustmentAmount;
	}

	public void setAdjustmentAmount(Double adjustmentAmount) {
		this.adjustmentAmount = adjustmentAmount;
	}

	public Long getConcernedSplitInvoice() {
		return concernedSplitInvoice;
	}

	public void setConcernedSplitInvoice(Long concernedSplitInvoice) {
		this.concernedSplitInvoice = concernedSplitInvoice;
	}
	
}
