package com.krishna.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

@RestController
public class ServerController {
	
	@GetMapping(value = UrlMappings.CHECK_IS_SERVER_RUNNING)
	public Object isServerRunning() {
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS,null);
	}

	
}