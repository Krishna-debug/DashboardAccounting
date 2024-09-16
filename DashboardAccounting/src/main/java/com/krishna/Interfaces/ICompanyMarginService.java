package com.krishna.Interfaces;

import java.util.List;
import java.util.Map;

import com.krishna.domain.ReserveSnapShot;

public interface ICompanyMarginService{

	Map<String, Object> getBuWiseInvoiceTotal( int month, String year, String accessToken);

	List<Object> getCompanywiseData(int month, int year, String accessToken);

	
	Map<String, Object> getDirectCostBuWise(int month, int year, List<Object> buWiseUsers,
			Map<String, Object> invoiceTotal, String accessToken);
	
	Map<String, Object> getCompanyMargin(String accessToken, Map<String, Object> directCostTotal,
			Map<String, Object> invoiceTotal,int month,int year);

	void flushInvoicesCache();

	void flushDirectCostCache();

	void flushTeamData();

	Map<String, Object> getCompanyPL(String accessToken, int month, int year);

	Map<String, Object> getTotalCostDivision(String accessToken, int month, int year, String businessVertical, Map<String,Object> invoiceTotal);

	Map<String, Object> getIndirectCostDivision(String accessToken, int month, int year, Long projectId);

	Map<String, Object> getBuReserve(int month, int year, Map<String, Object> companyMargins, Map<String, Double> disputedAmount,Map<String, Object> invoiceTotal, Map<String, Object> directCostTotal, List<Object> buWiseUsers,Map<String, Double> disputedAmountLTM);

	Map<String, Double> getAverageDisputedPercentage(Integer year,String accessToken);

	Map<String, Double> getLTMBuDisputedPercentage(Integer year,Integer month, String accessToken);


	List<ReserveSnapShot> buReserveCrone(String accessToken);

	Double getReimbursementCost(String accessToken, int month, int year);

	List<Map<String, Object>> getYearlyRevenueForecast(String accessToken, Integer year, String businessVertical);

}
