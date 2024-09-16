package com.krishna.Interfaces;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.krishna.dto.DollarCostDTO;

@Service
public interface DollarCostService {
	/**
	 * Gets All the available dollar rate.
	 * @return Map containing the values.
	 */
	Map<String, Object> getAllCost(Integer year);
	/**
	 * Saves the dollar rate in the database.
	 * @param requestBody
	 * @param accessToken
	 * @return
	 */
	Map<String, Object> saveDollarCost(DollarCostDTO requestBody, String accessToken);
	/**
	 * Updates the existing dollar cost
	 * @param requestBody
	 * @param id
	 * @param accessToken
	 * @return
	 */
	Map<String, Object> updateDollarCost(DollarCostDTO requestBody, Long id, String accessToken);
	/**
	 * Deletes the dollar cost.
	 * @param id
	 * @param accessToken
	 */
	void deleteDollarCost(Long id, String accessToken);
	/**
	 * Returns the dollar cost of the given month and year.
	 * @param month
	 * @param year
	 * @return
	 */
	Map<String, Object> getCostByMonthAndYear(int month, int year);
	
	/**
	 * Fetch last 6 month average dollar cost.
	 * @return
	 */
	ArrayList<Object> getLast6MonthAverage();
	
	
	Double getAverageDollarCost(int i, int year);
	
	Object getDollarCostForConversion(int monthNum, int year);

	public List<Map<String,Object>> getMonthWiseAverageDollarCost(Date fromDate, Date toDate);

}
