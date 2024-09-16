package com.krishna.accountspayable.serviceimpl;

import java.sql.Timestamp;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import java.time.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krishna.accountspayable.domain.AccountsHead;
import com.krishna.accountspayable.domain.AccountsPayable;
import com.krishna.accountspayable.domain.HsnCode;
import com.krishna.accountspayable.domain.PayableStatus;
import com.krishna.accountspayable.dto.AccountsPayableDataTransfer;
import com.krishna.accountspayable.dto.PayableResponseDto;
import com.krishna.accountspayable.enums.PayableTypes;
import com.krishna.accountspayable.enums.TaxType;
import com.krishna.accountspayable.repository.AccountsHeadRepository;
import com.krishna.accountspayable.repository.AccountsPayableRepository;
import com.krishna.accountspayable.repository.HsnCodeRepository;
import com.krishna.accountspayable.repository.PayableStatusRepository;
import com.krishna.accountspayable.services.AccountsPayableService;

@Service
public class AccountsPayableServiceImpl implements AccountsPayableService {
	
	private static final Logger logger = LoggerFactory.getLogger(AccountsPayableServiceImpl.class);
	
	@Autowired private AccountsPayableRepository accountsPayableRepository;
	@Autowired private AccountsHeadRepository accountHeadRepository;
	@Autowired private HsnCodeRepository hsnCodeRepository;
	@Autowired private ObjectMapper mapper;
	@Autowired private PayableStatusRepository payableStatusRepository;
	
	@Override
	public PayableResponseDto saveAccountsPayableData(AccountsPayableDataTransfer data) {
		AccountsPayable entity = mapper.convertValue(data, AccountsPayable.class);
		HsnCode code = hsnCodeRepository.findById(data.getHsnCodeId());
		Optional<AccountsHead> accountHead = accountHeadRepository.findById(data.getAccountsHeadId());
		if(code != null && accountHead.isPresent()) {
			entity.setAccountsHead(accountHead.get());
			entity.setHsnSacCode(code);
			entity.setCreationDate(new Timestamp(data.getCreationDate()).toLocalDateTime());
			entity.setPaidDate(new Timestamp(data.getPaidDate()).toLocalDateTime());
			entity.setPaymentTermsOrDueDate(new Timestamp(data.getPaymentTermsOrDueDate()).toLocalDateTime());
			entity.setMonth(data.getMonth() + 1);
			entity.setStatusAmount(this.calculateStatusAmount(data));
			entity.setPayableStatus(this.getPayableStatus(data.getPayableStatusId()));
			return this.createResponse(accountsPayableRepository.save(entity));
		}
		return null;
	}

	private PayableStatus getPayableStatus(long id) {
		return payableStatusRepository.findById(id);
	}

	private double calculateStatusAmount(AccountsPayableDataTransfer requestPayload) {
		double invoiceAmount = requestPayload.getInvoiceAmount();
		double sgst = requestPayload.getSgst();
		double igst = requestPayload.getIgst();
		double cgst = requestPayload.getCgst();
		double totalAmount = invoiceAmount + cgst + igst + sgst;
		return Math.round((totalAmount -requestPayload.getTdsAmount()) * 100.00) 
				/ 100.00;
	}

	@Override
	public List<PayableResponseDto> getAllAccountsPayableData(int month, int year, Long hsnCodeId) {
		List<PayableResponseDto> response = new ArrayList<>();
		List<AccountsPayable> accountsPayableList = null;
		if(hsnCodeId == null) {
			accountsPayableList = accountsPayableRepository.findByisArchiveAndMonthAndYear(false, month, year);
		} else {
			HsnCode code = hsnCodeRepository.findById(hsnCodeId.longValue());
			accountsPayableList = accountsPayableRepository.
					findByisArchiveAndMonthAndYearAndHsnSacCode(false, month, year, code);
		}
		for(AccountsPayable payable : accountsPayableList) {
			PayableResponseDto payableResponseDto = this.createResponse(payable);
			response.add(payableResponseDto);
		}
		return response;
	}

