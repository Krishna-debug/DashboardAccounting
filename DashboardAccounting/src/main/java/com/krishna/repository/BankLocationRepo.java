package com.krishna.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.krishna.domain.BankLocation;

@Repository
public interface BankLocationRepo extends JpaRepository<BankLocation,Long> {

	public BankLocation findByLocation(String location);

	public BankLocation findByIdAndIsDeleted(long id, boolean b);

	public List<BankLocation> findAllByIsDeletedFalse();

}
