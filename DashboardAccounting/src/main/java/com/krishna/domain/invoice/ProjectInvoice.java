package com.krishna.domain.invoice;

import java.util.Date;

import java.time.LocalDateTime;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.data.annotation.CreatedDate;

import com.krishna.domain.BankLocation;
import com.krishna.enums.InvoiceGenerationStatus;
import com.krishna.enums.PayStatus;
import com.krishna.enums.Taxable;
import com.krishna.util.DoubleEncryptDecryptConverter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Audited
@Entity
@Getter @Setter @ToString
public class ProjectInvoice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//Id 
	private Long id;

//	@NotBlank@NotEmpty
	//Name of the project
	private String project;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
//	@NotNull
	//Amount of the invoice
	private double amount;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
//	@NotNull
	//Valyue of amount in Dollar
	private double amountInDollar;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
//	@NotNull
	//Taxable amount in Dollar
	private double taxableAmountInDollar;

	@NotBlank
	@NotEmpty
	//Type of Currency
	private String currency;

	@NotNull
	//Type of invoice cycle's id
	private Long invoiceCycleId;

	@NotNull
	//Type of mode of Payment's Id
	private Long modeOfPaymentId;

	@NotNull
	//Type of Payment term's Id
	private Long paymentTermsId;

//	@NotNull
	//Date of creation of invoice
	private Date billingDate;

	@NotNull
	//Due date of invoice
	private Date dueDate;
	
	//Is Deleted Invoice
	private boolean isDeleted = false;

	@NotNull
	//Type of Invoice's Status
	private Long invoiceStatus;

//	@NotBlank @NotEmpty
	//Month
	private String month;

//	@NotBlank @NotEmpty
	//Year
	private String year;

	//Comment on invoice
	private String comment;

//	@NotBlank@NotEmpty
	//Name of the Manager of the project
	private String manager;

//	@NotBlank@NotEmpty
	//Name of the client
	private String clientName;

	@CreatedDate
	@Temporal(TemporalType.TIMESTAMP)
	//Date created on 
	private Date createdDate;

	@NotNull
	//Exchange rate from one currency to another
	private double exchangeRate;

	//Id of the creator
	private long creatorId;

	//Payment received on
	private Date receivedOn;

	//Date invoice marked disputed
	private LocalDateTime disputedDate;

	@Convert(converter = DoubleEncryptDecryptConverter.class)
//	@NotNull
	//Amount of invoice in rupees
	double amountInRupee;

	//Project's Id
	private Long projectId;

	//Manager's Id
	private long managerId;

	//TDS value amount
	private double tdsValue;

	//Comment by BU's head
	private String buHeadComment;

	//If the invoice is Internal 
	private Boolean isInternal;

	//If internal invoice, raised from BU's name
	private Long raisedFromBu;

	//If Internal, raised for BU's name
	private Long raisedToBu;

	@OneToOne
	//Name of the Bank for Invoice
	private InvoiceBank bank;

	@OneToOne
	//Type of Invoice-> Domestic/International
	private InvoiceType invoiceType;

	//Start of Date of billing
	private Date fromDate;

	//End date of Billing
	private Date toDate;

	@Enumerated(EnumType.STRING)
	//Status of Invoice-> Generated/Pending/In_Draft
	private InvoiceGenerationStatus invoiceGenerationStatus;

	@Enumerated(EnumType.STRING)
	private Taxable taxable = Taxable.NO;

	//Country's name
	private String placeOfSupply;
	
	//City's Name
	private String cityOfSupply;


	@Enumerated(EnumType.STRING)
	//If Paid or unpaid
	private PayStatus payStatus;

	//if transaction completed
	private boolean isCompleted = false;

	//Details of the Payment 
	private String payDetails;

	//Comment if invoice is disputed
	private String disputedAutomatedComment;

	//If invoice is interstate or intrastate
	private String domesticType;

	private Boolean isOthersInUrl = false;

	private Boolean isMilestone = false;

	private Boolean isIfsd = false;

	//This is the extra charge which client pay,for transaction
	private Double paymentCharges = 0D;
	
	//If imported items to be included
	private Boolean importItems=false;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	//Amount to be adjusted from security deposit
	private Double adjustmantAmount=0D;
	
	//Id of Security deposite
	private Long securityDepositeId;
	
	//Date security deposit adjusted on 
	private Date adjustmentDate;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	//Central Goods and Service tax
	private Double cgst=0D;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	//State Goods and Service tax
	private Double sgst=0D;
	
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	//Integrated Goods and Service tax
	private Double igst=0D;
	
	//Is Split type General/Split
	private String splitType;
	//Id of concerned Split Type
	private Long concernedSplitInvoice;
	
	private Boolean isIfsdAdjustment=false;
	
	private Boolean isKycComplaint;

//	private long bankLocationId;
	@OneToOne
	@NotAudited
	private BankLocation bankLocation;

	private Double waivedOffAmount=0D;
	
	private String payingEntityName;
	
	private String currencyRecevied;
	
	private Double recievedAmount=0D;

	//Comment if the thank-you email is not sent.
	private String skipComment;
	
	//if Mail Sent SuccessFully

	private Long projectSettingId;
	

	private boolean isThanksMail = false; 
	private boolean isDollarCurrency=false;
}
