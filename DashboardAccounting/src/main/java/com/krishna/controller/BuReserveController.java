package com.krishna.controller;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.Interfaces.ICompanyMarginService;
import com.krishna.domain.ReserveSnapShot;
import com.krishna.domain.Margin.BuReserve;
import com.krishna.dto.ReserveDto;
import com.krishna.repository.BuReserveRepository;
import com.krishna.service.BuReserveService;
import com.krishna.service.BuReserveServiceImpl;
import com.krishna.service.ProjectInvoiceService;
import com.krishna.service.ProjectMarginService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

@RestController
public class BuReserveController {
	@Autowired
	BuReserveService buReserveService;
	
	@Autowired
	BuReserveRepository buReserveRepo;
	
	@Autowired
	BuReserveServiceImpl buReserveServiceImpl;
	
	@Autowired
	ICompanyMarginService companyMarginService;

	@Autowired
	ProjectInvoiceService projectInvoiceService;

	@Autowired
	FeignLegacyInterface feignLegacyInterface;
	
	@Autowired
	ProjectMarginService projectMarginService;
	
	public static final Logger LOGGER = LoggerFactory.getLogger(BuReserveController.class);


	@PostMapping(UrlMappings.BU_RESERVE_UPDATE_DEDUCTED_AMOUNT)
	public ResponseEntity<Object> updateDeductedAmount(@RequestHeader("Authorization") String accessToken,@RequestBody ReserveDto reserveDto) {
		BuReserve bureserve=buReserveRepo.findAllByYearAndBuNameAndMonth(LocalDateTime.now().minusMonths(1).getYear(), reserveDto.getBuName(), LocalDateTime.now().minusMonths(1).getMonthValue());
		if(bureserve!=null ) {
			if(bureserve.getTotalReserve()==0)
				return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Insufficient Reserve!", null);
			else if(((bureserve.getTotalReserve()*50)/100) <reserveDto.getDeductionAmount())
				return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Debiting amount is more than 50%", null);
		}
		Boolean result = buReserveService.updateDeductedAmount(accessToken,reserveDto);
		if(result==true) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS,"Update Success");
		} else {
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILED, "Update Failed");
		}
	}
	
	@GetMapping(UrlMappings.GET_BU_WISE_RESERVE)
	public ResponseEntity<Object> getBuWiseReserve(@RequestHeader("Authorization")String accessToken,@RequestParam String buName,@RequestParam int year) {
		Map<String, Object> result = buReserveService.getBuWiseReserve(accessToken,buName,year);
		if(result!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS,result);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILED, null);
		}
	}	

	@GetMapping(UrlMappings.BU_GET_BU_WISE_RESERVE)
	public ResponseEntity<Object> getBuWiseReserveForBuDashboard(@RequestHeader("Authorization")String accessToken,@RequestParam String buName,@RequestParam int year) {
		Map<String, Object> result = buReserveService.getBuWiseReserve(accessToken,buName,year);
		if(result!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS,result);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILED, null);
		}
	}	
	
	@DeleteMapping(UrlMappings.DELETE_BU_WISE_RESERVE)
	public ResponseEntity<Object> deleteBuReserve(@RequestHeader("Authorization")String accessToken,@RequestParam Long id) {
		Boolean isDeleted = buReserveService.deleteBuReserve(accessToken,id);
		if(isDeleted==true) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DELETED_SUCCESSFULLY,isDeleted);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILED, isDeleted);
		}
	}
	
	@PostMapping(UrlMappings.UPDATE_BU_WISE_REMARKS)
	public ResponseEntity<Object> updateRemarks(@RequestHeader("Authorization") String accessToken,@RequestParam Long id,@RequestParam String remarks) {
		Boolean result = buReserveService.updateRemarks(accessToken,id,remarks);
		if(result==true) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS,result);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILED, result);
		}
	}
	
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_BU_RESERVES)
	public ResponseEntity<Object> getBuReserve(@RequestHeader("Authorization") String accessToken,@RequestParam int month,@RequestParam int year){
		Map<String, Object> invoiceTotal=companyMarginService.getBuWiseInvoiceTotal(month+1,Integer.toString(year),accessToken);
		List<Object> buWiseUsers=companyMarginService.getCompanywiseData(month+1,year,accessToken);
		Map<String,Object> directCostTotal=companyMarginService.getDirectCostBuWise(month+1,year,buWiseUsers,invoiceTotal,accessToken);
		Map<String, Object> companyMargins=companyMarginService.getCompanyMargin(accessToken,directCostTotal,invoiceTotal,month+1,year);
		Map<String, Double> disputedAmount=companyMarginService.getAverageDisputedPercentage(year,accessToken);
		Map<String, Double> disputedAmountLTM=companyMarginService.getLTMBuDisputedPercentage(year,month+1,accessToken);
		Map<String, Object> response= companyMarginService.getBuReserve(month+1,year,companyMargins,disputedAmount,invoiceTotal,directCostTotal,buWiseUsers,disputedAmountLTM);
		if(response!=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Bu Reserve Data fetched successfully", response);
		}
		return ResponseHandler.errorResponse(ConstantUtility.UNABLE_TO_FETCH_DATA, HttpStatus.EXPECTATION_FAILED, null);
	}
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN','ROLE_ANONYMOUS')")
	@GetMapping("/cron/buReserveCron")
	public ResponseEntity<Object> buReserveCrone(@RequestHeader("Authorization") String accessToken){
		LOGGER.debug("Starting Reserve Snapshot Cron...");
		companyMarginService.flushInvoicesCache();
		projectInvoiceService.flushYtdPerc();
		projectMarginService.flushTotalBuMargins();
	    projectMarginService.flushBuMargins();
	    projectInvoiceService.flushProjectDetailsCache();
		List<ReserveSnapShot> reserves=companyMarginService.buReserveCrone(accessToken);
		if(reserves !=null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Snapshot data saved successfully!!", reserves);
		}
		return ResponseHandler.errorResponse(ConstantUtility.UNABLE_TO_FETCH_DATA, HttpStatus.EXPECTATION_FAILED, reserves);
	}
	
	@PostMapping(UrlMappings.DEDUCTION_CRON)
	public ResponseEntity<Object> sendMailOnDeductedAmount(@RequestHeader("Authorization") String accessToken) {
		buReserveServiceImpl.sendMailOnDeductedAmount(accessToken);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, ConstantUtility.SUCCESS);

	}

	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN','ROLE_ANONYMOUS')")
	@GetMapping(UrlMappings.RESERVE_PERCENTAGE_CRON)
	public ResponseEntity<Object> carryForwardReservePercentage(@RequestHeader("Authorization") String accessToken) {
		buReserveServiceImpl.carryForwardReservePercentage(accessToken);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, null);

	}

	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.DATA_CORRECT)
	public ResponseEntity<Object> correctData(@RequestHeader("Authorization") String accessToken) {
		buReserveServiceImpl.correctData();
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, null);

	}

}
