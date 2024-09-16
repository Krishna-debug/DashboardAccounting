package com.krishna.domain;

import java.util.Date;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import com.krishna.domain.invoice.InvoiceBank;
import com.krishna.domain.invoice.InvoiceType;
import com.krishna.enums.InvoiceGenerationStatus;
import com.krishna.enums.PayStatus;
import com.krishna.enums.Taxable;
import com.krishna.util.DoubleEncryptDecryptConverterSecurityDeposit;

@Entity
@Audited
public class SecurityDeposit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*ID*/
	private long id;
	
	@NotNull
	/*Lead Id*/
	private long leadId;

	/*Project Id*/
	private long projectId;

	/*Id of th project manager*/
	private long managerId;

	@NotNull
	/*Name of the client*/
	private String clientName;
	
	/*Manager's Name*/
	private String manager;
	
	/*Name of the Project*/
	private String project;
	
	@Convert(converter = DoubleEncryptDecryptConverterSecurityDeposit.class)
	@NotNull
	/*Amount value*/
	private double amount;
	
	@Convert(converter = DoubleEncryptDecryptConverterSecurityDeposit.class)
	@NotNull
	/*Amount to be taxed*/
	private double taxableAmount;
	/*Date of creation*/
	private Date createdDate;
	/*User ID of creator*/
	private long createdBy;
	/*User id of the person updating*/
	private long updatedBy;
	/*Date updated on*/
	private Date updatedOn;

	/*If is Deleted */
	private boolean isDeleted = false;
	/*Recieved on Date*/
	private Date receivedOn;
	/*Amount in Dollar*/
	private double amountInDollar;
	/*Current Type like-> DOLLAR, RUPEES etc*/
	private String currency;
	/*Exchange Rate value*/
	private double exchangeRate;
	/*Tax Deduction at source value, Tax paid by company*/
	private double tdsValue;

	@OneToOne
	/*Object of Bank Table*/
	private InvoiceBank bank;

	@OneToOne
	/*Type of Invoice Object*/
	private InvoiceType invoiceType;

	@Enumerated(EnumType.STRING)
	/*Invoice Generation Status ENUM->PENDING , IN_DRAFT,GENERATED */
	private InvoiceGenerationStatus invoiceGenerationStatus;

	@Enumerated(EnumType.STRING)
	/*If Taxable ENUM-> YES or NO*/
	private Taxable taxable = Taxable.NO;
	/*Country Name */
	private String placeOfSupply;
	/*City's Name*/
	private String cityOfSupply;
	/*Pay Status ENUM-> PAID or UNPAID */
	@Enumerated(EnumType.STRING)
	private PayStatus payStatus;
	/*If is Completed*/
	private boolean isCompleted = false;
	/*Details of Pay*/
	private String payDetails;
	/*If Type is Domestic or International*/
	private String domesticType;

	private Boolean isOthersInUrl = false;
	/*If IFSD Available*/
	private Boolean isIfsd = true;
	
	/*Amount of Payment Charges like paid to third party for transaction*/
	private Double paymentCharges = 0D;
	
	@OneToOne
	@NotAudited
	private BankLocation bankLocation;
	
	/*Comment on IFSD*/
	private String comment;
	
	public long getLeadId() {
		return leadId;
	}

	public void setLeadId(long leadId) {
		this.leadId = leadId;
	}
	
	public double getTaxableAmount() {
		return taxableAmount;
	}

	public void setTaxableAmount(double taxableAmount) {
		this.taxableAmount = taxableAmount;
	}

	public double getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(double exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public double getAmountInDollar() {
		return amountInDollar;
	}

	public void setAmountInDollar(double amountInDollar) {
		this.amountInDollar = amountInDollar;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Date getReceivedOn() {
		return receivedOn;
	}

	public void setReceivedOn(Date receivedOn) {
		this.receivedOn = receivedOn;
	}

	public boolean isIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(boolean isdeleted) {
		this.isDeleted = isdeleted;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getProjectId() {
		return projectId;
	}

	public void setProjectId(long projectId) {
		this.projectId = projectId;
	}

	public long getManagerId() {
		return managerId;
	}

	public void setManagerId(long managerId) {
		this.managerId = managerId;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(long createdBy) {
		this.createdBy = createdBy;
	}

	public long getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(long updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public double getTdsValue() {
		return tdsValue;
	}

	public void setTdsValue(double tdsValue) {
		this.tdsValue = tdsValue;
	}

	public InvoiceBank getBank() {
		return bank;
	}

	public void setBank(InvoiceBank bank) {
		this.bank = bank;
	}

	public InvoiceType getInvoiceType() {
		return invoiceType;
	}

	public void setInvoiceType(InvoiceType invoiceType) {
		this.invoiceType = invoiceType;
	}

	public InvoiceGenerationStatus getInvoiceGenerationStatus() {
		return invoiceGenerationStatus;
	}

	public void setInvoiceGenerationStatus(InvoiceGenerationStatus invoiceGenerationStatus) {
		this.invoiceGenerationStatus = invoiceGenerationStatus;
	}

	public Taxable getTaxable() {
		return taxable;
	}

	public void setTaxable(Taxable taxable) {
		this.taxable = taxable;
	}

	public String getPlaceOfSupply() {
		return placeOfSupply;
	}

	public void setPlaceOfSupply(String placeOfSupply) {
		this.placeOfSupply = placeOfSupply;
	}

	public PayStatus getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(PayStatus payStatus) {
		this.payStatus = payStatus;
	}

	public boolean isCompleted() {
		return isCompleted;
	}

	public void setCompleted(boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

	public String getPayDetails() {
		return payDetails;
	}

	public void setPayDetails(String payDetails) {
		this.payDetails = payDetails;
	}

	public String getDomesticType() {
		return domesticType;
	}

	public void setDomesticType(String domesticType) {
		this.domesticType = domesticType;
	}

	public Boolean getIsOthersInUrl() {
		return isOthersInUrl;
	}

	public void setIsOthersInUrl(Boolean isOthersInUrl) {
		this.isOthersInUrl = isOthersInUrl;
	}

	public Boolean getIsIfsd() {
		return isIfsd;
	}

	public void setIsIfsd(Boolean isIfsd) {
		this.isIfsd = isIfsd;
	}

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public Double getPaymentCharges() {
		return paymentCharges;
	}

	public void setPaymentCharges(Double paymentCharges) {
		this.paymentCharges = paymentCharges;
	}

	public String getCityOfSupply() {
		return cityOfSupply;
	}

	public void setCityOfSupply(String cityOfSupply) {
		this.cityOfSupply = cityOfSupply;
	}

	public BankLocation getBankLocation() {
		return bankLocation;
	}

	public void setBankLocation(BankLocation bankLocation) {
		this.bankLocation = bankLocation;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
}
