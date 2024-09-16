package com.krishna.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.krishna.domain.EmailTemplate;

public interface EmailTemplateRepository extends CrudRepository<EmailTemplate, Long> {

	 
	public List<EmailTemplate> findAllByIsDeleted(boolean isdelete);
	
	public EmailTemplate findOneByIdAndIsDeleted(Long id, boolean isdelete);
	
//	public EmailTemplate findOneByTemplateNameAndIsDeleted(String name, boolean isdelete);

	public EmailTemplate findAllById(Long id);

	public EmailTemplate findAllByTemplateNameAndIsDeleted(String templatename, boolean b);
}
