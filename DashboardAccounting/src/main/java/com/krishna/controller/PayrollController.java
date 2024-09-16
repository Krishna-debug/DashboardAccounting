package com.krishna.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.Interfaces.IPayRegisterService;
import com.krishna.Interfaces.IPayrollService;
import com.krishna.domain.Arrear;
import com.krishna.domain.AttendanceVerification;
import com.krishna.domain.Payroll;
import com.krishna.domain.TimesheetCompVerification;
import com.krishna.dto.ArrearFilesDto;
import com.krishna.dto.AttendanceCommentDto;
import com.krishna.dto.PayrollDto;
import com.krishna.enums.PayRollStatus;
import com.krishna.repository.payroll.ArrearRepository;
import com.krishna.repository.payroll.AttendanceVerificationRepository;
import com.krishna.repository.payroll.PayrollRepository;
import com.krishna.security.JwtValidator;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

/**
 * 
 * @author shivangi
 *
 *The payroll Controller
 */
@RestController
// @PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
public class PayrollController {
	
	Logger log=LoggerFactory.getLogger(PayrollController.class);

	@Autowired
	IPayrollService payRollService;

	@Autowired
	JwtValidator validator;
	
	@Autowired
	IPayRegisterService payRegisterService;
	
	@Autowired
	PayrollRepository payrollRepository;
	
	@Autowired
	ArrearRepository arrearRepository;
	
	@Autowired
	AttendanceVerificationRepository verificationRepository;

