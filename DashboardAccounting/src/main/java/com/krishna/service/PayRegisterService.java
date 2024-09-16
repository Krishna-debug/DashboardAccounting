package com.krishna.service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.krishna.Interfaces.IPayRegisterService;
import com.krishna.Interfaces.IPayslipService;
import com.krishna.controller.FeignLegacyInterface;
import com.krishna.domain.Bank;
import com.krishna.domain.LeaveCostPercentage;
import com.krishna.domain.PayRegister;
import com.krishna.domain.PayRevisions;
import com.krishna.domain.Payroll;
import com.krishna.domain.Payslip;
import com.krishna.domain.UserModel;
import com.krishna.dto.PayRegisterDto;
import com.krishna.enums.PayRegisterStatus;
import com.krishna.enums.PayslipStatus;
import com.krishna.repository.payroll.BankRepository;
import com.krishna.repository.payroll.LeaveCostPercentageRepository;
import com.krishna.repository.payroll.PayRegisterRepository;
import com.krishna.repository.payroll.PayRevisionRepository;
import com.krishna.repository.payroll.PayrollRepository;
import com.krishna.repository.payroll.PayslipRepository;
import com.krishna.security.JwtValidator;
import com.krishna.util.ConstantUtility;


@Service
public class PayRegisterService implements IPayRegisterService {

	@Autowired
	PayRegisterRepository payRegisterRepository;

	@Autowired
	JwtValidator validator;

	@Autowired
	BankRepository bankRepository;

	@Autowired
	PayRevisionRepository payrevisionRepository;
	
	@Autowired
	PayrollRepository payrollRepository;
	
	@Autowired
	PayslipRepository payslipRepository;
	
	@Autowired
	IPayslipService payslipService;
	
	@Autowired
	LeaveCostPercentageRepository leaveCostPercentageRepository;
	
	@Autowired
	FeignLegacyInterface feignLegacyInterface;

	/**
	 * Get User Details
	 *
	 * @param accessToken
	 * @return userDetails
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<Object> getUserDetails(String accessToken, String userStatus,int month,int year) throws Exception {
		Map<String, Object> allUsers = (Map<String, Object>) feignLegacyInterface.getAllUsers(accessToken,month,year, userStatus).get("data");
		ArrayList<Object> usersData = (ArrayList<Object>) allUsers.get(ConstantUtility.USER_LIST);
		return usersData;
	}

	/**
	 * Create Payregister	 */
	@Override
	public Map<String, Object> createPayregister(String accessToken, PayRegisterDto payRegister,int month,int year) throws Exception {
		Map<String, Object> salaryAccount=new HashMap<>();
		if (payRegister != null) {
			PayRegister payRegisterData = payRegisterRepository.findAllByUserIdAndIsCurrent(payRegister.getUserId(), true);
			if(payRegister.getPanNumber()==null || payRegister.getUan()==null || payRegister.getIfsc()==null || payRegister.getAccountNo()==null) {
				payRegister=setNullableString(payRegister);
			}
			PayRegister salaryAccountData=savePayRollData(payRegister, accessToken,month,year);
			salaryAccount=getExistingCandidates(salaryAccountData,accessToken);
			salaryAccount = getPayRollGenerationStatus(salaryAccount, month, year, salaryAccountData);
			salaryAccount=(HashMap<String, Object>) getUserDetails(salaryAccount,salaryAccountData.getUserId(),accessToken);
			LocalDateTime effectiveDateDto=new Timestamp(payRegister.getEffectiveDate()).toLocalDateTime();
			if (payRegisterData != null) {
				if(!payRegisterData.getPanNumber().equals(payRegister.getPanNumber()) || !payRegisterData.getUan().equals(payRegister.getUan()) || payRegisterData.getBank().getId()!=payRegister.getBank() 
				|| !payRegisterData.getAccountNo().equals(payRegister.getAccountNo()) || !payRegisterData.getIfsc().equals(payRegister.getIfsc()) || payRegisterData.getPfSubscription()!=payRegister.getPfSubscription()
				|| payRegisterData.getBasicPay()!=payRegister.getBasicPay() || payRegisterData.getHra()!=payRegister.getHra() || payRegisterData.getConveyance()!=payRegister.getConveyance()
				|| payRegisterData.getMedicalAllowance()!=payRegister.getMedicalAllowance() || payRegisterData.getProjectAllowance()!=payRegister.getProjectAllowance() || payRegisterData.getEmployerPfContribution()!=payRegister.getEmployerPfContribution()
				|| payRegisterData.getTotalMonthlyPay()!=payRegister.getTotalMonthlyPay() || payRegisterData.getStatutoryMaternityPay()!=payRegister.getStatutoryMaternityPay() || payRegisterData.getLaptopAllowance()!=payRegister.getLaptopAllowance() 
				|| !payRegisterData.getEffectiveDate().isEqual(effectiveDateDto)) {
					setPayRevisions(payRegister,payRegisterData,accessToken);
				}
				payRegisterData.setCurrent(false);
				payRegisterData.setEffectiveTo(new Timestamp(payRegister.getEffectiveDate()).toLocalDateTime().minusDays(1));
				payRegisterData.setLastUpdatedOn(LocalDateTime.now());
				payRegisterRepository.saveAndFlush(payRegisterData);
			}
		}
		return salaryAccount;
	}

