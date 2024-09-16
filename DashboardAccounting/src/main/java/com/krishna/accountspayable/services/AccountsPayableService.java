package com.krishna.accountspayable.services;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.krishna.accountspayable.domain.AccountsPayable;
import com.krishna.accountspayable.domain.PayableStatus;
import com.krishna.accountspayable.dto.AccountsPayableDataTransfer;
import com.krishna.accountspayable.dto.PayableResponseDto;
import com.krishna.accountspayable.enums.PayableTypes;
import com.krishna.accountspayable.enums.TaxType;

@Service
public interface AccountsPayableService {
	
	/**
	 * Saves the record in the database.
	 * @param data
	 * @return the saved data
	 */
	PayableResponseDto saveAccountsPayableData(AccountsPayableDataTransfer data);
	
	/**
	 * Gets all unArchived data.
	 * @return  List of all payables.
	 * @param month, year
	 */
	List<PayableResponseDto> getAllAccountsPayableData(int month, int year, Long hsnCodeId);
	
	/**
	 * Updates the existing record.
	 * @param id
	 * @param updatedData
	 * @return the updated record.
	 */
	PayableResponseDto updateAccountsPayableData(long id, AccountsPayableDataTransfer updatedData);
	
	/**
	 * Marks the given entry archive value true
	 * @param id
	 * @return current Entity
	 */
	AccountsPayable deleteAccountsPayableData(long id);
	
	/**
	 * Returns all AccountsPayable types.
	 * @return
	 */
	public List<PayableTypes> getAllPayableTypes();
	
	/**
	 * Returns all the recurring payables of last month.
	 */
	List<Map<String, Object>> isRecurringpayableTransferred(int month, int year);
	
	/**
	 * Returns all the available Tax types
	 */
	List<TaxType> getAllPayableTaxTypes();
	
	/**
	 * Calculates SGST, IGST, CGST based on tax type.
	 */
	Map<String, Object> calculateTaxesOfGivenType(TaxType taxType, double invoiceAmount, double taxPercentage);
	
	/**
	 * Returns All the available payable status
	 */
	List<PayableStatus> getAllPayableStatus();
}
