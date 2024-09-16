package com.krishna.service;

import static java.util.stream.Collectors.toList;

import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.krishna.Interfaces.IInvoiceService;
import com.krishna.controller.FeignLegacyInterface;
import com.krishna.controller.PayRegisterController;
import com.krishna.domain.BankLocation;
import com.krishna.domain.IndirectCost;
import com.krishna.domain.ProjectTrendsDTO;
import com.krishna.domain.SecurityDeposit;
import com.krishna.domain.UserModel;
import com.krishna.domain.invoice.Currency;
import com.krishna.domain.invoice.InvoiceBank;
import com.krishna.domain.invoice.InvoiceCycle;
import com.krishna.domain.invoice.InvoiceProjectSettings;
import com.krishna.domain.invoice.InvoiceStatus;
import com.krishna.domain.invoice.InvoiceType;
import com.krishna.domain.invoice.PaymentMode;
import com.krishna.domain.invoice.PaymentTerms;
import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.dto.InvoiceCycleDto;
import com.krishna.dto.InvoiceFilterDto;
import com.krishna.dto.PaymentModeDto;
import com.krishna.dto.PaymentTermsDto;
import com.krishna.dto.ProjectDto;
import com.krishna.dto.ProjectInvoiceDto;
import com.krishna.dto.UserIdsDto;
import com.krishna.dto.invoice.ClientUpdateDto;
import com.krishna.dto.invoice.InvoiceSlipDto;
import com.krishna.dto.invoice.ProjectSettingsDto;
import com.krishna.dto.invoice.ThankYouMailDto;
import com.krishna.enums.Months;
import com.krishna.enums.PayStatus;
import com.krishna.repository.BankLocationRepo;
import com.krishna.repository.ExpectedBillingRepository;
import com.krishna.repository.IndirectCostRepository;
import com.krishna.repository.MarginBasisRepository;
import com.krishna.repository.ProjectExpectedHoursRepository;
import com.krishna.repository.SecurityDepositRepository;
import com.krishna.repository.invoice.CurrencyRepository;
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
import com.krishna.schedulers.Scheduler;
import com.krishna.security.JwtAuthorizationFilter;
import com.krishna.security.JwtValidator;
import com.krishna.service.util.ConsolidatedService;
import com.krishna.service.util.UtilityService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.TicketUtilities;

@Service
@SuppressWarnings({"removal","unchecked"})
public class ProjectInvoiceService {

	@Autowired BankLocationRepo locationRepo;
	
	@Autowired
	ProjectInvoiceRepository projectInvoiceRepository;

	@Autowired
	public PaymentModeRepository paymentModeRepository;

	@Autowired
	public InvoiceCycleRepository invoiceCycleRepository;

	@Autowired
	public PaymentTermsRepository paymentTermsRepository;

	@Autowired
	CurrencyRepository currencyRepository;

	@Autowired
	InvoiceStatusRepository invoiceStatusRepository;

	@Autowired
	InvoiceSourceRepository sourceRepository;

	@Autowired
	JwtValidator validator;

	@Autowired
	MailService mailService;

	@Autowired
	LoginUtiltiyService loginUtilityService;

	@Autowired
	DollarCostServiceImpl dollarCostService;

	@Autowired
	ProjectExpectedHoursRepository hoursrepo;

	@Autowired
	UtilityService utilService;

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
	ConsolidatedService consolidateService;

	@Autowired
	ProjectMarginService projectMarginService;
	
	@Autowired
	IndirectCostService indirectCostService;

	@Autowired
	IndirectCostRepository costRepository;

	@Autowired
	Scheduler scheduler;
		
	@Autowired
	TemplateEngine templateEngine;
	@Autowired
	SecurityDepositRepository securityDepositRepository;
	
	@Autowired
	IInvoiceService invoiceService;
	
	@Value("${env.url}")
	private String environmentUrl;
	
	@Value("${com.oodles.accounts.email}")
	private String accountsMail;
	
	@Autowired
	EntityManager entityManager;
	
	@Value("${com.oodles.legal.email}")
	private String legalMail;

	@Autowired
	FeignLegacyInterface feignLegacyInterface;
	
	@Autowired
	JwtAuthorizationFilter validatorForAudit;
	
	@Autowired
	InvoiceSlipRepository invoiceSlipRepo;
	
	@Autowired
	FileUploadService fileuploadService;

	@Autowired
	MarginBasisRepository marginBasisRepo;
	
	Logger log = LoggerFactory.getLogger(PayRegisterController.class);

	static String SECRET_ENCRYPTION_KEY;
	static String SECRET_NEW_ENCRYPTION_KEY;

	@Value("${com.oodles.accounts.key}")
	public void setKey(String key) {
		ProjectInvoiceService.SECRET_ENCRYPTION_KEY = key;
	}

	@Value("${com.oodles.accounts.new.key}")
	public void setNewKey(String key) {
		ProjectInvoiceService.SECRET_NEW_ENCRYPTION_KEY = key;
	}

	Logger logger = LoggerFactory.getLogger(ProjectInvoiceService.class);

	/**
	 * Keeps the project expected Hours data. See getProjectExpectedHours method.
	 */
	private List<Map<String, Object>> projectExpectedHours = null;
	

	public ProjectInvoice addProjectInvoice(ProjectInvoiceDto projectInvoiceDto, String authorization) {
		ProjectInvoice projectInvoice = new ProjectInvoice();
		String operation = "add";
		boolean result = saveProjectInvoiceData(projectInvoice, projectInvoiceDto, authorization, operation);
		if(result) {
			return projectInvoice;
		}
		return null;
	}

	public Map<String, Object> getInvoiceData(String authorization, InvoiceFilterDto invoiceFilterDto,
			List<Map<String, Object>> allProjectDetails) {
		List<ProjectInvoice> projectInvoice = new ArrayList<>();
		UserModel currentUser = validator.tokenbValidate(authorization);
		String buFromName = "";
		String buToName = "";
		List<Map<String, Object>> buList = (List<Map<String, Object>>) feignLegacyInterface
				.getBusinessVerticalDetails(authorization).get("data");
		Map<String, Object> buMap = new HashMap<>();
		int monthNum = 0;
		if (invoiceFilterDto.getMonth() != null && !invoiceFilterDto.getMonth().equals("")
				&& !invoiceFilterDto.getYear().equals("")) {
			monthNum = loginUtilityService.getMonthNumber(invoiceFilterDto.getMonth());
			buMap = (Map<String, Object>) feignLegacyInterface
					.monthwiseProjectBUData(authorization, monthNum, Integer.parseInt(invoiceFilterDto.getYear()))
					.get("data");
		}
		final Map<String, Object> buMapFinalData = buMap;
		
		if(invoiceFilterDto.getFromDate()!=null && invoiceFilterDto.getToDate()!= null)
			projectInvoice = projectInvoiceRepository.findAllByDateRangeFilterIsDeleted( new Date (invoiceFilterDto.getFromDate()), new Date (invoiceFilterDto.getToDate()), false);
		else {
			if (invoiceFilterDto.getMonth().equals("") && !invoiceFilterDto.getYear().equals("")) {
				projectInvoice = projectInvoiceRepository.findAllByYearAndIsDeleted(invoiceFilterDto.getYear(), false);

			} else if (!invoiceFilterDto.getYear().equals("") && !invoiceFilterDto.getMonth().equals("")) {
				projectInvoice = projectInvoiceRepository.findAllByMonthAndYearAndIsDeleted(invoiceFilterDto.getMonth(),
						invoiceFilterDto.getYear(), false);
			} else {
				projectInvoice = projectInvoiceRepository.findAllByIsDeleted(false);

			}
		}
		
		List<Long> createdIdList = new ArrayList<>();
		projectInvoice.forEach(invoice -> {
			createdIdList.add(invoice.getCreatorId());
		});
		UserIdsDto uids = new UserIdsDto();
		uids.setUserIds(createdIdList);

		List<Map<String, Object>> userNameList = (List<Map<String, Object>>) feignLegacyInterface
				.getUserNameList(authorization, uids).get("data");
		List<InvoiceCycle> billingCycleList = invoiceCycleRepository.findAll();
		List<InvoiceStatus> invoiceStatusList = invoiceStatusRepository.findAll();
		List<PaymentMode> paymentModeList = paymentModeRepository.findAll();
		List<PaymentTerms> paymentTermsList = paymentTermsRepository.findAll();
		List<ProjectInvoice> filteredList = new ArrayList<>();
		List<ProjectInvoice> internalList = new ArrayList<>();
		projectInvoice.forEach(invoice -> {
			if (invoiceFilterDto.isInternal()) {
				if (invoice.getIsInternal()) {
					Map<String, Object> buFilter = null;
					if (!invoiceFilterDto.getBusinessVertical().equals("")) {
						buFilter = buList.stream().filter(
								bu -> (bu.get("name").toString()).equals(invoiceFilterDto.getBusinessVertical()))
								.findFirst().orElse(null);
						if (buFilter != null
								&& (invoice.getRaisedFromBu().toString().equals(buFilter.get("id").toString())
										|| invoice.getRaisedToBu().toString().equals(buFilter.get("id").toString()))) {
							filteredList.add(invoice);
						}
					} else {
						filteredList.add(invoice);
					}
				}
			} else {
				if (invoice.getIsInternal()) {
					Map<String, Object> buFilter = null;
					if (!invoiceFilterDto.getBusinessVertical().equals("")) {
						buFilter = buList.stream().filter(
								bu -> (bu.get("name").toString()).equals(invoiceFilterDto.getBusinessVertical()))
								.findFirst().orElse(null);
						if (buFilter != null
								&& (invoice.getRaisedFromBu().toString().equals(buFilter.get("id").toString())
										|| invoice.getRaisedToBu().toString().equals(buFilter.get("id").toString()))) {
							internalList.add(invoice);
						}
					} else {
						internalList.add(invoice);
					}
				} else {
					String projectBusinessVertical = ConstantUtility.UNASSIGNED;
					if (buMapFinalData != null && buMapFinalData.containsKey(invoice.getProjectId().toString())) {
						projectBusinessVertical = buMapFinalData.get(invoice.getProjectId().toString()).toString();
					} else {
						Map<String, Object> projectDetails = this.getProjectDetail(allProjectDetails,
								invoice.getProjectId());
						if (projectDetails != null)
							projectBusinessVertical = (String) projectDetails.get("businessVertical");
					}
					if (!invoiceFilterDto.getBusinessVertical().equals("")) {
						if (projectBusinessVertical.equals(invoiceFilterDto.getBusinessVertical())) {
							filteredList.add(invoice);
						}
					}
					if (invoiceFilterDto.getBusinessVertical().equals("")) {
						filteredList.add(invoice);
					}
				}
			}
		});

		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object> response = new HashMap<>();
		Map<String, Object> widgetsData = getWidgetsData(filteredList, invoiceFilterDto);
		if (!internalList.isEmpty()) {
			Map<String, Object> internalWidgetsData = getWidgetsData(internalList, invoiceFilterDto);
			widgetsData.put("internalInvoice", internalWidgetsData.get("internalInvoice"));
			widgetsData.put("internalInvoicePercentage", internalWidgetsData.get("internalInvoicePercentage"));
		}
		if (filteredList != null) {
			for (ProjectInvoice invoice : filteredList) {
				HashMap<String, Object> res = new HashMap<>();
				res = getData(res, invoice, allProjectDetails, userNameList, paymentTermsList,
						paymentModeList, invoiceStatusList, billingCycleList, buMapFinalData);
				if (invoice.getRaisedFromBu() != null) {
					Map<String, Object> buFromDetails = buList.stream()
							.filter(bu -> (bu.get("id").toString()).equals(invoice.getRaisedFromBu().toString()))
							.findFirst().orElse(null);
					if (buFromDetails != null)
						buFromName = buFromDetails.get("name").toString();
					Map<String, Object> buToDetails = buList.stream()
							.filter(bu -> (bu.get("id").toString()).equals(invoice.getRaisedToBu().toString()))
							.findFirst().orElse(null);
					if (buToDetails != null)
						buToName = buToDetails.get("name").toString();
				}
				res.put("buFromName", buFromName);
				res.put("buToName", buToName);
				result.add(res);
			}
		}
		if (!invoiceFilterDto.getAging().equals("")) {
			List<Map<String, Object>> agingList = new ArrayList<>();
			String aging = invoiceFilterDto.getAging();
			switch (aging) {
			case ConstantUtility.AVERAGE:
				agingList = this.getAgaingList(0, 10, result);
				break;
			case ConstantUtility.BAD:
				agingList = this.getAgaingList(10, 30, result);
				break;
			case ConstantUtility.POOR:
				agingList = this.getAgaingList(30, 60, result);
				break;
			case ConstantUtility.VERY_POOR:
				agingList = result.stream()
						.filter(invoices -> !(invoices).get(ConstantUtility.AGING).equals("N/A")
								&& Integer.parseInt((String) (invoices).get(ConstantUtility.AGING)) > 60)
						.collect(Collectors.toList());
				break;
			default:
				log.error("Aging don't match");
				break;
			}
			result = agingList;
		}
		response.put("invoiceList", result);
		response.put("totalCount", result.size());
		response.put("widgetList", widgetsData);
		response.put("currentUserRoles", currentUser.getRoles());
		return response;
	}
	
	private List<Map<String, Object>> getAgaingList(int  startDay, int  endDay, List<Map<String, Object>> result) {
		List<Map<String, Object>> agingList = new ArrayList<>();
		for (Map<String, Object> invoices : result) {
			if (!invoices.get(ConstantUtility.AGING).equals("N/A"))
				 if (Integer.parseInt((String) invoices.get(ConstantUtility.AGING)) <= endDay
						&& Integer.parseInt((String) invoices.get(ConstantUtility.AGING)) > startDay) {
					agingList.add(invoices);
				}
		}
		return agingList;
	}

	public boolean editProjectInvoice(ProjectInvoiceDto projectInvoiceDto, String authorization) {
		ProjectInvoice projectInvoice = projectInvoiceRepository.findById(projectInvoiceDto.getId());
		boolean result = false;
		if (projectInvoice != null) {

			if(projectInvoiceDto.getInvoiceStatusId()!=projectInvoice.getInvoiceStatus()) {
				Long currentStatus =projectInvoiceDto.getInvoiceStatusId();

				sendMailOnInvoiceStatusChange(authorization, projectInvoice,currentStatus);
			}
			if (projectInvoice.getFromDate() == null) {
				String operation = "edit";
				result = saveProjectInvoiceData(projectInvoice, projectInvoiceDto, authorization, operation);


			} else {
				result = saveInvoiceGeneratedData(projectInvoice, projectInvoiceDto);
			}
			if((projectInvoiceDto.getInvoiceStatusId()!=2  && projectInvoiceDto.getInvoiceStatusId()!=7)&& projectInvoice.getReceivedOn()!=null){
				projectInvoice.setReceivedOn(null);
			}
			projectInvoiceRepository.save(projectInvoice);
		} else
			result = false;
		return result;
	}
	
	public Map<String, Object> getClientEmails(String accessToken, Long projectId){
		Map<String, Object> result = new HashMap<>();
		if(Objects.nonNull(projectId)) {
			Map<String, Object> projectDetails = (Map<String, Object>) feignLegacyInterface.getProjectDescription(projectId,"",null,null).get("data");
			if(projectDetails!=null) {
				Set<String> ccMails = new HashSet<>();
				String toMail = projectDetails.get("clientEmail").toString();
				if(projectDetails.get("billingEmail")!=null && !projectDetails.get("billingEmail").toString().equals("NA") && !projectDetails.get("billingEmail").toString().equals("") && !projectDetails.get("billingEmail").toString().equals(toMail)) {
					ccMails.add(toMail);
					toMail = projectDetails.get("billingEmail").toString();
				}
				if(projectDetails.get("secondaryEmail") !=null && !projectDetails.get("secondaryEmail").toString().equals("NA") && !projectDetails.get("secondaryEmail").toString().equals("")) {
					ccMails.add(projectDetails.get("secondaryEmail").toString());
				}
				if( projectDetails.get("clientRepresentativeEmail") != null) {
					List<Object> list = (List<Object>) projectDetails.get("clientRepresentativeEmail");
					List<String> clientRepresentativeEmailList = list.stream().map(Object::toString).collect(Collectors.toList());
					ccMails.addAll(clientRepresentativeEmailList);
				}
				InvoiceProjectSettings projectSettings = projectSettingsRepo.findByProjectId(projectId);
				if(Objects.nonNull(projectSettings)) {
					if(projectSettings.getToMail()!=null && projectSettings.getCcMail()!=null) {
						ccMails.addAll(projectSettings.getCcMail());
					}
				}
				result.put("to", toMail);
				result.put("ccMails",ccMails);
				return result;
			}
		}
		return Collections.emptyMap();
	}
	
