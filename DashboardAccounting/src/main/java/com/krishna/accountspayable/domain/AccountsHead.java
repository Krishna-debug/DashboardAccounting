package com.krishna.accountspayable.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity(name  = "accounts_payable_accounts_head")
public @Data class AccountsHead {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Column(name = "accounts_head", nullable = false)
	private String type;
	
	private boolean isArchive;
	
}
