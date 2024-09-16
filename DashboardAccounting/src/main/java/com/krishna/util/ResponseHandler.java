package com.krishna.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * The Class ResponseHendler.
 */
public class ResponseHandler {
	
	/**
	 * Generate response.
	 *
	 * @param status the status
	 * @param error the error
	 * @param message the message
	 * @param extendedMessage the extended message
	 * @param responseObj the response obj
	 * @return the map
	 */
	public static Map<String, Object> generateResponse(HttpStatus status, boolean error, String message, Object extendedMessage, Object responseObj) {
		Map<String, Object> map = new HashMap<>(6);
		try {
			map.put(ConstantUtility.TIME_STAMP, new Date());
			map.put(ConstantUtility.STATUS, status);
			map.put("error", error);
			map.put(ConstantUtility.MESSAGE, message);
			map.put("extendedMessage", extendedMessage);
			map.put("data",responseObj);
			return map;
		} catch (Exception e) {
			map.clear();
			map.put(ConstantUtility.TIME_STAMP, new Date());
			map.put(ConstantUtility.STATUS, HttpStatus.INTERNAL_SERVER_ERROR);
			map.put(ConstantUtility.MESSAGE, e.getMessage());
			return map;
		}
	}
	
	public static ResponseEntity<Object> generateResponse(HttpStatus status, boolean isSuccess,String message, Object responseObj) {
		Map<String, Object> map = new HashMap<>();
		try {
			map.put(ConstantUtility.TIME_STAMP, new Date());
			map.put(ConstantUtility.STATUS, status.value());
			map.put("isSuccess", isSuccess);
			map.put(ConstantUtility.MESSAGE, message);
			map.put("data", responseObj);

			return new ResponseEntity<>(map,status);
		} catch (Exception e) {
			map.clear();
			map.put(ConstantUtility.TIME_STAMP, new Date());
			map.put(ConstantUtility.STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
			map.put(ConstantUtility.ISSUCCESS,false);
			map.put(ConstantUtility.MESSAGE, e.getMessage());
			map.put("data", null);
			return new ResponseEntity<>(map,status);
		}
	}
	
	/**
     * Use This Method When Need to Success Response with Status Code 200
     * @param  data 
     * @param  message
     * @return ResponseEntity
     * @author dharmendra
     */


	public static final ResponseEntity<Object> response(Object data,String message){
		Map<String,Object> map=new HashMap<>();
		map.put(ConstantUtility.ISSUCCESS,true);
		map.put("data",data);
		map.put(ConstantUtility.STATUS,HttpStatus.OK);
		map.put(ConstantUtility.MESSAGE, message);
		map.put(ConstantUtility.TIME_STAMP,new Date());
		map.put(ConstantUtility.FIELD_ERROR,null);
		return new ResponseEntity<>(map,HttpStatus.OK);
	}
	
	/**
	 * Use This Method when need to create error Response 
	 * @param message
	 * @param httpStatus
	 * @return ResponseEntity
	 * @author dharmendra
	 */
	 public static final ResponseEntity<Object> errorResponse(String message,HttpStatus httpStatus){
		Map<String,Object> map=new HashMap<>();
		map.put(ConstantUtility.ISSUCCESS,false);
		map.put("data",null);
		map.put(ConstantUtility.STATUS,httpStatus);
		map.put(ConstantUtility.MESSAGE, message);
		map.put(ConstantUtility.TIME_STAMP,new Date());
		map.put(ConstantUtility.FIELD_ERROR,null);
		return new ResponseEntity<>(map,httpStatus);
	} 
	
	/**
	 * Use This method when need to Genrating
	 * response relating to Spring's Form Validation
	 * Error
	 * @param message
	 * @param fieldError
	 * @return ResponseEntity
	 * @author dharmendra
	 */	
	 public static final ResponseEntity<Object> fieldErrorResponse(String message,Object fieldError){
		Map<String,Object> map=new HashMap<>();
		map.put(ConstantUtility.ISSUCCESS,false);
		map.put("data",null);
		map.put(ConstantUtility.STATUS,HttpStatus.BAD_REQUEST);
		map.put(ConstantUtility.MESSAGE, message);
		map.put(ConstantUtility.TIME_STAMP,new Date());
		map.put(ConstantUtility.FIELD_ERROR,fieldError);
		return new ResponseEntity<>(map,HttpStatus.BAD_REQUEST);
	} 
	
	/**
	 * Use This method when need to Generate response related to spring validation
	 * response relating to Spring's Form Validation
	 * Error
	 * @param message
	 * @param fieldError
	 * @return ResponseEntity
	 * @author shivangi
	 */	
	 public static final ResponseEntity<Object> errorResponse(String message,HttpStatus httpStatus,Object response){
		Map<String,Object> map=new HashMap<>();
		map.put("data",null);
		map.put(ConstantUtility.STATUS,httpStatus);
		map.put(ConstantUtility.MESSAGE, message);
		map.put(ConstantUtility.TIME_STAMP,new Date());
		map.put(ConstantUtility.FIELD_ERROR,null);
		return new ResponseEntity<>(map,httpStatus);
	}
}
