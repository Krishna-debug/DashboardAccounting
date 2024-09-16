package com.krishna.service.invoice;

import java.math.BigInteger;
import java.text.DateFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.krishna.controller.FeignLegacyInterface;
import com.krishna.domain.RevInfo;
import com.krishna.domain.invoice.InvoiceBank;
import com.krishna.domain.invoice.InvoiceCycle;
import com.krishna.domain.invoice.PaymentMode;
import com.krishna.domain.invoice.PaymentTerms;
import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.repository.invoice.InvoiceBankRepository;
import com.krishna.repository.invoice.InvoiceCycleRepository;
import com.krishna.repository.invoice.PaymentModeRepository;
import com.krishna.repository.invoice.PaymentTermsRepository;
import com.krishna.util.ConstantUtility;


@Service
public class InvoiceRevisionService {
	
	@Autowired
	FeignLegacyInterface feignLegacyInterface;
	
	@Autowired
	InvoiceBankRepository bankRepository;
	
	@Autowired
	public InvoiceCycleRepository invoiceCycleRepository;
	
	@Autowired
	public PaymentModeRepository paymentModeRepository;
	
	@Autowired
	public PaymentTermsRepository paymentTermsRepository;
	
	@Autowired
	EntityManager entityManager;
	
	public List<Map<String, Object>> getInvoiceHistory(String accessToken, Long projectInvoiceId) {
		List<Map<String, Object>> resultHistory = new ArrayList<>();
		Map<Date,Object> res=new HashMap<>();
		AuditReader auditReader = AuditReaderFactory.get(entityManager);
		AuditQuery auditQuery = auditReader.createQuery().forRevisionsOfEntity(ProjectInvoice.class, false, true);
		auditQuery.add(AuditEntity.id().eq(projectInvoiceId));
		List<Object[]> auditList = auditQuery.getResultList();
		List<ProjectInvoice> projectInvoice = new ArrayList<>();
		
		Map<String, Object> name = (Map<String, Object>) feignLegacyInterface.getUserNameAndDob(accessToken);
		List<Map<String, Object>> nameList = (List<Map<String, Object>>) name.get(ConstantUtility.DATA_);
		
		
		if (!auditList.isEmpty()) {
			for (int index = auditList.size() - 1; index >= 0; index--) {
				if(index >0) {
					Object[] previousData = auditList.get(index - 1);
					Object[] currData = auditList.get(index);
					ProjectInvoice current = (ProjectInvoice) currData[0];
					ProjectInvoice previous = (ProjectInvoice) previousData[0];
					RevInfo revInfo = (RevInfo) currData[1];
					RevisionType revisionType = (RevisionType) previousData[2];
					int revTypeId=revisionType.getRepresentation();
					String revType="";
					if(revTypeId==0) {
						 revType="Created";
					}
					else if(revTypeId==1) {
						 revType="Updated";
					}
					else if(revTypeId==2) {
						 revType="Deleted";
					}
					if (Objects.nonNull(previous.getInvoiceStatus()) &&Objects.nonNull(current.getInvoiceStatus()))
						if(current.getInvoiceStatus()!=previous.getInvoiceStatus()) {
							resultHistory.add(responseHistory(previous, current, 1 ,revInfo,revType,nameList));
					}
					if (Objects.nonNull(previous.getComment()) &&Objects.nonNull(current.getComment()))
						if(!current.getComment().equals(previous.getComment())) {
							resultHistory.add(responseHistory(previous, current, 2 ,revInfo,revType,nameList));
						}
					
					if (Objects.nonNull(previous.getAmount()) && Objects.nonNull(current.getAmount()))
							if(current.getAmount()!=previous.getAmount()) {
								resultHistory.add(responseHistory(previous, current, 3 ,revInfo,revType,nameList));
					
					}
					if (Objects.nonNull(previous.getCurrency()) &&Objects.nonNull(current.getCurrency()))
						if(!current.getCurrency().equals(previous.getCurrency())) {
							resultHistory.add(responseHistory(previous, current, 4 ,revInfo,revType,nameList));
					}
					if (Objects.nonNull(previous.getBillingDate()) &&Objects.nonNull(current.getBillingDate()))
						if(!current.getBillingDate().equals(previous.getBillingDate())) {
							resultHistory.add(responseHistory(previous, current, 5 ,revInfo,revType,nameList));
					}
					if (Objects.nonNull(previous.getDueDate()) &&Objects.nonNull(current.getDueDate()))
						if(!current.getDueDate().equals(previous.getDueDate())) {
							resultHistory.add(responseHistory(previous, current, 6 ,revInfo,revType,nameList));
					}
					if (Objects.nonNull(previous.getPlaceOfSupply()) &&Objects.nonNull(current.getPlaceOfSupply()))
						if(!current.getPlaceOfSupply().equals(previous.getPlaceOfSupply())) {
							resultHistory.add(responseHistory(previous, current, 7 ,revInfo,revType,nameList));
					}
					if (Objects.nonNull(previous.getCityOfSupply()) &&Objects.nonNull(current.getCityOfSupply()))
						if(!current.getCityOfSupply().equals(previous.getCityOfSupply())) {
							resultHistory.add(responseHistory(previous, current, 8 ,revInfo,revType,nameList));
					}
					if (Objects.nonNull(previous.getPayStatus()) &&Objects.nonNull(current.getPayStatus()))
						if(!current.getPayStatus().equals(previous.getPayStatus())) {
							resultHistory.add(responseHistory(previous, current, 9 ,revInfo,revType,nameList));
					}
					if (Objects.nonNull(previous.getMonth()) &&Objects.nonNull(current.getMonth()))
						if(!current.getMonth().equals(previous.getMonth())) {
							resultHistory.add(responseHistory(previous, current, 10 ,revInfo,revType,nameList));
					}
					if (Objects.nonNull(previous.getYear()) &&Objects.nonNull(current.getYear()))
						if(!current.getYear().equals(previous.getYear())) {
							resultHistory.add(responseHistory(previous, current, 11 ,revInfo,revType,nameList));
					}
					if (Objects.nonNull(previous.getReceivedOn()) &&Objects.nonNull(current.getReceivedOn()))
						if(!current.getReceivedOn().equals(previous.getReceivedOn())) {
							resultHistory.add(responseHistory(previous, current, 12 ,revInfo,revType,nameList));
					}
					if (Objects.nonNull(previous.getBuHeadComment()) &&Objects.nonNull(current.getBuHeadComment()))
						if(!current.getBuHeadComment().equals(previous.getBuHeadComment())) {
							resultHistory.add(responseHistory(previous, current, 13 ,revInfo,revType,nameList));
					}
					if (Objects.nonNull(previous.getTaxable()) &&Objects.nonNull(current.getTaxable()))
						if(!current.getTaxable().equals(previous.getTaxable())) {
							resultHistory.add(responseHistory(previous, current, 14 ,revInfo,revType,nameList));
					}
					if (Objects.nonNull(previous.getModeOfPaymentId()) &&Objects.nonNull(current.getModeOfPaymentId()))
						if(current.getModeOfPaymentId()!=previous.getModeOfPaymentId()) {
							resultHistory.add(responseHistory(previous, current, 15 ,revInfo,revType,nameList));
					}
					if (Objects.nonNull(previous.getPaymentTermsId()) &&Objects.nonNull(current.getPaymentTermsId()))
						if(current.getPaymentTermsId()!=previous.getPaymentTermsId()) {
							resultHistory.add(responseHistory(previous, current, 16 ,revInfo,revType,nameList));
					}
					if (Objects.nonNull(previous.getInvoiceCycleId()) &&Objects.nonNull(current.getInvoiceCycleId()))
						if(current.getInvoiceCycleId()!=previous.getInvoiceCycleId()) {
							resultHistory.add(responseHistory(previous, current, 17 ,revInfo,revType,nameList));
					}
					if (Objects.nonNull(previous.getBank()) &&Objects.nonNull(current.getBank()))
						if(!current.getBank().equals(previous.getBank())) {
							resultHistory.add(responseHistory(previous, current, 18 ,revInfo,revType,nameList));
					}
					if(Objects.nonNull(previous.getSkipComment()) && Objects.nonNull(current.getSkipComment())) {
						if(!current.getSkipComment().equals(previous.getSkipComment())) {
							resultHistory.add(responseHistory(previous, current, 19 , revInfo, revType,nameList));
						}
					}
				}
				
			}
			if(auditList.size()==1) {
				Object[] createdData = auditList.get(auditList.size()-1);
				ProjectInvoice current = (ProjectInvoice) createdData[0];
				ProjectInvoice previous = (ProjectInvoice) createdData[0];
				RevInfo revInfo = (RevInfo) createdData[1];
				RevisionType revisionType = (RevisionType) createdData[2];
				int revTypeId=revisionType.getRepresentation();
				String revType="";
				if(revTypeId==0) {
					 revType="Created";
				}
				if(current.getAmount()!=0) {
				if (Objects.nonNull(current.getAmount())){
						resultHistory.add(responseHistory(previous, current, 60 ,revInfo,revType,nameList));
				}
				}
			}
			
		}
		return resultHistory;
	}
	