	private Map<String, Object> getPayRollGenerationStatus(Map<String, Object> salaryAccount, int month, int year, PayRegister payee) {
		if(payee != null) {
			Payroll payroll = payrollRepository.findAllByMonthAndUserIdAndYearAndIsDeleted(month, payee.getUserId(), year, false);
			if (payroll != null)
				salaryAccount.put(ConstantUtility.IS_PAYROLL_GENERATED, true);
			else
				salaryAccount.put(ConstantUtility.IS_PAYROLL_GENERATED, false);
		}
		return salaryAccount;
	}

	/**
	 * Saves the data in PayRegister Domain
	 * 
	 * @param salaryAccount
	 * @param payRegister
	 * @param user
	 * @return saved PayRegsiter
	 */
	private PayRegister savePayRollData(PayRegisterDto payRegister, String accessToken,int month,int year) {
		PayRegister salaryAccount = new PayRegister();
		UserModel user = validator.tokenbValidate(accessToken);
		salaryAccount=new PayRegister();
		salaryAccount.setUserId(payRegister.getUserId());
		Map<String, Object> userInformation = (Map<String, Object>) feignLegacyInterface.getUserDetails(accessToken, payRegister.getUserId()).get("data");
		salaryAccount.setEmployeeId((String) userInformation.get(ConstantUtility.EMPLOYEE_ID));
		salaryAccount.setAccountNo(payRegister.getAccountNo());
		Bank bank = bankRepository.findAllById(payRegister.getBank());
		salaryAccount.setBank(bank);
		salaryAccount.setComment(payRegister.getComment());
		salaryAccount.setBasicPay(payRegister.getBasicPay());
		salaryAccount.setUan(payRegister.getUan());
		salaryAccount.setPanNumber(payRegister.getPanNumber());
		salaryAccount.setConveyance(payRegister.getConveyance());
		salaryAccount.setEffectiveDate(new Timestamp(payRegister.getEffectiveDate()).toLocalDateTime());
		salaryAccount.setEmployerPfContribution(payRegister.getEmployerPfContribution());
		salaryAccount.setHra(payRegister.getHra());
		salaryAccount.setIfsc(payRegister.getIfsc());
		salaryAccount.setMedicalAllowance(payRegister.getMedicalAllowance());
		salaryAccount.setPfSubscription(payRegister.getPfSubscription());
		salaryAccount.setProjectAllowance(payRegister.getProjectAllowance());
		salaryAccount.setTotalMonthlyPay(payRegister.getTotalMonthlyPay());
		salaryAccount.setStatutoryMaternityPay(payRegister.getStatutoryMaternityPay());
		salaryAccount.setStatus(payRegister.getStatus());
		salaryAccount.setCreationDate(LocalDateTime.now());
		salaryAccount.setLaptopAllowance(payRegister.getLaptopAllowance());
		double annualCtc=(payRegister.getTotalMonthlyPay()*12);
		double salaryExcludedLA=(payRegister.getTotalMonthlyPay()-payRegister.getLaptopAllowance())*12;
		salaryAccount.setAnnualCTC(annualCtc);
		List<LeaveCostPercentage> prevCosts = leaveCostPercentageRepository.findAllByIsDeleted(false);
		LeaveCostPercentage leaveCostPerc=prevCosts.get(0);
		double paidLeavesAmount=(leaveCostPerc.getLeaveCostPercentage()*(salaryExcludedLA))/100;
		salaryAccount.setPaidLeavesAmount(paidLeavesAmount);
		salaryAccount.setTotalAnnualCtc(annualCtc+paidLeavesAmount);
		salaryAccount.setCurrent(true);
		salaryAccount.setLastUpdatedOn(LocalDateTime.now());
		salaryAccount.setCreatedBy(user.getUserId());
		salaryAccount = payRegisterRepository.saveAndFlush(salaryAccount);
		return salaryAccount;
	}

