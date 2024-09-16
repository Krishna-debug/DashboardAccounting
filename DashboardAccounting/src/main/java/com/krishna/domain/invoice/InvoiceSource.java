package com.krishna.domain.invoice;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import lombok.Data;

@Entity
@Table
@Audited
@Data
public class InvoiceSource {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*ID*/
	private Long id;
	/*Name of the company*/
	private String companyName;
	/*If source of invoice is Archieved*/
	private boolean isArchived;
	/*GST number of company for whom invoice is generated*/
	private String gstNumber;
	/*PAN number of the company for whom invoice is generated*/
	private String companyPAN;
	/*Address of the company for whom invoice is generated*/
	private String address;

	
	public boolean isArchived() {
		return isArchived;
	}

	public void setArchived(boolean isArchived) {
		this.isArchived = isArchived;
	}

}
