package com.krishna.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.NoArgsConstructor;

@Entity
@Table(name = "email_templates")
@NoArgsConstructor
public class EmailTemplate {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	/*ID*/
	private Long id;
	/*Name of the Tempelate*/
	private String templateName;

	@Column(columnDefinition = "TEXT", length = 3000)
	/*Templelate Data or format*/
	private String templateData;

	@Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
	/*If Tempelate is Deleted*/
	private boolean isDeleted = false;


	public EmailTemplate(String templateName, String templateData) {
		super();
		this.templateName = templateName;
		this.templateData = templateData;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getTemplateData() {
		return templateData;
	}

	public void setTemplateData(String templateData) {
		this.templateData = templateData;
	}

	@Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
}