	@Override
	public List<Object> getAllPayRegisters(String accessToken, String userStatus, int month, int year, String payrollStatus,List<Object> usersData) throws ParseException {
		List<Object> payRegisters = getPayRegisterData(month, year, accessToken,payrollStatus,usersData);
		return payRegisters;
	}
	
	@SuppressWarnings("unchecked")
	private List<Object> getPayRegisterData( int month, int year, String accessToken, String payrollStatus,List<Object> usersData) {
		List<Object> payRegisters = new ArrayList<>();
		List<PayRegister> payeeList = payRegisterRepository.findAllByIsCurrent(true);
		List<Payroll> payrollList = payrollRepository.findAllByMonthAndYearAndIsDeletedFalse(month, year);
		usersData.forEach(user -> {
			Map<String, Object> userData = new HashMap<>();
			Map<String, Object> data = (Map<String, Object>) user;
			Long userId=new Long((Integer) data.get("id"));
			List<PayRegister> filteredPayee = payeeList.stream().filter(payRegister -> Long.toString(payRegister.getUserId()).equals(userId.toString())).collect(Collectors.toList());
			PayRegister payee = null;
			if(!filteredPayee.isEmpty())
				payee = filteredPayee.get(filteredPayee.size()-1);
			Payroll payroll = null;
			if (payee != null) {
				userData = (Map<String, Object>) getExistingCandidates(payee, accessToken);
				List<Payroll> filteredPayroll = payrollList.stream().filter(payRoll -> Long.toString(payRoll.getUserId()).equals(userId.toString())).collect(Collectors.toList());
				if(!filteredPayroll.isEmpty())
					payroll = filteredPayroll.get(filteredPayroll.size()-1);
				if (payroll != null) {
					userData.put(ConstantUtility.IS_PAYROLL_GENERATED, true);
					userData.put("payrollStatus", payroll.getPayRollStatus());
				}
				else {
					userData.put(ConstantUtility.IS_PAYROLL_GENERATED, false);
					userData.put("payrollStatus", "NA");
				}
			} 
			else 
				userData = (Map<String, Object>) getNonExistingCandidates(userId, accessToken);
			userData=(Map<String, Object>) getUserDetailsFromList(userData,userId,accessToken,data);
			
			if(payrollStatus.equals(""))
				payRegisters.add(userData);
			if(payrollStatus.equals("Generated")) {
				if(userData.get(ConstantUtility.IS_PAYROLL_GENERATED).toString().equals("true"))
					payRegisters.add(userData);
			}
			if(payrollStatus.equals("Pending")) {
				if(userData.get(ConstantUtility.IS_PAYROLL_GENERATED).toString().equals("false") || payroll==null)
					payRegisters.add(userData);
			}
		});
		return payRegisters;
	}

	@Override
	public List<Bank> getBanks() {
		return bankRepository.findAllByIsArchived(false);
	}

	@Override
	public List<PayRevisions> getPayRevisions(long userId) {
		List<PayRevisions> payrevisions=payrevisionRepository.findAllByUserIdAndIsDeleted(userId,false);
		return payrevisions;
	}

