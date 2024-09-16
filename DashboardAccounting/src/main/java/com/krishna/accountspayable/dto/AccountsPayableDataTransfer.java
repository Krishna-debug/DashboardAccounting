package com.krishna.accountspayable.dto;

import com.krishna.accountspayable.controllers.AccountsPayableController;
import com.krishna.accountspayable.enums.PayableTypes;
import com.krishna.accountspayable.enums.TaxType;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>A data transfer object for accepting request payload in 
 * {@link AccountsPayableController}</p>
 * 
 * @author Amit Mishra
 */
@Data
@NoArgsConstructor
public class AccountsPayableDataTransfer {
	
	private Long creationDate;	
	private String officeUnit;
	private String vendor;
	private String accountNumber; 
	private String ifsCode;
	private String po;
	private long contactNumber;
	private String gstin;
	private long accountsHeadId;
	private long hsnCodeId;
	private double taxPercentage; 
	private String invoiceNumber;
	private double invoiceAmount;
	private double cgst;
	private double sgst;
	private double igst;
	private double statusAmount;
	private Long paymentTermsOrDueDate;
	private Long paidDate;
	private String summaryForItems;
	private String concernedPersonEmail;
	private boolean isArchive;
	private int month;
	private int year;
	private PayableTypes payType;
	private double tdsAmount;
	private TaxType taxType;
	private long payableStatusId;
}
