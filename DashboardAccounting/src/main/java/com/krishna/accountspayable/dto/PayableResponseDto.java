package com.krishna.accountspayable.dto;

import com.krishna.accountspayable.enums.PayableTypes;
import com.krishna.accountspayable.enums.TaxType;
import com.krishna.accountspayable.serviceimpl.AccountsPayableServiceImpl;

import lombok.Data;

/**
 * <p>The class PayableResponseDto is used to format the entity into a representable
 * format. Used in {@link AccountsPayableServiceImpl}</p>
 * @author Amit Mishra
 */

@Data
public class PayableResponseDto {
	
	private long id;
	private Long creationDate;	
	private String officeUnit;
	private String vendor;
	private String accountNumber; 
	private String ifsCode;
	private String po;
	private long contactNumber;
	private String gstin;
	private String accountsHead;
	private String hsnSacCode;
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
	private PayableTypes payableType;
	private String month;
	private int year;
	private double tdsAmount;
	private String payableStatus;
	private TaxType taxType;
}