	private Map<String, Object> getExistingCandidates(PayRegister payee, String accessToken) {
		HashMap<String, Object> userData = new HashMap<>();
		userData.put(ConstantUtility.PAN_NUMBER, payee.getPanNumber());
		userData.put("uanNumber", payee.getUan());
		userData.put("bank", payee.getBank());
		Bank bank = payee.getBank();
		userData.put("bankId", bank.getId());
		userData.put(ConstantUtility.BANK_NAME, bank.getName());
		userData.put("accountNo", payee.getAccountNo());
		userData.put("comment", payee.getComment());
		userData.put("ifsc", payee.getIfsc());
		userData.put("pfSubscription", payee.getPfSubscription());
		userData.put("basic", payee.getBasicPay());
		userData.put("hra", payee.getHra());
		userData.put("conveyance", payee.getConveyance());
		userData.put("id", payee.getId());
		userData.put("medical", payee.getMedicalAllowance());
		userData.put("employerPfContribution", payee.getEmployerPfContribution());
		userData.put(ConstantUtility.TOTAL_MONTHLY_PAY, payee.getTotalMonthlyPay());
		userData.put("effectiveDate", Date.from((payee.getEffectiveDate()).atZone(ZoneId.systemDefault()).toInstant()));
		userData.put("status", payee.getStatus());
		userData.put("projectAllowance", payee.getProjectAllowance());
		userData.put("laptopAllowance", payee.getLaptopAllowance());
		userData.put("annualCtc", payee.getAnnualCTC());
		double annualCtcExcLA=(payee.getTotalMonthlyPay()-payee.getLaptopAllowance())*12;
		userData.put("annualCtcLaptopAllowance", annualCtcExcLA);
		userData.put("laptopAllowanceAmount", payee.getLaptopAllowance()*12);
		List<LeaveCostPercentage> prevCosts = leaveCostPercentageRepository.findAllByIsDeleted(false);
		LeaveCostPercentage leaveCostPerc=prevCosts.get(0);
		userData.put("paidLeavesPercentangeAmount",leaveCostPerc.getLeaveCostPercentage());
		userData.put("totalAnnualCtc", payee.getTotalAnnualCtc());
		userData.put("paidLeavesAmount", payee.getPaidLeavesAmount());
		if (payee.getEffectiveTo() != null)
			userData.put(ConstantUtility.EFFECTIVE_TO, Date.from((payee.getEffectiveTo()).atZone(ZoneId.systemDefault()).toInstant()));
		else
			userData.put(ConstantUtility.EFFECTIVE_TO, null);
		userData.put("statutoryMaternityPay", payee.getStatutoryMaternityPay());
		return userData;
	}

	private Map<String, Object> getNonExistingCandidates(Long userId, String accessToken) {
		HashMap<String, Object> userData = new HashMap<>();
//		userData=(HashMap<String, Object>) getUserDetails(userData,userId,accessToken);
		userData.put(ConstantUtility.PAN_NUMBER, null);
		userData.put("uanNumber", null);
		userData.put("bank", null);
		userData.put("bankId", null);
		userData.put(ConstantUtility.BANK_NAME, null);
		userData.put("comment", "");
		userData.put("accountNo", null);
		userData.put("ifsc", null);
		userData.put("pfSubscription", null);
		userData.put("basic", 0);
		userData.put("hra", 0);
		userData.put("conveyance", 0);
		userData.put("medical", 0);
		userData.put("employerPfContribution", 0);
		userData.put(ConstantUtility.TOTAL_MONTHLY_PAY, 0);
		userData.put("effictiveDate", null);
		userData.put("status", "INCOMPLETE");
		userData.put("projectAllowance", 0);
		userData.put(ConstantUtility.EFFECTIVE_TO, null);
		userData.put("statutoryMaternityPay", 0);
		userData.put(ConstantUtility.IS_PAYROLL_GENERATED, false);
		userData.put("laptopAllowance", 0);
		userData.put("annualCtc", 0);
		userData.put("totalAnnualCtc", 0);
		userData.put("paidLeavesAmount", 0);
		userData.put("annualCtcLaptopAllowance", 0);
		userData.put("laptopAllowanceAmount", 0);
		List<LeaveCostPercentage> prevCosts = leaveCostPercentageRepository.findAllByIsDeleted(false);
		LeaveCostPercentage leaveCostPerc=prevCosts.get(0);
		userData.put("paidLeavesPercentangeAmount",leaveCostPerc.getLeaveCostPercentage());
		return userData;
	}
	
