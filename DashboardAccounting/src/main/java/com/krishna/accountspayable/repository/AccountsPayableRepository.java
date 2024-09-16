package com.krishna.accountspayable.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.krishna.accountspayable.domain.AccountsPayable;
import com.krishna.accountspayable.domain.HsnCode;
import com.krishna.accountspayable.enums.PayableTypes;

@Repository
public interface AccountsPayableRepository extends JpaRepository<AccountsPayable, Long> {
	
	List<AccountsPayable> findByisArchiveAndMonthAndYearAndHsnSacCode(boolean isArchive, int month, int year,
			HsnCode code);
	List<AccountsPayable> findByisArchiveAndMonthAndYear(boolean isArchive, int month, int year);
	
	List<AccountsPayable> findAllByIsArchiveAndMonthAndYearAndPayType(boolean isArchive, int month, int year,
			PayableTypes payType);
}
