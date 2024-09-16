package com.krishna.controller;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.dto.GetNotificationDto;
import com.krishna.service.EmailNotificationService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

@RestController
public class EmailNotificationController {
	
	@Autowired
	EmailNotificationService emailNotificationService;
	
	@PostMapping(value = UrlMappings.GET_MAIL_NOTIFICATION_DATA)
	public ResponseEntity<Object> getMailNotificationsData(@RequestHeader("Authorization") String accessToken,@RequestBody GetNotificationDto getNotificationDto) {
		Map<String, Object> mailNotification = emailNotificationService.getMailNotification(accessToken,getNotificationDto);
		if (!mailNotification.isEmpty()) {
			return ResponseHandler.generateResponse(HttpStatus.OK, true, "Mail Notifications Data fetched!!!", mailNotification);
		} else {
			return ResponseHandler.generateResponse(HttpStatus.NO_CONTENT, false, ConstantUtility.NOT_FOUND,
					mailNotification);
		}
	}

}
