package com.krishna.service.AccessUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.krishna.controller.FeignLegacyInterface;
import com.krishna.domain.UserModel;
import com.krishna.security.JwtValidator;
import com.krishna.service.LoginUtiltiyService;
import com.krishna.util.ConstantUtility;

@Service
public class AccessUtilityService {

	@Autowired
	public JwtValidator validator;
	
	@Autowired
	LoginUtiltiyService loginUtilityService;
	
	@Autowired
	FeignLegacyInterface feignLegacyInterface;
	
	public boolean hasProjectAccess(Long projectId,String accessToken) {
		boolean isAuthorised=false;
		UserModel currentUser=validator.tokenbValidate(accessToken);
		List<String> roles=currentUser.getRoles();
		Map<String, Object> projectDetails = (Map<String, Object>) feignLegacyInterface.getProjectDescription(projectId,"",null,null).get("data");

		Long managerId=(long) 0;
		Long teamHeadId = 0L;
		List<Object> superVisorListData=new ArrayList<>();
		if(projectDetails.containsKey("managerId")) {
			managerId= Long.parseLong(projectDetails.get("managerId").toString());
			Map<String,Object>superVisorMapData=(Map<String, Object>) feignLegacyInterface.getSupervisorIdList(accessToken, managerId).get("data") ;
			superVisorListData=(List<Object>) superVisorMapData.get("userSupervisorList") ;
		}
		if(projectDetails.containsKey("deliveryTeamHeadId")) {
			teamHeadId = Long.parseLong(projectDetails.get("deliveryTeamHeadId").toString());
		}
		boolean isSup=false;
		for(Object sup:superVisorListData) {
			if(sup.toString().equals(Long.toString(currentUser.getUserId()))) 
					isSup=true;
		}
		if(managerId==currentUser.getUserId() || roles.contains(ConstantUtility.ROLE_ACCOUNTS) || roles.contains(ConstantUtility.ROLE_ACCOUNTS_ADMIN) || isSup || teamHeadId.longValue() == currentUser.getUserId())  
			isAuthorised=true;
		return isAuthorised;
	}

	public boolean billingRateAccess(String accessToken, Long projectId){
		boolean isAuthorised=false;
		UserModel currentUser=validator.tokenbValidate(accessToken);
		List<String> roles=currentUser.getRoles();
		Map<String,Object> deliveryHeadMap = feignLegacyInterface.getTeamHeadByProjectId(accessToken, projectId);
		long teamHeadId = 0;
		List<Object> superVisorListData=new ArrayList<>();
		if(deliveryHeadMap.containsKey("teamHeadId")) {
			teamHeadId= Long.parseLong(deliveryHeadMap.get("teamHeadId").toString());
			Map<String,Object>superVisorMapData=(Map<String, Object>) feignLegacyInterface.getSupervisorIdList(accessToken, teamHeadId).get("data") ;
			superVisorListData=(List<Object>) superVisorMapData.get("userSupervisorList") ;
		}
		boolean isSup=false;
		for(Object sup:superVisorListData) {
			if(sup.toString().equals(Long.toString(currentUser.getUserId()))) 
					isSup=true;
		}
		if(teamHeadId==currentUser.getUserId() || roles.contains(ConstantUtility.ROLE_ACCOUNTS) || roles.contains(ConstantUtility.ROLE_ACCOUNTS_ADMIN) || isSup)  
			isAuthorised=true;

		return isAuthorised;
	}
	
	public boolean isAccountsAdmin(String accessToken,Boolean checkForAccounts) {
		boolean isAccountsAdmin=false;
		UserModel currentUser=validator.tokenbValidate(accessToken);
		List<String> roles=currentUser.getRoles();
		if((checkForAccounts==null || checkForAccounts) && roles.contains(ConstantUtility.ROLE_ACCOUNTS))
			isAccountsAdmin=true;
		else if (!checkForAccounts  && roles.contains(ConstantUtility.ROLE_DASHBOARD_ADMIN))
			isAccountsAdmin=true;
		return isAccountsAdmin;
	}
	
	public boolean hasMarginAccess(String businessVertical,String accessToken) {
		boolean hasAccess=false;
		UserModel currentUser=validator.tokenbValidate(accessToken);
		List<String> roles=currentUser.getRoles();
		if(roles.contains(ConstantUtility.ROLE_ACCOUNTS) || roles.contains(ConstantUtility.ROLE_ACCOUNTS_ADMIN))
			hasAccess=true;
		else {
		switch(businessVertical) {
		case "Digital Marketing":
			if(roles.contains("ROLE_BU_DM"))
				hasAccess=true;
				break;
		case "Blockchain":
			if(roles.contains("ROLE_BU_BLOCKCHAIN"))
				hasAccess=true;
				break;
		case "Artificial Intelligence":
			if(roles.contains("ROLE_BU_AI"))
				hasAccess=true;
				break;
		case "ERP Solution":
			if(roles.contains("ROLE_BU_ES"))
				hasAccess=true;
				break;
		case "Oodles Technologies":
			if(roles.contains("ROLE_BU_OT"))
				hasAccess=true;
				break;
		case "Oodles Studio":
			if(roles.contains("ROLE_BU_OS"))
				hasAccess=true;
				break;
		case "Operations Support":
			if(roles.contains("ROLE_BU_OPS"))
				hasAccess=true;
				break;
		case "DPP":
			if(roles.contains("ROLE_BU_DPP"))
				hasAccess=true;
				break;
		default :
			hasAccess=false;
		}
		}
		return hasAccess;
	}
	
	public List<String> buAccess(String accessToken) {
		UserModel currentUser=validator.tokenbValidate(accessToken);
		List<String> roles=currentUser.getRoles();
		List<String> buList=new ArrayList<>();
		if(roles.contains(ConstantUtility.ROLE_ACCOUNTS) || roles.contains(ConstantUtility.ROLE_ACCOUNTS_ADMIN)
			|| roles.contains("ROLE_RESOURCING") || roles.contains("ROLE_RESOURCING_ADMIN"))
			buList.add("All");
		else {
			if(roles.contains("ROLE_BU_DM"))
				buList.add("Digital Marketing");
			if(roles.contains("ROLE_BU_BLOCKCHAIN"))
				buList.add("Blockchain");
			if(roles.contains("ROLE_BU_AI"))
				buList.add("Artificial Intelligence");
			if(roles.contains("ROLE_BU_ES"))
				buList.add("ERP Solution");
			if(roles.contains("ROLE_BU_OT"))
				buList.add("Oodles Technologies");
			if(roles.contains("ROLE_BU_OS"))
				buList.add("Oodles Studio");
			if(roles.contains("ROLE_BU_OPS"))
				buList.add("Operations Support");
			if(roles.contains("ROLE_BU_UNASSIGNED"))
				buList.add("Unassigned");
			if(roles.contains("ROLE_BU_DPP"))
				buList.add("DPP");
		}
		return buList;
	}
	
}
