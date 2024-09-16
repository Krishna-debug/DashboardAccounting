package com.krishna.dto.variablePay;

import lombok.Data;

@Data
public class AddVariablePayDto {
	private int month;
	private int year;
	private double amount;
	private Long id;
	private String buName;	
	private Long userId;
	private Long buId;
	private Long payRegisterId;
}
