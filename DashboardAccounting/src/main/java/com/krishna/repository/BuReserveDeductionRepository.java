package com.krishna.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.krishna.domain.Margin.BuReserveDeductions;

@Repository
public interface BuReserveDeductionRepository extends JpaRepository<BuReserveDeductions, Long>{

	BuReserveDeductions findByBuName(String buName);

	List<BuReserveDeductions> findAllByMonthAndYearAndBuName(int i, int year, String buName);

	List<BuReserveDeductions> findAllByBuName(String buName);

	List<BuReserveDeductions> findAllByBuNameAndIsDeleted(String buName, boolean b);

	List<BuReserveDeductions> findAllByMonthAndYearAndBuNameAndIsDeleted(int i, int year, String buName, boolean b);

	List<BuReserveDeductions> findAllByBuNameAndIsDeletedAndYear(String buName, boolean b, int year);

	@Query(value="Select * from bu_reserve_deductions where bu_name=:buName and is_deleted=:result and deducted_on between :preDate and :currentDate ",nativeQuery=true)
	List<BuReserveDeductions> findByDeductedOnAndBuNameAndIsDeleted(Date preDate , Date  currentDate, String buName,Boolean result);

	List<BuReserveDeductions> findAllByIsDeleted(boolean b);

	@Query(value="select  * from bu_reserve_deductions where bu_expenses_id=:id",nativeQuery=true)
	List<BuReserveDeductions> findByBuExpensesId(Long id);
}