	private Map<String, Object> responseHistory(ProjectInvoice previous, ProjectInvoice current, int i,
			RevInfo revInfo, String revType, List<Map<String, Object>> nameList) {
		Map<String, Object> res = new HashMap<>();
		Map<Long, String> invoiceStatus = new HashMap<>();
		invoiceStatus.put((long) 1, "Pending");
		invoiceStatus.put((long) 2, "Paid");
		invoiceStatus.put((long) 3, "In Transit");
		invoiceStatus.put((long) 4, "Within Due Date");
		invoiceStatus.put((long) 5, "Disputed");
		invoiceStatus.put((long) 6, "Refunded");
		
		if (i == 1) {
			res.put("field", "status");
			res.put("current", invoiceStatus.get(current.getInvoiceStatus()));
			res.put("previous",invoiceStatus.get(previous.getInvoiceStatus()));
			res.put("operation",revType);
		}
		else if (i == 2) {
			res.put("field", "comment");
			res.put("current", current.getComment());
			res.put("previous", previous.getComment());
			res.put("operation",revType);
		}
		else if (i == 3){
			res.put("field", "invoiceAmount");
			res.put("current", current.getAmount());
			res.put("previous", previous.getAmount());
			res.put("operation",revType);
		}else if (i == 4) {
			res.put("field", "currency");
			res.put("current", current.getCurrency());
			res.put("previous", previous.getCurrency());
			res.put("operation",revType);
		}else if (i == 5) {
			res.put("field", "billingDate");
			res.put("current", current.getBillingDate());
			res.put("previous", previous.getBillingDate());
			res.put("operation",revType);
		}else if (i == 6) {
			res.put("field", "dueDate");
			res.put("current", current.getDueDate());
			res.put("previous", previous.getDueDate());
			res.put("operation",revType);
		}else if (i == 7) {
			res.put("field", "placeOfSupply");
			res.put("current", current.getPlaceOfSupply());
			res.put("previous", previous.getPlaceOfSupply());
			res.put("operation",revType);
		}else if (i == 8) {
			res.put("field", "cityOfSupply");
			res.put("current", current.getCityOfSupply());
			res.put("previous", previous.getCityOfSupply());
			res.put("operation",revType);
		}else if (i == 9) {
			res.put("field", "payStatus");
			res.put("current", current.getPayStatus());
			res.put("previous", previous.getPayStatus());
			res.put("operation",revType);
		}else if (i == 10) {
			res.put("field", "month");
			res.put("current", current.getMonth());
			res.put("previous", previous.getMonth());
			res.put("operation",revType);
		}else if (i == 11) {
			res.put("field", "year");
			res.put("current", current.getYear());
			res.put("previous", previous.getYear());
			res.put("operation",revType);
		}else if (i == 12) {
			res.put("field", "receivedOn");
			res.put("current", current.getReceivedOn());
			res.put("previous", previous.getReceivedOn());
			res.put("operation",revType);
		}else if (i == 13) {
			res.put("field", "buHeadComment");
			res.put("current", current.getBuHeadComment());
			res.put("previous", previous.getBuHeadComment());
			res.put("operation",revType);
		}else if (i == 14) {
			res.put("field", "taxable");
			res.put("current", current.getTaxable());
			res.put("previous", previous.getTaxable());
			res.put("operation",revType);
		}else if (i == 15) {
			res.put("field", "modeOfPayment");
			PaymentMode PaymentModeCurrent =paymentModeRepository.findById(current.getModeOfPaymentId());
			PaymentMode PaymentModePrevious =paymentModeRepository.findById(previous.getModeOfPaymentId());
			res.put("current", PaymentModeCurrent.getPaymentModeType());
			res.put("previous",PaymentModePrevious.getPaymentModeType());
			res.put("operation",revType);
		}else if (i == 16) {
			res.put("field", "paymentTerm");
			PaymentTerms paymentTermsCurrent = paymentTermsRepository.findById(current.getPaymentTermsId());
			PaymentTerms paymentTermsPrevious = paymentTermsRepository.findById(previous.getPaymentTermsId());
			res.put("current", paymentTermsCurrent.getPaymentTermsType());
			res.put("previous", paymentTermsPrevious.getPaymentTermsType());
			res.put("operation",revType);
		}else if (i == 17) {
			res.put("field", "billingCycle");
			InvoiceCycle invoiceCycleCurrent = invoiceCycleRepository.findById(current.getInvoiceCycleId());
			InvoiceCycle invoiceCyclePrevious = invoiceCycleRepository.findById(previous.getInvoiceCycleId());
			res.put("current", invoiceCycleCurrent.getInvoiceCycleType());
			res.put("previous", invoiceCyclePrevious.getInvoiceCycleType());
			res.put("operation",revType);
		}else if (i == 18) {
			res.put("field", "bank");
			InvoiceBank invoiceBankCurrent = bankRepository.findByIdAndIsArchived(current.getBank().getId(), false);
			InvoiceBank invoiceBankPrevious = bankRepository.findByIdAndIsArchived(previous.getBank().getId(), false);
			res.put("current", invoiceBankCurrent.getName());
			res.put("previous", invoiceBankPrevious.getName());
			res.put("operation",revType);
		}else if(i == 19) {
			res.put("field", "skipComment");
			res.put("current", current.getSkipComment());
			res.put("previous", previous.getSkipComment());
			res.put("operation", revType);
		}else if (i == 60) {
			res.put("field", "InvoiceAmount");
			res.put("current", current.getAmount());
			res.put("previous", 0);
			res.put("operation",revType);
		}
		if(revInfo.getAuditorId()!=null) {
		Map<String, Object> auditor = nameList.stream()
				.filter(it -> it.get("userId").toString().equals(revInfo.getAuditorId().toString())).findFirst().orElse(null);
		if (auditor != null && auditor.containsKey("name"))
		res.put("updatedBy", auditor.get("name"));
		}else {
			res.put("updatedBy", null);
		}
		res.put("updatedOn",  revInfo.getTimestamp());
		return res;
	}
}


