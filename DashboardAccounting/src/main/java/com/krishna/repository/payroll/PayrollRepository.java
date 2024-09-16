package com.krishna.repository.payroll;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.krishna.domain.Payroll;
import com.krishna.domain.TimesheetCompVerification;
import com.krishna.dto.ExcludePayrollDto;
import com.krishna.enums.PayRollStatus;

public interface PayrollRepository extends JpaRepository<Payroll, Long>{

	@Cacheable("findByMonthAndUserIdPayroll")
	Payroll findByMonthAndUserId(int month, long userId);

	@Cacheable("findAllByIdPayroll")
	Payroll findAllById(long payrollId);

	@Cacheable("findAllByMonthAndYearPayroll")
	List<Payroll> findAllByMonthAndYear(int month, int year);

	@Cacheable("findByMonthAndUserIdAndYearPayroll")
	Payroll findByMonthAndUserIdAndYear(int month, long userId, int year);

	@Cacheable("findAllByMonthAndUserIdAndYearPayroll")
	Payroll findAllByMonthAndUserIdAndYear(int month, long userId, int year);

	@Cacheable("findAllByMonthAndYearAndPayRollStatusAndIsVerifiedPayroll")
	List<Payroll> findAllByMonthAndYearAndPayRollStatusAndIsVerified(int month, int year, PayRollStatus verified,
			boolean b);

	@Cacheable("findAllByMonthAndYearAndPayRollStatusPayroll")
	List<Payroll> findAllByMonthAndYearAndPayRollStatus(int month, int year, PayRollStatus verified);

	@Cacheable("findAllByMonthAndYearAndIsAttendanceVerifiedPayroll")
	List<Payroll> findAllByMonthAndYearAndIsAttendanceVerified(int month, int year, boolean b);

	@Cacheable("findAllByMonthAndUserIdAndYearAndIsDeletedPayroll")
	Payroll findAllByMonthAndUserIdAndYearAndIsDeleted(int month, long userId, int year, boolean b);

	@Cacheable("findAllByMonthAndYearAndIsDeletedFalsePayroll")
	List<Payroll> findAllByMonthAndYearAndIsDeletedFalse(int month, int year);

	@Cacheable("findAllByMonthAndYearAndIsDeletedFalseAndIsPriorityPayroll")
	List<Payroll> findAllByMonthAndYearAndIsDeletedFalseAndIsPriority(int month, int year, boolean b);
	
	@Cacheable("findAllByUserIdAndMonthAndYearPayroll")
	Payroll findAllByUserIdAndMonthAndYear(Long userId,int month, int year);
	
	@Cacheable("findAllByMonthAndUserIdAndYearAndIsDeletedFalsePayroll")
	Payroll findAllByMonthAndUserIdAndYearAndIsDeletedFalse(int month, long userId, int year);

	@Cacheable("findAllByMonthAndYearAndIsDeletedPayroll")
	List<Payroll> findAllByMonthAndYearAndIsDeleted(int month, int year, boolean b);

	@CacheEvict(value = {"findAllByMonthAndYearAndIsDeletedPayroll","findAllByMonthAndUserIdAndYearAndIsDeletedFalsePayroll",
			"findAllByUserIdAndMonthAndYearPayroll","findAllByMonthAndYearAndIsDeletedFalseAndIsPriorityPayroll","findAllByMonthAndYearAndIsDeletedFalsePayroll","findAllByMonthAndUserIdAndYearAndIsDeletedPayroll",
			"findAllByMonthAndYearAndIsAttendanceVerifiedPayroll","findAllByMonthAndYearAndPayRollStatusPayroll", "findAllByMonthAndYearAndPayRollStatusAndIsVerifiedPayroll","findAllByMonthAndUserIdAndYearPayroll",
			"findByMonthAndUserIdAndYearPayrollPayroll","findAllByMonthAndYearPayroll","findByMonthAndUserIdPayroll","findAllByIdPayroll"}, allEntries = true)
	Payroll saveAndFlush(TimesheetCompVerification payroll);

//	@Cacheable("findAllByMonthAndYearAndIsDeletedAndIsMarginIncludedFalse")
//	List<Payroll> findAllByMonthAndYearAndIsDeletedAndIsMarginIncludedFalse(int month, int year, boolean b);
	
