package com.krishna.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public @Data class GradeBasedIndirectCost {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*ID*/
	private long id;
	/*Grade of Employee*/
	private String grade;
	/*Fixed Cost, Fixed Amount*/
	private Double fixedCost;
	/*Month for which entry is created*/
	private int month;
	/*Year for which entry is created*/
	private int year;
	/*If is Variable Pay*/
	private boolean isVariable;
}
