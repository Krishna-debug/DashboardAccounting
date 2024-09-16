package com.krishna.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.krishna.domain.BuSpecificCost;
import com.krishna.domain.BuSpecificType;


public interface BuSpecificCostRepository extends  JpaRepository<BuSpecificCost, Long> {

    Optional<BuSpecificCost> findById(long costId);

    List<BuSpecificCost> findAllByYearAndMonthAndBusinessVerticalAndDeleted(Integer year, Integer month, String bu,
            boolean b);

    List<BuSpecificCost> findAllByYearAndBusinessVerticalAndDeleted(Integer year, String bu, boolean b);

    List<BuSpecificCost> findAllByYearAndMonthAndDeleted(Integer year, Integer month, boolean b);

    List<BuSpecificCost> findAllByYearAndDeleted(Integer year, boolean b);

    List<BuSpecificCost> findAllByBusinessVerticalAndDeleted(String bu, boolean b);

    List<BuSpecificCost> findAllByDeleted(boolean b);

    @Query(value = "select amount from bu_specific_cost where year=:year and month=:month and business_vertical=:bu and is_deleted=:b", nativeQuery = true)
    List<Double> findAmountByYearAndMonthAndBusinessVerticalAndDeleted(Integer year, Integer month, String bu,
    boolean b);

   // @Query(value="select id from BuSpecificCost where type:=type")
    List<BuSpecificCost> findIdByType(BuSpecificType type);
    
}
