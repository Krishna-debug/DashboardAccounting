package com.krishna.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.springframework.lang.NonNull;

@Entity
@Table
@Audited
public class AttendanceVerification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*ID*/
	private long id;

	/*USer Id*/
	private long userId;
	/*If attendance is verified*/
	private boolean isAttendanceVerified;
	/*If entry is deleted*/
	private boolean isDeleted;
	/*Month in Integer*/
	private int month;
	/*Year in Integer*/
	private int year;
	/*Comment for attendance verification*/
	private String attendanceComment;

	public long getId() {
		return id;
	}

	public long getUserId() {
		return userId;
	}

	public boolean isAttendanceVerified() {
		return isAttendanceVerified;
	}

	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public void setAttendanceVerified(boolean isAttendanceVerified) {
		this.isAttendanceVerified = isAttendanceVerified;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getComment() {
		return attendanceComment;
	}

	public void setComment(String attendanceComment) {
		this.attendanceComment = attendanceComment;
	}
	
	
	public String getAttendanceComment() {
		return attendanceComment;
	}

	public void setAttendanceComment(String attendanceComment) {
		this.attendanceComment = attendanceComment;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	@Override
	public String toString() {
		return "AttendanceVerification [id=" + id + ", userId=" + userId + ", isAttendanceVerified="
				+ isAttendanceVerified + ", month=" + month + ", year=" + year + "]";
	}

}
