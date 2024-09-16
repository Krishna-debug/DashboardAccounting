package com.krishna.service.invoice;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.krishna.domain.invoice.InvoiceSource;
import com.krishna.dto.invoice.InvoiceSourceAddDto;
import com.krishna.dto.invoice.InvoiceSourceUpdateDto;
import com.krishna.repository.invoice.InvoiceSourceRepository;

@Service
public class InvoiceSourceService {
	
	@Autowired
	InvoiceSourceRepository invoiceSourceRepo;
	
	public InvoiceSource addInvoiceSource(String accessToken, InvoiceSourceAddDto invoiceSourceDto) {
			InvoiceSource source=new InvoiceSource();
			BeanUtils.copyProperties(invoiceSourceDto, source);
			source.setArchived(false);
			invoiceSourceRepo.save(source);
			return source;
	}
	
	public InvoiceSource updateInvoiceSource(String accessToken, InvoiceSourceUpdateDto invoiceSourceDto) {
		InvoiceSource source=invoiceSourceRepo.findByIdAndIsArchived(invoiceSourceDto.getId(),false);
		if(source!=null) {
			BeanUtils.copyProperties(invoiceSourceDto, source);
			source.setArchived(false);
		}
		invoiceSourceRepo.save(source);
		return source;
	}
	
	public InvoiceSource deleteInvoiceSource(String accessToken,Long id) {
		InvoiceSource source=invoiceSourceRepo.findByIdAndIsArchived(id,false);
		if(source!=null) 
			source.setArchived(true);
		invoiceSourceRepo.save(source);
		return source;
	}
	
	public List<InvoiceSource> getInvoiceSources(String accessToken){
		List<InvoiceSource> sources=invoiceSourceRepo.findAllByIsArchived(false);
		return sources;
	}

}
