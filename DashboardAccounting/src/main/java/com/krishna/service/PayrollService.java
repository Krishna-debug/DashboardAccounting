package com.krishna.service;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;

import com.krishna.Interfaces.IPayrollService;
import com.krishna.Interfaces.IPayslipService;
import com.krishna.controller.FeignLegacyInterface;
import com.krishna.domain.Arrear;
import com.krishna.domain.ArrearFile;
import com.krishna.domain.AttendanceVerification;
import com.krishna.domain.PayRegister;
import com.krishna.domain.Payroll;
import com.krishna.domain.Payslip;
import com.krishna.domain.TimesheetCompVerification;
import com.krishna.domain.UserModel;
import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.domain.variablePay.VariablePay;
import com.krishna.dto.ArrearFilesDto;
import com.krishna.dto.PayrollDto;
import com.krishna.dto.PayrollPaidUnpaidDto;
import com.krishna.dto.PayrollWidgetDto;
import com.krishna.enums.PayRegisterStatus;
import com.krishna.enums.PayRollStatus;
import com.krishna.enums.PayslipStatus;
import com.krishna.exceptionhandling.RecordsNotFoundException;
import com.krishna.repository.TimesheetCompVerificationRepository;
import com.krishna.repository.payroll.ArrearFileRepository;
import com.krishna.repository.payroll.ArrearRepository;
import com.krishna.repository.payroll.AttendanceVerificationRepository;
import com.krishna.repository.payroll.BankRepository;
import com.krishna.repository.payroll.PayRegisterRepository;
import com.krishna.repository.payroll.PayrollRepository;
import com.krishna.repository.payroll.PayslipRepository;
import com.krishna.repository.variablePay.VariablePayRepository;
import com.krishna.security.JwtValidator;
import com.krishna.service.util.UtilityService;
import com.krishna.util.ConstantUtility;
import com.krishna.util.NumberToWordsConverter;

/**
 * 
 * @author shivangi
 *
 *         The Payroll Service
 */

@Service
public class PayrollService implements IPayrollService {

	@Autowired
	FeignLegacyInterface feignLegacyInterface;

	@Autowired
	PayRegisterRepository payRegisterRepository;

	@Autowired
	PayrollRepository payrollRepository;

	@Autowired
	JwtValidator validator;
	
	@Autowired
	EntityManager em;

	@Autowired
	ArrearRepository arrearRepository;

	@Autowired
	PayslipRepository payslipRepository;

	@Autowired
	BankRepository bankRepository;

	@Autowired
	FileUploadService fileuploadService;

	@Value("${cloud.aws.payroll.folder.name}")
	private String folderName;

	@Autowired
	ArrearFileRepository arrearFileRepository;

	@Autowired
	private MailService mailService;

	@Autowired
	AttendanceVerificationRepository attendanceVerificationRepository;
	
	@Autowired TimesheetCompVerificationRepository timesheetVerificationRepository;

	@Autowired
	LoginUtiltiyService loginUtilityService;
	
	@Autowired
	UtilityService utilService;
	
	@Autowired
	PayRegisterService payRegisterService;
	
	@Autowired
	IPayslipService payslipService;
	
	@Autowired
	VariablePayRepository variablePayRepository;
	
	@Value("${env.url}")
	private String environmentUrl;
	
	@Value("${com.oodles.hr.email}")
	private String hrMail;
	
	@Value("${com.oodles.accounts.email}")
	private String accountsMail;
	
	@Value("${com.oodles.resourcing.email}")
	private String resourcingMail;
	
	
	
	@Autowired
	EntityManager entityManager;
	
	Logger log = LoggerFactory.getLogger(PayrollService.class);

	/**
	 * @author shivangi
	 * 
	 * Generates Payroll from payRegister
	 */
	ReentrantLock lock=new ReentrantLock();
	@Override
	public List<Payroll> generatePayroll(String accessToken, int month, int year, String userStatus)  {

		Map<String, Object> allUsers = (Map<String, Object>) feignLegacyInterface.getAllUsers(accessToken,month,year, userStatus).get("data");

		List<Map<String,Object>> usersData = (List<Map<String, Object>>) allUsers.get("userList");
		List<Payroll> payrolls = new ArrayList<>();
		int userListSize = usersData.size();
		List<PayRegister> payRegisterList = payRegisterRepository.findByIsCurrentAndStatus(true,PayRegisterStatus.COMPLETE);
		
		for (int i = 0; i < userListSize; i++) {
			Map<String, Object> data = loginUtilityService.objectToMapConverter(usersData.get(i));
			Long userId = new Long((Integer) data.get("id"));
			PayRegister payRegister = null;
			List<PayRegister> filteredPayee = payRegisterList.stream().filter(payee -> Long.toString(payee.getUserId()).equals(userId.toString())).collect(Collectors.toList());
			if(!filteredPayee.isEmpty())
				payRegister = filteredPayee.get(filteredPayee.size()-1);
			if (payRegister != null) {
				if (!lock.isLocked()) {
					lock.lock();
					payrolls = saveData(payRegister, payrolls, month, year, accessToken);
					lock.unlock();
				} else {
					throw new RecordsNotFoundException("Payroll generation is in progress");
				}
			}

		}
		return payrolls;
	}
	
	private List<Payroll> saveData(PayRegister payRegister,List<Payroll> payrolls,int month,int year,String accessToken){
		if (payRegister.getEffectiveDate().toLocalDate().atStartOfDay()
				.isBefore(LocalDateTime.now().minusMonths(1).toLocalDate().atStartOfDay())
				|| payRegister.getEffectiveDate().toLocalDate().atStartOfDay().isEqual(LocalDateTime.now().minusMonths(1).toLocalDate().atStartOfDay())
				|| YearMonth.of(payRegister.getEffectiveDate().toLocalDate().getYear(),
						payRegister.getEffectiveDate().toLocalDate().getMonthValue()).isBefore(YearMonth.of(LocalDateTime.now().minusMonths(1).getYear(),
								LocalDateTime.now().minusMonths(1).getMonthValue()))
				|| YearMonth.of(payRegister.getEffectiveDate().toLocalDate().getYear(),
								payRegister.getEffectiveDate().toLocalDate().getMonthValue()).equals(YearMonth.of(LocalDateTime.now().minusMonths(1).getYear(),
								LocalDateTime.now().minusMonths(1).getMonthValue()))) {
			Payroll existingPayroll = payrollRepository.findAllByMonthAndUserIdAndYearAndIsDeleted(month, payRegister.getUserId(), year, false);
			if (existingPayroll == null) {
				Payroll payroll = savePayroll(payRegister, accessToken, month, year);
				payrolls.add(payroll);
			}
		}
		else {
			Payroll existingPayroll = payrollRepository.findAllByMonthAndUserIdAndYearAndIsDeleted(month, payRegister.getUserId(), year, false);
			if(existingPayroll == null) {
				payRegister=utilService.getMonthsalary(payRegister.getUserId(), month, year);
				if(payRegister!=null) {
					Payroll payroll = savePayroll(payRegister, accessToken, month, year);
					payrolls.add(payroll);
				}
			}
		}
		return payrolls;
	}

	/**
	 * Saves the payroll Data
	 * 
	 * @author shivangi
	 * @param payRegister
	 * @param accessToken
	 * @param month
	 * @return
	 */
	private Payroll savePayroll(PayRegister payRegister, String accessToken, int month, int year) {
		UserModel currentUser = validator.tokenbValidate(accessToken);
		Payroll payroll = new Payroll();
		payroll.setUserId(payRegister.getUserId());
		payroll = setUserDetails(payRegister, payroll, accessToken);
		payroll.setPanNumber(payRegister.getPanNumber());
		payroll.setUan(payRegister.getUan());
		payroll.setBank(payRegister.getBank());
		payroll.setAccountNo(payRegister.getAccountNo());
		payroll.setIfsc(payRegister.getIfsc());
		payroll.setPfSubscription(payRegister.getPfSubscription());
		payroll.setBasicPay(payRegister.getBasicPay());
		payroll.setHra(payRegister.getHra());
		payroll.setConveyance(payRegister.getConveyance());
		payroll.setMedicalAllowance(payRegister.getMedicalAllowance());
		payroll.setProjectAllowance(payRegister.getProjectAllowance());
		payroll.setEmployerPfContribution(payRegister.getEmployerPfContribution());
		payroll.setTotalMonthlyPay(payRegister.getTotalMonthlyPay());
		payroll.setEffectiveDate(payRegister.getEffectiveDate());
		payroll.setEffectiveTo(payRegister.getEffectiveTo());
		payroll.setStatus(payRegister.getStatus());
		payroll.setStatutoryMaternityPay(payRegister.getStatutoryMaternityPay());
		payroll.setLaptopAllowance(payRegister.getLaptopAllowance());
		payroll.setPayRollStatus(PayRollStatus.PENDING);
		if(currentUser!=null)
			payroll.setGeneratedBy(currentUser.getUserId());
		payroll.setDeleted(false);
		payroll = setVariablePay(payroll,month,year,payRegister.getUserId());
		payroll = setPayrollData(payroll, month, year);
		payroll = setPaidDays(payroll, month, year, accessToken);
		payroll = payrollRepository.saveAndFlush(payroll);
		return payroll;
	}



	/**
	 * Sets the user details in payroll
	 * 
	 * @author shivangi
	 * @param payRegister
	 * @param payroll
	 * @param accessToken
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Payroll setUserDetails(PayRegister payRegister, Payroll payroll, String accessToken) {
		payroll.setEmployeeId(payRegister.getEmployeeId());
		Map<String, Object> userInformation = (Map<String, Object>) feignLegacyInterface.getUserDetails(accessToken, payRegister.getUserId()).get("data");
		payroll.setDesignation((String) userInformation.get(ConstantUtility.DESIGNATION));
		payroll.setGrade((String) userInformation.get("grade"));
		payroll.setDepartment((String) userInformation.get(ConstantUtility.DEPARTMENT));
		String employeeStatus = (String) userInformation.get("employmentStatus");
		payroll.setEmploymentStatus(employeeStatus);
		ArrayList<Object> projects = (ArrayList<Object>) userInformation.get("projects");
		ArrayList<Long> projectId = new ArrayList<>();
		ArrayList<String> projectNames = new ArrayList<>();
		for (int i = 0; i < projects.size(); i++) {
			HashMap<String, Object> projectData = (HashMap<String, Object>) projects.get(i);
			int id = (int) projectData.get("id");
			projectId.add(Long.valueOf(id));
			projectNames.add((String) projectData.get("name"));
		}
		payroll.setProjectIds(projectId);
		payroll.setProjectNames(projectNames);
		return payroll;
	}

	/**
	 * Set the payroll data the can vary monthly
	 * 
	 * @author shivangi
	 * @param payroll
	 * @param month
	 * @return
	 */
	private Payroll setPayrollData(Payroll payroll, int month, int year) {
		payroll.setIncentives(0.0);
		payroll.setTotalArrear(0.0);
		payroll.setVerified(false);
		int previousMonth=month - 1;
		if(month==0){
			previousMonth = 11;
		}
		Payroll previousPayroll = payrollRepository.findByMonthAndUserIdAndYear(previousMonth, payroll.getUserId(),year);
		if (previousPayroll != null) {
			payroll.setTds(previousPayroll.getTds());
			payroll.setEmployeePfContribution(previousPayroll.getEmployeePfContribution());
			payroll.setHealthInsurance(previousPayroll.getHealthInsurance());
			payroll.setInfraDeductions(previousPayroll.getInfraDeductions());
			payroll.setLeaveDeductions(previousPayroll.getLeaveDeductions());
			payroll.setWorkFromHomeAllowance(previousPayroll.getWorkFromHomeAllowance());
			payroll.setSpecialAllowance(previousPayroll.getSpecialAllowance());			

		} else {
			payroll.setTds(0.0);
			payroll.setEmployeePfContribution(0.0);
			payroll.setHealthInsurance(0.0);
			payroll.setInfraDeductions(0.0);
			payroll.setLeaveDeductions(0.0);
			payroll.setWorkFromHomeAllowance(0.0);
			payroll.setSpecialAllowance(0.0);

		}
		double totalMonthlyPay = (payroll.getTotalMonthlyPay() + payroll.getIncentives() +payroll.getSpecialAllowance()
		+payroll.getWorkFromHomeAllowance()+payroll.getVariablePayAmount()+payroll.getLaptopAllowance()+payroll.getVoluntaryPayAmount()+
		payroll.getTotalArrear() + payroll.getTotalReimbursement())
				- (payroll.getTds() + payroll.getEmployeePfContribution() + payroll.getHealthInsurance()
						+ payroll.getInfraDeductions() + payroll.getLeaveDeductions());
		payroll.setNetPay(totalMonthlyPay);
		payroll = setColumnsForPayslip(payroll, previousPayroll);
		payroll.setMonth(month);
		payroll.setYear(year);
		payroll.setGeneratedOn(LocalDateTime.now());
		payroll = payrollRepository.save(payroll);
		return payroll;
	}

