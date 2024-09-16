package com.krishna.repository.invoice;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.invoice.InvoiceType;

public interface InvoiceTypeRepository extends JpaRepository<InvoiceType, Long>{

	InvoiceType findByNameAndIsArchived(String invoiceType, boolean b);

	List<InvoiceType> findAllByIsArchived(boolean b);

}
