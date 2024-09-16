package com.krishna.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.Interfaces.ICompanyMarginService;
import com.krishna.domain.UserModel;
import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.dto.InvoiceCycleDto;
import com.krishna.dto.InvoiceFilterDto;
import com.krishna.dto.PaymentModeDto;
import com.krishna.dto.PaymentTermsDto;
import com.krishna.dto.ProjectInvoiceDto;
import com.krishna.schedulers.Scheduler;
import com.krishna.security.JwtValidator;
import com.krishna.service.ProjectInvoiceService;
import com.krishna.service.ProjectMarginService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.MessageUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

@RestController
public class ProjectInvoiceController {

	@Autowired
	ProjectInvoiceService projectInvoiceService;

	@Autowired
	JwtValidator validator;

	@Autowired
	ICompanyMarginService companyMarginService;
	
	@Autowired
	ProjectMarginService projectMarginService;
	
	@Autowired
    Scheduler scheduler;
	
	@Autowired
	FeignLegacyInterface feignLegacyInterface;


//	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@PostMapping(UrlMappings.ADD_PROJECT_INVOICE)
	public ResponseEntity<Object> addProjectInvoice(@RequestBody ProjectInvoiceDto projectInvoiceDto,
			@RequestHeader String authorization) {
		ProjectInvoice response = projectInvoiceService.addProjectInvoice(projectInvoiceDto, authorization);
		if (response!=null){
			projectInvoiceService.flushInvoiceChart();
		    companyMarginService.flushInvoicesCache();
		    projectMarginService.flushTotalBuMargins();
		    projectMarginService.flushBuMargins();
		    projectInvoiceService.flushYtdPerc();
		    projectInvoiceService.flushProjectDetailsCache();
		    projectInvoiceService.flushgetInternalInvoices();
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS,
					response.getId());
		}
		else
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false,
					MessageUtility.getMessage("Please.fill.all.mandatory.fields"), null);
		}


	@PostMapping(UrlMappings.GET_ALL_DATA_ACCOUNTS)
	public ResponseEntity<Object> getInvoiceData(@RequestHeader String authorization,@RequestBody InvoiceFilterDto invoiceFilterDto) {

		UserModel currentLoginUser = validator.tokenbValidate(authorization);
		if((invoiceFilterDto.getBusinessVertical()==null || invoiceFilterDto.getBusinessVertical().equals("")) && !currentLoginUser.getRoles().contains("ROLE_ACCOUNTS") && !currentLoginUser.getRoles().contains("ROLE_ACCOUNTS_ADMIN"))
			return ResponseHandler.errorResponse("Not Authorized", HttpStatus.FORBIDDEN, null);
		List<Map<String,Object>> allProjectData = projectInvoiceService.getProjectDetails(authorization);
		Map<String, Object> response = projectInvoiceService.getInvoiceData(authorization, invoiceFilterDto, allProjectData);
		if (!response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true,
					MessageUtility.getMessage("data.fetched.successfully"), response);
		else
			return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false,
					MessageUtility.getMessage(ConstantUtility.FAILED), null);
	}

	@PostMapping(UrlMappings.GET_ALL_BU_DATA_ACCOUNTS)
	public ResponseEntity<Object> getBuInvoiceData(@RequestHeader String authorization,@RequestBody InvoiceFilterDto invoiceFilterDto) {

		UserModel currentLoginUser = validator.tokenbValidate(authorization);
		if((invoiceFilterDto.getBusinessVertical()==null || invoiceFilterDto.getBusinessVertical().equals("")) && !currentLoginUser.getRoles().contains("ROLE_ACCOUNTS") && !currentLoginUser.getRoles().contains("ROLE_ACCOUNTS_ADMIN"))
			return ResponseHandler.errorResponse("Not Authorized", HttpStatus.FORBIDDEN, null);
		List<Map<String,Object>> allProjectData = projectInvoiceService.getProjectDetails(authorization);
		Map<String, Object> response = projectInvoiceService.getInvoiceData(authorization, invoiceFilterDto, allProjectData);
		if (!response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true,
					MessageUtility.getMessage("data.fetched.successfully"), response);
		else
			return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false,
					MessageUtility.getMessage(ConstantUtility.FAILED), null);
	}

	//@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@PutMapping(UrlMappings.EDIT_PROJECT_INVOICE)
	public ResponseEntity<Object> editProjectInvoice(@RequestBody ProjectInvoiceDto projectInvoiceDto,
			@RequestHeader String authorization) {
		boolean response = projectInvoiceService.editProjectInvoice(projectInvoiceDto, authorization);
		if (response){
		    companyMarginService.flushInvoicesCache();
		    projectMarginService.flushTotalBuMargins();
		    projectMarginService.flushBuMargins();
		    projectInvoiceService.flushInvoiceChart();
		    projectInvoiceService.flushYtdPerc();
		    projectInvoiceService.flushProjectDetailsCache();
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true,
					MessageUtility.getMessage(ConstantUtility.SUCCESS), projectInvoiceDto.getProject());
		}
		else
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false,
					MessageUtility.getMessage(ConstantUtility.FAILED), null);

	}

	@GetMapping("/api/v1/flushYtdPerc")
	public ResponseEntity<Object> flushYtdPerc(){
		projectInvoiceService.flushYtdPerc();
		return ResponseHandler.generateResponse(HttpStatus.OK, true,
		MessageUtility.getMessage(ConstantUtility.SUCCESS), null);
	}
	//@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@DeleteMapping(UrlMappings.DELETE_PROJECT_INVOICE)
	public ResponseEntity<Object> deleteProjectInvoice(@RequestParam Long id, @RequestHeader String authorization) {
		boolean result = projectInvoiceService.deleteProjectInvoice(id, authorization);
		if (result) {
			projectInvoiceService.flushInvoiceChart();
			companyMarginService.flushInvoicesCache();
			projectMarginService.flushTotalBuMargins();
			projectMarginService.flushBuMargins();
			projectInvoiceService.flushYtdPerc();
			 projectInvoiceService.flushgetInternalInvoices();
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DELETED_SUCCESSFULLY, id);
		}
		else
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false,
					MessageUtility.getMessage(ConstantUtility.FAILED), null);
	}

	//@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@PostMapping(UrlMappings.ADD_PAYMENT_MODE)
	public ResponseEntity<Object> addPaymentMode(@RequestBody PaymentModeDto paymentModeDto,
			@RequestHeader String authorization) {
		String response = null;
		response = projectInvoiceService.addPaymentMode(paymentModeDto, authorization);
		if (response.equals("success"))
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.ADDED_SUCCESSFULLY,
					paymentModeDto.getPaymentModeType());
		else if (response.equals(ConstantUtility.ALREADY_EXIST))
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false, ConstantUtility.ALREADY_EXIST,
					null);
		else
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false, ConstantUtility.NON_NULL, null);

	}

	//@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@PutMapping(UrlMappings.EDIT_PAYMENT_MODE)
	public ResponseEntity<Object> editPaymentMode(@RequestBody PaymentModeDto paymentModeDto,
			@RequestHeader String authorization) {
		boolean response = projectInvoiceService.editPaymentMode(paymentModeDto, authorization);
		if (response)
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS,
					paymentModeDto.getPaymentModeType());
		else
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false, ConstantUtility.NON_NULL, null);
	}

	//@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@DeleteMapping(UrlMappings.DELETE_PAYMENT_MODE)
	public ResponseEntity<Object> deletePaymentMode(@RequestParam Long id, @RequestHeader String authorization) {
		boolean result = projectInvoiceService.deletePaymentMode(id, authorization);
		if (result)
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DELETED_SUCCESSFULLY, id);
		else
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false,
					MessageUtility.getMessage(ConstantUtility.FAILED), null);
	}
    
	@GetMapping(UrlMappings.GET_ALL_PAYMENT_MODE)
	public ResponseEntity<Object> getAllPaymentMode(@RequestHeader String authorization) {
		UserModel currentLoginUser = validator.tokenbValidate(authorization);
		List<String> roles = currentLoginUser.getRoles();
		String userGrade = currentLoginUser.getGrade();

			List response = projectInvoiceService.getAllPaymentMode(authorization);
			if (!response.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true,
						MessageUtility.getMessage(ConstantUtility.SUCCESS), response);
			else
				return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false,
						MessageUtility.getMessage(ConstantUtility.FAILED), null);

	}

	@GetMapping(UrlMappings.GET_BU_ALL_PAYMENT_MODE)
	public ResponseEntity<Object> getBuAllPaymentMode(@RequestHeader String authorization) {
		UserModel currentLoginUser = validator.tokenbValidate(authorization);
		List<String> roles = currentLoginUser.getRoles();
		String userGrade = currentLoginUser.getGrade();

			List response = projectInvoiceService.getAllPaymentMode(authorization);
			if (!response.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true,
						MessageUtility.getMessage(ConstantUtility.SUCCESS), response);
			else
				return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false,
						MessageUtility.getMessage(ConstantUtility.FAILED), null);

	}


	//@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@PostMapping(UrlMappings.ADD_INVOICE_CYCLE)
	public ResponseEntity<Object> addInvoiceCycle(@RequestBody InvoiceCycleDto billingCycleDto,
			@RequestHeader String authorization) {
		String response = null;
		response = projectInvoiceService.addInvoiceCycle(billingCycleDto, authorization);
		if (response.equals("success"))
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.ADDED_SUCCESSFULLY,
					billingCycleDto.getInvoiceCycleType());
		else if (response.equals(ConstantUtility.ALREADY_EXIST))
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false, ConstantUtility.ALREADY_EXIST,
					null);
		else
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false, ConstantUtility.NON_NULL, null);
	}

	//@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@PutMapping(UrlMappings.EDIT_INVOICE_CYCLE)
	public ResponseEntity<Object> editInvoiceCycle(@RequestBody InvoiceCycleDto invoiceCycleDto,
			@RequestHeader String authorization) {
		boolean response = projectInvoiceService.editInvoiceCycle(invoiceCycleDto, authorization);
		if (response)
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true,
					MessageUtility.getMessage(ConstantUtility.SUCCESS), invoiceCycleDto.getInvoiceCycleType());
		else
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false, ConstantUtility.NON_NULL, null);
	}

	//@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@DeleteMapping(UrlMappings.DELETE_INVOICE_CYCLE)
	public ResponseEntity<Object> deleteInvoiceCycle(@RequestParam Long id, @RequestHeader String authorization) {
		boolean result = projectInvoiceService.deleteInvoiceCycle(id, authorization);
		if (result)
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DELETED_SUCCESSFULLY, id);
		else
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false, ConstantUtility.FAILED, null);
	}

	@GetMapping(UrlMappings.GET_ALL_INVOICE_CYCLE)
	public ResponseEntity<Object> getAllInvoiceCycle(@RequestHeader String authorization) {
		UserModel currentLoginUser = validator.tokenbValidate(authorization);
		List<String> roles = currentLoginUser.getRoles();
		String userGrade = currentLoginUser.getGrade();
			List response = projectInvoiceService.getAllInvoiceCycle(authorization);
			if (!response.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, response);
			else
				return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, null);
	}

	@GetMapping(UrlMappings.GET_BU_ALL_INVOICE_CYCLE)
	public ResponseEntity<Object> getBuAllInvoiceCycle(@RequestHeader String authorization) {
		UserModel currentLoginUser = validator.tokenbValidate(authorization);
		List<String> roles = currentLoginUser.getRoles();
		String userGrade = currentLoginUser.getGrade();
			List response = projectInvoiceService.getAllInvoiceCycle(authorization);
			if (!response.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, response);
			else
				return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, null);
	}

	@GetMapping(UrlMappings.GET_ALL_INVOICE_STATUS)
	public ResponseEntity<Object> getAllInvoiceStatus(@RequestHeader String authorization) {
		UserModel currentLoginUser = validator.tokenbValidate(authorization);
			List response = projectInvoiceService.getAllInvoiceStatus(authorization);
			if (!response.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, response);
			else
				return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, null);
	}

	@GetMapping(UrlMappings.GET_BU_ALL_INVOICE_STATUS)
	public ResponseEntity<Object> getBuAllInvoiceStatus(@RequestHeader String authorization) {
		UserModel currentLoginUser = validator.tokenbValidate(authorization);
			List response = projectInvoiceService.getAllInvoiceStatus(authorization);
			if (!response.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, response);
			else
				return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, null);
	}

	//@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@PostMapping(UrlMappings.ADD_PAYMENT_TERMS)
	public ResponseEntity<Object> addPaymentTerms(@RequestBody PaymentTermsDto paymentTermsDto,
			@RequestHeader String authorization) {
		String response = null;
		response = projectInvoiceService.addPaymentTerms(paymentTermsDto, authorization);
		if (response.equals(ConstantUtility.SUCCESS))
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.ADDED_SUCCESSFULLY,
					paymentTermsDto.getPaymentTermsType());
		else if (response.equals(ConstantUtility.ALREADY_EXIST))
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false, ConstantUtility.ALREADY_EXIST,
					null);
		else
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false, ConstantUtility.NON_NULL, null);
	}

	//@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@PutMapping(UrlMappings.EDIT_PAYMENT_TERMS)
	public ResponseEntity<Object> editPaymentTerms(@RequestBody PaymentTermsDto paymentTermsDto,
			@RequestHeader String authorization) {
		boolean response = projectInvoiceService.editPaymentTerms(paymentTermsDto, authorization);
		if (response)
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS,
					paymentTermsDto.getPaymentTermsType());
		else
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false, ConstantUtility.NON_NULL, null);
	}

	//@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@DeleteMapping(UrlMappings.DELETE_PAYMENT_TERMS)
	public ResponseEntity<Object> deletePaymentTerms(@RequestParam Long id, @RequestHeader String authorization) {
		boolean result = projectInvoiceService.deletePaymentTerms(id, authorization);
		if (result)
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DELETED_SUCCESSFULLY, id);
		else
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false, ConstantUtility.FAILED, null);
	}

	@GetMapping(UrlMappings.GET_ALL_PAYMENT_TERMS)
	public ResponseEntity<Object> getAllPaymentTerms(@RequestHeader String authorization) {
		UserModel currentLoginUser = validator.tokenbValidate(authorization);
		List<String> roles = currentLoginUser.getRoles();
		String userGrade = currentLoginUser.getGrade();

			List response = projectInvoiceService.getAllPaymentTerms(authorization);
			if (!response.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, response);
			else
				return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, null);

	}

	@GetMapping(UrlMappings.GET_BU_ALL_PAYMENT_TERMS)
	public ResponseEntity<Object> getBuAllPaymentTerms(@RequestHeader String authorization) {
		UserModel currentLoginUser = validator.tokenbValidate(authorization);
		List<String> roles = currentLoginUser.getRoles();
		String userGrade = currentLoginUser.getGrade();

			List response = projectInvoiceService.getAllPaymentTerms(authorization);
			if (!response.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, response);
			else
				return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, null);

	}

	@GetMapping(UrlMappings.GET_ALL_CURRENCIES)
	public ResponseEntity<Object> getAllCurrencies(@RequestHeader String authorization) {
		UserModel currentLoginUser = validator.tokenbValidate(authorization);
		List<String> roles = currentLoginUser.getRoles();
		String userGrade = currentLoginUser.getGrade();

			List response = projectInvoiceService.getAllCurrencies(authorization);
			if (!response.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, response);
			else
				return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, null);

	}

	@GetMapping(UrlMappings.GET_BU_ALL_CURRENCIES)
	public ResponseEntity<Object> getBuAllCurrencies(@RequestHeader String authorization) {
		UserModel currentLoginUser = validator.tokenbValidate(authorization);
		List<String> roles = currentLoginUser.getRoles();
		String userGrade = currentLoginUser.getGrade();

			List response = projectInvoiceService.getAllCurrencies(authorization);
			if (!response.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, response);
			else
				return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, null);

	}

	@GetMapping(UrlMappings.GET_ALL_PROJECT_DETAILS)
	public ResponseEntity<Object> getProjectDetails(@RequestHeader String authorization) {
		UserModel currentLoginUser = validator.tokenbValidate(authorization);
		List<String> roles = currentLoginUser.getRoles();
		String userGrade = currentLoginUser.getGrade();

			List response = projectInvoiceService.getProjectDetails(authorization);
			if (!response.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, response);
			else
				return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, null);

	}

	@GetMapping(UrlMappings.PRO_DASHBOARD_GET_ALL_PROJECT_DETAILS)
	public ResponseEntity<Object> getProjectDetailsProDashboard(@RequestHeader String authorization) {
		UserModel currentLoginUser = validator.tokenbValidate(authorization);
		List<String> roles = currentLoginUser.getRoles();
		String userGrade = currentLoginUser.getGrade();

		List response = projectInvoiceService.getProjectDetails(authorization);
			if (!response.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, response);
			else
				return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, null);

	}

	@GetMapping(UrlMappings.GET_BU_ALL_PROJECT_DETAILS)
	public ResponseEntity<Object> getBuProjectDetails(@RequestHeader String authorization) {
		UserModel currentLoginUser = validator.tokenbValidate(authorization);
		List<String> roles = currentLoginUser.getRoles();
		String userGrade = currentLoginUser.getGrade();

			List response = projectInvoiceService.getProjectDetails(authorization);
			if (!response.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, response);
			else
				return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, null);

	}

	@PreAuthorize("hasRole('DASHBOARD_ADMIN')")
	@PostMapping(UrlMappings.SET_AMOUNT_IN_RUPEE)
	public ResponseEntity<Object> setAmountInRupee(@RequestHeader String authorization) {
		boolean response = projectInvoiceService.setAmountInRupee(authorization);
		if (response)
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, response);
		else
			return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, null);
	}
	
	@GetMapping(UrlMappings.GET_PROJECTWISE_DATA)
	public ResponseEntity<Object> getProjectWiseData(@RequestHeader("Authorization") String authorization, @RequestParam String month,
			@RequestParam String year, @RequestParam Long id) {
		UserModel currentLoginUser = validator.tokenbValidate(authorization);
		List<String> roles = currentLoginUser.getRoles();
		String userGrade = currentLoginUser.getGrade();
		projectInvoiceService.flushProjectDetailsCache();
		Map<String,Object>data = (Map<String, Object>) feignLegacyInterface.getManagerByProjectId(authorization, id).get("data");
		Integer managerId = null;	
		if(data != null)		 
			managerId=	(Integer) data.get("userId");

		List<Long> supervisorsIdList = new ArrayList<>();
		if (managerId != null) {
			if (managerId == currentLoginUser.getUserId()) {
				Map<String, Object> supervisorsList = (Map<String, Object>) feignLegacyInterface.getSupervisorIdList(authorization,
						Long.valueOf(managerId)).get("data") ;
				supervisorsIdList = (List<Long>) supervisorsList.get("userSupervisorList");
				
			}
		}
		if (roles.contains(ConstantUtility.ROLE_ACCOUNTS) || roles.contains("ROLE_ADMIN") || roles.contains("ROLE_PMO")
						|| roles.contains(ConstantUtility.ROLE_ACCOUNTS_ADMIN) || userGrade.equals("M3") || userGrade.equals("C")
						|| userGrade.equals("D") || userGrade.equals("V")
						|| (managerId != null && managerId.toString().equals(String.valueOf(currentLoginUser.getUserId())))
						|| supervisorsIdList.contains(Long.toString(currentLoginUser.getUserId())))  {
			List<Map<String, Object>> allProjectData = projectInvoiceService.getProjectDetails(authorization);
			Map<String, Object> response = projectInvoiceService.getProjectWiseData(authorization, month, year,
					id, allProjectData);
			if (!response.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, response);
			else
				return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, null);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.FORBIDDEN, false, ConstantUtility.ACCESS_DENIED, null);
		}
	}

	@GetMapping(UrlMappings.PRO_DASHBOARD_GET_PROJECTWISE_DATA)
	public ResponseEntity<Object> getProjectWiseDataProDashboard(@RequestHeader("Authorization") String authorization, @RequestParam String month,
			@RequestParam String year, @RequestParam Long id) {
		UserModel currentLoginUser = validator.tokenbValidate(authorization);
		List<String> roles = currentLoginUser.getRoles();
		String userGrade = currentLoginUser.getGrade();
		projectInvoiceService.flushProjectDetailsCache();
		Map<String,Object>data = (Map<String, Object>) feignLegacyInterface.getManagerByProjectId(authorization, id).get("data");
		Integer managerId = null;	
		if(data != null)		 
			managerId=	(Integer) data.get("userId");

		List<Long> supervisorsIdList = new ArrayList<>();
		if (managerId != null) {
			if (managerId == currentLoginUser.getUserId()) {
				Map<String, Object> supervisorsList = (Map<String, Object>) feignLegacyInterface.getSupervisorIdList(authorization,
						Long.valueOf(managerId)).get("data") ;
				supervisorsIdList = (List<Long>) supervisorsList.get("userSupervisorList");
				
			}
		}
		if (roles.contains(ConstantUtility.ROLE_ACCOUNTS) || roles.contains("ROLE_ADMIN") || roles.contains("ROLE_PMO")
						|| roles.contains(ConstantUtility.ROLE_ACCOUNTS_ADMIN) || userGrade.equals("M3") || userGrade.equals("C")
						|| userGrade.equals("D") || userGrade.equals("V")
						|| (managerId != null && managerId.toString().equals(String.valueOf(currentLoginUser.getUserId())))
						|| supervisorsIdList.contains(Long.toString(currentLoginUser.getUserId())))  {
			List<Map<String, Object>> allProjectData = projectInvoiceService.getProjectDetails(authorization);
			Map<String, Object> response = projectInvoiceService.getProjectWiseData(authorization, month, year,
					id, allProjectData);
			if (!response.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, response);
			else
				return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, null);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.FORBIDDEN, false, ConstantUtility.ACCESS_DENIED, null);
		}
	}

	@PreAuthorize("hasRole('ROLE_CLIENT')")
	@GetMapping(UrlMappings.GET_PROJECTWISE_DATA_CLIENT)
	public ResponseEntity<Object> getProjectWiseDataClient(@RequestHeader String authorization, @RequestParam String month,
			@RequestParam String year, @RequestParam long id) {
		List<Map<String, Object>> allProjectData = projectInvoiceService.getProjectDetails(authorization);
		Map<String, Object> response = projectInvoiceService.getProjectWiseData(authorization, month, year,
				id, allProjectData);
		if (!response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, response);
		else
			return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, null);

	}


	/**
	 * @author shivangi
	 * 
	 *         To get all the Business Verticals
	 * 
	 * @param accessToken
	 * @return list of business vertical
	 */
	@RequestMapping(method = RequestMethod.GET, value = UrlMappings.GET_BUSINESS_VERTICAL)
	public ResponseEntity<Object> getBusinessVerticals(@RequestHeader("Authorization") String accessToken) {
		Object businessVericals = projectInvoiceService.getBusinessVerticals(accessToken);
		if (businessVericals != null)
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Fetched Successfully", businessVericals);
		else
			return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.NO_CONTENT);
	}

	/**
	 * @author shivangi
	 * 
	 *         Changes invoice status of Within Due Date projects to Pending if the
	 *         due date exceeds. [CRON]
	 * 
	 * @param accessToken
	 * @param month
	 * @param year
	 * @return list of project Invoices.
	 */
	@RequestMapping(method = RequestMethod.PUT, value = UrlMappings.CHANGE_INVOICE_STATUS)
	public ResponseEntity<Object> changeInvoiceStatus(@RequestHeader("Authorization") String accessToken) {
		List<Map<String,Object>> allProjectData = projectInvoiceService.getProjectDetails(accessToken);
		List<ProjectInvoice> projectInvoices = projectInvoiceService.changeInvoiceStatus(accessToken, 
				allProjectData);
		if (!projectInvoices.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, projectInvoices);
		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.PUT, value = UrlMappings.INVOICE_CREATION_REMINDER)
	public ResponseEntity<Object> invoiceCreationREminder(@RequestHeader("Authorization") String accessToken) {
		boolean projectInvoices = projectInvoiceService.testingCron(accessToken);
		if (projectInvoices) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, projectInvoices);
		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.CONFLICT);
	}

	@GetMapping(UrlMappings.GET_DATA_CHART)
	public ResponseEntity<Object> getDataLineChart(@RequestHeader("Authorization") String accessToken,
			@RequestParam String year, @RequestParam String businessVertical) {
		List<Map<String,Object>> allProjectData = projectInvoiceService.getProjectDetails(accessToken);
		List<HashMap<String, Object>> data = projectInvoiceService.getDataLineChart( allProjectData,year,
				businessVertical);
		if (!data.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, data);
		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.CONFLICT);
	}
	
	//@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_INVOICE_TRENDS)
	public Object getInvoiceTrends(@RequestHeader("Authorization") String accessToken, @RequestParam Long year,
			@RequestParam int month, @RequestParam(required = false) String businessVertical) throws Exception {
		Map<String, Object> response = projectInvoiceService.getInvoiceTrends(accessToken ,businessVertical, month+1, year);
		if (response != null) {
			if(response.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, true, ConstantUtility.SUCCESS, response);

			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, response);

		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.CONFLICT);

	}

	@GetMapping(UrlMappings.GET_BU_INVOICE_TRENDS)
	public Object getBuInvoiceTrends(@RequestHeader("Authorization") String accessToken, @RequestParam Long year,
			@RequestParam int month, @RequestParam(required = false) String businessVertical) throws Exception {
		Map<String, Object> response = projectInvoiceService.getInvoiceTrends(accessToken ,businessVertical, month+1, year);
		if (response != null) {
			if(response.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, true, ConstantUtility.SUCCESS, response);

			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, response);

		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.CONFLICT);

	}

	@GetMapping(UrlMappings.GET_PROJECT_WISE_INVOICE_STATUS)
	public ResponseEntity<Object> getProjectWiseInvoiceStatus(@RequestHeader String authorization, @RequestParam String month,
			@RequestParam String year, @RequestParam long id) {
				
		Map<String, Object> response = projectInvoiceService.getProjectWiseInvoiceStatus(authorization, month, year, id);
			if (!response.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, response);
			else
				return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, null);
		}
	
	@GetMapping(UrlMappings.GET_DISPUTED_INVOICES)
	public Object getDisputedInvoices(@RequestHeader("Authorization") String accessToken, @RequestParam(required = false) String businessVertical) throws Exception {
		Object response = projectInvoiceService.getDisputedInvoices(businessVertical);
		if (response != null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, response);

		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.CONFLICT);

	}
	
	@GetMapping(UrlMappings.GET_DISPUTED_PERCENTAGE)
	public Object getDisputedInvoicePercentage(@RequestHeader("Authorization") String accessToken,@RequestParam(required = false) Integer month,@RequestParam(required = false) Long year) throws Exception {
		Object response = projectInvoiceService.getDisputedInvoicePercentage(month,year);
		if (response != null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, response);

		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.CONFLICT);

	}
	
	@GetMapping(UrlMappings.GET_AVERAGE_DISPUTED_PERCENTAGE)
	public Object getAverageDisputedPercentage(@RequestHeader("Authorization") String accessToken,@RequestParam Long year,@RequestParam String businessVertical)throws Exception {
		Object response = projectInvoiceService.getAverageDisputedPercentage(year,businessVertical,accessToken);
		if (response != null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, response);

		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.CONFLICT);
	}

	@GetMapping(UrlMappings.GET_LTM_DISPUTED_PERCENTAGE)
	public Object getLTMDisputedPercentage(@RequestHeader("Authorization") String accessToken,@RequestParam Long year,@RequestParam String businessVertical, @RequestParam Integer month)throws Exception {
		Object response = projectInvoiceService.getLTMDisputedPercentage(year,businessVertical,accessToken,month);
		if (response != null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, response);

		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.CONFLICT);
	}
	
	@GetMapping(UrlMappings.GET_DISPUTED_INVOICES_OF_PROJECT)
	public Object getDisputedInvoicesOfProject(@RequestHeader("Authorization") String accessToken,@RequestParam Long projectId, @RequestParam Long year)throws Exception {
		Object response = projectInvoiceService.getDisputedInvoicesOfProject(projectId, year);
		if (response != null) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, response);
		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.CONFLICT);

	}
	
	@GetMapping(UrlMappings.GET_ALL_DISPUTED_INVOICES_OF_PROJECT)
	public Object getAllDisputedInvoicesOfProject(@RequestHeader("Authorization") String accessToken)throws Exception {
		List<Map<String,Object>> response = projectInvoiceService.getAllDisputedInvoicesOfProject();
		if (!response.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, response);
		}
		return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.CONFLICT);

	}
	@GetMapping(value = UrlMappings.COMPLIANCE_CRON)
	public Object accountCompliantStatusCron(@RequestHeader("Authorization") String accessToken)
	{
		scheduler.accountsCompliantStatusChange(accessToken);
		return "success";
	}


	@PostMapping(UrlMappings.GET_PROJECT_WISE_INVOICES)
	public ResponseEntity<Object> getProjectWiseInvoices(@RequestHeader String authorization,@RequestBody List<Long> projectIds) {
				ArrayList<HashMap<String, Object>> response = projectInvoiceService.getProjectWiseInvoices(authorization, projectIds);
		if (response != null)
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, response);
			else
				return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.FAILED, null);
		}
	
	
	@GetMapping(value = UrlMappings.GET_PREVIOUS_MONTH_INVOICES)
	public ResponseEntity<Object> getPreviousMonthInvoices(@RequestHeader("Authorization") String accessToken,
			@RequestParam Integer month, @RequestParam String year) {
		List<Map<String, Object>> response = projectInvoiceService.getPreviousMonthInvoices(accessToken, month, year);
		if (!response.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, response);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.FAILED,
					response);
		}
	}

	@GetMapping(value = UrlMappings.PRO_DASHBOARD_GET_PREVIOUS_MONTH_INVOICES)
	public ResponseEntity<Object> getPreviousMonthInvoicesProDashboard(@RequestHeader("Authorization") String accessToken,
			@RequestParam Integer month, @RequestParam String year) {
		List<Map<String, Object>> response = projectInvoiceService.getPreviousMonthInvoices(accessToken, month, year);
		if (!response.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, response);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.FAILED,
					response);
		}
	}

	@GetMapping(value = UrlMappings.GET_BU_PREVIOUS_MONTH_INVOICES)
	public ResponseEntity<Object> getBuPreviousMonthInvoices(@RequestHeader("Authorization") String accessToken,
			@RequestParam Integer month, @RequestParam String year) {
		List<Map<String, Object>> response = projectInvoiceService.getPreviousMonthInvoices(accessToken, month, year);
		if (!response.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, response);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.FAILED,
					response);
		}
	}

	@GetMapping(UrlMappings.YEARLY_DIPUTED_INVOICES)
	public ResponseEntity<Object> projectWiseYearlyDisputedInvoices(@RequestHeader("Authorization") String accessToken,
			@RequestParam int year, @RequestParam String buFilter, @RequestParam String projectStatus, 
			@RequestParam(defaultValue = "5", required = false) Long invoiceStatus) {
		Map<String, Object> responseObj = projectInvoiceService.diputedInvoiceYearly(year, accessToken, buFilter, projectStatus, invoiceStatus, null);
		if(!responseObj.isEmpty()) { 
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, responseObj);
		}
		else {
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, true, ConstantUtility.NO_CONTENT, responseObj);
		}
	}

	@GetMapping(UrlMappings.BU_YEARLY_DIPUTED_INVOICES)
	public ResponseEntity<Object> buProjectWiseYearlyDisputedInvoices(@RequestHeader("Authorization") String accessToken,
			@RequestParam int year, @RequestParam String buFilter, @RequestParam String projectStatus, 
			@RequestParam(defaultValue = "5", required = false) Long invoiceStatus, @RequestParam(required=false) Long teamHeadId) {
		Map<String, Object> responseObj = projectInvoiceService.diputedInvoiceYearly(year, accessToken, buFilter, projectStatus, invoiceStatus, teamHeadId);
		if(!responseObj.isEmpty()) { 
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, responseObj);
		}
		else {
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, true, ConstantUtility.NO_CONTENT, responseObj);
		}
	}
	
	@PostMapping(UrlMappings.CHANGE_INVOICE_STATUS_DISPUTED)
	public ResponseEntity<Object> markInvoiceDisputed(@RequestHeader("Authorization") String accessToken, 
			@RequestParam Long invoiceId, @RequestParam String comments,@RequestParam String invoiceStatus) {
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS,
				projectInvoiceService.markInvoiceDisputed(invoiceId, comments,invoiceStatus));
	}
	
	@GetMapping(UrlMappings.PENDING_INVOICES_OF_PROJECT)
	public ResponseEntity<Object> getPendingInvoicesOfProject(@RequestHeader("Authorization") String accessToken, 
			@RequestParam Long projectId, @RequestParam Long year) {
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, 
				projectInvoiceService.getPendingProjectInvoices(projectId,year));
	}
	
	@GetMapping(UrlMappings.UPDATE_COMPLIANCE_STATUS)
	public ResponseEntity<Object> updateComplianceStatus(@RequestHeader("Authorization") String accessToken, @RequestParam Long invoiceId) {
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, 
				projectInvoiceService.updateComplianceStatus(invoiceId, accessToken));
	}

	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_PAYMENT_MODE_PIECHART)
	public ResponseEntity<Object> getPaymentModeForPieChart(@RequestHeader("Authorization") String accessToken, @RequestParam long fromDate ,@RequestParam long toDate,@RequestParam String businessVertical) {
		List<Map<String, Object>> allProjectData = projectInvoiceService.getProjectDetails(accessToken);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, 
				projectInvoiceService.getPaymentModeForPieChart(accessToken,fromDate,toDate,businessVertical,allProjectData));
	}
	
	@PreAuthorize("hasAnyRole('DASHBOARD_ADMIN','ROLE_ANONYMOUS')")
	@PostMapping(UrlMappings.MARK_INVOICE_DISPUTED)
	public ResponseEntity<Object> markInvoiceDisputed(@RequestHeader("Authorization") String accessToken) {
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, 
				projectInvoiceService.markInvoiceDisputed());
	}
	
	@PostMapping(UrlMappings.SAVE_DISPUTED_COMMENT_FOR_LEGAL)
	public ResponseEntity<Object> saveDisputedCommentForLegalAction(@RequestHeader("Authorization") String accessToken, @RequestParam Long invoiceId, @RequestParam String comment) {
		String result=projectInvoiceService.saveDisputedCommentForLegalAction(invoiceId,comment);
		if(result.equals("Saved Successfully!"))
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, 
				result);
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILED, result);
	}
	
	@GetMapping(UrlMappings.GET_PENDING_INVOICES_DATA)
	public ResponseEntity<Object> getPendingInvoiceData(@RequestHeader("Authorization") String authorization,
			@RequestParam Long projectId) throws Exception {
		List<Map<String, Object>> response = projectInvoiceService.getPendingInvoiceData(projectId);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, response);

	}
	
	@GetMapping(UrlMappings.GET_COUNTRY_WISE_PIE_CHART)
	public ResponseEntity<Object> getCountryWisePieChart(@RequestHeader("Authorization") String accessToken,@RequestParam long fromDate ,@RequestParam long  toDate,@RequestParam String businessVertical
			) throws Exception {
		List<Map<String, Object>> allProjectData = projectInvoiceService.getProjectDetails(accessToken);
		Map<String,Object>response = projectInvoiceService.getCountryWisePieChart(accessToken,fromDate,toDate,allProjectData,businessVertical);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, response);

	}

	@GetMapping(UrlMappings.GET_PROJECT_WISE_TOTAL_REVENUE)
	public ResponseEntity<Object> getProjectWiseTotalRevenue(@RequestHeader("Authorization") String accessToken,
			@RequestParam String month, @RequestParam String year) {
		List<Map<String, Object>> response = projectInvoiceService.getProjectWiseTotalRevenue(accessToken, month, year);
		if (!response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, response);
		else if (response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.NO_DATA_AVAILABLE, response);
	
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILURE,response);
	}

	@GetMapping(UrlMappings.GET_BU_WISE_TOTAL_REVENUE)
	public ResponseEntity<Object> getBuWiseTotalRevenue(@RequestHeader("Authorization") String accessToken,
			@RequestParam List<String> month, @RequestParam String year, @RequestParam String bu,
			@RequestParam String filter) {
		List<Map<String, Object>> response = projectInvoiceService.getBuWiseTotalRevenue(accessToken, month, year, bu,
				filter);
		if (!response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, response);
		else if (response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.NO_DATA_AVAILABLE, response);
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILURE,
					response);
	}

	@GetMapping(UrlMappings.GET_BU_TOTAL_REVENUE)
	public ResponseEntity<Object> getBuTotalRevenue(@RequestHeader("Authorization") String accessToken,
			@RequestParam List<String> month, @RequestParam String year, @RequestParam String bu,
			@RequestParam String filter) {
		List<Map<String, Object>> response = projectInvoiceService.getBuWiseTotalRevenue(accessToken, month, year, bu,
				filter);
		if (!response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, response);
		else if (response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.NO_DATA_AVAILABLE, response);
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILURE,
					response);
	}
	
	@GetMapping(UrlMappings.GET_MANAGER_WISE_QUARTERLY_REVENUE)
	public ResponseEntity<Object> getManagerWiseQuarterlyRevenue(@RequestHeader("Authorization") String accessToken, @RequestParam String quarter, @RequestParam String year){
		List<Map<String, Object>> response = projectInvoiceService.getManagerWiseQuarterlyRevenue(quarter, year);
		if (!response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, response);
		else if (response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.NO_DATA_AVAILABLE, response);
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILURE,
					response);
	}
	
	@GetMapping(UrlMappings.GET_CLIENT_WISE_COUNTRY_LIST)
	public ResponseEntity<Object> getClientWiseCountryList(@RequestHeader("Authorization") String accessToken,@RequestParam String businessVertical){
		List<Map<String, Object>> response = projectInvoiceService.getClientWiseCountryList(accessToken,businessVertical);
		if (!response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, response);
		else if (response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.NO_DATA_AVAILABLE, response);
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILURE,
					response);
	}

	@GetMapping(UrlMappings.PRO_DASHBOARD_GET_CLIENT_WISE_COUNTRY_LIST)
	public ResponseEntity<Object> getClientWiseCountryListProDashboard(@RequestHeader("Authorization") String accessToken,@RequestParam String businessVertical){
		List<Map<String, Object>> response = projectInvoiceService.getClientWiseCountryList(accessToken,businessVertical);
		if (!response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, response);
		else if (response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.NO_DATA_AVAILABLE, response);
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.FAILURE,
					response);
	}
	
	 @GetMapping(UrlMappings.FLUSH_PROJECT_DETAILS_DATA)
	    public ResponseEntity<Object> flushProjectDetailsCache(@RequestHeader("Authorization") String accessToken){
	    	projectInvoiceService.flushProjectDetailsCache();
	        return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, null);

	    }

	@GetMapping(UrlMappings.BU_DISPUTED_INVOICE_LIST)
	public Object getDisputedInvoiceForBu(@RequestHeader("Authorization") String accessToken, @RequestParam String businessVertical, @RequestParam int year) throws Exception {
		List<ProjectInvoice> response = projectInvoiceService.getDisputedInvoiceListForBu(businessVertical,year,accessToken);
			if (response != null) {
				return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, response);
	
			}
			return ResponseHandler.errorResponse(ConstantUtility.FAILED, HttpStatus.CONFLICT);
	
	}
}
