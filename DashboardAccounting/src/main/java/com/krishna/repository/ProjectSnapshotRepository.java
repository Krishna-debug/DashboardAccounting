package com.krishna.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.Margin.ProjectSnapshots;

public interface ProjectSnapshotRepository extends JpaRepository<ProjectSnapshots, Long>{

	List<ProjectSnapshots> findByProjectIdAndMonthAndYear(Long projectId, int monthValue, int year);

	List<ProjectSnapshots> findAllByCreationDate(Date date);

	List<ProjectSnapshots> findAllByProjectIdAndMonthAndYearAndCreationDate(Long projectId, int monthValue, int year,
			Date date);

	List<ProjectSnapshots> findAllByMonthAndYearAndCreationDate(int month, int year, Date dateToFind);

}