	public Map<String, Object> sendMailOnInvoicePaid(String accessToken, ThankYouMailDto thankYouMailDto) {
		Map<String,Object> data = new HashMap<>();
		ProjectInvoice projectInvoice = projectInvoiceRepository.findById(thankYouMailDto.getInvoiceId());
		if(thankYouMailDto.getAction().equals("skip") && thankYouMailDto.getSkipComment()!=null && projectInvoice != null){
			projectInvoice.setSkipComment(thankYouMailDto.getSkipComment());
			projectInvoiceRepository.save(projectInvoice);
			data.put("skipComment", thankYouMailDto.getSkipComment());
			data.put("status", false);
			return data;
		}
		if(projectInvoice != null && (thankYouMailDto.getAction().equals("preview")  || ( projectInvoice.getInvoiceStatus() == 2 && !projectInvoice.isThanksMail()))) {
			Map<String, Object> projectDetails = (Map<String, Object>) feignLegacyInterface.getProjectDescription(projectInvoice.getProjectId(),"",null,null).get("data");
			if(projectDetails!=null) {
				String billingName = projectDetails.get("billingName").toString();
				String buOwnerMail = projectDetails.get("buOwnerMail").toString();
				String clientEmail = thankYouMailDto.getToMail() !=null ? thankYouMailDto.getToMail() : projectDetails.get("clientEmail").toString();
				String ManagerEmail = projectDetails.get("managerEmail").toString();
				PaymentMode modeOfPayment = paymentModeRepository.findById(projectInvoice.getModeOfPaymentId());
				Context context = new Context();
				List<String> orgCcList = new ArrayList<String>(Arrays.asList(ManagerEmail, buOwnerMail));
				List<String> clientCcList = new ArrayList<>();
 				if(thankYouMailDto.getCcMail() != null && thankYouMailDto.getCcMail().size() > 0)
 					clientCcList.addAll(thankYouMailDto.getCcMail());
				SimpleDateFormat DateFor = new SimpleDateFormat("dd/MMM/yyyy");
				String subject = "Payment Confirmation" + " | "+projectInvoice.getProject()+ " | " + "Invoice Period "; subject = "Payment Confirmation" + " | "+projectInvoice.getProject()+ " | " + "Invoice Period ";
				context.setVariable("clientName", projectDetails.get("billingName").equals("NA") ? projectDetails.get("primaryContactName").equals("NA") ? projectInvoice.getClientName() : projectDetails.get("primaryContactName")  : projectDetails.get("billingName"));
				context.setVariable("project", projectInvoice.getProject());
				context.setVariable("invoiceId", projectInvoice.getId());
				context.setVariable("invoiceType", "Standard");
				context.setVariable("clientMail", false);
				if(projectInvoice.getInvoiceType()!=null) {
					subject = subject +  DateFor.format(projectInvoice.getFromDate())+ "-" + DateFor.format(projectInvoice.getToDate());
					context.setVariable("invoiceType", projectInvoice.getInvoiceType().getName());
					context.setVariable("toDate", DateFor.format(projectInvoice.getToDate()));
					context.setVariable("fromDate",DateFor.format(projectInvoice.getFromDate()));
				}
				context.setVariable("modeOfPayment",modeOfPayment.getPaymentModeType());
				context.setVariable("payingEntityName", projectInvoice.getPayingEntityName());
				String template =  "Invoice-Thank-You-Mail-With-Feedback.html";
				List<Map<String, Object>> clientQuickFeedback = (List<Map<String, Object>>) feignLegacyInterface.getClientQuickFeedbackStatus(accessToken, projectInvoice.getProjectId() , LocalDateTime.now().getMonth().toString(), LocalDateTime.now().getYear()).get("data");
				if(clientQuickFeedback!=null && !clientQuickFeedback.isEmpty() &&  clientQuickFeedback.get(0).containsKey("clientToken")) {
					String clientToken = clientQuickFeedback.get(0).get("clientToken").toString();
					String emailtrackUrl = ConstantUtility.HTTPS+ environmentUrl +"/zuul/dashboard_core/api/v1/feedback/emailTracker?token=" +clientToken;
					context.setVariable("lookup", emailtrackUrl);
					context.setVariable("ratingUrl", ConstantUtility.HTTPS+ environmentUrl +"/#/clientFeedbackForm?t="+clientToken + "&source=Accounts" +"&feedbackFormType=Quick Feedback" +"&rating=");
					context.setVariable("feedbackUrl", ConstantUtility.HTTPS + environmentUrl + "/#/feedbackc?t="
							+ clientToken + "&source=Accounts" +"&feedbackFormType=Detailed Feedback");
					context.setVariable("feedbackEnable", "YES");
					context.setVariable("lookupEnable", "YES");
				}
				if(thankYouMailDto.getAction().equals("preview")) {
					context.setVariable("lookupEnable", "NO");
					String process = templateEngine.process(template, context);
					data.put("subject", subject);
					data.put("htmlPreview", process.replaceAll("\\t", ""));
					data.put("status", false);
					return data;
				}
				else {
					if(thankYouMailDto.getAction().equals("send") && clientEmail!=null) {
						//** set client "to" and "ccMails"
						InvoiceProjectSettings projectSettings = projectSettingsRepo.findByProjectId(projectInvoice.getProjectId());
						if(Objects.nonNull(projectSettings)) {
							projectSettings.setToMail(thankYouMailDto.getToMail());
							projectSettings.setCcMail(thankYouMailDto.getCcMail());
							projectSettingsRepo.saveAndFlush(projectSettings);
						}
						
						/**
						 * Generate PDF and send mail with attachment 
						 */
						try {
							Map<String, Object> fileData = invoiceService.generatePDFFromHTML(accessToken, new InvoiceSlipDto(thankYouMailDto.getHtml(), thankYouMailDto.getInvoiceId(), thankYouMailDto.getIsIfsd()));
							clientCcList.add(accountsMail);
							mailService.sendScheduleHtmlMailWithCcAndAttachmentFile(clientEmail, subject, context, "Invoice-Thank-You-Mail-With-Feedback", clientCcList.stream().toArray(String[]::new), new FileSystemResource(Paths.get(fileData.get("fileName").toString()).getFileName()));
							projectInvoice.setThanksMail(true);
							projectInvoiceRepository.save(projectInvoice);
							data.put("status", true);
							return data;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			return data;
		}
		return data;
	}

	public Boolean sendMailOnInvoiceStatusChange(String accessToken, ProjectInvoice invoice,Long current ) {
		InvoiceStatus previousInvoiceStatus = invoiceStatusRepository.findById(invoice.getInvoiceStatus());
		InvoiceStatus currentInvoiceStatus = invoiceStatusRepository.findById(current);
		Boolean result = false;
		UserModel currentUser = validator.tokenbValidate(accessToken);
		Map<String, Object> projectDetails = (Map<String, Object>) feignLegacyInterface.getProjectDescription(invoice.getProjectId(),"",null,null).get("data");
		List<String> hierarchyEmails=new ArrayList<>();
		String managerEmail = "";
		if (projectDetails.get("managerName") != null) {
			managerEmail = projectDetails.get("managerEmail").toString();
			Long managerId=Long.parseLong(projectDetails.get("managerId").toString());
			 hierarchyEmails=(List<String>) feignLegacyInterface.getManagerHierarchyEmail(accessToken,managerId).get("data");
		}
		if(currentInvoiceStatus.getId() == 5){
			hierarchyEmails.add(legalMail);
			log.info(" added legal mail " + hierarchyEmails.toString());
		}
		Context context = new Context();
		context.setVariable("managerName", projectDetails.get("managerName"));
		context.setVariable("invoiceId", invoice.getId());
		context.setVariable("projectName", invoice.getProject());
		context.setVariable("currentInvoiceStatus", currentInvoiceStatus.getStatusName());
		context.setVariable("previousInvoiceStatus", previousInvoiceStatus.getStatusName());
		context.setVariable("currentDate", LocalDate.now().format(DateTimeFormatter.ofPattern(ConstantUtility.DATE_FORMAT)));
		context.setVariable("currentUserName", currentUser.getEmpName());
		context.setVariable("amtInInr", new DecimalFormat("0.00").format(invoice.getAmountInRupee()));
		context.setVariable("amount", invoice.getAmount());
		context.setVariable("currency", invoice.getCurrency());
		context.setVariable("clientName", invoice.getClientName());
		context.setVariable("currentYear", LocalDate.now().getYear());
		context.setVariable("createdDate", new SimpleDateFormat("dd-MM-yyyy").format(invoice.getCreatedDate()));
		String subject = "Invoice INV-" + invoice.getId() + " || " + invoice.getProject() + " || " + currentInvoiceStatus.getStatusName();	
		if(!managerEmail.equals("")) {
			mailService.sendScheduleHtmlMailWithCc(managerEmail, subject, context, "Invoice-Status-Changed",hierarchyEmails.stream().toArray(String[]::new));
		result=true;
		}
		return result;
	}
	public boolean deleteProjectInvoice(Long id, String authorization) {
		ProjectInvoice projectInvoice = projectInvoiceRepository.findById(id);
		if (projectInvoice == null) {
			return false;
		} else {
			projectInvoice.setDeleted(true);
			projectInvoiceRepository.save(projectInvoice);
			return true;
		}
	}

	public boolean saveProjectInvoiceData(ProjectInvoice projectInvoice, ProjectInvoiceDto projectInvoiceDto,
			String accessToken, String operation) {
		BankLocation location = locationRepo.findByIdAndIsDeleted(projectInvoiceDto.getBankLocationId(),false);
		if(location!=null)
			projectInvoice.setBankLocation(location);
		UserModel currentUser = validator.tokenbValidate(accessToken);
		projectInvoice = setInvoiceInfo(projectInvoiceDto, projectInvoice);
		projectInvoice = setInvoiceAmountData(projectInvoiceDto, projectInvoice);
		projectInvoice = setInvoiceSources(projectInvoiceDto, projectInvoice);
		projectInvoice.setCreatorId(currentUser.getUserId());
		projectInvoice.setMonth(projectInvoiceDto.getMonth());
		projectInvoice.setYear(projectInvoiceDto.getYear());
		projectInvoice.setComment(projectInvoiceDto.getComment());
		projectInvoice.setReceivedOn(projectInvoiceDto.getReceivedOn());

		InvoiceStatus invoiceStatus = invoiceStatusRepository.findById(projectInvoiceDto.getInvoiceStatusId());
		if(invoiceStatus.getId() == 2) {
			projectInvoice.setPayingEntityName(projectInvoiceDto.getPayingEntityName());
			projectInvoice.setCurrencyRecevied(projectInvoiceDto.getCurrencyRecevied());
			projectInvoice.setRecievedAmount(projectInvoiceDto.getRecievedAmount());	
		}
		projectInvoice.setInvoiceStatus(invoiceStatus.getId());
		PaymentMode paymentMode = paymentModeRepository.findById(projectInvoiceDto.getModeOfPaymentId());
		projectInvoice.setModeOfPaymentId(paymentMode.getId());
		InvoiceCycle billingCycle = invoiceCycleRepository.findById(projectInvoiceDto.getInvoiceCycleId());
		projectInvoice.setInvoiceCycleId(billingCycle.getId());
		PaymentTerms paymentTerms = paymentTermsRepository.findById(projectInvoiceDto.getPaymentTermsId());
		projectInvoice.setPaymentTermsId(paymentTerms.getId());
		if (operation.equals("add")) {
			projectInvoice.setCreatedDate(new Date());
		}
		if (projectInvoiceDto.getInvoiceStatusId() == 5) {
			projectInvoice.setDisputedDate(java.time.LocalDateTime.now());
		}
		try {
			projectInvoiceRepository.save(projectInvoice);
			if (!projectInvoiceDto.getIsInternal())
				feignLegacyInterface.addProjectPaymentSettings(accessToken,projectInvoiceDto.getProjectId(), paymentMode.getId(),
					paymentTerms.getId(), billingCycle.getId());
		} catch (Exception e) {
			logger.info(e.getMessage());
			return false;
		}

		return true;

	}

	private ProjectInvoice setInvoiceAmountData(ProjectInvoiceDto projectInvoiceDto, ProjectInvoice projectInvoice) {
		int monthnum = 0;
		if(projectInvoiceDto.getMonth()!=null && !projectInvoiceDto.getMonth().equals(""))
			monthnum=loginUtilityService.getMonthNumber(projectInvoiceDto.getMonth());
		else
			monthnum = LocalDateTime.now().minusMonths(1).getMonthValue();
		int year=0;
		if(!projectInvoiceDto.getYear().equals(""))
			year=Integer.parseInt(projectInvoiceDto.getYear());
		else 
			year= LocalDateTime.now().minusMonths(1).getYear();
		Double dollarexchangeCost = dollarCostService.getAverageDollarCost(monthnum,year);
		
		if(projectInvoiceDto.getSecurityDepositeId()!=null) {
			Optional<SecurityDeposit> securityDep=securityDepositRepository.findById(projectInvoiceDto.getSecurityDepositeId());
			if(securityDep.isPresent()) {
				SecurityDeposit security = securityDep.get();
				security.setAmount(security.getAmount()-projectInvoiceDto.getAdjustmentAmount());
				security.setAmountInDollar(security.getAmountInDollar()-projectInvoiceDto.getAdjustmentAmount());
				securityDepositRepository.save(security);
				projectInvoice.setSecurityDepositeId(projectInvoiceDto.getSecurityDepositeId());
				projectInvoice.setAdjustmentDate(DateUtils.truncate(new Date(), Calendar.DATE));
			}
		}
		if(projectInvoice.getInvoiceStatus() != null && projectInvoice.getInvoiceStatus().equals(7L) && projectInvoiceDto.getInvoiceStatusId()==2){
			projectInvoiceDto.setAmount(projectInvoiceDto.getAmount() + projectInvoiceDto.getWaivedOffAmount());
			if(projectInvoiceDto.getCurrency().equalsIgnoreCase("RUPEE"))
				projectInvoiceDto.setAmountInDollar(projectInvoiceDto.getAmountInDollar() + projectInvoiceDto.getWaivedOffAmount());
			projectInvoiceDto.setWaivedOffAmount(0D);
		}
		projectInvoice.setWaivedOffAmount(projectInvoiceDto.getWaivedOffAmount());
		projectInvoice.setAdjustmantAmount(projectInvoiceDto.getAdjustmentAmount());
		projectInvoice.setAmount(projectInvoiceDto.getAmount()+projectInvoiceDto.getAdjustmentAmount()-(projectInvoiceDto.getWaivedOffAmount()!= null ? projectInvoiceDto.getWaivedOffAmount() : 0));
		

		if (projectInvoiceDto.getCurrency().equals("DOLLAR")) {
			projectInvoice.setAmountInDollar(projectInvoiceDto.getAmount()+projectInvoiceDto.getAdjustmentAmount()-(projectInvoiceDto.getWaivedOffAmount()!= null ? projectInvoiceDto.getWaivedOffAmount() : 0));
			projectInvoice.setAmountInRupee(dollarexchangeCost * (projectInvoiceDto.getAmount()+projectInvoiceDto.getAdjustmentAmount()-(projectInvoiceDto.getWaivedOffAmount()!= null ? projectInvoiceDto.getWaivedOffAmount() : 0)));
			projectInvoice.setTaxableAmountInDollar(projectInvoice.getAmountInDollar()+ projectInvoice.getCgst()+ projectInvoice.getIgst()+ projectInvoice.getSgst()+projectInvoiceDto.getAdjustmentAmount());
			projectInvoice.setExchangeRate(1);
		} else {
			projectInvoice.setAmountInDollar(projectInvoiceDto.getAmountInDollar()+projectInvoice.getAdjustmantAmount()-(projectInvoiceDto.getWaivedOffAmount()!= null ? projectInvoiceDto.getWaivedOffAmount() : 0));
			projectInvoice.setTaxableAmountInDollar(projectInvoiceDto.getExchangeRate()*(projectInvoiceDto.getAmount()+ projectInvoice.getCgst()+ projectInvoice.getIgst()+ projectInvoice.getSgst()+projectInvoiceDto.getAdjustmentAmount()-(projectInvoiceDto.getWaivedOffAmount()!= null ? projectInvoiceDto.getWaivedOffAmount() : 0)));
			projectInvoice.setExchangeRate(projectInvoiceDto.getExchangeRate());
		}
		if (projectInvoiceDto.getCurrency().equalsIgnoreCase("RUPEE")) {
			projectInvoice.setAmountInRupee(projectInvoiceDto.getAmount()+projectInvoiceDto.getAdjustmentAmount()-(projectInvoiceDto.getWaivedOffAmount()!= null ? projectInvoiceDto.getWaivedOffAmount() : 0));
		} else if (!projectInvoiceDto.getCurrency().equals("DOLLAR")) {
			projectInvoice.setAmountInRupee(65 * (projectInvoiceDto.getAmountInDollar()+projectInvoiceDto.getAdjustmentAmount()-(projectInvoiceDto.getWaivedOffAmount()!= null ? projectInvoiceDto.getWaivedOffAmount() : 0)));
		}
		
		return projectInvoice;
	}

	public ProjectInvoice setInvoiceInfo(ProjectInvoiceDto projectInvoiceDto, ProjectInvoice projectInvoice) {
		projectInvoice.setDueDate(projectInvoiceDto.getDueDate());
		projectInvoice.setCurrency(projectInvoiceDto.getCurrency());
		projectInvoice.setTdsValue(projectInvoiceDto.getTdsValue());
		projectInvoice.setBillingDate(projectInvoiceDto.getBillingDate());
		projectInvoice.setReceivedOn(projectInvoiceDto.getReceivedOn());
		projectInvoice.setPaymentCharges(projectInvoiceDto.getPaymentCharges());
		projectInvoice.setImportItems(projectInvoiceDto.getImportItems());
		if(projectInvoiceDto.getPlaceOfSupply()!=null)
		projectInvoice.setPlaceOfSupply(projectInvoiceDto.getPlaceOfSupply());
		if(projectInvoiceDto.getCityOfSupply()!=null)
		projectInvoice.setCityOfSupply(projectInvoiceDto.getCityOfSupply());
		if(projectInvoiceDto.getInvoiceStatusId()==2)
			projectInvoice.setPayStatus(PayStatus.PAID);
		else
			projectInvoice.setPayStatus(PayStatus.UNPAID);
		return projectInvoice;
	}

	public ProjectInvoice setInvoiceSources(ProjectInvoiceDto projectInvoiceDto, ProjectInvoice projectInvoice) {
		if (!projectInvoiceDto.getIsInternal()) {
			projectInvoice.setProjectId(projectInvoiceDto.getProjectId());
			projectInvoice.setProject(projectInvoiceDto.getProject());
			projectInvoice.setManagerId(projectInvoiceDto.getManagerId());
			projectInvoice.setClientName(projectInvoiceDto.getClientName());
			projectInvoice.setManager(projectInvoiceDto.getManager());
		} else {
			projectInvoice.setProjectId(projectInvoiceDto.getProjectId());
			projectInvoice.setProject(projectInvoiceDto.getProject());
			projectInvoice.setRaisedFromBu(projectInvoiceDto.getRaisedFromBu());
			Map<String,Object> projectDetails=(Map<String, Object>) feignLegacyInterface.getProjectDescription(projectInvoiceDto.getProjectId(),"",null,null).get("data");
			Long projectBusinessVerticalId = new Long (projectDetails.get("businessVerticalId").toString());
			projectInvoice.setRaisedToBu(projectBusinessVerticalId);
		}
		projectInvoice.setIsInternal(projectInvoiceDto.getIsInternal());

		if (projectInvoiceDto.getBankId() != null) {
			Optional<InvoiceBank> bank = bankRepository.findById(projectInvoiceDto.getBankId());
			if (bank.isPresent())
				projectInvoice.setBank(bank.get());
		}
		return projectInvoice;
	}

	public String addPaymentMode(PaymentModeDto paymentModeDto, String authorization) {

		PaymentMode paymentModeexist = paymentModeRepository
				.findByPaymentModeTypeIgnoreCase(paymentModeDto.getPaymentModeType());
		if (paymentModeexist != null) {
			return ConstantUtility.ALREADY_EXISTS;
		}
		PaymentMode paymentMode = new PaymentMode();
		paymentMode.setPaymentModeType(paymentModeDto.getPaymentModeType());
		try {
			paymentModeRepository.save(paymentMode);
		} catch (Exception e) {
			logger.info(e.getMessage());
			return ConstantUtility.FAILURE;
		}
		return ConstantUtility.SUCCESS;
	}

	public boolean deletePaymentMode(Long id, String authorization) {
		boolean result = false;
		PaymentMode paymentMode = paymentModeRepository.findById(id);
		List<ProjectInvoice> projectInvoices = projectInvoiceRepository
				.findByIsDeletedAndModeOfPaymentIdAndIsInternal(false, paymentMode.getId(), false);
		if (projectInvoices.isEmpty()) {
			paymentMode.setArchived(true);
			paymentModeRepository.save(paymentMode);
			result = true;
		}
		return result;
	}

	public String addInvoiceCycle(InvoiceCycleDto invoiceCycleDto, String authorization) {
		InvoiceCycle invoiceCycleExist = invoiceCycleRepository
				.findByInvoiceCycleTypeIgnoreCase(invoiceCycleDto.getInvoiceCycleType());
		if (invoiceCycleExist != null) {
			return ConstantUtility.ALREADY_EXISTS;
		}
		InvoiceCycle invoiceCycle = new InvoiceCycle();
		invoiceCycle.setInvoiceCycleType(invoiceCycleDto.getInvoiceCycleType());
		try {
			invoiceCycleRepository.save(invoiceCycle);
		} catch (Exception e) {
			logger.info(e.getMessage());
			return ConstantUtility.FAILURE;
		}

		return ConstantUtility.SUCCESS;
	}

	public boolean deleteInvoiceCycle(Long id, String authorization) {
		boolean result = false;
		InvoiceCycle invoiceCycle = invoiceCycleRepository.findById(id);
		List<ProjectInvoice> invoices = projectInvoiceRepository.findByIsDeletedAndInvoiceCycleIdAndIsInternal(false,
				invoiceCycle.getId(), false);
		if (invoices.isEmpty()) {
			invoiceCycle.setArchived(true);
			invoiceCycleRepository.save(invoiceCycle);
			result = true;
		}
		return result;
	}

	public boolean editInvoiceCycle(InvoiceCycleDto invoiceCycleDto, String authorization) {

		InvoiceCycle invoiceCycle = invoiceCycleRepository.findById(invoiceCycleDto.getId());
		invoiceCycle.setInvoiceCycleType(invoiceCycleDto.getInvoiceCycleType());
		try {
			invoiceCycleRepository.save(invoiceCycle);
		} catch (Exception e) {
			logger.info(e.getMessage());
			return false;
		}
		return true;

	}

	public boolean editPaymentMode(PaymentModeDto paymentModeDto, String authorization) {
		PaymentMode paymentMode = paymentModeRepository.findById(paymentModeDto.getId());
		paymentMode.setPaymentModeType(paymentModeDto.getPaymentModeType());
		try {
			paymentModeRepository.save(paymentMode);
		} catch (Exception e) {
			logger.info(e.getMessage());
			return false;
		}

		return true;

	}

	public List<Object> getAllPaymentMode(String authorization) {
		List<PaymentMode> data = paymentModeRepository.findByIsArchived(false);
		List<Object> result = new ArrayList<>();
		if (data != null) {
			for (PaymentMode paymentMode : data) {
				HashMap<String, Object> res = new HashMap<>();
				res.put("id", paymentMode.getId());
				res.put(ConstantUtility.PAYMENT_MODE_TYPE, paymentMode.getPaymentModeType());
				result.add(res);
			}
		}
		return result;
	}

	public List<Object> getAllInvoiceCycle(String authorization) {
		List<InvoiceCycle> data = invoiceCycleRepository.findByIsArchived(false);
		List<Object> result = new ArrayList<>();
		if (data != null) {
			for (InvoiceCycle invoiceCycle : data) {
				HashMap<String, Object> res = new HashMap<>();
				res.put("id", invoiceCycle.getId());
				res.put(ConstantUtility.PAYMENT_MODE_TYPE, invoiceCycle.getInvoiceCycleType());
				result.add(res);
			}
		}
		return result;
	}

	public List<Object> getAllInvoiceStatus(String authorization) {
		List<InvoiceStatus> invoiceStatusList = invoiceStatusRepository.findAll();
		List<Object> result = new ArrayList<>();

		if (invoiceStatusList != null) {
			for (InvoiceStatus status : invoiceStatusList) {
				HashMap<String, Object> res = new HashMap<>();
				res.put("id", status.getId());
				res.put("statusName", status.getStatusName());
				result.add(res);
			}
		}
		return result;
	}

	public String addPaymentTerms(PaymentTermsDto paymentTermsDto, String authorization) {
		PaymentTerms paymentTermsexist = paymentTermsRepository
				.findByPaymentTermsTypeIgnoreCase(paymentTermsDto.getPaymentTermsType());
		if (paymentTermsexist != null) {
			return ConstantUtility.ALREADY_EXISTS;
		}

		PaymentTerms paymentTerms = new PaymentTerms();
		paymentTerms.setPaymentTermsType(paymentTermsDto.getPaymentTermsType());
		try {
			paymentTermsRepository.save(paymentTerms);
		} catch (Exception e) {
			logger.info(e.getMessage());
			return ConstantUtility.FAILURE;
		}

		return ConstantUtility.SUCCESS;
	}

	public boolean deletePaymentTerms(Long id, String authorization) {
		boolean result = false;
		PaymentTerms paymentTerms = paymentTermsRepository.findById(id);
		List<ProjectInvoice> invoices = projectInvoiceRepository.findByIsDeletedAndPaymentTermsIdAndIsInternal(false,
				paymentTerms.getId(), false);
		if (invoices.isEmpty()) {
			paymentTerms.setArchived(true);
			paymentTermsRepository.save(paymentTerms);
			result = true;
		}
		return result;
	}

	public boolean editPaymentTerms(PaymentTermsDto paymentTermsDto, String authorization) {

		PaymentTerms paymentTerms = paymentTermsRepository.findById(paymentTermsDto.getId());
		paymentTerms.setPaymentTermsType(paymentTermsDto.getPaymentTermsType());
		try {
			paymentTermsRepository.save(paymentTerms);
		} catch (Exception e) {
			logger.info(e.getMessage());
			return false;
		}
		return true;

	}

	public List<Object> getAllPaymentTerms(String authorization) {
		List<PaymentTerms> data = paymentTermsRepository.findByIsArchived(false);
		List<Object> result = new ArrayList<>();
		if (data != null) {
			for (PaymentTerms paymentTerms : data) {
				HashMap<String, Object> res = new HashMap<>();
				res.put("id", paymentTerms.getId());
				res.put(ConstantUtility.PAYMENT_MODE_TYPE, paymentTerms.getPaymentTermsType());
				result.add(res);
			}
		}
		return result;
	}

	public List<Object> getAllCurrencies(String authorization) {
		List<Currency> currencyList = currencyRepository.findAll();
		List<Object> result = new ArrayList<>();
		if (currencyList != null) {
			for (Currency currency : currencyList) {
				result.add(currency.getCurrencyName());
			}
		}

		return result;
	}

	public Map<String, Object> getWidgetsData(List<ProjectInvoice> projectInvoice,InvoiceFilterDto invoiceFilterDto) {
		HashMap<String, Object> res = new HashMap<>();
		double paidTotal = 0;
		double inTransitTotal = 0;
		double pendingTotal = 0;
		double withinDueDateTotal = 0;
		double disputedTotal = 0;
		double totalAmount = 0;
		double internalInvoice = 0;
		double refundedTotal = 0;
		//Double dollarCost=1D;
		int monthnum=0;
		Double waivedOffAmountTotal=0D;
		double paymentCharges=0D;
		double paymentChargesRefunded = 0D;

		
		List<Map<String,Object>> avgDollarCost = new ArrayList<>();
		Double avgCost = 1D;
		Double dollarCostInvoice =1D;
		if(invoiceFilterDto.getCurrencyType()!=null && invoiceFilterDto.getCurrencyType().equals("Rupee")) {
			if(invoiceFilterDto.getFromDate() != null && invoiceFilterDto.getToDate() != null){
				avgDollarCost = dollarCostService.getMonthWiseAverageDollarCost(new Date(invoiceFilterDto.getFromDate()), new Date(invoiceFilterDto.getToDate()));
				for(Map<String,Object> map : avgDollarCost){
					if(map.containsKey("AverageCost"))
						avgCost=Double.parseDouble(map.get("AverageCost").toString());
					else
						continue;
				}

			}
			else{
				if(invoiceFilterDto.getMonth()!=null && !invoiceFilterDto.getMonth().equals(""))
				monthnum=loginUtilityService.getMonthNumber(invoiceFilterDto.getMonth());
			else
				monthnum = LocalDateTime.now().minusMonths(1).getMonthValue();
			

				dollarCostInvoice= dollarCostService.getAverageDollarCost(monthnum,Integer.parseInt(invoiceFilterDto.getYear()) );

			}
		}
		for (ProjectInvoice invoice : projectInvoice) {
			int monthNum = 0;
			
			

			Double invoiceAmount=invoice.getAmountInDollar()*dollarCostInvoice;
			totalAmount = totalAmount + invoiceAmount;
			paymentCharges = paymentCharges + (invoice.getPaymentCharges()!= null ?invoice.getPaymentCharges()*dollarCostInvoice : 0.0) ;
			InvoiceStatus invoiceStatus = invoiceStatusRepository.findById(invoice.getInvoiceStatus());
			if (invoiceStatus.getStatusName().equals("Paid")) {
				paidTotal = paidTotal + invoiceAmount;
			}
			if (invoiceStatus.getStatusName().equals("Pending")) {
				pendingTotal = pendingTotal + invoiceAmount;
			}
			if (invoiceStatus.getStatusName().equals("In Transit")) {
				inTransitTotal = inTransitTotal + invoiceAmount;
			}
			if (invoiceStatus.getStatusName().equals("Within Due Date")) {
				withinDueDateTotal = withinDueDateTotal + invoiceAmount;
			}
			if (invoiceStatus.getStatusName().equals("Disputed")) {
				disputedTotal = disputedTotal + invoiceAmount;
			}
			if (invoice.getIsInternal() != null && invoice.getIsInternal())
				internalInvoice = internalInvoice + invoiceAmount;
			if(invoiceStatus.getStatusName().equals("Refunded")) {
				refundedTotal=refundedTotal+invoiceAmount;
				paymentChargesRefunded = paymentChargesRefunded + (invoice.getPaymentCharges()!= null ?invoice.getPaymentCharges()*dollarCostInvoice : 0.0);
			}
			if(invoiceStatus.getStatusName().equals("Partially Waived-Off")) {
				waivedOffAmountTotal=waivedOffAmountTotal+ invoiceAmount;
			}

		}
		DecimalFormat df = new DecimalFormat("#.##");
		res.put("totalAmount", (totalAmount-refundedTotal)*avgCost);
		res.put("paidTotal", paidTotal*avgCost);
		res.put("inTransitTotal", inTransitTotal*avgCost);
		res.put("pendingTotal", pendingTotal*avgCost);
		res.put("withinDueDateTotal", withinDueDateTotal*avgCost);
		res.put("disputedTotal", disputedTotal*avgCost);
		res.put("internalInvoice", internalInvoice*avgCost);
		res.put("refundedTotal", refundedTotal*avgCost);
		res.put("withinDueDatePercentage", df.format(0));
		res.put("paidPercentage", df.format(0));
		res.put("intransitPercentage", df.format(0));
		res.put("pendingPercentage", df.format(0));
		res.put("disputedTotalPercentage", df.format(0));
		res.put("internalInvoicePercentage", df.format(0));
		res.put("refundedTotalPercentage",df.format(0));
		res.put("waivedOffAmountTotal", waivedOffAmountTotal*avgCost);
		res.put("waivedOffAmountPercentage",df.format(0));

		res.put("paymentChargesTotal", (paymentCharges - paymentChargesRefunded)*avgCost);
		res.put("AverageDollarCost", avgCost);


		if (totalAmount != 0) {
			res.put("withinDueDatePercentage", df.format(withinDueDateTotal / totalAmount * 100));
			res.put("paidPercentage", df.format(paidTotal / totalAmount * 100));
			res.put("intransitPercentage", df.format(inTransitTotal / totalAmount * 100));
			res.put("pendingPercentage", df.format(pendingTotal / totalAmount * 100));
			res.put("disputedTotalPercentage", df.format(disputedTotal / totalAmount * 100));
			res.put("internalInvoicePercentage", df.format(internalInvoice / totalAmount * 100));
			res.put("refundedTotalPercentage", df.format(refundedTotal / totalAmount * 100));
			res.put("waivedOffAmountPercentage", df.format(waivedOffAmountTotal / totalAmount * 100));

		}
		return res;

	}

	public String calculateAging(ProjectInvoice projectInvoice, List<InvoiceStatus>invoiceStatusList) {
		InvoiceStatus invoiceStatus = invoiceStatusList.stream().filter(status->status.getId()==projectInvoice.getInvoiceStatus()).findAny().orElse(null);
		Date date = new Date();
		if (invoiceStatus.getStatusName().equals("Pending")) {
			if (date.compareTo(projectInvoice.getDueDate()) <= 0) {
				return "0";
			} else {
				long difference = date.getTime() - projectInvoice.getDueDate().getTime();
				float daysBetween = (difference / (1000 * 60 * 60 * 24));
				return String.valueOf((int) daysBetween);
				
			}

		} else {
			return "N/A";
		}

	}

	@Cacheable("projectDetailsCache")
	public List<Map<String,Object>> getProjectDetails(String accessToken) {
		List<Map<String, Object>> result=(List<Map<String,Object>>)feignLegacyInterface.getProjectDetails().get("data");
		return result;
				
	}

	public Map<String, Object> getProjectDetail(List<Map<String,Object>> allProjectDetails, long projectId) {
		for (Object project : allProjectDetails) {
			Map<String, Object> currentProjectInIteration = (Map<String, Object>) project;
			if (new Long(projectId)
					.compareTo(Long.parseLong(currentProjectInIteration.get("id").toString())) == 0) {
				return currentProjectInIteration;
			}
		}

		return null;
	}

	public boolean setAmountInRupee(String accessToken) {
		List<ProjectInvoice> data = projectInvoiceRepository.findAll();
		for (ProjectInvoice invoice : data) {
			if (invoice.getCurrency().equalsIgnoreCase("RUPEE")) {
				invoice.setAmountInRupee(invoice.getAmount());
				projectInvoiceRepository.save(invoice);
			} else {
				invoice.setAmountInRupee(65 * invoice.getAmountInDollar());
				projectInvoiceRepository.save(invoice);
			}
		}
		return true;
	}

	public Map<String, Object> getProjectWiseData(String authorization, String month, String year, Long id,
			List<Map<String, Object>> allProjectDetails) {
		List<ProjectInvoice> data;
		if (month.equals("")) {
			data = projectInvoiceRepository.findAllByProjectIdAndYearAndIsDeleted(id, year, false);
		} else {
			data = projectInvoiceRepository.findByProjectIdAndMonthAndYearAndIsDeleted(id, month, year,
					false);
		}
		List<Long> createdIdList = new ArrayList<>();
		data.forEach(invoice -> {
			createdIdList.add(invoice.getCreatorId());
		});
		UserIdsDto uids = new UserIdsDto();
		uids.setUserIds(createdIdList);
		
		List<Map<String, Object>> userNameList=(List<Map<String, Object>>) feignLegacyInterface.getUserNameList(authorization, uids).get("data");
		List<InvoiceCycle> billingCycleList = invoiceCycleRepository.findAll();
		List<InvoiceStatus> invoiceStatusList = invoiceStatusRepository.findAll();
		List<PaymentMode> paymentModeList = paymentModeRepository.findAll();
		List<PaymentTerms> paymentTermsList = paymentTermsRepository.findAll();
		
		List<Object> result = new ArrayList<>();
		Map<String, Object> response = new HashMap<>();
		double totalInvoiceAmountInDollar = 0;
		double totalInvoiceAmount = 0;
		if (data != null) {
			for (ProjectInvoice invoice : data) {
				totalInvoiceAmountInDollar = totalInvoiceAmountInDollar + invoice.getAmountInDollar();
				totalInvoiceAmount = totalInvoiceAmount + invoice.getAmount();
				HashMap<String, Object> res = new HashMap<>();
				res = getData(res, invoice, allProjectDetails,userNameList,paymentTermsList,paymentModeList,invoiceStatusList,billingCycleList,null);
				result.add(res);
			}
		}
		response.put("invoiceList", result);
		response.put("totalInvoiceAmountInDollar", totalInvoiceAmountInDollar);
		response.put("totalInvoiceAmount", totalInvoiceAmount);

		return response;
	}

	public HashMap<String, Object> getData(HashMap<String, Object> res, ProjectInvoice invoice,
			List<Map<String,Object>> allProjectDetails,List<Map<String, Object>> userNameList,List<PaymentTerms>paymentTermsList,List<PaymentMode>paymentModeList,List<InvoiceStatus>invoiceStatusList,List<InvoiceCycle>billingCycleList, Map<String, Object> buMapFinalData) {
		res.put("id", invoice.getId());
		res.put("invoiceNumber", "INV-" + invoice.getId());
		res.put("invoiceGenerationStatus", invoice.getInvoiceGenerationStatus());
		res.put("project", invoice.getProject());
		res.put("tdsValue", invoice.getTdsValue());
		res.put("buHeadComment", invoice.getBuHeadComment());
		res.put("isIfsd", false);
		res.put("placeOfSupply", invoice.getPlaceOfSupply());
		res.put("adjustmentAmount", invoice.getAdjustmantAmount() != null ? invoice.getAdjustmantAmount() : 0.0);
		res.put("securityDepositeId", invoice.getSecurityDepositeId() != null ? invoice.getSecurityDepositeId() : "NA");
		res.put("cityOfSupply", invoice.getCityOfSupply() != null ? invoice.getCityOfSupply() : "NA");
		String projectStatus = "";
		String projectBusinessVertical = "UnAssigned";

		Map<String, Object> projectDetails = this.getProjectDetail(allProjectDetails, invoice.getProjectId());
		if (projectDetails != null) {
			projectBusinessVertical = (String) projectDetails.get("businessVertical");
			projectStatus = (String) projectDetails.get("status");
		}
		if (buMapFinalData != null && buMapFinalData.containsKey(invoice.getProjectId().toString())) {
			projectBusinessVertical = buMapFinalData.get(invoice.getProjectId().toString()).toString();
		}

		res.put("projectId",
				projectDetails != null ? projectDetails.get("id") != null ? projectDetails.get("id") : "NA" : "NA");
		if (invoice.getBank() != null) {
			res.put("bankName", invoice.getBank().getName());
			res.put("bankId", invoice.getBank().getId());
		} else {
			res.put("bankName", null);
			res.put("bankId", null);
		}
		res.put("bankLocation", invoice.getBankLocation()!=null ?  invoice.getBankLocation() : null);
		res.put("paymentChargesInDollar", 0);
		res.put("paymentChargesInInr", 0);
		res.put("paymentCharges", 0);

		res.put("amountInclPaymentCharges", invoice.getAmountInDollar());
		if (invoice.getPaymentCharges() != null) {
			res.put("paymentChargesInDollar", getPaymentCharges("DOLLAR", invoice));
			res.put("paymentChargesInInr", getPaymentCharges("RUPEE", invoice));
			res.put("paymentCharges", invoice.getPaymentCharges());
			double amountInclPaymentCharge = invoice.getAmountInDollar()
					- (invoice.getPaymentCharges() * invoice.getExchangeRate());
			res.put("amountInclPaymentCharges", amountInclPaymentCharge);
		}
		res.put("invoiceType", invoice.getInvoiceType() != null ? invoice.getInvoiceType().getName() : null);
		res.put("projectStatus", projectStatus);
		res.put("importItems",invoice.getImportItems() != null ? invoice.getImportItems() : false);
		res.put("businessVerical", projectBusinessVertical);
		res.put("amountInDollar", invoice.getAmountInDollar());
		res.put("taxableInvoiceAmount",(Double) invoice.getTaxableAmountInDollar() != null ?invoice.getTaxableAmountInDollar() : null);
		res.put("currency", invoice.getCurrency());
		res.put("exchangeRate", invoice.getExchangeRate());
		res.put("month", invoice.getMonth());
		res.put("year", invoice.getYear());
		res.put("manager", invoice.getManager());
		res.put("managerId", invoice.getManagerId());	
		res.put("clientName", invoice.getClientName());
		res.put("amount", invoice.getAmount());
		res.put("buFrom", invoice.getRaisedFromBu());
		res.put("buTo", invoice.getRaisedToBu());
		res.put("isKycComplaint", invoice.getIsKycComplaint()!=null?invoice.getIsKycComplaint():null);
		res.put("cgst", invoice.getCgst());
		res.put("sgst", invoice.getSgst());
		res.put("igst", invoice.getIgst());
		res.put("isInternal", invoice.getIsInternal());
		res.put("splitType", invoice.getSplitType() != null ? invoice.getSplitType() : "NA");
		res.put("isMilestone", invoice.getIsMilestone() != null ? invoice.getIsMilestone() : false);
		res.put("from", invoice.getFromDate() != null ? invoice.getFromDate().getTime() : 0);
		res.put("to", invoice.getToDate() != null ? invoice.getToDate().getTime() : 0);
		InvoiceStatus invoiceStatus = invoiceStatusList.stream()
				.filter(inv -> inv.getId().toString().equals(invoice.getInvoiceStatus().toString())).findFirst()
				.orElse(null);
		HashMap<String, Object> invoiceStatusRes = new HashMap<>();
		invoiceStatusRes.put("invoiceStatusId", invoiceStatus.getId());
		invoiceStatusRes.put("invoiceStatusName", invoiceStatus.getStatusName());
		//if(invoiceStatus.getId() == 7){
			res.put("waivedOffAmount", invoice.getWaivedOffAmount());
			
		//}
		res.put("receivedOn", invoice.getReceivedOn());
		res.put("invoiceStatus", invoiceStatusRes);
		
		res.put("payingEntityName",invoice.getInvoiceStatus()==2 ? invoice.getPayingEntityName() != null ? invoice.getPayingEntityName() : "NA" : "NA" );
		res.put("currencyRecevied", invoice.getInvoiceStatus()==2 ? invoice.getCurrencyRecevied() !=null ? invoice.getCurrencyRecevied() : "NA" : "NA");
		res.put("recievedAmount", invoice.getInvoiceStatus()==2 ? invoice.getRecievedAmount()!=null ? invoice.getRecievedAmount() : 0D : 0D	);
		res.put("isThanksMail", invoice.isThanksMail());
		res.put("skipComment", invoice.getSkipComment()!=null ? invoice.getSkipComment() : "NA" );
		res.put("billingDate",
				new SimpleDateFormat(ConstantUtility.DATE_FORMAT).format(new Date(invoice.getBillingDate().getTime())));
		res.put("dueDate",
				new SimpleDateFormat(ConstantUtility.DATE_FORMAT).format(new Date(invoice.getDueDate().getTime())));

		InvoiceCycle billingCycle = billingCycleList.stream()
				.filter(billing -> billing.getId().toString().equals(invoice.getInvoiceCycleId().toString()))
				.findFirst().orElse(null);
		HashMap<String, Object> billingCycleRes = new HashMap<>();
		billingCycleRes.put("billingCycleId", billingCycle.getId());
		billingCycleRes.put("billingCycleName", billingCycle.getInvoiceCycleType());
		res.put("billingCycle", billingCycleRes);
		PaymentMode paymentMode = paymentModeList.stream()
				.filter(mode -> mode.getId().toString().equals(invoice.getModeOfPaymentId().toString())).findFirst()
				.orElse(null);
		HashMap<String, Object> paymentModeRes = new HashMap<>();
		paymentModeRes.put("modeOfPaymentId", paymentMode.getId());
		paymentModeRes.put("modeOfPaymentName", paymentMode.getPaymentModeType());
		res.put("paymentMode", paymentModeRes);

		PaymentTerms paymentTerms = paymentTermsList.stream()
				.filter(terms -> terms.getId().toString().equals(invoice.getPaymentTermsId().toString())).findFirst()
				.orElse(null);
		HashMap<String, Object> paymentTermsRes = new HashMap<>();
		paymentTermsRes.put("paymentTermsId", paymentTerms.getId());
		paymentTermsRes.put("paymentTermsName", paymentTerms.getPaymentTermsType());
		res.put("paymentTerms", paymentTermsRes);
		res.put("invoiceStatusId", invoiceStatus.getId());
		res.put("invoiceStatusName", invoiceStatus.getStatusName());

		res.put(ConstantUtility.AGING, calculateAging(invoice, invoiceStatusList));
		if (invoice.getReceivedOn() != null) {
			res.put("receivedOn", new SimpleDateFormat(ConstantUtility.DATE_FORMAT)
					.format(new Date(invoice.getReceivedOn().getTime())));
		}
		if (invoice.getComment() != null && !invoice.getComment().isEmpty()) {
			res.put("comment", invoice.getComment());
		}
		try {
			if ((Long) invoice.getCreatorId() != null) {

				Map<String, Object> invoiceMap = new HashMap<String, Object>();
				invoiceMap.put("createdId", invoice.getCreatorId());

				userNameList.forEach(map -> {
					if (map.get("userId").toString().equals(invoiceMap.get("createdId").toString())) {
						res.put("createdBy", map.get("userName"));
					}
				});
			}
		} catch (Exception e) {
			logger.info("inside getData method" + e.getMessage());
		}
		res.put("amountInRupee", invoice.getAmountInRupee());

		if (!SECRET_ENCRYPTION_KEY.equals(SECRET_NEW_ENCRYPTION_KEY)) {
			invoice.setAmount(invoice.getAmount());
			invoice.setAmountInDollar(invoice.getAmountInDollar());
			invoice.setAmountInRupee(invoice.getAmountInRupee());
			projectInvoiceRepository.save(invoice);
		}
		return res;
	}

	public Object getBusinessVerticals(String accessToken) {
		Map<String, Object> officeAndVerticals = (Map<String, Object>) feignLegacyInterface.getBusinessVerticals(accessToken).get("data");

		Object businessVerticals = officeAndVerticals.get("businessVertical");

		return businessVerticals;
	}

	@Cacheable("invoiceLineChart")
	public List<HashMap<String, Object>> getDataLineChart(List<Map<String,Object>> allProjectData,String year, String businessVertical) {
		List<String> months = Stream.of("January", "February", "March", "April", "May", "June", "July", "August",
				"September", "October", "November", "December").collect(Collectors.toList());
		
		ArrayList<HashMap<String, Object>> result = new ArrayList<>();
		double previousAmount=0;
		String status1Name = invoiceStatusRepository.findById(1l).getStatusName();
		String status2Name = invoiceStatusRepository.findById(2l).getStatusName();
		String status3Name = invoiceStatusRepository.findById(3l).getStatusName();
		String status4Name = invoiceStatusRepository.findById(4l).getStatusName();
		String status5Name = invoiceStatusRepository.findById(5l).getStatusName();
		
		List<ProjectInvoice> yearLyInvoices = projectInvoiceRepository
				.findAllByYearAndIsDeletedAndIsInternal(year, false, false);
		for (String month : months) {
			HashMap<String, Object> res = new HashMap<>();
			List<ProjectInvoice> buisnessVerticalFiltered = new ArrayList<>();
			res.put("month", month);
			res.put("businessVertical", businessVertical);
			List<ProjectInvoice> ProjectsBusinessVertical = 
					yearLyInvoices.stream().filter(invoice->invoice.getMonth().equals(month)).collect(toList());
			
			if (!businessVertical.equals("all")) {
				List<ProjectInvoice> invoiceList = new ArrayList<>();
				ProjectsBusinessVertical.forEach(invoice -> {
					Map<String, Object> projectDetails = this.getProjectDetail(allProjectData, invoice.getProjectId());
					String projectBusinessVertical ="UnAssigned";;
					if(projectDetails!=null)
						projectBusinessVertical = (String) projectDetails.get("businessVertical");
					if (projectBusinessVertical.equals(businessVertical))
						invoiceList.add(invoice);
				});
				buisnessVerticalFiltered=invoiceList;
			}
			else
				buisnessVerticalFiltered=ProjectsBusinessVertical;
			
			
			double status1Amount = 0;
			double status2Amount = 0;
			double status3Amount = 0;
			double status4Amount = 0;
			double status5Amount = 0;
			double totalAmount = 0;
			status1Amount=buisnessVerticalFiltered.stream().filter(inv-> Long.toString(inv.getInvoiceStatus()).equals("1") ).collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
			status2Amount=buisnessVerticalFiltered.stream().filter(inv-> Long.toString(inv.getInvoiceStatus()).equals("2") ).collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
			status3Amount=buisnessVerticalFiltered.stream().filter(inv-> Long.toString(inv.getInvoiceStatus()).equals("3") ).collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
			status4Amount=buisnessVerticalFiltered.stream().filter(inv-> Long.toString(inv.getInvoiceStatus()).equals("4") ).collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
			status5Amount=buisnessVerticalFiltered.stream().filter(inv-> Long.toString(inv.getInvoiceStatus()).equals("5") ).collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
			totalAmount = buisnessVerticalFiltered.stream().collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar));
			double differencePerc=0;
			if (totalAmount != 0) {
				if (previousAmount != 0)
					differencePerc = ((totalAmount - previousAmount) / previousAmount) * 100;
				else
					differencePerc = ((totalAmount - previousAmount) / totalAmount) * 100;
			}
			previousAmount=totalAmount;
			res.put(status1Name, status1Amount);
			res.put(status2Name, status2Amount);
			res.put(status3Name, status3Amount);
			res.put(status4Name, status4Amount);
			res.put(status5Name, status5Amount);
			res.put("projectCost", 0.0);
			res.put("total", totalAmount);
			res.put("differencePerc", differencePerc);
			result.add(res);
		}
		return result;
	}

	public List<ProjectInvoice> changeInvoiceStatus(String accessToken,
			List<Map<String,Object>> allProjectDetails) {
		List<ProjectInvoice> projectInvoices = new ArrayList<>();
		projectInvoices = projectInvoiceRepository.findAllByInvoiceStatusAndIsDeletedAndIsInternal(4L,false, false);
		List<Long> createdIdList = new ArrayList<>();
		projectInvoices.forEach(invoice -> {
			createdIdList.add(invoice.getCreatorId());
		});
		UserIdsDto uids = new UserIdsDto();
		uids.setUserIds(createdIdList);
		List<Map<String, Object>> userNameList=(List<Map<String, Object>>) feignLegacyInterface.getUserNameList(accessToken, uids).get("data");
		List<InvoiceCycle> billingCycleList = invoiceCycleRepository.findAll();
		List<InvoiceStatus> invoiceStatusList = invoiceStatusRepository.findAll();
		List<PaymentMode> paymentModeList = paymentModeRepository.findAll();
		List<PaymentTerms> paymentTermsList = paymentTermsRepository.findAll();
		projectInvoices.forEach(projectInvoice -> {
			InvoiceStatus invoiceStatus = invoiceStatusRepository.findById(projectInvoice.getInvoiceStatus());
			if (invoiceStatus.getStatusName().equals("Within Due Date")) {
				Date currentDate = new Date();
				if (currentDate.after(projectInvoice.getDueDate())) {
					InvoiceStatus pendingStatus = invoiceStatusRepository.findByStatusName("Pending");
					projectInvoice.setInvoiceStatus(pendingStatus.getId());
					projectInvoice = projectInvoiceRepository.saveAndFlush(projectInvoice);

					String dueDate = new SimpleDateFormat(ConstantUtility.DATE_FORMAT).format(new Date(projectInvoice.getDueDate().getTime()));
					HashMap<String, Object> data = new HashMap<>();
					data = getData(data, projectInvoice, allProjectDetails,userNameList,paymentTermsList,paymentModeList,invoiceStatusList,billingCycleList,null);
					Boolean mailSent=false;
					try {
						 mailSent = (Boolean) feignLegacyInterface.sendMailOnInvoiceStatusChange(accessToken,
								data,dueDate).get("data");
					} catch (Exception e) {
						e.printStackTrace();
					}					

					log.info("Mail sent status {} :" + mailSent);
				}
			}
		});

		return projectInvoices;
	}

	public boolean testingCron(String accessToken) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		HashMap<String, Object> hm = new HashMap<>();
		String year = String.valueOf(YearMonth.now().getYear());
		List<Long>projectList=(List<Long>) feignLegacyInterface.getProjectIdExceptClosed().get(ConstantUtility.DATA);
		for (Object projectId : projectList) {
			List<ProjectInvoice> invoiceList = projectInvoiceRepository
					.findByProjectIdAndMonthAndYearAndIsDeletedAndIsInternal(Integer.parseInt(projectId.toString()),
							TicketUtilities.getPreviousMonth(), year, false, false);
			if (invoiceList.isEmpty()) {
				result.add(Integer.parseInt(projectId.toString()));
			}
		}
		hm.put("projectList", result);
		feignLegacyInterface.sendInvoiceReminderMail(hm);

		return true;
	}

	public boolean cronServiceForInvoiceReminder() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		HashMap<String, Object> hm = new HashMap<String, Object>();
		String year = String.valueOf(YearMonth.now().getYear());
		List<Long>projectList=(List<Long>) feignLegacyInterface.getProjectIdExceptClosed().get(ConstantUtility.DATA);
		for (Object projectId : projectList) {
			List<ProjectInvoice> invoiceList = projectInvoiceRepository
					.findByProjectIdAndMonthAndYearAndIsDeletedAndIsInternal(Integer.parseInt(projectId.toString()),
							TicketUtilities.getPreviousMonth(), year, false, false);
			if (invoiceList.isEmpty()) {
				result.add(Integer.parseInt(projectId.toString()));
			}
		}
		hm.put("projectList", result);
		feignLegacyInterface.sendInvoiceReminderMail(hm);

		return true;
	}

	

