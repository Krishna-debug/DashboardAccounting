package com.krishna.dto;

import com.krishna.domain.GradeBasedIndirectCost;
import com.krishna.service.IndirectCostService;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>Data transfer object for {@linkplain GradeBasedIndirectCost}
 * used in {@linkplain IndirectCostService#getGradeWiseIndirectCost(int, int, String)}
 * </p>
 * @author Amit Mishra
 */
@NoArgsConstructor
public @Data class IndirectCostGradeBasedDTO {
	
	private String grade;
	private double userCount;
	private double fixedCost;
	private double referenceCost;
	private boolean isVariable;
	private double totalCommulativeCost;
	private double companyCount;
    private double buCost;
    private Long rank;

}
