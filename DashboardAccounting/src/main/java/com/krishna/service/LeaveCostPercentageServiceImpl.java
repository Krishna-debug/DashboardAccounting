package com.krishna.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krishna.Interfaces.LeaveCostPercentageService;
import com.krishna.domain.LeaveCostPercentage;
import com.krishna.domain.PayRegister;
import com.krishna.domain.PayRevisions;
import com.krishna.domain.UserModel;
import com.krishna.repository.payroll.LeaveCostPercentageRepository;
import com.krishna.repository.payroll.PayRegisterRepository;
import com.krishna.repository.payroll.PayRevisionRepository;
import com.krishna.security.JwtValidator;
import com.krishna.util.ConstantUtility;

@Service
public class LeaveCostPercentageServiceImpl implements LeaveCostPercentageService {

	private static final Logger logger = LoggerFactory.getLogger(LeaveCostPercentageServiceImpl.class);
	
	private LeaveCostPercentageRepository leaveCostPercentageRepo;	
	private JwtValidator validator;
	private PayRegisterRepository payRegisterRepository;
	
	@Autowired
	private PayRevisionRepository payrevisionRepository;
	
	//Empty Constructor
	public LeaveCostPercentageServiceImpl() {}
	/**
	 * Constructor Injection
	 * @param leaveCostPercentageRepo
	 * @param validator
	 */
	@Autowired
	public LeaveCostPercentageServiceImpl(LeaveCostPercentageRepository leaveCostPercentageRepo,
			JwtValidator validator, ObjectMapper mapper,PayRegisterRepository payRegisterRepository) {
		this.leaveCostPercentageRepo = leaveCostPercentageRepo;
		this.validator = validator;
		this.payRegisterRepository=payRegisterRepository;
	}

	@Override
	public Map<String, Object> saveLeaveCostPercentage(Double leaveCostPercentage, String accessToken) {
		List<LeaveCostPercentage> prevCosts = leaveCostPercentageRepo.findAllByIsDeleted(false);
		if(!prevCosts.isEmpty()) {
			prevCosts.forEach(prevCost->{
				prevCost.setDeleted(true);
				prevCost = leaveCostPercentageRepo.save(prevCost);
			});
		}
		UserModel currentUser = getUser(accessToken);
		LeaveCostPercentage entity = new LeaveCostPercentage();
		entity.setLeaveCostPercentage(leaveCostPercentage);
		entity.setCreatedBy(currentUser.getUserId());
		entity.setCreationDate(LocalDate.now());
		entity.setDeleted(false);
		entity = leaveCostPercentageRepo.save(entity);
		setPayregisterData(leaveCostPercentage);
		return generateResponse(entity);
	}
	
	private void setPayregisterData(Double leaveCostPercentage) {
		List<PayRegister> payRegisters = payRegisterRepository.findAllByIsCurrent(true);
		payRegisters.forEach(payRegister -> {
			double annualCtc = payRegister.getTotalMonthlyPay() * 12;
			double monthlysalaryExcLA = payRegister.getTotalMonthlyPay() - payRegister.getLaptopAllowance();
			double paidLeavesAmount = (leaveCostPercentage * (monthlysalaryExcLA*12)) / 100;
			payRegister.setAnnualCTC(annualCtc);
			payRegister.setTotalAnnualCtc(annualCtc + paidLeavesAmount);
			payRegister.setPaidLeavesAmount(paidLeavesAmount);
			payRegister = payRegisterRepository.saveAndFlush(payRegister);
		});
		List<PayRevisions> payRevisions=payrevisionRepository.findAll();
		if (!payRevisions.isEmpty()) {
			payRevisions.forEach(payrevision -> {
				double annualPayrevctc = payrevision.getTotalMonthlyPay() * 12;
				double monthlysalaryExcLA = payrevision.getTotalMonthlyPay() - payrevision.getLaptopAllowance();
				double paidLeavesPayrevAmount = (leaveCostPercentage * (monthlysalaryExcLA*12)) / 100;
				payrevision.setAnnualCTC(annualPayrevctc);
				payrevision.setTotalAnnualCtc(annualPayrevctc + paidLeavesPayrevAmount);
				payrevision = payrevisionRepository.saveAndFlush(payrevision);
				PayRegister payee=payRegisterRepository.findAllById(payrevision.getPayRegister().getId());
				payee.setAnnualCTC(annualPayrevctc);
				payee.setTotalAnnualCtc(annualPayrevctc + paidLeavesPayrevAmount);
				payee.setPaidLeavesAmount(paidLeavesPayrevAmount);
				payee = payRegisterRepository.saveAndFlush(payee);
			});
		}
	}

