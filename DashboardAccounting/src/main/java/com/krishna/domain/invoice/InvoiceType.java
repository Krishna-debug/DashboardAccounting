package com.krishna.domain.invoice;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import lombok.Getter;
import lombok.Setter;

@Audited
@Entity
@Table
@Getter @Setter
public class InvoiceType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*ID*/
	private Long id;
	/*Name of Invoice Type-> like General and Interim*/
	private String name;
	/*If is Archieved*/
	private boolean isArchived;


}
