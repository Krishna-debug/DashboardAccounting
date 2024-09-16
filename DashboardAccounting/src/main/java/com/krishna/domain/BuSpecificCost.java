package com.krishna.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;


import javax.persistence.Id;

import lombok.Data;

@Audited
@Data
@Entity
public class BuSpecificCost {
    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	/*ID*/
	private Long id;

	private Integer year;

	@NotNull
	/*Month in Integer like -> 1(JANUARY),2(FEBUARY) etc*/
	private Integer month;

    private Double amount;

    @OneToOne
    private BuSpecificType type;

    private String businessVertical;

    private String comment;
	

	@Column(name = "is_deleted")
	/*If is deleted*/
	private boolean deleted;
	/*Created By User's Id*/
	Long createdBy;
	/*Date of creation*/
	Date createOn;
	/*Modified By user's Id*/
	Long lastModifiedBy;
	/*Date of Last Modification Date*/
	Date LastModifiedOn;
}
