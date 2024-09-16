package com.krishna.dto;

import java.util.List;

public class UserIdsDto {
	public List<Long> userIds;

	public UserIdsDto() {
		
	}
	
	public UserIdsDto(List<Long> userIds) {
		super();
		this.userIds = userIds;
	}

	public List<Long> getUserIds() {
		return userIds;
	}

	public void setUserIds(List<Long> userIds) {
		this.userIds = userIds;
	}
}
