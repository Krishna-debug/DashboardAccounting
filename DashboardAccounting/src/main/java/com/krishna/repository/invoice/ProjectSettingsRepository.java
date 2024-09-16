package com.krishna.repository.invoice;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.invoice.InvoiceProjectSettings;

public interface ProjectSettingsRepository extends JpaRepository<InvoiceProjectSettings, Long>{

	InvoiceProjectSettings findByProjectId(Long projectId);
	
	InvoiceProjectSettings findByLeadId(Long leadId);

}
