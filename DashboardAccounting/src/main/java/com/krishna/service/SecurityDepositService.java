package com.krishna.service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Objects;
import com.google.common.util.concurrent.AtomicDouble;
import com.krishna.controller.FeignLegacyInterface;
import com.krishna.domain.BankLocation;
import com.krishna.domain.SecurityDeposit;
import com.krishna.domain.UserModel;
import com.krishna.domain.invoice.InvoiceBank;
import com.krishna.domain.invoice.InvoiceHSN;
import com.krishna.domain.invoice.InvoiceProjectSettings;
import com.krishna.domain.invoice.InvoiceType;
import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.domain.invoice.ProjectInvoiceItem;
import com.krishna.dto.ProjectInvoiceDto;
import com.krishna.dto.SecurityDepositDto;
import com.krishna.dto.invoice.InvoiceSourceUpdateDto;
import com.krishna.dto.invoice.ProjectInvoiceGenerateDto;
import com.krishna.dto.invoice.ProjectInvoiceItemGetDto;
import com.krishna.enums.InvoiceGenerationStatus;
import com.krishna.enums.PayStatus;
import com.krishna.enums.Taxable;
import com.krishna.repository.BankLocationRepo;
import com.krishna.repository.SecurityDepositRepository;
import com.krishna.repository.invoice.HsnRepository;
import com.krishna.repository.invoice.InvoiceBankRepository;
import com.krishna.repository.invoice.InvoiceTypeRepository;
import com.krishna.repository.invoice.ProjectInvoiceItemRepository;
import com.krishna.repository.invoice.ProjectInvoiceRepository;
import com.krishna.repository.invoice.ProjectSettingsRepository;
import com.krishna.security.JwtValidator;
import com.krishna.service.invoice.InvoiceService;
import com.krishna.util.DoubleEncryptDecryptConverter;

@Service
public class SecurityDepositService {
	
	@Autowired
	JwtValidator validator;

	@Autowired 
	BankLocationRepo locationRepo;
	
	@Autowired
	FeignLegacyInterface feignLegacyInterface;
	
	@Autowired
	SecurityDepositRepository securityDepositRepository;
	
	@Autowired
	ProjectInvoiceItemRepository itemRepository;
	
	@Autowired
	ProjectSettingsRepository projectSettingsRepo;
	
	@Autowired
	HsnRepository hsnRepo;
	
	@Autowired
	InvoiceService invoiceService;
	
	@Autowired
	InvoiceTypeRepository invoiceTypeRepository;
	
	@Autowired
	InvoiceBankRepository bankRepository;
	
	@Autowired
	DollarCostServiceImpl dollarCostService;
	
	@Autowired
	ProjectInvoiceRepository projectInvoiceRepository;
	
	@Autowired
	EntityManager entityManager;
	
	Logger logger = LoggerFactory.getLogger(SecurityDepositService.class);
	
	public  boolean addSecurityDeposit(SecurityDepositDto securityDepositDto,String accessToken){
		SecurityDeposit security=new SecurityDeposit();
		String operation="add";
		return saveData(accessToken,securityDepositDto,operation);

	
	}
	
	
	
