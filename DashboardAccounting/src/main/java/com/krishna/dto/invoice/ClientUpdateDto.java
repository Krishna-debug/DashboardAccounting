package com.krishna.dto.invoice;

import lombok.Data;

@Data
public class ClientUpdateDto {
	private long id;
	private String billingName;
	private String billingEmail;
	private Long leadId;
}