	public Map<String, Object> getInvoiceTrends(String accessToken,String businessVertical, int monthValue, Long year) throws Exception {
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		List<ProjectTrendsDTO> projectTrends = new ArrayList<ProjectTrendsDTO>();
		HashMap<Long, Object> projectIdAndVerticals = new HashMap<Long, Object>();
		String month = Month.of(monthValue).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
		String previousMonth = Month.of(monthValue).minus(1).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
		String previousYear = Integer.toString(YearMonth.of(Integer.parseInt(year.toString()), monthValue).minusMonths(1).getYear());
		double ammountsInDoller = 0;
		double previousMonthAmmount = 0;
		double trends = 0;
		double trendsAmount = 0;
		if (businessVertical == null || businessVertical.equals("")) {
			businessVertical = "all";
		}
		Calendar cal = Calendar.getInstance();
		if( monthValue > (cal.get(Calendar.MONTH) + 1) && (year >= cal.get(Calendar.YEAR)))
			return Collections.emptyMap();

		

		List<ProjectInvoice> currentMonthInvoices = projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndIsInternal(month, year.toString(),	false, false);
		List<ProjectInvoice> previousMonthInvoices = projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndIsInternal(previousMonth, previousYear, false, false);
		projectIdAndVerticals = getInvoiceProjects(accessToken, year, projectIdAndVerticals, businessVertical, monthValue, currentMonthInvoices, previousMonthInvoices);
		if (projectIdAndVerticals != null) {
			for (Entry<Long, Object> entry : projectIdAndVerticals.entrySet()) {
				ProjectTrendsDTO trend = trendsOnProjectBasisV2(entry.getKey().longValue(), entry.getValue().toString(), month, previousMonth, year, currentMonthInvoices, previousMonthInvoices);
				ammountsInDoller += trend.getProvidedMonthTotalAmmount();
				previousMonthAmmount += trend.getPreviousMonthTotalAmmount();
				projectTrends.add(trend);
			}
			Collections.sort(projectTrends, new SortByTrend());
		}
		if (previousMonthAmmount != 0) {
			trendsAmount = ammountsInDoller - previousMonthAmmount;
			trends = ((ammountsInDoller - previousMonthAmmount) / previousMonthAmmount) * 100;
			trends = (double) Math.round(trends * 100) / 100;
		} else {
			trendsAmount = ammountsInDoller - previousMonthAmmount;
			trends = 100D;
		}
		response.put("businessVertical", businessVertical);
		response.put("overallTotalAmmount", (double) Math.round(ammountsInDoller * 100) / 100);
		response.put("overallpreviousMonthTotalAmmount", (double) Math.round(previousMonthAmmount * 100) / 100);
		response.put("overAlltrend", trends + "%");
		response.put("projectWiseTrends", projectTrends);
		response.put("overallTrendAmount", Math.round(trendsAmount * 100.0) / 100.0);
		return response;
	}
	
	
	
