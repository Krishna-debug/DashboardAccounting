package com.krishna.controller;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.Interfaces.ICompanyMarginService;
import com.krishna.domain.GradeBasedIndirectCost;
import com.krishna.domain.IndirectCost;
import com.krishna.domain.UserModel;
import com.krishna.domain.Margin.MarginBasis;
import com.krishna.dto.IndirectCostDto;
import com.krishna.repository.IndirectCostRepository;
import com.krishna.schedulers.Scheduler;
import com.krishna.security.JwtValidator;
import com.krishna.service.IndirectCostService;
import com.krishna.service.ProjectInvoiceService;
import com.krishna.service.ProjectMarginService;
import com.krishna.service.AccessUtility.AccessUtilityService;
import com.krishna.service.util.UtilityService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.MessageUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

@RestController
public class IndirectCostController {
	@Autowired
	IndirectCostService indirectCostService;

	@Autowired
	private JwtValidator validator;
	
	@Autowired
	AccessUtilityService utilityService;
	
	@Autowired
	ICompanyMarginService companyMarginService;
	
	@Autowired
	ProjectMarginService projectMarginService;
	
	@Autowired
	ProjectInvoiceService projectInvoiceService;
	
	@Autowired Scheduler scheduledTasks;
	
	@Autowired
	IndirectCostRepository costRepository;
	
	@Autowired
	UtilityService utilservice;
	
