package com.krishna.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/*
 * @author Pankaj Garg
 * 
 * This is Entity class for bank location.
 * */
@Entity 
@Data
public class BankLocation {

	/*Id-> Primary Key*/
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	/*Location name of the bank*/
	private String location;
	/*if the bank location is deleted*/
	private boolean isDeleted;
}
