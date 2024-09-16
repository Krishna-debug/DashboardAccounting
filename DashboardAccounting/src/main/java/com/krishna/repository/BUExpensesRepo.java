package com.krishna.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.krishna.domain.BuExpenses;

@Repository
public interface BUExpensesRepo extends JpaRepository<BuExpenses,Long>{

	public BuExpenses findByExpenseType(String expenseType);

	public List<BuExpenses> findAllByIsDeletedFalse();
	
	public Optional<BuExpenses> findById(Long id);
}
