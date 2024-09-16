package com.krishna.repository.invoice;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.krishna.domain.DollarCost;

@Repository
public interface DollarCostRepository extends JpaRepository<DollarCost, Long> {
	
	@Cacheable("findAllByIsDeletedOrderByMonthAsc")
	List<DollarCost> findAllByIsDeletedOrderByMonthAsc(boolean isDeleted);
	
	@Cacheable("findByMonthAndIsDeletedAndYear")
	DollarCost findByMonthAndIsDeletedAndYear(int month, boolean isDeleted, int year);

	@Cacheable("findAllByIsDeletedOrderByMonthAscYearAsc")
	List<DollarCost> findAllByIsDeletedOrderByMonthAscYearAsc(boolean b);

	@Cacheable("findAllByIsDeletedOrderByYearAsc")
	List<DollarCost> findAllByIsDeletedOrderByYearAsc(boolean b);

	@Cacheable("findAllByIsDeletedOrderByYearAscMonthAsc")
	List<DollarCost> findAllByIsDeletedOrderByYearAscMonthAsc(boolean b);

	@Cacheable("findAllByYearAndIsDeletedOrderByYearDescMonthDesc")
	List<DollarCost> findAllByYearAndIsDeletedOrderByYearDescMonthDesc(int year, boolean b);

	@Cacheable("findAllByIsDeletedOrderByYearDescMonthDesc")
	List<DollarCost> findAllByIsDeletedOrderByYearDescMonthDesc(boolean b);

	@CacheEvict(value= {"findAllByIsDeletedOrderByMonthAsc","findByMonthAndIsDeletedAndYear","findAllByIsDeletedOrderByMonthAscYearAsc",
"findAllByIsDeletedOrderByYearAsc","findAllByIsDeletedOrderByYearAscMonthAsc","findAllByYearAndIsDeletedOrderByYearDescMonthDesc","findAllByIsDeletedOrderByYearDescMonthDesc"}, allEntries=true)
	<S extends DollarCost> S save(S entity);
	
	@CacheEvict(value= {"findAllByIsDeletedOrderByMonthAsc","findByMonthAndIsDeletedAndYear","findAllByIsDeletedOrderByMonthAscYearAsc",
	"findAllByIsDeletedOrderByYearAsc","findAllByIsDeletedOrderByYearAscMonthAsc","findAllByYearAndIsDeletedOrderByYearDescMonthDesc","findAllByIsDeletedOrderByYearDescMonthDesc"}, allEntries=true)
	 <S extends DollarCost> S saveAndFlush(S entity);
	
	
}
