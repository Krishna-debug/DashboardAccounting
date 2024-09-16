package com.krishna.domain.variablePay;

import java.util.Date;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.krishna.domain.PayRegister;
import com.krishna.util.DoubleEncryptDecryptConverter;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@Table
@Entity
public class VariablePay {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*ID*/
	private Long id;
	/*Month in Int*/
	private int month;
	/*Year in Int*/
	private int year;
	@Convert(converter = DoubleEncryptDecryptConverter.class)
 	@NotNull
 	/*Amount of variable Pay*/
 	private Double amount=0D;
	/*If amount to be included in PayRoll*/
 	private Boolean isIncludeInPayroll=false;
 	/*Name of Business Unit*/
 	private String buName;
 	/*Id of Business Unit*/
 	private Long buId;
 	/*Created By's ID*/
 	private Long createdBy;
 	/*Uploaded By's ID*/
 	private Long updatedBy;
 	/*Date of variable pay amount is updated*/
 	private Date updatedOn;
 	/*Date of variable pay is created on*/
 	private Date createdOn;
 	/*User's Id*/
 	private Long userId;
 	/*Pay Register Id*/
 	private Long payRegisterId;
 	/*If is deleted*/
 	private Boolean isDeleted=false;
}