	public Map<String, Object> getSecurityDepositData(String accessToken, String businessVertical, List<Map<String, Object>> allProjectData, List<Map<String, Object>> userInfo, Integer year) {
		
		List<Map<String, Object>> result = new ArrayList<>();
		List<SecurityDeposit> security = new ArrayList<>();
		if(year!=null) {
			 security = securityDepositRepository.findAllByYearAndIsDeleted(year, false);
		}else {
			 security = securityDepositRepository.findAllByIsDeleted(false);		
		}
		List<SecurityDeposit> emptyLeadIdList = security.stream().filter(inv->inv.getLeadId()==0).collect(Collectors.toList());
		if(!emptyLeadIdList.isEmpty()) {
			setLeadId(emptyLeadIdList, allProjectData);
		}
		AtomicDouble paidAmount=new AtomicDouble(0);
		AtomicDouble unpaidAmount=new AtomicDouble(0);
		AtomicDouble totalAmount=new AtomicDouble(0);
		AtomicDouble adjustedAmount=new AtomicDouble(0);
		AtomicDouble adjustedAmountInDoller=new AtomicDouble(0);
		AtomicDouble availableAmount=new AtomicDouble(0);
		Query query = entityManager.createNativeQuery("select * from project_invoice where security_deposite_id is not null", ProjectInvoice.class);
		List<ProjectInvoice> invoiceData = query.getResultList();
		security.stream().forEach(securityDeposit->{
			Map<String, Object> projectData = allProjectData.stream().filter(project -> (project.get("leadId")!=null ? project.get("leadId").toString().equals(String.valueOf(securityDeposit.getLeadId())) : false )||(project.get("id").toString().equals(String.valueOf(securityDeposit.getProjectId())))).findAny().orElse(null);
			Map<String, Object> createdBy = userInfo.stream().filter(userdata -> userdata.get("userId").toString().equals(String.valueOf(securityDeposit.getCreatedBy()))).findAny().orElse(null);
			Map<String, Object> manager = userInfo.stream().filter(userdata -> userdata.get("userId").toString().equals(String.valueOf(securityDeposit.getManagerId()))).findAny().orElse(null);
			Map<String, Object> response = this.getData(securityDeposit, businessVertical, projectData, manager, createdBy, invoiceData);
			if (!response.isEmpty()) {
				result.add(response);
				Double ifsdAmount=Double.parseDouble(response.get("amountInDollar").toString());
				Double adjustedValue=Double.parseDouble(response.get("adjustmentSum").toString());
				adjustedAmount.getAndAdd(adjustedValue);
				totalAmount.getAndAdd(ifsdAmount);
				adjustedAmountInDoller.getAndAdd(Double.parseDouble(response.get("adjustmentAmountInDoller").toString()));
				availableAmount.getAndAdd(ifsdAmount-adjustedValue);
				if(response.get("ifsdStatus").toString().equals("UNPAID"))
					unpaidAmount.getAndAdd(ifsdAmount);
				else
					paidAmount.getAndAdd(ifsdAmount);
			}
		});
		Collections.sort(result,(p1, p2)->new Long(p1.get("id").toString()).compareTo(new Long(p2.get("id").toString())));
		Collections.reverse(result);
		Map<String, Object> resultMap=new HashMap<>();
		resultMap.put("ifsdData", result);
		resultMap.put("totalAmount",Double.parseDouble(new DecimalFormat("#.##").format(totalAmount)));
		resultMap.put("unpaidAmount", unpaidAmount);
		resultMap.put("paidAmount", paidAmount);
		resultMap.put("availableAmount", availableAmount);
		resultMap.put("adjustedAmount", adjustedAmount);
		resultMap.put("adjustedAmountInDoller", adjustedAmountInDoller);
		return resultMap;
		}

		private void setLeadId(List<SecurityDeposit> emptyLeadIdList, List<Map<String, Object>> allProjectData) {
			emptyLeadIdList.stream().forEach(securityDeposit -> {
				Map<String, Object> projectData = allProjectData.stream().filter(
						project -> project.get("id").toString().equals(String.valueOf(securityDeposit.getProjectId())))
						.findFirst().orElse(null);
				if (projectData != null) {
					securityDeposit.setLeadId(Long.parseLong(projectData.get("leadId").toString()));
					securityDepositRepository.save(securityDeposit);
				}
			});
		}

