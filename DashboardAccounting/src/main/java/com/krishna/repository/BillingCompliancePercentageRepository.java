package com.krishna.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.averagebilling.BillingCompliancePercentage;

public interface BillingCompliancePercentageRepository extends JpaRepository<BillingCompliancePercentage, Long>{
	
	BillingCompliancePercentage findByIsArchive(Boolean isArchived);
}
