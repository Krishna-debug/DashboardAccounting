package com.krishna.domain;

import java.util.Date;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.envers.Audited;

import com.krishna.util.DoubleEncryptDecryptConverter;

@Audited
@Entity
public class CostForecasting {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Long projectId;
	private Integer month;
	private Integer year;
	private boolean isArchived;
	private Long userId;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double directCost;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double indirectCost;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double hourlySalary;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double expectedHours;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double resourcingHours;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double monthlyHours;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double forecastedHours;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double forecastedAccountingHours;

	private Date creationDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
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

	public boolean isArchived() {
		return isArchived;
	}

	public void setArchived(boolean isArchived) {
		this.isArchived = isArchived;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Double getHourlySalary() {
		return hourlySalary;
	}

	public void setHourlySalary(Double hourlySalary) {
		this.hourlySalary = hourlySalary;
	}

	public Double getExpectedHours() {
		return expectedHours;
	}

	public void setExpectedHours(Double expectedHours) {
		this.expectedHours = expectedHours;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Double getDirectCost() {
		return directCost;
	}

	public void setDirectCost(Double directCost) {
		this.directCost = directCost;
	}

	public Double getIndirectCost() {
		return indirectCost;
	}

	public void setIndirectCost(Double indirectCost) {
		this.indirectCost = indirectCost;
	}

	public Double getResourcingHours() {
		return resourcingHours;
	}

	public void setResourcingHours(Double resourcingHours) {
		this.resourcingHours = resourcingHours;
	}

	public Double getMonthlyHours() {
		return monthlyHours;
	}

	public void setMonthlyHours(Double monthlyHours) {
		this.monthlyHours = monthlyHours;
	}

	public Double getForecastedHours() {
		return forecastedHours;
	}

	public void setForecastedHours(Double forecastedHours) {
		this.forecastedHours = forecastedHours;
	}

	public Double getForecastedAccountingHours() {
		return forecastedAccountingHours;
	}

	public void setForecastedAccountingHours(Double forecastedAccountingHours) {
		this.forecastedAccountingHours = forecastedAccountingHours;
	}
	
	
	
}