	public Map<String, Object> getData(SecurityDeposit securityDeposit, String bu, Map<String, Object> projectData, Map<String, Object> manager, Map<String, Object> createdBy, List<ProjectInvoice> invoiceData) {
		Map<String, Object> res = new HashMap<>();
		String projectBusinessVertical = projectData != null ? (String) projectData.get("businessVertical") : "";
		if (projectBusinessVertical.equals(bu) || bu == null || bu.equals("")) {
			Double adjustment = 0D;
			List<ProjectInvoice> securityDepositInvoices = invoiceData.stream().filter(invoice->invoice.getSecurityDepositeId().equals(securityDeposit.getId())).collect(Collectors.toList());
			adjustment = securityDepositInvoices.stream().collect(Collectors.summingDouble(ProjectInvoice::getAdjustmantAmount));
			Double adjustmentAmount = adjustment<0?-adjustment:adjustment;
			res.put("adjustmentSum", adjustmentAmount);
			res.put("id", securityDeposit.getId());
			res.put("leadId", projectData != null ? projectData.get("leadId")!=null ? projectData.get("leadId") : "NA" : securityDeposit.getLeadId());
			res.put("invoiceGenerationStatus", securityDeposit.getInvoiceGenerationStatus());
			res.put("project", projectData != null ? (String) projectData.get("projectName") : "NA");
			res.put("projectId", projectData != null ?  securityDeposit.getProjectId() : "NA");
			res.put("projectStatus", projectData != null ?  (String) projectData.get("status") : "NA");
			res.put("currentManager",projectData != null ? (String) projectData.get("manager") : "NA");
			res.put("currentManagerId",projectData != null ?  projectData.get("managerId") : "NA");
			res.put("managerId", securityDeposit.getManagerId());
			res.put("ifsdStatus", securityDeposit.getPayStatus());
			res.put("manager", manager!=null ?  manager.get("name") : "NA");
			res.put("clientName", securityDeposit.getClientName());
			res.put("amount", securityDeposit.getAmount());
			res.put("availableAmount", securityDeposit.getAmount()-(adjustment<0?-adjustment:adjustment));
			res.put("createdOn", securityDeposit.getCreatedDate().getTime());
			res.put("createdBy", createdBy.get("name"));
			res.put("businessVertical", projectBusinessVertical);
			res.put("isMilestone", false);
			res.put("invoiceType", null);
			res.put("isIfsd", true);
			res.put("paymentCharges", 0);
			res.put("comment",securityDeposit.getComment()!=null ? securityDeposit.getComment() : "NA");
			if (securityDeposit.getPaymentCharges() != null)
				res.put("paymentCharges", securityDeposit.getPaymentCharges());
			if (securityDeposit.getBank() != null) {
				res.put("bankName", securityDeposit.getBank().getName());
				res.put("bankId", securityDeposit.getBank().getId());
			} else
				res.put("bankName", "N/A");
			res.put("bankLocation", securityDeposit.getBankLocation()!=null ?  securityDeposit.getBankLocation() : null);
			if (securityDeposit.getInvoiceType() != null)
				res.put("invoiceType", securityDeposit.getInvoiceType().getName());
			if (securityDeposit.getReceivedOn() != null) {
				res.put("receivedOn", securityDeposit.getReceivedOn().getTime());
			}
			if ((Double) securityDeposit.getAmountInDollar() != null) {
				res.put("amountInDollar", Math.round(securityDeposit.getAmountInDollar() * 100.00) / 100.00);

			}
			res.put("taxableInvoiceAmount", null);
			if ((Double) securityDeposit.getTaxableAmount() != null) {
				res.put("taxableInvoiceAmount", securityDeposit.getTaxableAmount());
			}
			Double exchangeRate = 0.0;
			if ((Double) securityDeposit.getExchangeRate() != null) {
				if(securityDeposit.getExchangeRate()!=0.0) {
				    res.put("exchangeRate", securityDeposit.getExchangeRate());
				    exchangeRate = securityDeposit.getExchangeRate();
				}else {
					res.put("exchangeRate", Math.round(1 * 100.00) / 100.00);
					exchangeRate = Math.round(1 * 100.00) / 100.00;
				}
			}
			res.put("adjustmentAmountInDoller",adjustmentAmount*exchangeRate);

			if (securityDeposit.getCurrency() != null && !securityDeposit.getCurrency().isEmpty()) {
				res.put("currency", securityDeposit.getCurrency());

			}
		}
		return res;
	}
	
	public boolean editSecurityDeposit(SecurityDepositDto securityDepositDto,String accessToken){
		String operation="edit";
		return saveData(accessToken,securityDepositDto,operation);


	}

	public Object deleteSecurityDeposit(long id,String accessToken){
		SecurityDeposit security=securityDepositRepository.findById(id);
		if(security!=null) {
			List<ProjectInvoice> projectInvoices = projectInvoiceRepository.findAllBySecurityDepositeIdAndIsDeleted(id, false);
			if(projectInvoices != null && !projectInvoices.isEmpty())
				return null;

			security.setIsDeleted(true);
			securityDepositRepository.save(security);
		    return true;
		}
		else
		    return false;
		
	}
	
