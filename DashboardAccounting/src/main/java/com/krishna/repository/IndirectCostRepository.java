package com.krishna.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.IndirectCost;
import com.krishna.enums.Months;

public interface IndirectCostRepository extends JpaRepository<IndirectCost, Long> {
	public Optional<IndirectCost> findById(Long id);
	
	public List<IndirectCost> findByYearAndIsDeleted(String year, boolean deleted);
	
	public Optional<IndirectCost> findByMonthAndYearAndIsDeleted(Months month, String year, boolean deleted);
	
	public List<IndirectCost> findByIsDeleted(boolean value);

	public IndirectCost findByYearAndIsDeletedAndMonth(String year, boolean b, Months monthObj);

//	public IndirectCost findByYearAndMonthAndIsDeleted(String year, Months monthObj, boolean b);
}