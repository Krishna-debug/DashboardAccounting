package com.krishna.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.krishna.domain.Margin.BuReserve;

public interface BuReserveRepository extends JpaRepository<BuReserve, Long>{

	List<BuReserve> findAllByYearAndBuName(int year,String buName);

	BuReserve findAllByYearAndBuNameAndMonth(int year, String buName, int month);

	List<BuReserve> findAllByBuName(String buName);

	@Query(value= "SELECT * from bu_reserve WHERE month=:month AND year=:year" , nativeQuery=true)
	List<BuReserve> findAllByMonthAndYear(int month, int year);

}