	public boolean saveData(String accessToken,SecurityDepositDto securityDepositDto,String operation) {
		SecurityDeposit security=null;
		if(operation.equals("add")) {
			 security=new SecurityDeposit();
		}
		else if(operation.equals("edit")){
		    security=securityDepositRepository.findById(securityDepositDto.getId());
		}
		UserModel currentUser = validator.tokenbValidate(accessToken);
		try {
			security.setLeadId(securityDepositDto.getLeadId());
			security.setPaymentCharges(securityDepositDto.getPaymentCharges());
			security.setProjectId(securityDepositDto.getProjectId());
			security.setManagerId(securityDepositDto.getManagerId());
			security.setClientName(securityDepositDto.getClientName());
			security.setPayStatus(PayStatus.getSecurityDepositeEnumStatus(securityDepositDto.getIfsdStatus()));
			security.setAmount(securityDepositDto.getAmount());
			security.setReceivedOn(securityDepositDto.getReceivedOn());
			security.setCurrency(securityDepositDto.getCurrency());
			if(securityDepositDto.getComment() != null) {
			security.setComment(securityDepositDto.getComment());
			}
			if(operation.equals("add")) {
				security.setCreatedDate(new Date());
				security.setCreatedBy(currentUser.getUserId());
			}
			else if(operation.equals("edit")){
				security.setUpdatedOn(new Date());
				security.setUpdatedBy(currentUser.getUserId());
			}
			
			double taxabaleAmount = (9 * securityDepositDto.getAmount()) / 100;
			double taxableTotalCost= securityDepositDto.getAmount();
			if(security.getInvoiceType() != null &&  security.getInvoiceType().getName().equals("Domestic")) {
				taxableTotalCost = taxableTotalCost+(taxabaleAmount*2);
			}
			if (securityDepositDto.getCurrency().equals("DOLLAR")) {
				security.setAmountInDollar(securityDepositDto.getAmount());
				security.setExchangeRate(1);
				security.setTaxableAmount(taxableTotalCost);
			} else {
				security.setAmountInDollar(securityDepositDto.getAmount()*securityDepositDto.getExchangeRate());
				security.setExchangeRate(securityDepositDto.getExchangeRate());
				security.setTaxableAmount(taxableTotalCost*securityDepositDto.getExchangeRate());
			}
			securityDepositRepository.save(security);
		}
		catch(Exception e){
			logger.info(" " + e.getMessage());
			return false;
		}
		return true;
	}
	
	public List<Object> getProjectWiseSecurityData(String authorization,Long projectId, List<Map<String, Object>> userInfo) {
		ArrayList<Object> result=new ArrayList<Object>();
		List<SecurityDeposit> security = securityDepositRepository.findAllByProjectIdAndIsDeleted(projectId,false);
		Map<String, Object> projectData = (Map<String, Object>) feignLegacyInterface.getProjectDescription(projectId,"",null,null).get("data");
		Query query = entityManager.createNativeQuery("select * from project_invoice where security_deposite_id is not null", ProjectInvoice.class);
		List<ProjectInvoice> invoiceData = query.getResultList();
		security.stream().forEach(securityDeposit->{
			Map<String, Object> createdBy = userInfo.stream().filter(userdata -> userdata.get("userId").toString().equals(String.valueOf(securityDeposit.getCreatedBy()))).findAny().orElse(null);
			Map<String, Object> manager = userInfo.stream().filter(userdata -> userdata.get("userId").toString().equals(String.valueOf(securityDeposit.getManagerId()))).findAny().orElse(null);
			Map<String, Object> response = this.getData(securityDeposit, null, projectData, manager, createdBy, invoiceData);
			if (!response.isEmpty())
				result.add(response);
			});
		return result;
	}
	
	public Map<String, Object> viewSecurityDeposit(String accessToken, Long projectInvoiceId, Map<String, Object> invoiceMap,boolean isInternalCall) {
		Optional<SecurityDeposit> securityDeposit=securityDepositRepository.findById(projectInvoiceId);
		if(securityDeposit.isPresent()) {
			SecurityDeposit invoice=securityDeposit.get();
			Double adjustment=0D;
		Query query = entityManager.createNativeQuery(
				"select * from project_invoice where security_deposite_id=:id",ProjectInvoice.class);
		query.setParameter("id", invoice.getId());
		List<ProjectInvoice> data=query.getResultList();
		adjustment=data.stream().collect(Collectors.summingDouble(ProjectInvoice::getAdjustmantAmount));
		List<ProjectInvoiceItemGetDto> itemList = new ArrayList<>();
		List<ProjectInvoiceItem> savedItemList = itemRepository.findAllByProjectInvoiceIdAndIsDeleted(invoice.getId(), false);
		Double total = 0D;
		Double totalDollar = 0D;
		for (ProjectInvoiceItem item : savedItemList) {
			// TODO
			ProjectInvoiceItemGetDto invoiceDto = getSavedItems(item);
			itemList.add(invoiceDto);
			total = total + invoiceDto.getTotalCost();
			totalDollar = totalDollar + invoiceDto.getTotalCostDollar();
		}
		
		
		invoiceMap = getSecurityDepositDataMap(accessToken, invoice);
		if(total >= Math.abs(adjustment)) {
			invoiceMap.put("remainingAmount ", Math.round((total - Math.abs(adjustment)) * 100.00) / 100.00);
		}
		invoiceMap.put("adjustmentSum",adjustment);
		invoiceMap.put("bankLocation", securityDeposit.get().getBankLocation());
		invoiceMap.put("dollarCost", Math.round(invoice.getAmountInDollar() * 100.00) / 100.0);
		invoiceMap.put("total", Math.round(total * 100.00) / 100.00);
		double taxabaleAmount = (9 * total) / 100;
		invoiceMap.put("cgst", Math.round(taxabaleAmount * 100.00) / 100.00);
		invoiceMap.put("sgst", Math.round(taxabaleAmount * 100.00) / 100.00);
		invoiceMap.put("igst", Math.round((taxabaleAmount + taxabaleAmount) * 100.00) / 100.00);
		double payableAfterTax = 0;
		if (invoice.getInvoiceType().getName().equals("International"))
			payableAfterTax = total;
		else
			payableAfterTax = total + (taxabaleAmount + taxabaleAmount);
		invoiceMap.put("payableAmount", Math.round(payableAfterTax * 100.00) / 100.00);
		double dueDateAmount = (2 * payableAfterTax) / 100;
		invoiceMap.put("afterDueDateAmount", Math.round((payableAfterTax + dueDateAmount) * 100.00) / 100.00);
		invoiceMap.put("itemList", itemList);
		return invoiceMap;
		}
		return null;
	}

