package com.krishna.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.krishna.dto.ReserveDto;

@Service
public interface BuReserveService {

	Map<String, Object> getBuWiseReserve(String accessToken, String buName, int year);

	Boolean updateDeductedAmount(String accessToken,ReserveDto reserveDto);

	Boolean deleteBuReserve(String accessToken, Long id);

	Boolean updateRemarks(String accessToken, Long id,String remarks);
	

}
