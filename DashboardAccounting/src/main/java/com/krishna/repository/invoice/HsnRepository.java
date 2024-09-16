package com.krishna.repository.invoice;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.invoice.InvoiceHSN;

public interface HsnRepository extends JpaRepository<InvoiceHSN, Long>{

	InvoiceHSN findByIsArchived(boolean b);

}
