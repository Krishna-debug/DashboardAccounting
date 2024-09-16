package com.krishna.dto;

public class AccountsCompliantStatusChangeDto {

	Long projectId;
	String comment;
	String issueType;
	Boolean compliantStatus;
	String callFrom;
	String expectedBilling;
	String actualBilling;
	String differencePerc;
	String content;
	
	
	public String getExpectedBilling() {
		return expectedBilling;
	}
	public void setExpectedBilling(String expectedBilling) {
		this.expectedBilling = expectedBilling;
	}
	public String getActualBilling() {
		return actualBilling;
	}
	public void setActualBilling(String actualBilling) {
		this.actualBilling = actualBilling;
	}
	public String getDifferencePerc() {
		return differencePerc;
	}
	public void setDifferencePerc(String differencePerc) {
		this.differencePerc = differencePerc;
	}
	public Long getProjectId() {
		return projectId;
	}
	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getIssueType() {
		return issueType;
	}
	public void setIssueType(String issueType) {
		this.issueType = issueType;
	}
	public Boolean getCompliantStatus() {
		return compliantStatus;
	}
	public void setCompliantStatus(Boolean compliantStatus) {
		this.compliantStatus = compliantStatus;
	}
	
	public String getCallFrom() {
		return callFrom;
	}
	public void setCallFrom(String callFrom) {
		this.callFrom = callFrom;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

}
