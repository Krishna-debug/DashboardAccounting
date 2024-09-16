package com.krishna.dto;

import com.krishna.domain.BuSpecificType;

import lombok.Data;

@Data
public class BuSpecificCostDto {

    private long Id;

    private Integer year;

	private Integer month;

    private Double amount;

    private long typeId;

    private String businessVertical;

    private String comment;

    private boolean deleted;

}
