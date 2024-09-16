package com.krishna.repository.variablePay;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.krishna.domain.variablePay.VariablePay;

@Repository
public interface VariablePayRepository extends JpaRepository<VariablePay, Long> {

	List<VariablePay> findAllByIsDeletedFalseAndYearAndUserId(int year, Long userId);

	VariablePay findByUserIdAndMonthAndYearAndIsDeletedFalse(long userId, int month, int year);

	VariablePay findByMonthAndYearAndUserIdAndIsDeletedFalse(int month, int year, long userId);

	VariablePay findByIdAndIsDeletedFalse(Long id);

	List<VariablePay> findAllByMonthAndYearAndIsDeleted(int month, int year, boolean b);

}
