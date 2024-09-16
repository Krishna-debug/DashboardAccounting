package com.krishna.domain.variablePay;

import java.util.Date;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.krishna.util.DoubleEncryptDecryptConverter;

import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@Table
@Entity
public class YearlyVariablePay {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*ID*/
	private Long id;
	/*Year of Calculation of Variable Pay*/
	private int year;
	@Convert(converter = DoubleEncryptDecryptConverter.class)
 	@NotNull
 	/*Amount of Variable pay in a year*/
 	private Double yearlyAmount=0D;
	/*Name of the Business Unit for variable*/
	private String buName;
	/*Business Unit ID*/
	private Long buId;
	/*Created By user's ID*/
	private Long createdBy;
	/*Updated by User's ID*/
	private Long updatedBy;
	/*Date to be updated on*/
	private Date updatedOn;
	/*Date to be created on*/
	private Date createdOn;
	/*User's ID*/
	private Long userId;
	/*PayRegister ID*/
	private Long payRegisterId;
	/*If is deleted*/
	private Boolean isDeleted=false;
}