package com.krishna.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.domain.BuSpecificCost;
import com.krishna.domain.BuSpecificType;
import com.krishna.domain.UserModel;
import com.krishna.dto.BuSpecificCostDto;
import com.krishna.security.JwtValidator;
import com.krishna.service.BuSpecificCostService;
import com.krishna.service.ProjectMarginService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.MessageUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;


@RestController
public class BuSpecificCostController {

    @Autowired
    private BuSpecificCostService buSpecificCostService;

    @Autowired
    private ProjectMarginService projectMarginService;

    @Autowired
	private JwtValidator validator;
	
    
    
    @PostMapping(UrlMappings.ADD_BU_SPECIFIC_COST)
    public ResponseEntity<Object> addBuSpecificCost(@RequestHeader String authorization,@RequestBody BuSpecificCostDto buSpecificCostDto) {
        UserModel user = validator.tokenbValidate(authorization);

        if(user != null){
           BuSpecificCost buSpecificCost= buSpecificCostService.addBuSpecificCost(buSpecificCostDto, user.getUserId());
           if(buSpecificCost == null) {
            return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, MessageUtility.getMessage("Invalid input parameters"), buSpecificCost);
            }
            projectMarginService.flushBuMargins();
            return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.ISSUCCESS, buSpecificCost);

        }
        else {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, ConstantUtility.INVALID_TOKEN, null);
		}
    }

    @PutMapping(UrlMappings.UPDATE_BU_SPECIFIC_COST)
    public ResponseEntity<Object> updateBuSpecificCost(@RequestHeader String authorization,@RequestBody BuSpecificCostDto buSpecificCostDto) {
        UserModel user = validator.tokenbValidate(authorization);

        if(user != null){
           BuSpecificCost buSpecificCost= buSpecificCostService.updateBuSpecificCost(buSpecificCostDto, user.getUserId());
           if(buSpecificCost == null) {
            return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, MessageUtility.getMessage("duplicate.entry"), buSpecificCost);
            }
            projectMarginService.flushBuMargins();
            return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.ISSUCCESS, buSpecificCost);

        }
        else {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, ConstantUtility.INVALID_TOKEN, null);
		}
    }

    @GetMapping(UrlMappings.GET_BU_SPECIFIC_COST)
    public ResponseEntity<Object> getBuSpecificCost(@RequestHeader String authorization,@RequestParam Integer year, @RequestParam Integer month, String businessVertical) {
        UserModel user = validator.tokenbValidate(authorization);

        if(user != null){
          List<Map<String,Object>>  buSpecificCost= buSpecificCostService.getBuSpecificCost(year,businessVertical,month+1);
           if(buSpecificCost == null) {
            return ResponseHandler.errorResponse("Unable to fetch data", HttpStatus.EXPECTATION_FAILED);
        }
            return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.ISSUCCESS, buSpecificCost);

        }
        else {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, ConstantUtility.INVALID_TOKEN, null);
		}
    }

    @DeleteMapping(UrlMappings.DELETE_BU_SPECIFIC_COST)
    public ResponseEntity<Object> deleteBuSpecificCost(@RequestHeader String authorization,@RequestParam long buCostId) {
        UserModel user = validator.tokenbValidate(authorization);

        if(user != null){
            BuSpecificCost buSpecificCost= buSpecificCostService.deleteBuSpecificCost(buCostId);
           if(buSpecificCost == null) {
            return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, ConstantUtility.NOT_FOUND, null);
            }
            projectMarginService.flushBuMargins();
            return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.ISSUCCESS, buSpecificCost);

        }
        else {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, ConstantUtility.INVALID_TOKEN, null);
		}
    }

    @PostMapping(UrlMappings.ADD_BU_SPECIFIC_TYPE)
    public ResponseEntity<Object> addBuSpecificType(@RequestHeader String authorization,@RequestParam String type) {
        UserModel user = validator.tokenbValidate(authorization);

        if(user != null){
           BuSpecificType result= buSpecificCostService.addBuSpecificType(type);
           if(result == null) {
            return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, MessageUtility.getMessage("Invalid input parameters"), null);
            }
            return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.ISSUCCESS, result);

        }
        else {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, ConstantUtility.INVALID_TOKEN, null);
		}
    }

    @PutMapping(UrlMappings.UPDATE_BU_SPECIFIC_TYPE)
    public ResponseEntity<Object> updateBuSpecificType(@RequestHeader String authorization,@RequestParam Long id, @RequestParam String typeName) {
        UserModel user = validator.tokenbValidate(authorization);

        if(user != null){
           BuSpecificType buType= buSpecificCostService.updateBuType(id,typeName);
           if(buType == null) {
            return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, MessageUtility.getMessage("duplicate.entry"), null);
            }
            return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.ISSUCCESS, buType);

        }
        else {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, ConstantUtility.INVALID_TOKEN, null);
		}
    }

    @GetMapping(UrlMappings.GET_BU_SPECIFIC_TYPE)
    public ResponseEntity<Object> getBuSpecificType(@RequestHeader String authorization) {
        UserModel user = validator.tokenbValidate(authorization);

        if(user != null){
          List<Map<String,Object>>  buSpecificCost= buSpecificCostService.getBuSpecificTypes();
           if(buSpecificCost == null) {
            return ResponseHandler.errorResponse("Unable to fetch data", HttpStatus.EXPECTATION_FAILED);
        }
            return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.ISSUCCESS, buSpecificCost);

        }
        else {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, ConstantUtility.INVALID_TOKEN, null);
		}
    }

    @DeleteMapping(UrlMappings.DELETE_BU_SPECIFIC_TYPE)
    public ResponseEntity<Object> deleteBuSpecificType(@RequestHeader String authorization,@RequestParam long id) {
        UserModel user = validator.tokenbValidate(authorization);

        if(user != null){
            boolean result= buSpecificCostService.deleteBuSpecificType(id);
           if(!result) {
            return ResponseHandler.generateResponse(HttpStatus.NOT_ACCEPTABLE, false, ConstantUtility.BU_SPECIFIC_TYPE_USE, null);
            }
            return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.ISSUCCESS, true);

        }
        else {
			return ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST, false, ConstantUtility.INVALID_TOKEN, null);
		}
    }

}
