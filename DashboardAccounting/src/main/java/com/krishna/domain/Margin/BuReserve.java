package com.krishna.domain.Margin;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

@Entity
@Table
@Audited
public class BuReserve {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	/*ID*/	
	private Long id;
	/*Name of the Business Unit*/
	private String buName;
	/*Id of the Business Unit*/
	private Long buId;
	/*Month in Integer*/
	private Integer month;
	/*Year in Integer*/
	private Integer year;
	/*Value of BU reserve Margin in percentage*/
	private Double marginPerc;
	/*Value of amount of BU reserve*/
	private Double monthlyReserveAmount;
	/*Total Reserve Amount*/
	private Double totalReserve;
	/*Deducted Amount from BU*/
	private Double deductedAmount=0D;
	/*Comment by Head*/
	private String remarks;
	/*If is Deleted*/
	private Boolean isDeleted;
	/*Value of Amount of margin*/
	private Double margin;
	/*Disputed Amount Value*/
	private Double disputedAmount;
	/*Disputed Amount percentage*/
	private Double disputedPerc;
	/*Total overall cost of BU*/
	private Double totalCost;
	/*Reserved as a surplus amount from Business Unit*/
	private Double surplusReserve;
	/*Target Reserve Amount*/
	private Double targetReserve;
	/*Revenue from each Business Unit*/
	private Double revenue;
	
	private boolean isReserveChanged=true;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBuName() {
		return buName;
	}

	public void setBuName(String buName) {
		this.buName = buName;
	}

	public Long getBuId() {
		return buId;
	}

	public void setBuId(Long buId) {
		this.buId = buId;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Double getMonthlyReserveAmount() {
		return monthlyReserveAmount;
	}

	public void setMonthlyReserveAmount(Double monthlyReserveAmount) {
		this.monthlyReserveAmount = monthlyReserveAmount;
	}

	public Double getTotalReserve() {
		return totalReserve;
	}

	public void setTotalReserve(Double totalReserve) {
		this.totalReserve = totalReserve;
	}

	public Double getDeductedAmount() {
		return deductedAmount;
	}

	public void setDeductedAmount(Double deductedAmount) {
		this.deductedAmount = deductedAmount;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public Double getSurplusReserve() {
		return surplusReserve;
	}

	public void setSurplusReserve(Double surplusReserve) {
		this.surplusReserve = surplusReserve;
	}

	public Double getTargetReserve() {
		return targetReserve;
	}

	public void setTargetReserve(Double targetReserve) {
		this.targetReserve = targetReserve;
	}

	public Double getMargin() {
		return margin;
	}

	public void setMargin(Double margin) {
		this.margin = margin;
	}

	public Double getDisputedAmount() {
		return disputedAmount;
	}

	public void setDisputedAmount(Double disputedAmount) {
		this.disputedAmount = disputedAmount;
	}

	public Double getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(Double totalCost) {
		this.totalCost = totalCost;
	}

	public Double getRevenue() {
		return revenue;
	}

	public void setRevenue(Double revenue) {
		this.revenue = revenue;
	}

	public Double getMarginPerc() {
		return marginPerc;
	}

	public void setMarginPerc(Double marginPerc) {
		this.marginPerc = marginPerc;
	}

	public Double getDisputedPerc() {
		return disputedPerc;
	}

	public void setDisputedPerc(Double disputedPerc) {
		this.disputedPerc = disputedPerc;
	}

	public boolean isReserveChanged() {
		return isReserveChanged;
	}

	public void setReserveChanged(boolean isReserveChanged) {
		this.isReserveChanged = isReserveChanged;
	}


}
