package com.krishna.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.Margin.TeamExpectedhours;

public interface UserExpectedHoursRepository extends JpaRepository<TeamExpectedhours, Long>{

	TeamExpectedhours findByProjectIdAndMonthAndYearAndUserId(Long projectId, int month, int year, Long userId);
	List<TeamExpectedhours> findAllByProjectIdAndMonthAndYear(Long projectId, int month, int year);


}
