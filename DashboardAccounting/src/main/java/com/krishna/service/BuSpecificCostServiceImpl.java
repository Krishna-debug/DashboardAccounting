package com.krishna.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.krishna.domain.BuSpecificCost;
import com.krishna.domain.BuSpecificType;
import com.krishna.dto.BuSpecificCostDto;
import com.krishna.repository.BuSpecifcTypeRepository;
import com.krishna.repository.BuSpecificCostRepository;


@Service
public class BuSpecificCostServiceImpl implements BuSpecificCostService {

    @Autowired
    BuSpecificCostRepository buSpecificCostRepository;

    @Autowired
    BuSpecifcTypeRepository buSpecificTypeRepo;
    
    @Override
    public BuSpecificCost addBuSpecificCost(BuSpecificCostDto buSpecificCostDto,long userId){
        BuSpecificCost buSpecificCost = new BuSpecificCost();
            buSpecificCost.setAmount(buSpecificCostDto.getAmount());
            buSpecificCost.setBusinessVertical(buSpecificCostDto.getBusinessVertical());
            buSpecificCost.setComment(buSpecificCostDto.getComment());
            buSpecificCost.setMonth(buSpecificCostDto.getMonth()+1);
            buSpecificCost.setYear(buSpecificCostDto.getYear());
            BuSpecificType buType = buSpecificTypeRepo.findById(buSpecificCostDto.getTypeId());
            if(buType != null)
                buSpecificCost.setType(buType);
            buSpecificCost.setCreatedBy(userId);
            buSpecificCost.setCreateOn(new Date());
            buSpecificCost.setDeleted(false);
            buSpecificCostRepository.save(buSpecificCost);
            
        return buSpecificCost;
    }

    @Override
    public BuSpecificCost deleteBuSpecificCost(Long costId) {
		Optional<BuSpecificCost> buSpecCost = buSpecificCostRepository.findById(costId);
		if(buSpecCost.isPresent()) {
			buSpecCost.get().setDeleted(true);
			buSpecificCostRepository.save(buSpecCost.get());
			return buSpecCost.get();
		}
		return null;
	}

    @Override
    public BuSpecificCost updateBuSpecificCost(BuSpecificCostDto buSpecificCostDto, long userId) {
		Optional<BuSpecificCost> data = buSpecificCostRepository.findById(buSpecificCostDto.getId());
		BuSpecificCost buSpecificCost = data.get();
		if(data.isPresent()) {
            buSpecificCost.setAmount(buSpecificCostDto.getAmount());
            buSpecificCost.setBusinessVertical(buSpecificCostDto.getBusinessVertical());
            buSpecificCost.setComment(buSpecificCostDto.getComment());
            buSpecificCost.setMonth(buSpecificCostDto.getMonth()+1);
            buSpecificCost.setYear(buSpecificCostDto.getYear());
            BuSpecificType buType = buSpecificTypeRepo.findById(buSpecificCostDto.getTypeId());
            if(buType != null)
                buSpecificCost.setType(buType);
            buSpecificCost.setLastModifiedBy(userId);
            buSpecificCost.setLastModifiedOn(new Date());
			buSpecificCostRepository.save(buSpecificCost);
		}
		return buSpecificCost;
	}

    @Override
    public List<Map<String,Object>> getBuSpecificCost(Integer year, String bu, Integer month){
        List<BuSpecificCost> buSpecificCost = null;
        List<Map<String,Object>> response = new ArrayList<>();
        if((bu != "") && (year != null) && (month != null))
            buSpecificCost = buSpecificCostRepository.findAllByYearAndMonthAndBusinessVerticalAndDeleted(year,month,bu,false);

        else if((bu != "") && (year != null) && (month == null))
            buSpecificCost = buSpecificCostRepository.findAllByYearAndBusinessVerticalAndDeleted(year,bu,false);

        else if(bu == "" && month != null)
            buSpecificCost = buSpecificCostRepository.findAllByYearAndMonthAndDeleted(year,month,false);
        else
            buSpecificCost = buSpecificCostRepository.findAllByYearAndDeleted(year,false);

       
            buSpecificCost.forEach(bCost-> {
                Map<String,Object> map = new HashMap<>();
                map.put("type", bCost.getType());
                map.put("amount", bCost.getAmount());
                map.put("businessVertical", bCost.getBusinessVertical());
                map.put("comment", bCost.getComment());
                map.put("month", bCost.getMonth());
                map.put("year", bCost.getYear());
                map.put("id", bCost.getId());
               
                response.add(map);

            });

            

        return response;
    }

    @Override
    public List<Map<String,Object>> getBuSpecificTypes(){
        List<Map<String,Object>> res = new ArrayList<>();
        List<BuSpecificType> buTypeList = buSpecificTypeRepo.findAll();
        buTypeList.forEach(val -> {
            Map<String,Object> map = new HashMap<>();

            map.put("id", val.getId());
            map.put("typeName", val.getTypeName());
            res.add(map);
        });
        return res;
    }

    @Override
    public BuSpecificType addBuSpecificType(String type){
        BuSpecificType bType = buSpecificTypeRepo.findByTypeName(type);
        if(bType == null){
            BuSpecificType buType = new BuSpecificType();
            buType.setTypeName(type);
            buType.setIsDeleted(false);
            buSpecificTypeRepo.save(buType);
            return buType;
        }
        return null;
        
    }

    @Override
    public BuSpecificType updateBuType(long id, String name){
        BuSpecificType bType = buSpecificTypeRepo.findById(id);
        if(bType != null){
            bType.setTypeName(name);
            buSpecificTypeRepo.save(bType);
        }
       return bType; 
    }

    @Override
    public boolean deleteBuSpecificType(long id){
        BuSpecificType bType = buSpecificTypeRepo.findById(id);
        List<BuSpecificCost> buSpecificCostIdList  = new ArrayList<>();
        buSpecificCostIdList = buSpecificCostRepository.findIdByType(bType);
        if(bType != null && buSpecificCostIdList.isEmpty()){
            bType.setIsDeleted(true);
            buSpecificTypeRepo.save(bType);
            return true;
        }
        return false;
    }

}
