package com.krishna.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table
@Data
public class ReserveSnapShot {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	/*ID*/
	private Long id;
	/*Name of the Business Unit*/
	private String buName;
	/*Month in integer*/	
	private Integer month;
	/*Year in integer*/
	private Integer year;
	/*Amount of a Business Unit month wise*/
	private Double monthlyReserveAmount;
	/*Total Reserve of a business Unit*/
	private Double totalReserve;
	/*Amount deducted from reserve*/
	private Double deductedAmount=0D;
	/*Comment*/
	private String remarks;
	/*If is deleted*/
	private Boolean isDeleted;
	/*Date of creation of snapshot*/
	private Date creationDate;
	/*If is archieved*/
	private boolean isArchived;
	/*Total Margin for reserve*/
	private Double totalMargin;
	/*Total Margin percentage for reserve*/
	private Double totalMarginPerc;
	/*Year to date disputed invoices*/
	private Double ytdDisputed;
	/*Year to date disputed Percentage*/
	private Double ytdDisputedPerc;
	/*Net Margin of Business Unit*/
	private Double netMargin;
	/*Net margin percentage of business unit*/
	private Double netMarginPerc;

	private boolean isChanged;
}
