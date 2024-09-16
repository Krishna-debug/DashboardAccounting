package com.krishna.domain.invoice;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
public class PaymentTerms {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*ID*/
	private Long id;
	
	@NotNull@NotEmpty
	/*Payment Term Type like-> weekly,biweekly, monthly etc*/
	private String paymentTermsType;
	
	/*If is Archieved*/
	private boolean isArchived=false;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPaymentTermsType() {
		return paymentTermsType;
	}

	public void setPaymentTermsType(String paymentTermsType) {
		this.paymentTermsType = paymentTermsType;
	}

	public boolean isArchived() {
		return isArchived;
	}

	public void setArchived(boolean isArchived) {
		this.isArchived = isArchived;
	}

}
