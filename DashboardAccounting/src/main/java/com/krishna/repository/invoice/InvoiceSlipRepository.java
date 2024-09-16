package com.krishna.repository.invoice;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.invoice.InvoiceSlip;

public interface InvoiceSlipRepository extends JpaRepository<InvoiceSlip, Long>{

	InvoiceSlip findByInvoiceId(Long invoiceId);

	InvoiceSlip findByInvoiceIdAndIsDeleted(Long invoiceId, boolean b);

	List<InvoiceSlip> findAllByInvoiceId(Long invoiceId);

	InvoiceSlip findByInvoiceIdAndIsDeletedAndIsIfsd(Long invoiceId, boolean b, Boolean isIfsd);

}
