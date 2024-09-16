package com.krishna.service.invoice;

import java.nio.file.Files;


import java.nio.file.Paths;
import java.text.DateFormatSymbols;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.time.DateUtils;
//import org.jolokia.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.AtomicDouble;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.layout.font.FontProvider;
import com.krishna.Interfaces.DollarCostService;
import com.krishna.Interfaces.IInvoiceService;
import com.krishna.controller.FeignLegacyInterface;
import com.krishna.domain.BankLocation;
import com.krishna.domain.ExpectedBillingRate;
import com.krishna.domain.SecurityDeposit;
import com.krishna.domain.UserModel;
import com.krishna.domain.invoice.InvoiceBank;
import com.krishna.domain.invoice.InvoiceCycle;
import com.krishna.domain.invoice.InvoiceHSN;
import com.krishna.domain.invoice.InvoiceProjectSettings;
import com.krishna.domain.invoice.InvoiceSlip;
import com.krishna.domain.invoice.InvoiceSource;
import com.krishna.domain.invoice.InvoiceStatus;
import com.krishna.domain.invoice.InvoiceType;
import com.krishna.domain.invoice.PaymentMode;
import com.krishna.domain.invoice.PaymentTerms;
import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.domain.invoice.ProjectInvoiceItem;
import com.krishna.dto.ProjectInvoiceDto;
import com.krishna.dto.invoice.InvoiceSlipDto;
import com.krishna.dto.invoice.InvoiceSourceUpdateDto;
import com.krishna.dto.invoice.ProjectInvoiceGenerateDto;
import com.krishna.dto.invoice.ProjectInvoiceItemDto;
import com.krishna.dto.invoice.ProjectInvoiceItemGetDto;
import com.krishna.enums.InvoiceGenerationStatus;
import com.krishna.enums.PayStatus;
import com.krishna.enums.Taxable;
import com.krishna.repository.BankLocationRepo;
import com.krishna.repository.ExpectedBillingRepository;
import com.krishna.repository.SecurityDepositRepository;
import com.krishna.repository.invoice.HsnRepository;
import com.krishna.repository.invoice.InvoiceBankRepository;
import com.krishna.repository.invoice.InvoiceCycleRepository;
import com.krishna.repository.invoice.InvoiceSlipRepository;
import com.krishna.repository.invoice.InvoiceSourceRepository;
import com.krishna.repository.invoice.InvoiceStatusRepository;
import com.krishna.repository.invoice.InvoiceTypeRepository;
import com.krishna.repository.invoice.PaymentModeRepository;
import com.krishna.repository.invoice.PaymentTermsRepository;
import com.krishna.repository.invoice.ProjectInvoiceItemRepository;
import com.krishna.repository.invoice.ProjectInvoiceRepository;
import com.krishna.repository.invoice.ProjectSettingsRepository;
import com.krishna.security.JwtAuthorizationFilter;
import com.krishna.security.JwtValidator;
import com.krishna.service.FileUploadService;
import com.krishna.service.LoginUtiltiyService;
import com.krishna.service.ProjectInvoiceService;
import com.krishna.service.SecurityDepositService;
import com.krishna.service.util.UtilityService;

@Service
public class InvoiceService implements IInvoiceService{
	
	@Autowired
	InvoiceBankRepository bankRepository;
	
	@Autowired
	InvoiceTypeRepository invoiceTypeRepository;
	
	@Autowired
	ProjectSettingsRepository projectSettingsRepo;
	
	@Autowired
	ProjectInvoiceItemRepository itemRepository;
	
	@Autowired
	ExpectedBillingRepository billingRepository;
	
	
	@Autowired
	ProjectInvoiceRepository projectInvoiceRepository;
	
	@Autowired
	SecurityDepositRepository securityDepositRepo;
	
	@Autowired
	LoginUtiltiyService loginUtilityService;
	
	@Autowired
	DollarCostService dollarCostService;
	
	@Autowired
	InvoiceSourceRepository sourceRepository;
	
	@Autowired
	InvoiceStatusRepository invoiceStatusRepository;
	
	@Autowired
	ProjectInvoiceService oldInvoiceService;
	
	@Autowired
	JwtValidator validator;
	
	@Autowired
	JwtAuthorizationFilter validatorForAudit;
	
	@Autowired
	FileUploadService fileuploadService;
	
	@Autowired
	InvoiceSlipRepository invoiceSlipRepo;
	
	@Autowired
	PaymentModeRepository paymentModeRepository;
	
	@Autowired
	InvoiceCycleRepository invoiceCycleRepository;
	
	@Autowired
	PaymentTermsRepository paymentTermsRepository;
	
	@Autowired
	HsnRepository hsnRepo;
	
	@Autowired
	SecurityDepositService securityDepositService;
	
	@Autowired
	EntityManager entityManager;
	
	@Autowired
	FeignLegacyInterface feignLegacyInterface;
	
	@Autowired
	UtilityService utilityService;
	
	@Autowired
	SecurityDepositRepository securityDepositRepository;
	
	@Autowired BankLocationRepo locationRepo;
	
	@Autowired
	ProjectInvoiceService projectInvoiceService;
	
	Logger logger = LoggerFactory.getLogger(InvoiceService.class);
	
	/**
	 * @author shivangi
	 * View Invoice details
	 */
	@Override
	public Map<String, Object> viewInvoice(String accessToken, Long projectInvoiceId, Boolean isInternalCall,
			Boolean isIfsd, Boolean isDollarCurrency) {
		Map<String, Object> invoiceMap = new HashMap<>();
		if (!isIfsd) {
			ProjectInvoice invoice = projectInvoiceRepository.findById(projectInvoiceId);
			List<ProjectInvoiceItemGetDto> itemList = new ArrayList<>();
			List<ProjectInvoiceItem> savedItemList = itemRepository
					.findAllByProjectInvoiceIdAndIsDeleted(invoice.getId(), false);
			Double total = 0D;
			Double totalDollar = 0D;
			invoice.setDollarCurrency(isDollarCurrency);
			for (ProjectInvoiceItem item : savedItemList) {
				ProjectInvoiceItemGetDto invoiceDto = getSavedItems(item, invoice);
				itemList.add(invoiceDto);
				total = total + invoiceDto.getTotalCost();
				totalDollar = totalDollar + invoiceDto.getTotalCostDollar();
			}
			
			if (isInternalCall)
				invoice = setInvoiceAmount(invoice, totalDollar);
			invoiceMap = getInvoiceData(invoice);
			Double adjustment = Double.valueOf(invoiceMap.get("adjustmentAmount").toString());
			total=total-(adjustment<0?-adjustment:adjustment);
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
		} else {
			invoiceMap = securityDepositService.viewSecurityDeposit(accessToken,projectInvoiceId, invoiceMap, false);
		}
		return invoiceMap;
	}

	private ProjectInvoiceItemDto setInvoiceItemDto(ProjectInvoice projectInvoice, Long userId, String grade, Double timesheetHours, String userName) {
		ProjectInvoiceItemDto dto=new ProjectInvoiceItemDto();
		dto.setGrade(grade);
		dto.setUserId(userId);
		dto.setProjectId(projectInvoice.getProjectId());
		dto.setProjectInvoiceId(projectInvoice.getId());
		dto.setTimesheetHours(timesheetHours);
		dto.setUserName(userName);
		dto.setUnitCost(0D);
		dto.setUnitDescription(userName);
		dto.setTotalCost(0D);
		return dto;
	}
	
