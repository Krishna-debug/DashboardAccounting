package com.krishna.repository.payroll;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.PayRegister;
import com.krishna.domain.PayRevisions;

public interface PayRevisionRepository extends JpaRepository<PayRevisions, Long>{

	List<PayRevisions> findAllById(long id);

	List<PayRevisions> findAllByPayRegister(PayRegister payRegister);

	List<PayRevisions> findAllByUserId(long userId);

	List<PayRevisions> findAllByUserIdAndIsDeleted(long userId, boolean b);
	
	List<PayRevisions> findAllByIsDeleted(boolean b);

}
