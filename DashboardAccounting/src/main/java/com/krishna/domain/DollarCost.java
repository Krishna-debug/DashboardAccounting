package com.krishna.domain;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
/**
 * Enitity is used to make the static dollor amount configurable for 
 * accounts service.
 * @author amit
 *
 */
@Entity
public class DollarCost {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	/*ID*/
	private Long id;
	
	@Column(name = "created_by", nullable = false)
	/*Creator's Id*/
	private Long createdBy;
	
	@Column(name = "last_updated_by")
	/*Who updtaed last time*/
	private Long lastUpdatedBy;
	
	@Column(name = "creation_date", nullable = false)
	/*Date of Creation*/
	private LocalDate creationDate;
	
	@Column(name = "last_updated_date")
	/*Date of last updated*/
	private LocalDate lastUpdatedDate;
	
	@Column(name = "dollar_cost", nullable = false)
	/*Value/Amount of Dollar*/
	private Double cost;
	
	@Column(name = "month", nullable = false)
	/*Month for which dollar cost is created*/
	private int month; 
	
	@Column(name = "year", nullable = false)
	/*Year for which dollar cost is created*/
	private int year;
	
	@Column(name="is_deleted")
	/*If entyr is deleted*/
	private boolean isDeleted;

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public Long getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(Long lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public LocalDate getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDate creationDate) {
		this.creationDate = creationDate;
	}

	public LocalDate getLastUpdatedDate() {
		return lastUpdatedDate;
	}

	public void setLastUpdatedDate(LocalDate lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
	}

	public Double getCost() {
		return cost;
	}

	public void setCost(Double cost) {
		this.cost = cost;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "DollarCost [id=" + id + ", createdBy=" + createdBy + ", lastUpdatedBy=" + lastUpdatedBy
				+ ", creationDate=" + creationDate + ", lastUpdatedDate=" + lastUpdatedDate + ", cost=" + cost
				+ ", month=" + month + ", year=" + year + ", isDeleted=" + isDeleted + "]";
	}
}