	private Payroll setColumnsForPayslip(Payroll payroll, Payroll previousPayroll) {
		ArrayList<String> defaultDeductionsColumns = new ArrayList<>(Arrays.asList(ConstantUtility.HEALTH_INSURANCE));
		ArrayList<String> defaultEarningColumns = new ArrayList<>();
		if (payroll.getHra() != 0)
			defaultEarningColumns.add("hRA");
		if (payroll.getBasicPay() != 0)
			defaultEarningColumns.add("basic");
		if (payroll.getConveyance() != 0)
			defaultEarningColumns.add("conveyance");
		if (payroll.getMedicalAllowance() != 0)
			defaultEarningColumns.add("medical");
		if (payroll.getProjectAllowance() != 0)
			defaultEarningColumns.add("project");
		if (payroll.getEmployerPfContribution() != 0)
			defaultEarningColumns.add("employerPFContribution");
		if (payroll.getStatutoryMaternityPay() != 0)
			defaultEarningColumns.add("smp");
		if (payroll.getLaptopAllowance() != 0)
			defaultEarningColumns.add("laptop");
		if (payroll.getVariablePayAmount() != 0)
			defaultEarningColumns.add(ConstantUtility.VARIABLE_PAY);
		if (payroll.getWorkFromHomeAllowance()!= 0)
			defaultEarningColumns.add(ConstantUtility.WORK_FROM_HOME_ALLOWANCE);
		if (payroll.getSpecialAllowance()!= 0)
			defaultEarningColumns.add(ConstantUtility.SPECIAL_ALLOWANCE);
		if (payroll.getVoluntaryPayAmount()!= 0)
			defaultEarningColumns.add(ConstantUtility.VOLUNTARY_PAY);
		
		// Sets the earning columns to be displayed in payslip
		if (previousPayroll != null) {
			List<String> earningConfig = new ArrayList<>();
			if (!previousPayroll.getColumnEarnings().isEmpty()) {
				previousPayroll.getColumnEarnings().forEach(column -> {
					earningConfig.add(column);
				});
				payroll.setColumnEarnings(earningConfig);
			}
			// Sets the deductions columns to be displayed in payslip
			ArrayList<String> deductionConfig = new ArrayList<>();
			if (!previousPayroll.getColumnDeductions().isEmpty()) {
				previousPayroll.getColumnDeductions().forEach(column -> {
					deductionConfig.add(column);
				});
				payroll.setColumnDeductions(deductionConfig);
			}
		} else {
			payroll.setColumnEarnings(defaultEarningColumns);
			payroll.setColumnDeductions(defaultDeductionsColumns);
		}
		return payroll;
	}

	/**
	 * Sets the paid and unpaid days for salary
	 * 
	 * @author shivangi
	 * @param payroll
	 * @return
	 */
	private Payroll setPaidDays(Payroll payroll, int month, int year, String accessToken) {
		Map<String, Object> getAttendanceDays = feignLegacyInterface.userLeavesdataForPayroll(accessToken, month, year, payroll.getUserId());
		
		Double unpaidDays = Double.parseDouble(getAttendanceDays.get(ConstantUtility.UNPAID_LEAVES).toString());
		Double nonWorkingDays = Double.parseDouble(getAttendanceDays.get(ConstantUtility.NON_WORKING_DAYS).toString());
		YearMonth yearMonth = YearMonth.of(year, month);
		int lengthOfMonth = yearMonth.lengthOfMonth();
		payroll.setPayDays(lengthOfMonth - nonWorkingDays);
		payroll.setUnpaidDays(unpaidDays.longValue());
		return payroll;
	}

	/**
	 * Edit Payroll Data
	 * 
	 * @author shivangi
	 */
	@Override
	public HashMap<String, Object> editPayRoll(String accessToken, PayrollDto payroll, int month, int year) {
		UserModel currentUser = validator.tokenbValidate(accessToken);
		Payroll existingPayroll = payrollRepository.findAllByMonthAndUserIdAndYearAndIsDeleted(month, payroll.getUserId(),year,false);
		String timesheetMessage="";
		Boolean accountsIssue=false;
		if (existingPayroll != null && (!existingPayroll.getPayRollStatus().equals(PayRollStatus.PROCESSED)	|| !existingPayroll.getPayRollStatus().equals(PayRollStatus.VERIFIED))) {
			existingPayroll.setModifiedOn(LocalDateTime.now());
			existingPayroll.setModifiedBy(currentUser.getUserId());
			if(payroll.getPayrollStatus().equals(PayRollStatus.TIMESHEET_ISSUE))
				timesheetMessage=sendTimesheetMail(accessToken,payroll.getUserId(), month, year);
			if(payroll.getPayrollStatus().equals(PayRollStatus.ATTENDANCE_ISSUE))
				accountsIssue=sendMailOnAccountsIssue(accessToken,payroll.getUserId(), month, year);
			if(timesheetMessage.equals("Timesheet Already Compliant!!! ")) {
				HashMap<String, Object> timesheetMap =  new HashMap<>();
				timesheetMap.put("timesheetIssueMessage", timesheetMessage);
				return timesheetMap;
			}
			existingPayroll = saveEditedData(existingPayroll, payroll, month, year);
			existingPayroll = payrollRepository.save(existingPayroll);
		}
		Map<Object,Object> timesheet = getTimesheetHours(accessToken, payroll.getUserId(), month, year);
		List<Object> users=new ArrayList<>();
		List<AttendanceVerification> attendanceVerification = attendanceVerificationRepository.findAllByMonthAndYear(month, year);
		List<Payslip> payslips = payslipRepository.findAllByPayslipMonthAndPayslipYear(
				 month, year);
		List<Arrear> arrears = arrearRepository.findByCreationMonthAndCreationYearAndIsArrearIncludedAndIsDeleted(month, year, true,false);
		HashMap<String, Object> payrollData = getPayrollData(existingPayroll, accessToken, month, year, timesheet,users, attendanceVerification, payslips, arrears);
		return payrollData;
	}

	/**
	 * Saves the edited data.
	 * 
	 * @author shivangi
	 * @param payroll
	 * @param payrollDto
	 * @return
	 */
	private Payroll saveEditedData(Payroll payroll, PayrollDto payrollDto, int month, int year) {
		payroll.setPayDays(payrollDto.getPayDays());
		payroll.setUnpaidDays(payrollDto.getUnpaidDays());
		payroll.setLeaveDeductions(payrollDto.getLeaveDeductions());
		payroll.setVariablePayAmount(0.0);
		payroll.setVoluntaryPayAmount(0.0);
		if(payrollDto.getVariablePay()!=null) {
			payroll.setVariablePayAmount(payrollDto.getVariablePay());
			updateVariablePayAmount(payroll.getMonth(),payroll.getYear(),payroll.getUserId(),payroll.getVariablePayAmount());
		}
		if(payrollDto.getVoluntaryPay()!=null)
			payroll.setVoluntaryPayAmount(payrollDto.getVoluntaryPay());
		payroll = setPayValuesAccordingToPayslip(payroll, payrollDto);
		payroll = setDeductionsAccordingToPayslip(payroll, payrollDto);
		if (payrollDto.getColumnEarnings().contains("incentives"))
			payroll.setIncentives(payrollDto.getIncentives());
		else
			payroll.setIncentives(0);
		if (payrollDto.getColumnEarnings().contains("smp"))
			payroll.setStatutoryMaternityPay(payrollDto.getStatutoryMaternityPay());
		else
			payroll.setStatutoryMaternityPay(0);
		List<Arrear> arrears=arrearRepository.findByPayrollIdAndCreationMonthAndCreationYearAndIsDeleted(payroll.getId(), month, year,false);
		if(arrears.isEmpty() && payrollDto.getColumnEarnings().contains(ConstantUtility.ARREAR)) {
			payrollDto.getColumnEarnings().remove(ConstantUtility.ARREAR);
		}
		payroll.setPayRollStatus(payrollDto.getPayrollStatus());
		payroll.setColumnEarnings(payrollDto.getColumnEarnings());
		payroll.setColumnDeductions(payrollDto.getColumnDeductions());
		payroll.setVerified(payrollDto.isVerified());
		double totalMonthlyPay = (payroll.getBasicPay() + payroll.getHra() + payroll.getConveyance()
				+ payroll.getMedicalAllowance() + payroll.getProjectAllowance() + payroll.getLaptopAllowance() + payroll.getTotalReimbursement()
				+ payroll.getEmployerPfContribution() + payroll.getStatutoryMaternityPay() + payrollDto.getIncentives()
				+ payroll.getTotalArrear() +payroll.getVariablePayAmount()+ payroll.getPaidLeaveAdditions()+payroll.getSpecialAllowance()
				+payroll.getWorkFromHomeAllowance())+payroll.getVoluntaryPayAmount()
				- (payroll.getTds() + payroll.getEmployeePfContribution() + payroll.getHealthInsurance()
						+ payroll.getInfraDeductions() + payroll.getLeaveDeductions());
		payroll.setNetPay(totalMonthlyPay);
		payroll = setChangedAttributes(payroll, payrollDto, month, year);
		payroll = payrollRepository.saveAndFlush(payroll);
		return payroll;
	}

	private void updateVariablePayAmount(int month, int year, long userId,Double amount) {
		VariablePay variablePay = variablePayRepository.findByMonthAndYearAndUserIdAndIsDeletedFalse(month,year,userId);
		if(variablePay!=null) {
			variablePay.setAmount(amount);
			variablePayRepository.save(variablePay);
		}
	}

