package com.krishna.dto;

public class AttendanceCommentDto {

	int month;

	int year;

	long userId;

	String comment;

	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}

	public long getUserId() {
		return userId;
	}

	public String getComment() {
		return comment;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
