package com.krishna.repository.payroll;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.ArrearFile;

public interface ArrearFileRepository extends JpaRepository<ArrearFile, Long>{

	List<ArrearFile> findAllByArrearIdAndIsDeleted(long id, boolean b);

}
