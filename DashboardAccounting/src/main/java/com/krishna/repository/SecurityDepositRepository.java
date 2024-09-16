package com.krishna.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.krishna.domain.SecurityDeposit;

@Repository
public interface SecurityDepositRepository extends JpaRepository <SecurityDeposit,  Serializable>{
	
	SecurityDeposit findById(long id);
	@Query(value="select * from security_deposit where  is_deleted=:b",nativeQuery=true)
	List<SecurityDeposit> findAllByIsDeleted(boolean b);
	List<SecurityDeposit> findAllByProjectIdAndIsDeleted(long projectId,boolean b);
	List<SecurityDeposit> findAllByIsDeletedAndProjectId(boolean b, Long projectId);
	List<SecurityDeposit> findAllByIsDeletedAndProjectIdIn(boolean b, List<Long> projectIds);
	@Query(value="select * from security_deposit where YEAR(security_deposit.created_date)=:year and is_deleted=:b",nativeQuery = true)
	List<SecurityDeposit> findAllByYearAndIsDeleted(Integer year, boolean b);
	List<SecurityDeposit> findAllByLeadIdInAndIsDeleted(List<Long> leadIds, boolean b);
	@Query(value="select * from security_deposit where lead_id in :leadIds and YEAR(security_deposit.created_date)=:year and is_deleted=:b",nativeQuery = true)
	List<SecurityDeposit> findAllByLeadIdInAndYearAndIsDeleted(List<Long> leadIds, Integer year, boolean b);
}