	private Map<String,Object> getSecurityDepositDataMap(String accessToken, SecurityDeposit invoice){
		Map<String,Object> invoiceMap=new HashMap<>();
		invoiceMap.put("id", invoice.getId());
		invoiceMap.put("invoiceId",invoice.getId());
		invoiceMap.put("invoiceType", invoice.getInvoiceType().getName());
		invoiceMap.put("invoiceTypeId", invoice.getInvoiceType().getId());
		invoiceMap.put("isIfsd", true);
		invoiceMap.put("clientName", invoice.getClientName());
		invoiceMap.put("projectId", invoice.getProjectId());
		invoiceMap.put("leadId", invoice.getLeadId());
		invoiceMap.put("payDetails", invoice.getPayDetails());
//		invoiceMap.put("createdOn", new SimpleDateFormat("dd-MM-yyyy").format(new Date(invoice.getCreatedDate().getTime())));
		invoiceMap.put("createdOn", invoice.getCreatedDate().getTime());
		if(invoice.getReceivedOn()!=null)
			invoiceMap.put("receivedOn", invoice.getReceivedOn());
		InvoiceSourceUpdateDto sourceDto=invoiceService.getInvoiceSource();
		invoiceMap.put("source", sourceDto);

		invoiceMap.put("currency", invoice.getCurrency());
		invoiceMap.put("exchangeRate", invoice.getExchangeRate());
		invoiceMap.put("tdsValue", invoice.getTdsValue());

		invoiceMap.put("taxable", invoice.getTaxable());
		invoiceMap.put("bank", invoice.getBank());
		
		
		if(invoice.getProjectId()!=0) {
			Map<String, Object> projectDetails = (Map<String, Object>) feignLegacyInterface.getProjectDescription(invoice.getProjectId(),"",null,null).get("data");
			invoiceMap.put("clientEmail", projectDetails.get("clientEmail"));
			invoiceMap.put("toCompanyName", projectDetails.get("clientCompany"));
			
			invoiceMap.put("primaryContactName", projectDetails.get("primaryContactName"));
			invoiceMap.put("secondaryEmail", projectDetails.get("secondaryEmail"));
			invoiceMap.put("clientRepresentativeEmail", "NA");
			invoiceMap.put("billingName", projectDetails.get("billingName").equals("NA") ? invoice.getClientName() : projectDetails.get("billingName"));
			invoiceMap.put("billingEmail",projectDetails.get("billingEmail").equals("NA") ? projectDetails.get("clientEmail") : projectDetails.get("billingEmail"));
			
			InvoiceProjectSettings projectSettings = projectSettingsRepo.findByProjectId(invoice.getProjectId());
			if (projectSettings != null) {
				if(projectSettings.getEmailId()!=null)
					invoiceMap.put("clientEmail", projectSettings.getEmailId());
				invoiceMap.put("toCompanyName", projectSettings.getCompanyName());
				invoiceMap.put("toAddress", projectSettings.getClientAddress());
				invoiceMap.put("toGST", projectSettings.getGstNumber());
			}
			else {
				invoiceMap.put("toAddress", "NA");
				invoiceMap.put("toGST", "NA");
			}
		}
		else {
			Map<String, Object> clientDetails = (Map<String, Object>) feignLegacyInterface.getClient(accessToken,invoice.getLeadId()).get("data");
			Map<String, Object> client = (Map<String, Object>) clientDetails.get("client");
			invoiceMap.put("clientEmail", client.get("emailId"));
			invoiceMap.put("toCompanyName", client.get("clientType").toString().equals("Individual") ? client.get("clientName") : client.get("organizationName"));
			invoiceMap.put("clientName", client.get("clientName"));
			invoiceMap.put("primaryContactName", client.get("primaryContact"));
			invoiceMap.put("secondaryEmail", "N/A");
			invoiceMap.put("clientRepresentativeEmail", "N/A");
			invoiceMap.put("billingName", client.get("billingName"));
			invoiceMap.put("billingEmail",client.get("billingEmail"));

			InvoiceProjectSettings projectSettings = projectSettingsRepo.findByLeadId(invoice.getLeadId());
			if (projectSettings != null) {
				invoiceMap.put("toAddress", projectSettings.getClientAddress());
				invoiceMap.put("toGST", projectSettings.getGstNumber());
			}
			else {
				invoiceMap.put("toAddress", "NA");
				invoiceMap.put("toGST", "NA");
			}
		}

		invoiceMap.put("placeOfSupply", invoice.getPlaceOfSupply());
		invoiceMap.put("cityOfSupply", invoice.getCityOfSupply()!=null?invoice.getCityOfSupply():"NA");

		invoiceMap.put("payStatus", invoice.getPayStatus());
		if(invoice.getIsOthersInUrl()!=null)
			invoiceMap.put("isOthersInUrl", invoice.getIsOthersInUrl());
		else
			invoiceMap.put("isOthersInUrl", false);
		invoiceMap.put("isMilestone", false);
		if(invoice.getDomesticType()!=null)
			invoiceMap.put("domesticType", invoice.getDomesticType());
		else
			invoiceMap.put("domesticType", "");
		InvoiceHSN hsn=hsnRepo.findByIsArchived(false);
		if(hsn!=null)
			invoiceMap.put("sac", hsn.getName());
		else
			invoiceMap.put("sac", "");
		return invoiceMap;
	}
	
