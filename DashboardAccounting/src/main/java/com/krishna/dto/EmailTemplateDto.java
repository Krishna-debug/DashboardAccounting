package com.krishna.dto;



import org.springframework.stereotype.Component;



@Component
public class EmailTemplateDto {

	private Long id;
	private String templateName;
	private String templateData;
  
    
    public EmailTemplateDto()
	{}
    public EmailTemplateDto(String templateName, String templateData) {
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
	
    
}