	/**
	 * @author shivangi
	 * 
	 * Generate Payrolls
	 * 
	 * @param accessToken
	 * @param month
	 * @return list of payrolls
	 */
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method = RequestMethod.POST, value = UrlMappings.GENERATE_PAYROLL)
	public ResponseEntity<Object> generatePayroll(@RequestHeader("Authorization") String accessToken,
			@RequestParam int month,@RequestParam int year,@RequestParam String userStatus) {
		List<Payroll> allPayRolls = payRollService.generatePayroll(accessToken, month+1,year,userStatus);
		payRollService.flushPayrollCache();
		if (!allPayRolls.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, allPayRolls);
		}
		return ResponseHandler.errorResponse(ConstantUtility.PAYROLL_CREATION_FAILED, HttpStatus.EXPECTATION_FAILED);
	}
	
	/**
	 * Edit Payrolls
	 * 
	 * @param accessToken
	 * @param payroll
	 * @param month
	 * @return Edited Payroll Data
	 */
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method=RequestMethod.PUT,value=UrlMappings.EDIT_PAYROLL)
	public ResponseEntity<Object> editPayroll(@RequestHeader("Authorization")String accessToken,@RequestBody PayrollDto payroll,@RequestParam int month,@RequestParam int year){
		AttendanceVerification attendance=verificationRepository.findAllByUserIdAndMonthAndYear(payroll.getUserId(), month+1, year);
		if((attendance==null && payroll.getPayrollStatus().equals(PayRollStatus.VERIFIED)) || (attendance!=null && payroll.getPayrollStatus().equals(PayRollStatus.VERIFIED) && !attendance.isAttendanceVerified())) {
			return ResponseHandler.errorResponse(ConstantUtility.ATTENDENCE_VERIFICATION_FAILED, HttpStatus.EXPECTATION_FAILED);
		}
		Map<String,Object> editedPayroll=payRollService.editPayRoll(accessToken,payroll,month+1,year);
		payRollService.flushPayrollCache();
		if(editedPayroll!=null) {
			if(editedPayroll.containsKey("timesheetIssueMessage"))
				return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, editedPayroll.get("timesheetIssueMessage").toString(), editedPayroll);
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, editedPayroll);
		}
		return ResponseHandler.errorResponse(ConstantUtility.EDITING_FAILED, HttpStatus.CONFLICT);
	}
	
	/**
	 * Saves Arrears
	 * 
	 * @param accessToken
	 * @param arrearAmount
	 * @param arrearMonth
	 * @param payrollId
	 * @param arrearYear
	 * @param arrearComment
	 * @return Payroll Data
	 */
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method = RequestMethod.POST, value = UrlMappings.SAVE_ARREAR)
	public ResponseEntity<Object> saveArrear(@RequestHeader("Authorization") String accessToken,@RequestParam double arrearAmount, @RequestParam int arrearMonth, @PathVariable("userId") long userId,@RequestParam int arrearYear, @RequestParam String arrearComment, @RequestParam int creationMonth, @RequestParam int creationYear, @RequestParam boolean isArrearIncluded,@RequestParam boolean isReimbursement,@RequestBody ArrearFilesDto arrearFilesDto) {
		Payroll existingPayroll = payrollRepository.findAllByMonthAndUserIdAndYear(creationMonth+1,userId, creationYear);
		if (existingPayroll == null && isArrearIncluded) {
			return ResponseHandler.generateResponse(HttpStatus.OK, false, ConstantUtility.PAYROLL_NOT_EXISTS,existingPayroll);
		} 
		if(existingPayroll!=null && (existingPayroll.getPayRollStatus().equals(PayRollStatus.PROCESSED) || existingPayroll.getPayRollStatus().equals(PayRollStatus.FILEPROCESSED)) && isArrearIncluded) 
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, true, ConstantUtility.PAYROLL_GENERATED_PAYSLIP_NOT_ADDED, null);
		else {
			Arrear payroll = payRollService.saveArrear(arrearAmount, arrearMonth, userId, arrearYear, arrearComment,accessToken,creationMonth+1,creationYear,isArrearIncluded,arrearFilesDto,isReimbursement);
			payRollService.flushPayrollCache();
			if (payroll != null) {
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, payroll);
			}
			return ResponseHandler.errorResponse(ConstantUtility.CREATION_FAILED, HttpStatus.CONFLICT);
		}
	}
	
	/**
	 * Get All Payrolls
	 * 
	 * @param accessToken
	 * @param month
	 * @param year
	 * @return list of payrolls
	 */
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method=RequestMethod.GET,value=UrlMappings.GET_PAYROLLS)
	public ResponseEntity<Object> getPayrolls(@RequestHeader("Authorization")String accessToken,@RequestParam int month,@RequestParam int year,@RequestParam String timesheetCompliance,@RequestParam(required = false) String priority){
		List<Object> users = payRegisterService.getUsersForPayregister(accessToken, "All", month+1, year);
		Map<String,Object> timesheets=payRollService.getPayrollUsersTimesheetByUserList(accessToken, month+1, year);
		List<Payroll> payrolls=payRollService.getPayrollsForMonth(month+1, year, timesheetCompliance, priority, timesheets);
		List<Object> payrollData= payRollService.getPayrolls(month+1,year,accessToken,timesheetCompliance,priority,timesheets,payrolls,users);
		if(!payrollData.isEmpty()){
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, payrollData);
		}
		if(payrollData.isEmpty())
			return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.OK);	
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.BAD_REQUEST);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method=RequestMethod.GET,value=UrlMappings.GET_PAYROLLS_TIMESHEET)
	public ResponseEntity<Object> getPayrollUsersTimesheet(@RequestHeader("Authorization")String accessToken,@RequestParam int month,@RequestParam int year,@RequestParam String timesheetCompliance,@RequestParam(required = false) String priority){
		List<Object> users = payRegisterService.getUsersForPayregister(accessToken, "All", month+1, year);
		Map<String,Object> timesheets=payRollService.getPayrollUsersTimesheet(accessToken,month+1, year);
		if(timesheets!=null){
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, timesheets);
		}
			return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.OK);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method=RequestMethod.GET,value=UrlMappings.GET_MONTHWISE_PAYROLLS)
	public ResponseEntity<Object> getPayrollsForMonth(@RequestHeader("Authorization")String accessToken,@RequestParam int month,@RequestParam int year,@RequestParam(required = false) String timesheetCompliance,@RequestParam(required = false) String priority){
		Map<String,Object> timesheets=payRollService.getPayrollUsersTimesheet(accessToken, month+1, year);
		List<Payroll> payrolls=payRollService.getPayrollsForMonth(month+1, year, timesheetCompliance, priority, timesheets);
		if(payrolls!=null){
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, payrolls);
		}
			return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.OK);
	}
	
	/**
	 * Get Arrears of an employee
	 * 
	 * @param accessToken
	 * @param payrollId
	 * @param month
	 * @param year
	 * @return a list of arrears of an employee for given month
	 */
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method=RequestMethod.GET,value=UrlMappings.GET_EMPLOYEE_ARREARS)
	public ResponseEntity<Object> getEmployeeArrears(@RequestHeader("Authorization")String accessToken,@PathVariable("payrollId") long payrollId,@RequestParam int month,@RequestParam int year,@RequestParam boolean isReimbursement){
		List<Object> arrearDetails= payRollService.getEmployeeArrears(payrollId,accessToken,month+1,year,isReimbursement);
		if(!arrearDetails.isEmpty()){
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, arrearDetails);
		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.OK);
	}
	
	/**
	 * Get User Details
	 * 
	 * @param accessToken
	 * @param userStatus
	 * @return a list of all Users
	 * @throws Exception
	 */
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method=RequestMethod.GET,value=UrlMappings.GET_USER_DETAILS)
	public ResponseEntity<Object> getUserDetails(@RequestHeader("Authorization")String accessToken,@RequestParam String userStatus,@RequestParam int month,@RequestParam int year) throws Exception{
		ArrayList<Object> users=payRegisterService.getUserDetails(accessToken, "All",month+1,year);
		payRollService.flushTimesheetCache();
		if(!users.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, users);
		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.OK);
	}
	
	/**
	 * Get All Arrears
	 * 
	 * @param accessToken
	 * @param month
	 * @param year
	 * @return a list of all arrears for given Month and year
	 */
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method = RequestMethod.GET, value = UrlMappings.GET_ALL_ARREARS)
	public ResponseEntity<Object> getAllArrears(@RequestHeader("Authorization") String accessToken,
			@RequestParam int month, @RequestParam int year,@RequestParam boolean isReimbursement) {
		List<Object> arrears = payRollService.getAllArrears(month + 1, year, accessToken,isReimbursement);
		if (!arrears.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, arrears);
		} 
		else
			return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.OK, arrears);
	}
	
	/**
	 * Edit the arrear
	 * 
	 * @param accessToken
	 * @param arrearId
	 * @param arrearAmount
	 * @param arrearMonth
	 * @param arrearYear
	 * @param arrearComment
	 * @return Edited Arrear
	 */
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method=RequestMethod.PUT,value=UrlMappings.EDIT_ARREARS)
	public ResponseEntity<Object> editArrear(@RequestHeader("Authorization") String accessToken,@PathVariable("arrearId") long arrearId,@RequestParam double arrearAmount,@RequestParam int arrearMonth,@RequestParam int arrearYear,@RequestParam String arrearComment,@RequestParam boolean isArrearIncluded,@RequestBody ArrearFilesDto arrearFileDto,@RequestParam boolean isReimbursement){
		Arrear arrear=arrearRepository.findAllById(arrearId);
		Payroll payroll=payrollRepository.findAllByMonthAndUserIdAndYearAndIsDeleted(arrear.getCreationMonth(), arrear.getUserId(), arrear.getCreationYear(), false);
		if(payroll==null && isArrearIncluded) {
			return ResponseHandler.errorResponse("Payroll not found to include arrear amount.", HttpStatus.EXPECTATION_FAILED, null);
		}
		else if(payroll!=null && !payroll.getPayRollStatus().equals(PayRollStatus.PENDING) && !payroll.getPayRollStatus().equals(PayRollStatus.PROCESSED)) {
			return ResponseHandler.errorResponse("Unable to edit record as payroll is not pending.", HttpStatus.EXPECTATION_FAILED, null);
		}
		if(payroll!=null && payroll.getPayRollStatus().equals(PayRollStatus.PROCESSED)) {
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, true, ConstantUtility.PAYROLL_GENERATED_PAYSLIP_NOT_ADDED, arrear);
		}
		Arrear editedArrear=payRollService.editArrear(accessToken, arrearId, arrearAmount,arrearMonth,arrearYear,arrearComment,isArrearIncluded,arrearFileDto,isReimbursement);
		payRollService.flushPayrollCache();
		if(editedArrear!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, editedArrear);
		}
		return ResponseHandler.errorResponse(ConstantUtility.EDITING_FAILED, HttpStatus.CONFLICT);
	}
	
	/**
	 * Get Id of All Payroll Users
	 * 
	 * @param accessToken
	 * @param month
	 * @param year
	 * @return
	 */
	@RequestMapping(method=RequestMethod.GET,value=UrlMappings.GET_PAYROLL_USERS)
	public ResponseEntity<Object> getAttendanceVerifiedUsers(@RequestHeader("Authorization")String accessToken,@RequestParam int month,@RequestParam int year){
		List<Long> payrollUsers=payRollService.getAttendanceVerifiedUsers(accessToken,month+1,year);
		if(!payrollUsers.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, payrollUsers);
		else
			return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.OK, payrollUsers);
	}
	
	/**
	 * @author shivangi
	 * 
	 * Verifies the attendance
	 *  
	 * @param accessToken
	 * @param month
	 * @param year
	 * @param userIds
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = UrlMappings.VERIFY_ATTENDANCE)
	ResponseEntity<Object> verifyAttendance(@RequestHeader("Authorization") String accessToken, @RequestParam int month,@RequestParam int year,@RequestParam List<Integer> userIds) {
		List<AttendanceVerification> attendanceVerifiedUsers = payRollService.verifyAttendance(accessToken, month, year,userIds);
		if (!attendanceVerifiedUsers.isEmpty()) {
			payRollService.flushPayrollCache();
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.ATTENDANCE_VERIFIED,attendanceVerifiedUsers);
		}else
			return ResponseHandler.errorResponse(ConstantUtility.ALL_USERS_VERIFIED, HttpStatus.EXPECTATION_FAILED);
	}

	
	/**
	 * @author shivangi
	 * 
	 * Download Arrear File
	 * 
	 * @param accessToken
	 * @param filePath
	 * @param fileName
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = UrlMappings.DOWNLOAD_ARREAR_FILE)
	ResponseEntity<Object> downloadArrearFiles(@RequestHeader("Authorization") String accessToken,@RequestParam String filePath,@RequestParam String fileName) {
		String url = payRollService.downloadArrearFiles(accessToken, filePath,fileName);
		if (!url.equals("")) 
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.FILE_DOWNLOADED,url);
		else
			return ResponseHandler.errorResponse(ConstantUtility.UNABLE_TO_DOWNLOAD, HttpStatus.EXPECTATION_FAILED);
	}
	
	/**
	 * @author shivangi
	 * 
	 * Verify/Unverify Attendance using toggle button
	 * 
	 * @param accessToken
	 * @param userId
	 * @param isAttendanceVerified
	 * @param month
	 * @param year
	 * @return
	 */
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN','DASHBOARD_ADMIN')")
	@RequestMapping(method = RequestMethod.PUT, value = UrlMappings.UNVERIFY_ATTENDANCE)
	ResponseEntity<Object> unverifyAttendance(@RequestHeader("Authorization") String accessToken,@RequestParam long userId,@RequestParam boolean isAttendanceVerified,@RequestParam int month,@RequestParam int year) {
		AttendanceVerification payroll = payRollService.unverifyAttendance(accessToken,userId,isAttendanceVerified,month+1,year);
		if (payroll!=null) {
			payRollService.flushPayrollCache();
			if(isAttendanceVerified)
				return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.ATTENDANCE_VERIFIED,payroll);
			else
				return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.ATTENDANCE_UNVERIFIED,payroll);
		}else
			return ResponseHandler.errorResponse(ConstantUtility.ATTENDENCE_VERIFICATION_FAILED, HttpStatus.EXPECTATION_FAILED);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method = RequestMethod.GET, value = UrlMappings.GET_USER_LEAVES_DATA)
	public ResponseEntity<Object> userLeavesdataForPayroll(@RequestHeader("Authorization") String accessToken,@RequestParam int month,@RequestParam int year,@RequestParam long userId) {
		ArrayList<Object> attendanceData = payRollService.userLeavesdataForPayroll(accessToken, userId,month+1,year);
		if (!attendanceData.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, attendanceData);
		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.OK);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method = RequestMethod.GET, value = UrlMappings.SEND_MAIL_ON_NONCOMPLIANT_TIMESHEET)
	public ResponseEntity<Object> sendMailOnNonCompliantTimesheet(@RequestHeader("Authorization") String accessToken,@RequestParam int month,@RequestParam int year,@RequestParam long userId) {
		boolean mailSent = payRollService.sendMailOnNonCompliantTimesheet(accessToken, userId,month+1,year);
		if (mailSent) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.MAIL_SENT, mailSent);
		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.OK);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN','ADMIN','HR','DASHBOARD_ADMIN','HR_ADMIN')")
	@RequestMapping(method = RequestMethod.PUT, value = UrlMappings.SAVE_PAYROLL_COMMENT)
	public ResponseEntity<Object> savePayrollComment(@RequestHeader("Authorization") String accessToken,@RequestBody AttendanceCommentDto attendanceCommentDto) {
		String attendanceComment = payRollService.savePayrollComment(accessToken, attendanceCommentDto.getUserId(),attendanceCommentDto.getMonth()+1,attendanceCommentDto.getYear(),attendanceCommentDto.getComment());
		payRollService.flushPayrollCache();
		if (attendanceComment!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.COMMENT_SAVED, attendanceComment);
		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.OK);
	}
	
	@RequestMapping(method=RequestMethod.GET,value=UrlMappings.GET_PAYROLL_COMMENTS)
	public ResponseEntity<Object> getPayrollComments(@RequestHeader("Authorization")String accessToken,@RequestParam int month,@RequestParam int year){
		List<Object> commentData=payRollService.getPayrollComments(accessToken,month+1,year);
		if(!commentData.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.COMMENTS_FETCHED, commentData);
		else
			return ResponseHandler.errorResponse(ConstantUtility.UNABLE_TO_FETCH_COMMENTS, HttpStatus.EXPECTATION_FAILED, commentData);
	}
	
	/**
	 * @param accessToken
	 * @param month
	 * @param year
	 * @param timesheetCompliance
	 * @return
	 */
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method = RequestMethod.GET, value = UrlMappings.GET_PAYROLL_WIDGETS_DATA)
	public ResponseEntity<Object> getPayRollWidgetsData(@RequestHeader("Authorization") String accessToken,
			@RequestParam int month, @RequestParam int year, @RequestParam String timesheetCompliance) {
		Map<String,Object> timesheets=payRollService.getPayrollUsersTimesheet(accessToken, month+1, year);
		List<Payroll> payrolls=payRollService.getPayrollsForMonth(month+1, year, timesheetCompliance, "", timesheets);
		Map<String, Object> payRollWidgetsData = payRollService.getPayRollWidgetsData(month + 1, year, accessToken,
				timesheetCompliance,payrolls);
		if (!payRollWidgetsData.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, payRollWidgetsData);
		}
		if (payRollWidgetsData.isEmpty())
			return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.OK);
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.BAD_REQUEST);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@DeleteMapping(UrlMappings.DELETE_PAYROLL)
	public ResponseEntity<Object> deletePayroll(@PathVariable("payrollId") Long payrollId, @RequestHeader("Authorization") String accessToken){
		Map<String, Object> data = payRollService.deletePayroll(accessToken, payrollId);
		payRollService.flushPayrollCache();
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "Done", data);
	}
	

	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method = RequestMethod.GET, value = UrlMappings.GET_ARREARS_WIDGET)
	public ResponseEntity<Object> getArrearWidgetData(@RequestHeader("Authorization") String accessToken,
			@RequestParam int month, @RequestParam int year, @RequestParam boolean isReimbursement) {
		Map<String,Object> arrears = payRollService.getArrearWidgetData(month + 1, year, accessToken, isReimbursement);
		if (arrears!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, arrears);
		} 
		else
			return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.OK, arrears);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@RequestMapping(method=RequestMethod.PUT,value=UrlMappings.CHANGE_PAYROLL_AND_INVOICE_STATUS)
	public ResponseEntity<Object> changePayrollAndInvoiceStatus(@RequestHeader("Authorization") String accessToken, @RequestParam("payrollId") long payrollId){
		boolean isChanged = payRollService.changePayrollAndPayslipStatus(payrollId);
		payRollService.flushPayrollCache();
		if(!isChanged) {
			return ResponseHandler.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, false, ConstantUtility.PAYROLL_OR_PAYSLIP_NOT_EXISTS, isChanged);
		}
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, isChanged);
	}
	
	@GetMapping(UrlMappings.IS_ATTENDANCE_VERIFIED)
	public ResponseEntity<Object> getAttendanceVerificationData(@RequestHeader("Authorization") String accessToken,@RequestParam int month, @RequestParam int year,@RequestParam long userId){
		Map<String,Object> result=payRollService.getIsAttendanceVerifiedForUser(userId,month+1,year);
		if (result.get("isAttendanceVerified")!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, result);
		} 
		else {
			return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.OK, result);	
		}
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@PutMapping(UrlMappings.SET_PAYROLL_ON_PRIORITY)
	public ResponseEntity<Object> setPayrollOnPriority(@RequestHeader("Authorization") String accessToken,@RequestParam long payrollId){
		HashMap<String, Object> savedData = payRollService.setPayrollOnPriority(payrollId, accessToken);
		payRollService.flushPayrollCache();
		if(savedData == null)
			return ResponseHandler.errorResponse(ConstantUtility.UNABLE_TO_SET_PAYROLL_ON_PRIORITY, HttpStatus.EXPECTATION_FAILED);
		return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.PAYROLL_ON_PRIORITY, savedData);
	}
	
	@RequestMapping(method = RequestMethod.PUT, value = UrlMappings.SET_PAID_DAYS)
	ResponseEntity<Object> setVerifiedPaidDays(@RequestHeader("Authorization") String accessToken, @RequestParam int month,@RequestParam int year,@RequestParam List<Integer> userIds) {
		String isVerified = payRollService.setVerifiedPaidDays(accessToken, month, year,userIds);
		if (isVerified.equals("Verified"))
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.ATTENDANCE_VERIFIED,isVerified);
		else
			return ResponseHandler.errorResponse(isVerified, HttpStatus.EXPECTATION_FAILED);
	}

	@PreAuthorize("hasAnyRole('ACCOUNTS' , 'ACCOUNTS_ADMIN')")
	@PutMapping(UrlMappings.RESET_PAYROLL_PRIORITY)
	public ResponseEntity<Object> resetPayrollPriority(@RequestHeader("Authorization") String accessToken, int month, int year){
		boolean status = payRollService.resetPayrollPriority(month + 1, year);
		payRollService.flushPayrollCache();
		if(status) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.PRIORITY_RESET_DONE, status);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.UNABLE_TO_RESET_PRIORITY, status);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS' , 'ACCOUNTS_ADMIN')")
	@PostMapping(UrlMappings.ADD_PAYROLL_COMMENTS)
	public ResponseEntity<Object> addPayrollComments(@RequestHeader("Authorization") String accessToken, 
			@RequestParam Long payrollId, @RequestParam String comments) {
		boolean isSaved = payRollService.addPayrollComments(payrollId, comments);
		payRollService.flushPayrollCache();
		if(isSaved) {
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SAVED_PAYROLL_COMMENTS, comments);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.UNABLE_TO_ADD_PAYROLL_COMMENTS, "");
		}
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS' , 'ACCOUNTS_ADMIN')")
	@PostMapping(UrlMappings.GET_PAYROLL_DETAILS)
	public ResponseEntity<Object> getPayrollDetails(@RequestHeader("Authorization") String accessToken, 
			@RequestParam Long userId,@RequestParam int month,@RequestParam int year) {
		Map<String,Object> accountDetails=payRollService.getPayrollDetails(userId, year, month+1, accessToken);
		if(accountDetails!=null) {
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.FETCHED_SUCCESSFULLY, accountDetails);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILED, "");
		}
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@DeleteMapping(UrlMappings.DELETE_ARREAR)
	public ResponseEntity<Object> deleteArrear(@PathVariable("id") Long arrearId, @RequestHeader("Authorization") String accessToken){
		Map<String,Object> data = payRollService.deleteArrear(accessToken, arrearId);
		payRollService.flushPayrollCache();
		return ResponseHandler.generateResponse(HttpStatus.OK, true, "Done", data);
	}
	
	@GetMapping(UrlMappings.GET_BU_WISE_REIMBURSEMENT)
	public ResponseEntity<Object> getBuWiseReimbursment(@RequestHeader("Authorization") String accessToken,
			@RequestParam String bu, @RequestParam int month, @RequestParam int year) {
		Map<String, Object> data = payRollService.getBuWiseReimbursment(accessToken, bu, month + 1, year);
		if (data != null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.FETCHED_SUCCESSFULLY, data);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILED, "");
		}
	}
	
	@GetMapping(UrlMappings.FLUSH_PAYROLL_CACHE)
	public ResponseEntity<Object> flushPayrollCache(@RequestHeader("Authorization") String accessToken) {
		payRollService.flushPayrollCache();
		payRollService.flushTimesheetCache();
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.FETCHED_SUCCESSFULLY, true);
	}
	
	/**
	 * @author pankaj
	 * 
	 * set bu_head_payroll_approval and comment to payRoll
	 * 
	 * @param accessToken
	 * @param userId
	 * @param month
	 * @param year
	 * @param buPayrollApproval
	 */
	@PostMapping(UrlMappings.SET_BU_HEAD_PAYROLL_APPROVAL)
	public ResponseEntity<Object> setPayRollApproval(@RequestHeader("Authorization") String accessToken,@RequestParam Long userId, @RequestParam int month, @RequestParam int year,@RequestParam boolean status,String comment){
		boolean response= payRollService.setPayRollApproval(userId, month+1, year,status,comment);
		if(response) {
			payRollService.flushPayrollCache();
			return ResponseHandler.generateResponse(HttpStatus.OK,true, "Data Updated Successfully",response);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED,true, "Data Not Updated",response);
	}

	/**
	 * @author pankaj
	 * 
	 * show comment if bu_head_payroll_approval true
	 * @header accessToken
	 * @param userId
	 * @param month
	 * @param year
	 */
	@GetMapping(UrlMappings.GET_BU_APPROVAL_COMMENT)
	public ResponseEntity<Object> getBuAppovalComment(@RequestHeader("Authorization") String accessToken,@RequestParam Long userId,@RequestParam int month, @RequestParam int year){
		String response =  payRollService.getBuAppovalComment(userId,month+1,year);
		if(Objects.nonNull(response))
			return ResponseHandler.generateResponse(HttpStatus.OK,true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY,response);
		else if(!Objects.nonNull(response))
			return ResponseHandler.generateResponse(HttpStatus.OK,true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY,response);
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED,true, response,response);
	}

	