	/**
	 * Saves the arrear for the month,if any.
	 * 
	 * @author shivangi
	 */
	@Override
	public Arrear saveArrear(double arrearAmount, int arrearMonth, long userId, int arrearYear, String arrearComment, String accessToken, int creationMonth, int creationYear, boolean isArrearIncluded,
			ArrearFilesDto arrearFilesDto,boolean isReimbursement) {
		if(isArrearIncluded) {
		Payroll existingPayroll = payrollRepository.findAllByMonthAndUserIdAndYearAndIsDeleted(creationMonth, userId, creationYear, false);
		if (existingPayroll != null) {
			Arrear arrear = new Arrear();
			arrear.setArrearAmount(arrearAmount);
			arrear.setArrearMonth(arrearMonth);
			arrear.setArrearComment(arrearComment);
			UserModel user = validator.tokenbValidate(accessToken);
			arrear.setCreatedBy(user.getUserId());
			arrear.setCreationDate(LocalDateTime.now());
			arrear.setEmployeeId(existingPayroll.getEmployeeId());
			arrear.setPayrollId(existingPayroll.getId());
			arrear.setUserId(existingPayroll.getUserId());
			arrear.setYear(arrearYear);
			arrear.setCreationMonth(arrearMonth+1);
			arrear.setCreationYear(creationYear);
			arrear.setArrearIncluded(isArrearIncluded);
			arrear.setIsReimbursement(isReimbursement);
			arrear = arrearRepository.save(arrear);
			if (!arrearFilesDto.getFileData().equals("") && !arrearFilesDto.getFileName().equals("")) {
				try {
					ArrearFile arrearFile = uploadArrearFile(arrearFilesDto, accessToken, arrear.getId());
					log.debug("File uploaded Successfully!!!!" + arrearFile.getFileName());
				} catch (Exception e) {
					log.debug(ConstantUtility.EXCEPTION_DEBUG_STRING + e);
				}
			}
			double totalArrear = 0.0;
			if (isArrearIncluded) {
				existingPayroll=addArrearInPayroll(existingPayroll, totalArrear, creationMonth, creationYear,isReimbursement);
				List<String> earningCols=(List<String>) existingPayroll.getColumnEarnings();
				if(!earningCols.contains(ConstantUtility.ARREAR) && isReimbursement) {
					earningCols.add(ConstantUtility.ARREAR);
					existingPayroll.setColumnEarnings(earningCols);
				}
				else if(!earningCols.contains(ConstantUtility.PREVIOUS_ARREAR) && !isReimbursement) {
					earningCols.add(ConstantUtility.PREVIOUS_ARREAR);
					existingPayroll.setColumnEarnings(earningCols);
				}
				existingPayroll = payrollRepository.saveAndFlush(existingPayroll);
			}
			return arrear;
		}
		
		}
		else {
			Arrear arrear = new Arrear();
			arrear.setArrearAmount(arrearAmount);
			arrear.setArrearMonth(arrearMonth);
			arrear.setArrearComment(arrearComment);
			UserModel user = validator.tokenbValidate(accessToken);
			arrear.setCreatedBy(user.getUserId());
			arrear.setCreationDate(LocalDateTime.now());
			arrear.setUserId(userId);
			arrear.setYear(arrearYear);
			arrear.setCreationMonth(arrearMonth+1);
			arrear.setCreationYear(creationYear);
			arrear.setArrearIncluded(isArrearIncluded);
			arrear.setIsReimbursement(isReimbursement);
			arrear = arrearRepository.save(arrear);
			if (!arrearFilesDto.getFileData().equals("") && !arrearFilesDto.getFileName().equals("")) {
				try {
					ArrearFile arrearFile = uploadArrearFile(arrearFilesDto, accessToken, arrear.getId());
					log.debug("File uploaded Successfully!!!!" + arrearFile.getFileName());
				} catch (Exception e) {
					log.debug(ConstantUtility.EXCEPTION_DEBUG_STRING + e);
				}
			}
			return arrear;
		}
		return null;
	}
	
	/**
	 * Add Arrear amount in payroll if arrear is included
	 * 
	 * @param existingPayroll
	 * @param totalArrear
	 * @param creationMonth
	 * @param creationYear
	 * @return
	 */
	public Payroll addArrearInPayroll(Payroll existingPayroll,double totalArrear,int creationMonth, int creationYear,boolean isReimbursement) {
		List<Arrear> arrears = arrearRepository.findByPayrollIdAndCreationMonthAndCreationYearAndIsReimbursementAndIsDeleted(
				existingPayroll.getId(), creationMonth, creationYear, isReimbursement,false);
		for (Arrear arrearData : arrears) {
			totalArrear = totalArrear + arrearData.getArrearAmount();
		}
		if(isReimbursement)
			existingPayroll.setTotalReimbursement(totalArrear);
		else
			existingPayroll.setTotalArrear(totalArrear);
		return existingPayroll;
	}

	/**
	 * Get All payrolls
	 * 
	 * @author shivangi
	 * 
	 */
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Object> getPayrolls(int month, int year, String accessToken, String timesheetCompliance,String priority,Map<String,Object> timesheets,List<Payroll> payrolls, List<Object> users) {
		List<Object> payrollList = new ArrayList<>();
		List<AttendanceVerification> attendanceVerification = attendanceVerificationRepository.findAllByMonthAndYear(month, year);
		List<Payslip> payslips = payslipRepository.findAllByPayslipMonthAndPayslipYear(
				 month, year);
		List<Arrear> arrears = arrearRepository.findByCreationMonthAndCreationYearAndIsArrearIncludedAndIsDeleted(month, year, true,false);
	
		for (Payroll payroll : payrolls) {
			Map<Object, Object> timesheet= (Map<Object, Object>) timesheets.get(Long.toString(payroll.getUserId()));
			HashMap<String, Object> payrolldata = getPayrollData(payroll, accessToken, month, year, timesheet,users,attendanceVerification,payslips,arrears);
			payrollList.add(payrolldata);	
		}
		return payrollList;
	}

