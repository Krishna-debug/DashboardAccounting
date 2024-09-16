package com.krishna.repository.variablePay;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.variablePay.YearlyVariablePay;

public interface YearlyVariablePayRepository extends JpaRepository<YearlyVariablePay, Long> {

	YearlyVariablePay findByUserIdAndYearAndIsDeletedFalse(Long userId, int year);

	YearlyVariablePay findByIdAndIsDeletedFalse(Long id);

}
