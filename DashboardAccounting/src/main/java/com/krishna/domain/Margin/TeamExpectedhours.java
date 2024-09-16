package com.krishna.domain.Margin;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import com.krishna.util.DoubleEncryptDecryptConverter;

@Audited
@Entity
@Table(name = "team_expected_hours")
public class TeamExpectedhours {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*ID*/
	private Long id;
	/*Project ID*/
	private Long projectId;
	/*Month in Integer*/
	private Integer month;
	/*Year in Integer*/	
	private Integer year;
	/*If is Archieved*/
	private boolean isArchived;
	/*User's Id*/
	private Long userId;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	/*Direct Cost of Team*/
	private Double directCost;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	/*Indirect Cost of Team*/
	private Double indirectCost;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	/*Hourly Salary of users*/
	private Double hourlySalary;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	/*Expected Hours for user to work*/
	private Double expectedHours;

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

}