	private HashMap<Long, Object> getInvoiceProjects(String accessToken, Long year, HashMap<Long, Object> projectIdAndVerticals, String businessVertical, int monthValue,List<ProjectInvoice> currentMonthInvoices, List<ProjectInvoice> previousMonthInvoices) {
		List<Long> projectIds = currentMonthInvoices.stream().map(object -> object.getProjectId()).collect(Collectors.toList());
		for (ProjectInvoice invoice : previousMonthInvoices) {
			if (!projectIds.contains(invoice.getProjectId()))
				projectIds.add(invoice.getProjectId());
		}
		this.loadProjectExpectedHours(projectIds, monthValue, year);
		List<Map<String,Object>> projects  = feignLegacyInterface.findProjectDescriptionList(accessToken , projectIds, null, null);
		for (ProjectInvoice invoice : currentMonthInvoices) {
			Map<String,Object> project = null;
			if (!projectIdAndVerticals.containsKey(invoice.getProjectId())) {
				project=projects.stream().filter(pr->pr.get("projectId").toString().equals(invoice.getProjectId().toString())).findFirst().orElse(null);
				if(project!=null) {
					if (businessVertical.equals(project.get("businessVertical").toString())) {
						projectIdAndVerticals.put(Long.valueOf(project.get("projectId").toString()),
								project.get("businessVertical").toString());
					} else if (businessVertical.equals("all")) {
						projectIdAndVerticals.put(Long.valueOf(project.get("projectId").toString()),
								project.get("businessVertical").toString());
					}
				}
			}
		}
		for (ProjectInvoice invoice : previousMonthInvoices) {
			Map<String, Object> project = null;
			if (!projectIdAndVerticals.containsKey(invoice.getProjectId())) {
				project=projects.stream().filter(pr->pr.get("projectId").toString().equals(invoice.getProjectId().toString())).findFirst().orElse(null);
				if (businessVertical.equals(project.get("businessVertical").toString())) {
					projectIdAndVerticals.put(Long.valueOf(project.get("projectId").toString()),
							project.get("businessVertical").toString());
				} else if (businessVertical.equals("all")) {
					projectIdAndVerticals.put(Long.valueOf(project.get("projectId").toString()),
							project.get("businessVertical").toString());
				}
			}
		}
		
		return projectIdAndVerticals;
	}
	
	public ProjectTrendsDTO trendsOnProjectBasis(Long projectId, String businessVertical, String month,
			String previousMonth, Long year) throws Exception {
		ProjectTrendsDTO trendsDTO = new ProjectTrendsDTO();
		double ammountInDollers = 0;
		double ammountsInDollerLastMonth = 0;
		double trends = 0;
		double trendAmount = 0;
		String projectName = "";
		Long previousYear = year - 1;
		List<ProjectInvoice> invoices = new ArrayList<ProjectInvoice>();
		List<ProjectInvoice> previousMonthInvoices = new ArrayList<ProjectInvoice>();
		try {
			invoices.addAll(projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndProjectIdAndIsInternal(month,
					year.toString(), false, projectId, false));

			if (month.contains("January")) {
				previousMonthInvoices
						.addAll(projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndProjectIdAndIsInternal(
								previousMonth, previousYear.toString(), false, projectId, false));
			} else {
				previousMonthInvoices
						.addAll(projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndProjectIdAndIsInternal(
								previousMonth, year.toString(), false, projectId, false));
			}
			if (!invoices.isEmpty()) {
				projectName = invoices.stream().findFirst().get().getProject();
				for (ProjectInvoice invoice : invoices) {
					ammountInDollers += invoice.getAmountInDollar();
				}
			}
			if (!previousMonthInvoices.isEmpty()) {
				projectName = previousMonthInvoices.stream().findFirst().get().getProject();
				for (ProjectInvoice previousMonthAmmount : previousMonthInvoices) {
					ammountsInDollerLastMonth += previousMonthAmmount.getAmountInDollar();
				}
			}
			trendAmount = ammountInDollers - ammountsInDollerLastMonth;
			if (ammountsInDollerLastMonth != 0) {
				trends = ((ammountInDollers - ammountsInDollerLastMonth) / ammountsInDollerLastMonth) * 100;
				trends = (double) Math.round(trends * 100.0) / 100.0;
			} else {
				trends = 100.00;
			}
			trendsDTO.setExpectedHours(this.getProjectExpectedHours(projectId));
			trendsDTO.setProjectName(projectName);
			trendsDTO.setProvidedMonthTotalAmmount((double) Math.round(ammountInDollers * 100) / 100);
			trendsDTO.setPreviousMonthTotalAmmount((double) Math.round(ammountsInDollerLastMonth * 100) / 100);
			trendsDTO.setBusinessVertical(businessVertical);
			trendsDTO.setTrends(trends);
			trendsDTO.setProjectId(projectId);
			trendsDTO.setTrendAmount(trendAmount);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception();
		}

		return trendsDTO;
	}
	
	public ProjectTrendsDTO trendsOnProjectBasisV2(Long projectId, String businessVertical, String month, String previousMonth, Long year, List<ProjectInvoice> currentMonthInvoices, List<ProjectInvoice> previousMonthInvoices) throws Exception {
		ProjectTrendsDTO trendsDTO = new ProjectTrendsDTO();
		double ammountInDollers = 0;
		double ammountsInDollerLastMonth = 0;
		double trends = 0;
		double trendAmount = 0;
		String projectName = "";
		try {
			List<ProjectInvoice> invoices = currentMonthInvoices.stream().filter(invoice -> invoice.getProjectId().equals(projectId)).collect(Collectors.toList());
			List<ProjectInvoice> previousInvoices = previousMonthInvoices.stream().filter(invoice -> invoice.getProjectId().equals(projectId)).collect(Collectors.toList());
			if (!invoices.isEmpty()) {
				projectName = invoices.stream().findFirst().get().getProject();
				ammountInDollers = ammountInDollers +invoices.stream().mapToDouble(inv -> inv.getAmountInDollar()).sum();
			}
			if (!previousInvoices.isEmpty()) {
				projectName = previousInvoices.stream().findFirst().get().getProject();
				ammountsInDollerLastMonth = ammountsInDollerLastMonth + previousInvoices.stream().mapToDouble(inv -> inv.getAmountInDollar()).sum();
			}
			trendAmount = ammountInDollers - ammountsInDollerLastMonth;
			if (ammountsInDollerLastMonth != 0) {
				trends = ((ammountInDollers - ammountsInDollerLastMonth) / ammountsInDollerLastMonth) * 100;
				trends = (double) Math.round(trends * 100.0) / 100.0;
			} else {
				trends = 100.00;
			}
			trendsDTO.setExpectedHours(this.getProjectExpectedHours(projectId));
			trendsDTO.setProjectName(projectName);
			trendsDTO.setProvidedMonthTotalAmmount((double) Math.round(ammountInDollers * 100) / 100);
			trendsDTO.setPreviousMonthTotalAmmount((double) Math.round(ammountsInDollerLastMonth * 100) / 100);
			trendsDTO.setBusinessVertical(businessVertical);
			trendsDTO.setTrends(trends);
			trendsDTO.setProjectId(projectId);
			trendsDTO.setTrendAmount(trendAmount);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception();
		}
		return trendsDTO;
	}	
	
	public List<Map<String, Object>> getDisputedInvoices(String businessVertical) {
		if (businessVertical == null || businessVertical.equals(""))
			businessVertical = "all";
		LocalDate currentdate = LocalDate.now();
		Integer currentYear = currentdate.getYear();
		String month = Month.of(currentdate.getMonth().getValue()).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
		List<Map<String, Object>> disputedInvoices = new ArrayList<Map<String, Object>>();
		List<ProjectInvoice> invoices = projectInvoiceRepository
				.findAllByInvoiceStatusAndMonthAndYearAndIsDeletedAndIsInternal(5L, month, currentYear.toString(),
						false, false);
		for (ProjectInvoice invoice : invoices) {
			Map<String, Object> project = null;
			String projectBusinessVertical = "";
			if (!businessVertical.contains("all")) {
				project = (Map<String, Object>) feignLegacyInterface.getProjectDescription(invoice.getProjectId(),"",null,null).get("data");

				projectBusinessVertical = project.get("businessVertical").toString();
			}
			if ((projectBusinessVertical.contains(businessVertical)) || businessVertical.contains("all")) {
				Map<String, Object> invoiceMap = new HashMap<String, Object>();
				invoiceMap.put("projectId", invoice.getProjectId());
				invoiceMap.put("projectName", invoice.getProject());
				invoiceMap.put("ammountInRupee", invoice.getAmountInRupee());
				invoiceMap.put("ammoubtInDollar", invoice.getAmountInDollar());
				disputedInvoices.add(invoiceMap);
			}
		}
		return disputedInvoices;
	}

	public List<ProjectInvoice> getDisputedInvoiceListForBu(String bu,Integer year,String accessToken){
		List<ProjectInvoice> projectInvoice = projectInvoiceRepository.findAllByYearAndIsDeletedAndInvoiceStatus(year.toString(), false, 5L);
		Map<String,Object> projectwiseBuDetails=(Map<String, Object>) feignLegacyInterface.getProjectwiseBuDetails(accessToken,bu,null,year).get("data");

		List<String> buProjectIds=projectwiseBuDetails.keySet().stream().collect(Collectors.toList());
		List<ProjectInvoice> buInvoiceList = projectInvoice.stream().filter(inv-> buProjectIds.contains(inv.getProjectId().toString())).collect(Collectors.toList());

		return buInvoiceList;

	}

	public Map<String, Object> getDisputedInvoicePercentage(Integer month, Long year) {
		Map<String, Object> response = new LinkedHashMap<String, Object>();
		List<ProjectInvoice> invoices = new ArrayList<>();
		if (month == null && year == null) {
			invoices = projectInvoiceRepository.findAllByIsDeletedAndIsInternal(false, false);
		} else if (month == null && year != null) {
			invoices = projectInvoiceRepository.findAllByYearAndIsDeletedAndIsInternal(year.toString(), false, false);
		} else if (year == null && month != null) {
			LocalDate currentdate = LocalDate.now();
			Integer currentYear = currentdate.getYear();
			invoices = projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndIsInternal(
					Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH), currentYear.toString(), false,
					false);
		} else {
			invoices = projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndIsInternal(
					Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH), year.toString(), false, false);
		}
		Double totalAmmount = 0.0;
		Double totalDisputedAmmount = 0.0;
		for (ProjectInvoice invoice : invoices) {
			totalAmmount = totalAmmount + invoice.getAmountInDollar();
			if (invoice.getInvoiceStatus() == 5L)
				totalDisputedAmmount = totalDisputedAmmount + invoice.getAmountInDollar();
		}
		double disputedInvoicePercentage = (totalDisputedAmmount * 100.0) / totalAmmount;
		disputedInvoicePercentage = Math.round(disputedInvoicePercentage * 100.0) / 100.0;
		response.put(ConstantUtility.TOTAL_INVOICE_AMOUNT , totalAmmount);
		response.put(ConstantUtility.TOTAL_DISPUTED_AMMOUNT, totalDisputedAmmount);
		response.put(ConstantUtility.DISPUTED_INVOICE_PERCENTAGE, disputedInvoicePercentage);
		return response;
	}

	
	@Cacheable(value="ytdDisputedPerc",key="{#year, #businessVertical}")
	public Map<String, Object> getAverageDisputedPercentage(Long year, String businessVertical, String accessToken) {
		Map<String, Object> response = new LinkedHashMap<>();
		Double totalAmount = 0.0;
		Double disputedAmount = 0.0;
		Double disputedPercentage = 0.0;

		List<ProjectInvoice> invoices = projectInvoiceRepository
				.findAllByYearAndIsDeletedAndIsInternalAndInvoiceStatusNot(year.toString(),
						false, false, 6L);
		List<Long> projectIds = invoices.stream().map(inv -> inv.getProjectId()).collect(Collectors.toList());

//		Map<String, Object> projectwiseBuDetails = (Map<String, Object>) feignLegacyInterface
//				.getProjectwiseBuDetails(accessToken, businessVertical, null, Integer.valueOf(year.toString())).get("data");
		List<Map<String,Object>> projectwiseBuDetails  = feignLegacyInterface.findProjectDescriptionList(accessToken , projectIds, null, Integer.valueOf(year.toString()));

		List<String> buProjectIds = projectwiseBuDetails.stream().filter(i-> i.get("businessVertical").toString().equals(businessVertical) || businessVertical.equals("")) .map(i-> i.get(ConstantUtility.PROJECT_ID).toString()).collect(Collectors.toList());
		disputedAmount = invoices.stream()
				.filter(inv -> buProjectIds.contains(inv.getProjectId().toString())
						&& Long.toString(inv.getInvoiceStatus()).equals("5"))
				.collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
		totalAmount = invoices.stream()
				.filter(inv -> buProjectIds.contains(inv.getProjectId().toString()))
				.collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
		
		Calendar fromCal = Calendar.getInstance();
		fromCal.set(Calendar.YEAR, Integer.parseInt(year.toString()));
		fromCal.set(Calendar.MONTH, Calendar.JANUARY);
		Calendar toCal = Calendar.getInstance();
		toCal.set(Calendar.YEAR, Integer.parseInt(year.toString()));
		toCal.set(Calendar.MONTH, Calendar.DECEMBER);
		List<Map<String,Object>> avgDollarCost = new ArrayList<>();
		Double avgCost = 1D;
		avgDollarCost = dollarCostService.getMonthWiseAverageDollarCost(fromCal.getTime(), toCal.getTime());
		for(Map<String,Object> map : avgDollarCost){
			if(map.containsKey("AverageCost")){
				avgCost=Double.parseDouble(map.get("AverageCost").toString());
				break;
			}
			else
				continue;
		}

		disputedPercentage = (disputedAmount / totalAmount) * 100;
		response.put("averageDisputedPercentage", Math.round(disputedPercentage * 100.0) / 100.0);
		
		response.put("disputedAmount", disputedAmount * avgCost);
		response.put("disputedAmountInDollars", disputedAmount);
		response.put("totalAmount", totalAmount * avgCost);

		return response;
	}

	@Cacheable(value="ltmDisputedPerc",key="{#year, #businessVertical, #month}")
	public Map<String, Object> getLTMDisputedPercentage(Long year, String businessVertical,String accessToken, Integer month) {
		Map<String, Object> response = new LinkedHashMap<>();
		
		Double totalAmountLTM = 0.0;
		Double disputedAmountLTM = 0.0;
		Double disputedPercentageLTM = 0.0;

		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if(month != null){
			cal.set(Calendar.MONTH, month-1);
		}
		cal.set(Calendar.YEAR, Integer.parseInt(year.toString()));
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DATE));
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(cal.getTime());
		cal1.add(Calendar.MONTH, -11);
		cal1.set(Calendar.DAY_OF_MONTH, 1);
		
		String monthName = new DateFormatSymbols().getMonths()[month - 1].toString();
		List<ProjectInvoice> invoices = projectInvoiceRepository.findAllByYearAndIsDeletedAndIsInternalAndInvoiceStatusNot(year.toString(),
				false, false, 6L);
		List<ProjectInvoice> previousInvoices = projectInvoiceRepository.findAllByYearAndIsDeletedAndIsInternalAndInvoiceStatusNot(String.valueOf(year-1),
				false, false, 6L);