	public ProjectInvoiceItemGetDto getSavedItems(ProjectInvoiceItem item){
		ProjectInvoiceItemGetDto invoiceDto=new ProjectInvoiceItemGetDto();
		BeanUtils.copyProperties(item, invoiceDto);
		invoiceDto.setTotalCostDollar(item.getUnitCost()*item.getTimesheetHours());
		invoiceDto.setTotalCost(item.getUnitCost()*item.getTimesheetHours());
		return invoiceDto;
	}
	
	public SecurityDeposit addSecurityDepositForCreation(ProjectInvoiceDto projectInvoiceDto, String authorization) {
		SecurityDeposit projectInvoice = null;
		if(projectInvoiceDto.getId()==null || projectInvoiceDto.getId()==0)
			projectInvoice = new SecurityDeposit();
		else {
			Optional<SecurityDeposit> securityDeposit = securityDepositRepository.findById(projectInvoiceDto.getId());
			if(securityDeposit.isPresent())
				projectInvoice= securityDeposit.get();
		}
		InvoiceType invoiceType=invoiceTypeRepository.findByNameAndIsArchived(projectInvoiceDto.getInvoiceType(),false);
		projectInvoice.setInvoiceType(invoiceType);
		projectInvoice.setInvoiceGenerationStatus(InvoiceGenerationStatus.PENDING);
		projectInvoice.setPaymentCharges(projectInvoiceDto.getPaymentCharges());
		BankLocation location = locationRepo.findByIdAndIsDeleted(projectInvoiceDto.getBankLocationId(), false);
		if(location!=null)
		projectInvoice.setBankLocation(location);
//		projectInvoice.setIsDeleted(true);
		boolean result = saveProjectInvoiceData(projectInvoice, projectInvoiceDto, authorization);
		if(result && projectInvoice!=null)
			return projectInvoice;
		return null;
	}
	
