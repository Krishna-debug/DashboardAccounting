package com.krishna.dto;

import java.util.List;

public class SecurityGroupUserDTO {
	
	private Long securityId;
	private String securityName;
	private List roles;
	private String grade;
	private Long securityOrder;
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
	public List getRoles() {
		return roles;
	}
	public void setRoles(List roles) {
		this.roles = roles;
	}
	public String getGrade() {
		return grade;
	}
	public void setGrade(String grade) {
		this.grade = grade;
	}
	public Long getSecurityOrder() {
		return securityOrder;
	}
	public void setSecurityOrder(Long securityOrder) {
		this.securityOrder = securityOrder;
	}
	@Override
	public String toString() {
		return "SecurityGroupUserDTO [securityId=" + securityId + ", securityName=" + securityName + ", roles=" + roles
				+ ", grade=" + grade + "]";
	}
	
	

}
