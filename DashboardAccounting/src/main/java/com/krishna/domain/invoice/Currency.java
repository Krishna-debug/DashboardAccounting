package com.krishna.domain.invoice;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class Currency {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*Id */
	private Long id;
	/* Type of currency  */
	private String currencyName;
	

}
