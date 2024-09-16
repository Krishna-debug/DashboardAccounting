package com.krishna.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.Margin.ProjectExpectedHours;

public interface ProjectExpectedHoursRepository extends JpaRepository<ProjectExpectedHours, Long>{

	ProjectExpectedHours findByProjectIdAndMonthAndYear(Long projectId, int monthValue, int year);

	List<ProjectExpectedHours> findAllByMonthAndYear(int monthnum, int parseInt);

}
