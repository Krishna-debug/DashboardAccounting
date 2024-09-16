package com.krishna.repository.invoice;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.krishna.domain.invoice.InvoiceCycle;

@Repository
public interface InvoiceCycleRepository extends JpaRepository<InvoiceCycle,Serializable> {
       
	InvoiceCycle findById(Long id);
	
	List<InvoiceCycle> findByIsArchived(boolean b);
	
	InvoiceCycle findByInvoiceCycleTypeIgnoreCase(String type);
}
