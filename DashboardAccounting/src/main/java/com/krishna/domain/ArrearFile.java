package com.krishna.domain;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

@Audited
@Entity
@Table
public class ArrearFile {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	/*ID*/
	private long id;
	/*Path of file value*/
	private String filePath;
	/*User Id Of file uploader*/
	private long uploadedBy;
	/*name of the file*/
	private String fileName;
	/*If is Deleted*/
	private boolean isDeleted = false;
	/*Date of File Upload*/
	private LocalDateTime uploadedDate;
	/*Name of the user uploading file*/
	private String fileUploaderName;
	/*Arrear ID*/
	private long arrearId;
	
	public long getId() {
		return id;
	}

	public String getFilePath() {
		return filePath;
	}

	public long getUploadedBy() {
		return uploadedBy;
	}

	public String getFileName() {
		return fileName;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public LocalDateTime getUploadedDate() {
		return uploadedDate;
	}

	public String getFileUploaderName() {
		return fileUploaderName;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void setUploadedBy(long uploadedBy) {
		this.uploadedBy = uploadedBy;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public void setUploadedDate(LocalDateTime uploadedDate) {
		this.uploadedDate = uploadedDate;
	}

	public void setFileUploaderName(String fileUploaderName) {
		this.fileUploaderName = fileUploaderName;
	}

	public long getArrearId() {
		return arrearId;
	}

	public void setArrearId(long arrearId) {
		this.arrearId = arrearId;
	}

	@Override
	public String toString() {
		return "ArrearFile [id=" + id + ", filePath=" + filePath + ", uploadedBy=" + uploadedBy + ", fileName="
				+ fileName + ", isDeleted=" + isDeleted + ", uploadedDate=" + uploadedDate + ", fileUploaderName="
				+ fileUploaderName + ", arrearId=" + arrearId + "]";
	}

}