	private Map<String, Object> getUserDetails(Map<String, Object> userData,Long userId,String accessToken){
		userData.put(ConstantUtility.USER_ID, userId);
		Map<String, Object> userInformation = (Map<String, Object>) feignLegacyInterface.getUserDetails(accessToken, userId).get("data");
		userData.put(ConstantUtility.EMPLOYEE_ID, (String) userInformation.get(ConstantUtility.EMPLOYEE_ID));
		userData.put(ConstantUtility.EMPLOYEE_NAME, userInformation.get("name"));
		userData.put(ConstantUtility.DESIGNATION, userInformation.get(ConstantUtility.DESIGNATION));
		String employeeStatus=(String) (userInformation.get(ConstantUtility.EMPLOYEE_STATUS));
		String billableStatus = (String) userInformation.get(ConstantUtility.BILLABLE_STATUS);
		String remarks = (String) userInformation.get(ConstantUtility.REMARKS);
		userData.put(ConstantUtility.EMPLOYEE_STATUS, employeeStatus);
		userData.put(ConstantUtility.BILLABLE_STATUS, billableStatus);
		userData.put(ConstantUtility.REMARKS, remarks);
		userData.put(ConstantUtility.GRADE, userInformation.get(ConstantUtility.GRADE));
		userData.put(ConstantUtility.DATE_OF_JOINING, userInformation.get(ConstantUtility.DATE_OF_JOINING));
		userData.put(ConstantUtility.BUSINESS_VERTICAL, userInformation.get(ConstantUtility.BUSINESS_VERTICAL));
		return userData;
	}
	
	private Map<String, Object> getUserDetailsFromList(Map<String, Object> userData, Long userId, String accessToken,
			Map<String, Object> user) {
		userData.put(ConstantUtility.USER_ID, userId);
		userData.put(ConstantUtility.EMPLOYEE_ID, (String) user.get(ConstantUtility.EMPLOYEE_ID));
		userData.put(ConstantUtility.EMPLOYEE_NAME, user.get("name"));
		userData.put(ConstantUtility.DESIGNATION, user.get(ConstantUtility.DESIGNATION));
		String employeeStatus = (String) (user.get(ConstantUtility.EMPLOYEE_STATUS));
		String billableStatus = (String) (user.get(ConstantUtility.BILLABLE_STATUS));
		String remarks =user.get("remarks ")!=null?user.get("remarks ").toString():"NA";
		userData.put(ConstantUtility.EMPLOYEE_STATUS, employeeStatus);
		userData.put(ConstantUtility.BILLABLE_STATUS, billableStatus);
		userData.put(ConstantUtility.REMARKS, remarks);
		userData.put(ConstantUtility.GRADE, user.get(ConstantUtility.GRADE));
		userData.put(ConstantUtility.DATE_OF_JOINING, user.get(ConstantUtility.DATE_OF_JOINING));
		userData.put(ConstantUtility.BUSINESS_VERTICAL, user.get(ConstantUtility.BUSINESS_VERTICAL));
		return userData;
	}
	
	private PayRevisions setPayRevisions(PayRegisterDto payRegisterDto,PayRegister payRegister,String accessToken) {
		PayRevisions payrevision=new PayRevisions();
		payrevision.setAccountNo(payRegister.getAccountNo());
		Bank bank=bankRepository.findAllById(payRegister.getBank().getId());
		payrevision.setBank(bank);
		payrevision.setComment(payRegister.getComment());
		payrevision.setPanNumber(payRegister.getPanNumber());
		payrevision.setUan(payRegister.getUan());
		payrevision.setIfsc(payRegister.getIfsc());
		payrevision.setPfSubscription(payRegister.getPfSubscription());
		payrevision.setBasicPay(payRegister.getBasicPay());
		payrevision.setHra(payRegister.getHra());
		payrevision.setConveyance(payRegister.getConveyance());
		payrevision.setMedicalAllowance(payRegister.getMedicalAllowance());
		payrevision.setProjectAllowance(payRegister.getProjectAllowance());
		payrevision.setEmployerPfContribution(payRegister.getEmployerPfContribution());
		payrevision.setTotalMonthlyPay(payRegister.getTotalMonthlyPay());
		payrevision.setLaptopAllowance(payRegister.getLaptopAllowance());
		payrevision.setStatutoryMaternityPay(payRegister.getStatutoryMaternityPay());
		double annualCtc=(payRegister.getTotalMonthlyPay()*12);
		double salaryExcludedLA=(payRegister.getTotalMonthlyPay()-payRegister.getLaptopAllowance())*12;
		payrevision.setAnnualCTC(annualCtc);
		List<LeaveCostPercentage> prevCosts = leaveCostPercentageRepository.findAllByIsDeleted(false);
		LeaveCostPercentage leaveCostPerc=prevCosts.get(0);
		double paidLeavesAmount=(leaveCostPerc.getLeaveCostPercentage()*(salaryExcludedLA))/100;
		payrevision.setTotalAnnualCtc(annualCtc+paidLeavesAmount);
		payrevision.setUserId(payRegister.getUserId());
		payrevision.setPayRegister(payRegister);
		payrevision.setEffectiveFrom(payRegister.getEffectiveDate());
		payrevision.setEffectiveTo(new Timestamp(payRegisterDto.getEffectiveDate()).toLocalDateTime().minusDays(1));
		payrevision.setUpdatedOn(LocalDateTime.now());
		UserModel currentUser=validator.tokenbValidate(accessToken);
		payrevision.setUpdatedBy(currentUser.getUserId());
		payrevision.setUpdatedByUserName(currentUser.getEmpName());
		payrevision.setDeleted(false);
		payrevision=setChangedAttributes(payRegister,payrevision);
		payrevision=payrevisionRepository.saveAndFlush(payrevision);
		return payrevision;
	}
	
