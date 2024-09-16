package com.krishna.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.krishna.domain.BuSpecificCost;
import com.krishna.domain.BuSpecificType;
import com.krishna.dto.BuSpecificCostDto;

public interface BuSpecificCostService {
    public BuSpecificCost addBuSpecificCost(BuSpecificCostDto buSpecificCostDto, long userId);

    public BuSpecificCost deleteBuSpecificCost(Long costId);

    public BuSpecificCost updateBuSpecificCost(BuSpecificCostDto buSpecificCostDto, long userId);

    public List<Map<String,Object>> getBuSpecificCost(Integer year, String bu, Integer month);

    public List<Map<String,Object>> getBuSpecificTypes();

    public BuSpecificType addBuSpecificType(String type);

    public BuSpecificType updateBuType(long id, String name);

    public boolean deleteBuSpecificType(long id);




}