	private boolean saveProjectInvoiceData(SecurityDeposit projectInvoice, ProjectInvoiceDto projectInvoiceDto,
			String accessToken) {
		UserModel currentUser = validator.tokenbValidate(accessToken);
		projectInvoice.setLeadId(projectInvoiceDto.getLeadId());
		projectInvoice.setCurrency(projectInvoiceDto.getCurrency());
		projectInvoice.setTdsValue(projectInvoiceDto.getTdsValue());
		projectInvoice.setPayStatus(PayStatus.getSecurityDepositeEnumStatus(projectInvoiceDto.getIfsdStatus()));
		projectInvoice.setReceivedOn(projectInvoiceDto.getReceivedOn());
		projectInvoice.setCreatedDate(new Date());
		projectInvoice.setProject(projectInvoiceDto.getProject());
		projectInvoice.setProjectId(projectInvoiceDto.getProjectId()!=null ? projectInvoiceDto.getProjectId() : 0);
		projectInvoice.setManager(projectInvoiceDto.getManager());
		projectInvoice.setManagerId(projectInvoiceDto.getManagerId());
		projectInvoice.setClientName(projectInvoiceDto.getClientName());
		projectInvoice.setAmount(0D);
		projectInvoice.setAmountInDollar(0D);
		projectInvoice.setTaxableAmount(0D);
		projectInvoice.setCreatedBy(currentUser.getUserId());
		projectInvoice.setExchangeRate(projectInvoiceDto.getExchangeRate());
		projectInvoice.setComment(projectInvoiceDto.getComment());
		if (projectInvoiceDto.getBankId() != null) {
			Optional<InvoiceBank> bank = bankRepository.findById(projectInvoiceDto.getBankId());
			if (bank.isPresent())
				projectInvoice.setBank(bank.get());
		}
		try {
			securityDepositRepository.save(projectInvoice);
		} catch (Exception e) {
			logger.info(e.getMessage());
			return false;
		}
		return true;
	}
	
	public void recalculateTotalCost(SecurityDeposit invoice) {
		List<ProjectInvoiceItem> savedItemList = itemRepository.findAllByProjectInvoiceIdAndIsDeletedAndIsIfsd(invoice.getId(), false,true);
		Double totalCost = 0D;
		for (ProjectInvoiceItem item : savedItemList) {
			totalCost= totalCost + (item.getUnitCost()* item.getTimesheetHours());
		}
		double taxabaleAmount = (9 * totalCost) / 100;
		double taxableTotalCost= totalCost;
		if(invoice.getInvoiceType().getName().equals("Domestic")) {
			taxableTotalCost = taxableTotalCost+(taxabaleAmount*2);
		}
		invoice.setAmount(totalCost);
		if (invoice.getCurrency().equals("DOLLAR")) {
			invoice.setAmountInDollar(totalCost);
			invoice.setTaxableAmount(taxableTotalCost);
		} else if (invoice.getCurrency().equals("RUPEE")) {
			invoice.setAmountInDollar(totalCost*invoice.getExchangeRate());
			invoice.setTaxableAmount(taxableTotalCost*invoice.getExchangeRate());
		} else {
			invoice.setAmountInDollar(totalCost*invoice.getExchangeRate());
			invoice.setTaxableAmount(taxableTotalCost*invoice.getExchangeRate());
		}
		securityDepositRepository.saveAndFlush(invoice);
	}
	
	public Map<String,Object> saveGeneratedInvoice(String accessToken,ProjectInvoiceGenerateDto invoiceDto, Map<String,Object> responseMap) {
		Optional<SecurityDeposit> invoice = securityDepositRepository.findById(invoiceDto.getId());
		if(invoice.isPresent()) {
			BeanUtils.copyProperties(invoiceDto, invoice.get());
			recalculateTotalCost(invoice.get());
			invoice.get().setIsDeleted(false);
			invoice.get().setCompleted(true);
			if(invoiceDto.getGenerationStatus().equals("INDRAFT")) 
				invoice.get().setInvoiceGenerationStatus(InvoiceGenerationStatus.INDRAFT);
			else
				invoice.get().setInvoiceGenerationStatus(InvoiceGenerationStatus.GENERATED);
			InvoiceBank bank= bankRepository.findByNameAndIsArchived(invoiceDto.getBankName(), false);
			if(bank!=null)
				invoice.get().setBank(bank);
			if(invoiceDto.getTaxable().contentEquals("YES")) 
				invoice.get().setTaxable(Taxable.YES);
			else
				invoice.get().setTaxable(Taxable.NO);
			securityDepositRepository.saveAndFlush(invoice.get());
			responseMap=prepareResponseForPdf(accessToken, invoice.get());
		}
		return responseMap;
	}
	
