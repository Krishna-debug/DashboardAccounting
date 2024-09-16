package com.krishna.controller.invoice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.krishna.domain.invoice.InvoiceSource;
import com.krishna.dto.invoice.InvoiceSourceAddDto;
import com.krishna.dto.invoice.InvoiceSourceUpdateDto;
import com.krishna.service.invoice.InvoiceSourceService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

@RestController
public class InvoiceSourceController {
	
	@Autowired
	InvoiceSourceService invoiceSourceService;
	
	@GetMapping(UrlMappings.INVOICE_SOURCES)
	public ResponseEntity<Object> getAllInvoiceSources(@RequestHeader("Authorization") String accessToken) {
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, invoiceSourceService.getInvoiceSources(accessToken));
	}
	
	@PostMapping(UrlMappings.INVOICE_SOURCES)
	public ResponseEntity<Object> addInvoiceSource(@RequestHeader("Authorization") String accessToken, @RequestBody InvoiceSourceAddDto invoiceSourceDto) {
		InvoiceSource invoiceSourceDetails=invoiceSourceService.addInvoiceSource(accessToken, invoiceSourceDto);
		if(invoiceSourceDetails!=null)
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, invoiceSourceDetails);
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.ALREADY_EXIST, invoiceSourceDetails);
	}
	
	@PatchMapping(UrlMappings.INVOICE_SOURCES)
	public ResponseEntity<Object> updateInvoiceSource(@RequestHeader("Authorization") String accessToken, @RequestBody InvoiceSourceUpdateDto invoiceSourceDto) {
		InvoiceSource invoiceSourceDetails=invoiceSourceService.updateInvoiceSource(accessToken, invoiceSourceDto);
		if(invoiceSourceDetails!=null)
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, invoiceSourceDetails);
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.NOT_FOUND, invoiceSourceDetails);
	}
	
	@DeleteMapping(UrlMappings.INVOICE_SOURCES)
	public ResponseEntity<Object> deleteInvoiceSource(@RequestHeader("Authorization") String accessToken, @RequestParam Long id) {
		InvoiceSource invoiceSourceDetails=invoiceSourceService.deleteInvoiceSource(accessToken, id);
		if(invoiceSourceDetails!=null)
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, invoiceSourceDetails);
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.NOT_FOUND, invoiceSourceDetails);
	}

	@GetMapping(UrlMappings.BU_INVOICE_SOURCES)
	public ResponseEntity<Object> getBuAllInvoiceSources(@RequestHeader("Authorization") String accessToken) {
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, invoiceSourceService.getInvoiceSources(accessToken));
	}
	
	@PostMapping(UrlMappings.BU_INVOICE_SOURCES)
	public ResponseEntity<Object> addBuInvoiceSource(@RequestHeader("Authorization") String accessToken, @RequestBody InvoiceSourceAddDto invoiceSourceDto) {
		InvoiceSource invoiceSourceDetails=invoiceSourceService.addInvoiceSource(accessToken, invoiceSourceDto);
		if(invoiceSourceDetails!=null)
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, invoiceSourceDetails);
		
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.ALREADY_EXIST, invoiceSourceDetails);
	}
	
	@PatchMapping(UrlMappings.BU_INVOICE_SOURCES)
	public ResponseEntity<Object> updateBuInvoiceSource(@RequestHeader("Authorization") String accessToken, @RequestBody InvoiceSourceUpdateDto invoiceSourceDto) {
		InvoiceSource invoiceSourceDetails=invoiceSourceService.updateInvoiceSource(accessToken, invoiceSourceDto);
		if(invoiceSourceDetails!=null)
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, invoiceSourceDetails);
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.NOT_FOUND, invoiceSourceDetails);
	}
	
	@DeleteMapping(UrlMappings.BU_INVOICE_SOURCES)
	public ResponseEntity<Object> deleteBuInvoiceSource(@RequestHeader("Authorization") String accessToken, @RequestParam Long id) {
		InvoiceSource invoiceSourceDetails=invoiceSourceService.deleteInvoiceSource(accessToken, id);
		if(invoiceSourceDetails!=null)
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, invoiceSourceDetails);
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.NOT_FOUND, invoiceSourceDetails);
	}

}
