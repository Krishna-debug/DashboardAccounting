package com.krishna.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

/*
 * @Author
 * Harikesh 26/12/2018
 * 
 */
@Entity
@Data
public class Templatecc {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	/*ID*/
	private Long id;
	/*Version number*/	
	private long version;
	/*Mail id fro cc*/
	private String ccMail;
}
