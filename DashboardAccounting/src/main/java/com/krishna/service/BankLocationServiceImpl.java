package com.krishna.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.krishna.domain.BankLocation;
import com.krishna.domain.invoice.InvoiceBank;
import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.repository.BankLocationRepo;
import com.krishna.repository.invoice.InvoiceBankRepository;
import com.krishna.repository.invoice.ProjectInvoiceRepository;

@Service
public class BankLocationServiceImpl implements BankLocationService {

	@Autowired BankLocationRepo bankLocationRepo;
	
	@Autowired InvoiceBankRepository bankRepository;
	
	@Autowired ProjectInvoiceRepository invoiceRepository;
	
	@Override
	public List<BankLocation> getAllBankLocations() {
		return bankLocationRepo.findAllByIsDeletedFalse();
	}

	@Override
	public BankLocation getBankLocation(long id) {
		BankLocation bank = bankLocationRepo.findByIdAndIsDeleted(id, false);
		if (bank != null)
			return bank;
		return null;
	}
	
	@Override
	public BankLocation addBankLocation(String location, Long id) {
		BankLocation existingBank = bankLocationRepo.findByLocation(location);
		if (existingBank == null) {
			if (id==null) {
				BankLocation bank = new BankLocation();
				bank.setLocation(location);
				bankLocationRepo.save(bank);
				return bank;
			} else {
				BankLocation bank = bankLocationRepo.findByIdAndIsDeleted(id, false);
				bank.setLocation(location);
				bankLocationRepo.save(bank);
				return bank;
			}
		} else {
			return null;
		}
	}

	@Override
	public boolean deleteBankLocation(long id) {
		BankLocation location = bankLocationRepo.findByIdAndIsDeleted(id, false);
		
		List<ProjectInvoice> invoice = invoiceRepository.findAllByBankLocationId(id);
		if(invoice.isEmpty()) {
			location.setDeleted(true);
			bankLocationRepo.save(location);
			return true;
		}
		return false;
	}

	

}