	private ProjectInvoice setInvoiceAmount(ProjectInvoice invoice,double totalCost) {
		Double dollarexchangeCost = invoice.getExchangeRate();
		totalCost = totalCost - (invoice.getWaivedOffAmount()!= null ? invoice.getWaivedOffAmount() : 0);
		double taxabaleAmount = (9 * totalCost) / 100;
		double taxableTotalCost = totalCost;
		if(invoice.getInvoiceType()!=null && invoice.getInvoiceType().getName()!=null && invoice.getInvoiceType().getName().equals("Domestic")) {
			taxableTotalCost = taxableTotalCost+(taxabaleAmount*2);
		}
		invoice.setAmount(totalCost);
		if (invoice.getCurrency().equals("DOLLAR")) {
			invoice.setAmountInDollar(totalCost);
			invoice.setTaxableAmountInDollar(taxableTotalCost);
			invoice.setAmountInRupee(1/dollarexchangeCost * totalCost);
		} else if(invoice.getCurrency().equals("RUPEE")){
			invoice.setAmountInRupee(totalCost);
			invoice.setAmountInDollar(totalCost*dollarexchangeCost);
			invoice.setTaxableAmountInDollar(taxableTotalCost*dollarexchangeCost);
		} else {
			if(invoice.getExchangeRate()!=0) {
				invoice.setAmountInDollar(totalCost*invoice.getExchangeRate());
				invoice.setTaxableAmountInDollar(taxableTotalCost*invoice.getExchangeRate());
				invoice.setAmountInRupee((totalCost*invoice.getExchangeRate())*dollarexchangeCost);
			}
			else {
				invoice.setAmountInDollar(0);
				invoice.setTaxableAmountInDollar(0);
				invoice.setAmountInRupee(0);
			}
		}
		if(invoice.getSplitType().equals("Split")) {
			if(invoice.getAmount()==0)
				invoice= getPrimaryInvoiceAmount(invoice);
			ProjectInvoice splitInvoice= projectInvoiceRepository.findById(invoice.getConcernedSplitInvoice());
			
			if (splitInvoice != null && splitInvoice.getAmount()==0) {
				splitInvoice.setAmount(totalCost);
				if (splitInvoice.getCurrency().equals("DOLLAR")) {
					splitInvoice.setAmountInDollar(totalCost);
					splitInvoice.setTaxableAmountInDollar(taxableTotalCost);
					splitInvoice.setAmountInRupee((1 / dollarexchangeCost) * totalCost);
				} else if (splitInvoice.getCurrency().equals("RUPEE")) {
					splitInvoice.setAmountInDollar(totalCost * dollarexchangeCost);
					splitInvoice.setTaxableAmountInDollar(taxableTotalCost*dollarexchangeCost);
					splitInvoice.setAmountInRupee(totalCost);
				} else {
					if(splitInvoice.getExchangeRate()!=0) {
						splitInvoice.setAmountInDollar(totalCost*invoice.getExchangeRate());
						splitInvoice.setTaxableAmountInDollar(taxableTotalCost*invoice.getExchangeRate());
						splitInvoice.setAmountInRupee((totalCost*invoice.getExchangeRate())*dollarexchangeCost);
					}
					else {
						splitInvoice.setAmountInDollar(0);
						splitInvoice.setTaxableAmountInDollar(0);
						splitInvoice.setAmountInRupee(0);
					}
				}
				splitInvoice = getPrimaryInvoiceAmount(splitInvoice);
				projectInvoiceRepository.saveAndFlush(splitInvoice);
			}
		}
		projectInvoiceRepository.save(invoice);
		return invoice;
	}
	
	private ProjectInvoiceItem saveInvoiceEmployees(ProjectInvoiceItemDto itemDto) {
		Optional<ProjectInvoiceItem> item=itemRepository.findByUserIdAndProjectInvoiceIdAndIsDeleted(itemDto.getUserId(), itemDto.getProjectInvoiceId(), false);
		ProjectInvoiceItem employee=null;
		if(!item.isPresent()) {
			employee=new ProjectInvoiceItem();
			BeanUtils.copyProperties(itemDto, employee);
			employee.setIsIfsd(false);
			employee.setIsDeleted(false);
		}
		else {
			employee=item.get();
			BeanUtils.copyProperties(itemDto, employee);
		}
		
		itemRepository.save(employee);
		return employee;
	}
	
	private ProjectInvoiceItemGetDto getSavedItems(ProjectInvoiceItem item, ProjectInvoice invoice){
		ProjectInvoiceItemGetDto invoiceDto=new ProjectInvoiceItemGetDto();
		if(item.getTimesheetHours() == null)
			item.setTimesheetHours(0D);
		if(item != null)
			BeanUtils.copyProperties(item, invoiceDto);
		invoiceDto.setAdjustmentAmount(item.getAdjustmentAmount());
		invoiceDto.setTimesheetHours(Math.round(item.getTimesheetHours()*100.00)/100.00);
		
		invoiceDto.setTotalCostDollar(Math.round((item.getUnitCost()*item.getTimesheetHours())*100.00)/100.00);
		if(invoice.isDollarCurrency())
			invoiceDto.setTotalCost(Math.round((item.getUnitCost()*item.getTimesheetHours()*invoice.getExchangeRate())*100.00)/100.00);
		else
			invoiceDto.setTotalCost(Math.round((item.getUnitCost()*item.getTimesheetHours())*100.00)/100.00);


		return invoiceDto;
	}
	
