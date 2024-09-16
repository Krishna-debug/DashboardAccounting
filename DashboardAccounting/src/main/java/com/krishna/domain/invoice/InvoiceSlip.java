package com.krishna.domain.invoice;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import lombok.Data;

@Audited
@Entity
@Table
@Data
public class InvoiceSlip {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*ID*/
	private Long id;
	/*Path of the invoice slip*/
	private String filePath;
	/*ID of Who uploaded invoice*/
	private Long uploadedBy;
	/*Name of th file*/
	private String fileName;
	/*If file is deleted*/
	private boolean isDeleted = false;
	/*Date of upload*/
	private LocalDateTime uploadedDate;
	/*Who uploaded file*/
	private String fileUploaderName;
	/*Invoice ID*/
	private Long invoiceId;
	/*IFSC code of bank*/
	private boolean isIfsd;

	

	public boolean isDeleted() {
		return isDeleted;
	}

	
	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}


	public boolean isIfsd() {
		return isIfsd;
	}


	public void setIfsd(boolean isIfsd) {
		this.isIfsd = isIfsd;
	}
	
}