	@Autowired
	FeignLegacyInterface feignLegacyInterface;

	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@PostMapping(UrlMappings.ADD_INDIRECT_COST)
	public ResponseEntity<Object> addIndirectCost(@RequestHeader String authorization,@RequestBody IndirectCostDto indirectCostDto) {
		UserModel user = validator.tokenbValidate(authorization);

		if (user != null) {
			String monthName=indirectCostDto.getMonth().getMonths();
			int monthNum=indirectCostService.getMonthNumber(monthName);
			YearMonth yearMonth=YearMonth.of(Integer.parseInt(indirectCostDto.getYear()), monthNum);
			YearMonth mayMonth=YearMonth.of(2020, 5);
			if(yearMonth.isBefore(mayMonth)) {
				return ResponseHandler.errorResponse("You can not generate Indirect Cost befre May 2020!", HttpStatus.EXPECTATION_FAILED, null);
			}
			else {
			IndirectCost indirectCost = indirectCostService.addIndirectCost(indirectCostDto, user.getUserId(),authorization);
			if(indirectCost == null) {
				return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, MessageUtility.getMessage("Invalid.month.year"), indirectCost);
			}
			companyMarginService.flushDirectCostCache();
			projectMarginService.flushTotalBuMargins();
		    projectMarginService.flushBuMargins();
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.ISSUCCESS, indirectCost);
			}
		} else {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, ConstantUtility.INVALID_TOKEN, null);
		}
	}

	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_INDIRECT_COSTS)
	public ResponseEntity<Object> getIndirectCost(@RequestHeader String authorization, @RequestParam String year,@RequestParam(required=false) String businessVertical) {
		UserModel user = validator.tokenbValidate(authorization);
		if (user != null) {
				List<Map<String, Object>> indirectCostList = indirectCostService.getIndirectCost(authorization,year,businessVertical);
				if(indirectCostList!=null)
					return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.ISSUCCESS, indirectCostList);
				else
					return ResponseHandler.errorResponse("Unable to fetch data", HttpStatus.EXPECTATION_FAILED);
			} else {
				return ResponseHandler.generateResponse(HttpStatus.FORBIDDEN, false,
						MessageUtility.getMessage("access.denied"), null);
			}
	}

	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@PutMapping(UrlMappings.UPDATE_INDIRECT_COSTS)
	public ResponseEntity<Object> updateIndirectCost(@RequestHeader String authorization,@RequestParam Double totalInfraCost, @RequestParam Double totalVariableCost, @RequestParam Long costId, @RequestParam Double reimbursement) {
		UserModel user = validator.tokenbValidate(authorization);
		Boolean isDashboardAdmin=false;
			if(user != null && user.getRoles().contains(ConstantUtility.ROLE_DASHBOARD_ADMIN))
				isDashboardAdmin=true;
		Optional<IndirectCost> data = costRepository.findById(costId);
		IndirectCost indirectCost = data.isPresent()? data.get():null;
			int month=13;
			if(indirectCost!=null){
				month=indirectCostService.getMonthNumber(indirectCost.getMonth().getMonths());
			if(indirectCost!=null && month<=12) {
			YearMonth indirectCostObj=YearMonth.of(Integer.parseInt(indirectCost.getYear()) ,month);
			YearMonth currObj=YearMonth.of(LocalDateTime.now().minusMonths(1).getYear(),LocalDateTime.now().minusMonths(1).getMonthValue());
			if(!isDashboardAdmin && indirectCostObj.isBefore(currObj))
				return ResponseHandler.errorResponse("Cost has been frozen for this month", HttpStatus.NOT_ACCEPTABLE, null);
			}
			IndirectCost updatedIndirectCost = indirectCostService.updateIndirectCost(totalInfraCost, totalVariableCost, costId,user.getUserId(),reimbursement);
			if (updatedIndirectCost != null) {
			    	companyMarginService.flushDirectCostCache();
			    	projectMarginService.flushTotalBuMargins();
				    projectMarginService.flushBuMargins();
				return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.ISSUCCESS,updatedIndirectCost);
			}
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false,MessageUtility.getMessage("duplicate.entry"), null);
		}
		return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, ConstantUtility.INVALID_TOKEN, null);
	}

	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@DeleteMapping(UrlMappings.DELETE_INDIRECT_COST)
	public ResponseEntity<Object> deleteIndirectCost(@RequestHeader String authorization, @RequestParam Long costId) {
		UserModel user = validator.tokenbValidate(authorization);
		if (user != null) {
				IndirectCost indirectCost = indirectCostService.deleteIndirectCost(costId);
				if (indirectCost != null) {
					companyMarginService.flushDirectCostCache();
					projectMarginService.flushTotalBuMargins();
				    projectMarginService.flushBuMargins();
					return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.ISSUCCESS,indirectCost);
				}
				return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, ConstantUtility.NOT_FOUND, null);
		}
		return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, ConstantUtility.INVALID_TOKEN, null);
	}

	@GetMapping(UrlMappings.GRAPH_DATA)
	public ResponseEntity<Object> graphData(@RequestHeader String authorization, @RequestParam String year) {
		UserModel user = validator.tokenbValidate(authorization);
		if (user != null) {
			List<String> roles = user.getRoles();
			String userGrade = user.getGrade();
			if (roles.contains("ROLE_ACCOUNTS") || roles.contains("ROLE_ACCOUNTS_ADMIN") || userGrade.equals("M3")
					|| userGrade.equals("C") || userGrade.equals("D") || userGrade.equals("V")) {
				Map<String, List<?>> graph = indirectCostService.graphData(year);
				return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.ISSUCCESS, graph);
			}
			return ResponseHandler.generateResponse(HttpStatus.FORBIDDEN, false, MessageUtility.getMessage("access.denied"), null);
		}
		return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, ConstantUtility.INVALID_TOKEN, null);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_STAFF_COST_PROJECTS)
	public ResponseEntity<Object> getStaffCostProjects(@RequestHeader String authorization,@RequestParam String month,@RequestParam String year) {
		Map<String,Object> staffCost=indirectCostService.getVerticalProjects(authorization,"Operations Support",month,year);
		if(staffCost!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Staff Cost Fetched Successfully", staffCost);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unbale to fetch direct cost", null);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_INFRA_COST)
	public ResponseEntity<Object> getInfraCost(@RequestHeader String authorization,@RequestParam String month,@RequestParam String year) {
		List<Object> infraCost=indirectCostService.getInfraCost(authorization,month,year);
		if(infraCost!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Infra Cost Fetched Successfully", infraCost);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.UNABLE_TO_FETCH_COST, null);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_VARIABLE_COST)
	public ResponseEntity<Object> getVariableCost(@RequestHeader String authorization,@RequestParam String month,@RequestParam String year) {
		List<Object> infraCost=indirectCostService.getVariableCost(authorization,month,year);
		if(infraCost!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Variable Cost Fetched Successfully", infraCost);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unbale to fetch Variable cost", null);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_REIMBURSEMENT)
	public ResponseEntity<Object> getReimbursement(@RequestHeader String authorization,@RequestParam String month,@RequestParam String year) {
		List<Object> reimbursement=indirectCostService.getReimbursement(authorization,month,year);
		if(reimbursement!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Reimbursement Cost Fetched Successfully", reimbursement);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unbale to fetch Variable cost", null);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_VERTICAL_COST)
	public ResponseEntity<Object> getVerticalCost(@RequestHeader String authorization,@RequestParam String month,@RequestParam String year,@RequestParam String businessVertical) {
		Map<String,Object> verticalCost=indirectCostService.getVerticalCost(authorization,month,year,businessVertical);
		if(verticalCost!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Vertical Cost Fetched Successfully", verticalCost);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unbale to fetch Vertical cost", null);
	}
	
//	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_BU_INDIRECT_TOTAL)
	public ResponseEntity<Object> getBuIndirectTotal(@RequestHeader String authorization,@RequestParam String month,@RequestParam String year,@RequestParam String businessVertical) {
//		boolean hasMarginAccess=utilityService.hasMarginAccess(businessVertical, authorization);
//		if(hasMarginAccess) {	
			List<Object> indirectTotalDetails=indirectCostService.getBuIndirectTotal(authorization,month,year,businessVertical);
			if(indirectTotalDetails!=null) {
				return ResponseHandler.generateResponse(HttpStatus.OK, true, "Bu Indirect Total Fetched Successfully", indirectTotalDetails);
			}
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unbale to fetch Bu Indirect Total", null);
//		}
//		else {
//			return ResponseHandler.errorResponse(ConstantUtility.ACCESS_DENIED, HttpStatus.FORBIDDEN);
//		}
	}
	
//	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_BU_ALL_PROJECTS)
	public ResponseEntity<Object> getBuAllProjects(@RequestHeader String authorization,@RequestParam String month,@RequestParam String year,@RequestParam String businessVertical) {
//		boolean hasMarginAccess=utilityService.hasMarginAccess(businessVertical, authorization);
//		if(hasMarginAccess) {	
			Map<String,Object> projectsData=indirectCostService.getVerticalProjects(authorization,businessVertical,month,year);
			if(projectsData!=null) {
				return ResponseHandler.generateResponse(HttpStatus.OK, true, "Projects Fetched Successfully", projectsData);
			}
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unbale to fetch Projects", null);
//		}
//		else {
//			return ResponseHandler.errorResponse(ConstantUtility.ACCESS_DENIED, HttpStatus.FORBIDDEN);
//		}
	}
	
//	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_BU_BILLABLE_PROJECTS)
	public ResponseEntity<Object> getBuBillableProjects(@RequestHeader String authorization,@RequestParam String month,@RequestParam String year,@RequestParam String businessVertical) {
//		boolean hasMarginAccess=utilityService.hasMarginAccess(businessVertical, authorization);
//		if(hasMarginAccess) {	
			List<Object> projectsData=indirectCostService.getBuBillableProjects(authorization,businessVertical,month,year);
			if(projectsData!=null) {
				return ResponseHandler.generateResponse(HttpStatus.OK, true, "Billable Projects Fetched Successfully", projectsData);
			}
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unbale to fetch Billable Projects", null);
//		}
//		else {
//			return ResponseHandler.errorResponse(ConstantUtility.ACCESS_DENIED, HttpStatus.FORBIDDEN);
//		}
	}
	
	@GetMapping(UrlMappings.GET_PER_PERSON_HOURS)
	public ResponseEntity<Object> getPerPersonHours(@RequestHeader String authorization,@RequestParam String month,@RequestParam String year) {
		Double hours=indirectCostService.getPerPersonHours(authorization,month,year);
		if(hours!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Per Person Hours Fetched Successfully", hours);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unbale to fetch Per Person hours", null);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_ENCRYPTED_MONTHLY_PAY)
	public ResponseEntity<Object> getEarnedMonthlyPay(@RequestHeader("Authorization") String accessToken, 
			@RequestParam int month, @RequestParam int year, @RequestParam long userId) {
		HashMap<String, Object> encryptedPay = indirectCostService.getEarnedMonthlyPay(accessToken, month + 1, year, userId); 
		if(encryptedPay != null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Fetched successfully", encryptedPay);
		}
		return ResponseHandler.errorResponse("Unable to fetch Data", HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@PostMapping(UrlMappings.SAVE_FIXED_COST)
	public ResponseEntity<Object> saveFixedCost(@RequestHeader("Authorization") String accessToken, @RequestParam int year, @RequestParam int month,
			@RequestParam double fixedCost, @RequestParam String grade) {
		GradeBasedIndirectCost indirectCost = indirectCostService.saveFixedCostOfGrade(grade, accessToken, fixedCost, month +1, year);
		companyMarginService.flushDirectCostCache();
		projectMarginService.flushTotalBuMargins();
	    projectMarginService.flushBuMargins();
		return ResponseHandler.generateResponse(HttpStatus.CREATED, true, "Saved or Updated", indirectCost);
	}
	
	
	@GetMapping(UrlMappings.SET_MARGIN_BASIS)
	public ResponseEntity<Object> setMarginBasis(@RequestParam int month, @RequestParam int year, @RequestHeader("Authorization") String accessToken, @RequestParam boolean isGradeWise) {
		MarginBasis data = indirectCostService.setMarginBasis(month + 1, year, isGradeWise);
		if(data!=null) {
			projectInvoiceService.flushInvoiceChart();
			companyMarginService.flushDirectCostCache();
			projectMarginService.flushTotalBuMargins();
		    projectMarginService.flushBuMargins();
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, data);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILED, data);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_ALL_INDIRECTCOST_GRADE_BASED)
	public ResponseEntity<Object> getAllGradeBasedIndirectCost(@RequestParam int month, @RequestParam int year, @RequestHeader("Authorization") String 
			accessToken) {
		Map<String, Object> data = indirectCostService.getGradeWiseIndirectCost(month + 1, year, accessToken);
		if(!data.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, data);
		}
		return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, data);
	}
	
	//@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_MARGIN_BASIS)
	public ResponseEntity<Object> getMarginBasis(@RequestHeader("Authorization") String accessToken, 
			@RequestParam int month, @RequestParam int year) {
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, 
				indirectCostService.getMarginBasis(month + 1, year));
	}

	//@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.BU_GET_MARGIN_BASIS)
	public ResponseEntity<Object> getBuMarginBasis(@RequestHeader("Authorization") String accessToken, 
			@RequestParam int month, @RequestParam int year) {
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, 
				indirectCostService.getMarginBasis(month + 1, year));
	}
	
	@PreAuthorize("hasAnyRole('ROLE_DASHBOARD_ADMIN')")
	@GetMapping(UrlMappings.CARRY_FORWARD_GRADE_IC)
	public ResponseEntity<Object> carryForwardGradeWiseIC(@RequestHeader("Authorization") String accessToken, 
			@RequestParam int month, @RequestParam int year) {
		scheduledTasks.carryForwardPreviousMonthGICForTesting(month+1,year);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, "");
	}
	
	@GetMapping(UrlMappings.GIC_YEARLY)
	public ResponseEntity<Object> getMonthWiseYearlyGIC(@RequestHeader("Authorization") String accessToken, 
			@RequestParam int year) {
		Map<Integer, Object> yearlyStatics = indirectCostService.getMonthWiseGICByYear(year);
		if(!yearlyStatics.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, yearlyStatics);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.NO_GIC_FOR_YEAR, "");
		}
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_ASSET_COST)
	public ResponseEntity<Object> getAssetCost(@RequestHeader String authorization,@RequestParam String month,@RequestParam String year,@RequestParam String businessVertical) {
		List<Object> infraCost=indirectCostService.getAssetCost(authorization,month,year,businessVertical);
		if(infraCost!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Infra Cost Fetched Successfully", infraCost);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.UNABLE_TO_FETCH_COST, null);
	}

	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN','ROLE_ANONYMOUS')")
	@GetMapping(UrlMappings.PREVIOUS_MONTH_GIC)
	public ResponseEntity<Object> carryForwardPreviousMonthGIC(@RequestHeader String authorization){
		scheduledTasks.carryForwardPreviousMonthGIC();
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, null);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping("/api/v1/monthwiseCost")
	public ResponseEntity<Object> getAssetCost(@RequestHeader String authorization,@RequestParam Integer month,@RequestParam String year,@RequestParam String businessVertical) {
		Map<String, Object> userListAndCount = feignLegacyInterface.gradeWiseExpectedHours(month, Integer.parseInt(year));

		Map<String, Double> gradeWiseCosts = utilservice.getGradeWiseCosts(month, Integer.parseInt(year), authorization, "",userListAndCount);
		Double buCost=indirectCostService.buIndirectCost(authorization, businessVertical, month, year,gradeWiseCosts);
		if(buCost!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "IndirectCost Cost Fetched Successfully", buCost);
		}
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.UNABLE_TO_FETCH_COST, null);
	}

	
}