	private Map<String,Object> prepareResponseForPdf(String accessToken, SecurityDeposit invoice){
		Map<String,Object> invoiceMap=new HashMap<>();
		invoiceMap.put("id", invoice.getId());
		invoiceMap.put("invoiceId", "INV-"+invoice.getId());
		invoiceMap.put("isOthersInUrl", invoice.getIsOthersInUrl());
		invoiceMap.put("payDetails", invoice.getPayDetails());
		InvoiceSourceUpdateDto sourceDto=invoiceService.getInvoiceSource();
		invoiceMap.put("source", sourceDto);
		invoiceMap.put("invoiceType", invoice.getInvoiceType().getName());
		invoiceMap.put("invoiceTypeId", invoice.getInvoiceType().getId());
		invoiceMap.put("clientName", invoice.getClientName());
		if(invoice.getProjectId()!=0) {
			Map<String, Object> projectDetails = (Map<String, Object>) feignLegacyInterface.getProjectDescription(invoice.getProjectId(),"",null,null).get("data");
			invoiceMap.put("clientEmail", projectDetails.get("clientEmail"));
			invoiceMap.put("toCompanyName", projectDetails.get("clientCompany"));
			InvoiceProjectSettings projectSettings = projectSettingsRepo.findByProjectId(invoice.getProjectId());
			if (projectSettings != null) {
				invoiceMap.put("toAddress", projectSettings.getClientAddress());
				invoiceMap.put("toGST", projectSettings.getGstNumber());
			}
			else {
				invoiceMap.put("toAddress", "NA");
				invoiceMap.put("toGST", "NA");
			}
			
		}
		else {
			Map<String, Object> clientDetails = (Map<String, Object>) feignLegacyInterface.getClient(accessToken,invoice.getLeadId()).get("data");
			Map<String, Object> client = (Map<String, Object>) clientDetails.get("client");
			invoiceMap.put("clientEmail", client.get("emailId"));
			invoiceMap.put("toCompanyName", client.get("clientType").toString().equals("Individual") ? client.get("clientName") : client.get("organizationName"));
			invoiceMap.put("clientName", client.get("clientName"));
			invoiceMap.put("primaryContactName", client.get("primaryContact"));
			invoiceMap.put("secondaryEmail", "N/A");
			invoiceMap.put("clientRepresentativeEmail", "N/A");
			invoiceMap.put("billingName", client.get("billingName"));
			invoiceMap.put("billingEmail",client.get("billingEmail"));
			InvoiceProjectSettings projectSettings = projectSettingsRepo.findByLeadId(invoice.getLeadId());
			if (projectSettings != null) {
				invoiceMap.put("toAddress", projectSettings.getClientAddress());
				invoiceMap.put("toGST", projectSettings.getGstNumber());
			}
			else {
				invoiceMap.put("toAddress", "NA");
				invoiceMap.put("toGST", "NA");
			}
		}
		invoiceMap.put("bank", invoice.getBank());
		invoiceMap.put("currency", invoice.getCurrency());
		invoiceMap.put("taxable", invoice.getTaxable());
		invoiceMap.put("invoicegenerationStatus", invoice.getInvoiceGenerationStatus());
		invoiceMap.put("placeOfSupply", invoice.getPlaceOfSupply());
		invoiceMap.put("cityOfSupply", invoice.getCityOfSupply());
		invoiceMap.put("payStatus", invoice.getPayStatus());
		invoiceMap=viewSecurityDeposit(accessToken, invoice.getId(), invoiceMap, false);
		return invoiceMap;
	}
	
	public List<String> getSecurityDepositeEnumsStatus(String accessToken) {
		List<String> securityDepositeEnum = new ArrayList<String>();
		PayStatus[] list = PayStatus.getSecurityDepositePayStatus();
		for (PayStatus status : list) {
			securityDepositeEnum.add(status.getInvoiceGenerationStatus());
		}
		return securityDepositeEnum;
	}



	public List<Map<String,Object>> getSecurityDepositDeductions(String authorization,Long id) {
		Query query = entityManager.createNativeQuery("select id,adjustmant_amount,adjustment_date from project_invoice where security_deposite_id=:id");
		query.setParameter("id", id);
		List<Object[]> invoiceData = query.getResultList();
		DoubleEncryptDecryptConverter converter = new DoubleEncryptDecryptConverter();
		List<Map<String,Object>> responseList=new ArrayList<>();
		invoiceData.forEach(data->{
			Map<String,Object> map=new HashMap<>();
			map.put("id", data[0]);
			Double adjustment  = converter.convertToEntityAttribute(data[1].toString());
			map.put("amount",adjustment<0?-adjustment:adjustment);
			map.put("adjustmentDate", data[2]);
			responseList.add(map);
		});
		return responseList;
	}
}
