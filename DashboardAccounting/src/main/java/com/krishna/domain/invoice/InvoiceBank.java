package com.krishna.domain.invoice;

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
public class InvoiceBank {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*ID*/
	private Long id;
	/* Name of the BanK  */
	private String name;
	/* if bank is archieved  */
	private boolean isArchived;
	/*  Route of payment */
	private String paymentRoute;
	/*Account Number*/
	private String accountNumber;
	/*IFSC code of the bank*/
	private String ifscCode;
	/*Swift Code of the bank*/
	private String swiftCode;
	/*Address of where the bank is located*/
	private String bankAddress;
	
	private String paymentRemittanceDescription;
	/*Purpose of payment*/
	private String paymentPurpose;
	/*Name of the person for whom payment is made*/
	private String beneficiaryName;
	/*Routing number of the bank*/
	private String routingNumber;
	

	public boolean isArchived() {
		return isArchived;
	}

	public void setArchived(boolean isArchived) {
		this.isArchived = isArchived;
	}
}
