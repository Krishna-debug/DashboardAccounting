package com.krishna.Interfaces;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.krishna.domain.LeaveCostPercentage;
import com.krishna.dto.LeaveCostPercentageDto;

/**
 * The leave cost percentage service.
 * @author amit
 *
 */
@Service
public interface LeaveCostPercentageService {
	
	/**
	 * Saves the leave cost percentage.
	 * @param leaveCostPercentageDto
	 * @param accessToken
	 * @return saved leave cost percentage entity data.
	 */
	Map<String, Object> saveLeaveCostPercentage(Double leaveCostPercentage, String accessToken);
	
	/**
	 * Updates the leave cost percentage and save if it doesn't exists.
	 * @param leaveCostPercentage
	 * @param accessToken
	 * @param id 
	 * @return
	 */
	Map<String, Object> updateLeaveCostPercentage(Double leaveCostPercentage, String accessToken, long id);
	
	/**
	 * Deletes the leave cost percentage.
	 * @param id - The id of the cost percentage.
	 * @param accessToken
	 * @return boolean
	 */
	boolean deleteLeaveCostPercentage(long id, String accessToken);
	
	/**
	 * Gets all leave cost percentage from the database.
	 * @return List of all leave cost percentage available.
	 */
	List<Map<String, Object>> getAllLeaveCostPercentage();

	/**
	 * Get Current Paid Leave Percentage
	 * 
	 * @return Current Paid Leave Percentage
	 */
	Map<String, Object> getCurrentLeaveCostPercent();
	
}
