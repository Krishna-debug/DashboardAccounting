package com.krishna.accountspayable.serviceimpl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.krishna.accountspayable.domain.AccountsHead;
import com.krishna.accountspayable.repository.AccountsHeadRepository;
import com.krishna.accountspayable.services.AccountsHeadService;

@Service
public class AccountsHeadServiceImpl implements AccountsHeadService {
	
	private static final Logger logger = LoggerFactory.getLogger(AccountsHeadServiceImpl.class);
	
	@Autowired private AccountsHeadRepository accountsHeadRepo;
	
	@Override
	public AccountsHead saveAccountHead(String name) {
		if(name != null) {
			boolean exists = isExists(name);
			if(exists) {
				logger.info("The Account head with name %s is already present", name);
				return accountsHeadRepo.findByTypeAndIsArchive(name, false).get();
			}
			AccountsHead accountshead = new AccountsHead();
			accountshead.setType(name);
			return accountsHeadRepo.save(accountshead);
		}
		logger.info("AccountHead name can't be null or an empty string");
		return null;
	}

	private boolean isExists(String name) {
		return accountsHeadRepo.findByTypeAndIsArchive(name, false).isPresent();
	}

	@Override
	public List<AccountsHead> getAllAccountsHead() {
		List<AccountsHead> accountsHeads = accountsHeadRepo.findByIsArchive(false);
		return accountsHeads;
	}

	@Override
	public AccountsHead updateExisting(long id, String updatedName) {
		Optional<AccountsHead> accountsHead = accountsHeadRepo.findById(id);
		if(accountsHead.isPresent()) {
			accountsHead.get().setType(updatedName);
			return accountsHeadRepo.saveAndFlush(accountsHead.get());
		}
		logger.info("There is no data present for the given account head id %d", id);
		return null;
	}

	@Override
	public AccountsHead deleteAccountsHead(long id) {
		Optional<AccountsHead> accountsHead = accountsHeadRepo.findById(id);
		if(accountsHead.isPresent()) {
			accountsHead.get().setArchive(true);
			return accountsHeadRepo.saveAndFlush(accountsHead.get());
		}
		logger.info("There is no data present for the given account head id %d", id);
		return null;
	}
}
