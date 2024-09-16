package com.krishna.dto;

public class AclMemberDto implements Comparable<AclMemberDto> {
	private Long securityId;
	private String securityName;
	private String module;
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
	
	public String getSecurityName() {
		return securityName;
	}
	public void setSecurityName(String securityName) {
		this.securityName = securityName;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
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
	public Long getSecurityOrder() {
		return securityOrder;
	}
	public void setSecurityOrder(Long securityOrder) {
		this.securityOrder = securityOrder;
	}


	@Override
	public int compareTo(AclMemberDto arg0) {
		// TODO Auto-generated method stub
		return (this.getSecurityId() < arg0.getSecurityId() ? -1 : 
            (this.getSecurityId() == arg0.getSecurityId() ? 0 : 1)); 
	}
	
	
	

}
