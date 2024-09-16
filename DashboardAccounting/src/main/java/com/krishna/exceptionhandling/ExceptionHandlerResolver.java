package com.krishna.exceptionhandling;

import java.util.HashMap;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;


/**
 * <p>The Class ExceptionHandler. Responsible for handling
 * all the exceptions throught the Application.</p>
 * 
 * @author Amit Mishra
 *
 */
@RestControllerAdvice
public class ExceptionHandlerResolver extends ResponseEntityExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerResolver.class);
	private ExceptionUtils exceptionUtils;
	
	@Autowired
	public ExceptionHandlerResolver(ExceptionUtils exceptionUtils) {
		this.exceptionUtils = exceptionUtils;
	}

	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<Object> handleNullPointerException(Exception ex, HttpServletRequest request) {
		NullPointerException nullPointerException = (NullPointerException) ex;
		StackTraceElement[] stackTrace = nullPointerException.getStackTrace();
		log.error("NullPointerException Handled at path {}. The cause "
				+ "of the exception is {}. Concerned Team has been "
				+ "notified about the error occured, please check the email "
				+ "for more details.", request.getRequestURI(), stackTrace[0]);
		
		return exceptionUtils.handleExceptionInternal(stackTrace,
				HttpStatus.INTERNAL_SERVER_ERROR, request);
	}
	
	@ExceptionHandler(RecordsNotFoundException.class)
	public ResponseEntity<Object>  RecordsNotFoundException(Exception ex, HttpServletRequest request) {
		return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, true, ex.getMessage(),null);	}
}
