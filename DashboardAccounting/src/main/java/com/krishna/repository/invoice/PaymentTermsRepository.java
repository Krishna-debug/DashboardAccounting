package com.krishna.repository.invoice;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.invoice.PaymentTerms;

public interface PaymentTermsRepository extends JpaRepository<PaymentTerms,Serializable>{
	
	PaymentTerms findById(Long id);
	
	List<PaymentTerms> findByIsArchived(boolean b);
	
	PaymentTerms findByPaymentTermsTypeIgnoreCase(String type);


}
