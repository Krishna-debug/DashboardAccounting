package com.krishna.accountspayable.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.krishna.accountspayable.domain.HsnCode;

/**
 * <p>The interface HsnCodeService is a part of AccountsPayable Module and 
 * responsible for managing all service operations for HsnCode Domain</p>
 * 
 * @author Amit Mishra
 */
@Service
public interface HsnCodeService {
	
	/**
	 * Saves the record in the database.
	 * @param
	 * @return the saved data
	 */
	HsnCode saveHSNCode(String hsnCode);
	
	/**
	 * Gets all HSN code data.
	 * @return  JSON array
	 */
	List<HsnCode> getAllHsnCodes();
	
	/**
	 * Updates the existing record.
	 * @param id
	 * @param updatedData
	 * @return the updated record.
	 */
	HsnCode updateHsnCode(long id, String updatedHsnCode);
	
	/**
	 * Marks the given entry archive value true
	 * @param id
	 * @return boolean
	 */
	HsnCode deleteHsnData(long id);
}
