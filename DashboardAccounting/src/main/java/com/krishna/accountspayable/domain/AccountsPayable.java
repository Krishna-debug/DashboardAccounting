package com.krishna.accountspayable.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import java.time.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.krishna.accountspayable.enums.PayableTypes;
import com.krishna.accountspayable.enums.TaxType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "accounts_payable")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public @Data class AccountsPayable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Column(name = "creation_date", nullable = false)
	@JsonIgnore
	private LocalDateTime creationDate;
	
	@Column(name = "office_unit", nullable = false)
	private String officeUnit;
	
	@Column(name = "vendor")
	private String vendor;
	
	@Column(name = "account_number")
	private String accountNumber;
	
	@Column(name = "ifsc_code")
	private String ifsCode;
	
	@Column(name = "po")
	private String po;
	
	@Column(name = "contact_number")
	private long contactNumber;
	
	@Column(name = "gstin_number")
	private String gstin;
	
	@OneToOne
	private AccountsHead accountsHead;
	
	@OneToOne
	private HsnCode hsnSacCode;
	
	@Column(name = "tax_percentage")
	private double taxPercentage; 
	
	@Column(name = "invoice_number", nullable = false)
	private String invoiceNumber;
	
	@Column(name = "invoice_amount")
	private double invoiceAmount;
	
	@Column(name = "cgst")
	private double cgst;
	
	@Column(name = "sgst")
	private double sgst;
	
	@Column(name = "igst")
	private double igst;
	
	@Column(name = "status_amount")
	private double statusAmount;
	
	@Column(name = "payment_terms_due_date")
	@JsonIgnore
	private LocalDateTime paymentTermsOrDueDate;
	
	@Column(name = "paid_date")
	@JsonIgnore
	private LocalDateTime paidDate;
	
	@Column(name = "comments", length = 1024)
	private String summaryForItems;
	
	@Column(name = "concerned_person_email")
	private String concernedPersonEmail;
	
	@Column(name = "is_archive")
	private boolean isArchive;
	
	@Column(name = "month", nullable = false)
	private int month;
	
	@Column(name = "year", nullable = false)
	private int year;
	
	@Column(name = "payable_type", nullable=false)
	private PayableTypes payType;
	
	@Column(name ="tds_amount")
	private double tdsAmount;
	
	@Column(name = "tax_type")
	private TaxType taxType;
	
	@OneToOne
	private PayableStatus payableStatus;
}
