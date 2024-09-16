package com.krishna.controller;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.Interfaces.ICompanyMarginService;
import com.krishna.Interfaces.IPayRegisterService;
import com.krishna.Interfaces.IPayrollService;
import com.krishna.domain.Bank;
import com.krishna.domain.PayRegister;
import com.krishna.domain.PayRevisions;
import com.krishna.domain.UserModel;
import com.krishna.dto.PayRegisterDto;
import com.krishna.repository.payroll.PayRegisterRepository;
import com.krishna.security.JwtValidator;
import com.krishna.service.PayrollService;
import com.krishna.service.ProjectMarginService;
import com.krishna.service.AccessUtility.AccessUtilityService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

/**
 * 
 * @author shivangi
 *
 */
@RestController
public class PayRegisterController {

	@Autowired
	IPayRegisterService payRegisterService;

	@Autowired
	JwtValidator validator;
	
	@Autowired
	PayRegisterRepository payRegisterRepository;
	
	@Autowired
	AccessUtilityService accessUtilityService;
	
	@Autowired
	FeignLegacyInterface feignLegacyInterface; 
	
	@Autowired
	ICompanyMarginService companyMarginService;
	
	@Autowired
	ProjectMarginService projectMarginService;
	
	@Autowired
	IPayrollService payrollService;
	