	@SuppressWarnings("unchecked")
	@Cacheable("payrolls")
	@Override
	public List<Payroll> getPayrollsForMonth(int month, int year, String timesheetCompliance,String priority,Map<String,Object> timesheets) {
		List<Payroll> payrolls = new ArrayList<Payroll>();
		if(!priority.equals("")) {
			if(priority.equals("High"))
				payrolls = payrollRepository.findAllByMonthAndYearAndIsDeletedFalseAndIsPriority(month, year,true);
			else
				payrolls = payrollRepository.findAllByMonthAndYearAndIsDeletedFalseAndIsPriority(month, year, false);				
		}
		else 
			payrolls = payrollRepository.findAllByMonthAndYearAndIsDeletedFalse(month, year);
		List<Payroll> payrollList = new ArrayList<>();
		for (Payroll payroll : payrolls) {
			Map<Object, Object> timesheet= (Map<Object, Object>) timesheets.get(Long.toString(payroll.getUserId()));
			if (timesheet!=null  && timesheetCompliance.equals("Compliant")) {
		
				if (new Double(timesheet.get(ConstantUtility.ACTUAL_HOURS).toString()) >= new Double(timesheet.get(ConstantUtility.EXPECTED_HOURS).toString())) {
					log.info(":::::::In Compliant ::::");
					payrollList.add(payroll);
				}
			} else if (timesheet!=null && timesheetCompliance.equals("Non Compliant")) {
				if (new Double(timesheet.get(ConstantUtility.ACTUAL_HOURS).toString()) < new Double(timesheet.get(ConstantUtility.EXPECTED_HOURS).toString())) {
					 log.info(":::::::In Non Compliant ::::");
					payrollList.add(payroll);
				}
			} else {
				payrollList.add(payroll);
			}
		}
		return payrollList;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Cacheable("usersTimesheet")
	public Map<String, Object> getPayrollUsersTimesheet(String accessToken,int month, int year){
		List<Long> userIds=new ArrayList<>();

		Map<String,Object> timesheet=(Map<String, Object>) feignLegacyInterface.getPayrollUsersTimesheet(accessToken,month,year).get("data");

		return timesheet;
	}
	
	@Override
	@Cacheable("usersTimesheet")
	@SuppressWarnings("unchecked")
	public Map<String, Object> getPayrollUsersTimesheetByUserList(String accessToken,int month, int year){
		Map<String,Object> timesheet=(Map<String, Object>) feignLegacyInterface.getPayrollUsersTimesheet(accessToken,month,year).get("data");
		return timesheet;
	}

	
	
	
	/**
	 * @author pankaj 
	 */
	private HashMap<String, Object> getPayrollData(Payroll payroll, String accessToken, int month, int year
			,Map<Object, Object> timesheet,List<Object> users,List<AttendanceVerification> av,List<Payslip> paySlipData,List<Arrear>arrearsData) {
		HashMap<String, Object> payrolldata = new HashMap<>();
		payrolldata=getUserDetails(payrolldata,payroll, accessToken, users);
		payrolldata = getUserData(payrolldata, payroll, accessToken);
		payrolldata = getEarningAndDeductions(payrolldata, payroll);
		payrolldata.put("isOnMl", false);
		AttendanceVerification attendanceVerification =av.stream().filter(attendance->String.valueOf(attendance.getUserId()).equals(String.valueOf(payroll.getUserId()))).findFirst().orElse(null);
			if (attendanceVerification != null) {
				payrolldata.put(ConstantUtility.IS_ATTENDANCE_VERIFIED, attendanceVerification.isAttendanceVerified());
				payrolldata.put("hrComment", attendanceVerification.getComment());
			} else {
				payrolldata.put(ConstantUtility.IS_ATTENDANCE_VERIFIED, false);
				payrolldata.put("hrComment", null);
			}
		
		payrolldata.put("laptopAllowance", payroll.getLaptopAllowance());
		if(timesheet!=null) {
			String timesheetHours = timesheet.get(ConstantUtility.ACTUAL_HOURS) + "/" + timesheet.get(ConstantUtility.EXPECTED_HOURS);
			payrolldata.put("timesheetHours", timesheetHours);
			payrolldata.put("expectedHours", Double.parseDouble(timesheet.get(ConstantUtility.EXPECTED_HOURS).toString()));
			if (new Double(timesheet.get(ConstantUtility.ACTUAL_HOURS).toString()) >= new Double(timesheet.get(ConstantUtility.EXPECTED_HOURS).toString())) {
				payrolldata.put(ConstantUtility.TIMESHEET_COMPLIANT, true);
				payrolldata.put(ConstantUtility.IS_TIMESHEET_COMPLIANT, "Yes");
			}
			else {
				payrolldata.put(ConstantUtility.TIMESHEET_COMPLIANT, false);
				payrolldata.put(ConstantUtility.IS_TIMESHEET_COMPLIANT, "No");
			}
		}
		else {
			payrolldata.put("expectedHours", 0.00);
			payrolldata.put("timesheetHours", "0.0/0.0");
			payrolldata.put(ConstantUtility.TIMESHEET_COMPLIANT, true);
			payrolldata.put(ConstantUtility.IS_TIMESHEET_COMPLIANT, "Yes");
		}
		payrolldata.put("isMarginIncluded", payroll.getIsMarginIncluded()!=null?payroll.getIsMarginIncluded():true);
		payrolldata.put("isPriority", payroll.isPriority());
		payrolldata.put(ConstantUtility.VARIABLE_PAY, payroll.getVariablePayAmount()!=null?payroll.getVariablePayAmount():0.0);
		payrolldata.put(ConstantUtility.VOLUNTARY_PAY, payroll.getVoluntaryPayAmount()!=null?payroll.getVoluntaryPayAmount():0.0);
		payrolldata.put("payrollComments", payroll.getPayrollComment() ==null ? "" : payroll.getPayrollComment());
		payrolldata.put("buApprovalComment", payroll.getBuApprovalComment()==null?"":payroll.getBuApprovalComment());
		payrolldata.put("buPayrollApproval", payroll.getBuPayrollApproval()==null?null: payroll.getBuPayrollApproval());
		payrolldata = getPayslipData(payrolldata, payroll, month, year,paySlipData,arrearsData);
		payrolldata = getChangedAttributes(payrolldata, payroll, accessToken);
		return payrolldata;
	}	

	/**
	 * Get Earning/deductions of payroll
	 */
	private HashMap<String, Object> getEarningAndDeductions(HashMap<String, Object> payrolldata,Payroll payroll){
		payrolldata.put("basicpay", payroll.getBasicPay());
		payrolldata.put("hra", payroll.getHra());
		payrolldata.put("conveyanceAllowances", payroll.getConveyance());
		payrolldata.put("medicalAllowances", payroll.getMedicalAllowance());
		payrolldata.put("projectAllowances", payroll.getProjectAllowance());
		payrolldata.put(ConstantUtility.WORK_FROM_HOME_ALLOWANCE, payroll.getWorkFromHomeAllowance());
		payrolldata.put(ConstantUtility.SPECIAL_ALLOWANCE, payroll.getSpecialAllowance());
		payrolldata.put(ConstantUtility.VARIABLE_PAY, payroll.getVariablePayAmount());
		payrolldata.put("incentives", payroll.getIncentives());
		payrolldata.put(ConstantUtility.ARREAR, payroll.getTotalReimbursement());
		payrolldata.put("previousArrears", payroll.getTotalArrear());
		payrolldata.put("tds", payroll.getTds());
		payrolldata.put("employeePfContribution", payroll.getEmployeePfContribution());
		payrolldata.put("employerPfContribution", payroll.getEmployerPfContribution());
		payrolldata.put("totalMonthlyPay", payroll.getTotalMonthlyPay());
		payrolldata.put(ConstantUtility.HEALTH_INSURANCE, payroll.getHealthInsurance());
		payrolldata.put("infraDeductions", payroll.getInfraDeductions());
		payrolldata.put("statutoryMaternityPay", payroll.getStatutoryMaternityPay());
		payrolldata.put("netPay", payroll.getNetPay());
		payrolldata.put("payrollStatus", payroll.getPayRollStatus());
		payrolldata.put("isVerified", payroll.isVerified());
		payrolldata.put("isProcessed", payroll.isProcessed());
		payrolldata.put(ConstantUtility.VOLUNTARY_PAY, payroll.getVoluntaryPayAmount()!=null?payroll.getVoluntaryPayAmount():0.0);
		payrolldata.put(ConstantUtility.LEAVE_DEDUCTION, payroll.getLeaveDeductions());
		payrolldata.put("paidLeaveAmount", payroll.getPaidLeaveAdditions());
		return payrolldata;
	}

	/**
	 * Get the arrears of particular Employee of given month and year
	 * 
	 * @author shivangi
	 */
	@Override
	public List<Object> getEmployeeArrears(long payrollId, String accessToken, int month, int year,boolean isReimbursement) {
		Payroll payroll = payrollRepository.findAllById(payrollId);
		List<Object> arrearList = new ArrayList<>();
		if (payroll != null) {
			List<Arrear> arrears = arrearRepository.findByPayrollIdAndCreationMonthAndCreationYearAndIsReimbursementAndIsDeleted(payrollId, month,
					year,isReimbursement,false);
			if (!arrears.isEmpty()) {
				arrears.forEach(arrear -> {
					Map<String, Object> arrearDetails = getArrearDetails(payroll, arrear, accessToken);
					arrearList.add(arrearDetails);
				});
			}
		}
		return arrearList;
	}

	/**
	 * Get all Arrears for the given year and month
	 * 
	 * @author shivangi
	 */
	@Override
	public List<Object> getAllArrears(int month, int year, String accessToken,boolean isReimbursement) {
		List<Arrear> allArrears = new ArrayList<>();
		if (month != 0 && year != 0) {
			allArrears = arrearRepository.findAllByCreationMonthAndCreationYearAndIsReimbursementAndIsDeleted(month, year,isReimbursement,false);
		}
		if (month != 0 && year == 0) {
			allArrears = arrearRepository.findAllByCreationMonthAndCreationYearAndIsReimbursementAndIsDeleted(month, LocalDateTime.now().getYear(),isReimbursement,false);
		}
		if (month == 0 && year != 0) {
			allArrears = arrearRepository.findAllByCreationYearAndIsReimbursementAndIsDeleted(year,isReimbursement,false);
		}
		List<Object> arrearList = new ArrayList<>();
		if (!allArrears.isEmpty()) {
			allArrears.forEach(arrear -> {
				Payroll payroll = payrollRepository.findAllById(arrear.getPayrollId());
				Map<String, Object> arrearDetails = getArrearDetails(payroll, arrear, accessToken);
				arrearList.add(arrearDetails);
			});
		}
		return arrearList;
	}

	/**
	 * @author shivangi
	 * 
	 *         Get Arrear Details
	 * 
	 * @param payroll
	 * @param arrear
	 * @param accessToken
	 * @return map of arrear data
	 */
	private Map<String, Object> getArrearDetails(Payroll payroll, Arrear arrear, String accessToken) {
		Map<String, Object> arrearDetails = new HashMap<>();
		arrearDetails.put(ConstantUtility.USER_ID, arrear.getUserId());
		
		if(payroll!=null)
		arrearDetails.put(ConstantUtility.EMPLOYEE_ID, payroll.getEmployeeId());
		Map<String, Object> userInformation = (Map<String, Object>) feignLegacyInterface.getUserDetails(accessToken, arrear.getUserId()).get("data");
		arrearDetails.put(ConstantUtility.EMPLOYEE_ID, userInformation.get(ConstantUtility.EMPLOYEE_ID));
		arrearDetails.put(ConstantUtility.EMPLOYEE_NAME, userInformation.get("name"));
		arrearDetails.put(ConstantUtility.DESIGNATION, userInformation.get("designation"));
		arrearDetails.put(ConstantUtility.DEPARTMENT, userInformation.get("department"));
		arrearDetails.put(ConstantUtility.DATE_OF_JOINING, userInformation.get(ConstantUtility.DATE_OF_JOINING));
		arrearDetails.put("arrearMonth", arrear.getArrearMonth());
		arrearDetails.put("arrearYear", arrear.getYear());
		arrearDetails.put(ConstantUtility.ARREAR, arrear.getArrearAmount());
		arrearDetails.put("arrearComment", arrear.getArrearComment());
		arrearDetails.put("payrollStatus", payroll!=null?payroll.getPayRollStatus():null);
		if(payroll!=null)
		arrearDetails.put("payrollId", payroll.getId());
		arrearDetails.put("arrearId", arrear.getId());
		arrearDetails.put("arrearMonthName", Month.of(arrear.getArrearMonth() + 1));
		List<ArrearFile> arrearFiles = arrearFileRepository.findAllByArrearIdAndIsDeleted(arrear.getId(), false);
		arrearDetails.put("arrearFiles", arrearFiles);
		arrearDetails.put(ConstantUtility.IS_ARREAR_INCLUDED, arrear.isArrearIncluded());
		arrearDetails.put("creationDate", arrear.getCreationDate());
		arrearDetails.put(ConstantUtility.BUSINESS_VERTICAL, userInformation.get(ConstantUtility.BUSINESS_VERTICAL));
		return arrearDetails;
	}

@Override
	public Arrear editArrear(String accessToken, long arrearId, double arrearAmount, int arrearMonth, int arrearYear,
			String arrearComment, boolean isArrearIncluded, ArrearFilesDto arrearFilesDto,@RequestParam boolean isReimbursement) {
		Arrear arrear = arrearRepository.findAllById(arrearId);
		if (arrear != null) {
			arrear.setArrearAmount(arrearAmount);
			arrear.setArrearMonth(arrearMonth);
			arrear.setArrearComment(arrearComment);
			UserModel user = validator.tokenbValidate(accessToken);
			arrear.setModificationDate(LocalDateTime.now());
			arrear.setModifiedBy(user.getUserId());
			arrear.setYear(arrearYear);
			arrear.setArrearIncluded(isArrearIncluded);
			arrear.setCreationMonth(arrearMonth+1);
			arrear.setCreationYear(arrearYear);
			if (!arrearFilesDto.getFileData().equals("") && !arrearFilesDto.getFileName().equals("")) {
				try {
					ArrearFile arrearFile = uploadArrearFile(arrearFilesDto, accessToken, arrear.getId());
					log.debug("File Uploaded Successfully!!!" + arrearFile.getUploadedBy());
				} catch (Exception e) {
					log.warn("Exception is" + e);
				}
			}
			arrear = arrearRepository.save(arrear);
			double totalArrear = 0.0;
				List<Arrear> arrears = arrearRepository.findByPayrollIdAndCreationMonthAndCreationYearAndIsArrearIncludedAndIsReimbursementAndIsDeleted(
						arrear.getPayrollId(), arrearMonth+1, arrearYear,true, isReimbursement, false);
				
				Payroll payroll=payrollRepository.findAllByMonthAndUserIdAndYearAndIsDeleted(arrear.getCreationMonth(), arrear.getUserId(), arrear.getCreationYear(), false);
				for (Arrear arrearData : arrears) {
					totalArrear = totalArrear + arrearData.getArrearAmount();
				}
				if(payroll!=null) {
				if(isReimbursement)
					payroll.setTotalReimbursement(totalArrear);
				else
					payroll.setTotalArrear(totalArrear);
				if (isArrearIncluded) {
					arrear.setPayrollId(payroll.getId());
					List<String> earningCols = (List<String>) payroll.getColumnEarnings();
					if (!earningCols.contains(ConstantUtility.ARREAR) && isReimbursement) {
						earningCols.add(ConstantUtility.ARREAR);
						payroll.setColumnEarnings(earningCols);
					} else if (!earningCols.contains(ConstantUtility.PREVIOUS_ARREAR) && !isReimbursement) {
						earningCols.add(ConstantUtility.PREVIOUS_ARREAR);
						payroll.setColumnEarnings(earningCols);
					}
				}
					payroll = payrollRepository.saveAndFlush(payroll);
				}
			arrear = arrearRepository.save(arrear);
		}
		return arrear;
	}


	
	public HashMap<String, Object> getPayslipData(HashMap<String, Object> payrollData, Payroll payroll, int month, int year,List<Payslip> paySlipData,List<Arrear>arrearsData) {
		double totalEarnings = payroll.getBasicPay() + payroll.getHra() + payroll.getConveyance()
				+ payroll.getMedicalAllowance() + payroll.getProjectAllowance() + payroll.getLaptopAllowance()
				+ payroll.getEmployerPfContribution() + payroll.getTotalArrear() + payroll.getIncentives()
				+ payroll.getStatutoryMaternityPay() + payroll.getPaidLeaveAdditions()+payroll.getTotalReimbursement()+payroll.getWorkFromHomeAllowance()
				+payroll.getSpecialAllowance()+payroll.getVariablePayAmount()+payroll.getVoluntaryPayAmount();
		payrollData.put("totalEarnings", totalEarnings);
		double totalDeduction = payroll.getTds() + payroll.getHealthInsurance() + payroll.getInfraDeductions()
				+ payroll.getEmployeePfContribution() + payroll.getLeaveDeductions();
		payrollData.put("totalDeduction", totalDeduction);
		int totalSalary = (int) (totalEarnings - totalDeduction);
		payrollData.put("totalSalary", totalSalary);
		new NumberToWordsConverter();
		payrollData.put("amountInWords", NumberToWordsConverter.convert(totalSalary));
		String monthAndYear = Month.of(month).name() + " " + year;
		payrollData.put("monthAndYear", monthAndYear);
		Payslip existingPayslip=paySlipData.stream().filter(pay->String.valueOf(pay.getUserId())
				.equals(String.valueOf(payroll.getUserId())) && String.valueOf(pay.getPayrollId()).equals(String.valueOf(payroll.getId()))).findAny().orElse(null);
	
		if (existingPayslip != null) {
			payrollData.put("payslipId", existingPayslip.getId());
			payrollData.put("payslipStatus", existingPayslip.getPayslipStatus());
			payrollData.put("isUpdated", existingPayslip.isUpdated());
			
		if(existingPayslip.getModificationDate()!=null)
			payrollData.put(ConstantUtility.UPDATED_ON, existingPayslip.getModificationDate().getDayOfMonth()+"/"+existingPayslip.getModificationDate().getMonthValue()+"/"+existingPayslip.getModificationDate().getYear());
		else
			payrollData.put(ConstantUtility.UPDATED_ON, null);
        } else {
			payrollData.put("payslipId", null);
			payrollData.put("payslipStatus", null);
			payrollData.put("isUpdated", false);
			payrollData.put(ConstantUtility.UPDATED_ON, null);
		}
		payrollData.put("earningColumns", payroll.getColumnEarnings());
		payrollData.put("deductionColumns", payroll.getColumnDeductions());
		
		List<Arrear> arrears =arrearsData.stream().filter(arrear-> String.valueOf(arrear.getPayrollId()).equals(String.valueOf(payroll.getId())) && arrear.getIsReimbursement().equals(true)).collect(Collectors.toList());
		if (!arrears.isEmpty())
			payrollData.put(ConstantUtility.IS_ARREAR_INCLUDED, true);
		else
			payrollData.put(ConstantUtility.IS_ARREAR_INCLUDED, false);
		List<Arrear> previousArrears =arrearsData.stream().filter(arrear-> String.valueOf(arrear.getPayrollId()).equals(String.valueOf(payroll.getId())) && arrear.getIsReimbursement().equals(false)).collect(Collectors.toList());		
		
		if (!previousArrears.isEmpty())
			payrollData.put(ConstantUtility.IS_PREVIOUS_ARREAR_INCLUDED, true);
		else
			payrollData.put(ConstantUtility.IS_PREVIOUS_ARREAR_INCLUDED, false);
		payrollData.put("accountEntry",
				"    INR" + payroll.getAccountNo().substring(0, Math.min(payroll.getAccountNo().length(), 4)));
		payrollData.put("creditTypeIcici", "    C");
		return payrollData;
	}
	/**
	 * @author shivangi
	 * 
	 *         Sets the changed Attributes that are different from current pay
	 *         register.
	 * 
	 * @param payroll
	 * @param payrollDto
	 * @return
	 */
	private Payroll setChangedAttributes(Payroll payroll, PayrollDto payrollDto, int month, int year) {
		PayRegister payRegister = payRegisterRepository.findAllByUserIdAndIsCurrent(payrollDto.getUserId(), true);
		if (payRegister.getBasicPay() != payroll.getBasicPay())
			payroll.setBasicPayChanged(true);
		else
			payroll.setBasicPayChanged(false);
		if (payRegister.getConveyance() != payroll.getConveyance())
			payroll.setConveyanceChanged(true);
		else
			payroll.setConveyanceChanged(false);
		if (payRegister.getHra() != payroll.getHra())
			payroll.setHraChanged(true);
		else
			payroll.setHraChanged(false);
		if (payRegister.getMedicalAllowance() != payroll.getMedicalAllowance())
			payroll.setMedicalChanged(true);
		else
			payroll.setMedicalChanged(false);
		if (payRegister.getProjectAllowance() != payroll.getProjectAllowance())
			payroll.setProjectAllowanceChanged(true);
		else
			payroll.setProjectAllowanceChanged(false);
		if (payRegister.getEmployerPfContribution() != payroll.getEmployerPfContribution())
			payroll.setEmployerPfContributionChanged(true);
		else
			payroll.setEmployerPfContributionChanged(false);
		if (payRegister.getLaptopAllowance() != payroll.getLaptopAllowance())
			payroll.setLaptopAllowanceChanged(true);
		else
			payroll.setLaptopAllowanceChanged(false);
		if (payRegister.getStatutoryMaternityPay() != payroll.getStatutoryMaternityPay())
			payroll.setSmpChanged(true);
		else
			payroll.setSmpChanged(false);
		payroll = payrollRepository.saveAndFlush(payroll);
		return payroll;
	}

	/**
	 * @author shivangi
	 * 
	 *         Get user Data for Payroll
	 * 
	 * @param payrolldata
	 * @param payroll
	 * @param accessToken
	 * @return
	 */
	private HashMap<String, Object> getUserData(HashMap<String, Object> payrolldata, Payroll payroll,
			String accessToken) {
		payrolldata.put(ConstantUtility.USER_ID, payroll.getUserId());
		payrolldata.put("payrollId", payroll.getId());
		payrolldata.put(ConstantUtility.EMPLOYEE_ID, payroll.getEmployeeId());
		payrolldata.put(ConstantUtility.DESIGNATION, payroll.getDesignation());
		payrolldata.put("grade", payroll.getGrade());
		payrolldata.put(ConstantUtility.DEPARTMENT, payroll.getDepartment());
		payrolldata.put("projects", payroll.getProjectNames());
		payrolldata.put("payDays", payroll.getPayDays());
		payrolldata.put(ConstantUtility.UNPAID_DAYS, payroll.getUnpaidDays());
		payrolldata.put("uan", payroll.getUan());
		payrolldata.put("pan", payroll.getPanNumber());
		payrolldata.put("bankName", (payroll.getBank()).getName());
		payrolldata.put("amountIcici", "        " + (new DecimalFormat("0.00").format(payroll.getNetPay())));
		payrolldata.put("accountNo", payroll.getAccountNo());
		payrolldata.put("ifsc", payroll.getIfsc());
		payrolldata.put("amountNeft", new DecimalFormat("0.00").format(payroll.getNetPay()));
		return payrolldata;
	}
	
	@SuppressWarnings("unchecked")
	private HashMap<String, Object> getUserDetails(HashMap<String, Object> payrolldata, Payroll payroll,
			String accessToken,List<Object> users) {
		Map<String, Object> userInformation = null;
		for(int i=0;i<users.size();i++) {
			Map<String, Object> data = (Map<String, Object>) users.get(i);
			Long userId=new Long((Integer) data.get("id"));
			if(userId==payroll.getUserId())
				userInformation=data;
		}
		if(userInformation==null)
		userInformation = (Map<String, Object>) feignLegacyInterface.getUserDetails(accessToken, payroll.getUserId()).get("data");

		Object employeeStatus = userInformation.get("employmentStatus");
		payrolldata.put(ConstantUtility.EMPLOYEE_NAME, userInformation.get("name"));
		payrolldata.put(ConstantUtility.BUSINESS_VERTICAL, userInformation.get(ConstantUtility.BUSINESS_VERTICAL));
		payrolldata.put(ConstantUtility.DATE_OF_JOINING, userInformation.get(ConstantUtility.DATE_OF_JOINING));
		payrolldata.put("employeeStatus", employeeStatus.toString());
		payrolldata.put("narrationIcici", "Sal paid to " + userInformation.get("name") + "  ");
		payrolldata.put("narrationHdfc", "Sal of " + userInformation.get("name") + " for "
				+ new DateFormatSymbols().getMonths()[payroll.getMonth() - 1] + " " + payroll.getYear());
		payrolldata.put("dateOfLeaving", userInformation.get("dateOfLeaving"));
		payrolldata.put("supervisorName", userInformation.get("supervisorName"));
		return payrolldata;
	}

	/**
	 * Get changed attributes if different from payregister.
	 * 
	 * @param payrolldata
	 * @param payroll
	 * @param accessToken
	 * @return booleans true/false according to changes
	 */
	private HashMap<String, Object> getChangedAttributes(HashMap<String, Object> payrolldata, Payroll payroll,String accessToken) {
		payrolldata.put("isBasicPayChanged", payroll.isBasicPayChanged());
		payrolldata.put("isHraChanged", payroll.isHraChanged());
		payrolldata.put("isConveyanceChanged", payroll.isConveyanceChanged());
		payrolldata.put("isMedicalChanged", payroll.isMedicalChanged());
		payrolldata.put("isProjectAllowanceChanged", payroll.isProjectAllowanceChanged());
		payrolldata.put("isEmployerPfContributionChanged", payroll.isEmployerPfContributionChanged());
		payrolldata.put("isLaptopAllowanceChanged", payroll.isLaptopAllowanceChanged());
		payrolldata.put("isSmpChanged", payroll.isSmpChanged());
		return payrolldata;
	}

	@Override
	public List<Long> getAttendanceVerifiedUsers(String accessToken, int month, int year) {
		List<AttendanceVerification> attendanceVerified = attendanceVerificationRepository
				.findAllByMonthAndYearAndIsAttendanceVerified(month, year, true);
		List<Long> attendanceVerifiedUsers = new ArrayList<>();
		if (!attendanceVerified.isEmpty()) {
			attendanceVerified.forEach(attendance -> {
				attendanceVerifiedUsers.add(attendance.getUserId());
			});
		}
		return attendanceVerifiedUsers;
	}

	@Override
	public List<AttendanceVerification> verifyAttendance(String accessToken, int month, int year,
			List<Integer> userIds) {
		List<AttendanceVerification> attendanceVerifiedList = new ArrayList<>();
		userIds.forEach(userId -> {
			AttendanceVerification attendance = attendanceVerificationRepository.findAllByUserIdAndMonthAndYearAndIsDeletedFalse(userId, month, year);
			if (attendance == null) {
				attendance = new AttendanceVerification();
				attendance.setMonth(month);
				attendance.setYear(year);
				attendance.setUserId(userId);
			}
			attendance.setAttendanceVerified(true);
			attendance = attendanceVerificationRepository.saveAndFlush(attendance);
			attendanceVerifiedList.add(attendance);
			Map<String, Object> getAttendanceDays = feignLegacyInterface.userLeavesdataForPayroll(accessToken, month, year, userId);
		
			Double unpaidDays = Double.parseDouble(getAttendanceDays.get(ConstantUtility.UNPAID_LEAVES).toString());
			Double nonWorkingDays = Double.parseDouble(getAttendanceDays.get(ConstantUtility.NON_WORKING_DAYS).toString());
			Integer lengthOfMonth = YearMonth.of(year, month).lengthOfMonth();
			Payroll payroll = payrollRepository.findAllByMonthAndUserIdAndYear(month, userId, year);
			if (payroll != null) {
				if (payroll.getPayDays() != (int) (new Double(lengthOfMonth) - nonWorkingDays) || payroll.getUnpaidDays()!= unpaidDays) {
					payroll.setPayDays((int) (new Double(lengthOfMonth) - nonWorkingDays));
					payroll.setUnpaidDays(unpaidDays.longValue());
					payroll = payrollRepository.saveAndFlush(payroll);
				}
			}
		});
		return attendanceVerifiedList;
	}

	public ArrearFile uploadArrearFile(ArrearFilesDto arrearFilesDto, String accessToken, long arrearId)
			throws Exception {
		Map<String, Object> result = fileuploadService.uploadFileOnS3Bucket(arrearFilesDto.getFileData(),
				arrearFilesDto.getFileName(), folderName);
		ArrearFile arrearFile = new ArrearFile();
		arrearFile.setFilePath(result.get("imagePath").toString());
		arrearFile.setFileName(result.get("originalFileName").toString());
		UserModel user = validator.tokenbValidate(accessToken);
		arrearFile.setFileUploaderName(user.getEmpName());
		arrearFile.setUploadedBy(user.getUserId());
		arrearFile.setUploadedDate(LocalDateTime.now());
		arrearFile.setDeleted(false);
		arrearFile.setArrearId(arrearId);
		arrearFile = arrearFileRepository.save(arrearFile);
		return arrearFile;
	}

	@Override
	public String downloadArrearFiles(String accessToken, String filePath, String fileName) {
		String response = fileuploadService.generatePresignedUrl(filePath, folderName, fileName);
		return response;
	}

	public Payroll setPayValuesAccordingToPayslip(Payroll payroll, PayrollDto payrollDto) {
		if (payrollDto.getColumnEarnings().contains("employerPFContribution"))
			payroll.setEmployerPfContribution(payrollDto.getEmployerPfContribution());
		else
			payroll.setEmployerPfContribution(0);
		if (payrollDto.getColumnEarnings().contains("basic"))
			payroll.setBasicPay(payrollDto.getBasicPay());
		else
			payroll.setBasicPay(0);
		if (payrollDto.getColumnEarnings().contains("hRA"))
			payroll.setHra(payrollDto.getHra());
		else
			payroll.setHra(0);
		if (payrollDto.getColumnEarnings().contains("conveyance"))
			payroll.setConveyance(payrollDto.getConveyance());
		else
			payroll.setConveyance(0);
		if (payrollDto.getColumnEarnings().contains("medical"))
			payroll.setMedicalAllowance(payrollDto.getMedicalAllowance());
		else
			payroll.setMedicalAllowance(0);
		if (payrollDto.getColumnEarnings().contains("project"))
			payroll.setProjectAllowance(payrollDto.getProjectAllowance());
		else
			payroll.setProjectAllowance(0);
		if (payrollDto.getColumnEarnings().contains("laptop"))
			payroll.setLaptopAllowance(payrollDto.getLaptopAllowance());
		else
			payroll.setLaptopAllowance(0);
		if(payrollDto.getColumnEarnings().contains(ConstantUtility.VARIABLE_PAY))
			payroll.setVariablePayAmount(payrollDto.getVariablePay());
		else
			payroll.setVariablePayAmount(0.0);
		if (payrollDto.getColumnEarnings().contains("paidLeave"))
			payroll.setPaidLeaveAdditions(payrollDto.getPaidLeaveAdditions());
		else
			payroll.setPaidLeaveAdditions(0);
		if(payrollDto.getColumnEarnings().contains(ConstantUtility.WORK_FROM_HOME_ALLOWANCE))
			payroll.setWorkFromHomeAllowance(payrollDto.getWorkFromHomeAllowance());
		else
			payroll.setWorkFromHomeAllowance(0.0);
		if(payrollDto.getColumnEarnings().contains(ConstantUtility.SPECIAL_ALLOWANCE))
			payroll.setSpecialAllowance(payrollDto.getSpecialAllowance());
		else
			payroll.setSpecialAllowance(0.0);
		
		if(payrollDto.getColumnEarnings().contains(ConstantUtility.VOLUNTARY_PAY))
			payroll.setVoluntaryPayAmount(payrollDto.getVoluntaryPay());
		else
			payroll.setVoluntaryPayAmount(0.0);
		
		double totalPay = payroll.getBasicPay() + payroll.getHra() + payroll.getConveyance()
				+ payroll.getMedicalAllowance() + payroll.getProjectAllowance() + payroll.getLaptopAllowance()
				+payroll.getSpecialAllowance()+payroll.getWorkFromHomeAllowance()+payroll.getVariablePayAmount()
				+ payroll.getEmployerPfContribution() + payroll.getStatutoryMaternityPay() 
				+ payroll.getPaidLeaveAdditions()+payroll.getVoluntaryPayAmount();
		payroll.setTotalMonthlyPay(totalPay);
		payroll = payrollRepository.saveAndFlush(payroll);
		return payroll;
	}

	@Override
	public AttendanceVerification unverifyAttendance(String accessToken, long userId, boolean isAttendanceVerified, int month, int year) {
		AttendanceVerification attendance = attendanceVerificationRepository.findAllByUserIdAndMonthAndYear(userId, month, year);
		if (attendance == null) {
			attendance = new AttendanceVerification();
			attendance.setMonth(month);
			attendance.setYear(year);
			attendance.setUserId(userId);
		}
		attendance.setAttendanceVerified(isAttendanceVerified);
		if (isAttendanceVerified) {
			Map<String, Object> getAttendanceDays = feignLegacyInterface.userLeavesdataForPayroll(accessToken, month, year, userId);
		
			Double unpaidDays = Double.parseDouble(getAttendanceDays.get(ConstantUtility.UNPAID_LEAVES).toString());
			Double nonWorkingDays = Double.parseDouble(getAttendanceDays.get(ConstantUtility.NON_WORKING_DAYS).toString());
			Integer lengthOfMonth = YearMonth.of(year, month).lengthOfMonth();
			Payroll payroll = payrollRepository.findAllByMonthAndUserIdAndYear(month, userId, year);
			if (payroll != null) {
				if (payroll.getPayDays() != (int) (new Double(lengthOfMonth) - nonWorkingDays) || payroll.getUnpaidDays()!= unpaidDays) {
					payroll.setPayDays((int) (new Double(lengthOfMonth) - nonWorkingDays));
					payroll.setUnpaidDays(unpaidDays.longValue());
					payroll = payrollRepository.saveAndFlush(payroll);
				}
			}
		}
		attendance = attendanceVerificationRepository.save(attendance);
		return attendance;
	}

	private Payroll setDeductionsAccordingToPayslip(Payroll payroll, PayrollDto payrollDto) {
		if (payrollDto.getColumnDeductions().contains("tds"))
			payroll.setTds(payrollDto.getTds());
		else
			payroll.setTds(0);
		if (payrollDto.getColumnDeductions().contains("employeePfContribution"))
			payroll.setEmployeePfContribution(payrollDto.getEmployeePfContribution());
		else
			payroll.setEmployeePfContribution(0);
		if (payrollDto.getColumnDeductions().contains(ConstantUtility.HEALTH_INSURANCE))
			payroll.setHealthInsurance(payrollDto.getHealthInsurance());
		else
			payroll.setHealthInsurance(0);
		if (payrollDto.getColumnDeductions().contains("infraDeduction"))
			payroll.setInfraDeductions(payrollDto.getInfraDeduction());
		else
			payroll.setInfraDeductions(0);
		if (payrollDto.getColumnDeductions().contains(ConstantUtility.LEAVE_DEDUCTION))
			payroll.setLeaveDeductions(payrollDto.getLeaveDeductions());
		else
			payroll.setLeaveDeductions(0);
		payroll = payrollRepository.saveAndFlush(payroll);
		return payroll;
	}

	@SuppressWarnings("unchecked")
	Map<Object,Object> getTimesheetHours(String accessToken, Long userId, Integer month, Integer year) {
		Map<String, Object> timsheetData = (Map<String, Object>) feignLegacyInterface.getUserMonthWiseTimesheetData(accessToken, month, year,
				userId);
		Map<String, Object> timeSheet = null;
		if(timsheetData!=null) {
			timeSheet=(Map<String, Object>) timsheetData.get("data");
		}
		Map<String, Object> data = new HashMap<String, Object>();
		if (timeSheet!=null && timeSheet.get("timesheetData") != null){
			data = (Map<String, Object>) timeSheet.get("timesheetData");
		}
		else {
			Map<String, Object> timesheetData = (Map<String, Object>) feignLegacyInterface.getTimeSheetHours(accessToken, month, year,
					userId);
			Map<String, Object> timeSheet2 = null;
			if(timesheetData!=null) {
				timesheetData=(Map<String, Object>) timsheetData.get("data");
			}
			
			List<Object> timesheetHoursData = (List<Object>) timesheetData.get(ConstantUtility.ATTENDANCE_DATA);
			if(timesheetHoursData!=null) {
			data = (Map<String, Object>) timesheetHoursData.get(0);
			}
		}
		double actualHours=0;
		double expectedHours=0;
		try {
			actualHours = Double.parseDouble((String) data.get(ConstantUtility.ACTUAL_HOURS));
		}
		catch(Exception e){
			log.info("---- Issue in actual Hours of ---"+userId);
			e.printStackTrace();
		}
		try {
			expectedHours = new Double(data.get(ConstantUtility.EXPECTED_HOURS).toString());
		}
		catch(Exception e) {
			log.info("---- Issue in Expected Hours of ---"+userId);
			e.printStackTrace();
		}
		Map<Object,Object> timesheetHours = new HashMap<>();
		timesheetHours.put(ConstantUtility.ACTUAL_HOURS, actualHours);
		timesheetHours.put(ConstantUtility.EXPECTED_HOURS, expectedHours);
		return timesheetHours;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<Object> userLeavesdataForPayroll(String accessToken, long userId, int month, int year) {
		Map<String, Object> getAttendanceDays = feignLegacyInterface.getUserLeavesdataForPayrollCalender(accessToken, month, year,
				userId);

		ArrayList<Object> attendanceData = (ArrayList<Object>) getAttendanceDays.get(ConstantUtility.ATTENDANCE_DATA);
		return attendanceData;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean sendMailOnNonCompliantTimesheet(String accessToken, long userId, int month, int year) {

			sendTimesheetMail(accessToken,userId, month, year);

		return true;
	}

	@Override
	public String savePayrollComment(String accessToken, long userId, int month, int year, String comment) {
		AttendanceVerification attendance = attendanceVerificationRepository.findAllByUserIdAndMonthAndYear(userId,
				month, year);
		if (attendance == null) {
			attendance = new AttendanceVerification();
			attendance.setMonth(month);
			attendance.setYear(year);
			attendance.setUserId(userId);
			attendance.setAttendanceVerified(false);
			attendance.setComment(comment);
		} else {
			attendance.setComment(comment);
		}
		attendance = attendanceVerificationRepository.saveAndFlush(attendance);
		return attendance.getComment();
	}

	@Override
	public List<Object> getPayrollComments(String accessToken, int month, int year) {
		List<AttendanceVerification> attendanceVerified = attendanceVerificationRepository.findAllByMonthAndYear(month,
				year);
		List<Object> commentList = new ArrayList<>();
		
		CriteriaBuilder criteria = em.getCriteriaBuilder();
		CriteriaQuery<PayrollPaidUnpaidDto> query = criteria.createQuery(PayrollPaidUnpaidDto.class);
		Root<Payroll> pr = query.from(Payroll.class);
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(criteria.equal(pr.get(ConstantUtility.MONTH), month));
		predicates.add(criteria.equal(pr.get("year"), year));
		predicates.add(criteria.equal(pr.get("isDeleted"), false));
		query.multiselect(pr.get("payDays"),pr.get(ConstantUtility.UNPAID_DAYS),pr.get("userId")).where(predicates.toArray(new Predicate[0]));
		List<PayrollPaidUnpaidDto> payrollList = em.createQuery(query).getResultList();
		Map<Long,Double> paidDaysMap = new HashMap<>();
		Map<Long,Double> unpaidDaysMap = new HashMap<>();
		
		payrollList.forEach(payroll ->{
			paidDaysMap.put(payroll.getUserId(), payroll.getPayDays());
			unpaidDaysMap.put(payroll.getUserId(), payroll.getUnpaidDays());
		});
		
		attendanceVerified.forEach(user -> {
			Map<String, Object> map = new HashMap<>();
			
				if(!paidDaysMap.containsKey(user.getUserId())) {
					map.put("paidDays", 0);
					map.put(ConstantUtility.UNPAID_DAYS, 0);
					map.put(ConstantUtility.IS_ATTENDANCE_VERIFIED_STRING, user.isAttendanceVerified());
					map.put(ConstantUtility.USER_ID, user.getUserId());
					map.put("payRollComment", user.getComment());
					map.put(ConstantUtility.IS_ATTENDANCE_VERIFIED_STRING, user.isAttendanceVerified());
					commentList.add(map);
				}else {
					map.put("paidDays", paidDaysMap.get(user.getUserId()));
					map.put(ConstantUtility.UNPAID_DAYS, unpaidDaysMap.get(user.getUserId()));
					map.put(ConstantUtility.IS_ATTENDANCE_VERIFIED_STRING, user.isAttendanceVerified());
					map.put(ConstantUtility.USER_ID, user.getUserId());
					map.put("payRollComment", user.getComment());
					map.put(ConstantUtility.IS_ATTENDANCE_VERIFIED_STRING, user.isAttendanceVerified());
					commentList.add(map);
					
			   }	
			
			
		});
		return commentList;
	}

	/**
	 * Get PayRoll Widgets Data
	 */
	@Override
	public Map<String, Object> getPayRollWidgetsData(int month, int year, String accessToken,String timesheetCompliance,List<Payroll> payrollList) {
		return getWidgetMap(payrollList);
	}
	
	private Map<String, Object> getWidgetMap(List<Payroll> payrollList) {
		double totalNetPay = 0.0;
		double totalDeduction = 0.0;
		double totalTds = 0.0;
		double totalHI=0.0;
		double totalEpf=0.0;
		double totalInfraDed=0.0;
		double leaveDeductions=0.0;
		double totalLaptopAllowance = 0.0;
		Double totalWfhAllowance = 0.0;
		Double variablePay = 0.0;
		Double voluntaryPay = 0.0;
		Double totalSpecialAllowance = 0.0;
		long totalCount=0;
		PayrollWidgetDto dto=new PayrollWidgetDto();
		Map<String, Object> payRollWidgetsData = new HashMap<>();
		int payrollListSize=payrollList.size();
		if (!payrollList.isEmpty()) {
			totalCount = payrollList.size();
			for(int i=0;i<payrollListSize;i++) {
				Payroll payroll=payrollList.get(i);
				totalNetPay=totalNetPay+payroll.getNetPay();
				totalDeduction=totalDeduction + payroll.getTds() + payroll.getHealthInsurance()+ payroll.getEmployeePfContribution() + payroll.getInfraDeductions() + payroll.getLeaveDeductions();
				totalTds=totalTds+payroll.getTds();
				totalLaptopAllowance=totalLaptopAllowance+payroll.getLaptopAllowance();
				totalHI=totalHI+payroll.getHealthInsurance();
				totalEpf=totalEpf+payroll.getEmployeePfContribution();
				totalInfraDed=totalInfraDed+payroll.getInfraDeductions();
				totalWfhAllowance=totalWfhAllowance+payroll.getWorkFromHomeAllowance();
				variablePay=variablePay+payroll.getVariablePayAmount();
				voluntaryPay=voluntaryPay+payroll.getVoluntaryPayAmount();
				totalSpecialAllowance=totalSpecialAllowance+payroll.getSpecialAllowance();
				leaveDeductions=leaveDeductions+payroll.getLeaveDeductions();
				dto=setDtoData(payroll,dto);
			}
		}
		double totalPay=totalNetPay+totalTds+totalHI+totalEpf+totalInfraDed;
		payRollWidgetsData.put("totalCount", totalCount);
		payRollWidgetsData.put(ConstantUtility.VOLUNTARY_PAY, voluntaryPay);
		payRollWidgetsData.put("totalNetPay", totalNetPay);
		payRollWidgetsData.put("totalWfhAllowance", totalWfhAllowance);
		payRollWidgetsData.put("totalSpecialAllowance", totalSpecialAllowance);
		payRollWidgetsData.put("totalDeduction", totalDeduction);
		payRollWidgetsData.put(ConstantUtility.VARIABLE_PAY, variablePay);
		payRollWidgetsData.put("totalTds", totalTds);
		payRollWidgetsData.put("totalHI", totalHI);
		payRollWidgetsData.put("totalEpf", totalEpf);
		payRollWidgetsData.put("totalEpf", totalEpf);
		payRollWidgetsData.put("totalInfraDed", totalInfraDed);
		payRollWidgetsData.put(ConstantUtility.LEAVE_DEDUCTION, leaveDeductions);
		payRollWidgetsData.put("totalPay", totalPay);
		payRollWidgetsData.put("totalLaptopAllowance", totalLaptopAllowance);
		payRollWidgetsData=getStatuswiseData(payRollWidgetsData, dto);
		return payRollWidgetsData;
	}
	
	private PayrollWidgetDto setDtoData(Payroll payroll,PayrollWidgetDto dto) {
		Double netPay=payroll.getNetPay();
		if(payroll.getPayRollStatus().equals(PayRollStatus.PROCESSED)) {
			dto.setProcessedCount(dto.getProcessedCount()+1);
			dto.setTotalProcessedNetPay(dto.getTotalProcessedNetPay()+netPay);
		}
		else if(payroll.getPayRollStatus().equals(PayRollStatus.PENDING)) {
			dto.setPendingCount(dto.getPendingCount()+1);
			dto.setTotalPendingNetPay(dto.getTotalPendingNetPay()+netPay);
		}
		else if(payroll.getPayRollStatus().equals(PayRollStatus.VERIFIED)) {
			dto.setVerfiedCount(dto.getVerfiedCount()+1);
			dto.setTotalVerifiedNetPay(dto.getTotalVerifiedNetPay()+netPay);
		}
		else if(payroll.getPayRollStatus().equals(PayRollStatus.ONHOLD)) {
			dto.setOnholdCount(dto.getOnholdCount()+1);
			dto.setTotalOnholdNetPay(dto.getTotalOnholdNetPay()+netPay);
		}
		else if(payroll.getPayRollStatus().equals(PayRollStatus.TIMESHEET_ISSUE)) {
			dto.setTimesheetIssueCount(dto.getTimesheetIssueCount()+1);
			dto.setTimeSheetIssueNetPay(dto.getTimeSheetIssueNetPay()+netPay);
		}
		else if(payroll.getPayRollStatus().equals(PayRollStatus.ATTENDANCE_ISSUE)) {
			dto.setAttendanceIssueCount(dto.getAttendanceIssueCount()+1);
			dto.setAttendanceIssueNetPay(dto.getAttendanceIssueNetPay()+netPay);
		}
		else {
			dto.setFileprocessedCount(dto.getFileprocessedCount()+1);
			dto.setTotalFileProcessedNetPay(dto.getTotalFileProcessedNetPay()+netPay);
		}
		return dto;
	}
	
	private Map<String,Object> getStatuswiseData(Map<String,Object> payRollWidgetsData, PayrollWidgetDto dto){
		payRollWidgetsData.put("processedCount", dto.getProcessedCount());
		payRollWidgetsData.put("pendingCount", dto.getPendingCount());
		payRollWidgetsData.put("verfiedCount", dto.getVerfiedCount());
		payRollWidgetsData.put("fileprocessedCount", dto.getFileprocessedCount());
		payRollWidgetsData.put("onholdCount", dto.getOnholdCount());
		payRollWidgetsData.put("timesheetIssueCount", dto.getTimesheetIssueCount());
		payRollWidgetsData.put("totalVerifiedNetPay", dto.getTotalVerifiedNetPay());
		payRollWidgetsData.put("totalOnholdNetPay", dto.getTotalOnholdNetPay());
		payRollWidgetsData.put("attendanceIssueCount", dto.getAttendanceIssueCount());
		payRollWidgetsData.put("totalAccountsIssueNetPay", dto.getAttendanceIssueNetPay());
		payRollWidgetsData.put("totalFileProcessedNetPay", dto.getTotalFileProcessedNetPay());
		payRollWidgetsData.put("totalProcessedNetPay", dto.getTotalProcessedNetPay());
		payRollWidgetsData.put("totalPendingNetPay", dto.getTotalPendingNetPay());
		payRollWidgetsData.put("totalTimesheetIssueNetPay", dto.getTimeSheetIssueNetPay());
		return payRollWidgetsData;
	}
	
	@Override
	public Map<String, Object> deletePayroll(String accessToken, Long payrollId){
		Payroll payroll = payrollRepository.findAllById(payrollId);
		Map<String, Object> response = new HashMap<>();
		if(payroll != null) {
			payroll.setDeleted(true);
			payrollRepository.save(payroll);
			response.put(ConstantUtility.MESSAGE, "deleted successfully!");
			return response;
		}else {
			response.put(ConstantUtility.MESSAGE, "No payrolls found with payroll id: "+ payrollId);
			return response;
		}
	}

	

	@Override
	public Map<String, Object> getArrearWidgetData(int month, int year, String accessToken, boolean isReimbursement) {
		List<Arrear> allArrears = arrearRepository.findAllByCreationMonthAndCreationYearAndIsReimbursementAndIsDeleted(month, year,isReimbursement,false);
		double includedSum=0;
		double nonIncludedSum=0;
		double totalSum=0;
		int size=allArrears.size();
		for(int i=0;i<size;i++) {
			Arrear arrear=allArrears.get(i);
			totalSum=totalSum+arrear.getArrearAmount();
			if(arrear.isArrearIncluded())
				includedSum=includedSum+arrear.getArrearAmount();
			else
				nonIncludedSum=nonIncludedSum+arrear.getArrearAmount();
		}
		Map<String, Object> map=new HashMap<>();
		map.put("totalSum", totalSum);
		map.put("includedSum", includedSum);
		map.put("nonIncludedSum", nonIncludedSum);
		return map;
	}

	@Override
	public boolean changePayrollAndPayslipStatus(long payrollId) {
		Payroll payroll = payrollRepository.findAllById(payrollId);
		Optional<Payslip> payslip = payslipRepository.findByPayrollId(payrollId);
		if(payslip.isPresent()) {
			payroll.setPayRollStatus(PayRollStatus.PENDING);
			payrollRepository.saveAndFlush(payroll);
			payslip.get().setPayslipStatus(PayslipStatus.SAVED);
			payslipRepository.saveAndFlush(payslip.get());
			return true;
		}
		return false;
	}
	public Map<String, Object> getIsAttendanceVerifiedForUser(long userId, int month, int year){
		Map<String, Object> map = new HashMap<String, Object>(); ;
		AttendanceVerification attendanceVerification =attendanceVerificationRepository.findByUserIdAndMonthAndYear(userId,month, year);
		if(attendanceVerification!=null)
			map.put(ConstantUtility.IS_ATTENDANCE_VERIFIED, attendanceVerification.isAttendanceVerified());
		
		return map;
				
	}

	@Override
	public HashMap<String, Object> setPayrollOnPriority(long payrollId, String accessToken) {
		Payroll payroll = payrollRepository.findAllById(payrollId);
		if(payroll != null) {
			if(payroll.isPriority())
				payroll.setPriority(false);
			else
				payroll.setPriority(true);
			payroll = payrollRepository.saveAndFlush(payroll);
			List<Object> users = payRegisterService.getUsersForPayregister(accessToken, "All", payroll.getMonth(), payroll.getYear());
			Map<String, Object> timesheets = this.getPayrollUsersTimesheet(accessToken, payroll.getMonth(), payroll.getYear());
			Map<Object, Object> timesheet= (Map<Object, Object>) timesheets.get(Long.toString(payroll.getUserId()));
			List<AttendanceVerification> attendanceVerification = attendanceVerificationRepository.findAllByMonthAndYear(payroll.getMonth(), payroll.getYear());
			List<Payslip> payslips = payslipRepository.findAllByPayslipMonthAndPayslipYear(
					payroll.getMonth(), payroll.getYear());
			List<Arrear> arrears = arrearRepository.findByCreationMonthAndCreationYearAndIsArrearIncludedAndIsDeleted(payroll.getMonth(), payroll.getYear(), true,false);
			return getPayrollData(payroll, accessToken, payroll.getMonth(), payroll.getYear(), timesheet,users, attendanceVerification, payslips, arrears);
		}
		return null;
	}

	@Override
	public String setVerifiedPaidDays(String accessToken, int month, int year, List<Integer> userIds) {
		try {
			userIds.forEach(userId -> {


			});
			return "Verified";
		} catch (Exception e) {
			log.info(ConstantUtility.EXCEPTION_DEBUG_STRING+e);
			return "Unable to Verify";
		}
	}

	@Override
	public boolean resetPayrollPriority(int month, int year) {
		List<Payroll> priorityPayrolls = payrollRepository.findAllByMonthAndYearAndIsDeletedFalseAndIsPriority(month, year, true);
		if(priorityPayrolls != null) {
			priorityPayrolls.forEach( payroll -> {
				payroll.setPriority(false);
			});
			payrollRepository.saveAll(priorityPayrolls);
			return true;
		}
		return false;
	}
	
	//@Scheduled(cron = "0 0 */4 * * ?")
	@CacheEvict(cacheNames="usersTimesheet", allEntries=true)
	public void flushTimesheetCache() { }
	
	@CacheEvict(cacheNames="payrolls", allEntries=true)
	public void flushPayrollCache() { }

	@Override
	public boolean addPayrollComments(long payrollId, String comments) {
		Optional<Payroll> payroll = payrollRepository.findById(payrollId);
		if(payroll.isPresent()) {
			payroll.get().setPayrollComment(comments);
			payrollRepository.saveAndFlush(payroll.get());
			return true;
		} else {
			return false;
		}
	}
	
	private String sendTimesheetMail(String accessToken, long userId, int month, int year) {

		Map<String, Object> timesheetData = (Map<String, Object>) feignLegacyInterface.getTimeSheetHours(accessToken, month, year,
				userId).get("data");
		List<Object> timesheetHoursData = (List<Object>) timesheetData.get(ConstantUtility.ATTENDANCE_DATA);
		Map<String, Object> data = (Map<String, Object>) timesheetHoursData.get(0);
		if (Double.parseDouble(data.get(ConstantUtility.ACTUAL_HOURS).toString()) < Double
				.parseDouble(data.get(ConstantUtility.EXPECTED_HOURS).toString())) {

			final String uri = ConstantUtility.HTTPS_STRING + environmentUrl
					+ "/zuul/dashboard_legacy/api/v1/timeTracker/sendMonthlyTimeSheet?id=" + userId + "&month=" + month
					+ "&year=" + year + "&callFrom=" + "Accounts";
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers.add("user-agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
			HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
			ResponseEntity<HashMap> response = restTemplate.exchange(uri, HttpMethod.GET, entity, HashMap.class);
			Map<String, Object> result = response.getBody();
			Map<String, Object> dataResult = (HashMap<String, Object>) result.get("data");
			return result.toString();
		}
		return "Timesheet Already Compliant!!! ";
	}
	private Boolean sendMailOnAccountsIssue(String accessToken, long userId, int month, int year) {
		Map<String, Object> userInformation = (Map<String, Object>) feignLegacyInterface.getUserDetails(accessToken, userId).get("data");
		String userName=(String) userInformation.get("name");
		Boolean mailSent=false;
		String monthName=new DateFormatSymbols().getMonths()[month-1].toString();
		Context context = new Context();
		context.setVariable("userName", userName);
		context.setVariable("monthName", monthName);
		context.setVariable("year", year);
		context.setVariable(ConstantUtility.CURRENT_YEAR, LocalDate.now().getYear());
		String subject = "Attendance Issue ||  Payroll  ||" +monthName+"||"+year;
		String[] ccArray = new String[1];
		ccArray[0]=accountsMail;
		try {
		mailService.sendScheduleHtmlMailWithCc(hrMail, subject, context, "Accounts-Issue-Marked-Mail", ccArray);
		mailSent=true;
		}
		catch(Exception ex) {
			ex.getMessage();
		}
		return mailSent;
		
	}

	
	@Override
	public Map<String, Object> getPayrollDetails(long userId,int year,int month,String accessToken) {
		Payroll payroll=payrollRepository.findAllByMonthAndUserIdAndYear(month, userId, year);
		
		Map<String, Object> payslipDetails = new HashMap<>();
		if(payroll!=null) {
			payslipDetails = (HashMap<String, Object>) payslipService.getPayslip(payroll.getId(), month, year);
			payslipDetails.put(ConstantUtility.BANK_NAME, payroll.getBank().getName());
			payslipDetails.put("accountNumber", payroll.getAccountNo());
			payslipDetails.put("ifsc", payroll.getIfsc());
			payslipDetails.put(ConstantUtility.PAN_NUMBER, payroll.getPanNumber());
			payslipDetails.put("uan", payroll.getUan());
		}
		Map<String, Object> userInformation = (Map<String, Object>) feignLegacyInterface.getUserDetails(accessToken, userId).get("data");

		if (userInformation.containsKey("name")) {
			payslipDetails.put(ConstantUtility.EMPLOYEE_NAME, userInformation.get("name"));
			payslipDetails.put(ConstantUtility.DESIGNATION, userInformation.get(ConstantUtility.DESIGNATION));
			payslipDetails.put("dateOfJoining", userInformation.get("dateOfJoining"));
		}
		return payslipDetails;
	}
	
	@Override
	public Map<String,Object> deleteArrear(String accessToken, Long arrearId) {
		Arrear arrear = arrearRepository.findAllById(arrearId);
		
		Map<String, Object> response = new HashMap<>();
		if(arrear != null && !arrear.isArrearIncluded()) {
			arrear.setIsDeleted(true);
			arrearRepository.save(arrear);
			response.put(ConstantUtility.MESSAGE, "deleted successfully!");
			return response;
		}else {
			response.put(ConstantUtility.MESSAGE, "No payrolls found with payroll id: "+ arrearId);
			return response;
		}
	}

    public Payroll setVariablePay(Payroll payroll,int month,int year,Long userId) {
    	payroll.setVariablePayAmount(0.0);
	    VariablePay variablePay = variablePayRepository.findByUserIdAndMonthAndYearAndIsDeletedFalse(userId,month,year);
	    if(variablePay!=null) {
	        payroll.setVariablePayAmount(variablePay.getAmount());
        	variablePay.setIsIncludeInPayroll(true);
        	variablePayRepository.save(variablePay);
    	}
    	return payroll;
    }

	@Override
	public Map<String, Object> getBuWiseReimbursment(String accessToken, String bu,int month ,int year) {
		Map<String, Object> response = new HashMap<>();
		List<Long> userId = (List<Long>) feignLegacyInterface.getBuWiseEmployee(accessToken, bu).get("data");
		Query query = entityManager.createNativeQuery(
				"select * from pay_roll where month =:month and year=:year and is_deleted=false and user_id in (:userIds)",
				Payroll.class);
		query.setParameter("userIds", userId);
		query.setParameter(ConstantUtility.MONTH, month);
		query.setParameter("year", year);
		List<Payroll> payroll = query.getResultList();
		Double reimbursment = payroll.stream().mapToDouble(payrol -> payrol.getTotalReimbursement()).sum();
		response.put("reimbursmentAmount", reimbursment);
		return response;
	}

	@Override
	public List<Map<Object, Object>> getUserGradeList(String accessToken, int month, int year) {
		Query q = entityManager.createNativeQuery("select user_id,grade from pay_roll where month=:month and year=:year and is_deleted=false",Payroll.class);
		q.setParameter(ConstantUtility.MONTH, month);
		q.setParameter("year", year);
		List<Object []> payrolls = q.getResultList(); 
		List<Map<Object,Object>> list=new ArrayList<>();
		payrolls.forEach(payroll->{
			Map<Object,Object>res=new HashMap<>();
			res.put(payroll[0], payroll[1])	;
			list.add(res);
			});
		return list;
	}
	public Double getVoluntaryPayMonthWise(int month, int year) {
		List<Payroll> payrolls = payrollRepository.findAllByMonthAndYearAndIsDeletedFalse(month, year);		
		Double voluntaryPayAmount = payrolls.stream().collect(Collectors.summingDouble(Payroll::getVoluntaryPayAmount));
		return voluntaryPayAmount;
	}

	@Override
	public boolean setPayRollApproval(Long userId, int month, int year, boolean status,String comment) {
		Payroll payroll = payrollRepository.findAllByMonthAndUserIdAndYearAndIsDeletedFalse(month,userId, year);
			payroll.setBuPayrollApproval(status);
			if(status) {
				payroll.setBuApprovalComment(comment);	
			}
			payrollRepository.save(payroll);	
			return true;
	}

	
	@Override
	public String getBuAppovalComment(Long userId,int month, int year) {
		Payroll payroll = payrollRepository.findAllByUserIdAndMonthAndYear(userId, month, year);
		String result;
		if(Objects.nonNull(payroll))
			result= payroll.getBuApprovalComment();	
		else 
			result="payroll not exist";
		
		return result;
	}

	
	@Override
	public List<Map<String,Object>> getPayrollStatusList(String accessToken, int month, int year) {
		List<Payroll> p = payrollRepository.findAllByMonthAndYearAndIsDeletedFalse(month, year);
		List<Map<String,Object>> list = new ArrayList<>();
			for(Payroll payroll : p) {
				Map<String, Object> map = new HashMap<>();
				map.put("userId",payroll.getUserId());
				map.put("comment",payroll.getBuApprovalComment());
				map.put("status",payroll.getBuPayrollApproval());
				list.add(map);
			}
			return list;		
	}



	

	/**
	 *
	 * @author pankaj
	 * 
	 * @header accessToken
	 * @param userId
	 * @param month
	 * @param year
	 * @return boolean
	 *
	 * 
	 * @apiNote Getting userDetails, buHead emailId and BU Name from feignLeagacy, send mail
	 * Mail To:- BuHead
	 * CC:- Accounts Team & Resourcing Team
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean sendMailForPayrollGenerationOfNonCompliantUser(String accessToken, long userId, int month, int year) {
		LocalDateTime now =  LocalDateTime.now();
		Map<String,Object> userDetails =  (Map<String, Object>) feignLegacyInterface.getUserDetails(accessToken, userId).get("data");
		String businessVertical  =userDetails.get(ConstantUtility.BUSINESS_VERTICAL).toString();
		String userName = userDetails.get("name").toString();
		boolean result;
		Date date = new Date();
		String approveUrl  = ConstantUtility.HTTPS_STRING + environmentUrl + "#/nonCompliantUser?userId="+userId +"&month="+month+"&year="+year+"&approve=true";
		String disapproveUrl =ConstantUtility.HTTPS_STRING + environmentUrl + "#/nonCompliantUser?userId="+userId +"&month="+month+"&year="+year+"&approve=false";
		String monthName=new DateFormatSymbols().getMonths()[month-1].toString();
		Map<String,Object> buHeadInfo = (Map<String, Object>)feignLegacyInterface.getBuOwnerInfo(accessToken, businessVertical).get("data");
		String email = buHeadInfo.get("ownerEmail").toString();
		String buHead = buHeadInfo.get("ownerName").toString();
		String[] ccArray = new String[1];
		ccArray[0] =resourcingMail;
		String subject ="TimeSheet Compliance Update";
		Context context = new Context();
		context.setVariable("buHead", buHead);
		context.setVariable(ConstantUtility.EMPLOYEE_NAME, userName);
		context.setVariable("monthName", monthName.toUpperCase());
		context.setVariable("year", year);
		context.setVariable("approveUrl",approveUrl);
		context.setVariable("disapproveUrl",disapproveUrl);
		context.setVariable(ConstantUtility.CURRENT_YEAR, now.getYear());
		try {
		mailService.sendScheduleHtmlMailWithCc(email, subject, context, "Bu-Head-Approval-Mail", ccArray);
		result=true;
		}catch(Exception e) {
			result=false;
		}
		if(result) {
			TimesheetCompVerification timesheetVerification = new TimesheetCompVerification();
			timesheetVerification.setUserId(userId);
			timesheetVerification.setMonth(month);
			timesheetVerification.setYear(year);
			timesheetVerification.setIsTimesheetComplianceVerified(null);
			timesheetVerification.setCreationDate(new Date());	
			timesheetVerificationRepository.save(timesheetVerification);
		}	
		return result;
	}

	
	/**
	 *
	 * @author pankaj
	 * 
	 * @header accessToken
	 * @param userId
	 * @param month
	 * @param year
	 * @return boolean
	 *
	 * 
	 * @apiNote verify timesheet compliance 
	 */
	@Override
	public List<TimesheetCompVerification> timesheetComplianceVerify(String accessToken, int month, int year, List<Integer> userIds) {
		List<TimesheetCompVerification> verifyPayrollList = new ArrayList<>();
		userIds.forEach(userId -> {
			TimesheetCompVerification timesheetVerification =timesheetVerificationRepository.findAllByUserIdAndMonthAndYearAndIsDeletedFalse(userId, month, year);
			if (timesheetVerification == null) {
				timesheetVerification = new TimesheetCompVerification();
				timesheetVerification.setMonth(month);
				timesheetVerification.setYear(year);
				timesheetVerification.setUserId(userId);
				timesheetVerification.setIsTimesheetComplianceVerified(null);
			}
			timesheetVerification = timesheetVerificationRepository.saveAndFlush(timesheetVerification);
			verifyPayrollList.add(timesheetVerification);
		});
		return verifyPayrollList;
	}

	/**
	 *@author pankaj
	 *
	 * Get all the data of a user for whom the mail to seek approval has been send in previous months
	 *
	 *@param userId
	 *@return List<Map<String,Object>>
	 *@apiNote getAll data of a user from timesheetCompVerification table and adds creation date to the map and returns
	 */
	@Override
	public List<Map<String, Object>> getTimeSheetMailHistory(long userId) {
		List<TimesheetCompVerification> users = timesheetVerificationRepository.findAllByUserIdAndIsDeletedFalse(userId);
		List<Map<String, Object>> list = new ArrayList<>();
		if (users != null) {
			for (TimesheetCompVerification timesheetCompVerification : users) {
				Map<String, Object> map = new HashMap<>();
				map.put("date", timesheetCompVerification.getCreationDate());
				list.add(map);
			}
		}
		return list;
	}

	@Override
	public List<Payroll> getBuWisePayrollsForMonth(int month, int year, List<Long> buUsers, String priority,
			String businessVertical) {
		List<Payroll> payrolls = new ArrayList<Payroll>();
		if(!priority.equals("")) {
			if(priority.equals("High"))
				payrolls = payrollRepository.findAllByUserIdInAndMonthAndYearAndIsDeletedFalseAndIsPriority(buUsers, month, year,true);
			else
				payrolls = payrollRepository.findAllByUserIdInAndMonthAndYearAndIsDeletedFalseAndIsPriority(buUsers, month, year, false);				
		}
		else 
			payrolls = payrollRepository.findAllByUserIdInAndMonthAndYearAndIsDeletedFalse(buUsers, month, year);
		
		return payrolls;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object> getBuWisePayrolls(int month, int year, String accessToken, String timesheetCompliance,
			String priority, Map<String, Object> timesheets, List<Payroll> payrolls, List<Object> usersData, List<Long> buUsers) {
		List<Object> payrollList = new ArrayList<>();
		List<AttendanceVerification> attendanceVerification = attendanceVerificationRepository.findAllByUserIdInAndMonthAndYear(buUsers, month, year);
		List<Payslip> payslips = payslipRepository.findAllByUserIdInAndPayslipMonthAndPayslipYear(buUsers, month, year);
		List<Arrear> arrears = arrearRepository.findByUserIdInAndCreationMonthAndCreationYearAndIsArrearIncludedAndIsDeleted(buUsers, month, year, true,false);
	
		for (Payroll payroll : payrolls) {
			Map<Object, Object> timesheet= (Map<Object, Object>) timesheets.get(Long.toString(payroll.getUserId()));
			HashMap<String, Object> payrolldata = getPayrollData(payroll, accessToken, month, year, timesheet,usersData,attendanceVerification,payslips,arrears);
			payrollList.add(payrolldata);	
		}
		return payrollList;
	}
}
