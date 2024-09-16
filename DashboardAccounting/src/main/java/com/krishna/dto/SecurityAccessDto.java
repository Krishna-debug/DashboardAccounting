package com.krishna.dto;

public class SecurityAccessDto {
	
	private Long securityId;
	private String securityName;
	private String module;
	private String securityType;
	private Long securityOrder;
	private boolean view;
	private boolean edit;
	private boolean delete;
	public Long getSecurityId() {
		return securityId;
	}
	public void setSecurityId(Long securityId) {
		this.securityId = securityId;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getSecurityType() {
		return securityType;
	}
	public void setSecurityType(String securityType) {
		this.securityType = securityType;
	}
	
	public boolean isView() {
		return view;
	}
	public void setView(boolean view) {
		this.view = view;
	}
	public boolean isEdit() {
		return edit;
	}
	public void setEdit(boolean edit) {
		this.edit = edit;
	}
	public boolean isDelete() {
		return delete;
	}
	public void setDelete(boolean delete) {
		this.delete = delete;
	}
	public String getSecurityName() {
		return securityName;
	}
	public void setSecurityName(String securityName) {
		this.securityName = securityName;
	}
	public Long getSecurityOrder() {
		return securityOrder;
	}
	public void setSecurityOrder(Long securityOrder) {
		this.securityOrder = securityOrder;
	}
	@Override
	public String toString() {
		return "SecurityAccessDto [securityId=" + securityId + ", securityName=" + securityName + ", module=" + module
				+ ", securityType=" + securityType + ", securityOrder=" + securityOrder + ", view=" + view + ", edit="
				+ edit + ", delete=" + delete + "]";
	}
	
	
	
	
	

}