	Logger log=LoggerFactory.getLogger(PayRegisterController.class);

	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method = RequestMethod.POST,value=UrlMappings.CREATE_PAYREGISTER)
	public ResponseEntity<Object> createPayregister(@RequestHeader("Authorization")String accessToken,@RequestBody PayRegisterDto payRegister,@RequestParam int month,@RequestParam int year) throws Exception{
		PayRegister existingPayRegister = payRegisterRepository.findAllByUserIdAndIsCurrent(payRegister.getUserId(), true);
		Map<String, Object> userInfo = (Map<String, Object>) feignLegacyInterface.getUserDetails(accessToken, payRegister.getUserId()).get("data");
		boolean isAccountsAdmin=accessUtilityService.isAccountsAdmin(accessToken,true);
		boolean isDashboardAdmin = accessUtilityService.isAccountsAdmin(accessToken,false);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ConstantUtility.DATE_FORMATTER_FORMAT2, Locale.ENGLISH);
		LocalDateTime joiningDate = LocalDateTime.parse((String)userInfo.get(ConstantUtility.DATE_OF_JOINING),formatter);
		YearMonth effectiveObj=YearMonth.of(new Timestamp(payRegister.getEffectiveDate()).toLocalDateTime().getYear(), new Timestamp(payRegister.getEffectiveDate()).toLocalDateTime().getMonthValue());
		YearMonth currObj=YearMonth.of(LocalDateTime.now().minusMonths(1).getYear(),LocalDateTime.now().minusMonths(1).getMonthValue());
		if(!isDashboardAdmin && effectiveObj.isBefore(currObj))
			return ResponseHandler.errorResponse(ConstantUtility.PAY_REGISTER_FROZEN, HttpStatus.NOT_ACCEPTABLE, existingPayRegister.getEffectiveDate());
		if(existingPayRegister!=null && new Timestamp(payRegister.getEffectiveDate()).toLocalDateTime().isBefore(existingPayRegister.getEffectiveDate()) && !isAccountsAdmin ) {
			return ResponseHandler.errorResponse(ConstantUtility.EFFECTIVE_DATE_IS_BEFORE_PREVIOUS_EFFECTIVE_DATE, HttpStatus.NOT_ACCEPTABLE, existingPayRegister.getEffectiveDate());
		}
		if(new Timestamp(payRegister.getEffectiveDate()).toLocalDateTime().isBefore(joiningDate)) {
			return ResponseHandler.errorResponse(ConstantUtility.EFFECTIVE_DATE_BEFORE_JOINING_DATE, HttpStatus.NOT_ACCEPTABLE);
		}
		boolean isOverlapping=payRegisterService.checkOverlappingExistingPayregister(accessToken,payRegister,month+1,year);
		if(existingPayRegister!=null && isAccountsAdmin && isOverlapping)
			return ResponseHandler.errorResponse(ConstantUtility.SAME_EFFECTIVE_DATE_PAYREGISTER_EXISTS, HttpStatus.NOT_ACCEPTABLE);
		Map<String,Object> payRegisterData=payRegisterService.createPayregister(accessToken,payRegister,month+1,year);
		if(payRegisterData!=null) {
			companyMarginService.flushDirectCostCache();
			projectMarginService.flushTotalBuMargins();
		    projectMarginService.flushBuMargins();
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, payRegisterData);
		}
		
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.BAD_REQUEST);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method=RequestMethod.GET,value=UrlMappings.GET_PAYREGISTERS)
	public ResponseEntity<Object> getAllPayRegisters(@RequestHeader("Authorization") String accessToken,@RequestParam String userStatus,@RequestParam int month,@RequestParam int year,@RequestParam(required=false) String payrollStatus) throws Exception{
		List<Object> users = payRegisterService.getUsersForPayregister(accessToken, userStatus, month+1, year);
		List<Object> allUsers=payRegisterService.getAllPayRegisters(accessToken,userStatus,month+1,year,payrollStatus,users);
		if(allUsers!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, allUsers);
		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.BAD_REQUEST);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method=RequestMethod.GET,value=UrlMappings.GET_BANKS)
	public ResponseEntity<Object> getBanks(@RequestHeader("Authorization") String accessToken){
		List<Bank> banks=payRegisterService.getBanks();
		if(!banks.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, banks);
		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.NO_CONTENT);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method=RequestMethod.GET,value=UrlMappings.GET_PAYREVISIONS)
	public ResponseEntity<Object> getPayRevisions(@RequestHeader("Authorization") String accessToken,@PathVariable("userId") long userId){
		List<PayRevisions> payRevisions=payRegisterService.getPayRevisions(userId);
		if(!payRevisions.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, payRevisions);
		}
		return ResponseHandler.generateResponse(HttpStatus.OK, false, ConstantUtility.NO_DATA_AVAILABLE, payRevisions);
	}
	
	@RequestMapping(method=RequestMethod.GET,value=UrlMappings.GET_USER_ACCOUNTDETAILS)
	public ResponseEntity<Object> getUserAccountDetails(@RequestHeader("Authorization") String accessToken,@RequestParam Long userId,@RequestParam String year){
		UserModel currentUser=validator.tokenbValidate(accessToken);
		if(Long.toString(currentUser.getUserId()).equals(userId.toString())) {
		Map<String,Object> accountDetails=payRegisterService.getUserAccountDetails(userId,year,accessToken);
		if(accountDetails!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.ACCOUNT_DETAILS_FETCHED, accountDetails);
		}
		else {
			return ResponseHandler.generateResponse(HttpStatus.OK, false, ConstantUtility.USER_IS_NOT_IN_PAYREGISTER, accountDetails);
		}
		}
		else {
			return ResponseHandler.generateResponse(HttpStatus.FORBIDDEN, false, ConstantUtility.ACCESS_DENIED, null);
		}
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS_ADMIN')")
	@RequestMapping(method=RequestMethod.PUT,value=UrlMappings.EDIT_PAYREVISION)
	public ResponseEntity<Object> editPayRevision(@RequestHeader("Authorization") String accessToken,@RequestParam long payRevisionId,@RequestParam Long effectiveFrom,@RequestParam Long effectiveTo){
		PayRevisions payrevision=payRegisterService.editPayRevision(accessToken,payRevisionId,effectiveFrom,effectiveTo);
		if(payrevision!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.PAYREVISION_EDITED, payrevision);
		}
		else {
			return ResponseHandler.generateResponse(HttpStatus.OK, false, ConstantUtility.UNABLE_TO_EDIT_PAYREVISION, payrevision);
		}
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS_ADMIN')")
	@RequestMapping(method=RequestMethod.PUT,value=UrlMappings.DELETE_PAYREVISION)
	public ResponseEntity<Object> deletePayRevision(@RequestHeader("Authorization") String accessToken,@RequestParam long payRevisionId){
		PayRevisions payrevision=payRegisterService.deletePayRevision(accessToken,payRevisionId);
		if(payrevision!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.PAYREVISION_DELETED, payrevision);
		}
		else {
			return ResponseHandler.generateResponse(HttpStatus.OK, false, ConstantUtility.UNABLE_TO_DELETE_PAYREVISION, payrevision);
		}
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method = RequestMethod.GET, value= UrlMappings.GET_SUM_FOR_WIDGETS)
	public ResponseEntity<Object> getTotalMonthlySalary(@RequestHeader("Authorization") String accessToken, String userStatus,int month, int year){
		List<Object> users = payRegisterService.getUsersForPayregister(accessToken, userStatus, month+1, year);
		Map<String, Object> sum = payRegisterService.getSumOfAllPayRegisterMonthlySalary(accessToken, userStatus, month+1, year, users);
		if(!sum.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, sum);
		}
		else {
			return ResponseHandler.generateResponse(HttpStatus.OK, false, ConstantUtility.FAILED, sum);
		}
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method = RequestMethod.GET, value= UrlMappings.GET_PAYREGISTER_USERS)
	public ResponseEntity<Object> getUsersForPayregister(@RequestHeader("Authorization") String accessToken, String userStatus,int month, int year){
		payRegisterService.flushUsersCache();
		payrollService.flushTimesheetCache();
		List<Object> users = payRegisterService.getUsersForPayregister(accessToken, userStatus, month+1, year);
		if(!users.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, users);
		}
		else {
			return ResponseHandler.generateResponse(HttpStatus.OK, false, ConstantUtility.FAILED, users);
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value= UrlMappings.GET_BU_DASHBOARD_PAYREGISTER_USERS)
	public ResponseEntity<Object> getBuWiseUsersForPayregister(@RequestHeader("Authorization") String accessToken, @RequestParam String businessVertical, String userStatus,int month, int year){
		payRegisterService.flushUsersCache();
		payrollService.flushTimesheetCache();
		List<Object> allUsers = payRegisterService.getUsersForPayregister(accessToken, userStatus, month+1, year);
		List<Map<String, Object>> allUsersMap = allUsers.stream().map(obj -> (Map<String, Object>) obj).collect(Collectors.toList());
		List<Object> buUsers = allUsersMap.stream()
			    .filter(user -> user.get("businessVertical").equals(businessVertical) && user.get("id") != null)
			    .collect(Collectors.toList());
		if(!buUsers.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, buUsers);
		}
		else {
			return ResponseHandler.generateResponse(HttpStatus.OK, false, ConstantUtility.FAILED, buUsers);
		}
	}
	
	@RequestMapping(method=RequestMethod.GET,value=UrlMappings.GET_BU_DASHBOARD_PAYREGISTERS)
	public ResponseEntity<Object> getAllBuWisePayRegisters(@RequestHeader("Authorization") String accessToken, @RequestParam String businessVertical, @RequestParam String userStatus,@RequestParam int month,@RequestParam int year,@RequestParam(required=false) String payrollStatus) throws Exception{
		List<Object> users = payRegisterService.getUsersForPayregister(accessToken, userStatus, month+1, year);
		List<Map<String, Object>> allUsersMap = users.stream().map(obj -> (Map<String, Object>) obj).collect(Collectors.toList());
		List<Object> buUsers = allUsersMap.stream()
			    .filter(user -> user.get("businessVertical").equals(businessVertical) && user.get("id") != null)
			    .collect(Collectors.toList());
		List<Object> allUsers = payRegisterService.getAllPayRegisters(accessToken,userStatus,month+1,year,payrollStatus,buUsers);
		if(allUsers!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, allUsers);
		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.BAD_REQUEST);
	}
}