package com.krishna.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.domain.ClientModel;
import com.krishna.domain.UserModel;
import com.krishna.dto.SecurityDepositDto;
import com.krishna.security.JwtValidator;
import com.krishna.service.ProjectInvoiceService;
import com.krishna.service.SecurityDepositService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.MessageUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

@RestController
public class SecurityDepositController {
	
	@Autowired
	SecurityDepositService secuirtyDepositService;

	@Autowired
	JwtValidator validator;

	@Autowired
	FeignLegacyInterface feignLegacyInterface;
	
	@Autowired
	ProjectInvoiceService projectInvoiceService;

	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@PostMapping(UrlMappings.ADD_SECURITY_DEPOSIT)
	public ResponseEntity<Object> addProjectInvoice(@RequestBody SecurityDepositDto securityDepositDto,
			@RequestHeader String authorization) {
		boolean response = false;
		response = secuirtyDepositService.addSecurityDeposit(securityDepositDto, authorization);
		if (response)
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS,
					securityDepositDto.getProjectId());
		else
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false,
					MessageUtility.getMessage("Please.fill.all.mandatory.fields"), null);

	}

	@GetMapping(UrlMappings.GET_ALL_SECURITY_DEPOSITS)
	public ResponseEntity<Object> getInvoiceData(@RequestHeader String authorization,
			@RequestParam(required = false) String businessVertical, @RequestParam(required = false) Integer year) {
		List<Map<String, Object>> allProjectData = projectInvoiceService.getProjectDetails(authorization);
		Map<String, Object> listAllUsers = (Map<String, Object>) feignLegacyInterface.getUserNameAndDob(authorization);
		List<Map<String, Object>> UserInfo = (List<Map<String, Object>>) listAllUsers.get(ConstantUtility.DATA_);
		Map<String, Object> response = secuirtyDepositService.getSecurityDepositData(authorization,businessVertical, allProjectData, UserInfo, year);
		if (!response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK, true,
					MessageUtility.getMessage("data.fetched.successfully"), response);
		else
			return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false,
					MessageUtility.getMessage(ConstantUtility.FAILED), null);

	}

	@GetMapping(UrlMappings.GET_BU_ALL_SECURITY_DEPOSITS)
	public ResponseEntity<Object> getBuInvoiceData(@RequestHeader String authorization,
			@RequestParam(required = false) String businessVertical, @RequestParam(required = false) Integer year) {
		List<Map<String, Object>> allProjectData = projectInvoiceService.getProjectDetails(authorization);
		Map<String, Object> listAllUsers = (Map<String, Object>) feignLegacyInterface.getUserNameAndDob(authorization);
		List<Map<String, Object>> UserInfo = (List<Map<String, Object>>) listAllUsers.get(ConstantUtility.DATA_);
		Map<String, Object> response = secuirtyDepositService.getSecurityDepositData(authorization,businessVertical, allProjectData, UserInfo, year);
		if (!response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK, true,
					MessageUtility.getMessage("data.fetched.successfully"), response);
		else
			return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false,
					MessageUtility.getMessage(ConstantUtility.FAILED), null);

	}

	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@PutMapping(UrlMappings.EDIT_SECURITY_DEPOSIT)
	public ResponseEntity<Object> editProjectInvoice(@RequestBody SecurityDepositDto secuirtyDepositDto,
			@RequestHeader String authorization) {
		boolean response = secuirtyDepositService.editSecurityDeposit(secuirtyDepositDto, authorization);
		if (response)
			return ResponseHandler.generateResponse(HttpStatus.CREATED, true,
					MessageUtility.getMessage(ConstantUtility.SUCCESS), secuirtyDepositDto.getProjectId());
		else
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false,
					MessageUtility.getMessage(ConstantUtility.FAILED), null);

	}

	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@DeleteMapping(UrlMappings.DELETE_SECURITY_DEPOSIT)
	public ResponseEntity<Object> deleteProjectInvoice(@RequestParam Long id, @RequestHeader String authorization) {
		Object result = secuirtyDepositService.deleteSecurityDeposit(id, authorization);
		if(result == null)
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false,
					ConstantUtility.USED_IFSD, null);

		else if ((boolean) result)
			return ResponseHandler.generateResponse(HttpStatus.OK, true,ConstantUtility.DELETED_SUCCESSFULLY, id);
		else
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false,
					MessageUtility.getMessage(ConstantUtility.FAILED), null);
	}
	
	@GetMapping(UrlMappings.GET_PROJECTWISE_SECURITY_DATA)
	public ResponseEntity<Object> getProjectWiseSecurityData(@RequestHeader String authorization, 
			@RequestParam Long projectId) {
		UserModel currentLoginUser = validator.tokenbValidate(authorization);
		List<String> roles = currentLoginUser.getRoles();
		String userGrade = currentLoginUser.getGrade();
		Map<String,Object>data = (Map<String, Object>) feignLegacyInterface.getManagerByProjectId(authorization, projectId).get("data");
		Long managerId=	Long.parseLong(data.get("userId").toString());
		List<Long> supervisorsIdList = new ArrayList<>();
		if (managerId != null) {
			if (managerId == currentLoginUser.getUserId()) {
				Map<String, Object> supervisorsList = (Map<String, Object>) feignLegacyInterface.getSupervisorIdList(authorization,
						Long.valueOf(managerId)).get("data") ;
				List<Object> list= (List<Object>) supervisorsList.get("userSupervisorList");
				supervisorsIdList = list.stream()
                .map(obj -> Long.valueOf(obj.toString())).collect(Collectors.toList());
			}
		}
		if (roles.contains("ROLE_ACCOUNTS") || roles.contains("ROLE_ADMIN") || roles.contains("ROLE_PMO")
				|| roles.contains("ROLE_ACCOUNTS_ADMIN") || userGrade.equals("M3") || userGrade.equals("C")
				|| userGrade.equals("D") || userGrade.equals("V") || ((managerId != null) && (managerId.equals(currentLoginUser.getUserId())))
				|| supervisorsIdList.contains(Long.parseLong(Long.toString(currentLoginUser.getUserId())))) {
			Map<String, Object> listAllUsers = (Map<String, Object>) feignLegacyInterface.getUserNameAndDob(authorization);
			List<Map<String, Object>> userInfo = (List<Map<String, Object>>) listAllUsers.get(ConstantUtility.DATA_);
			List<Object> response = secuirtyDepositService.getProjectWiseSecurityData(authorization,
					projectId,userInfo);
			if (!response.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, response);
			else
				return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false,ConstantUtility.FAILED, null);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.FORBIDDEN, false, ConstantUtility.ACCESS_DENIED, null);
		}
	}

	@GetMapping(UrlMappings.PRO_DASHBOARD_GET_PROJECTWISE_SECURITY_DATA)
	public ResponseEntity<Object> getProjectWiseSecurityDataProDashboard(@RequestHeader String authorization, 
			@RequestParam Long projectId) {
		UserModel currentLoginUser = validator.tokenbValidate(authorization);
		List<String> roles = currentLoginUser.getRoles();
		String userGrade = currentLoginUser.getGrade();
		Map<String,Object>data = (Map<String, Object>) feignLegacyInterface.getManagerByProjectId(authorization, projectId).get("data");
		Long managerId=	Long.parseLong(data.get("userId").toString());
		List<Long> supervisorsIdList = new ArrayList<>();
		if (managerId != null) {
			if (managerId == currentLoginUser.getUserId()) {
			Map<String, Object> supervisorsList = (Map<String, Object>) feignLegacyInterface.getSupervisorIdList(authorization,
						Long.valueOf(managerId)).get("data") ;
				List<Object> list= (List<Object>) supervisorsList.get("userSupervisorList");
				supervisorsIdList = list.stream()
                .map(obj -> Long.valueOf(obj.toString())).collect(Collectors.toList());
			}
		}
		if (roles.contains("ROLE_ACCOUNTS") || roles.contains("ROLE_ADMIN") || roles.contains("ROLE_PMO")
				|| roles.contains("ROLE_ACCOUNTS_ADMIN") || userGrade.equals("M3") || userGrade.equals("C")
				|| userGrade.equals("D") || userGrade.equals("V") || ((managerId != null) && (managerId.equals(currentLoginUser.getUserId())))
				|| supervisorsIdList.contains(Long.parseLong(Long.toString(currentLoginUser.getUserId())))) {
			Map<String, Object> listAllUsers = (Map<String, Object>) feignLegacyInterface.getUserNameAndDob(authorization);
			List<Map<String, Object>> userInfo = (List<Map<String, Object>>) listAllUsers.get(ConstantUtility.DATA_);
			List<Object> response = secuirtyDepositService.getProjectWiseSecurityData(authorization,
					projectId,userInfo);
			if (!response.isEmpty())
				return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, response);
			else
				return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false,ConstantUtility.FAILED, null);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.FORBIDDEN, false, ConstantUtility.ACCESS_DENIED, null);
		}
	}

	@PreAuthorize("hasAnyRole('ROLE_CLIENT')")
	@GetMapping(UrlMappings.GET_PROJECTWISE_SECURITY_DATA_CLIENT)
	public ResponseEntity<Object> getProjectWiseSecurityDataClient(@RequestHeader String authorization, 
			@RequestParam long projectId) {
		Map<String, Object> listAllUsers = (Map<String, Object>) feignLegacyInterface.getUserNameAndDob(authorization);
		List<Map<String, Object>> userInfo = (List<Map<String, Object>>) listAllUsers.get(ConstantUtility.DATA_);
		List<Object> response = secuirtyDepositService.getProjectWiseSecurityData(authorization,
				projectId,userInfo);
		return ResponseHandler.generateResponse(HttpStatus.CREATED, true, ConstantUtility.SUCCESS, response);
	}
	
	@GetMapping(UrlMappings.GET_SECURITY_DEPOSITE_STATUS)
    public ResponseEntity<Object> getSecurityDepositeEnumsStatus(@RequestHeader  String authorization){
		List<String> response =secuirtyDepositService.getSecurityDepositeEnumsStatus(authorization);
		if (!response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK, true,ConstantUtility.SUCCESS, response);
		else
			return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT,false,ConstantUtility.FAILED,null);
	}

	@GetMapping(UrlMappings.GET_BU_SECURITY_DEPOSITE_STATUS)
    public ResponseEntity<Object> getBuSecurityDepositeEnumsStatus(@RequestHeader  String authorization){
		List<String> response =secuirtyDepositService.getSecurityDepositeEnumsStatus(authorization);
		if (!response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK, true,ConstantUtility.SUCCESS, response);
		else
			return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT,false,ConstantUtility.FAILED,null);
	}
	
	@GetMapping(UrlMappings.GET_IFSD_ADJUSTED_INVOICES)
    public ResponseEntity<Object> getSecurityDepositDeductions(@RequestHeader  String authorization, @RequestParam Long id){
		List<Map<String,Object>> response =secuirtyDepositService.getSecurityDepositDeductions(authorization,id);
		
		if (!response.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.OK, true,ConstantUtility.SUCCESS, response);
		else
			return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT,false,ConstantUtility.FAILED,null);
	}

}
