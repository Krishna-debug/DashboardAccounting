package com.krishna.accountspayable.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.krishna.accountspayable.domain.AccountsHead;

/**
 * <p>The interface AccountsHeadService is responsible for managing all 
 * service operations for AccountsHead Domain.</p>
 * 
 * @author Amit Mishra
 */
@Service
public interface AccountsHeadService {
	/**
	 * Saves the record in the database.
	 * @param
	 * @return the saved data
	 */
	AccountsHead saveAccountHead(String name);
	
	/**
	 * Gets all AccountsHead data.
	 * @return  JSON array
	 */
	List<AccountsHead> getAllAccountsHead();
	
	/**
	 * Updates the existing record.
	 * @param id
	 * @param updatedData
	 * @return the updated record.
	 */
	AccountsHead updateExisting(long id, String updatedName);
	
	/**
	 * Marks the given entry archive value true
	 * @param id
	 * @return boolean
	 */
	AccountsHead deleteAccountsHead(long id);
}
