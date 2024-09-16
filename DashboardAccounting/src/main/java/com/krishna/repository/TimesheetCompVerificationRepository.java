package com.krishna.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.TimesheetCompVerification;

public interface TimesheetCompVerificationRepository extends JpaRepository<TimesheetCompVerification,Integer>{

	TimesheetCompVerification findAllByUserIdAndMonthAndYear(Long userId, int month, int year);

	TimesheetCompVerification findAllByMonthAndUserIdAndYearAndIsDeletedFalse(int month, Long userId, int year);

	TimesheetCompVerification findAllByUserIdAndMonthAndYearAndIsDeletedFalse(long userId, int month, int year);

	List<TimesheetCompVerification> findAllByMonthAndYearAndIsDeletedFalse(int month, int year);

	List<TimesheetCompVerification> findAllByUserIdAndIsDeletedFalse(long userId);


}