	private PayRevisions setChangedAttributes(PayRegister payRegister,PayRevisions payRevision) {
		List<PayRevisions> payRevisions = payrevisionRepository.findAllByUserIdAndIsDeleted(payRegister.getUserId(),false);
		if (!payRevisions.isEmpty()) {
			PayRevisions previousPayRevision=payRevisions.get(payRevisions.size() - 1);
			payRevision=setChangedBankDetails(payRevision, previousPayRevision);
			if (payRevision.getBasicPay() != previousPayRevision.getBasicPay())
				payRevision.setBasicPayChanged(true);
			if (payRevision.getConveyance() != previousPayRevision.getConveyance())
				payRevision.setConveyanceChanged(true);
			if (payRevision.getPfSubscription() != previousPayRevision.getPfSubscription())
				payRevision.setPFSubscriptionChanged(true);
			if (payRevision.getHra() != previousPayRevision.getHra())
				payRevision.setHraChanged(true);
			if (payRevision.getMedicalAllowance() != previousPayRevision.getMedicalAllowance())
				payRevision.setMedicalChanged(true);
			if (payRevision.getProjectAllowance() != previousPayRevision.getProjectAllowance())
				payRevision.setProjectAllowanceChanged(true);
			if (payRevision.getEmployerPfContribution() != previousPayRevision.getEmployerPfContribution())
				payRevision.setEmployerPfContributionChanged(true);
			if (payRevision.getTotalMonthlyPay() != previousPayRevision.getTotalMonthlyPay())
				payRevision.setTotalMonthlyPayChanged(true);
			if(payRevision.getStatutoryMaternityPay()!=previousPayRevision.getStatutoryMaternityPay())
				payRevision.setSmpChanged(true);
			if(payRevision.getLaptopAllowance()!=previousPayRevision.getLaptopAllowance())
				payRevision.setLaptopAllowanceChanged(true);
			if(payRevision.getAnnualCTC()!=previousPayRevision.getAnnualCTC())
				payRevision.setAnnualCtcChanged(true);
			if(payRevision.getTotalAnnualCtc()!=previousPayRevision.getTotalAnnualCtc())
				payRevision.setTotalAnnualCtcChanged(true);
			if(!payRevision.getEffectiveFrom().isEqual(previousPayRevision.getEffectiveFrom()))
				payRevision.setEffectiveDateChanged(true);
			payRevision = payrevisionRepository.saveAndFlush(payRevision);
		}
		return payRevision;
	}
	
	private PayRevisions setChangedBankDetails(PayRevisions payRevision, PayRevisions previousPayRevision) {
		if (!payRevision.getPanNumber().equals(previousPayRevision.getPanNumber()))
			payRevision.setPanChanged(true);
		if (!payRevision.getAccountNo().equals(previousPayRevision.getAccountNo()))
			payRevision.setAccountNumberChanged(true);
		Bank changedBank = bankRepository.findAllById(previousPayRevision.getBank().getId());
		if (payRevision.getBank() != changedBank)
			payRevision.setBankChanged(true);
		if (!payRevision.getUan().contentEquals(previousPayRevision.getUan()))
			payRevision.setUanChanged(true);
		if (!payRevision.getIfsc().contentEquals(previousPayRevision.getIfsc()))
			payRevision.setIFSCChanged(true);
		return payRevision;
	}

