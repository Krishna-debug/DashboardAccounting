package com.krishna.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.krishna.domain.Margin.OverAllPl;

@Repository
public interface OverAllPlRepo extends JpaRepository<OverAllPl, Long> {

	OverAllPl findByMonthAndYear(int currentMonth, int currentYear);

	List<OverAllPl> findAllByYear(Integer year);

	List<OverAllPl> findAllByYearAndMonthLessThan(Integer year, int i);

}
