package com.krishna.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import org.hibernate.envers.Audited;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
@Audited
public class BuSpecificType {

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

    private String typeName;

    private Boolean isDeleted=false;

   

}
