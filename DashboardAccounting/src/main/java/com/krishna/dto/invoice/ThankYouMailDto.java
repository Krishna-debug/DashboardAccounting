package com.krishna.dto.invoice;

import java.util.List;

public class ThankYouMailDto {
	
	private String toMail;
	
	private List<String> ccMail;
	
	private String action;
	
	private String html;
	
	private Long invoiceId;
	
	private Boolean isIfsd;
	
	private String skipComment;

	public String getToMail() {
		return toMail;
	}

	public void setToMail(String toMail) {
		this.toMail = toMail;
	}

	public List<String> getCcMail() {
		return ccMail;
	}

	public void setCcMail(List<String> ccMail) {
		this.ccMail = ccMail;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public Long getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(Long invoiceId) {
		this.invoiceId = invoiceId;
	}

	public Boolean getIsIfsd() {
		return isIfsd;
	}

	public void setIsIfsd(Boolean isIfsd) {
		this.isIfsd = isIfsd;
	}

	public String getSkipComment() {
		return skipComment;
	}

	public void setSkipComment(String skipComment) {
		this.skipComment = skipComment;
	}
	
}
