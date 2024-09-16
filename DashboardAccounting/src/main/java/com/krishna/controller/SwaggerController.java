package com.krishna.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.service.SwaggerService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;

@RestController
public class SwaggerController {
	
	
	@Autowired
	private SwaggerService swgger;
	
	@GetMapping(value = "apiCount")
	public ResponseEntity<Object> swagger(@RequestHeader("Authorization") String accessToken) {
		try {

			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS,swgger.testHome());
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false, ConstantUtility.NOT_FOUND, null);
		}
	}

}
