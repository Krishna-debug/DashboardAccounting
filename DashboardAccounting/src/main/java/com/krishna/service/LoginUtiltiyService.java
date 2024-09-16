package com.krishna.service;

import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krishna.controller.FeignLegacyInterface;
import com.krishna.domain.UserModel;
import com.krishna.security.JwtValidator;


@Service
public class LoginUtiltiyService {
	
	@Autowired
	FeignLegacyInterface feignLegacyInterface;
	
	@Autowired
	private JwtValidator validator;
	
	public Map<String,Object> getUserInfo(String accessToken) {
		UserModel user = validator.tokenbValidate(accessToken);
		return (Map<String,Object>) feignLegacyInterface.getUserInformation(accessToken,user.getUserId()).get("data");
	}

	public List<Object> objectToListConverter(Object obj){
		List<Object> list=new ArrayList<>();
		if(obj.getClass().isArray()) {
			list=Arrays.asList((Object[])obj);
		}
		else if(obj instanceof Collection<?>) {
			list=new ArrayList<>((Collection<?>)obj);
		}
		return list;
	}
	
	public Map<String,Object> objectToMapConverter(Object obj){
		ObjectMapper mapper=new ObjectMapper();
		Map<String,Object> map=mapper.convertValue(obj, Map.class);
		return map;
	}

	public Map<Integer,Object> objectToIntMapConverter(Object obj){
		ObjectMapper mapper=new ObjectMapper();
		Map<Integer,Object> map=mapper.convertValue(obj, new TypeReference<Map<Integer,Object>>(){});
		return map;
	}

	public Map<Long,Object> objectToLongMapConverter(Object obj){
		ObjectMapper mapper=new ObjectMapper();
		Map<Long,Object> map=mapper.convertValue(obj, new TypeReference<Map<Long,Object>>(){});
		return map;
	}
	
	public int getMonthNumber(String monthName) {
	    return Month.valueOf(monthName.toUpperCase()).getValue();
	}

}