	@Override
	public Map<String, Object> getUserAccountDetails(long userId,String year,String accessToken) {
		PayRegister payregister=payRegisterRepository.findAllByUserIdAndIsCurrent(userId, true);
		Map<String, Object> userInformation = (Map<String, Object>) feignLegacyInterface.getUserDetails(accessToken, userId).get("data");
		Map<String, Object> accountData=new HashMap<>();
		if (userInformation != null) {
			accountData.put(ConstantUtility.EMPLOYEE_NAME, userInformation.get("name"));
			accountData.put(ConstantUtility.DESIGNATION, userInformation.get(ConstantUtility.DESIGNATION));
		}
		if(payregister!=null) {
			accountData.put(ConstantUtility.BANK_NAME, payregister.getBank().getName());
			accountData.put("accountNumber", payregister.getAccountNo());
			accountData.put("ifsc", payregister.getIfsc());
			accountData.put(ConstantUtility.PAN_NUMBER, payregister.getPanNumber());
			accountData.put("uan", payregister.getUan());
			List<Payslip> payslips=payslipRepository.findAllByUserIdAndPayslipYearAndPayslipStatusNotAndIsArchived(userId,Integer.parseInt(year), PayslipStatus.SAVED, false);
			List<Object> payslipData=new ArrayList<>();
			payslips.forEach(payslip->{
				HashMap<String, Object> payslipDetails=new HashMap<>();
				payslipDetails.put("Month", Month.of(payslip.getPayslipMonth()));
				payslipDetails.put(ConstantUtility.USER_ID,payregister.getUserId());
				payslipDetails.put("payrollId", payslip.getPayrollId());
				payslipDetails=(HashMap<String, Object>) payslipService.getPayslip(payslip.getPayrollId(), payslip.getPayslipMonth(), Integer.parseInt(year));
				payslipData.add(payslipDetails);
			});
			
			accountData.put("paySlips", payslipData);
		}
		return accountData;
	}
	
	public PayRegisterDto setNullableString(PayRegisterDto payRegisterDto) {
		if(payRegisterDto.getAccountNo()==null)
			payRegisterDto.setAccountNo("");
		if(payRegisterDto.getIfsc()==null)
			payRegisterDto.setIfsc("");
		if(payRegisterDto.getUan()==null)
			payRegisterDto.setUan("");
		if(payRegisterDto.getPanNumber()==null)
			payRegisterDto.setPanNumber("");
		return payRegisterDto;
	}

