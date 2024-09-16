package com.krishna.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.krishna.dto.UserIdsDto;
import com.krishna.dto.invoice.ClientUpdateDto;

// @FeignClient(name = "DashboardLegacy", url = "http://" + "${DASHBOARD_CORE_HOST_ADDRESS}" + ":"
// 		+ "${DASHBOARD_LEGACY_PORT_NUMBER}")
@FeignClient(name = "api-gateway", url = "https://"+"${ENVIRONMENT_URL}"+"/zuul/dashboard_core/")
public interface FeignLegacyInterface {

	public final static String PREFIX = "api/v1";

	@GetMapping(PREFIX + "/widget/getActiveProjectsList")
	public Map<String, Object> getAllActiveProjectList(@RequestHeader("Authorization") String accessToken,
			@RequestParam int month, @RequestParam int year,@RequestParam boolean isProjectCompliance, @RequestParam String projectStatus);

	@GetMapping(PREFIX + "/widget/getActiveProjectForCompliance")
	public Map<String, Object> getActiveProjectForCompliance(@RequestHeader("Authorization") String accessToken,
			@RequestParam int month, @RequestParam int year,@RequestParam boolean isProjectCompliance, @RequestParam String projectStatus);
	
//	@GetMapping("accountUtility/getUserDataForConsolidatedPage")
//	public List<Object> getUserDataForConsolidatedPage(@RequestHeader("Authorization") String accessToken,@RequestParam int month,@RequestParam int year,@RequestParam String userStatus,
//			@RequestParam String businessVertical);
//	
	@GetMapping(PREFIX + "/indirectCost/getBuUsers")
	public Map<String, Object> getBuUsers( @RequestParam(value="month") int month,@RequestParam(value="year") int year,@RequestParam(value="businessVertical") String businessVertical);
	
	@GetMapping(PREFIX + "/ticket/getUserName")
	public Map<String,Object> getUserName(@RequestHeader("Authorization") String accessToken,@RequestParam(value="userId") Long userId);
	
	@GetMapping(PREFIX + "/utility/getUsersByGrade")
	public List<Object> getUsersByGrade(@RequestHeader("Authorization") String accessToken, @RequestParam String grade);
	
	@GetMapping(PREFIX+"/payRegister/userDetailsForPayRoll")
	public Map<String,Object> getAllUsers(@RequestHeader("Authorization") String accessToken,@RequestParam int month,@RequestParam int year,@RequestParam String userStatus);
	
	@GetMapping (PREFIX+"/accountUtility/getUserDetails")
	public Map<String,Object> getUserDetails(@RequestHeader("Authorization")String accessToken,@RequestParam long id);
	
	//checked
	@GetMapping(PREFIX+"/indirectCost/getCompanyExpectedHours")
	public Map<String, Object> getCompanyExpectedHours( @RequestHeader("Authorization") String accessToken,@RequestParam(value="month") int month,@RequestParam(value="year") String year);

	//checked
	@GetMapping(PREFIX+"/user/payrollWorkingDays")
	public Map<String, Object> getpayrollWorkingDays( @RequestHeader("Authorization") String accessToken,@RequestParam(value="month") int month,@RequestParam(value="year") int year);

	@GetMapping(PREFIX+"/user/payrollWorkingDaysList")
	public List<Map<String,Object>> getpayrollWorkingDaysList(@RequestHeader("Authorization") String accessToken,@RequestParam(value="year") Integer year);
	//Checked
	@GetMapping(PREFIX+"/indirectCost/getBuWiseProjects")
	public Map<String, Object> getBuWiseProjects(@RequestHeader("Authorization")String accessToken,@RequestParam String businessVertical,@RequestParam Integer month,@RequestParam Integer year);

	//checked
	@GetMapping(PREFIX+"/indirectCost/getBuProjectIds")
	public Map<String, Object> getBuProjectIds(@RequestHeader("Authorization")String accessToken,@RequestParam(value="businessVertical") String businessVertical,@RequestParam(value="month") int month,@RequestParam(value="year") String year);

	//checked
	@GetMapping(PREFIX+"/indirectCost/getProjectDataById")
	public Map<String, Object> getIndirectCostProjects(@RequestHeader("Authorization")String accessToken,@RequestParam(value="projectId") Long id, @RequestParam(value="month") int month,@RequestParam(value="year") int year);

