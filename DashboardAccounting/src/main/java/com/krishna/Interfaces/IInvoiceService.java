package com.krishna.Interfaces;

import java.util.Map;

import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.domain.invoice.ProjectInvoiceItem;
import com.krishna.dto.ProjectInvoiceDto;
import com.krishna.dto.invoice.InvoiceSlipDto;
import com.krishna.dto.invoice.ProjectInvoiceGenerateDto;
import com.krishna.dto.invoice.ProjectInvoiceItemDto;
import com.krishna.dto.invoice.ProjectInvoiceItemGetDto;

public interface IInvoiceService {

	Map<String, Object> viewInvoice(String authorization, Long projectInvoiceId, Boolean isInternalCall, Boolean isIfsd, Boolean isDollarCurrency);

	Object addProjectInvoiceForCreation(ProjectInvoiceDto projectInvoiceDto, String authorization);

	ProjectInvoiceItemGetDto editInvoiceItems(String authorization, ProjectInvoiceItemGetDto invoiceItemDto);

	ProjectInvoiceItem deleteInvoiceItems(String authorization, Long id);

	Map<String, Object> saveGeneratedInvoice(String accessToken, ProjectInvoiceGenerateDto invoiceDto);

	Map<String, Object> generatePDFFromHTML(String authorization, InvoiceSlipDto slipDto) throws Exception;

	Object resetInvoice(Long invoiceId, boolean  isIfsd);

	ProjectInvoiceItemGetDto addInvoiceItem(ProjectInvoiceItemDto invoiceItemDto, String accessToken);

}
