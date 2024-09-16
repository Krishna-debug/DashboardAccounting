package com.krishna.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.parser.Part.IgnoreCaseType;
import org.springframework.stereotype.Service;

import com.krishna.Interfaces.IBuExpensesService;
import com.krishna.domain.BuExpenses;
import com.krishna.domain.Margin.BuReserveDeductions;
import com.krishna.dto.BuExpensesDto;
import com.krishna.repository.BUExpensesRepo;
import com.krishna.repository.BuReserveDeductionRepository;

@Service
public class BuExpensesService implements IBuExpensesService{

	@Autowired
	BUExpensesRepo buExpensesRepo;
	
	
	@Autowired
	BuReserveDeductionRepository buReserveDeductionRepository;
	
	@Override
	public List<Object> getExpensesType() {
		List<BuExpenses> data = buExpensesRepo.findAllByIsDeletedFalse();
		List<Object> result = new ArrayList<>();
		if(data!=null) {
			for(BuExpenses buExpenses : data) {
				HashMap<String, Object> res = new HashMap<>();
				res.put("id", buExpenses.getId());
				res.put("expenseType",buExpenses.getExpenseType());
				result.add(res);
			}
		}
		return result;
	}

	@Override
	public BuExpenses addExpenseType(String expenseType) {
		BuExpenses buExpenseExists = buExpensesRepo.findByExpenseType(expenseType);
		if (buExpenseExists== null) {
			BuExpenses buExpense = new BuExpenses();
			buExpense.setExpenseType(expenseType);	
			BuExpenses result = buExpensesRepo.save(buExpense);
			return result;
		}
		return null;
	}


	@Override
	public boolean deleteExpenseType(Long id) {
		Optional<BuExpenses> buExpenseData = buExpensesRepo.findById(id);
		if (buExpenseData.isPresent()) {
			List<BuReserveDeductions> deductions = buReserveDeductionRepository.findByBuExpensesId(id);
			if (deductions.isEmpty()) {
				BuExpenses bu = buExpenseData.get();
				bu.setDeleted(true);
				buExpensesRepo.save(bu);
			}
			return true;
		} else
			return false;
	}


	@Override
	public BuExpenses editExpenseType(BuExpensesDto dto) {
		Optional<BuExpenses> buExpenseData = buExpensesRepo.findById(dto.getId());
		if(buExpenseData.isPresent()){
			BuExpenses bu = buExpenseData.get();
			bu.setExpenseType(dto.getExpenseType());
			try {
				buExpensesRepo.save(bu);
			}catch(Exception e) {
				return null;
			}
			return bu;
		}
		return null;
	}

}
