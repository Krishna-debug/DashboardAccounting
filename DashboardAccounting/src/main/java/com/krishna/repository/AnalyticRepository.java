package com.krishna.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.Margin.AnalyticServer;

public interface AnalyticRepository extends JpaRepository<AnalyticServer, Long>{

	AnalyticServer findByMonthAndYearAndBuName(Integer month, Integer year, String buName);

	AnalyticServer findByBuName(String buName);

}
