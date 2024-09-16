package com.krishna.repository.payroll;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.Bank;

public interface BankRepository extends JpaRepository<Bank, Long>{

	Bank findAllById(long bank);

	List<Bank> findAllByIsArchived(boolean b);

}
