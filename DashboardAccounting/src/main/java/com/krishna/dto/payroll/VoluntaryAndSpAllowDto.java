package com.krishna.dto.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoluntaryAndSpAllowDto {
	
	private Long userId;
	private Double specialAllowance;
	private Double voluntaryPayAmount;

	public VoluntaryAndSpAllowDto(Object[] columns) {
        this.userId = (columns[0] != null)?Long.parseLong(columns[0].toString()):0;
        this.specialAllowance = Double.parseDouble(columns[1].toString()) ;
        this.voluntaryPayAmount = Double.parseDouble(columns[2].toString()) ;
    }

}
