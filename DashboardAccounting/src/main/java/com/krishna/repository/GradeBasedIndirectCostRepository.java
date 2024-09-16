package com.krishna.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.krishna.controller.IndirectCostController;
import com.krishna.domain.GradeBasedIndirectCost;
import com.krishna.service.IndirectCostService;

/**
 * <p>Service Repository for {@link GradeBasedIndirectCost}
 *  referenced in {@link IndirectCostService} also see
 *  {@link IndirectCostController#getAllGradeBasedIndirectCost(int, int, String)}
 *  and {@link IndirectCostController#saveFixedCost(String, int, int, double, String)}
 *  <p>
 * @author Amit Mishra
 *
 */
@Repository
public interface GradeBasedIndirectCostRepository extends JpaRepository<GradeBasedIndirectCost, Long> {
	
	Optional<GradeBasedIndirectCost> findByGradeAndMonthAndYear(String grade, int month, int year);
	
	List<GradeBasedIndirectCost> findAllByMonthAndYearAndIsVariable(int month, int year, boolean isVariable);

	Optional<List<GradeBasedIndirectCost>> findAllByYearAndIsVariable(int year, boolean isVariable);
	
	@Query(value="Select * from grade_based_indirect_cost where grade in (:grade) and month=:month and year=:year",nativeQuery=true)
	List<GradeBasedIndirectCost> findAllByGradeInAndMonthAndYear(List<String> grade, int month, int year);	
}