	private Map<String,Object> getInvoiceData(ProjectInvoice invoice){
		Map<String,Object> invoiceMap=new HashMap<>();
		Long invoiceId = invoice.getId();
		invoiceMap.put("id", invoice.getId());
		invoiceMap.put("invoiceId", "INV-"+invoiceId);
		if(invoice.getSplitType()!=null && invoice.getSplitType().equals("Split")) {
			if(invoiceId >invoice.getConcernedSplitInvoice())
			invoiceMap.put("invoiceId", "INV-"+invoice.getConcernedSplitInvoice());
		}
		invoiceMap.put("isInternal", invoice.getIsInternal());
		invoiceMap.put("invoiceType", invoice.getInvoiceType().getName());
		invoiceMap.put("invoiceTypeId", invoice.getInvoiceType().getId());
		invoiceMap.put("isIfsd", false);
		invoiceMap.put("bankLocation", invoice.getBankLocation());
		invoiceMap.put("receivedOn", invoice.getReceivedOn());
		invoiceMap.put("clientName", invoice.getClientName());
		invoiceMap.put("projectId", invoice.getProjectId());
		invoiceMap.put("payDetails", invoice.getPayDetails());
		invoiceMap.put("isKycComplaint", invoice.getIsKycComplaint()!=null?invoice.getIsKycComplaint():null);
		invoiceMap.put("splitType", invoice.getSplitType()!=null ? invoice.getSplitType(): "NA");
		invoiceMap.put("splitInvoiceId", invoice.getConcernedSplitInvoice());
		InvoiceSourceUpdateDto sourceDto=getInvoiceSource();
		invoiceMap.put("source", sourceDto);
		invoiceMap.put("invoiceDate", invoice.getBillingDate());
		invoiceMap.put("dueDate", invoice.getDueDate());
		invoiceMap.put("currency", invoice.getCurrency());
		invoiceMap.put("exchangeRate", invoice.getExchangeRate());
		invoiceMap.put("tdsValue", invoice.getTdsValue());
		invoiceMap.put("fromDate", invoice.getFromDate().getTime());
		invoiceMap.put("invoicegenerationStatus", invoice.getInvoiceGenerationStatus());
		invoiceMap.put("toDate", invoice.getToDate().getTime());
		InvoiceStatus invoiceStatus = invoiceStatusRepository.findById(invoice.getInvoiceStatus());
		invoiceMap.put("status", invoiceStatus);
		invoiceMap.put("taxable", invoice.getTaxable());
		invoiceMap.put("paymentCharges", 0);
		if(invoice.getPaymentCharges()!=null)
			invoiceMap.put("paymentCharges", invoice.getPaymentCharges());
		invoiceMap.put("bank", invoice.getBank());
		invoiceMap.put("clientName", invoice.getClientName());
//		Map<String,Object> projectDetails= new HashMap<>();
		Map<String,Object> projectDetails = (Map<String, Object>) feignLegacyInterface.getProjectDescription(invoice.getProjectId(),"",null,null).get("data");
		invoiceMap.put("clientEmail", projectDetails.get("clientEmail"));
		invoiceMap.put("toCompanyName", projectDetails.get("clientCompany"));
		
		invoiceMap.put("primaryContactName", projectDetails.get("primaryContactName"));
		invoiceMap.put("secondaryEmail", projectDetails.get("secondaryEmail"));
		invoiceMap.put("clientRepresentativeEmail", "NA");
		invoiceMap.put("billingName", projectDetails.get("billingName").equals("NA") ? projectDetails.get("primaryContactName").equals("NA") ? invoice.getClientName() : projectDetails.get("primaryContactName")  : projectDetails.get("billingName"));
		invoiceMap.put("billingEmail",projectDetails.get("billingEmail").equals("NA") ? projectDetails.get("clientEmail") : projectDetails.get("billingEmail"));
		
		invoiceMap.put("toAddress", "NA");
		invoiceMap.put("toGST", "NA");
		InvoiceProjectSettings projectSettings = projectSettingsRepo.findByProjectId(invoice.getProjectId());
		if (projectSettings != null) {
			if(invoice.getProjectSettingId()!=null &&  projectSettings.getEmailId()!=null )
				invoiceMap.put("clientEmail", projectSettings.getEmailId());
			if(invoice.getProjectSettingId()!=null && projectSettings.getCompanyName()!=null)
				invoiceMap.put("toCompanyName", projectSettings.getCompanyName());
			if(projectSettings.getClientAddress()!=null)
				invoiceMap.put("toAddress", projectSettings.getClientAddress());
			if(projectSettings.getGstNumber()!=null)
				invoiceMap.put("toGST", projectSettings.getGstNumber());
		}
		invoiceMap.put("placeOfSupply", invoice.getPlaceOfSupply());
		invoiceMap.put("isIfsdAdjustment", invoice.getIsIfsdAdjustment()!=null?invoice.getIsIfsdAdjustment():false);
		invoiceMap.put("securityDepositeId", invoice.getSecurityDepositeId()!=null?invoice.getSecurityDepositeId():null);
		invoiceMap.put("adjustmentAmount", invoice.getAdjustmantAmount()!=null?invoice.getAdjustmantAmount():0.0);
		if(invoice.getSplitType().equals("Split")) {
			ProjectInvoice concernedInvoice=projectInvoiceRepository.findById(invoice.getConcernedSplitInvoice());
			invoiceMap.put("adjustmentAmount", invoice.getAdjustmantAmount()!=null && concernedInvoice.getAdjustmantAmount()!=null?(invoice.getAdjustmantAmount()+concernedInvoice.getAdjustmantAmount()):0.0);
		}
			
		invoiceMap.put("cityOfSupply", invoice.getCityOfSupply()!=null?invoice.getCityOfSupply():"NA");
		invoiceMap.put("payStatus", invoice.getPayStatus());
		if(invoice.getIsOthersInUrl()!=null)
			invoiceMap.put("isOthersInUrl", invoice.getIsOthersInUrl());
		else
			invoiceMap.put("isOthersInUrl", false);
		if(invoice.getIsMilestone()!=null)
			invoiceMap.put("isMilestone", invoice.getIsMilestone());
		else
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
	
	public InvoiceSourceUpdateDto getInvoiceSource(){
		List<InvoiceSource> sources=sourceRepository.findAllByIsArchived(false);
		InvoiceSourceUpdateDto sourceDto=new InvoiceSourceUpdateDto();
		if(!sources.isEmpty()) {
			InvoiceSource source=sources.get(0);
			BeanUtils.copyProperties(source, sourceDto);
		}
		return sourceDto;
	}
	
	/**
	 * @author shivangi
	 * Add invoice
	 */
	@Override
	public Object addProjectInvoiceForCreation(ProjectInvoiceDto projectInvoiceDto, String authorization) {
		if (!projectInvoiceDto.getIsIfsd()) {
			ProjectInvoice projectInvoice = createProjectInvoice(projectInvoiceDto, authorization);
			if ((!projectInvoiceDto.getIsMilestone() && projectInvoiceDto.getImportItems()))
				projectInvoice = setEmployeeData(projectInvoice, projectInvoiceDto.getFromDate(),
						projectInvoiceDto.getToDate());
			if((projectInvoice.getExchangeRate()!=0 && projectInvoiceDto.getExchangeRate()!=0 && projectInvoice.getExchangeRate()!=projectInvoiceDto.getExchangeRate())) {
				projectInvoice.setExchangeRate(projectInvoiceDto.getExchangeRate());
				Double totalCost=projectInvoice.getAmount();
				projectInvoice = setInvoiceAmount(projectInvoice, totalCost);
			}
			if (projectInvoiceDto.getSplitType().equals("Split")) {
				if(projectInvoiceDto.getId()==0) {
					projectInvoice= getPrimaryInvoiceAmount(projectInvoice);
					String month=new DateFormatSymbols().getMonths()[projectInvoice.getFromDate().getMonth()].toString();
					String year=Integer.toString(projectInvoice.getFromDate().getYear()+1900);
					projectInvoice.setMonth(month);
					projectInvoice.setYear(year);
				}
				projectInvoice=projectInvoiceRepository.save(projectInvoice);
				if(projectInvoice.getConcernedSplitInvoice()!=null)
					projectInvoiceDto.setId(projectInvoice.getConcernedSplitInvoice());
				ProjectInvoice concernedInvoice = createProjectInvoice(projectInvoiceDto, authorization);
				
				if ((!projectInvoiceDto.getIsMilestone() && projectInvoiceDto.getImportItems()))
					concernedInvoice = setEmployeeData(concernedInvoice, projectInvoiceDto.getFromDate(),
							projectInvoiceDto.getToDate());
				if (projectInvoiceDto.getId() == 0) {
					String month = new DateFormatSymbols().getMonths()[concernedInvoice.getToDate().getMonth()]
							.toString();
					String year = Integer.toString(concernedInvoice.getToDate().getYear() + 1900);
					concernedInvoice.setMonth(month);
					concernedInvoice.setYear(year);
					concernedInvoice.setConcernedSplitInvoice(projectInvoice.getId());
					
					concernedInvoice.setAmount(concernedInvoice.getAmount()-projectInvoice.getAmount());
					concernedInvoice.setAmountInDollar(concernedInvoice.getAmountInDollar()-projectInvoice.getAmountInDollar());
					concernedInvoice.setAmountInRupee(concernedInvoice.getAmountInRupee()-projectInvoice.getAmountInRupee());
					concernedInvoice.setTaxableAmountInDollar(concernedInvoice.getTaxableAmountInDollar()-projectInvoice.getTaxableAmountInDollar());
					concernedInvoice.setPaymentCharges(concernedInvoice.getPaymentCharges()-projectInvoice.getPaymentCharges());

				}

				concernedInvoice=projectInvoiceRepository.save(concernedInvoice);
				projectInvoice.setConcernedSplitInvoice(concernedInvoice.getId());
				projectInvoice=projectInvoiceRepository.save(projectInvoice);
			}

			return projectInvoice;
		} else {
			SecurityDeposit securityDeposit = securityDepositService.addSecurityDepositForCreation(projectInvoiceDto,
					authorization);
			return securityDeposit;
		}
	}
	
	public ProjectInvoice getPrimaryInvoiceAmount(ProjectInvoice invoice) {
		Calendar calendar = Calendar.getInstance();
	    calendar.setTime(invoice.getFromDate());

	    calendar.add(Calendar.MONTH, 1);
	    calendar.set(Calendar.DAY_OF_MONTH, 1);
	    calendar.add(Calendar.DATE, -1);
	    Date lastDayOfMonth = calendar.getTime();

	    int days=utilityService.getWorkingDaysBetweenDates(invoice.getFromDate(), lastDayOfMonth);
	    int totalDays=utilityService.getWorkingDaysBetweenDates(invoice.getFromDate(), invoice.getToDate());
		
	    double perDayPaymentCharges = invoice.getPaymentCharges()/totalDays;
		double perDayAmountDollar = invoice.getAmountInDollar()/totalDays;
		double perDayAmountRupee = invoice.getAmountInRupee()/totalDays;
		double perDayAmount=invoice.getAmount()/totalDays;
		double perDayAmountTax=invoice.getTaxableAmountInDollar()/totalDays;
		invoice.setTaxableAmountInDollar(perDayAmountTax*days);
		invoice.setAmount(perDayAmount*days);
		invoice.setAmountInDollar(perDayAmountDollar*days);
		invoice.setAmountInRupee(perDayAmountRupee*days);
		invoice.setPaymentCharges(perDayPaymentCharges*days);

		return invoice;
	}
	
	public Double getPrimaryAdjustmentAmount(ProjectInvoice invoice,Double adjustmentAmount) {
		Calendar calendar = Calendar.getInstance();
	    calendar.setTime(invoice.getFromDate());

	    calendar.add(Calendar.MONTH, 1);
	    calendar.set(Calendar.DAY_OF_MONTH, 1);
	    calendar.add(Calendar.DATE, -1);
	    Date lastDayOfMonth = calendar.getTime();
	    int days=utilityService.getWorkingDaysBetweenDates(invoice.getFromDate(), lastDayOfMonth);
	    int totalDays=utilityService.getWorkingDaysBetweenDates(invoice.getFromDate(), invoice.getToDate());
		double perDayAmountDollar = adjustmentAmount/totalDays;
		return perDayAmountDollar*days;
	}
	
	public ProjectInvoice createProjectInvoice(ProjectInvoiceDto projectInvoiceDto, String authorization) {
		ProjectInvoice projectInvoice = null;
		if (projectInvoiceDto.getId() == null || projectInvoiceDto.getId() == 0)
			projectInvoice = new ProjectInvoice();
		else
			projectInvoice = projectInvoiceRepository.findById(projectInvoiceDto.getId());
		String operation = "add";
		InvoiceType invoiceType = invoiceTypeRepository
				.findByNameAndIsArchived(projectInvoiceDto.getInvoiceType(), false);
		projectInvoice.setInvoiceType(invoiceType);
		projectInvoice.setFromDate(new Date(projectInvoiceDto.getFromDate()));
		projectInvoice.setIsKycComplaint(projectInvoiceDto.getIsKycComplaint());
		projectInvoice.setToDate(new Date(projectInvoiceDto.getToDate()));

		if(projectInvoiceDto.getInvoiceStatusId()==2) {
			projectInvoice.setPayingEntityName(projectInvoiceDto.getPayingEntityName());
			projectInvoice.setCurrencyRecevied(projectInvoiceDto.getCurrencyRecevied());
			projectInvoice.setRecievedAmount(projectInvoiceDto.getRecievedAmount());
		}
		
		BankLocation location = locationRepo.findByIdAndIsDeleted(projectInvoiceDto.getBankLocationId(), false);
		if(location!=null)
			projectInvoice.setBankLocation(location);

		if(projectInvoice.getInvoiceGenerationStatus()==null) {
			projectInvoice.setInvoiceGenerationStatus(InvoiceGenerationStatus.PENDING);
			projectInvoice.setDeleted(true);
//			projectInvoice.setIsDeleted(true);
		}
		
	
		projectInvoice.setIsMilestone(projectInvoiceDto.getIsMilestone());
		projectInvoice.setSplitType(projectInvoiceDto.getSplitType());
		saveProjectInvoiceData(projectInvoice, projectInvoiceDto, authorization, operation);
		projectInvoice = setInvoiceBilling(projectInvoice, authorization, projectInvoiceDto);
		return projectInvoice;
	}
	private ProjectInvoice setInvoiceBilling(ProjectInvoice invoice, String accessToken, ProjectInvoiceDto projectInvoiceDto) {
		PaymentMode paymentMode = paymentModeRepository.findById(projectInvoiceDto.getModeOfPaymentId());
		invoice.setModeOfPaymentId(paymentMode.getId());
		InvoiceCycle billingCycle = invoiceCycleRepository.findById(projectInvoiceDto.getInvoiceCycleId());
		invoice.setInvoiceCycleId(billingCycle.getId());
		PaymentTerms paymentTerms = paymentTermsRepository.findById(projectInvoiceDto.getPaymentTermsId());
		invoice.setPaymentTermsId(paymentTerms.getId());
		try {
			feignLegacyInterface.addProjectPaymentSettings(accessToken,projectInvoiceDto.getProjectId(), paymentMode.getId(),
						paymentTerms.getId(), billingCycle.getId());
				invoice=projectInvoiceRepository.save(invoice);
		} catch (Exception e) {
			logger.info(e.getMessage());
			return null;
		}

		return invoice;
	}
	
	private boolean saveProjectInvoiceData(ProjectInvoice projectInvoice, ProjectInvoiceDto projectInvoiceDto,
			String accessToken, String operation) {
		UserModel currentUser = validator.tokenbValidate(accessToken);
		projectInvoice = oldInvoiceService.setInvoiceInfo(projectInvoiceDto, projectInvoice);
		projectInvoice = oldInvoiceService.setInvoiceSources(projectInvoiceDto, projectInvoice);
		projectInvoice.setCreatorId(currentUser.getUserId());
		if(!projectInvoiceDto.getSplitType().equals("Split")) {
			projectInvoice.setMonth(projectInvoiceDto.getMonth());
			if(!projectInvoiceDto.getYear().equals(""))
			projectInvoice.setYear(projectInvoiceDto.getYear());
		}
		projectInvoice.setComment(projectInvoiceDto.getComment());
		projectInvoice.setCreatedDate(new Date());
		if(projectInvoiceDto.getInvoiceStatusId() == 5) 
			projectInvoice.setDisputedDate(java.time.LocalDateTime.now());
		InvoiceStatus invoiceStatus = invoiceStatusRepository.findById(projectInvoiceDto.getInvoiceStatusId());
		projectInvoice.setInvoiceStatus(invoiceStatus.getId());
		projectInvoice.setWaivedOffAmount(projectInvoiceDto.getWaivedOffAmount());
		projectInvoice.setExchangeRate(projectInvoiceDto.getExchangeRate());
		projectInvoice=oldInvoiceService.setBillingDetails(projectInvoiceDto, projectInvoice);
		try {
			projectInvoiceRepository.save(projectInvoice);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(e.getMessage());
			return false;
		}
		return true;
	}
	
	private ProjectInvoice setEmployeeData(ProjectInvoice invoice, Long from, Long to) {
		List<ProjectInvoiceItem> invoiceItems = fetchPreSavedItems(invoice);
		if (invoiceItems.isEmpty()) {
			List<Map<String,Object>> projectEmployees = new ArrayList<>();
			if(invoice!=null && feignLegacyInterface.getDatewiseTeamMembers(invoice.getProjectId(), from, to).containsKey("data"))
				projectEmployees = (List<Map<String, Object>>) feignLegacyInterface.getDatewiseTeamMembers(invoice.getProjectId(), from, to).get("data");
			int listSize = projectEmployees.size();
			Double totalCost = 0D;
			List<Long> currentUserIds = new ArrayList<>();
			for (int i = 0; i < listSize; i++) {
				Map<String, Object> userData = (Map<String, Object>) projectEmployees.get(i);
				ProjectInvoiceItemDto projectInvoiceItemDto = setInvoiceItemDto(invoice,
						new Long(userData.get("id").toString()), userData.get("grade").toString(),
						new Double(userData.get("expectedHours").toString()), userData.get("fullName").toString());
				ProjectInvoiceItem invoiceItem = saveInvoiceEmployees(projectInvoiceItemDto);
				invoiceItems.add(invoiceItem);
				totalCost = totalCost + projectInvoiceItemDto.getTotalCost();
				currentUserIds.add(new Long(userData.get("id").toString()));
			}
			invoice = setInvoiceAmount(invoice, totalCost);
			deleteNonExistingTeam(currentUserIds, invoice);
		}
		return invoice;
	}
	
	private List<ProjectInvoiceItem> fetchPreSavedItems(ProjectInvoice newInvoice) {
		Long newInvoiceId=newInvoice.getId();
		List<ProjectInvoiceItem> newItems = itemRepository.findAllByProjectInvoiceIdAndIsDeleted(newInvoice.getId(), false);
		if (newItems.isEmpty()) {
			Query q = entityManager.createNativeQuery("select max(id) from project_invoice where project_id=:projectId and is_completed=true");
			q.setParameter("projectId", newInvoice.getProjectId());
			Long invoiceId = null;
			if(q.getSingleResult()!=null)
				invoiceId = new Long(q.getSingleResult().toString());
			List<ProjectInvoiceItem> items = new ArrayList<>();
			List<ProjectInvoiceItem> savedItems = new ArrayList<>();
//			Long splitId=newInvoice.getConcernedSplitInvoice();
			if (invoiceId != null) {
				AtomicDouble totalCost=new AtomicDouble(0);
				items = itemRepository.findAllByProjectInvoiceIdAndIsDeleted(invoiceId, false);
				items.forEach(item -> {
					ProjectInvoiceItemDto projectInvoiceItemDto = new ProjectInvoiceItemDto();
					BeanUtils.copyProperties(item, projectInvoiceItemDto);
					
					ProjectInvoiceItem employee = new ProjectInvoiceItem();
					BeanUtils.copyProperties(projectInvoiceItemDto, employee);
					employee.setProjectInvoiceId(newInvoiceId);
					employee.setIsIfsd(false);
					employee.setIsDeleted(false);
//					if(splitId!=null)
//						employee.setConcernedSplitInvoice(splitId);
					itemRepository.save(employee);
					savedItems.add(employee);
					Double itemCost=(item.getTimesheetHours()*item.getUnitCost());
					totalCost.getAndAdd(itemCost);
				});
				newInvoice = setInvoiceAmount(newInvoice, totalCost.doubleValue());
			}
			return savedItems;
		}
		else
			return newItems;
	}
	
	private void deleteNonExistingTeam(List<Long> currentUserIds,ProjectInvoice invoice) {
		List<ProjectInvoiceItem> items=itemRepository.findAllByProjectInvoiceIdAndIsDeleted(invoice.getId(), false);
		items.forEach(item->{
			if(!currentUserIds.contains(item.getUserId()))
				item.setIsDeleted(true);
				itemRepository.save(item);
		});
		
	}

	/**
	 * @author shivangi
	 * Update Invoice Items
	 * Invoice Items are resources in that invoice
	 */
	@Override
	public ProjectInvoiceItemGetDto editInvoiceItems(String authorization, ProjectInvoiceItemGetDto invoiceItemDto) {
		Optional<ProjectInvoiceItem> item=itemRepository.findById(invoiceItemDto.getId());
		if(item.isPresent()) {
			BeanUtils.copyProperties(invoiceItemDto, item.get());
			itemRepository.save(item.get());
		}
		BeanUtils.copyProperties(item.get(), invoiceItemDto);
		invoiceItemDto.setTotalCostDollar(Math.round((invoiceItemDto.getUnitCost()*invoiceItemDto.getTimesheetHours())*100.00)/100.00);
		invoiceItemDto.setTotalCost(Math.round((invoiceItemDto.getUnitCost()*invoiceItemDto.getTimesheetHours())*100.00)/100.00);
		if(!item.get().getIsIfsd()) {
			ProjectInvoice invoice = projectInvoiceRepository.findById(invoiceItemDto.getProjectInvoiceId());
			if(invoice!=null) {
				item.get().setConcernedSplitInvoice(invoice.getConcernedSplitInvoice());
				itemRepository.save(item.get());
				if(invoice.getSplitType().equals("Split")) {
					ProjectInvoice splitInvoice = projectInvoiceRepository.findById(invoice.getConcernedSplitInvoice());
					if(splitInvoice!=null) {
						invoiceItemDto.setProjectInvoiceId(splitInvoice.getId());
						Optional<ProjectInvoiceItem> splitItem = itemRepository.findByUserNameAndIsDeletedAndProjectInvoiceId(
								invoiceItemDto.getUserName(), false, invoiceItemDto.getProjectInvoiceId());
						if(splitItem.isPresent()) {
							Long splitItemId=splitItem.get().getId();
							invoiceItemDto.setId(splitItemId);
							BeanUtils.copyProperties(invoiceItemDto, splitItem.get());
							splitItem.get().setConcernedSplitInvoice(splitInvoice.getConcernedSplitInvoice());
							itemRepository.save(item.get());
						}
						if(splitInvoice.getId()<invoice.getId()) 
							invoice= splitInvoice;
					}
				}
				recalculateTotalCost(invoice);
			}
			}
		else {
			Optional<SecurityDeposit> securityDeposit = securityDepositRepo.findById(invoiceItemDto.getProjectInvoiceId());
			securityDepositService.recalculateTotalCost(securityDeposit.get()); 
		}
		
		return invoiceItemDto;
	}
	
	private void recalculateTotalCost(ProjectInvoice invoice) {
		List<ProjectInvoiceItem> savedItemList = itemRepository.findAllByProjectInvoiceIdAndIsDeletedAndIsIfsd(invoice.getId(), false,false);
		Double totalCost = 0D;
		for (ProjectInvoiceItem item : savedItemList) {
			totalCost= totalCost + (item.getUnitCost()* (item.getTimesheetHours()!=null?item.getTimesheetHours():0D));
		}
		Double dollarexchangeCost = invoice.getExchangeRate();
		double taxabaleAmount = (9 * totalCost) / 100;
		double taxableTotalCost = totalCost;
		if(invoice.getInvoiceType().getName().equals("Domestic")) {
			taxableTotalCost = taxableTotalCost+(taxabaleAmount*2);
		}
		invoice.setAmount(totalCost);
		if (invoice.getCurrency().equals("DOLLAR")) {
			invoice.setAmountInDollar(totalCost);
			invoice.setTaxableAmountInDollar(taxableTotalCost);
			invoice.setAmountInRupee((1/dollarexchangeCost) * totalCost);
		} else if (invoice.getCurrency().equals("RUPEE")) {
			invoice.setAmountInDollar(totalCost*dollarexchangeCost);
			invoice.setTaxableAmountInDollar(taxableTotalCost*dollarexchangeCost);
			invoice.setAmountInRupee(totalCost);
		} else {
			invoice.setAmountInDollar(totalCost/invoice.getExchangeRate());
			invoice.setTaxableAmountInDollar(taxableTotalCost/invoice.getExchangeRate());
			invoice.setAmountInRupee(invoice.getAmountInDollar()*dollarexchangeCost);
		}
		if(invoice.getSplitType().equals("Split")) {
			invoice= getPrimaryInvoiceAmount(invoice);
			ProjectInvoice splitInvoice= projectInvoiceRepository.findById(invoice.getConcernedSplitInvoice());
			splitInvoice.setAmount(totalCost);
			if (splitInvoice.getCurrency().equals("DOLLAR")) {
				splitInvoice.setAmountInDollar(totalCost);
				splitInvoice.setTaxableAmountInDollar(taxableTotalCost);
				splitInvoice.setAmountInRupee((1/dollarexchangeCost) * totalCost);
			} else if (splitInvoice.getCurrency().equals("RUPEE")) {
				splitInvoice.setAmountInDollar(totalCost*dollarexchangeCost);
				splitInvoice.setTaxableAmountInDollar(taxableTotalCost*dollarexchangeCost);
				splitInvoice.setAmountInRupee(totalCost);
			} else {
				splitInvoice.setAmountInDollar(totalCost/invoice.getExchangeRate());
				splitInvoice.setTaxableAmountInDollar(taxableTotalCost/invoice.getExchangeRate());
				splitInvoice.setAmountInRupee(invoice.getAmountInDollar()*dollarexchangeCost);
			}
			splitInvoice.setAmount(splitInvoice.getAmount()- invoice.getAmount());
			splitInvoice.setAmountInDollar(splitInvoice.getAmountInDollar()-invoice.getAmountInDollar());
			splitInvoice.setTaxableAmountInDollar(splitInvoice.getTaxableAmountInDollar()-invoice.getTaxableAmountInDollar());
			splitInvoice.setAmountInRupee(splitInvoice.getAmountInRupee()-invoice.getAmountInRupee());
			projectInvoiceRepository.saveAndFlush(splitInvoice);
		}
		projectInvoiceRepository.saveAndFlush(invoice);
	}

	/**
	 * @author shivangi
	 * Delete Invoice Items
	 */
	@Override
	public ProjectInvoiceItem deleteInvoiceItems(String authorization, Long id) {
		Optional<ProjectInvoiceItem> item = itemRepository.findById(id);
		if (item.isPresent()) {
			item.get().setIsDeleted(true);
			itemRepository.save(item.get());
		}
		ProjectInvoice invoice = projectInvoiceRepository.findById(item.get().getProjectInvoiceId());
		if (invoice != null & !item.get().getIsIfsd()) {
			if(invoice.getSplitType().equals("Split")) {
				ProjectInvoice splitInvoice = projectInvoiceRepository.findById(invoice.getConcernedSplitInvoice());
				if(splitInvoice!=null) {
					Optional<ProjectInvoiceItem> splitItem = itemRepository.findByUnitDescriptionAndIsDeletedAndProjectInvoiceId(
							item.get().getUserName(), false, splitInvoice.getId());
					if(splitItem.isPresent()) {
						splitItem.get().setIsDeleted(true);
						itemRepository.save(item.get());
					}
					if(splitInvoice.getId()<invoice.getId()) 
						invoice= splitInvoice;
				}
			}
			recalculateTotalCost(invoice);
		} else {
			Optional<SecurityDeposit> securityDeposit = securityDepositRepo.findById(item.get().getProjectInvoiceId());
			if (securityDeposit.isPresent()) {
				securityDepositService.recalculateTotalCost(securityDeposit.get());
			}
		}
		return item.get();
	}
	
	/**
	 * To save the generated invoice
	 * @param accessToken
	 * @param invoiceDto
	 * @return
	 */
	@Override
	public Map<String, Object> saveGeneratedInvoice(String accessToken, ProjectInvoiceGenerateDto invoiceDto) {
		Map<String, Object> responseMap = null;
		if (!invoiceDto.getIsIfsd()) {
			ProjectInvoice invoice = projectInvoiceRepository.findById(invoiceDto.getId());
			Long splitId= invoice.getConcernedSplitInvoice();
			
			Double adjustmentAmount=0D;
			if(invoiceDto.getSecurityDepositeId()!=null) {
				Optional<SecurityDeposit> securityDep=securityDepositRepository.findById(invoiceDto.getSecurityDepositeId());
				if(securityDep.isPresent()) {
					adjustmentAmount=setSecurityDepositAdjustedAmount(invoice, invoiceDto,securityDep.get());
					if(adjustmentAmount == null){
						responseMap = new HashMap<>();
						responseMap.put("message", "Adjustment amount should be less than or equal to IFSD amount " + securityDep.get().getAmount());
						return responseMap;
					}
						
				}
			}
			
			invoice= saveInvoiceAfterCreation(invoice, invoiceDto, adjustmentAmount);

			
			if(invoice.getSplitType().equals("Split")) {
				invoiceDto.setId(splitId);
				ProjectInvoice splitInvoice = projectInvoiceRepository.findById(splitId);
				splitInvoice = saveInvoiceAfterCreation(splitInvoice, invoiceDto,adjustmentAmount);
			}
			responseMap = prepareResponseForPdf(accessToken, invoice);
		} else {
			responseMap=securityDepositService.saveGeneratedInvoice(accessToken, invoiceDto, responseMap);
		}
		return responseMap;
	}
	
	private ProjectInvoice saveInvoiceAfterCreation(ProjectInvoice invoice, ProjectInvoiceGenerateDto invoiceDto, Double adjustmentAmount) {
		Date receivedOn = invoice.getReceivedOn();
		String splitType=invoice.getSplitType();
		//boolean isDollarCurrency = invoiceDto.getIsDollarCurrency();
		invoiceDto.setAmount(invoice.getAmount());
		invoiceDto.setAmountInDollar(invoice.getAmountInDollar());
		BeanUtils.copyProperties(invoiceDto, invoice);
		invoice.setDeleted(false);
		if(invoiceDto.getIsDollarCurrency() != null)
			invoice.setDollarCurrency(invoiceDto.getIsDollarCurrency());
//		invoice.setIsDeleted(false);
		invoice.setCompleted(true);
		invoice.setReceivedOn(receivedOn);
		invoice.setSplitType(splitType);
		InvoiceBank bank = bankRepository.findByNameAndIsArchived(invoiceDto.getBankName(), false);
		if (bank != null)
			invoice.setBank(bank);
		if (invoiceDto.getTaxable().equals("YES"))
			invoice.setTaxable(Taxable.YES);
		else
			invoice.setTaxable(Taxable.NO);
		
		if(invoice.getInvoiceGenerationStatus().equals(InvoiceGenerationStatus.PENDING) || invoice.getInvoiceGenerationStatus().equals(InvoiceGenerationStatus.INDRAFT)) {
			invoice.setAdjustmantAmount(adjustmentAmount);
			invoice.setAdjustmentDate(DateUtils.truncate(new Date(), Calendar.DATE));
		if(invoice.getSplitType().equals("Split") && invoice.getId()<invoice.getConcernedSplitInvoice()) {
			adjustmentAmount=getPrimaryAdjustmentAmount(invoice, adjustmentAmount);
			invoice.setAdjustmantAmount(adjustmentAmount);
		}else if(invoice.getSplitType().equals("Split") && invoice.getId()>invoice.getConcernedSplitInvoice()) {
			ProjectInvoice primaryInv=projectInvoiceRepository.findById(invoice.getConcernedSplitInvoice());
			adjustmentAmount=adjustmentAmount-primaryInv.getAdjustmantAmount();
			invoice.setAdjustmantAmount(adjustmentAmount);
		}
		}
		else 
			adjustmentAmount =0D;
		if (invoiceDto.getGenerationStatus().equals("INDRAFT"))
			invoice.setInvoiceGenerationStatus(InvoiceGenerationStatus.INDRAFT);
		else 
			invoice.setInvoiceGenerationStatus(InvoiceGenerationStatus.GENERATED);
			
		
		
		
		Double invTotalCost = invoice.getAmount();
		double invTaxabaleAmount = (9 * invTotalCost) / 100;
		double invTaxableTotalCost= invTotalCost;
		if(invoice.getInvoiceType().getName().equals("Domestic")) {
			invTaxableTotalCost = invTaxableTotalCost+(invTaxabaleAmount*2);
		}
		
		invoice.setAmount(invTotalCost);
		if (invoice.getCurrency().equals("DOLLAR")) {
			invoice.setAmountInDollar(invTotalCost);
			invoice.setTaxableAmountInDollar(invTaxableTotalCost);
			invoice.setExchangeRate(1);
		} else if (invoice.getCurrency().equals("RUPEE")) {
			invoice.setAmountInDollar(invTotalCost*invoice.getExchangeRate());
			invoice.setTaxableAmountInDollar(invTaxableTotalCost*invoice.getExchangeRate());
		} else {
			invoice.setAmountInDollar(invTotalCost*invoice.getExchangeRate());
			invoice.setTaxableAmountInDollar(invTaxableTotalCost*invoice.getExchangeRate());
		}
		projectInvoiceRepository.saveAndFlush(invoice);
		return invoice;
	}
	
	private Double setSecurityDepositAdjustedAmount(ProjectInvoice invoice, ProjectInvoiceGenerateDto invoiceDto,
			SecurityDeposit security) {
		Double adjustmentAmount = 0D;
		Double totalCost = 0D;

		if (security.getCurrency().equals(invoice.getCurrency())) {
			adjustmentAmount = invoiceDto.getAdjustmentAmount();

		} else if (!security.getCurrency().equals(invoice.getCurrency())) {
			adjustmentAmount = invoiceDto.getAdjustmentAmount() * security.getExchangeRate();
		}
//		totalCost = security.getAmount() - adjustmentAmount;
		totalCost = security.getAmount();
		if(totalCost < adjustmentAmount)
			return null;
		double taxabaleAmount = (9 * totalCost) / 100;
		double taxableTotalCost = totalCost;
		if (invoice.getInvoiceType().getName().equals("Domestic")) {
			taxableTotalCost = taxableTotalCost + (taxabaleAmount * 2);
		}
		security.setAmount(totalCost);
		if (security.getCurrency().equals("DOLLAR")) {
			security.setAmountInDollar(totalCost);
			security.setTaxableAmount(taxableTotalCost);
		} else if (security.getCurrency().equals("RUPEE")) {
			security.setAmountInDollar(totalCost * security.getExchangeRate());
			security.setTaxableAmount(taxableTotalCost * security.getExchangeRate());
		} else {
			security.setAmountInDollar(totalCost * security.getExchangeRate());
			security.setTaxableAmount(taxableTotalCost * security.getExchangeRate());
		}
		securityDepositRepository.save(security);
		return adjustmentAmount;
	}
	 
	private Map<String,Object> prepareResponseForPdf(String accessToken,ProjectInvoice invoice){
		Map<String,Object> invoiceMap=new HashMap<>();
		invoiceMap.put("id", invoice.getId());
		invoiceMap.put("invoiceId", "INV-"+invoice.getId());
		invoiceMap.put("invoiceDate", invoice.getBillingDate());
		invoiceMap.put("dueDate", invoice.getDueDate());
		invoiceMap.put("isOthersInUrl", invoice.getIsOthersInUrl());
		invoiceMap.put("payDetails", invoice.getPayDetails());
		InvoiceSourceUpdateDto sourceDto=getInvoiceSource();
		invoiceMap.put("source", sourceDto);
		invoiceMap.put("invoiceType", invoice.getInvoiceType().getName());
		invoiceMap.put("invoiceTypeId", invoice.getInvoiceType().getId());
		invoiceMap.put("clientName", invoice.getClientName());
		Map<String,Object> projectDetails = (Map<String, Object>) feignLegacyInterface.getProjectDescription(invoice.getProjectId(),"",null,null).get("data");
		invoiceMap.put("clientEmail", projectDetails.get("clientEmail"));
		invoiceMap.put("toCompanyName", projectDetails.get("clientCompany"));
		invoiceMap.put("toAddress", "NA");
		invoiceMap.put("toGST", "NA");
		InvoiceProjectSettings projectSettings = projectSettingsRepo.findByProjectId(invoice.getProjectId());
		if (projectSettings != null) {
			if(invoice.getProjectSettingId()!=null && projectSettings.getEmailId()!=null )
				invoiceMap.put("clientEmail", projectSettings.getEmailId());
			if(invoice.getProjectSettingId()!=null && projectSettings.getCompanyName()!=null )
				invoiceMap.put("toCompanyName", projectSettings.getCompanyName());
			invoiceMap.put("toAddress", projectSettings.getClientAddress());
			invoiceMap.put("toGST", projectSettings.getGstNumber());
		}
		invoiceMap.put("bank", invoice.getBank());
		invoiceMap.put("currency", invoice.getCurrency());
		invoiceMap.put("taxable", invoice.getTaxable());
		invoiceMap.put("invoicegenerationStatus", invoice.getInvoiceGenerationStatus());
		////////
		invoiceMap.put("placeOfSupply", invoice.getPlaceOfSupply());
		invoiceMap.put("cityOfSupply", invoice.getCityOfSupply());
		invoiceMap.put("payStatus", invoice.getPayStatus());
		
		invoiceMap=viewInvoice(accessToken, invoice.getId(),false,false,invoice.isDollarCurrency());
		return invoiceMap;
	}
	
	@Override
	public Map<String,Object> generatePDFFromHTML(String accessToken,InvoiceSlipDto slipDto) throws Exception{
		InvoiceSlip slip = invoiceSlipRepo.findByInvoiceIdAndIsDeletedAndIsIfsd(slipDto.getInvoiceId(),false,slipDto.getIsIfsd());
		List<InvoiceSlip> previousSlips = invoiceSlipRepo.findAllByInvoiceId(slipDto.getInvoiceId());
		Map<String, Object> result = new HashMap<>();
		UserModel user=validator.tokenbValidate(accessToken);
		int slipCount=previousSlips.size();
			String fileName="/OODLES_INV-"+slipDto.getInvoiceId()+".pdf";
			if (slip != null ) {
				slip.setDeleted(true);
				fileName="/OODLES_INV-"+slipDto.getInvoiceId()+"("+slipCount+").pdf";
			}
			FontProvider fontProvider = new DefaultFontProvider(true, true, true);
			fontProvider.addSystemFonts();
			ConverterProperties converterProperties = new ConverterProperties();
			converterProperties.setFontProvider(fontProvider);
			HtmlConverter.convertToPdf(slipDto.getHtml(), new FileOutputStream(fileName),converterProperties);
			// Reading file and saving the data in DTO.
			byte[] data = Files.readAllBytes(Paths.get(fileName));
			// upload file on AWS.
			result = fileuploadService.uploadFileOnS3Bucket(data, fileName, "/invoiceSlips");
			slip = new InvoiceSlip();
			slip.setDeleted(false);
			slip.setFileName(result.get("originalFileName").toString());
			slip.setFilePath(result.get("imagePath").toString());
			slip.setFileUploaderName(user.getEmpName());
			slip.setInvoiceId(slipDto.getInvoiceId());
			slip.setUploadedBy(user.getUserId());
			slip.setUploadedDate(LocalDateTime.now());
			slip.setIfsd(slipDto.getIsIfsd());
			slip=invoiceSlipRepo.save(slip);
//		}
		String response = fileuploadService.generatePresignedUrl(slip.getFilePath(), "/invoiceSlips",
				slip.getFileName());
		Map<String, Object> url = new HashMap<>();
		url.put("fileName", fileName);
		url.put("url", response);
		return url;
	}

	@Override
	public Object resetInvoice(Long invoiceId,boolean isIfsd) {
		ProjectInvoice invoice = null;
		Optional<SecurityDeposit> securityDeposit = Optional.empty();
		if(!isIfsd)
			invoice = projectInvoiceRepository.findById(invoiceId);
		else
			securityDeposit=securityDepositRepo.findById(invoiceId);
		if(invoice!=null) {
			invoice.setInvoiceGenerationStatus(InvoiceGenerationStatus.INDRAFT);
			if(invoice.getSplitType().equals("Split")) {
				ProjectInvoice splitInvoice = projectInvoiceRepository.findById(invoice.getConcernedSplitInvoice());
				splitInvoice.setInvoiceGenerationStatus(InvoiceGenerationStatus.INDRAFT);
				try {
					projectInvoiceRepository.saveAndFlush(splitInvoice);
				}catch (Exception e) {
					throw e;
				}
			}
			projectInvoiceRepository.save(invoice);
			return invoice;
		}else if(securityDeposit.isPresent()) {
			securityDeposit.get().setInvoiceGenerationStatus(InvoiceGenerationStatus.INDRAFT);
			securityDepositRepo.save(securityDeposit.get());
			return securityDeposit.get();
		}
		return null;
	}
	
	@Override
	public ProjectInvoiceItemGetDto addInvoiceItem(ProjectInvoiceItemDto invoiceItemDto, String accessToken) {
		Optional<ProjectInvoiceItem> item = itemRepository.findByUnitDescriptionAndIsDeletedAndProjectInvoiceId(
				invoiceItemDto.getUserName(), false, invoiceItemDto.getProjectInvoiceId());
		ProjectInvoiceItem newItem = null;
		ProjectInvoiceItemGetDto result = null;
		if (!item.isPresent()) {
			newItem = createNewItem(newItem, invoiceItemDto);
		}
		BeanUtils.copyProperties(newItem, invoiceItemDto);
		if (invoiceItemDto.getIsIfsd()) {
			Optional<SecurityDeposit> invoice = securityDepositRepo.findById(invoiceItemDto.getProjectInvoiceId());
			if (invoice.isPresent()) {
				securityDepositService.recalculateTotalCost(invoice.get());
			}
			result = securityDepositService.getSavedItems(newItem);
		} else {
			ProjectInvoice invoice = projectInvoiceRepository.findById(invoiceItemDto.getProjectInvoiceId());
			if (invoice != null) {
				recalculateTotalCost(invoice);
				newItem.setConcernedSplitInvoice(invoice.getConcernedSplitInvoice());
				itemRepository.save(newItem);
				ProjectInvoice splitInvoice = projectInvoiceRepository.findById(invoice.getConcernedSplitInvoice());
				if(splitInvoice!=null) {
					invoiceItemDto.setProjectInvoiceId(splitInvoice.getId());
					ProjectInvoiceItem splitItem=null;
					splitItem=createNewItem(splitItem, invoiceItemDto);
					splitItem.setConcernedSplitInvoice(splitInvoice.getConcernedSplitInvoice());
					itemRepository.save(newItem);	
				}
				result = getSavedItems(newItem, invoice);
			}
		}
		return result;
	}
	
	private ProjectInvoiceItem createNewItem(ProjectInvoiceItem newItem, ProjectInvoiceItemDto invoiceItemDto) {
		newItem = new ProjectInvoiceItem();
		BeanUtils.copyProperties(invoiceItemDto, newItem);
		if(invoiceItemDto.getUnitDescription().equals("")) {
			newItem.setUnitDescription(invoiceItemDto.getUserName());
		}
		newItem.setIsDeleted(false);
		newItem = itemRepository.save(newItem);
		return newItem;
	}
	

}
