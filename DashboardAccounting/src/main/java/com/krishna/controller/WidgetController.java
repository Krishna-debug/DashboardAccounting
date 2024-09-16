package com.krishna.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.domain.UserModel;
import com.krishna.security.JwtValidator;
import com.krishna.service.WidgetService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

@RestController
public class WidgetController {

	@Autowired
	WidgetService widgetService;
	
	@Autowired
	JwtValidator validator;
	
	@GetMapping(UrlMappings.INVOICE_DATA_WIDGET)
	public ResponseEntity<Object> getInvoiceWidgetData(@RequestHeader String authorization){
		UserModel user = validator.tokenbValidate(authorization);
		if(user != null) {
			Map<String,List<?>> data = widgetService.getInvoiceWidgetData();
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, data);
		}
		return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, ConstantUtility.INVALID_TOKEN, null);
	}
}
