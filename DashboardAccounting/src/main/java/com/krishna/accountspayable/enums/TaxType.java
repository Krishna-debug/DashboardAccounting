package com.krishna.accountspayable.enums;

import com.krishna.accountspayable.domain.AccountsPayable;

/**
 * Enum Tax type. Used in {@link AccountsPayable} for determining the tax type
 * for calculating cgst, igst, sgst in AccountsPayable.
 * 
 * @author Amit Mishra
 */
public enum TaxType {
	
	CENTRAL_STATE("central_state"), INTRA("intra"), NONE("None");
	
	private String value;
	
	TaxType(String value){
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
}
