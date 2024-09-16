package com.krishna.repository.invoice;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.krishna.domain.invoice.InvoiceBank;

@Repository
public interface InvoiceBankRepository extends JpaRepository<InvoiceBank, Long>{

	List<InvoiceBank> findAllByIsArchived(boolean b);

	InvoiceBank findByNameAndIsArchived(String name, boolean b);

	InvoiceBank findByIdAndIsArchived(Long id, boolean b);

}
