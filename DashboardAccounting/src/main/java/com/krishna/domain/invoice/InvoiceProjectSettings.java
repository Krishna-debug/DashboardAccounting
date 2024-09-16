package com.krishna.domain.invoice;

import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import lombok.Data;

@Entity
@Table
@Audited
@Data
public class InvoiceProjectSettings {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*ID*/
	private Long id;
	/*Id of the mode of payment*/
	private Long paymentModeId;
	/*Payment Terms ID-> like 1,2,3,7,10 etc*/
	private Long paymentTermsId;
	/*Billing Cycle Id -> Daily, Monthly, weekly etc*/
	private Long billingCycleId;
	/*Address of the client*/
	private String clientAddress;
	/*GST number*/
	private String gstNumber;
	/*Id of Project*/
	private Long projectId;
	/*Name of the company*/
	private String companyName;
	/*Client E-mail Id*/
	private String emailId;
	/*Client toMail id */
	private String toMail;
	/*Client ccMails id */
	@ElementCollection
	private List<String> ccMail;
	
	private Long leadId;
}
