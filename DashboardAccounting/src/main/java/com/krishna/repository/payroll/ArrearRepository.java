package com.krishna.repository.payroll;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.Arrear;

public interface ArrearRepository extends JpaRepository<Arrear, Long>{

	List<Arrear> findByPayrollIdAndArrearMonthAndYearAndIsDeleted(long id, int month, int year, Boolean b);

	Arrear findAllById(long arrearId);

	List<Arrear> findAllByCreationMonthAndCreationYearAndIsDeleted(int month, int year, Boolean b);

	List<Arrear> findAllByCreationMonthAndIsDeleted(int month, Boolean b);

	List<Arrear> findAllByCreationYearAndIsDeleted(int year, Boolean b);

	List<Arrear> findByPayrollIdAndCreationMonthAndCreationYearAndIsDeleted(long payrollId, int month, int year, Boolean b);

	List<Arrear> findByPayrollIdAndCreationMonthAndCreationYearAndIsArrearIncludedAndIsDeleted(long id, int month, int year,
			boolean b, Boolean c);

	List<Arrear> findByPayrollIdAndCreationMonthAndCreationYearAndIsReimbursementAndIsDeleted(long id, int creationMonth,
			int creationYear, boolean isReimbursement, Boolean b);

	List<Arrear> findAllByCreationMonthAndCreationYearAndIsReimbursementAndIsDeleted(int month, int year, Boolean b, Boolean c);

	List<Arrear> findAllByCreationYearAndIsReimbursementAndIsDeleted(int year, boolean isReimbursement, Boolean b);

	List<Arrear> findByPayrollIdAndCreationMonthAndCreationYearAndIsArrearIncludedAndIsReimbursementAndIsDeleted(long id, int month,
			int year, boolean b, boolean c, Boolean d);

	List<Arrear> findAllByCreationMonthAndCreationYearAndIsReimbursementAndIsArrearIncludedAndIsDeleted(
			int month, int year, boolean b, boolean c, Boolean d);

	List<Arrear> findByCreationMonthAndCreationYearAndIsArrearIncludedAndIsDeleted(int month, int year, boolean b,
			boolean c);

	List<Arrear> findAllByCreationYearAndIsReimbursementAndIsArrearIncludedAndIsDeleted(int year, boolean b,
			boolean c, boolean d);

	List<Arrear> findByUserIdInAndCreationMonthAndCreationYearAndIsArrearIncludedAndIsDeleted(List<Long> userId, int month, int year, boolean b,
			boolean c);
}
