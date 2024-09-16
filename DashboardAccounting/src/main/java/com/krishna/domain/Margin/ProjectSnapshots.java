package com.krishna.domain.Margin;

import java.util.Date;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.krishna.util.DoubleEncryptDecryptConverter;

@Entity
@Table(name = "project_snapshots")
public class ProjectSnapshots {
		
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		/*ID*/
		private Long id;
		/*Project's ID*/
		private Long projectId;
		/*Date of creation of Project*/
		private Date creationDate;
		/*If is Archieved*/
		private boolean isArchived;
		
		@Convert(converter = DoubleEncryptDecryptConverter.class)
		/*Cost for Project*/
		private Double projectCost;
		
		@Convert(converter = DoubleEncryptDecryptConverter.class)
		/*Amount of invoice in rupee*/
		private Double invoiceAmountInRupees;
		
		@Convert(converter = DoubleEncryptDecryptConverter.class)
		/*Invoice Amount*/
		private Double invoiceAmount;
		
		@Convert(converter = DoubleEncryptDecryptConverter.class)
		/*Indirect Cost Value*/
		private Double indirectCost;
		
		@Convert(converter = DoubleEncryptDecryptConverter.class)
		/*Direct Cost value*/
		private Double directCost;
		
		@Convert(converter = DoubleEncryptDecryptConverter.class)
		/*BU's Margin Amount*/
		private Double margin;
		
		@Convert(converter = DoubleEncryptDecryptConverter.class)
		/*BU's Margin Percentage*/
		private Double marginPerc;
		/*Month in Integer*/
		private Integer month;
		/*Year in Integer*/
		private Integer year;
		/*If is Changed in snapshot*/
		private boolean isChanged;
		
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

		public Date getCreationDate() {
			return creationDate;
		}

		public void setCreationDate(Date creationDate) {
			this.creationDate = creationDate;
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

		public Integer getYear() {
			return year;
		}

		public void setYear(Integer year) {
			this.year = year;
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

		public Integer getMonth() {
			return month;
		}

		public void setMonth(Integer month) {
			this.month = month;
		}

		public boolean isChanged() {
			return isChanged;
		}

		public void setChanged(boolean isChanged) {
			this.isChanged = isChanged;
		}

}
