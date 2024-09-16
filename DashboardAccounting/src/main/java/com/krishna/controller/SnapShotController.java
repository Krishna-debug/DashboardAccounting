package com.krishna.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.service.SnapShotService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

@RestController
public class SnapShotController {

	@Autowired
	SnapShotService snapshotService;
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_BU_WISE_MARGIN_SNAPSHOT)
	public ResponseEntity<Object> getBuWiseMarginSnapShot(@RequestHeader("Authorization") String accessToken,@RequestParam Long from,@RequestParam Long to,@RequestParam String businessVertical){
		List<Map<Object,Object>> data=snapshotService.getBuWiseMarginSnapShot(accessToken,to,from,businessVertical);
		if(data.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unable to fetch data", data);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY, data);
	}
	
	@GetMapping(UrlMappings.GET_BU_WISE_RESERVE_SNAPSHOT)
	public ResponseEntity<Object> getBuWiseReserveSnapShot(@RequestHeader("Authorization") String accessToken,@RequestParam Long from,@RequestParam Long to,@RequestParam String businessVertical){
		Map<Object,Object> data=snapshotService.getBuWiseReserveSnapShot(accessToken,to,from,businessVertical);
		if(data.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unable to fetch data", data);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY, data);
	}
	
	
	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN')")
	@GetMapping(UrlMappings.GET_MARGIN_SNAPSHOT)
	public ResponseEntity<Object> getMarginSnapShot(@RequestHeader("Authorization") String accessToken,@RequestParam long from,@RequestParam long to, @RequestParam long projectId,@RequestParam(required=false) String businessVertical){
		List<Object> data=snapshotService.getMarginSnapShot(accessToken,to,from,projectId,businessVertical);
		if(data.isEmpty())
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, "Unable to fetch data", data);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.DATA_FETCHED_SUCCESSFULLY, data);
	} 
	

	@PreAuthorize("hasAnyRole('ACCOUNTS','ACCOUNTS_ADMIN','ROLE_ANONYMOUS')")
	@GetMapping(UrlMappings.SEND_MAIL_ON_RESERVE_CHANGE)
	public ResponseEntity<Object> sendMailOnReserveChange(@RequestHeader("Authorization") String accessToken) {

		snapshotService.sendMailOnReserveChange(accessToken);
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.MAIL_SENT, null);

	}

}
