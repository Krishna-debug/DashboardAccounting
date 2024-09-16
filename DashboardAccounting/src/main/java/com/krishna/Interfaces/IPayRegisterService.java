package com.krishna.Interfaces;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.krishna.domain.Bank;
import com.krishna.domain.PayRevisions;
import com.krishna.dto.PayRegisterDto;

public interface IPayRegisterService {

	/**
	 * Get User Details
	 * 
	 * @param accessToken
	 * @return userDetails
	 */
	public ArrayList<Object> getUserDetails(String accessToken,String userStatus,int month,int year) throws Exception;

	/**
	 * Edit/ Create Payregister
	 * @param accessToken
	 * @param payRegister
	 * @param i
	 * @param year
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> createPayregister(String accessToken, PayRegisterDto payRegister, int i, int year) throws Exception;

	/**
	 * Get All Payregisters
	 * @param accessToken
	 * @param userStatus
	 * @param month
	 * @param year
	 * @param payrollStatus 
	 * @param users 
	 * @return
	 * @throws ParseException
	 */
	public List<Object> getAllPayRegisters(String accessToken,String userStatus, int month, int year, String payrollStatus, List<Object> users) throws ParseException;

	/**
	 * Get All Banks
	 * @return
	 */
	public List<Bank> getBanks();

	/**
	 * Get All Payrevisions of payregister
	 * @param userId
	 * @return
	 */
	public List<PayRevisions> getPayRevisions(long userId);

	/**
	 * Get User Account Details to be shown on My Accounts Section
	 * @param userId
	 * @param year
	 * @param accessToken
	 * @return
	 */
	public Map<String, Object> getUserAccountDetails(long userId, String year, String accessToken);

	/**
	 * Check if payregister exists for this effective date.
	 * 
	 * @param accessToken
	 * @param payRegister
	 * @param month
	 * @param year
	 * @return boolean isOverlapped
	 */
	public boolean checkOverlappingExistingPayregister(String accessToken, PayRegisterDto payRegister, int month, int year);

	/**
	 * Edit PayRevision Dates
	 * 
	 * @param accessToken
	 * @param payRevisionId
	 * @param effectiveTo 
	 * @param effectiveFrom 
	 * @return edited Payrevision Date
	 */
	public PayRevisions editPayRevision(String accessToken, long payRevisionId, Long effectiveFrom, Long effectiveTo);

	/**
	 * To check if payrevision with same Date Already exists.
	 * 
	 * @param accessToken
	 * @param payRevisionId
	 * @param effectiveFrom
	 * @param effectiveTo
	 * @return boolean isOverlapped
	 */
	boolean checkOverlappingExistingPayRevision(String accessToken, long payRevisionId, Long effectiveFrom,
			Long effectiveTo);

	/**
	 * Deletes payRevision
	 * 
	 * @param accessToken
	 * @param payRevisionId
	 * @return the deleted pay Revision data
	 */
	public PayRevisions deletePayRevision(String accessToken, long payRevisionId);
	
	/**
	 * Get the sum of monthly pay of all users
	 * @param accessToken
	 * @param month
	 * @param year
	 * @param userStatus
	 * @param users 
	 * @return
	 */
	public Map<String, Object> getSumOfAllPayRegisterMonthlySalary(String accessToken, String userStatus, int month, int year, List<Object> users);

	public List<Object> getUsersForPayregister(String accessToken, String string, int i, int year);

	public void flushUsersCache();

}
