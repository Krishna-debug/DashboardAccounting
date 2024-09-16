package com.krishna.domain.Margin;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.envers.Audited;

import com.krishna.util.DoubleEncryptDecryptConverter;

import lombok.Data;

@Entity
@Data
@Audited
public class OverAllPl {

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Long id;
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double effectiveRevenueDollar;
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double effectiveDollarValue;
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double effectiveRevenue;
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double paymentCharges;
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double infraCost;  
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double variableCost;          
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double reimbursementAndBonusCost; 
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double buSpecificCost;         
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double variablePay;   
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double voluntaryPay;         
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double totalIndirectCost;     
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double totalSalary;           
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double totalSalaryBuffer;      
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double salaryDifference;      
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double totalCost;             
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double totalProfitAndLoss;     
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double totalProfitAndLossBuWise; 
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double totalMargin;         
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double netMargin;          
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double ytdDisputed;           
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double ltmDisputed;
	@Convert(converter = DoubleEncryptDecryptConverter.class)
	private Double netProfitAndLoss;    
	private Integer month;
	private Integer year;

}