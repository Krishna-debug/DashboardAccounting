package com.krishna.domain;

public class ProjectTrendsDTO {
	private String projectName;
	private Double providedMonthTotalAmmount;
	private Double previousMonthTotalAmmount;
	private Double trends;
	private String businessVertical;
	private Long projectId;
	private Double trendAmount;
	private String expectedHours;
	
	public String getExpectedHours() {
		return expectedHours;
	}
	public void setExpectedHours(String expectedHours) {
		this.expectedHours = expectedHours;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public double getProvidedMonthTotalAmmount() {
		return providedMonthTotalAmmount;
	}
	public void setProvidedMonthTotalAmmount(double providedMonthTotalAmmount) {
		this.providedMonthTotalAmmount = providedMonthTotalAmmount;
	}
	public double getPreviousMonthTotalAmmount() {
		return previousMonthTotalAmmount;
	}
	public void setPreviousMonthTotalAmmount(double previousMonthTotalAmmount) {
		this.previousMonthTotalAmmount = previousMonthTotalAmmount;
	}
	public double getTrends() {
		return trends;
	}
	public void setTrends(double trends) {
		this.trends = trends;
	}
	

	public void setProvidedMonthTotalAmmount(Double providedMonthTotalAmmount) {
		this.providedMonthTotalAmmount = providedMonthTotalAmmount;
	}
	public void setPreviousMonthTotalAmmount(Double previousMonthTotalAmmount) {
		this.previousMonthTotalAmmount = previousMonthTotalAmmount;
	}
	public void setTrends(Double trends) {
		this.trends = trends;
	}
	
	public String getBusinessVertical() {
		return businessVertical;
	}
	public void setBusinessVertical(String businessVertical) {
		this.businessVertical = businessVertical;
	}
	public Long getProjectId() {
		return projectId;
	}
	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}
	public Double getTrendAmount() {
		return trendAmount;
	}
	public void setTrendAmount(Double trendAmount) {
		this.trendAmount = trendAmount;
	}
	@Override
	public String toString() {
		return "ProjectTrendsDTO [projectName=" + projectName + ", providedMonthTotalAmmount="
				+ providedMonthTotalAmmount + ", previousMonthTotalAmmount=" + previousMonthTotalAmmount + ", trends="
				+ trends +", businessVertical= "+businessVertical+"]";
	}
	
	

}