	//checked
	@GetMapping(PREFIX+"/payRegister/getTimeSheetHours")
	public Map<String,Object> getTimeSheetHours(@RequestHeader("Authorization") String accessToken,@RequestParam(value="month") int month,@RequestParam(value="year") int year,@RequestParam(value="userId") long userId);
	
	//checked 
	@GetMapping(PREFIX+"/utility/gradeWiseExpectedHours")
	public Map<String, Object> gradeWiseExpectedHours(@RequestParam("month") int month, @RequestParam("year") int year);
	
	@GetMapping(PREFIX+"/securityGroupUtility/getAllrolesAndGrades")
	public Map<String, Object> getAllrolesAndGrades(@RequestHeader("Authorization") String accessToken);

	@GetMapping(PREFIX+"/designation/getAllBands")
	public Map<String,Object> getAllBands(@RequestHeader("Authorization") String accessToken); 
	//Consolidated
	@GetMapping(PREFIX+"/accountUtility/getExpectedHours")
	public Map<String,Object> getMarginExpectedHours(@RequestHeader("Authorization") String accessToken,@RequestParam Long projectId,@RequestParam int month,@RequestParam int year);

	@GetMapping(PREFIX+"/indirectCost/getBuProjectsForsnapshots")
	public Map<String, Object> getBuProjectsForsnapshots(@RequestHeader("Authorization") String accessToken,@RequestParam String businessVertical);
	
	@GetMapping(PREFIX+"/indirectCost/getBuProjectsForsnapshotsV2")
	public Map<String, Object> getBuProjectsForsnapshotsV2(@RequestHeader("Authorization") String accessToken);
	
	
	@GetMapping(PREFIX+"/accountUtility/getUserDataForConsolidatedPage")
	public Map<String,Object> getUserDataForConsolidatedPage(@RequestHeader("Authorization") String accessToken, @RequestParam int month,@RequestParam int year, @RequestParam String userStatus,@RequestParam String businessVertical);
	
	@GetMapping( PREFIX + "/accountUtility/getProjectsForInvoicePipeline")
	public Map<String,Object> getProjectsForInvoicePipeline(@RequestParam("month") int month, @RequestParam("year") int year,@RequestParam("projectType") String projectType);

	@GetMapping(PREFIX+"/accountUtility/getProjectDataForComplianceMail")
	public Map<String, Object> getProjectDataForComplianceMail(@RequestHeader("Authorization") String accessToken,@RequestParam int month, @RequestParam int year,@RequestParam Long projectId);
	
	@GetMapping(PREFIX+"/accountUtility/getYearlyProjectDataForComplianceMail")
	public Map<String, Object> getYearlyProjectDataForComplianceMail(@RequestHeader("Authorization") String accessToken, @RequestParam int year,@RequestParam Long projectId);
	
	@GetMapping( PREFIX + "/deliveryTeam/invoicePipeline")
	public Map<String,Object> getDeliveryTeamInvoicePipeline(@RequestHeader("Authorization") String accessToken,@RequestParam("month") int month, @RequestParam("year") int year,@RequestParam("projectType") String projectType,@RequestParam Long teamHeadId,@RequestParam String bu);

	//ProjectInvoice
	@GetMapping(PREFIX+"/user/getUserNameAndDob")
	 public Map<String,Object>getUserNameAndDob(@RequestHeader("Authorization")String authorization);
	
	//Checked
	@GetMapping(PREFIX+"/legalDocuments/legalTeamDataByProjectIds")
	public Map<String, Object> getLegalTeamDataByProjectIds(@RequestHeader("Authorization") String accessToken,
			 @RequestParam List<Long> ids,@RequestParam String buFilter, @RequestParam String projectStatus);
	
	//Checked
	@GetMapping(PREFIX+"/businessVertical/getAllBusinessVertical")
	public Map<String, Object> getBusinessVerticalDetails(@RequestHeader("Authorization") String accessToken);
	
	@GetMapping(PREFIX+"/accountUtility/monthwiseProjectBUData")
	public Map<String, Object> monthwiseProjectBUData(@RequestHeader("Authorization") String accessToken,@RequestParam Integer month,@RequestParam Integer year);
	
	
	@PostMapping(PREFIX+"/accountUtility/addProjectPaymentSettings")
	public Map<String,Object> addProjectPaymentSettings(@RequestHeader("Authorization") String accessToken,@RequestParam(value="projectId") long projectId,@RequestParam(value="modeOfPayment") Long modeOfPayment,@RequestParam(value="paymentTerms") Long paymentTerms,@RequestParam(value="invoiceCycle") Long invoiceCycle);
	
