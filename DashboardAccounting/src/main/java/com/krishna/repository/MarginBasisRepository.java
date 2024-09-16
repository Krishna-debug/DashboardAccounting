package com.krishna.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.Margin.MarginBasis;

public interface MarginBasisRepository extends JpaRepository<MarginBasis, Long>{

	MarginBasis findByMonthAndYear(int month, int year);

	List<MarginBasis> findByYear(int year);

}
