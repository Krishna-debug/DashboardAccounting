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
@Table(name = "project_expected_hours")
public class ProjectExpectedHours {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*ID*/
	private Long id;
	/*Id of Project*/
	private Long projectId;
	/*Month of Project */
	private Integer month;
	/*Year of Project*/
	private Integer year;
	/*If entry is Archieved*/
	private boolean isArchived;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double projectCost;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double invoiceAmountInRupees;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double invoiceAmount;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double indirectCost;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double directCost;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double margin;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double marginPerc;
	
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

	public Double getProjectCost() {
		return projectCost;
	}

	public void setProjectCost(Double projectCost) {
		this.projectCost = projectCost;
	}

	public Double getInvoiceAmountInRupees() {
		return invoiceAmountInRupees;
	}

	public void setInvoiceAmountInRupees(Double invoiceAmountInRupees) {
		this.invoiceAmountInRupees = invoiceAmountInRupees;
	}

	public Double getInvoiceAmount() {
		return invoiceAmount;
	}

	public void setInvoiceAmount(Double invoiceAmount) {
		this.invoiceAmount = invoiceAmount;
	}

	public Double getIndirectCost() {
		return indirectCost;
	}

	public void setIndirectCost(Double indirectCost) {
		this.indirectCost = indirectCost;
	}

	public Double getDirectCost() {
		return directCost;
	}

	public void setDirectCost(Double directCost) {
		this.directCost = directCost;
	}

	public Double getMargin() {
		return margin;
	}

	public void setMargin(Double margin) {
		this.margin = margin;
	}

	public Double getMarginPerc() {
		return marginPerc;
	}

	public void setMarginPerc(Double marginPerc) {
		this.marginPerc = marginPerc;
	}

}