	@Override
	public boolean checkOverlappingExistingPayregister(String accessToken, PayRegisterDto payRegister, int month,int year) {
		List<PayRevisions> revisions=payrevisionRepository.findAllByUserIdAndIsDeleted(payRegister.getUserId(),false);
		if(!revisions.isEmpty()) {
			for(PayRevisions revision:revisions) {
				LocalDateTime effectiveDate=new Timestamp(payRegister.getEffectiveDate()).toLocalDateTime();
				if((effectiveDate.isAfter(revision.getEffectiveFrom()) && effectiveDate.isBefore(revision.getEffectiveTo())) || effectiveDate.isEqual(revision.getEffectiveFrom()) || effectiveDate.isEqual(revision.getEffectiveTo())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Edits the PayRevision Effective From And Effective To 
	 */
	@Override
	public PayRevisions editPayRevision(String accessToken, long payRevisionId, Long effectiveFrom, Long effectiveTo) {
		Optional<PayRevisions> payRevision=payrevisionRepository.findById(payRevisionId);
		if(!payRevision.isPresent()) 
			return null;
		else {
			PayRevisions revision=payRevision.get();
			revision.setEffectiveFrom(new Timestamp(effectiveFrom).toLocalDateTime());
			revision.setEffectiveTo(new Timestamp(effectiveTo).toLocalDateTime());
			revision=payrevisionRepository.saveAndFlush(revision);
		}
		return payRevision.get();
	}
	
	@Override
	public boolean checkOverlappingExistingPayRevision(String accessToken, long payRevisionId, Long effectiveFrom, Long effectiveTo) {
		Optional<PayRevisions> payRevision=payrevisionRepository.findById(payRevisionId);
		if(payRevision.isPresent()) {
			List<PayRevisions> revisions=payrevisionRepository.findAllByUserIdAndIsDeleted(payRevision.get().getUserId(),false);
			if(!revisions.isEmpty()) {
				for(PayRevisions revision:revisions) {
					LocalDateTime effectiveFromDate=new Timestamp(effectiveFrom).toLocalDateTime();
					LocalDateTime effectiveToDate=new Timestamp(effectiveTo).toLocalDateTime();
					if(revision.getId()!=payRevisionId && ((effectiveFromDate.isAfter(revision.getEffectiveFrom()) && effectiveFromDate.isBefore(revision.getEffectiveTo())) || (effectiveToDate.isAfter(revision.getEffectiveFrom()) && effectiveToDate.isBefore(revision.getEffectiveTo())) || effectiveToDate.isEqual(effectiveToDate) || effectiveToDate.isEqual(effectiveFromDate) || effectiveFromDate.isEqual(effectiveFromDate) || effectiveFromDate.isEqual(effectiveToDate)) ) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public PayRevisions deletePayRevision(String accessToken, long payRevisionId) {
		Optional<PayRevisions> payRevision=payrevisionRepository.findById(payRevisionId);
		UserModel currentUser=validator.tokenbValidate(accessToken);
		if(!payRevision.isPresent()) 
			return null;
		else {
			PayRevisions revision=payRevision.get();
			revision.setDeleted(true);
			revision.setDeletedBy(currentUser.getUserId());
			revision=payrevisionRepository.save(revision);
			return revision;
		}
	}

	@Cacheable("payregisterUsers")
	public List<Object> getUsersForPayregister(String accessToken, String userStatus, int month, int year){
		Map<String, Object> allUsers = (Map<String, Object>) feignLegacyInterface.getAllUsers(accessToken,month,year,userStatus).get("data");
		List<Object> usersData = (ArrayList<Object>) allUsers.get("userList");
		return usersData;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> getSumOfAllPayRegisterMonthlySalary(String accessToken, String userStatus, int month, int year,List<Object> usersData) {
		List<Long> allUsersId = new ArrayList<>();
		for (int i = 0; i < usersData.size(); i++) {
			Map<String, Object> data = (Map<String, Object>) usersData.get(i);
			allUsersId.add(new Long((Integer) data.get("id")));
		}
		Map<String, Object> returnValue=getWidgetData(allUsersId);
		return returnValue;
	}
	
	public Map<String, Object> getWidgetData(List<Long> allUsersId){
		int allUsersCount=allUsersId.size();
		int completePayRegisterCount=0;
		double totalMonthlyPay = 0.0; 
		double completePayregistersPay=0.0;
		double nonCompletePayregistersPay=0.0;
		for(Long userId:allUsersId) {
			PayRegister payee = payRegisterRepository.findAllByUserIdAndIsCurrent(userId, true);
			if(payee != null) { 
				double monthlyPay=payee.getTotalMonthlyPay();
				totalMonthlyPay=totalMonthlyPay+monthlyPay;
				if(payee.getStatus().equals(PayRegisterStatus.COMPLETE)) {
					completePayregistersPay=completePayregistersPay+monthlyPay;
					completePayRegisterCount=completePayRegisterCount+1;
				}
				else
					nonCompletePayregistersPay=nonCompletePayregistersPay+monthlyPay;
			}
		}
		Map<String, Object> returnValue = new HashMap<>();
		returnValue.put(ConstantUtility.TOTAL_MONTHLY_PAY, totalMonthlyPay);
		returnValue.put("completePay", completePayregistersPay);
		returnValue.put("nonCompletePayregistersPay", nonCompletePayregistersPay);
		returnValue.put("payregisterCount",allUsersCount);
		returnValue.put("completePayregisterCount",completePayRegisterCount);
		returnValue.put("noncompletePayregisterCount",allUsersCount-completePayRegisterCount);
		return returnValue;
	}

	@CacheEvict(cacheNames="payregisterUsers", allEntries=true)
	public void flushUsersCache() { }
}