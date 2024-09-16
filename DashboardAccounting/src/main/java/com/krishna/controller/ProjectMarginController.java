package com.krishna.controller;

import java.text.DateFormatSymbols;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.domain.UserModel;
import com.krishna.security.JwtValidator;
import com.krishna.service.IndirectCostService;
import com.krishna.service.ProjectInvoiceService;
import com.krishna.service.ProjectMarginService;
import com.krishna.service.AccessUtility.AccessUtilityService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

@RestController
public class ProjectMarginController {
	
	@Autowired
	ProjectMarginService projectMarginService;
	
	@Autowired
	IndirectCostService indirectCostService;
	
	@Autowired
	AccessUtilityService utilityService;
	
	@Autowired
	ProjectInvoiceService projectInvoiceService;
	
	@Autowired
	JwtValidator validator;
	
	@GetMapping(UrlMappings.GET_DIRECT_COST)
	public ResponseEntity<Object> getDirectCost(@RequestHeader String authorization, @RequestParam long projectId,
			@RequestParam int month, @RequestParam int year/* , @RequestParam boolean isGradeWise */) {
		boolean isAuthorised=utilityService.hasProjectAccess(projectId, authorization);

		if(isAuthorised) {
		UserModel user=validator.tokenbValidate(authorization);
		boolean isAccountUser=false;
		if(user.getRoles().contains("ROLE_ACCOUNTS") || user.getRoles().contains("ACCOUNTS_ADMIN"))
			isAccountUser=true;
		projectMarginService.flushcurrentMonthMargin();
			Map<String, Object> directCost = projectMarginService.getDirectCost(projectId, month + 1, year,
					authorization, isAccountUser/* , isGradeWise */);
		if(directCost!=null ) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Direct Cost Fetched Successfully", directCost);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unbale to fetch direct cost", null);
		}
		else {
			return ResponseHandler.generateResponse(HttpStatus.FORBIDDEN, false, "You're not authorised to view Margins", null);
		}
	}

	@GetMapping(UrlMappings.PRO_DASHBOARD_GET_DIRECT_COST)
	public ResponseEntity<Object> getDirectCostProDashboard(@RequestHeader String authorization, @RequestParam long projectId,
			@RequestParam int month, @RequestParam int year/* , @RequestParam boolean isGradeWise */) {
		boolean isAuthorised=utilityService.hasProjectAccess(projectId, authorization);

		if(isAuthorised) {
		UserModel user=validator.tokenbValidate(authorization);
		boolean isAccountUser=false;
		if(user.getRoles().contains("ROLE_ACCOUNTS") || user.getRoles().contains("ACCOUNTS_ADMIN"))
			isAccountUser=true;
		projectMarginService.flushcurrentMonthMargin();
			Map<String, Object> directCost = projectMarginService.getDirectCost(projectId, month + 1, year,
					authorization, isAccountUser/* , isGradeWise */);
		if(directCost!=null ) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Direct Cost Fetched Successfully", directCost);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unbale to fetch direct cost", null);
		}
		else {
			return ResponseHandler.generateResponse(HttpStatus.FORBIDDEN, false, "You're not authorised to view Margins", null);
		}
	}
	
	@GetMapping(UrlMappings.GET_BU_MARGIN)
	public ResponseEntity<Object> getBuMargin(@RequestHeader String authorization, @RequestParam String businessVertical,@RequestParam int month,@RequestParam int year
			, @RequestParam(defaultValue = "0", required = false) int billingRateFilter) {
		List<Object> buMarginData=projectMarginService.getBuMargin(businessVertical,month+1,year, billingRateFilter,authorization);
			if(buMarginData!=null) {
				return ResponseHandler.generateResponse(HttpStatus.OK, true, "Margin Data Fetched Successfully", buMarginData);
			}
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unbale to fetch Margin data", null);
	}
	
	@GetMapping(UrlMappings.GET_BU_INDIRECT_COST)
	public ResponseEntity<Object> getBuIndirectCost(@RequestHeader String authorization, @RequestParam String businessVertical,@RequestParam int month,@RequestParam int year) {
			Map<String,Object> buMarginData=projectMarginService.getBuIndirectCost(businessVertical,month+1,year,authorization);
			if(buMarginData!=null) {
				return ResponseHandler.generateResponse(HttpStatus.OK, true, "Indirect Cost Data Fetched Successfully", buMarginData);
			}
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unbale to fetch Indirect Cost data", null);
	}
	
	@GetMapping(UrlMappings.GET_BU_MARGIN_VERTICAL_COST)
	public ResponseEntity<Object> getBUmarginVerticalCost(@RequestHeader String authorization,@RequestParam int month,@RequestParam int year,@RequestParam String businessVertical) {
		String monthName=new DateFormatSymbols().getMonths()[month].toString();
		Map<String,Object> verticalCost=indirectCostService.getVerticalCost(authorization,monthName,Integer.toString(year),businessVertical);
		if(verticalCost!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Vertical Cost Fetched Successfully", verticalCost);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unbale to fetch Vertical cost", null);
	}
	
	@GetMapping(UrlMappings.GET_BU_TOTAL_MARGIN)
	public ResponseEntity<Object> getBuTotalMargin(@RequestHeader String authorization,@RequestParam int month,@RequestParam int year,@RequestParam String businessVertical) {
		Map<String,Object> totalMargin=projectMarginService.getBuTotalMargin(month+1,Integer.toString(year),businessVertical,authorization);
		if(totalMargin!=null) {
			Map<String, Object> response = projectInvoiceService.getAverageDisputedPercentage(new Long(year),businessVertical,authorization);
			Map<String, Object> responseLTM = projectInvoiceService.getLTMDisputedPercentage(new Long(year),businessVertical,authorization,month+1);

			if(totalMargin.containsKey("totalMargin")) {
				Double revenue=new Double(totalMargin.get("overallInvoicesInRupee").toString());
				System.out.println("revenue....."+revenue+"---DisputedPerc---"+response.get("averageDisputedPercentage").toString());
				Double disputedAmount = (new Double(response.get("averageDisputedPercentage").toString()) * revenue)/100;
				Double disputedAmountLTM = (new Double(responseLTM.get("averageDisputedPercentageLTM").toString()) * new Double(responseLTM.get("monthlyExtInvoiceTotal").toString()))/100;
				Boolean isLTM = Boolean.valueOf(totalMargin.get("isLTM").toString());
				Double netMargin = 0D;
				Double netMarginPerc = 0D;
				if(isLTM){
					netMargin = new Double(totalMargin.get("totalMargin").toString())-disputedAmountLTM;
					netMarginPerc = new Double(totalMargin.get("totalMarginPerc").toString())- new Double(responseLTM.get("averageDisputedPercentageLTM").toString());
				}
				else{
					netMargin = new Double(totalMargin.get("totalMargin").toString())-disputedAmount;
					netMarginPerc = new Double(totalMargin.get("totalMarginPerc").toString())- new Double(response.get("averageDisputedPercentage").toString());

				}
				totalMargin.put("disputedAmountYtd", disputedAmount);
				totalMargin.put("disputedAmountLTM", disputedAmountLTM);
				totalMargin.put("disputedPercLTM", new Double(responseLTM.get("averageDisputedPercentageLTM").toString()));
				totalMargin.put("netMargin", netMargin);
				totalMargin.put("netMarginPerc", netMarginPerc);
			}
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Total Bu Margin Fetched Successfully", totalMargin);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unbale to fetch Total Bu Margin", null);
	}
	
	@GetMapping(UrlMappings.GET_LIFETIME_INVOICE)
	public ResponseEntity<Object> getLifeTimeInvoices(@RequestHeader("Authorization") String accessToken, @RequestParam int year, @RequestParam long projectId){
		Map<String,Object> invoices=projectMarginService.getLifeTimeInvoices(year,projectId);
		if(invoices!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Invoices fetched Successfully!", invoices);
		}
		return ResponseHandler.generateResponse(HttpStatus.CONFLICT, false, "Unable to fetch data", invoices);
	}
	
	@GetMapping(UrlMappings.GET_LIFETIME_RESOURCES)
	public ResponseEntity<Object> getLifeTimeResources(@RequestHeader("Authorization") String accessToken, @RequestParam int year, @RequestParam long projectId){
		Map<String,Object> resources=projectMarginService.getLifeTimeRersources(year,projectId,accessToken);
		if(resources!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Resources fetched Successfully!", resources);
		}
		return ResponseHandler.generateResponse(HttpStatus.CONFLICT, false, "Unable to fetch resources", resources);
	}
	
	@GetMapping(UrlMappings.GET_LIFETIME_LEAVES)
	public ResponseEntity<Object> getResourcesAttendance(@RequestHeader("Authorization") String accessToken, @RequestParam int year, @RequestParam long projectId){
		Map<String,Object> resources=projectMarginService.getLifeTimeRersources(year,projectId,accessToken);
		Map<String,Object> leaves=projectMarginService.getResourcesAttendance(year, resources, projectId, accessToken);
		if(leaves!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Leaves fetched Successfully!", leaves);
		}
		return ResponseHandler.generateResponse(HttpStatus.CONFLICT, false, "Unable to fetch leaves", leaves);
	}
	
	@GetMapping(UrlMappings.GET_LIFETIME_EXPECTED_HOURS)
	public ResponseEntity<Object> getLifeTimeExpectedHours(@RequestHeader("Authorization") String accessToken, @RequestParam int year, @RequestParam long projectId){
		Map<String,Object> resources=projectMarginService.getLifeTimeRersources(year,projectId,accessToken);
		Map<String,Object> expectedHours=projectMarginService.getLifeTimeExpectedHours(year, resources, projectId, accessToken);
		if(expectedHours!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Expected Hours fetched Successfully!", expectedHours);
		}
		return ResponseHandler.generateResponse(HttpStatus.CONFLICT, false, "Unable to fetch Expected Hours", expectedHours);
	}
	
	@GetMapping(UrlMappings.GET_LIFETIME_INDIRECTCOST)
	public ResponseEntity<Object> getLifeTimeIndirectCost(@RequestHeader("Authorization") String accessToken, @RequestParam int year, @RequestParam long projectId){
		Map<String,Object> invoices=projectMarginService.getLifeTimeInvoices(year,projectId);
		Map<String,Object> indirectCost=projectMarginService.getLifeTimeIndirectCost(accessToken,year,projectId,invoices);
		if(indirectCost!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Indirect Cost fetched Successfully!", indirectCost);
		}
		return ResponseHandler.generateResponse(HttpStatus.CONFLICT, false, "Unable to fetch Indirect Cost", indirectCost);
	}
	
	@GetMapping(UrlMappings.GET_LIFETIME_GRADEWISECOST)
	public ResponseEntity<Object> getLifeTimeGradewiseCost(@RequestHeader("Authorization") String accessToken, @RequestParam int year, @RequestParam long projectId){
		Map<String,Object> gradewiseIndirectCost=projectMarginService.getLifeTimeGradewiseCost(accessToken,year);
		if(gradewiseIndirectCost!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Gradewise Cost fetched Successfully!", gradewiseIndirectCost);
		}
		return ResponseHandler.generateResponse(HttpStatus.CONFLICT, false, "Unable to fetch Indirect Cost", gradewiseIndirectCost);
	}
	
	@GetMapping(UrlMappings.GET_LIFETIME_MARGIN)
	public ResponseEntity<Object> getLifeTimeMargin(@RequestHeader("Authorization") String accessToken, @RequestParam int year, @RequestParam long projectId){
		
		Map<String,Object> invoices=projectMarginService.getLifeTimeInvoices(year,projectId);
		Map<String,Object> resources=projectMarginService.getLifeTimeRersources(year,projectId,accessToken);
		Map<String,Object> expectedHours=projectMarginService.getLifeTimeExpectedHours(year, resources, projectId,accessToken);
		Map<String,Object> indirectCost=projectMarginService.getLifeTimeIndirectCost(accessToken,year,projectId,invoices);
		Map<String,Object> currentMonthMarginData=projectMarginService.getDirectCost(projectId, LocalDateTime.now().minusMonths(1).getMonthValue(), LocalDateTime.now().minusMonths(1).getYear(), accessToken, true);
		boolean isAuthorised=utilityService.hasProjectAccess(projectId, accessToken);
		if(isAuthorised) {
		Map<String,Object> margin=projectMarginService.getLifeTimeMargin(accessToken,year,projectId,invoices,expectedHours,indirectCost,currentMonthMarginData);
		projectMarginService.flushLifeTimeInvoices();
		projectMarginService.flushLifeTimeExpectedHours();
		projectMarginService.flushLifeTimeResources();
		if(margin!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Margin fetched Successfully!", margin);
		}
		return ResponseHandler.generateResponse(HttpStatus.CONFLICT, false, "Unable to fetch margin", null);
		}
		else
			return ResponseHandler.generateResponse(HttpStatus.FORBIDDEN,false,"You're not Authorized",null);
	}
	
	@GetMapping(UrlMappings.GET_GRADEWISE_COST_BU_MARGIN)
	public ResponseEntity<Object> getGradeBasedIndirectCostBuMargin(@RequestParam int month, @RequestParam int year, @RequestHeader("Authorization") String accessToken, @RequestParam String businessVertical) {
		Map<String, Object> data = projectMarginService.getGradeBasedIndirectCostBuMargin(month + 1, year, accessToken, businessVertical);
		Map<String,Object> totalMargin=projectMarginService.getBuTotalMargin(month+1,Integer.toString(year),businessVertical,accessToken);
		double buAvergeGic=0;
		if(totalMargin!=null && data!=null) {
			double buCost=(double) data.get("totalCcCost");
			double totalIC= (double) totalMargin.get("totalIndirectCost");
			double buSize= (double) totalMargin.get("buSize");
			double nonBillableCostSum = (double) data.get("nonBillableCostSum");
			if(buSize!=0)
			buAvergeGic= Math.round(((totalIC-buCost + nonBillableCostSum)/buSize)*100.00)/100.00;
		}
		if(!data.isEmpty()) {
			if(data!=null)
				data.put("buAvergeGic", buAvergeGic);
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, data);
		}
		return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, data);
	}
	
	@GetMapping(UrlMappings.GET_UIC_PER_SEAT)
	public ResponseEntity<Object> getUicPerSeat(@RequestParam int month, @RequestParam int year, @RequestHeader("Authorization") String accessToken) {
		Map<String, Object> data = projectMarginService.getUicPerSeat(accessToken, month+1, year);
		if(data!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, data);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILED, data);
	}
	
	@GetMapping(UrlMappings.GET_BUSINESS_AMOUNT)
	public ResponseEntity<Object> overallBusinessAmount(@RequestHeader("Authorization") String accessToken,@RequestParam String businessVertical) {
		List<Map<String,Object>> projectsInvoice=projectMarginService.overallBusinessAmount(accessToken,businessVertical);
		if(!projectsInvoice.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Invoices Fetched Successfully", projectsInvoice);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unable to fetch Invoices", projectsInvoice);
	}

	@GetMapping(UrlMappings.GET_BU_BUSINESS_AMOUNT)
	public ResponseEntity<Object> buOverallBusinessAmount(@RequestHeader("Authorization") String accessToken,@RequestParam String businessVertical) {
		List<Map<String,Object>> projectsInvoice=projectMarginService.overallBusinessAmount(accessToken,businessVertical);
		if(!projectsInvoice.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Invoices Fetched Successfully", projectsInvoice);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unable to fetch Invoices", projectsInvoice);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN','ROLE_ANONYMOUS')")
	@PutMapping(UrlMappings.FLUSH_TOTAL_BU_MARGIN)
	public ResponseEntity<Object> flushTotalBuMargins(@RequestHeader("Authorization") String accessToken) {
		projectMarginService.flushTotalBuMargins();
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, null);

	}

	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN','ROLE_ANONYMOUS')")
	@PutMapping(UrlMappings.FLUSH_BU_MARGINS)
	public ResponseEntity<Object> flushBuMargins(@RequestHeader("Authorization") String accessToken) {
		projectMarginService.flushBuMargins();
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, null);

	}

	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN','ROLE_ANONYMOUS')")
	@PutMapping(UrlMappings.FLUSH_LIFETIME_INDIRECT_COST)
	public ResponseEntity<Object> flushLifeTimeIndirectCost(@RequestHeader("Authorization") String accessToken) {
		projectMarginService.flushLifeTimeIndirectCost();
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, null);

	}

	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN','ROLE_ANONYMOUS')")
	@PutMapping(UrlMappings.SAVE_PROJECT_SNAPSHOT_AUTO)
	public ResponseEntity<Object> saveProjectSnapshotAuto(@RequestHeader("Authorization") String accessToken) {
		projectMarginService.saveProjectSnapshotAuto(accessToken);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, null);

	}
	

}