	//Open Api
	@GetMapping(PREFIX+"/portfolio/findProjectDescription")
    public Map<String,Object> getProjectDescription(@RequestParam Long id,@RequestParam(required=false) String name,@RequestParam(required=false) Integer month,@RequestParam(required=false) Integer year);

	//fetch client details by using lead_id
	@GetMapping(PREFIX+"/leads/getClient")
	public Map<String,Object> getClient(@RequestHeader("Authorization") String accessToken, @RequestParam("id") Long id);

	@PostMapping(PREFIX+ "/portfolio/findProjectDescriptionList")
	public List<Map<String, Object>> findProjectDescriptionList(@RequestHeader("Authorization") String accessToken,@RequestBody List<Long> projectIds,@RequestParam(required=false) Integer month,@RequestParam(required=false) Integer year);

	//Open Api
	@GetMapping(PREFIX+"/accountUtility/getProjectDetailsForAccounts")
	public Map<String,Object> getProjectDetails();
	
	@GetMapping(PREFIX+"/ticket/getUserInformation")
	public Map<String,Object> getUserInformation(@RequestHeader("Authorization") String accessToken,@RequestParam(value="userId") Long id);
	
	@GetMapping(PREFIX+"/user/getUserBasicInfo")
	public Map<String,Object> getUserBasicInfo(@RequestHeader("Authorization") String accessToken,@RequestParam(value="userId") Long id);
	
	//Open Api
	@GetMapping("/businessVertical/getOfficeCodeAndVertical")
	public Map<String,Object> getBusinessVerticals(@RequestHeader("Authorization") String accessToken);
	
	//Open Api
	@GetMapping(PREFIX+"/accountUtility/getProjectIdsExceptClosedForAccounts")
	public Map<String,Object> getProjectIdExceptClosed();
	
	@GetMapping(PREFIX+"/accountUtility/sendInvoiceReminderMail")
	public Map<String,Object> sendInvoiceReminderMail(@RequestParam("data")Map<String,Object> data);

	//Doubt
	//Manual
	@PostMapping(PREFIX+"/portfolio/accountsCompliantStatusChange")
	public Map<String,Object> accountsCompliantStatusChange(@RequestHeader("Authorization") String accessToken,
	@RequestParam Long projectId,@RequestParam String comment,
	@RequestParam boolean compliantStatus,@RequestParam String issueType,@RequestParam String data,
	@RequestParam String callFrom);
	
	//Cron
	@PostMapping(PREFIX + "/accounts/accountsCompliantStatusChange")
	public Map<String,Object> accountsCompliantStatusChange(@RequestParam Long projectId,@RequestParam String comment,@RequestParam boolean compliantStatus,@RequestParam String issueType,@RequestHeader("Authorization") String token,@RequestParam String data, @RequestParam String callFrom);

	//Open Api
	@GetMapping(PREFIX+"/utility/getProjectExpectedHours")
	public Map<String, Object> getProjectExpectedHours(@RequestParam Integer month, @RequestParam Long year, @RequestParam List<Long> projectIds);
			
	@GetMapping(PREFIX + "/portfolio/sendMailOnInvoiceStatusChange")
	public Map<String,Object> sendMailOnInvoiceStatusChange(@RequestHeader("Authorization")String accessToken,@RequestParam("data")Map<String,Object> data,@RequestParam String dueDate);

	//OpenApi
	@GetMapping(PREFIX+"/accountUtility/getDatewiseTeamMembers")
	public Map<String,Object> getDatewiseTeamMembers(@RequestParam Long projectId,@RequestParam Long from,@RequestParam Long to);
	
	//OpenApi
	@GetMapping(PREFIX +"/accountUtility/getTokenForCron")
	public Map<String,Object> getTokenForCron(@RequestParam("tokenType") String tokenType);
	
	@GetMapping(PREFIX + "/utility/getStartedProject")
	public List<Object> getMonthlyStartedProjects(@RequestHeader("Authorization")String accessToken, @RequestParam(value="month")int month,@RequestParam(value="year") int year,@RequestParam(value="businessVertical")String businessVertical);
	
