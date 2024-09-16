package com.krishna.exceptionhandling;

import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RecordsNotFoundException extends RuntimeException {
	private String message;
	   private static final long serialVersionUID = 1L;
	public RecordsNotFoundException(String message) {
		super();
		this.message = message;
	}
	
	   public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public RecordsNotFoundException() {
		// TODO Auto-generated constructor stub
	}
}

