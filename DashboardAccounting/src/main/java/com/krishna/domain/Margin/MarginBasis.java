package com.krishna.domain.Margin;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class MarginBasis {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	/*ID*/
	private Long id;
	/*if margin is based on grade of employee*/
	private Boolean isGradeWise;
	/*If margin is  Uniformed*/
	private Boolean isUniform;
	/*Month in Integer*/
	private Integer month;
	/*Year in INteger*/
	private Integer year;
	/*if net margin is based on lTM  */
	private Boolean isLTM = false;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getIsGradeWise() {
		return isGradeWise;
	}

	public void setIsGradeWise(Boolean isGradeWise) {
		this.isGradeWise = isGradeWise;
	}

	public Boolean getIsUniform() {
		return isUniform;
	}

	public void setIsUniform(Boolean isUniform) {
		this.isUniform = isUniform;
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

	public Boolean getIsLTM() {
		return isLTM;
	}

	public void setIsLTM(Boolean isLTM) {
		this.isLTM = isLTM;
	}

}