//		Map<String,Object> projectwiseBuDetails=(Map<String, Object>) feignLegacyInterface.getProjectwiseBuDetails(accessToken,businessVertical,null,null).get("data");
//		List<String> buProjectIds=projectwiseBuDetails.keySet().stream().collect(Collectors.toList());
//		List<Long> projectIds = invoices.stream().map(inv -> inv.getProjectId()).collect(Collectors.toList());
//		projectIds.addAll(previousInvoices.stream().map(inv -> inv.getProjectId()).collect(Collectors.toList()));
//		projectIds = projectIds.stream().distinct().collect(Collectors.toList());
		List<Map<String,Object>> projectwiseBuDetailsCurrent  = feignLegacyInterface.findProjectDescriptionList(accessToken , invoices.stream().map(inv -> inv.getProjectId()).collect(Collectors.toList()), null, Integer.valueOf(year.toString()));
		List<Map<String,Object>> projectwiseBuDetailsPrevious  = feignLegacyInterface.findProjectDescriptionList(accessToken , previousInvoices.stream().map(inv -> inv.getProjectId()).collect(Collectors.toList()), null, Integer.valueOf(String.valueOf(year-1)));

		List<String> buProjectIds = projectwiseBuDetailsCurrent.stream().filter(i-> i.get("businessVertical").toString().equals(businessVertical) || businessVertical.equals("")) .map(i-> i.get(ConstantUtility.PROJECT_ID).toString()).collect(Collectors.toList());
		buProjectIds.addAll(projectwiseBuDetailsPrevious.stream().filter(i-> i.get("businessVertical").toString().equals(businessVertical) || businessVertical.equals("")) .map(i-> i.get(ConstantUtility.PROJECT_ID).toString()).collect(Collectors.toList()));
		List<ProjectInvoice> ltmInvoices = new ArrayList<>();
		List<ProjectInvoice> totalInvoices = new ArrayList<>();
		totalInvoices.addAll(invoices);
		totalInvoices.addAll(previousInvoices);
		for(ProjectInvoice inv : totalInvoices){
			Calendar invCal = Calendar.getInstance();
			invCal.setTime(date);
			invCal.set(Calendar.YEAR,Integer.parseInt(inv.getYear()));
			invCal.set(Calendar.MONTH,Month.valueOf(inv.getMonth().toUpperCase()).getValue()-1);
			invCal.set(Calendar.DAY_OF_MONTH, 15);
			if(invCal.getTime().before(cal.getTime()) && invCal.getTime().after(cal1.getTime()) ){
				ltmInvoices.add(inv);
			}
		}
		Double monthlyTotalInvoiceAmt = invoices.stream().filter(inv-> buProjectIds.contains(inv.getProjectId().toString()) && inv.getMonth().equals(monthName)).collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar));
		
		disputedAmountLTM = ltmInvoices.stream()
	            .filter(inv -> buProjectIds.contains(inv.getProjectId().toString()) && Long.toString(inv.getInvoiceStatus()).equals("5"))
	            .collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
		totalAmountLTM= ltmInvoices.stream()
	            .filter(inv -> buProjectIds.contains(inv.getProjectId().toString()))
	            .collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
		

		List<Map<String,Object>> avgDollarCost = new ArrayList<>();
		Double avgCost = 1D;
		avgDollarCost = dollarCostService.getMonthWiseAverageDollarCost(cal1.getTime(), cal.getTime());
		for(Map<String,Object> map : avgDollarCost){
			if(map.containsKey("AverageCost")){
				avgCost=Double.parseDouble(map.get("AverageCost").toString());
				break;
			}
			else
				continue;
		}

		
			
		disputedPercentageLTM = (disputedAmountLTM / totalAmountLTM) * 100;

		Double dollarexchangeCost = dollarCostService.getAverageDollarCost(month,Integer.parseInt(year.toString()));

		response.put("averageDisputedPercentageLTM", Math.round(disputedPercentageLTM * 100.0) / 100.0);
		response.put("disputedAmountLTM", disputedAmountLTM * avgCost);
		response.put("disputedAmountInDollarsLTM", disputedAmountLTM);
		response.put("monthlyExtInvoiceTotal", monthlyTotalInvoiceAmt*dollarexchangeCost);
		response.put("totalAmountLTM",totalAmountLTM * avgCost);

		return response;
	}

	public Double getDisputedPercentageOfMonth(int monthValue, Long year, String businessVertical) {
		List<ProjectInvoice> invoices = new ArrayList<ProjectInvoice>();
		String month = Month.of(monthValue).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

		Double totalAmmount = 0.0;
		Double totalDisputedAmmount = 0.0;
		invoices = projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndIsInternal(month, year.toString(),
				false, false);
		for (ProjectInvoice invoice : invoices) {
			Map<String, Object> projectDetails = (Map<String, Object>) feignLegacyInterface.getProjectDescription(invoice.getProjectId(),"",monthValue,Integer.parseInt(year.toString())).get("data");

			String projectBusinessVertical = (String) projectDetails.get("businessVertical");
			if (projectBusinessVertical.equals(businessVertical)) {
				totalAmmount = totalAmmount + invoice.getAmountInDollar();
				if (invoice.getInvoiceStatus() == 5) {
					totalDisputedAmmount = totalDisputedAmmount + invoice.getAmountInDollar();
				}
			}
		}

		Double disputedPercentage = (totalDisputedAmmount * 100.0) / totalAmmount;
		if (disputedPercentage.isNaN())
			disputedPercentage = 0.0;
		return disputedPercentage;
	}

	
	
	
	public List<Map<String,Object>> getAllDisputedInvoicesOfProject() {
		List<Map<String,Object>> response = new ArrayList<>();
		Map<String, Object> totalAmount = new HashMap<>();
		List<ProjectInvoice> projectInvoices = projectInvoiceRepository.findAllByInvoiceStatusAndIsDeletedAndIsInternal(5L,false, false);
		List<Long> projects=projectInvoices.stream().map(pro->pro.getProjectId()).collect(Collectors.toList());		
		Double totalDisputedAmount = 0.0;
		projects=projects.stream().distinct().collect(Collectors.toList());
		for (Long project : projects) {
			Map<String, Object> data = new LinkedHashMap<>();
			List<ProjectInvoice> invoices=projectInvoices.stream().filter(pr->pr.getProjectId().toString().equals(project.toString())).collect(Collectors.toList());
			Double totalDisputedAmmountsInDollars = 0.0;
			for (ProjectInvoice invoice : invoices) {
					totalDisputedAmmountsInDollars = totalDisputedAmmountsInDollars + invoice.getAmountInDollar();
					totalDisputedAmount += totalDisputedAmmountsInDollars;
			} 
			if (totalDisputedAmmountsInDollars != 0) {
				data.put("disputedAmmountInDollars", Math.round(totalDisputedAmmountsInDollars * 100.0) / 100.0);
				data.put("projectId", project);
				response.add(data);
			}
		}
		totalAmount.put("totalDisputedAmount", Math.round(totalDisputedAmount * 100.0) / 100.0); 
		response.add(totalAmount);
   		return response;
	}

	public Map<String, Object> getDisputedInvoicesOfProject(Long projectId, Long year) {
		Map<String, Object> response = new LinkedHashMap<>();
		List<ProjectInvoice> invoices = new ArrayList<>();
		if (year == 0)
			invoices = projectInvoiceRepository.findAllByProjectIdAndInvoiceStatusAndIsDeletedAndIsInternal(projectId,
					5L, false, false);
		else
			invoices = projectInvoiceRepository.findAllByProjectIdAndInvoiceStatusAndYearAndIsDeletedAndIsInternal(
					projectId, 5L, year.toString(), false, false);
		List<Object> invoiceList = new ArrayList<>();
		Double totalAmmountInDollars = 0.0;
		Double totalDisputedAmmountsInDollars = 0.0;
		Double totalAmmountInRupee = 0.0;
		Double totalDisputedAmmountInRupee = 0.0;
		Double disputedPercentage = 0.0;
		Double totalAmountRecovered = 0.0;

		for (ProjectInvoice invoice : invoices) {
			totalAmmountInDollars = totalAmmountInDollars + invoice.getAmountInDollar();
			totalAmmountInRupee = totalAmmountInRupee + invoice.getAmountInRupee();

			if (invoice.getInvoiceStatus() == 5L) {
				totalDisputedAmmountsInDollars = totalDisputedAmmountsInDollars + invoice.getAmountInDollar();
				totalDisputedAmmountInRupee = totalAmmountInRupee + invoice.getAmountInRupee();
				Map<String, Object> invoiceMap = new LinkedHashMap<>();
				invoiceMap.put("ammountInDollars", Math.round(invoice.getAmountInDollar() * 100.0) / 100.0);
				invoiceMap.put("ammountInRupee", Math.round(invoice.getAmountInRupee() * 100.0) / 100.0);
				invoiceMap.put("dueDate", new SimpleDateFormat(ConstantUtility.DATE_FORMAT).format(new Date(invoice.getDueDate().getTime())));
				invoiceMap.put( "billingDate", new SimpleDateFormat(ConstantUtility.DATE_FORMAT).format(new Date(invoice.getBillingDate().getTime())));
				invoiceMap.put("billingCycle",invoiceCycleRepository.findById(invoice.getInvoiceCycleId()).getInvoiceCycleType());
				invoiceMap.put("modeOfPayment",paymentModeRepository.findById(invoice.getModeOfPaymentId()).getPaymentModeType());
				invoiceMap.put("paymentTerms",paymentTermsRepository.findById(invoice.getPaymentTermsId()).getPaymentTermsType());
				invoiceMap.put("exchangeRate", invoice.getExchangeRate());
				invoiceMap.put("currency", invoice.getCurrency());
				invoiceMap.put("invoiceId", invoice.getId());
				invoiceMap.put("buHeadComment", invoice.getBuHeadComment() == null ? "" : invoice.getBuHeadComment());
				invoiceMap.put("disputedDate",invoice.getDisputedDate() == null ? "NA" : Timestamp.valueOf(invoice.getDisputedDate()));
				if (Objects.nonNull(invoice.getDisputedDate())) {
					invoiceMap.put(ConstantUtility.AGING,Period.between(invoice.getDisputedDate().toLocalDate(), LocalDate.now()).getDays());
				} else {
					invoiceMap.put(ConstantUtility.AGING, "0");
				}
				invoiceList.add(invoiceMap);
			} else if (invoice.getInvoiceStatus() == 2L) {
				totalAmountRecovered += invoice.getAmountInDollar();
			}
		}
		disputedPercentage = (totalDisputedAmmountsInDollars * 100.0) / totalAmmountInDollars;
		disputedPercentage = Math.round(disputedPercentage * 100.0) / 100.0;
		if (invoices.size() != 0) {
			response.put("projectId", invoices.stream().findFirst().get().getProjectId());
			response.put("projectName", invoices.stream().findFirst().get().getProject());
		} else {
			response.put("projectId", null);
			response.put("projectName", null);
		}
		response.put("totalAmmountInRupee", Math.round(totalAmmountInRupee * 100.0) / 100.0);
		response.put("disputedAmmountInRupee" , Math.round(totalDisputedAmmountInRupee * 100.0) / 100.0);
		response.put("totalAmmountInDollars", Math.round(totalAmmountInDollars * 100.0) / 100.0);
		response.put("disputedAmmountInDollars", Math.round(totalDisputedAmmountsInDollars * 100.0) / 100.0);
		response.put("disputedPercentage", Math.round(disputedPercentage * 100.0) / 100.0);
		response.put("invoices", invoiceList);
		return response;
	}

	public List<ProjectInvoice> getAccountCompliantFlag(Long projectId, String month) {
		List<ProjectInvoice> invoices = projectInvoiceRepository
				.findAllByProjectIdAndMonthAndIsDeletedAndIsInternal(projectId, month, false, false);
		return invoices.parallelStream().filter(unpaidOrCrossedDateInvoice()).collect(Collectors.toList());
	}

	public Predicate<ProjectInvoice> unpaidOrCrossedDateInvoice() {
		return invoice -> invoice.getInvoiceStatus() == 1
				|| (invoice.getDueDate().before(new Date(System.currentTimeMillis()))
						&& invoice.getInvoiceStatus() == 4);
	}

	@CacheEvict(cacheNames = "invoiceLineChart", allEntries = true)
	public void flushInvoiceChart() {
	}

	@CacheEvict(value = { "ytdDisputedPerc", "ytdBuDisputedPerc","ltmDisputedPerc","ltmBuDisputedPerc" }, allEntries = true)
	public void flushYtdPerc() {
	}

	@CacheEvict(cacheNames = "projectDetailsCache", allEntries = true)
	public void flushProjectDetailsCache() {
	}

	public Map<String, Object> getProjectWiseInvoiceStatus(String authorization, String month, String year,
			long project) {
		List<ProjectInvoice> data;
		if (month.equals("")) {
			data = projectInvoiceRepository.findByProjectIdAndYearAndIsDeletedAndIsInternal(project, year, false,
					false);
		} else {
			data = projectInvoiceRepository.findByProjectIdAndMonthAndYearAndIsDeletedAndIsInternal(project, month,
					year, false, false);
		}
		HashMap<String, Object> response = new HashMap<>();
		for (ProjectInvoice invoice : data) {
			InvoiceStatus invoiceStatus = invoiceStatusRepository.findById(invoice.getInvoiceStatus());
			HashMap<String, Object> invoiceStatusRes = new HashMap<>();
			invoiceStatusRes.put("invoiceStatusId", invoiceStatus.getId());
			invoiceStatusRes.put("invoiceStatusName", invoiceStatus.getStatusName());
			response.put("invoiceStatus", invoiceStatusRes);
		}
		return response;
	}

	public ArrayList<HashMap<String, Object>> getProjectWiseInvoices(String authorization, List<Long> projectIds) {
		Double invoiceAmountInDollar = 0.0;
		ArrayList<HashMap<String, Object>> response = new ArrayList<>();
		List<ProjectInvoice> invoiceList = projectInvoiceRepository.findAllByIsDeleted(false);
		
		for (Long projectId : projectIds) {
			List<ProjectInvoice> invoices = invoiceList.stream().filter(inv-> inv.getProjectId().equals(projectId)).collect(Collectors.toList());
			HashMap<String, Object> invoiceMap = new HashMap<>();

			if (!invoices.isEmpty()) {
				invoiceAmountInDollar = invoices.stream().mapToDouble(invoice -> invoice.getAmountInDollar())
						.sum();
			}
			invoiceMap.put("invoiceAmountInDollar", invoiceAmountInDollar);
			invoiceMap.put("projectId", projectId);
			response.add(invoiceMap);
		}
		return response;
	}

	public String getProjectExpectedHours(Long projectId) {
		String expectedHours = "0.00";
		if (projectExpectedHours != null) {
			for (Map<String, Object> projectDetails : projectExpectedHours) {
				if (((Number) projectDetails.get("projectId")).longValue() == projectId) {
					expectedHours = projectDetails.get("expectedHours").toString();
				}
			}
		}
		return expectedHours;
	}

	public void loadProjectExpectedHours(List<Long> projectIds, int month, Long year) {
		try{
		this.projectExpectedHours = (List<Map<String, Object>>) feignLegacyInterface
				.getProjectExpectedHours(month, year, projectIds).get("data");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public List<Map<String, Object>> getPreviousMonthInvoices(String accessToken, Integer monthValue, String year) {
		Map<String, Object> projectsData =new HashMap<>();
		UserModel currentUser = validator.tokenbValidate(accessToken);
		//Fetch only Open and Closed ProjectList
		if (currentUser.getRoles().contains("ROLE_PMO")){
			projectsData = (Map<String,Object>)feignLegacyInterface.getActiveProjectForCompliance(accessToken, monthValue , Integer.valueOf(year),true, "All");
		}else {
			projectsData = (Map<String,Object>)feignLegacyInterface.getActiveProjectForCompliance(accessToken, monthValue , Integer.valueOf(year),false, "All");
		} 
		List<Integer> projects = (List<Integer>) ((Map<String,Object>)projectsData.get("data")).get("projectData");
		List<Long>activeProjects=new ArrayList<>();
		if(projects!=null && !projects.isEmpty()){
			projects.forEach(pro->{
				activeProjects.add(Long.valueOf(pro.toString()));
			});
		}
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		List<ProjectInvoice> invoices  = new ArrayList<>();
		//calculate last 3 month invoices 
		monthValue = monthValue + 1;
		if(monthValue <= 3 ){
			LocalDate date = YearMonth.of(Integer.valueOf(year).intValue(), monthValue.intValue()).atEndOfMonth(); 
		    for (int i = 1; i <= 3; i++) {
		    	 YearMonth yearMonth = YearMonth.from(date.minusMonths(i));
		    	 String yearName = String.valueOf(yearMonth.getYear());
		         String monthName = yearMonth.getMonth().name();
		         invoices.addAll(projectInvoiceRepository.findAllByMonthAndYearAndIsDeletedAndProjectIdIn(monthName, yearName, false , activeProjects));
		         }
		}
		else {
			invoices = projectInvoiceRepository.findAllByProjectIdInAndYearAndIsDeleted(activeProjects,year,false);
		}
		for(Long projectId:activeProjects) {
			resultList.add(getInvoicesV2(projectId,invoices,3, monthValue, year));
		}
		return resultList;
	}
		

	private boolean checkProjectClosedAndStatusNotNull(ProjectDto projectDto) {
		boolean flag = false;
		if (projectDto.getCurrentStatus().equals("Closed")) {
			if (projectDto.getClosedDate() != null) {
				flag = true;

			}
		}
		return flag;
	}

	private boolean checkIfInvoiceExists(Long projectId, String month, String year) {
		Month date = Month.of(Integer.valueOf(month));
		LocalDate localDate = LocalDate.of(Integer.valueOf(year), date, 15);
		List<ProjectInvoice> invoices = projectInvoiceRepository
				.findAllByMonthAndYearAndProjectIdAndIsDeletedAndIsInternal(
						localDate.minusMonths(1).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
						String.valueOf(localDate.minusMonths(1).getYear()), projectId, false, false);
		return !invoices.isEmpty();
	}

	public Map<String, Object> getInvoices(Long projectId, int totalMonths, Integer month, String year) {
		Map<String, Object> invoiceResponse = new LinkedHashMap<>();
		Double ammountInDollars = 0d;
		Month date = Month.of(month);
		List<Double> monthAmmount = new ArrayList<Double>();
		for (int i = 1; i <= totalMonths; i++) {
			Double currentMonthAmmount = 0d;
			LocalDate localDate = LocalDate.of(Integer.valueOf(year), date, 15);
			List<ProjectInvoice> invoices = projectInvoiceRepository
					.findAllByMonthAndYearAndProjectIdAndIsDeletedAndIsInternal(
							localDate.minusMonths(i).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
							String.valueOf(localDate.minusMonths(i).getYear()), projectId, false, false);
			if (!invoices.isEmpty()) {
				for (ProjectInvoice invoice : invoices) {
					currentMonthAmmount = currentMonthAmmount + invoice.getAmountInDollar();
					ammountInDollars = ammountInDollars + invoice.getAmountInDollar();
				}
			}
			monthAmmount.add(Math.round(currentMonthAmmount * 100.0) / 100.0);
		}
		invoiceResponse.put("projectId", projectId);
		invoiceResponse.put("totalAmmount", Math.round(ammountInDollars * 100.0) / 100.0);
		invoiceResponse.put("monthAmmount", monthAmmount);
		return invoiceResponse;

	}
	public Map<String, Object> getInvoicesV2(Long projectId,List<ProjectInvoice> invoicesList,int totalMonths, Integer month, String year) {
		Map<String, Object> invoiceResponse = new LinkedHashMap<>();
		Double ammountInDollars = 0d;
		List<Double> monthAmmount = new ArrayList<Double>();
		LocalDate date = YearMonth.of(Integer.valueOf(year).intValue(), month.intValue()).atEndOfMonth(); 
	    for (int i = 1; i <= totalMonths; i++) {
	    	Double currentMonthAmmount = 0d;
	    	YearMonth yearMonth = YearMonth.from(date.minusMonths(i));
	    	String yearName = String.valueOf(yearMonth.getYear());
	        String monthName = yearMonth.getMonth().name();
	        List<ProjectInvoice> invoices = invoicesList.stream().filter(inv->inv.getProjectId().toString().equals(projectId.toString()) &&  inv.getMonth().equalsIgnoreCase(monthName) && inv.getYear().equalsIgnoreCase(yearName)).collect(Collectors.toList());			
			if (!invoices.isEmpty()) {
				for (ProjectInvoice invoice : invoices) {
					currentMonthAmmount = currentMonthAmmount + invoice.getAmountInDollar();
					ammountInDollars = ammountInDollars + invoice.getAmountInDollar();
				}
			}
			monthAmmount.add(Math.round(currentMonthAmmount * 100.0) / 100.0);
		}
		invoiceResponse.put("projectId", projectId);
		invoiceResponse.put("totalAmmount", Math.round(ammountInDollars * 100.0) / 100.0);
		invoiceResponse.put("monthAmmount", monthAmmount);
		return invoiceResponse;
	}
	
	public Map<String, Object> diputedInvoiceYearly(int year, String accessToken, String buFilter, String projectStatus,
			Long invoiceStatus, Long teamHeadId) {
		if(teamHeadId==null) {
			return projectInvoiceRepository
				.findAllByYearAndInvoiceStatusAndIsDeletedAndIsInternal(String.valueOf(year), invoiceStatus, false,
						false)
				.map(invoiceList -> this.yearlyDisputedInvoicesResponse(invoiceList, accessToken, buFilter,
						projectStatus))
				.orElse(new HashMap<>());
		}
		else {
			List<Map<String,Object>>projects=(List<Map<String, Object>>) feignLegacyInterface.getTeamHeadWiseProjectList(accessToken, teamHeadId, buFilter).get("data");
			List<Long> projectIds=new ArrayList<>();
			projects.forEach(project->{
				Long projectId=Long.parseLong(project.get("projectId").toString()) ;
				projectIds.add(projectId);
			});
			return projectInvoiceRepository
					.findAllByYearAndInvoiceStatusAndIsDeletedAndIsInternalAndProjectIdIn(String.valueOf(year), invoiceStatus, false,
							false,projectIds)
					.map(invoiceList -> this.yearlyDisputedInvoicesResponse(invoiceList, accessToken, buFilter,
							projectStatus))
					.orElse(new HashMap<>());
		}
	}

	private Map<String, Object> yearlyDisputedInvoicesResponse(List<ProjectInvoice> invoiceList, String accessToken,
			String buFilter, String projectStatus) {
		HashMap<String, Object> response = new HashMap<>();
		List<Map<String, Object>> finalProjectResponseList = new ArrayList<>();
		List<Long> uniqueProjectIds = invoiceList.parallelStream().map(ProjectInvoice::getProjectId).distinct()
				.collect(Collectors.toList());
		Map<String, Object> responseForProjectInfo = getProjectDataByIds(accessToken, uniqueProjectIds, buFilter,
				projectStatus);
		List<Map<String, Object>> projectsData = (List<Map<String, Object>>) responseForProjectInfo.get("data");
		for (Long projectId : uniqueProjectIds) {
			Optional<Map<String, Object>> projectData = yearlyProjectMap(invoiceList, projectId, projectsData);
			if (projectData.isPresent()) {
				finalProjectResponseList.add(projectData.get());
			}
		}
		Double disputedAmountSum = finalProjectResponseList.parallelStream()
				.flatMap(project -> Stream.of(Double.parseDouble(project.get("amountSumInDollars").toString())))
				.reduce(0D, Double::sum);
		response.put("invoicesWithProject", finalProjectResponseList);
		response.put("totalRecoveredAmount", responseForProjectInfo.get("totalRecoveredAmount"));
		response.put("totalDisputedAmountSum", Math.round(disputedAmountSum * 100.00) / 100.00);
		return response;
	}

	private Map<String, Object> getProjectDataByIds(String accessToken, List<Long> uniqueProjectIds, String buFilter,
			String projectStatus) {
		return (Map<String, Object>) feignLegacyInterface.getLegalTeamDataByProjectIds(accessToken, uniqueProjectIds,buFilter,
				 projectStatus).get("data");
	}

	private static Optional<Map<String, Object>> yearlyProjectMap(List<ProjectInvoice> invoiceList, Long projectId,
			List<Map<String, Object>> allProjects) {
		Map<String, Object> projectData = filterProjectFromList(projectId, allProjects);
		if (Objects.nonNull(projectData)) {
			HashMap<String, Object> resObj = new HashMap<>();
			resObj.put("projectId", projectId);
			resObj.put("projectData", projectData);
			resObj.put("maxAging", calculateAgingForDisputed(projectId, invoiceList));
			Double sum = invoiceList.parallelStream().filter(invoice -> invoice.getProjectId().toString().equals(projectId.toString()))
					.collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar));	 
			 resObj.put("amountSumInDollars", Math.round(sum * 100.00) / 100.00);
			return Optional.of(resObj);
		}
		return Optional.empty();
	}

	private static int calculateAgingForDisputed(Long projectId, List<ProjectInvoice> invoiceList) {
		List<ProjectInvoice> disputedList = invoiceList.stream().filter(invoice -> invoice.getProjectId().toString().equals(projectId.toString())).collect(Collectors.toList());
		Date date = new Date();
		int maxAging = 0;
		for (ProjectInvoice invoice : disputedList) {
			if (invoice.getDueDate() != null) {
				long difference = date.getTime() - invoice.getDueDate().getTime();
				float daysBetween = (difference / (1000 * 60 * 60 * 24));
				maxAging = (int) daysBetween >= maxAging ? (int) daysBetween : maxAging;
			}
		}
		return maxAging;
	}

	private static Map<String, Object> filterProjectFromList(Long projectId,
			List<Map<String, Object>> projectDataList) {
		return projectDataList.parallelStream()
				.filter(project -> new Long(project.get("projectId").toString()).equals(projectId)).findFirst()
				.orElse(null);
	}

	public Object markInvoiceDisputed(Long invoiceId, String comments, String invoiceStatus) {
		if (Objects.nonNull(invoiceId) && Objects.nonNull(comments)) {
			ProjectInvoice invoice = projectInvoiceRepository.findById(invoiceId);
			if (Objects.nonNull(invoice)) {
				InvoiceStatus pendingStatus = invoiceStatusRepository.findByStatusName(invoiceStatus);
				invoice.setInvoiceStatus(pendingStatus.getId());
				invoice.setDisputedDate(java.time.LocalDateTime.now());
				invoice.setBuHeadComment(comments);
				return projectInvoiceRepository.save(invoice);
			} else {
				return "Invalid invoice Id";
			}
		} else {
			return "Invoice Id or Comments Can't be null";
		}
	}

	public Object getPendingProjectInvoices(long projectId, Long year) {
		return projectInvoiceRepository.findAllByProjectIdAndInvoiceStatusAndIsDeletedAndYearAndIsInternal(projectId,
				(long) 1, false, year.toString(), false).map(invoices -> {
					HashMap<String, Object> res = new HashMap<>();
					List<?> invoicesList = pendingInvoicesResponse().apply(invoices);
					res.put("invoices", invoicesList);
					res.put("totalSumInDollar",
							invoices.stream().collect(Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
					res.put("totalAmountSuminINR",
							invoices.stream().collect(Collectors.summingDouble(ProjectInvoice::getAmountInRupee)));
					return res;
				}).orElseGet(HashMap::new);
	}

	public Function<List<ProjectInvoice>, List<?>> pendingInvoicesResponse() {
		return invoicesList -> invoicesList.parallelStream().map(invoice -> {
			Map<String, Object> res = new HashMap<>();
			res.put("invoiceStatus", invoiceStatusRepository.findById(invoice.getInvoiceStatus()).getStatusName());
			res.put("invoiceAmount", invoice.getAmountInDollar());
			res.put("currency", invoice.getCurrency());
			res.put("exchangeRate", Math.round(invoice.getExchangeRate() * 100.00) / 100.00);
			res.put("paymentTerms", paymentTermsRepository.findById(invoice.getPaymentTermsId()).getPaymentTermsType());
			res.put("modeOfPayment", paymentModeRepository.findById(invoice.getModeOfPaymentId()).getPaymentModeType());
			res.put("billingCycle", invoiceCycleRepository.findById(invoice.getInvoiceCycleId()).getInvoiceCycleType());
			res.put("amount", invoice.getAmountInRupee());
			res.put("dueDate", invoice.getDueDate().getTime());
			res.put("id", invoice.getId());
			res.put("invoiceDate", invoice.getBillingDate().getTime());
			res.put("buHeadComment", invoice.getBuHeadComment());
			return res;
		}).collect(Collectors.toList());
	}

	public List<InvoiceType> getInvoiceType() {
		List<InvoiceType> invoices = invoiceTypeRepository.findAllByIsArchived(false);
		return invoices;
	}
	

	public InvoiceProjectSettings addProjectSettings(ProjectSettingsDto projectSettingsDto, String authorization) {
		InvoiceProjectSettings projectSettings = null;
		if(projectSettingsDto.getProjectId()!=null && projectSettingsDto.getProjectId()!=0L) {
			projectSettings = projectSettingsRepo.findByProjectId(projectSettingsDto.getProjectId());
		}
		else {
			projectSettings = projectSettingsRepo.findByLeadId(projectSettingsDto.getLeadId());
		}
		if (projectSettings == null)
			projectSettings = new InvoiceProjectSettings();
		if (projectSettingsDto.getPaymentModeId() != null) {
			PaymentMode paymentMode = paymentModeRepository.findById(projectSettingsDto.getPaymentModeId());
			projectSettings.setPaymentModeId(paymentMode.getId());
		}
		if (projectSettingsDto.getBillingCycleId() != null) {
			PaymentTerms paymentTerms = paymentTermsRepository.findById(projectSettingsDto.getPaymentTermsId());
			projectSettings.setPaymentTermsId(paymentTerms.getId());
		}
		if (projectSettingsDto.getBillingCycleId() != null) {
			InvoiceCycle billingCycle = invoiceCycleRepository.findById(projectSettingsDto.getBillingCycleId());
			projectSettings.setBillingCycleId(billingCycle.getId());
		}
		projectSettings.setGstNumber(projectSettingsDto.getGstNumber());
		projectSettings.setClientAddress(projectSettingsDto.getClientAddress());
		if(projectSettingsDto.getProjectId()!=null && projectSettingsDto.getProjectId()!=0L)
		{
			projectSettings.setProjectId(projectSettingsDto.getProjectId());
		}
		else {
			projectSettings.setLeadId(projectSettingsDto.getLeadId());
		}
		projectSettings.setCompanyName(projectSettingsDto.getCompanyName());
		projectSettings.setEmailId(projectSettingsDto.getEmailId());
		projectSettingsRepo.saveAndFlush(projectSettings);
		if (projectSettingsDto.getBillingEmail() != null && projectSettingsDto.getBillingName() != null) {
			ClientUpdateDto clientUpdateDto = new ClientUpdateDto();
			if(projectSettingsDto.getProjectId()!=null && projectSettingsDto.getProjectId()!=0L) {
				clientUpdateDto.setId(projectSettingsDto.getProjectId());
			}
			else {
				clientUpdateDto.setLeadId(projectSettingsDto.getLeadId());
			}
			clientUpdateDto.setBillingEmail(projectSettingsDto.getBillingEmail());
			clientUpdateDto.setBillingName(projectSettingsDto.getBillingName());
			feignLegacyInterface.updateClientDataAccount(authorization, clientUpdateDto);
		}
		return projectSettings;
	}

	public InvoiceProjectSettings getProjectSettings(String authorization, Long projectId) {
		InvoiceProjectSettings projectSettings = projectSettingsRepo.findByProjectId(projectId);
		return projectSettings;
	}

	public ProjectInvoice setBillingDetails(ProjectInvoiceDto projectInvoiceDto, ProjectInvoice projectInvoice) {
		PaymentMode paymentMode = paymentModeRepository.findById(projectInvoiceDto.getModeOfPaymentId());
		projectInvoice.setModeOfPaymentId(paymentMode.getId());
		InvoiceCycle billingCycle = invoiceCycleRepository.findById(projectInvoiceDto.getInvoiceCycleId());
		projectInvoice.setInvoiceCycleId(billingCycle.getId());
		PaymentTerms paymentTerms = paymentTermsRepository.findById(projectInvoiceDto.getPaymentTermsId());
		projectInvoice.setPaymentTermsId(paymentTerms.getId());
		projectInvoice.setPaymentCharges(projectInvoiceDto.getPaymentCharges());
		if (projectInvoiceDto.getCurrency().equals("DOLLAR")) 
			projectInvoice.setExchangeRate(1);
		else 
			projectInvoice.setExchangeRate(projectInvoiceDto.getExchangeRate());
		double cgst=0;
		double igst=0;
		double sgst=0;
		if(projectInvoiceDto.getCgst()!=null) 
			cgst= projectInvoiceDto.getCgst();
		if(projectInvoiceDto.getSgst()!=null) 
			sgst= projectInvoiceDto.getSgst();
		if(projectInvoiceDto.getIgst()!=null) 
			igst= projectInvoiceDto.getIgst();
		projectInvoice.setCgst(cgst);
		projectInvoice.setSgst(sgst);
		projectInvoice.setIgst(igst);
		if(projectInvoiceDto.getId()==0)
			projectInvoice = setInvoiceAmountData(projectInvoiceDto, projectInvoice);
		return projectInvoice;
	}

	private boolean saveInvoiceGeneratedData(ProjectInvoice invoice, ProjectInvoiceDto invoiceDto) {
		Double oldWaivedOffAmt = 0D;
		invoice.setMonth(invoiceDto.getMonth());
		invoice.setYear(invoiceDto.getYear());
		invoice.setPaymentCharges(invoiceDto.getPaymentCharges());
		InvoiceStatus invoiceStatus = invoiceStatusRepository.findById(invoiceDto.getInvoiceStatusId());
		
		invoice.setReceivedOn(invoiceDto.getReceivedOn());
		invoice.setComment(invoiceDto.getComment());
		if(invoice.getInvoiceStatus().equals(7L) && invoiceDto.getInvoiceStatusId() == 2){
			invoice.setAmount(invoice.getAmount() + invoiceDto.getWaivedOffAmount() );
			invoice.setWaivedOffAmount(0D);
			if (!invoice.getCurrency().equals("DOLLAR")) {
					
				invoice.setTaxableAmountInDollar(invoice.getTaxableAmountInDollar() + invoiceDto.getWaivedOffAmount() * invoice.getExchangeRate());
				invoice.setAmountInDollar(
						invoice.getAmountInDollar() +  invoiceDto.getWaivedOffAmount() * invoice.getExchangeRate());
				invoice.setAmountInRupee(invoice.getAmountInRupee() + invoiceDto.getWaivedOffAmount());

			} else {
				invoice.setTaxableAmountInDollar(invoice.getTaxableAmountInDollar() +  invoiceDto.getWaivedOffAmount());
				invoice.setAmountInDollar(invoice.getAmountInDollar() + invoiceDto.getWaivedOffAmount());

			}


		}
		if(invoiceDto.getInvoiceStatusId()!=7)
			invoice.setInvoiceStatus(invoiceStatus.getId());
		if (invoiceDto.getInvoiceStatusId()==7 && ((invoice.getWaivedOffAmount() != null 
				&& ((invoice.getWaivedOffAmount() + invoiceDto.getWaivedOffAmount()) <= invoice.getAmount())) ||
				(invoice.getWaivedOffAmount() == null && invoiceDto.getWaivedOffAmount() <= invoice.getAmount()))) {
			if (invoice.getWaivedOffAmount() != null){
				oldWaivedOffAmt = invoice.getWaivedOffAmount();

			}
				invoice.setWaivedOffAmount(invoiceDto.getWaivedOffAmount());
				invoice.setInvoiceStatus(invoiceDto.getInvoiceStatusId());
			if (invoiceDto.getWaivedOffAmount() != null && invoiceDto.getInvoiceStatusId() == 7) {
				invoice.setAmount(invoice.getAmount() + oldWaivedOffAmt - invoiceDto.getWaivedOffAmount());

				if (!invoice.getCurrency().equals("DOLLAR")) {
					
					invoice.setTaxableAmountInDollar(invoice.getTaxableAmountInDollar() + oldWaivedOffAmt* invoice.getExchangeRate()
							- invoiceDto.getWaivedOffAmount() * invoice.getExchangeRate());
					invoice.setAmountInDollar(
							invoice.getAmountInDollar() + oldWaivedOffAmt* invoice.getExchangeRate() - invoiceDto.getWaivedOffAmount() * invoice.getExchangeRate());

				} else {
					invoice.setTaxableAmountInDollar(invoice.getTaxableAmountInDollar() + oldWaivedOffAmt- invoiceDto.getWaivedOffAmount());
					invoice.setAmountInDollar(invoice.getAmountInDollar() + oldWaivedOffAmt - invoiceDto.getWaivedOffAmount());

				}
			}
		}
	
		if(invoiceDto.getInvoiceStatusId()==2) {
			invoice.setPayStatus(PayStatus.PAID);
			invoice.setPayingEntityName(invoiceDto.getPayingEntityName());
			invoice.setCurrencyRecevied(invoiceDto.getCurrencyRecevied());
			invoice.setRecievedAmount(invoiceDto.getRecievedAmount());	
		}
		
		else
			invoice.setPayStatus(PayStatus.UNPAID);
	
		if(invoice.getSplitType().equals("Split")) {
			ProjectInvoice splitInvoice = projectInvoiceRepository.findById(invoice.getConcernedSplitInvoice());
			splitInvoice.setInvoiceStatus(invoiceStatus.getId());
			splitInvoice.setReceivedOn(invoiceDto.getReceivedOn());
			if(invoiceDto.getInvoiceStatusId()==2) {
				splitInvoice.setPayStatus(PayStatus.PAID);
				splitInvoice.setPayingEntityName(invoiceDto.getPayingEntityName());
				splitInvoice.setCurrencyRecevied(invoiceDto.getCurrencyRecevied());
				splitInvoice.setRecievedAmount(invoiceDto.getRecievedAmount());
			}
			else
				splitInvoice.setPayStatus(PayStatus.UNPAID);
			
			try {
				projectInvoiceRepository.saveAndFlush(splitInvoice);
			}catch (Exception e) {
				throw e;
			}
		}
		try {
			projectInvoiceRepository.saveAndFlush(invoice);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Boolean updateComplianceStatus(Long invoiceId, String accessToken) {
		try {
			ProjectInvoice invoice = projectInvoiceRepository.findById(invoiceId);
			String comment = null;
			String compliantType = null;
			String data = null;
			int monthnum = loginUtilityService.getMonthNumber(invoice.getMonth());
			int year=Integer.parseInt(invoice.getYear()) ;
			
			Months monthEnum = Months.valueOf(invoice.getMonth().toUpperCase());
			IndirectCost monthIndirectCost = costRepository.findByYearAndIsDeletedAndMonth(invoice.getYear(),
					false, monthEnum);
			if(monthIndirectCost!=null) {
				List<ProjectInvoice> allInvoices = projectInvoiceRepository.findAllByProjectIdAndIsDeletedAndIsInternal(invoice.getProjectId(), false, false);
				List<ProjectInvoice> unpaidOrDueInvoices = scheduler.checkPendingOrUnPaidInvoices(invoice.getProjectId(),
						invoice.getMonth(), new Integer(invoice.getYear()), allInvoices);
				log.info("::: total unpaidOrDueInvoices :::"+unpaidOrDueInvoices);
				if(!unpaidOrDueInvoices.isEmpty()) {
					String invoiceIds="";
					int invoiceCount=unpaidOrDueInvoices.size();
					int i=1;
					for(ProjectInvoice inv:unpaidOrDueInvoices) {
						if(invoiceCount == i)
							invoiceIds=invoiceIds+inv.getId();
						else
							invoiceIds=invoiceIds+inv.getId()+",";
						i++;
					}
					log.info("Project Id {} has been marked as account compliant due to unpaid/Due invoices {} : ", invoice.getProjectId(), invoiceIds);
					comment="Marked non compliant by the system for Payment_Issues  as the invoices("+invoiceIds+") is/are Pending.";
					compliantType="Payment_Issues";
					data= invoiceIds;
					feignLegacyInterface.accountsCompliantStatusChange(invoice.getProjectId(), comment, false, compliantType, accessToken, data, "Cron");
				} else {
					if (monthIndirectCost != null) {
						Double margin = (Double) projectMarginService.getDirectCost(invoice.getProjectId(), monthnum, year, accessToken, true).get("marginPercentage");
						if (margin < 20) {
							log.info("ProjectId {} has been found for Direct Cost Compliant", invoice.getProjectId());
							comment = "Marked non compliant by the system for Margin_issues, as the current margin("+ margin + ") is less than 20%.";
							compliantType = "Margin_Issues";
							data = margin.toString();
							feignLegacyInterface.accountsCompliantStatusChange(invoice.getProjectId(), comment, false, compliantType, accessToken, data, "Cron");
						}
					}
					Map<String,Object> averageBillingCompliance=consolidateService.getBillingComplianceProjectData(accessToken, invoice.getProjectId(), monthnum, year);
				Boolean averageBillingNonCompliant=false;
				if(averageBillingCompliance!=null && averageBillingCompliance.containsKey("compliant") )
					averageBillingNonCompliant=new Boolean(averageBillingCompliance.get("compliant").toString());
				if(!averageBillingNonCompliant) {
					if(averageBillingCompliance!=null && averageBillingCompliance .containsKey("compliant")) {
						Double diffPerc=new Double(averageBillingCompliance.get("differencePerc").toString());
						Double compliancePerc=new Double(averageBillingCompliance.get("compliantPerc").toString());
						Double averageBillingRate= new Double(averageBillingCompliance.get("averageBillingRate").toString());
						Double expectedBillingRate = new Double(averageBillingCompliance.get("expectedBillingRate").toString());
						comment="Marked non compliant by the system for Avg_Billing_Rate, as the current billing Rate("+ diffPerc+") is less than "+compliancePerc+".";
						compliantType="Avg_Billing_Rate";
						data = diffPerc.toString()+"!"+(Math.round(averageBillingRate*100.00)/100.00)+"!"+(Math.round(expectedBillingRate*100.00)/100.00);
						feignLegacyInterface.accountsCompliantStatusChange(invoice.getProjectId(), comment, false, compliantType, accessToken, data, "Cron");
					}
				}
				else {
					comment="Marked account's compliant by System.";
					compliantType="";
					feignLegacyInterface.accountsCompliantStatusChange(invoice.getProjectId(), comment, true, compliantType, accessToken, data, "Cron");
				}
				}
				return true;
			}
			return false;
		} catch (Exception e) {
			log.debug("accountsCompliantStatusChange Exception : ");
			e.printStackTrace();
			return false;
		}
	}

	public Map<String, Object> getPaymentModeForPieChart(String accessToken, long fromDate, long toDate, String businessVertical, List<Map<String, Object>> allProjectData) {
		List<String> paymentModeTypes = new ArrayList<>();
		HashMap<String,Double> paymentModeMap=new HashMap<>();
		List<Double> invoiceAmount = new ArrayList<>();
		Map<String, Object> map = new HashMap<>();
		
		double amt = 0;
		List<ProjectInvoice> totalInvoices = new ArrayList<>();
		
		totalInvoices = projectInvoiceRepository.findAllByDateRangeFilterIsDeleted(new Date(fromDate),new Date (toDate), false);
		for (ProjectInvoice amounts : totalInvoices) {
			Map<String, Object> projectDetails = this.getProjectDetail(allProjectData, amounts.getProjectId());
			String projectBusinessVertical = "";
			if(projectDetails!=null) {
				if(projectDetails.containsKey("businessVertical") && projectDetails.get("businessVertical")!=null)
					projectBusinessVertical = projectDetails.get("businessVertical").toString();
				else 
					projectBusinessVertical = "Unassigned";
			}
			if(businessVertical.equals(projectBusinessVertical) || businessVertical.equals(""))
				amt = amt + amounts.getAmountInDollar();
		}
		
		List<PaymentMode> paymentModes = paymentModeRepository.findByIsArchived(false);
		List<Double> projectInvoicePercentage = new ArrayList<>();
		for (PaymentMode paymentMode : paymentModes) {
			double amount = 0;
			List<ProjectInvoice> invoices = new ArrayList<>();
			
			invoices = totalInvoices.stream().filter(inv -> inv.getModeOfPaymentId()!=null && inv.getModeOfPaymentId().equals(paymentMode.getId())).collect(Collectors.toList());			
			
			for (ProjectInvoice invoice : invoices) {
				Map<String, Object> projectDetails = this.getProjectDetail(allProjectData, invoice.getProjectId());
				String projectBusinessVertical = "";
				if(projectDetails!=null)
					projectBusinessVertical = projectDetails.get("businessVertical").toString();
				if(businessVertical.equals(projectBusinessVertical) || businessVertical.equals("")) 
				{
					amount = amount + invoice.getAmountInDollar();
				}
			}
			paymentModeMap.put(paymentMode.getPaymentModeType(), Math.round(((amount / amt) * 100)*100.00)/100.00);
			invoiceAmount.add(amount);
		}

		Map<String, Double> sortedByCount = paymentModeMap.entrySet().stream()
				.sorted((Map.Entry.<String, Double>comparingByValue().reversed()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		paymentModeTypes.addAll(sortedByCount.keySet());
		projectInvoicePercentage.addAll(sortedByCount.values());

		map.put("modeOfpayement", paymentModeTypes);
		map.put("invoicePercentage", projectInvoicePercentage);
		map.put("invoices", invoiceAmount);
		map.put("totalAmount", amt);
		map = getBankwiseDataForPieChart(totalInvoices, map, amt, allProjectData, businessVertical);
		return map;
	}
	
	private Map<String,Object> getBankwiseDataForPieChart(List<ProjectInvoice> totalInvoices, Map<String, Object> map,double amt, List<Map<String, Object>> allProjectData,String businessVertical){
		List<String> allBanks=new ArrayList<>();
		List<Double> bankWisePercentage=new ArrayList<>();
		HashMap<String,Double> bankDataMap=new HashMap<>();
		List<Double> bankAmount=new ArrayList<>();
		List<InvoiceBank> banks = bankRepository.findAllByIsArchived(false);
		for(InvoiceBank bank:banks) {
			double amount=0;
			List<ProjectInvoice> invoices = new ArrayList<>();
			invoices =totalInvoices.stream().filter(inv -> inv.getBank()!=null && inv.getBank().getId().equals(bank.getId())).collect(Collectors.toList());
			for(ProjectInvoice invoice:invoices) {
				Map<String, Object> projectDetails = this.getProjectDetail(allProjectData, invoice.getProjectId());
				String projectBusinessVertical = "";
				if(projectDetails!=null)
					projectBusinessVertical = projectDetails.get("businessVertical").toString();
				if(businessVertical.equals(projectBusinessVertical) || businessVertical.equals("")) {
				amount=amount+invoice.getAmountInDollar();
				}

			}
			bankDataMap.put(bank.getName(), Math.round(((amount / amt) * 100)*100.00)/100.00);
			bankAmount.add(amount);
		}
		Map<String, Double> sortedByCount = bankDataMap.entrySet().stream()
				.sorted((Map.Entry.<String, Double>comparingByValue().reversed()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		allBanks.addAll(sortedByCount.keySet());
		bankWisePercentage.addAll(sortedByCount.values());
		
		map.put("allBankNames", allBanks);
		map.put("bankWisePercentage", bankWisePercentage);
		map.put("bankAmount", bankAmount);
		return map;
	}


	//@Scheduled(cron = "0 0 8 * * *",zone="IST")
	public Boolean markInvoiceDisputed() {
		List<ProjectInvoice> invoices = projectInvoiceRepository.findAllByInvoiceStatusAndIsDeletedAndIsInternal(1L,
				false, false);
		for (ProjectInvoice invoice : invoices) {
			try {
				Date dueDate = DateUtils.addDays(invoice.getDueDate(), 60);
				if (dueDate.equals(new Date()) || dueDate.before(new Date())) {
					Map<String,Object> projectDetails=(Map<String, Object>) feignLegacyInterface.getProjectDescription(invoice.getProjectId(),"",null,null).get("data");
					String managerEmail="";
					String managerName="";
					String buOwnerMail="";
					if(projectDetails.get("managerName")!=null) {
						managerName = projectDetails.get("managerName").toString();
						managerEmail = projectDetails.get("managerEmail").toString();
					}
					if(projectDetails.get("buOwnerMail")!=null)
						buOwnerMail = projectDetails.get("buOwnerMail").toString();
					invoice.setInvoiceStatus(5L);
					projectInvoiceRepository.save(invoice);
					Context context = new Context();
					context.setVariable("userName", managerName);
					context.setVariable("invoiceId", invoice.getId());
					context.setVariable("responseUrl", "https://"+environmentUrl+"/#/legal-team-action?invoiceId="+invoice.getId());
					context.setVariable("currentYear", LocalDate.now().getYear());
					String subject = invoice.getProject() + " || " + invoice.getId() + " || Disputed";
					List<String> ccList = new ArrayList<>();
					ccList.add(accountsMail);
					ccList.add(legalMail);
					if(!buOwnerMail.equals(""))
						ccList.add(buOwnerMail);
					if(!managerEmail.equals(""))
						mailService.sendScheduleHtmlMailWithCc(managerEmail, subject, context, "Mark-Invoice-Disputed", ccList.stream().toArray(String[]::new));
					else {
						if(!buOwnerMail.equals(""))
							mailService.sendScheduleHtmlMail(buOwnerMail, subject, context, "Mark-Invoice-Disputed");
					}
				}
			} catch (Exception e) {
				log.debug("markInvoiceDisputed Exception : "+e.toString());	
				return false;
			}
		}
		return true;
	}
	
	public String saveDisputedCommentForLegalAction(Long invoiceId, String comment) {
		ProjectInvoice invoice=projectInvoiceRepository.findById(invoiceId);
		if(invoice!=null) {
			if (invoice.getDisputedAutomatedComment() == null) {
				invoice.setDisputedAutomatedComment(comment);
				invoice.setBuHeadComment(comment);
				try {
					projectInvoiceRepository.save(invoice);
					return ConstantUtility.SAVED_SUCCESSFULLY;
				} catch (Exception e) {
					return "Unable to save due to " + e.getMessage();
				}
			}
			else
				return ConstantUtility.ACTION_ALREADY_TAKEN ;
		}
		return ConstantUtility.INVOICE_NOT_EXISTS;
	}
	
	public Boolean sendMailOnPaidInvoice(String accessToken, ProjectInvoice invoice) {
		InvoiceStatus invoiceStatus = invoiceStatusRepository.findById(invoice.getInvoiceStatus());
		Boolean result = false;
		UserModel currentUser = validator.tokenbValidate(accessToken);
		Map<String, Object> projectDetails = (Map<String, Object>) feignLegacyInterface.getProjectDescription(invoice.getProjectId(),"",null,null).get("data");

		String managerEmail = "";
		if (projectDetails.get("managerName") != null) {
			managerEmail = projectDetails.get("managerEmail").toString();
		}

		Context context = new Context();
		context.setVariable("managerName", invoice.getManager());
		context.setVariable("invoiceId", invoice.getId());
		context.setVariable("projectName", invoice.getProject());
		context.setVariable("previousInvoiceStatus", invoiceStatus.getStatusName());
		context.setVariable("currentDate", LocalDate.now());
		context.setVariable("currentUserName", currentUser.getEmpName());
		context.setVariable("amtInInr", invoice.getAmountInRupee());
		context.setVariable("amount", invoice.getAmount());
		context.setVariable("currency", invoice.getCurrency());
		context.setVariable("clientName", invoice.getClientName());
		context.setVariable("createdDate", new SimpleDateFormat("dd-MM-yyyy").format(invoice.getCreatedDate()));
		String subject = "Invoice INV-" + invoice.getId() + " || " + invoice.getProject() + " || " + "Paid";
		String[] ccArray = new String[1];
		ccArray[0] = accountsMail;
		try {
			if (!managerEmail.equals("")) {
				mailService.sendScheduleHtmlMailWithCc(managerEmail, subject, context, "Mark-Invoice-Paid", ccArray);
				result = true;
			}

		} catch (Exception ex) {
			result = false;
		}
		return result;
	}
	
	public List<Map<String, Object>> getPendingInvoiceData(Long projectId) {
		Double invoiceAmountInDollar = 0.0;
		List<Map<String, Object>> responseList = new ArrayList<>();
		
		List<ProjectInvoice> invoices = new ArrayList<>();
		List<InvoiceStatus> invoiceStatusList = invoiceStatusRepository.findAll();
		invoices = projectInvoiceRepository.findAllByProjectIdAndInvoiceStatusAndIsDeleted(projectId, 1L, false);
		for (ProjectInvoice invoice : invoices) {

			if (Integer.valueOf(calculateAging(invoice, invoiceStatusList)) > 10) {
				Map<String, Object> invoiceMap = new HashMap<>();
				invoiceAmountInDollar = invoice.getAmountInDollar();
				invoiceMap.put("id", invoice.getId());
				invoiceMap.put("invoiceNumber", "INV-" + invoice.getId());
				invoiceMap.put("invoiceAmountInDollar", Math.round(invoiceAmountInDollar * 100.0) / 100.0);
				invoiceMap.put("dueDate", new SimpleDateFormat(ConstantUtility.DATE_FORMAT)
						.format(new Date(invoice.getDueDate().getTime())));
				invoiceMap.put("createDate", new SimpleDateFormat(ConstantUtility.DATE_FORMAT)
						.format(new Date(invoice.getBillingDate().getTime())));
				invoiceMap.put(ConstantUtility.AGING, calculateAging(invoice, invoiceStatusList));
				invoiceMap.put("projectId", projectId);
				responseList.add(invoiceMap);
			}
		}
		return responseList;

	}
	
	public Double getPaymentCharges(String currency, ProjectInvoice invoice) {
		int monthNum = indirectCostService.getMonthNumber(invoice.getMonth());

		Double effectivePayment = 0D;
			Double exchangeRate = invoice.getExchangeRate();
			if (invoice.getCurrency().equals(currency))
				effectivePayment = invoice.getPaymentCharges();
			if (invoice.getCurrency().equals("RUPEE") && currency.equals("DOLLAR"))
				effectivePayment = invoice.getPaymentCharges() * exchangeRate;
			if (invoice.getCurrency().equals("DOLLAR") && currency.equals("RUPEE")) {
				Double dollarexchangeCost = dollarCostService.getAverageDollarCost(monthNum,
						Integer.parseInt(invoice.getYear()));
				effectivePayment = invoice.getPaymentCharges() * dollarexchangeCost;
			}
			if ((!invoice.getCurrency().equals("DOLLAR") && !invoice.getCurrency().equals("RUPEE"))
					&& currency.equals("DOLLAR"))
				effectivePayment = invoice.getPaymentCharges() * exchangeRate;
			if ((!invoice.getCurrency().equals("DOLLAR") && !invoice.getCurrency().equals("RUPEE"))
					&& currency.equals("RUPEE")) {
				Double paymentExch = invoice.getPaymentCharges() * exchangeRate;
				Double dollarexchangeCost = dollarCostService.getAverageDollarCost(monthNum,
						Integer.parseInt(invoice.getYear()));
				effectivePayment = paymentExch * dollarexchangeCost;
			}
		return effectivePayment;
	}

	public Map<String, Object> getCountryWisePieChart(String accessToken, long fromDate, long toDate,
			List<Map<String, Object>> allProjectData, String businessVertical) {
		Map<String,Double> mapSortToBe=new HashMap<>();

		List<ProjectInvoice> invoices = projectInvoiceRepository.findAllByDateRangeFilterIsDeletedAndIsInternal(new Date(fromDate), new Date(toDate), false, false);
		List<String> placeOfSupply = new ArrayList<>();
		List<String> placeOfSupplySorted = new ArrayList<>();

		Map<String, Object> map = new HashMap<>();
		List<Double> amountInPerc = new ArrayList<>();
		List<Double> amount = new ArrayList<>();
		Double nonPlaceAmount = 0.0;

		for (ProjectInvoice data : invoices) {
			if (data.getPlaceOfSupply() != null)
				placeOfSupply.add(data.getPlaceOfSupply());
		}
		placeOfSupply = placeOfSupply.stream().distinct().collect(Collectors.toList());
		Double totalAmount = 0.0;

		for (String it : placeOfSupply) {
			Double amount1 = 0.0;
			for (ProjectInvoice invoice : invoices) {

				Map<String, Object> projectDetails = this.getProjectDetail(allProjectData, invoice.getProjectId());
				String projectBusinessVertical = "";
				if (projectDetails != null)
					projectBusinessVertical = projectDetails.get("businessVertical").toString();
				if (businessVertical.equals(projectBusinessVertical) || businessVertical.equals("")) {
					if (invoice.getPlaceOfSupply() != null) {
						if (invoice.getPlaceOfSupply().equals(it)) {
							amount1 = amount1 + invoice.getAmountInDollar();
							totalAmount = totalAmount + invoice.getAmountInDollar();
						}
					} else {
						nonPlaceAmount = nonPlaceAmount + invoice.getAmountInDollar();
						totalAmount = totalAmount + invoice.getAmountInDollar();
					}
				}
			}
			mapSortToBe.put(it, amount1);
		}
		mapSortToBe.put("NA", nonPlaceAmount);
		Map<String, Double> sortedByValue = mapSortToBe.entrySet().stream()
				.sorted((Map.Entry.<String, Double>comparingByValue().reversed()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		placeOfSupplySorted.addAll(sortedByValue.keySet());
		amount.addAll(sortedByValue.values());
		for (Double amt : amount) {
			amountInPerc.add(Math.round(((amt / totalAmount) * 100) * 100.00) / 100.00);
		}
		map.put("invoiceAmount", amountInPerc);
		map.put("placeOfSupply", placeOfSupplySorted);
		return map;
	}

	public List<Map<String, Object>> getProjectWiseTotalRevenue(String accessToken, String month, String year) {
		List<Map<String, Object>> response = new ArrayList<>();
		List<ProjectInvoice> totalInvoices ;
		
		if(month.equals("") && !year.equals("")) {
			totalInvoices = projectInvoiceRepository.findAllByYearAndIsDeletedAndIsInternalAndInvoiceStatusNot( year, false, false,6L);

		}
		else if(!month.equals("") && !year.equals("")){
			totalInvoices = projectInvoiceRepository
					.findAllByMonthAndYearAndIsDeletedAndIsInternalAndInvoiceStatusNot(month, year, false, false,6L);
		}
		else {
			totalInvoices = projectInvoiceRepository.findAllByIsDeletedAndIsInternalAndInvoiceStatusNot(false, false,6L);

		}
		List<Map<String,Object>> projectData=new ArrayList<>();
		for(ProjectInvoice total:totalInvoices){
			Map<String,Object> res=new HashMap<>();
			res.put("projectId", total.getProjectId());
			res.put("projectName", total.getProject());
			projectData.add(res);
		}
		projectData=projectData.stream().distinct().collect(Collectors.toList());
		for (Map<String,Object> project : projectData) {
			Double invoiceAmountInDollar = 0.0;
			Map<String, Object> invoiceMap = new HashMap<>();
			List<ProjectInvoice> invoices = totalInvoices.stream()
					.filter(inv -> inv.getProjectId().toString().equals(project.get("projectId").toString()))
					.collect(Collectors.toList());
			invoiceAmountInDollar = invoices.stream().mapToDouble(invoice -> invoice.getAmountInDollar()).sum();
			invoiceMap.put("invoiceAmountInDollar",Math.round(invoiceAmountInDollar * 100.0) / 100.0 );
			invoiceMap.put("projectName", project.get("projectName"));
			invoiceMap.put("projectId", project.get("projectId"));

			response.add(invoiceMap);
		}
		return response;
	}

	public List<Map<String, Object>> getBuWiseTotalRevenue(String accessToken, List<String> month, String year, String bu, String filter) {
		if(filter.equals("modeOfPayment"))
			return getModeOfPaymentWiseTotalRevenue(accessToken, month, year, bu);
		else if(filter.equals("country"))
			return getCountryWiseTotalRevenue(accessToken, month, year, bu);
		else
			return getClinetwiseTotalRevenue(accessToken, month, year, bu);
	}
	
	private List<Map<String, Object>> getClinetwiseTotalRevenue(String accessToken, List<String> month, String year, String bu){
		List<Map<String, Object>> response = new ArrayList<>();
		List<ProjectInvoice> totalInvoicesMonthly = projectInvoiceRepository.findAllByMonthInAndYearAndIsDeletedAndIsInternal(month, year, false, false);
		Double totalmonthlyAmount = totalInvoicesMonthly.stream().mapToDouble(invoice -> invoice.getAmountInDollar()).sum();
		
		List<ProjectInvoice> totalInvoiceTillDate = projectInvoiceRepository.findAllByIsDeletedAndIsInternal(false, false);

		if(!totalInvoicesMonthly.isEmpty()){
			List<Map<String, Object>> clientDataList = (List<Map<String, Object>>) feignLegacyInterface.getClientWiseData(accessToken,bu).get("data");
			Map<String,Object> listOfProjects= (Map<String,Object>) feignLegacyInterface.getBuWiseProjectIdsList(accessToken,bu).get("data");
	
			List<Object> buWiseProjectIds = (List<Object>) listOfProjects.get("map");
			buWiseProjectIds = buWiseProjectIds.stream().distinct().collect(Collectors.toList());
			Double monthlyRevenueBuWise = 0.0;

			for(Object id:buWiseProjectIds) {
				List<ProjectInvoice> monthlyInvoicesBuWise=totalInvoicesMonthly.stream().filter(inv-> Long.toString(inv.getProjectId()).equals((id.toString()))).collect(Collectors.toList());
				monthlyRevenueBuWise=monthlyRevenueBuWise+monthlyInvoicesBuWise.stream().collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
			}
			for (Map<String, Object> clientData : clientDataList) {
				Map<String, Object> invoiceMap = new HashMap<>();
				invoiceMap.put("clientId", clientData.get("clientId"));
				invoiceMap.put("primaryContactName", clientData.get("primaryContactName"));
				invoiceMap.put("clientType", clientData.get("clientType") != null ? clientData.get("clientType") : "NA");
				if (clientData.get("clientType") != null && clientData.get("clientType").toString().equalsIgnoreCase("Organization")) {
					invoiceMap.put("clientOrOrganization", ((clientData.get("organisation")!=null) && (!clientData.get("organisation").toString().equals("")))?clientData.get("organisation").toString() : clientData.get("clientName"));
				} else {
					invoiceMap.put("clientOrOrganization", clientData.get("clientName"));
				}
				List<Map<String, Object>> clientProjects = (List<Map<String, Object>>) clientData.get("projectsData");
				Double monthlyRevenueClient = 0.0;
				Double monthlyRevenueClientInr = 0.0;
				Double lifeTimeRevenueOfClient = 0.0;
				List<ProjectInvoice> monthlyRevenue=new ArrayList<>();
				List<Map<String,Object>> projectDataList=new ArrayList<>();
				for (Map<String, Object> pro : clientProjects) {
					Map<String,Object> projectMap=new HashMap<>();
					List<ProjectInvoice> monthlyInvoices=totalInvoicesMonthly.stream().filter(inv-> Long.toString(inv.getProjectId()).equals(pro.get("projectId").toString())).collect(Collectors.toList());
					monthlyRevenueClient=monthlyRevenueClient+monthlyInvoices.stream().collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar))); 
					monthlyRevenue.addAll(monthlyInvoices);
			 
					monthlyRevenueClientInr=monthlyRevenueClientInr+totalInvoicesMonthly.stream().filter(inv-> Long.toString(inv.getProjectId()).equals(pro.get("projectId").toString())).collect((Collectors.summingDouble(ProjectInvoice::getAmountInRupee)));
					lifeTimeRevenueOfClient =lifeTimeRevenueOfClient+totalInvoiceTillDate.stream().filter(inv-> Long.toString(inv.getProjectId()).equals(pro.get("projectId").toString())).collect((Collectors.summingDouble(ProjectInvoice::getAmountInDollar)));
					projectMap.put("projectName", pro.get("projectName"));
					projectMap.put("projectId", pro.get("projectId"));
					projectDataList.add(projectMap);
				}
				invoiceMap.put("monthlyRevenueClientInr", Math.round(monthlyRevenueClientInr * 100.0) / 100.0);
				invoiceMap.put("monthlyRevenueClient", Math.round(monthlyRevenueClient * 100.0) / 100.0);
				invoiceMap.put("lifeTimeRevenueClient",Math.round(lifeTimeRevenueOfClient * 100.0) / 100.0);
				if (bu != null && !bu.isEmpty()){
					invoiceMap.put("buOrOrgContrPer",Math.round(((monthlyRevenueClient /monthlyRevenueBuWise) * 100) * 100.00) / 100.00);
				}
				else {
					invoiceMap.put("buOrOrgContrPer",Math.round(((monthlyRevenueClient / totalmonthlyAmount) * 100) * 100.00) / 100.00);
				}
				invoiceMap.put("projects", projectDataList);
				if(!monthlyRevenue.isEmpty())
					response.add(invoiceMap);
			}	
			Collections.sort(response,(p1, p2)->new Double(p1.get("buOrOrgContrPer").toString()).compareTo(new Double(p2.get("buOrOrgContrPer").toString())));
			Collections.reverse(response);	
		}
		return response;
	}
	
	private List<Map<String, Object>> getModeOfPaymentWiseTotalRevenue(String accessToken, List<String> month, String year, String businessVertical){
		List<Map<String, Object>> response = new ArrayList<>();
		List<PaymentMode> paymentModes = paymentModeRepository.findByIsArchived(false);
		List<Map<String, Object>> allProjectData = this.getProjectDetails(accessToken);
		List<ProjectInvoice> totalInvoicesMonthly = projectInvoiceRepository.findAllByMonthInAndYearAndIsDeleted(month, year, false);
		List<ProjectInvoice> totalInvoiceTillDate = projectInvoiceRepository.findAllByIsDeletedAndIsInternal(false, false);
		
		Double totalmonthlyAmount =0.0;
		for (ProjectInvoice projectInvoice : totalInvoicesMonthly) {
			Map<String, Object> projectDetails = this.getProjectDetail(allProjectData, projectInvoice.getProjectId());
			String projectBusinessVertical = "";
			if(projectDetails!=null) {
				if(projectDetails.containsKey("businessVertical") && projectDetails.get("businessVertical")!=null)
					projectBusinessVertical = projectDetails.get("businessVertical").toString();
				else 
					projectBusinessVertical = ConstantUtility.UNASSIGNED;
			}
			if(businessVertical.equals(projectBusinessVertical) || businessVertical.equals(""))
				totalmonthlyAmount = totalmonthlyAmount + projectInvoice.getAmountInDollar();
		}
		if(!totalInvoicesMonthly.isEmpty()) {
			for (PaymentMode paymentMode : paymentModes) {
				Map<String, Object> map = new HashMap<>();
				Double monthlyRevenue = 0.0;
				Double lifeTimeRevenue = 0.0;
				List<Map<String,Object>> projectDataList=new ArrayList<>();
				List<ProjectInvoice> totalPaymentModeWiseMonthlyInvoices = totalInvoicesMonthly.stream().filter(inv->inv.getModeOfPaymentId().equals(paymentMode.getId())).collect(Collectors.toList());
				for (ProjectInvoice invoice : totalPaymentModeWiseMonthlyInvoices) {
					Map<String,Object> projectMap=new HashMap<>();
					Map<String, Object> projectDetails = this.getProjectDetail(allProjectData, invoice.getProjectId());
					String projectBusinessVertical = "";
					if(projectDetails!=null)
						projectBusinessVertical = projectDetails.get("businessVertical").toString();
					if(businessVertical.equals(projectBusinessVertical) || businessVertical.equals("")) 
					{
						monthlyRevenue = monthlyRevenue + invoice.getAmountInDollar();
						projectMap.put("projectName", invoice.getProject());
						projectMap.put("projectId", invoice.getProjectId());
						projectDataList.add(projectMap);
					}
				}
				projectDataList = projectDataList.stream().distinct().collect(Collectors.toList());
				List<ProjectInvoice> paymentModeWiseTillDateTotalInvoices =totalInvoiceTillDate.stream().filter(inv->inv.getModeOfPaymentId().equals(paymentMode.getId())).collect(Collectors.toList());
				for (ProjectInvoice projectInvoice : paymentModeWiseTillDateTotalInvoices) {
					Map<String, Object> projectDetails = this.getProjectDetail(allProjectData, projectInvoice.getProjectId());
					String projectBusinessVertical = "";
					if(projectDetails!=null) {
						if(projectDetails.containsKey("businessVertical") && projectDetails.get("businessVertical")!=null)
							projectBusinessVertical = projectDetails.get("businessVertical").toString();
						else 
							projectBusinessVertical = ConstantUtility.UNASSIGNED;
					}
					if(businessVertical.equals(projectBusinessVertical) || businessVertical.equals(""))
						lifeTimeRevenue = lifeTimeRevenue + projectInvoice.getAmountInDollar();
				}
				map.put("modeOfPayment", paymentMode.getPaymentModeType());
				map.put("projects", projectDataList);
				map.put("monthlyRevenue", Math.round(monthlyRevenue * 100.0) / 100.0);
				map.put("lifeTimeRevenue",Math.round(lifeTimeRevenue * 100.0) / 100.0);
				map.put("buOrOrgContrPer", Math.round(((monthlyRevenue / totalmonthlyAmount) * 100) * 100.00) / 100.00);
				response.add(map);
				Collections.sort(response,(p1, p2)->new Double(p1.get("buOrOrgContrPer").toString()).compareTo(new Double(p2.get("buOrOrgContrPer").toString())));
				Collections.reverse(response);	
			}
			return response;
		}
		return response;
	}
	
	private List<Map<String, Object>> getCountryWiseTotalRevenue(String accessToken, List<String> month, String year, String businessVertical){
		List<Map<String, Object>> response = new ArrayList<>();
		List<String> countryList = new ArrayList<>();
		List<ProjectInvoice> totalInvoicesMonthly = projectInvoiceRepository.findAllByMonthInAndYearAndIsDeletedAndIsInternal(month, year, false, false);
		Double totalmonthlyAmount = totalInvoicesMonthly.stream().mapToDouble(invoice -> invoice.getAmountInDollar()).sum();
		
		List<ProjectInvoice> totalInvoiceTillDate = projectInvoiceRepository.findAllByIsDeletedAndIsInternal(false, false);
		countryList = totalInvoicesMonthly.stream().map(inv -> inv.getPlaceOfSupply()).distinct().collect(Collectors.toList()).stream().filter(list->list!=null).collect(Collectors.toList());
		
		List<Map<String,Object>> nonPlaceProjectDataList=new ArrayList<>();
		Double totalNonPlaceMonthlyAmount = 0.0;
		Double totalNonPlaceAmountTillDate = 0.0;
		
		Double totalmonthlyRevenueBuWise = 0.0;
		List<ProjectInvoice> monthlyInvoicesBuWise = new ArrayList<>();
		List<ProjectInvoice> tillDateInvoicesBuWise = new ArrayList<>();
		
		if (businessVertical != null && !businessVertical.isEmpty()) {
			Map<String,Object> listOfProjects= (Map<String,Object>) feignLegacyInterface.getBuWiseProjectIdsList(accessToken,businessVertical).get("data");
			List<Object> buWiseProjectIds = (List<Object>) listOfProjects.get("map");
			buWiseProjectIds = buWiseProjectIds.stream().distinct().collect(Collectors.toList());
		
			for(Object id:buWiseProjectIds) {
			monthlyInvoicesBuWise.addAll(totalInvoicesMonthly.stream().filter(inv-> Long.toString(inv.getProjectId()).equals((id.toString()))).collect(Collectors.toList()));
			tillDateInvoicesBuWise.addAll(totalInvoiceTillDate.stream().filter(inv-> Long.toString(inv.getProjectId()).equals((id.toString()))).collect(Collectors.toList()));
			}
			totalmonthlyRevenueBuWise = totalmonthlyRevenueBuWise + monthlyInvoicesBuWise.stream().mapToDouble(invoice -> invoice.getAmountInDollar()).sum();
			nonPlaceProjectDataList = this.mapProjectDataList(monthlyInvoicesBuWise.stream().filter(inv->inv.getPlaceOfSupply()==null).collect(Collectors.toList()));
			totalNonPlaceMonthlyAmount = totalNonPlaceMonthlyAmount +  monthlyInvoicesBuWise.stream().filter(inv->inv.getPlaceOfSupply()==null).mapToDouble(invoice -> invoice.getAmountInDollar()).sum();
			totalNonPlaceAmountTillDate = totalNonPlaceAmountTillDate + tillDateInvoicesBuWise.stream().filter(inv->inv.getPlaceOfSupply()==null).mapToDouble(invoice -> invoice.getAmountInDollar()).sum();
			countryList = monthlyInvoicesBuWise.stream().map(inv -> inv.getPlaceOfSupply()).distinct().collect(Collectors.toList()).stream().filter(list->list!=null).collect(Collectors.toList());
			
		}
		else {
			nonPlaceProjectDataList =this.mapProjectDataList(totalInvoicesMonthly.stream().filter(inv->inv.getPlaceOfSupply()==null).collect(Collectors.toList()));
			totalNonPlaceMonthlyAmount = totalNonPlaceMonthlyAmount + totalInvoicesMonthly.stream().filter(inv->inv.getPlaceOfSupply()==null).mapToDouble(invoice -> invoice.getAmountInDollar()).sum();
			totalNonPlaceAmountTillDate = totalNonPlaceAmountTillDate + totalInvoiceTillDate.stream().filter(inv->inv.getPlaceOfSupply()==null).mapToDouble(invoice -> invoice.getAmountInDollar()).sum();
		}
		if(!totalInvoicesMonthly.isEmpty()) {
			for(String  country :countryList) {
				List<Map<String,Object>> projectDataList=new ArrayList<>();
				Map<String, Object> map = new HashMap<>();
				Double monthlyRevenue = 0.0;
				Double lifeTimeRevenue = 0.0;
				Double monthlyRevenueBuWise = 0.0;
				Double lifeTimeRevenueBuWise = 0.0;
				
				map.put("countryName", country);
				if (businessVertical != null && !businessVertical.isEmpty()) {
					List<ProjectInvoice> totalBuWiseCountryWiseMonthlyInvoices = monthlyInvoicesBuWise.stream().filter(inv->inv.getPlaceOfSupply() != null && inv.getPlaceOfSupply().equals(country)).collect(Collectors.toList());
					projectDataList = this.mapProjectDataList(totalBuWiseCountryWiseMonthlyInvoices);
					monthlyRevenueBuWise =  monthlyRevenueBuWise + totalBuWiseCountryWiseMonthlyInvoices.stream().mapToDouble(invoice -> invoice.getAmountInDollar()).sum();
					List<ProjectInvoice> countryWiseBuWiseTillDateTotalInvoices = tillDateInvoicesBuWise.stream().filter(inv->inv.getPlaceOfSupply() != null && inv.getPlaceOfSupply().equals(country)).collect(Collectors.toList());
					lifeTimeRevenueBuWise = lifeTimeRevenueBuWise + countryWiseBuWiseTillDateTotalInvoices.stream().mapToDouble(invoice -> invoice.getAmountInDollar()).sum();
					map.put("projects", projectDataList);
					map.put("monthlyRevenue", Math.round(monthlyRevenueBuWise * 100.0) / 100.0);
					map.put("lifeTimeRevenue",Math.round(lifeTimeRevenueBuWise * 100.0) / 100.0);
					map.put("buOrOrgContrPer", Math.round(((monthlyRevenueBuWise / totalmonthlyRevenueBuWise) * 100) * 100.00) / 100.00);
					response.add(map);	
				}
				else {
					List<ProjectInvoice> totalCountryWiseMonthlyInvoices = totalInvoicesMonthly.stream().filter(inv->inv.getPlaceOfSupply() != null && inv.getPlaceOfSupply().equals(country)).collect(Collectors.toList());
					projectDataList = this.mapProjectDataList(totalCountryWiseMonthlyInvoices);
					monthlyRevenue = monthlyRevenue + totalCountryWiseMonthlyInvoices.stream().mapToDouble(invoice -> invoice.getAmountInDollar()).sum();
					List<ProjectInvoice> countryWiseTillDateTotalInvoices =totalInvoiceTillDate.stream().filter(inv->inv.getPlaceOfSupply() != null && inv.getPlaceOfSupply().equals(country)).collect(Collectors.toList());
					lifeTimeRevenue = lifeTimeRevenue + countryWiseTillDateTotalInvoices.stream().mapToDouble(invoice -> invoice.getAmountInDollar()).sum();
					map.put("projects", projectDataList);
					map.put("monthlyRevenue", Math.round(monthlyRevenue * 100.0) / 100.0);
					map.put("lifeTimeRevenue",Math.round(lifeTimeRevenue * 100.0) / 100.0);
					map.put("buOrOrgContrPer", Math.round(((monthlyRevenue / totalmonthlyAmount) * 100) * 100.00) / 100.00);
					response.add(map);
				}	
			}
			Map<String, Object> map = new HashMap<>();
			map.put("countryName","NA");
			map.put("projects", nonPlaceProjectDataList);
			map.put("monthlyRevenue",Math.round(totalNonPlaceMonthlyAmount * 100.0) / 100.0 );
			map.put("lifeTimeRevenue",Math.round(totalNonPlaceAmountTillDate * 100.0) / 100.0 );
			map.put("buOrOrgContrPer", Math.round(((totalNonPlaceMonthlyAmount / (businessVertical!=null && !businessVertical.isEmpty() ? totalmonthlyRevenueBuWise :totalmonthlyAmount)) * 100) * 100.00) / 100.00);
			response.add(map);
			Collections.sort(response,(p1, p2)->new Double(p1.get("buOrOrgContrPer").toString()).compareTo(new Double(p2.get("buOrOrgContrPer").toString())));
			Collections.reverse(response);
			return response;
		}
		return response;
	}
	
	private  List<Map<String, Object>> mapProjectDataList(List<ProjectInvoice> monthlyInvoices) {
		if (!monthlyInvoices.isEmpty()) {
			return monthlyInvoices.stream().map(invoice->{
				Map<String,Object> projectMap=new HashMap<>();
				projectMap.put("projectName", invoice.getProject());
				projectMap.put("projectId", invoice.getProjectId());
				return projectMap;
			}).distinct().collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@CacheEvict(cacheNames = "getInternalInvoices", allEntries = true)
	public void flushgetInternalInvoices() {
	}
	
	public List<Map<String, Object>> getManagerWiseQuarterlyRevenue(String quarter, String year) {
		List<Map<String, Object>> response = new ArrayList<>();
		if(!quarterNotOpenedYet(quarter, year)) {
			List<ProjectInvoice> quarterlyInvoices = getInvoices(quarter, year);
			String prevQuarter = getPreviousQuarter(quarter);
			if(!prevQuarter.equals("NA") && prevQuarter.equals("Q4")) {
				year = Integer.toString((Integer.parseInt(year.toString())-1));
			}
			List<ProjectInvoice> previousQuarterlyInvoices = getInvoices(prevQuarter, year);
			List<Long> managerList = new ArrayList<>();
			List<String> manager = new ArrayList<>();
			managerList = quarterlyInvoices.stream().map(inv -> inv.getManagerId()).distinct().collect(Collectors.toList()).stream().filter(list->list!=null).collect(Collectors.toList());
			manager= quarterlyInvoices.stream().map(inv -> inv.getManager()).distinct().collect(Collectors.toList()).stream().filter(list->list!=null).collect(Collectors.toList());
			if(!quarterlyInvoices.isEmpty()) {
				for(Long managerId : managerList){
					Map<String, Object> map = new HashMap<>();
					Double quarterlyRevenue = 0.0D;
					Double previousQuarterlyRevenue = 0.0D;
					Double quarterlyDisputedAmt =0.0D;
					Double revenueGrowthPercentage = 0.0D;
					List<ProjectInvoice> invoices = quarterlyInvoices.stream().filter(inv->inv.getManagerId()!= 0L && inv.getManagerId()==managerId.longValue() && inv.getInvoiceStatus()!=6L).collect(Collectors.toList());
					List<ProjectInvoice> disputedInvoices = invoices.stream().filter(inv->inv.getInvoiceStatus() == 5L).collect(Collectors.toList());
					quarterlyRevenue = quarterlyRevenue + invoices.stream().mapToDouble(invoice -> invoice.getAmountInDollar()).sum();
					quarterlyDisputedAmt = quarterlyDisputedAmt + disputedInvoices.stream().mapToDouble(invoice -> invoice.getAmountInDollar()).sum();
					List<ProjectInvoice> previousInvoices = previousQuarterlyInvoices.stream().filter(inv->inv.getManagerId()!= 0L && inv.getManagerId()==managerId.longValue() && inv.getInvoiceStatus()!=6L).collect(Collectors.toList());
					previousQuarterlyRevenue = previousQuarterlyRevenue + previousInvoices.stream().mapToDouble(invoice -> invoice.getAmountInDollar()).sum();
					if(previousQuarterlyRevenue!=0.0)
						revenueGrowthPercentage = Math.round(((( quarterlyRevenue - previousQuarterlyRevenue )/previousQuarterlyRevenue)*100 ) * 100.00) / 100.00;
					else
						revenueGrowthPercentage=100.00;
					map.put("managerId", managerId);
					map.put("quarterlyDisputedAmt",Math.round(quarterlyDisputedAmt * 100.0) / 100.0);
					map.put("revenueGrowthPercentage", revenueGrowthPercentage);
					response.add(map);
				}
				return response;
			}
			return response;
		}
		return response;
	}
	
	private List<ProjectInvoice> getInvoices(String quarter, String year) {
		Map<String, List<Month>> monthsPerQuarter = Arrays.stream(Month.values())
				.collect(Collectors.groupingBy(DateTimeFormatter.ofPattern("QQQ", Locale.ENGLISH)::format));
		List<String> months = monthsPerQuarter.get(quarter).stream().map(Object::toString).collect(Collectors.toList());
		return projectInvoiceRepository.findAllByMonthInAndYearAndIsDeletedAndIsInternal(months, year, false, false);
	}

	
	private boolean quarterNotOpenedYet(String quarter, String year) {
		DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		Date startDate = null;
		Date now = new Date();
		try {
			switch (quarter) {

			case "Q1":
				startDate = formatter.parse("25-03-" + year);
				break;
			case "Q2":
				startDate = formatter.parse("25-06-" + year);
				break;
			case "Q3":
				startDate = formatter.parse("25-09-" + year);
				break;
			case "Q4":
				startDate = formatter.parse("25-12-" + year);
				break;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (now.before(startDate))
			return true;
		else
			return false;
	}
	
	private String getPreviousQuarter(String quarter) {
		String prevQuarter = "NA";
		switch (quarter) {
		case "Q1":
			prevQuarter = "Q4";
			break;
		case "Q2":
			prevQuarter = "Q1";
			break;
		case "Q3":
			prevQuarter = "Q2";
			break;
		case "Q4":
			prevQuarter = "Q3";
			break;
		}
		return prevQuarter;
	}

	public List<Map<String, Object>> getClientWiseCountryList(String accessToken, String businessVertical) {
	    try {
	        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	        // Fetch clientDataList using feignLegacyInterface
	        List<Map<String, Object>> clientDataList = (List<Map<String, Object>>) feignLegacyInterface.getClientWiseData(accessToken, businessVertical).get("data");

	        // Extract the project IDs from clientDataList
	        Set<Long> projectIds = clientDataList.stream()
	                .flatMap(clientData -> ((List<Map<String, Object>>) clientData.get("projectsData")).stream())
	                .map(projectData -> Long.parseLong(projectData.get("projectId").toString()))
	                .collect(Collectors.toSet());

	        // Fetch totalInvoiceTillDate data only for selected project IDs
	        List<Object[]> totalInvoiceTillDate = projectInvoiceRepository.findAllByNetiveIsDeletedAndIsInternalAndProjectIdIn(false, false, projectIds);

	        // Create a map for faster lookup
	        Map<String, List<Object[]>> projectInvoiceMap = totalInvoiceTillDate.stream()
	                .collect(Collectors.groupingBy(inv -> inv[0].toString(), Collectors.toList()));

	        List<Map<String, Object>> response = clientDataList.stream().map(clientData -> {
	            Map<String, Object> resultMap = new HashMap<>();
	            resultMap.put("clientId", clientData.get("clientId"));

	            List<Map<String, Object>> clientProjects = (List<Map<String, Object>>) clientData.get("projectsData");
	            List<Map<String, Object>> projectList = clientProjects.stream().map(pro -> {
	                Map<String, Object> projectMap = new HashMap<>();
	                String projectId = pro.get("projectId").toString();
	                projectMap.put("projectId", pro.get("projectId"));

	                List<Object[]> collect = projectInvoiceMap.getOrDefault(projectId, Collections.emptyList());

	                List<String> countryList = collect.stream()
	                        .filter(inv -> inv[2] != null)
	                        .map(inv -> inv[2].toString())
	                        .distinct()
	                        .collect(Collectors.toList());

	                List<String> payingEntityNameList = collect.stream()
	                        .filter(inv -> inv[1] != null && Long.parseLong(inv[1].toString()) == 2 && inv[3] != null)
	                        .map(inv -> inv[3].toString())
	                        .distinct()
	                        .collect(Collectors.toList());

	                List<Date> receivedOnList = collect.stream()
	                        .filter(inv -> inv[1] != null && Long.parseLong(inv[1].toString()) == 2 && inv[4] != null)
	                        .map(inv -> {
	                            try {
	                                return simpleDateFormat.parse(inv[4].toString());
	                            } catch (ParseException e) {
	                                throw new RuntimeException(e);
	                            }
	                        })
	                        .distinct()
	                        .collect(Collectors.toList());

	                projectMap.put("countryList", countryList);
	                projectMap.put("payingEntityNameList", payingEntityNameList);
	                projectMap.put("lastReceivedOn", !receivedOnList.isEmpty() ? Collections.max(receivedOnList) : null);
	                return projectMap;
	            }).collect(Collectors.toList());

	            resultMap.put("projects", projectList);
	            return resultMap;
	        }).collect(Collectors.toList());

	        return response;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}

}

	class SortByTrend implements Comparator<ProjectTrendsDTO> {
		@Override
		public int compare(ProjectTrendsDTO arg0, ProjectTrendsDTO arg1) {
			return (int) (arg1.getTrends() - arg0.getTrends());
		}
}
