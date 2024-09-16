package com.krishna.repository.invoice;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.krishna.domain.invoice.PaymentMode;

@Repository
public interface PaymentModeRepository extends JpaRepository<PaymentMode,Serializable>{
	
	PaymentMode findById(Long id);
	
	List<PaymentMode> findByIsArchived(boolean b);
	PaymentMode findByPaymentModeTypeIgnoreCase(String type);

	List<PaymentMode> findAllByIdAndIsArchived(Long modeOfPaymentId, boolean result);
	

}
