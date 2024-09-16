package com.krishna.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

@Audited
@Entity
@Table(name = "expected_billing_rate")
public class ExpectedBillingRate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*ID*/
	private Long id;
	/*Month for which entry is created*/
	private int month;
	/*Year for which entry is created*/
	private int year;
	/*Billing Rate or dollar rate*/
	private Double billingRate;
	/*Grade of Employee*/
	private String grade;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Double getBillingRate() {
		return billingRate;
	}

	public void setBillingRate(Double billingRate) {
		this.billingRate = billingRate;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

}
