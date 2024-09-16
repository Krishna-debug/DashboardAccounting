package com.krishna.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PayrollPaidUnpaidDto {
	
	/* The pay Days */
	private Double payDays;
	
	/* The unpaid Days */
	private Double unpaidDays;

	
	private Long userId;
}

