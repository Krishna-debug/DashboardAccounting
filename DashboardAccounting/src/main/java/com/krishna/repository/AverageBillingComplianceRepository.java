package com.krishna.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.averagebilling.AverageBillingCompliance;

public interface AverageBillingComplianceRepository extends JpaRepository<AverageBillingCompliance, Long> {
	
	Optional<AverageBillingCompliance> findByProjectIdAndMonthAndYear(long projectId, int month, int year);
}
