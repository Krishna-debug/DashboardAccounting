package com.krishna.exceptionhandling;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@Component
public class ExceptionUtils {
	
	private TemplateEngine templateEngine;
	@Autowired RestTemplate restTemplate;
	
	@Value("${spring.address}")
	String springAddress;
	
	private static String TOKEN = "";
	
	@Autowired
	public ExceptionUtils(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}
	
	
	/**
	 * <p>This method extracts the JWT Token from the
	 * Server request and saves the token for current request
	 * in a constant.</p>
	 * 
	 * @param request WebRequest in form of HttpServletRequest.
	 * @return Extracted JWT Token from the request Headers.
	 */
	protected String filterAndExtractAccessToken(HttpServletRequest request) {
		String jwtToken = request.getHeader("Authorization");
		ExceptionUtils.TOKEN = "Bearer " + jwtToken;
 		return jwtToken;
	}

	/**
	 * <p>This method reports the exception to concerned dashboard
	 * admins and then it sends response telling where the exception
	 * has occured and stating the cause of the exception. </p>
	 * 
	 * @param stackTrace Array containing stack trace message.
	 * @param status HttpStatus to send.
	 * @param request WebRequest in form of HttpServletRequest.
	 * @return ResponseEntity to the server.
	 */
	public ResponseEntity<Object> handleExceptionInternal(StackTraceElement[] stackTrace,
		   HttpStatus status, HttpServletRequest request) {
		notifyConcernedTeam(Arrays.asList(stackTrace), request.getRequestURI(), request);
		return new ResponseEntity<>(stackTrace[0], status);
	}

	/**
	 * <p>This method gets trigged when the controller advice raise an exception by 
	 * {@link #handleExceptionInternal(StackTraceElement[], HttpStatus, HttpServletRequest)}
	 * This method sends notification to all dashboard admin users and runs in a separate
	 * thread.</p>
	 * 
	 * @param stackTrace StackTraces of the Exception.
	 * @param contextPath The request URI path 
	 * @param WebRequest in form of HttpServletRequest.
	 */
	private void notifyConcernedTeam(List<StackTraceElement> stackTrace, String contextPath, HttpServletRequest request) {
		new Thread(() -> {
			Context context = new Context();
			context.setVariable("url", contextPath);
			context.setVariable("messages", stackTrace);
			List<Long> concernedTeamIds =new ArrayList<>();
			
			//concernedTeamIds = this.getAllDashboardAdmins(request, "Dashboard");
			String html = templateEngine.process("exception-notification", context);
			concernedTeamIds.parallelStream().forEach(id -> {
				Map<String, Object> body = new HashMap<>();
				body.put("type", "Cron");
				body.put("subject", "Exception Occured while processing request at " + contextPath);
				body.put("message", "Please check the exception Occured while processing the WebRequest.");
				body.put("userId", id);
				body.put("concernedId", id);
				body.put("templateName", "exception-notification.html");
				body.put("templateData", html);
				body.put("creationDate", null);
				
				HttpHeaders headers = new HttpHeaders();
				headers.set("Authorization", ExceptionUtils.TOKEN);
				headers.set("ContentType", "application/json");
				
				HttpEntity<Object> entity = new HttpEntity<>(body, headers);
				restTemplate.postForObject(URI.create(springAddress + "/notifications/save?userId="+id), entity, Object.class);
			});
			
		}).start();
		
	}
	
	
}
