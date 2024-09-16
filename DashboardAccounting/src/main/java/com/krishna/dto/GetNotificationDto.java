package com.krishna.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class GetNotificationDto {

	
	private List<SearchFilter> searchFilter;
	private Long startDate;
	private Long endDate;
	private String notificationType;
	private String offset;
	private String maxSize;
	//private long id;

	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	public static class SearchFilter {
		private String fromEmployee;
		private String toEmployees;
		private String ccEmployee;
		private String status;
		private String subject;
		private String type;
	}
}