	@GetMapping(PREFIX + "/asset/getAssetAnalyticData")
	HashMap<Object,Object> getAssetAnalyticData(@RequestHeader("Authorization")String accessToken,@RequestParam(value="year") Long year, @RequestParam(value="bu") String bu, @RequestParam(value="assetTypeId") Long assetTypeId);
	
	//ProjectMargin
	@GetMapping(PREFIX+"/utility/getMonthlyResources")
	public Map<String, Object> getMonthlyResources(@RequestHeader("Authorization") String accessToken,@RequestParam("year")Integer year,@RequestParam("projectId") Long projectId);
	
	@GetMapping(PREFIX+"/utility/getMonthlyLeaveData")
	public Map<String, Object> getMonthlyLeaveData(@RequestHeader("Authorization") String accessToken,@RequestParam("year")int year,@RequestParam("userId") long userId,@RequestParam("month") int month, @RequestParam("projectId")long projectId);

	@GetMapping(PREFIX+"/utility/getTeamExpectedhours")
	public Map<String, Object> getTeamExpectedHours(@RequestHeader("Authorization") String accessToken,@RequestParam("year") int year,@RequestParam("teamList") List<Object> teamList,@RequestParam("month") int month,@RequestParam("projectId") long projectId);
	
	//AcessUtility
	@GetMapping(PREFIX+"/ticket/getSupervisorList")
    public Map<String,Object> getSupervisorIdList(@RequestHeader("Authorization")String accessToken,@RequestParam Long userId);

	@GetMapping(PREFIX+"/portfolio/getProjectManagedByUserId")
    public Map<String,Object> getManagerByProjectId(@RequestHeader("Authorization")String accessToken,@RequestParam Long projectId);

	
	//PayrollTrends
	@GetMapping(PREFIX+"/utility/getGradeWiseUserList")
	public Map<String, Object> getGradeWiseUserData(@RequestHeader("Authorization") String accessToken,@RequestParam Integer month, @RequestParam Integer year);


	//payRoll Service
	@GetMapping(PREFIX+"/payRegister/userDetailsForPayRoll")
	public Map<String,Object> getAllUsers(@RequestHeader("Authorization") String accessToken,@RequestParam(value="userStatus") String userStatus,@RequestParam(value="month") int month,@RequestParam(value="year") int year);
	
//	@GetMapping(PREFIX+"/attendance/userLeavesdataForPayroll")
//	public Map<String,Object> userLeavesdataForPayroll(@RequestHeader("Authorization")String accessToken,@RequestParam(value="month") int month,@RequestParam(value="year") int year,@RequestParam(value="id")long id);
	
	@PostMapping(PREFIX+"/payRegister/getUserTimesheet")
	public Map<String, Object> getPayrollUsersTimesheet(@RequestHeader("Authorization")String accessToken,@RequestParam("month") Integer month,@RequestParam("year") Integer year);
	
	@GetMapping(PREFIX+"/timeTracker/getUserMonthWiseTimesheetData")
	public Map<String,Object> getUserMonthWiseTimesheetData(@RequestHeader("Authorization") String accessToken,@RequestParam(value="month") Integer month,@RequestParam(value="year") Integer year,@RequestParam Long userId);
	
	@GetMapping("/attendance/userLeavesdataForPayroll")
	public Map<String,Object> userLeavesdataForPayroll(@RequestHeader("Authorization")String accessToken,@RequestParam(value="month") int month,@RequestParam(value="year") int year,@RequestParam(value="id")long id);
	
	@GetMapping("/attendance/getUserLeavesdataForPayrollCalender")
	public Map<String,Object> getUserLeavesdataForPayrollCalender(@RequestHeader("Authorization")String accessToken,@RequestParam(value="month") int month,@RequestParam(value="year") int year,@RequestParam(value="id")long id);
	
	//DeliveryTeam
	@GetMapping(PREFIX+"/deliveryTeam/getProjectData")
	public Map<String,Object> getProjectListByTeamHeadAndBu(@RequestHeader("Authorization")String accessToken,@RequestParam Long teamHeadId,@RequestParam String bu,@RequestParam int year,@RequestParam int month);

	@GetMapping(PREFIX+"/deliveryTeam/getBuWiseProjectDataByTeamHeadList")
	public Map<String,Object> getProjectListByTeamHeadListAndBu(@RequestHeader("Authorization")String accessToken,@RequestParam List<Long> teamHeadIds,@RequestParam String bu,@RequestParam int year,@RequestParam int month);
	
