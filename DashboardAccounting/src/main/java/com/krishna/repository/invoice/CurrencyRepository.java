package com.krishna.repository.invoice;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.invoice.Currency;

public interface CurrencyRepository extends JpaRepository<Currency,Serializable> {
	
	List<Currency> findAll();

}
