package com.krishna.accountspayable.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.krishna.accountspayable.domain.HsnCode;

@Repository
public interface HsnCodeRepository extends JpaRepository<HsnCode, Long> {
	
	HsnCode findById(long id);
	
	List<HsnCode> findAllByIsArchive(boolean isArchive);
	
	HsnCode findByHsnCodeAndIsArchive(String hsnCode, boolean isArchive);
}