	@Override
	@CacheEvict(value = {"findAllByMonthAndYearAndIsDeletedPayroll","findAllByMonthAndUserIdAndYearAndIsDeletedFalsePayroll",
			"findAllByUserIdAndMonthAndYearPayroll","findAllByMonthAndYearAndIsDeletedFalseAndIsPriorityPayroll","findAllByMonthAndYearAndIsDeletedFalsePayroll","findAllByMonthAndUserIdAndYearAndIsDeletedPayroll",
			"findAllByMonthAndYearAndIsAttendanceVerifiedPayroll","findAllByMonthAndYearAndPayRollStatusPayroll", "findAllByMonthAndYearAndPayRollStatusAndIsVerifiedPayroll","findAllByMonthAndUserIdAndYearPayroll",
			"findByMonthAndUserIdAndYearPayrollPayroll","findAllByMonthAndYearPayroll","findByMonthAndUserIdPayroll","findAllByIdPayroll"}, allEntries = true)
    <S extends Payroll> S save(S entity);
	
	@Override
	@CacheEvict(value = {"findAllByMonthAndYearAndIsDeletedPayroll","findAllByMonthAndUserIdAndYearAndIsDeletedFalsePayroll",
			"findAllByUserIdAndMonthAndYearPayroll","findAllByMonthAndYearAndIsDeletedFalseAndIsPriorityPayroll","findAllByMonthAndYearAndIsDeletedFalsePayroll","findAllByMonthAndUserIdAndYearAndIsDeletedPayroll",
			"findAllByMonthAndYearAndIsAttendanceVerifiedPayroll","findAllByMonthAndYearAndPayRollStatusPayroll", "findAllByMonthAndYearAndPayRollStatusAndIsVerifiedPayroll","findAllByMonthAndUserIdAndYearPayroll",
			"findByMonthAndUserIdAndYearPayrollPayroll","findAllByMonthAndYearPayroll","findByMonthAndUserIdPayroll","findAllByIdPayroll"}, allEntries = true)
    <S extends Payroll> S saveAndFlush(S entity);
	
	@Override
	@CacheEvict(value = {"findAllByMonthAndYearAndIsDeletedPayroll","findAllByMonthAndUserIdAndYearAndIsDeletedFalsePayroll",
			"findAllByUserIdAndMonthAndYearPayroll","findAllByMonthAndYearAndIsDeletedFalseAndIsPriorityPayroll","findAllByMonthAndYearAndIsDeletedFalsePayroll","findAllByMonthAndUserIdAndYearAndIsDeletedPayroll",
			"findAllByMonthAndYearAndIsAttendanceVerifiedPayroll","findAllByMonthAndYearAndPayRollStatusPayroll", "findAllByMonthAndYearAndPayRollStatusAndIsVerifiedPayroll","findAllByMonthAndUserIdAndYearPayroll",
			"findByMonthAndUserIdAndYearPayrollPayroll","findAllByMonthAndYearPayroll","findByMonthAndUserIdPayroll","findAllByIdPayroll"}, allEntries = true)
	<S extends Payroll> List<S> saveAll(Iterable<S> entities);
	
//	@Cacheable("findAllExcludedPayrolls")
//	@Query(value="select user_id,is_margin_included from pay_roll where is_margin_included=:b and month=:month and year=:year and is_deleted=false",nativeQuery=true)
//	List<Object[]> findAllExcludedPayrolls(int month, int year, boolean b);

	List<Payroll> findAllByUserIdInAndMonthAndYearAndIsDeletedFalseAndIsPriority(List<Long> userId, int month, int year, boolean b);
	
	List<Payroll> findAllByUserIdInAndMonthAndYearAndIsDeletedFalse(List<Long> userId, int month, int year);

}
