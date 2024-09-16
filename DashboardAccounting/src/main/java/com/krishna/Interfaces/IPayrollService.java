package com.krishna.Interfaces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.krishna.domain.Arrear;
import com.krishna.domain.AttendanceVerification;
import com.krishna.domain.Payroll;
import com.krishna.domain.TimesheetCompVerification;
import com.krishna.dto.ArrearFilesDto;
import com.krishna.dto.PayrollDto;

public interface IPayrollService {

	List<Payroll> generatePayroll(String accessToken, int month, int year, String userStatus);

	Map<String, Object> editPayRoll(String accessToken, PayrollDto payroll, int month, int year);

	Arrear saveArrear(double arrearAmount, int arrearMonth, long userId, int arrearYear, String arrearComment,
			String accessToken, int creationMonth, int creationYear, boolean isArrearIncluded,
			ArrearFilesDto arrearFilesDto, boolean isReimbursement);


	List<Object> getEmployeeArrears(long payrollId, String accessToken, int month, int year, boolean isReimbursement);

	List<Object> getAllArrears(int month, int year, String accessToken, boolean isReimbursement);

	Arrear editArrear(String accessToken, long arrearId, double arrearAmount, int arrearMonth, int arrearYear,String arrearComment, boolean isArrearIncluded, ArrearFilesDto arrearFilesDto, boolean isReimbursement);

	List<Long> getAttendanceVerifiedUsers(String accessToken, int month, int year);

	List<AttendanceVerification> verifyAttendance(String accessToken, int month, int year, List<Integer> userIds);

	String downloadArrearFiles(String accessToken, String filePath, String fileName);

	AttendanceVerification unverifyAttendance(String accessToken, long userId, boolean isAttendanceVerified, int month,int year);

	ArrayList<Object> userLeavesdataForPayroll(String accessToken, long userId, int month, int year);

	boolean sendMailOnNonCompliantTimesheet(String accessToken, long userId, int month, int year);

	String savePayrollComment(String accessToken, long userId, int month, int year, String comment);

	List<Object> getPayrollComments(String accessToken, int month, int year);
	
	public Map<String, Object> getPayRollWidgetsData(int month, int year, String accessToken, String timesheetCompliance, List<Payroll> payrolls) ;
	
	public Map<String, Object> deletePayroll(String accessToken, Long payrollId);

	//void generateMonthlyPayroll(String accessToken);

	Map<String, Object> getArrearWidgetData(int i, int year, String accessToken, boolean isReimbursement);
	
	boolean changePayrollAndPayslipStatus(long payrollId);

	Map<String, Object> getIsAttendanceVerifiedForUser(long userId, int month, int year);
	
	HashMap<String, Object> setPayrollOnPriority(long payrollId, String accessToken);

	String setVerifiedPaidDays(String accessToken, int month, int year, List<Integer> userIds);
	
	boolean resetPayrollPriority(int month, int year);

	void flushPayrollCache();

	List<Payroll> getPayrollsForMonth(int i, int year, String timesheetCompliance, String priority,
			Map<String, Object> timesheets);

	Map<String, Object> getPayrollUsersTimesheet(String accessToken, int i, int year);
	Map<String, Object> getPayrollUsersTimesheetByUserList(String accessToken,int month, int year);

	void flushTimesheetCache();
	
	boolean addPayrollComments(long payrollId, String comments);

	Map<String, Object> getPayrollDetails(long userId, int year, int month, String accessToken);

	Map<String,Object> deleteArrear(String accessToken, Long arrearId);

	Map<String, Object> getBuWiseReimbursment(String accessToken, String bu,int monh ,int year);

	List<Map<Object, Object>> getUserGradeList(String accessToken, int month, int year);
	


	List<Object> getPayrolls(int month, int year, String accessToken, String timesheetCompliance, String priority,
			Map<String, Object> timesheets, List<Payroll> payrolls, List<Object> users);


	String getBuAppovalComment(Long userId, int month, int year);


	
	List<Map<String,Object>> getPayrollStatusList(String accessToken, int month, int year);




	boolean sendMailForPayrollGenerationOfNonCompliantUser(String accessToken , long userId, int month, int year);

	List<TimesheetCompVerification> timesheetComplianceVerify(String accessToken, int month, int year, List<Integer> userIds);

	List<Map<String, Object>> getTimeSheetMailHistory(long userId);
	
	boolean setPayRollApproval(Long userId, int month, int year,boolean status,String comment);

	List<Payroll> getBuWisePayrollsForMonth(int month, int year, List<Long> buUsers, String priority,
			String businessVertical);

	List<Object> getBuWisePayrolls(int month, int year, String accessToken, String timesheetCompliance, String priority,
			Map<String, Object> timesheets, List<Payroll> payrolls, List<Object> usersData, List<Long> buUsers);

}
