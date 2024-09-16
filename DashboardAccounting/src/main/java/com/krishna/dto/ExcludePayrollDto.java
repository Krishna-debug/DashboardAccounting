package com.krishna.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public @Data class ExcludePayrollDto {
	
	private long userId;
	
	private Boolean isMarginIncluded;
	
	public ExcludePayrollDto(Object [] obj) {
		this.userId=Long.parseLong(obj[0].toString());
	    if(obj[1]!=null)
	    	this.isMarginIncluded=Boolean.parseBoolean(obj[1].toString());
	    else
	    	this.isMarginIncluded=null;
	}

}