/**
	 * @author pankaj
	 * 
	 * @header accessToken
	 * @param month
	 * @param year
	 * @return map(id,comment and status)
	 */
		@GetMapping(UrlMappings.GET_PAYROLL_STATUS_LIST)
		public ResponseEntity<Object> getPayrollStatusList(@RequestHeader ("Authorization") String accessToken,@RequestParam  int month,@RequestParam int year ){
			List<Map<String,Object>> response = payRollService.getPayrollStatusList(accessToken, month+1,year);
			if(!response.isEmpty()) 
				return ResponseHandler.generateResponse(HttpStatus.OK,true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY,response);
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED,true, "Exception Failed",response);
		}
	
	
	
	
		
		/**
		 *
		 * @author pankaj
		 * 
		 * @header accessToken
		 * @param userId
		 * @param month
		 * @param year
		 * @return boolean
		 *
		 * 
		 * @apiNote This Api is used to send Email triggered by Resourcing team to Bu Head ,seeking approval for payroll generation in case of Non-Compliant
		 */
		@GetMapping(UrlMappings.SEND_BU_HEAD_APPROVAL_MAIL)
		public ResponseEntity<Object> sendMailForPayrollGenerationOfNonCompliantUser(@RequestHeader("Authorization") String accessToken, 
				@RequestParam long userId,@RequestParam int month,@RequestParam int year) {
			boolean response = payRollService.sendMailForPayrollGenerationOfNonCompliantUser(accessToken, userId,month + 1, year);
			if(response) {
				return ResponseHandler.generateResponse(HttpStatus.OK, true, "Mail send", response);	
			}
			else {
				return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, true, "Mail not send", response);
			}
			
		}
		
		
		/**
		 * @author pankaj
		 * 
		 * Verifies the payroll
		 *  
		 * @param accessToken
		 * @param month
		 * @param year
		 * @param userIds
		 * @return list 
		 */
		@PostMapping(UrlMappings.VERIFY_TIMESHEET)
		ResponseEntity<Object> timesheetComplianceVerify(@RequestHeader("Authorization") String accessToken, @RequestParam int month,@RequestParam int year,@RequestParam List<Integer> userIds) {
			List<TimesheetCompVerification> payrollVerifiedUsers = payRollService.timesheetComplianceVerify(accessToken, month+1, year,userIds);
			if (!payrollVerifiedUsers.isEmpty()) {
				payRollService.flushPayrollCache();
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.ATTENDANCE_VERIFIED,payrollVerifiedUsers);
			}else
				return ResponseHandler.errorResponse(ConstantUtility.ALL_USERS_VERIFIED, HttpStatus.EXPECTATION_FAILED);
		}	
		
		/**
		 *@author pankaj
		 *
		 * Get all the data of a user for whom the mail to seek approval has been send in previous months
		 *
		 *@param userId
		 *@return List<Map<String,Object>>
		 */
		@GetMapping(UrlMappings.GET_TIMESHEET_MAIL_HISTORY)
		ResponseEntity<Object> getTimeSheetMailHistory(@RequestHeader ("Authorization") String accessToken, @RequestParam  long userId){
			List<Map<String,Object>> data = payRollService.getTimeSheetMailHistory(userId);
			if(!data.isEmpty()) 
				return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY ,data);
			else if(data.isEmpty()) 
				return ResponseHandler.generateResponse(HttpStatus.OK, true, "No Data Available" ,data);
			return ResponseHandler.errorResponse("Expectation Failed", HttpStatus.EXPECTATION_FAILED);
		}
		
		@GetMapping(UrlMappings.FLUSH_PAYROLL_USER_CACHE)
		public ResponseEntity<Object> flushPayrollUserCache(@RequestHeader("Authorization") String accessToken) {
			payRegisterService.flushUsersCache();
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.FETCHED_SUCCESSFULLY, true);
		}
		
}
	