	@Override
	public PayableResponseDto updateAccountsPayableData(long id, AccountsPayableDataTransfer updatedData) {
		Optional<AccountsPayable> accountsPayable = accountsPayableRepository.findById(id);
		if(accountsPayable.isPresent()) {
			AccountsPayable updatedEntity = mapper.convertValue(updatedData, AccountsPayable.class);
			HsnCode code = hsnCodeRepository.findById(updatedData.getHsnCodeId());
			Optional<AccountsHead> accountHead = accountHeadRepository.findById(updatedData.getAccountsHeadId());
			if(code != null && accountHead.isPresent()) {
				updatedEntity.setId(accountsPayable.get().getId());
				updatedEntity.setAccountsHead(accountHead.get());
				updatedEntity.setHsnSacCode(code);
				updatedEntity.setCreationDate(new Timestamp(updatedData.getCreationDate()).toLocalDateTime());
				updatedEntity.setPaidDate(new Timestamp(updatedData.getPaidDate()).toLocalDateTime());
				updatedEntity.setPaymentTermsOrDueDate(new Timestamp(updatedData.getPaymentTermsOrDueDate()).toLocalDateTime());
				updatedEntity.setMonth(updatedData.getMonth() + 1);
				updatedEntity.setStatusAmount(this.calculateStatusAmount(updatedData));
				updatedEntity.setPayableStatus(this.getPayableStatus(updatedData.getPayableStatusId()));
				return this.createResponse(accountsPayableRepository.saveAndFlush(updatedEntity));
			}
		}
		return null;
	}

	@Override
	public AccountsPayable deleteAccountsPayableData(long id) {
		Optional<AccountsPayable> entity = accountsPayableRepository.findById(id);
		if(entity.isPresent()) {
			entity.get().setArchive(true);
			return accountsPayableRepository.save(entity.get());
		}
		logger.info("There is no entry for %d id", id);
		return null;
	}
	
	private PayableResponseDto createResponse(AccountsPayable payable){
		PayableResponseDto payableResponseDto = new PayableResponseDto();
		BeanUtils.copyProperties(payable, payableResponseDto);
		payableResponseDto.setCreationDate(Timestamp.valueOf(payable.getCreationDate()).getTime());
		payableResponseDto.setAccountsHead(payable.getAccountsHead().getType());
		payableResponseDto.setHsnSacCode(payable.getHsnSacCode().getHsnCode());
		payableResponseDto.setPaymentTermsOrDueDate(Timestamp.valueOf(payable.getPaymentTermsOrDueDate()).getTime());
		payableResponseDto.setPaidDate(Timestamp.valueOf(payable.getPaidDate()).getTime());
		payableResponseDto.setMonth(this.getMonthName(payable.getMonth()));
		payableResponseDto.setPayableType(payable.getPayType());
		payableResponseDto.setPayableStatus(payable.getPayableStatus().getPayableStatus());
		return payableResponseDto;
	}
	
	private String getMonthName(int month) {
	    return new DateFormatSymbols().getMonths()[month-1];
	}

	@Override
	public List<PayableTypes> getAllPayableTypes() {
		return Arrays.asList(PayableTypes.values());
	}

//	@Scheduled(cron = "* * * 1 * *")
	public void getLastMonthRecurringPayables() {
		logger.info("Executing Cron to carry forward payables data");
		LocalDate now = LocalDate.now();
		int month = Month.of(now.getMonthValue()).minus(2).getValue();
		int year = YearMonth.of(now.getYear(), now.getMonth()).minusMonths(2).getYear();
		List<AccountsPayable> payables = accountsPayableRepository.findAllByIsArchiveAndMonthAndYearAndPayType(false, month, year, 
				PayableTypes.RECURRING);
		if(payables != null) {
			for(AccountsPayable payable : payables) {
				AccountsPayable recurringPayable = mapper.convertValue(payable, AccountsPayable.class);
				recurringPayable.setMonth(payable.getMonth() +1);
				recurringPayable.setId(0);
				accountsPayableRepository.save(recurringPayable);
			}
		}
	}

	@Override
	public List<Map<String, Object>> isRecurringpayableTransferred(int month, int year) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TaxType> getAllPayableTaxTypes() {
		return Arrays.asList(TaxType.values());
	}

	@Override
	public Map<String, Object> calculateTaxesOfGivenType(TaxType taxType, double invoiceAmount, double taxPercentage) {
		Map<String, Object> response = new HashMap<>();
		double cgst = 0.00;
		double sgst = 0.00;
		double igst = 0.00;
		if(taxType.equals(TaxType.CENTRAL_STATE)) {
			sgst = cgst = Math.round(((taxPercentage/ 2) / 100) * invoiceAmount *100.00) / 100.00;
		} else {
			igst = Math.round((taxPercentage / 100) * invoiceAmount *100.00)/100.00;
		}
		response.put("cgst", cgst);
		response.put("igst", igst);
		response.put("sgst", sgst);
		return response;
	}

	@Override
	public List<PayableStatus> getAllPayableStatus() {
		List<PayableStatus> payableStatus = payableStatusRepository.findAll();
		return payableStatus;
	}
}
