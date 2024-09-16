package com.krishna.service.invoice;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.krishna.domain.invoice.InvoiceBank;
import com.krishna.dto.invoice.InvoiceBankDto;
import com.krishna.repository.invoice.InvoiceBankRepository;

@Service
public class InvoiceBankService {
	
	@Autowired
	InvoiceBankRepository bankRepository;

	public List<InvoiceBank> getAllInvoiceBanks(String accessToken) {
		List<InvoiceBank> banksList= bankRepository.findAllByIsArchived(false);
		return banksList;
	}
	
	public InvoiceBank addInvoiceBank(String accessToken, InvoiceBankDto invoiceBankDto) {
		InvoiceBank bank=bankRepository.findByNameAndIsArchived(invoiceBankDto.getName(),false);
		if(bank==null) {
			bank=new InvoiceBank();
			bank.setArchived(false);
			BeanUtils.copyProperties(invoiceBankDto, bank);
			bankRepository.save(bank);
			return bank;
		}
		else
			return null;
	}

	public InvoiceBank updateInvoiceBank(String accessToken, InvoiceBankDto invoiceBankDto,long id) {
		InvoiceBank bank = bankRepository.findByIdAndIsArchived(id, false);
		if(bank!=null) 
			BeanUtils.copyProperties(invoiceBankDto, bank);
		bankRepository.save(bank);
		return bank;
	}
	
	public InvoiceBank deleteInvoiceBank(String accessToken,InvoiceBank bank) {
		if(bank!=null) 
			bank.setArchived(true);
		bankRepository.save(bank);
		return bank;
	}

}