	@Override
	public Map<String, Object> updateLeaveCostPercentage(Double leaveCostPercentage, String accessToken, long id) {
		Optional<LeaveCostPercentage> entity = leaveCostPercentageRepo.findById(id);
		LocalDate today = LocalDate.now();
		UserModel currentUser = getUser(accessToken);
		
		if(entity.isPresent()) {
			entity.get().setLeaveCostPercentage(leaveCostPercentage);
			entity.get().setLastUpdated(today);
			entity.get().setLastUpdatedBy(currentUser.getUserId());
			leaveCostPercentageRepo.saveAndFlush(entity.get());
			
			return generateResponse(entity.get());
		}
		return new HashMap<>();
	}

	@Override
	public boolean deleteLeaveCostPercentage(long id, String accessToken) {
		boolean deleted = false;
		LocalDate today = LocalDate.now();
		
		Optional<LeaveCostPercentage> entity = leaveCostPercentageRepo.findById(id);
		if(entity.isPresent()) {
			UserModel currentUser = getUser(accessToken);
			entity.get().setDeleted(true);
			entity.get().setLastUpdated(today);
			entity.get().setLastUpdatedBy(currentUser.getUserId());
			leaveCostPercentageRepo.saveAndFlush(entity.get());
			deleted = true;
		}
		logger.info(":::::::Is Deleted : {}", deleted);
		return deleted;
	}

	@Override
	public List<Map<String, Object>> getAllLeaveCostPercentage() {
		List<LeaveCostPercentage> leaveCostPercentages = leaveCostPercentageRepo.findAllByIsDeleted(true);
		ArrayList<Map<String, Object>> response =null;
		
		if(leaveCostPercentages!=null)
			response = generateResponse(leaveCostPercentages);
		else
			response = new ArrayList<>();
		
		return response;
	}
	
	private UserModel getUser(String accessToken) {
		return validator.tokenbValidate(accessToken);
	}
	
	private Map<String, Object> generateResponse(LeaveCostPercentage entity) {
		Map<String, Object> returnValue = new HashMap<>();
		returnValue.put(ConstantUtility.LEAVE_COST_PERCENTAGE, entity.getLeaveCostPercentage());
		returnValue.put("id", entity.getId());
		return returnValue;
	}
	
	private ArrayList<Map<String, Object>> generateResponse(List<LeaveCostPercentage> leaveCostPercentages) {
		ArrayList<Map<String, Object>> returnValue = new ArrayList<>();
		leaveCostPercentages.forEach( entity ->{
			Map<String, Object> detail = new HashMap<>();
			detail.put(ConstantUtility.LEAVE_COST_PERCENTAGE, entity.getLeaveCostPercentage());
			detail.put("id", entity.getId());
			detail.put("createdOn", Timestamp.valueOf(entity.getCreationDate().atStartOfDay()));
			returnValue.add(detail);
		});
		return returnValue;
	}
	
	@Override
	public Map<String, Object> getCurrentLeaveCostPercent() {
		List<LeaveCostPercentage> leaveCosts=leaveCostPercentageRepo.findAllByIsDeleted(false);
		Map<String, Object> detail = new HashMap<>();
		if(!leaveCosts.isEmpty()) {
			LeaveCostPercentage leaveCost=leaveCosts.get(0);
			detail.put(ConstantUtility.LEAVE_COST_PERCENTAGE, leaveCost.getLeaveCostPercentage());
			detail.put("id", leaveCost.getId());
			detail.put("createdOn", Timestamp.valueOf(leaveCost.getCreationDate().atStartOfDay()));
		}
		return detail;
	}
}
