package com.krishna.dto.variablePay;

import lombok.Data;

@Data
public class AddYearlyVariablePayDto {
	private int year;
	private double yearlyAmount;
	private Long id;
	private String buName;	
	private Long userId;
	private Long buId;
	private Long payRegisterId;
}