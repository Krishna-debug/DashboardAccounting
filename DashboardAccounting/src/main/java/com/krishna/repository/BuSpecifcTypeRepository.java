package com.krishna.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.BuSpecificType;

public interface BuSpecifcTypeRepository extends JpaRepository<BuSpecificType,Long> {
    public BuSpecificType findById(long id);

    public BuSpecificType findByTypeName(String type);

    public List<BuSpecificType> findAll();
}
