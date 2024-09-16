package com.krishna.repository.payroll;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.AttendanceVerification;

public interface AttendanceVerificationRepository extends JpaRepository<AttendanceVerification, Long>{

	AttendanceVerification findAllByUserIdAndMonthAndYear(long userId, int month, int year);

	List<AttendanceVerification> findAllByMonthAndYearAndIsAttendanceVerified(int month, int year, boolean b);

	List<AttendanceVerification> findAllByMonthAndYear(int month, int year);
	
	AttendanceVerification findByUserIdAndMonthAndYear(long userId, int month, int year);

	AttendanceVerification findAllByUserIdAndMonthAndYearAndIsDeletedFalse(long userId, int month, int year);

	List<AttendanceVerification> findAllByUserIdInAndMonthAndYear(List<Long> userId, int month, int year);
	
	

}
