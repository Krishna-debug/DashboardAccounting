package com.krishna.controller.invoice;

import java.util.List;

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

import com.krishna.domain.invoice.InvoiceBank;
import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.dto.invoice.InvoiceBankDto;
import com.krishna.repository.invoice.InvoiceBankRepository;
import com.krishna.repository.invoice.ProjectInvoiceRepository;
import com.krishna.service.invoice.InvoiceBankService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.ResponseHandler;
import com.krishna.util.UrlMappings;

@RestController
public class InvoiceBankController {
	
	@Autowired
	InvoiceBankService invoiceService;
	
	@Autowired
	InvoiceBankRepository bankRepository;
	
	@Autowired
	ProjectInvoiceRepository invoiceRepo;
	
	@GetMapping(UrlMappings.INVOICE_BANKS)
	public ResponseEntity<Object> getAllInvoiceBanks(@RequestHeader("Authorization") String accessToken) {
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, invoiceService.getAllInvoiceBanks(accessToken));
	}
	
	@PostMapping(UrlMappings.INVOICE_BANKS)
	public ResponseEntity<Object> addInvoiceBank(@RequestHeader("Authorization") String accessToken, @RequestBody InvoiceBankDto invoiceBankDto) {
		InvoiceBank invoiceBankDetails=invoiceService.addInvoiceBank(accessToken, invoiceBankDto);
		if(invoiceBankDetails!=null)
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, invoiceBankDetails);
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.ALREADY_EXIST, invoiceBankDetails);
	}
	
	@PatchMapping(UrlMappings.INVOICE_BANKS)
	public ResponseEntity<Object> updateInvoiceBank(@RequestHeader("Authorization") String accessToken, @RequestBody InvoiceBankDto invoiceBankDto, @RequestParam long id) {
		InvoiceBank invoiceBankDetails=invoiceService.updateInvoiceBank(accessToken, invoiceBankDto,id);
		if(invoiceBankDetails!=null)
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, invoiceBankDetails);
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.NOT_FOUND, invoiceBankDetails);
	}
	
	@DeleteMapping(UrlMappings.INVOICE_BANKS)
	public ResponseEntity<Object> deleteInvoiceBank(@RequestHeader("Authorization") String accessToken, @RequestParam Long id) {
		InvoiceBank bank=bankRepository.findByIdAndIsArchived(id,false);
		if(bank!=null) {
			List<ProjectInvoice> invoices=invoiceRepo.findAllByBankAndIsDeleted(bank,false);
			if(!invoices.isEmpty())
				return ResponseHandler.errorResponse("Invoices for the bank exist, cant deleted", HttpStatus.EXPECTATION_FAILED, null);
		}
		InvoiceBank invoiceBankDetails=invoiceService.deleteInvoiceBank(accessToken, bank);
		if(invoiceBankDetails!=null)
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, invoiceBankDetails);
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.NOT_FOUND, invoiceBankDetails);
	}

	@GetMapping(UrlMappings.BU_INVOICE_BANKS)
	public ResponseEntity<Object> getBuAllInvoiceBanks(@RequestHeader("Authorization") String accessToken) {
		return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, invoiceService.getAllInvoiceBanks(accessToken));
	}
	
	@PostMapping(UrlMappings.BU_INVOICE_BANKS)
	public ResponseEntity<Object> addBuInvoiceBank(@RequestHeader("Authorization") String accessToken, @RequestBody InvoiceBankDto invoiceBankDto) {
		InvoiceBank invoiceBankDetails=invoiceService.addInvoiceBank(accessToken, invoiceBankDto);
		if(invoiceBankDetails!=null)
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, invoiceBankDetails);
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.ALREADY_EXIST, invoiceBankDetails);
	}
	
	@PatchMapping(UrlMappings.BU_INVOICE_BANKS)
	public ResponseEntity<Object> updateBuInvoiceBank(@RequestHeader("Authorization") String accessToken, @RequestBody InvoiceBankDto invoiceBankDto, @RequestParam long id) {
		InvoiceBank invoiceBankDetails=invoiceService.updateInvoiceBank(accessToken, invoiceBankDto,id);
		if(invoiceBankDetails!=null)
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, invoiceBankDetails);
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.NOT_FOUND, invoiceBankDetails);
	}
	
	@DeleteMapping(UrlMappings.BU_INVOICE_BANKS)
	public ResponseEntity<Object> deleteBuInvoiceBank(@RequestHeader("Authorization") String accessToken, @RequestParam Long id) {
		InvoiceBank bank=bankRepository.findByIdAndIsArchived(id,false);
		if(bank!=null) {
			List<ProjectInvoice> invoices=invoiceRepo.findAllByBankAndIsDeleted(bank,false);
			if(!invoices.isEmpty())
				return ResponseHandler.errorResponse("Invoices for the bank exist, cant deleted", HttpStatus.EXPECTATION_FAILED, null);
		}
		InvoiceBank invoiceBankDetails=invoiceService.deleteInvoiceBank(accessToken, bank);
		if(invoiceBankDetails!=null)
			return ResponseHandler.generateResponse(HttpStatus.OK, true, ConstantUtility.SUCCESS, invoiceBankDetails);
		else
			return ResponseHandler.generateResponse(HttpStatus.EXPECTATION_FAILED, false, ConstantUtility.NOT_FOUND, invoiceBankDetails);
	}


}
