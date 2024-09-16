package com.krishna.domain;

import java.util.ArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class ClientModel {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "clientId")
	/*Client's ID*/
	private Long contactId;
	/*Client's Email ID*/
	@Column(name = "email")
	private String email;
	
	public Long getContactId() {
		return contactId;
	}

//	@Column(name = "name")
//	private String name;
	
	@Column(name = "roles")
	private ArrayList<String> roles;
	
	public ClientModel() {

	}

	public ClientModel(String email, /* String name, */ ArrayList<String> roles) {
		super();
		this.email = email;
//		this.name = name;
		this.roles = roles;
	}
	
	public void setContactId(Long contactId) {
		this.contactId = contactId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

//	public String getName() {
//		return name;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}

	public ArrayList<String> getRoles() {
		return roles;
	}

	public void setRoles(ArrayList<String> roles) {
		this.roles = roles;
	}
}
