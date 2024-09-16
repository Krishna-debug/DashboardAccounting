package com.krishna.domain;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The Domain LeaveCostPercentage.
 * @author amit
 *
 */
@Entity
@Table(name = "leave_cost_percentage")
public class LeaveCostPercentage {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*ID*/
	private long id;
	
	@Column(name = "leave_cost_percentage", nullable = false)
	/*Leave Cost Percentage Value*/
	private double leaveCostPercentage;
	
	@Column(name = "creation_date", nullable = false)
	/*Date of Creation*/
	private LocalDate creationDate;
	
	@Column(name = "createdBy", nullable = false)
	/*Creator's User Id*/
	private long createdBy;
	
	@Column(name = "last_updated")
	/*Date of Last updated*/
	private LocalDate lastUpdated;
	
	@Column(name = "last_updated_by")
	/*Last Updated By user's ID*/
	private long lastUpdatedBy;
	
	@Column(name = "is_deleted", nullable = false)
	/*If is Deleted*/
	private boolean isDeleted;
	
	public double getLeaveCostPercentage() {
		return leaveCostPercentage;
	}
	public void setLeaveCostPercentage(double leaveCostPercentage) {
		this.leaveCostPercentage = leaveCostPercentage;
	}
	public LocalDate getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(LocalDate creationDate) {
		this.creationDate = creationDate;
	}
	public long getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(long createdBy) {
		this.createdBy = createdBy;
	}
	public LocalDate getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(LocalDate lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	public long getLastUpdatedBy() {
		return lastUpdatedBy;
	}
	public void setLastUpdatedBy(long lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}
	public boolean isDeleted() {
		return isDeleted;
	}
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	public long getId() {
		return id;
	}
	
}
