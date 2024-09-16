package com.krishna.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.krishna.domain.BankLocation;

@Service
public interface BankLocationService {

	public List<BankLocation> getAllBankLocations();

	public BankLocation addBankLocation(String location,Long id);

	public boolean deleteBankLocation(long id);

	public BankLocation getBankLocation(long id);

}