	@GetMapping(PREFIX+"/deliveryTeam/getTeamHeadWiseProjectList")
	public Map<String,Object> getTeamHeadWiseProjectList(@RequestHeader("Authorization")String accessToken,@RequestParam Long teamHeadId,@RequestParam String bu);
	
	@GetMapping(PREFIX+"/deliveryTeam/getDeliveryHeadWiseYearlyProjects")
	public Map<String,Object> getDeliveryHeadWiseYearlyProjects(@RequestHeader("Authorization") String accessToken, @RequestParam String bu ,@RequestParam int year);

	@GetMapping(PREFIX+"/deliveryTeam/getTeamHeadByProjectId")
	public Map<String,Object> getTeamHeadByProjectId(@RequestHeader("Authorization") String accessToken, @RequestParam long projectId);
	
	
	@GetMapping(PREFIX + "/deliveryTeam/getBusinessVerticalData")
	public Map<String,Object> getBusinessVerticalData(@RequestHeader("Authorization")String accessToken,@RequestParam String bu);

	@GetMapping(PREFIX + "/deliveryTeam/getBuWiseProjectList")
	public Map<String,Object> getBuWiseProjectIds(@RequestHeader("Authorization")String accessToken,@RequestParam String bu);
	
	@GetMapping(PREFIX + "/deliveryTeam/getDeliveryTeam")
	public Map<String,Object> getDeliveryTeam(@RequestHeader("Authorization")String accessToken,@RequestParam String bu,@RequestParam  Integer month,@RequestParam  Integer year);

	@GetMapping(PREFIX + "/user/getSupervisorsEmailList")
	public Map<String,Object> getManagerHierarchyEmail(@RequestHeader("Authorization")String accessToken,@RequestParam Long managerId);

	@GetMapping(PREFIX + "/businessVertical/getBuOwnerInfo")
	public Map<String,Object> getBuOwnerInfo(@RequestHeader("Authorization")String accessToken,@RequestParam String buName);
	
	@RequestMapping(method = RequestMethod.POST ,value=PREFIX + "/ticket/getUserNameList")
	public Map<String,Object> getUserNameList(@RequestHeader("Authorization") String accessToken,@RequestBody UserIdsDto userIds);
	
	@GetMapping(PREFIX + "/projectBuDetails")
	public Map<String,Object> getProjectwiseBuDetails(@RequestHeader("Authorization") String accessToken,@RequestParam String businessVertical,@RequestParam(required=false) Integer month,@RequestParam(required=false) Integer year);
	
	@GetMapping(PREFIX+"/deliveryTeam/getClientWiseData")
	public Map<String, Object> getClientWiseData(@RequestHeader("Authorization") String accessToken,@RequestParam String bu);
	
	@GetMapping(PREFIX+"/deliveryTeam/getBuWiseEmployee")
	public Map<String, Object> getBuWiseEmployee(@RequestHeader("Authorization") String accessToken,@RequestParam String bu);
	
	@GetMapping(PREFIX+"/deliveryTeam/getBuWiseProjectIds")
	public Map<String, Object> getBuWiseProjectIdsList(@RequestHeader("Authorization") String accessToken,@RequestParam String businessVertical);
	
	@PutMapping(PREFIX+"/clientManagement/updateClientDataAccounts")
	public Map<String, Object> updateClientDataAccount(@RequestHeader("Authorization") String accessToken, @RequestBody ClientUpdateDto clientUpdateDto);

	@GetMapping(PREFIX+"/yearly/projectData")
	public Map<String,Object> getYearlyProjects(@RequestHeader("Authorization") String accessToken,@RequestParam Integer year,@RequestParam String businessVertical);
	
	@PostMapping(PREFIX+"/projectFeedback/getClientQuickFeedbackStatus")
	public Map<String, Object> getClientQuickFeedbackStatus(@RequestHeader("Authorization") String accessToken, @RequestParam Long projectId, @RequestParam String month, @RequestParam int year);

	@GetMapping(PREFIX+"/accountUtility/getLifetimeProjectDetails")
	public Map<String,Object> getLifetimeProjectDetails(@RequestHeader("Authorization") String accessToken,@RequestParam String projectType, @RequestParam int year, @RequestParam int month);

}
