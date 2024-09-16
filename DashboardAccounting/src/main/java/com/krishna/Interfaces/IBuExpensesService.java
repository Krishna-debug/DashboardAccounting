package com.krishna.Interfaces;

import java.util.List;

import org.springframework.stereotype.Service;

import com.krishna.domain.BuExpenses;
import com.krishna.dto.BuExpensesDto;

@Service
public interface IBuExpensesService {

	public List<Object> getExpensesType();

	public BuExpenses addExpenseType(String expenseType);

	public boolean deleteExpenseType(Long id);

	public BuExpenses editExpenseType(BuExpensesDto dto);

}
