package com.krishna.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.krishna.Interfaces.IPayslipService;
import com.krishna.controller.FeignLegacyInterface;
import com.krishna.domain.Arrear;
import com.krishna.domain.Payroll;
import com.krishna.domain.Payslip;
import com.krishna.domain.UserModel;
import com.krishna.dto.PayslipDto;
import com.krishna.enums.PayRollStatus;
import com.krishna.enums.PayslipStatus;
import com.krishna.repository.payroll.ArrearRepository;
import com.krishna.repository.payroll.PayrollRepository;
import com.krishna.repository.payroll.PayslipRepository;
import com.krishna.security.JwtValidator;
import com.krishna.util.ConstantUtility;
import com.krishna.util.NumberToWordsConverter;

@Service
public class PayslipService implements IPayslipService{
	
	@Autowired
	PayrollRepository payrollRepository;

	@Autowired
	JwtValidator validator;
	
	@Autowired
	FeignLegacyInterface feignLegacyInterface;
	
	@Autowired
	PayslipRepository payslipRepository;
	
	@Autowired
	private MailService mailService;
	
	@Value("${com.oodles.ticket.serverUrl}")
	private String ticketServerUrl;
	
	@Autowired
	ArrearRepository arrearRepository;
	
	Logger log=LoggerFactory.getLogger(PayslipService.class);

	@Override
	public Map<String, Object> getPayslip(long payrollId, int month, int year) {
		Payroll payroll=payrollRepository.findAllById(payrollId);
		Map<String,Object> payslipData=new HashMap<>();
		payslipData=getUserAndBankDetails(payslipData,payroll);
		payslipData.put("basicPay",payroll.getBasicPay());
		payslipData.put("hra",payroll.getHra());
		payslipData.put("conveyanceAllowance",payroll.getConveyance());
		payslipData.put("medicalAllowance",payroll.getMedicalAllowance());
		payslipData.put("projectAllowance",payroll.getProjectAllowance());
		payslipData.put("tds",payroll.getTds());
		payslipData.put("healthInsurance",payroll.getHealthInsurance());
		payslipData.put("infraDeductions",payroll.getInfraDeductions());
		payslipData.put("incentives", payroll.getIncentives());
		payslipData.put("arrear", payroll.getTotalReimbursement());
		payslipData.put("previousArrears", payroll.getTotalArrear());
		payslipData.put("employeePfContribution", payroll.getEmployeePfContribution());
		payslipData.put("employerPfContribution", payroll.getEmployerPfContribution());
		payslipData.put("statutoryMaternityPay", payroll.getStatutoryMaternityPay());
		payslipData.put("netPay", payroll.getNetPay());
		payslipData.put("laptopAllowance", payroll.getLaptopAllowance());
		payslipData.put("workFromHomeAllowance", payroll.getWorkFromHomeAllowance());
		payslipData.put("specialAllowance", payroll.getSpecialAllowance());
		payslipData.put("variablePay", payroll.getVariablePayAmount());
		payslipData.put("voluntaryPayAmount", payroll.getVoluntaryPayAmount());
		payslipData.put("leaveDeductions", payroll.getLeaveDeductions());
		payslipData.put("paidLeave", payroll.getPaidLeaveAdditions());
		double totalEarnings=payroll.getBasicPay() + payroll.getHra() + payroll.getConveyance()
		+ payroll.getMedicalAllowance() + payroll.getProjectAllowance()+payroll.getLaptopAllowance()
		+payroll.getEmployerPfContribution()+payroll.getTotalArrear()+payroll.getIncentives()
		+payroll.getStatutoryMaternityPay()+payroll.getPaidLeaveAdditions()+payroll.getTotalReimbursement()
		+payroll.getSpecialAllowance()+payroll.getWorkFromHomeAllowance()+payroll.getVariablePayAmount()+payroll.getVoluntaryPayAmount();
		payslipData.put("totalEarnings",totalEarnings);
		double totalDeduction=payroll.getTds() + payroll.getHealthInsurance() + payroll.getInfraDeductions()+payroll.getEmployeePfContribution()+payroll.getLeaveDeductions();
		payslipData.put("totalDeduction",totalDeduction);
		payslipData.put("totalSalary", totalEarnings-totalDeduction);
		new NumberToWordsConverter();
		payslipData.put("amountInWords", NumberToWordsConverter.convert((int) (totalEarnings-totalDeduction)));
		payslipData.put("earningColumns", payroll.getColumnEarnings());
		payslipData.put("deductionColumns", payroll.getColumnDeductions());
		payslipData=getarrearAndpayslipData(payslipData, payroll.getUserId(), month, year, payrollId);
		return payslipData;
	}
	
	private Map<String,Object> getarrearAndpayslipData(Map<String,Object> payslipData,long userId,int month,int year,long payrollId){
		Payslip existingPayslip = payslipRepository.findAllByUserIdAndPayslipMonthAndPayslipYearAndPayrollId(userId,month, year, payrollId);
		if(existingPayslip!=null) {
			payslipData.put("payslipId",existingPayslip.getId());
			payslipData.put("payslipStatus",existingPayslip.getPayslipStatus());
			payslipData.put("isUpdated", existingPayslip.isUpdated());
			if(existingPayslip.getModificationDate()!=null)
				payslipData.put(ConstantUtility.LAST_UPDATED_ON, existingPayslip.getModificationDate().getDayOfMonth()+"/"+existingPayslip.getModificationDate().getMonthValue()+"/"+existingPayslip.getModificationDate().getYear());
			else
				payslipData.put(ConstantUtility.LAST_UPDATED_ON, null);
		}
		else {
			payslipData.put("isUpdated", false);
			payslipData.put("payslipId",null);
			payslipData.put("payslipStatus",null);
			payslipData.put(ConstantUtility.LAST_UPDATED_ON, null);
		}
		List<Arrear> arrears=arrearRepository.findByPayrollIdAndCreationMonthAndCreationYearAndIsArrearIncludedAndIsDeleted(payrollId, month, year,true,false);
		if(!arrears.isEmpty()) 
			payslipData.put("isArrearIncluded", true);
		else
			payslipData.put("isArrearIncluded", false);
		return payslipData;
	}
	
	private Map<String,Object> getUserAndBankDetails(Map<String,Object> payslipData,Payroll payroll){
		payslipData.put("presentDays",payroll.getPayDays());
		payslipData.put("absentDays",payroll.getUnpaidDays());
		payslipData.put("designation", payroll.getDesignation());
		payslipData.put("department", payroll.getDepartment());
		payslipData.put("pan", payroll.getPanNumber());
		payslipData.put("uan", payroll.getUan());
		payslipData.put("EmployeeId",payroll.getEmployeeId());
		payslipData.put("bank", payroll.getBank().getName());
		String monthAndYear=Month.of(payroll.getMonth()).name()+" "+payroll.getYear();
		payslipData.put("monthAndYear",monthAndYear);
		return payslipData;
	}
	
	private Payslip createPayslip(long payrollId,long userId,int month,int year) {
		Payslip payslip=new Payslip();
		payslip.setUserId(userId);
		payslip.setPayrollId(payrollId);
		payslip.setGenerationDate(LocalDateTime.now());
		payslip.setPayslipMonth(month);
		payslip.setPayslipYear(year);
		payslip.setPayslipStatus(PayslipStatus.GENERATED);
		payslip=payslipRepository.save(payslip);
		return payslip;
	}

	@Override
	public Payroll savePayslip(String accessToken, long payrollId, int month, int year, PayslipDto payslipDto) {
		Payroll payroll = payrollRepository.findAllById(payrollId);
		Payslip existingPayslip = payslipRepository.findAllByUserIdAndPayslipMonthAndPayslipYearAndPayrollId(payroll.getUserId(),
				month, year, payrollId);
		if (existingPayslip != null) {
			existingPayslip.setModificationDate(LocalDateTime.now());
			existingPayslip.setPayslipStatus(PayslipStatus.SAVED);
			UserModel currentUser = validator.tokenbValidate(accessToken);
			existingPayslip.setModifiedBy(currentUser.getUserId());
			existingPayslip = payslipRepository.saveAndFlush(existingPayslip);
			payroll.setModifiedOn(LocalDateTime.now());
			payroll.setBasicPay(payslipDto.getBasicPay());
			payroll.setHra(payslipDto.getHra());
			payroll.setConveyance(payslipDto.getConveyance());
			payroll.setMedicalAllowance(payslipDto.getMedicalAllowance());
			payroll.setWorkFromHomeAllowance(payslipDto.getWorkFromHomeAllowance());
			payroll.setSpecialAllowance(payslipDto.getSpecialAllowance());
			payroll.setProjectAllowance(payslipDto.getProjectAllowance());
			payroll.setPayDays(payslipDto.getPayDays());
			payroll.setUnpaidDays(payslipDto.getUnpaidDays());
			payroll.setIncentives(payslipDto.getIncentives());
			payroll.setTds(payslipDto.getTds());
			payroll.setHealthInsurance(payslipDto.getHealthInsurance());
			payroll.setInfraDeductions(payslipDto.getInfraDeduction());
			payroll = payrollRepository.saveAndFlush(payroll);
		}
		return payroll;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Payslip> generatePayslip(String accessToken, int month, int year, List<Integer> payrollIds) {
		List<Payslip> payslips = new ArrayList<>();
		payrollIds.forEach(id -> {
			Payroll payroll = payrollRepository.findAllById(id);
			if (payroll != null && payroll.getPayRollStatus().equals(PayRollStatus.FILEPROCESSED)) {
				Map<String, Object> timesheetData = (Map<String, Object>) feignLegacyInterface.getTimeSheetHours(accessToken, month, year,payroll.getUserId()).get("data");
				List<Object> timesheetHoursData = (List<Object>) timesheetData.get("attendanceData");
				Map<String, Object> data = (Map<String, Object>) timesheetHoursData.get(0);
				double actualHours = Double.parseDouble((String) data.get("actualHours"));
				double expectedHours = new Double(data.get("expectedHours").toString());
				boolean buApproval = payroll.getBuPayrollApproval()!=null?payroll.getBuPayrollApproval():false;
					if (actualHours >= expectedHours||(expectedHours >= actualHours && buApproval)) {
					payroll.setProcessed(true);
					payroll.setPayRollStatus(PayRollStatus.PROCESSED);
					payroll = payrollRepository.save(payroll);
					Payslip payslip = payslipRepository.findAllByUserIdAndPayslipMonthAndPayslipYearAndPayrollId(
							payroll.getUserId(), month, year, payroll.getId());
					UserModel currentUser = validator.tokenbValidate(accessToken);
					log.info("payslip of user -----------------"+payslip);
					if (payslip == null) {
						log.info("If payslip not found");
						payslip = createPayslip(payroll.getId(), payroll.getUserId(), month, year);
						payslip.setGeneratedBy(currentUser.getUserId());
						payslips.add(payslip);
					}
					else {
						log.info("If payslip is found");
						if(payslip.getPayslipStatus().equals(PayslipStatus.SAVED)) {
							payslip.setPayslipStatus(PayslipStatus.GENERATED);
							payslip.setUpdated(true);
							payslip.setModificationDate(LocalDateTime.now());
							payslip.setModifiedBy(currentUser.getUserId());
							payslips.add(payslip);
						}
					}
					payslipRepository.save(payslip);
					sendPayslip(month, year, payroll.getUserId(), accessToken);
				}
			}
		});
		log.info("Payslips -----------------"+payslips);
		return payslips;
	}
	
	@SuppressWarnings("unchecked")
	public boolean sendPayslip(int month,int year,long userId,String accessToken) {
		Map<String,Object> userInfo=(Map<String, Object>) feignLegacyInterface.getUserBasicInfo(accessToken,userId).get("data");
		Map<String,Object> valueMap=new HashMap<>();
		valueMap.put("userName", userInfo.get("fullName"));
		valueMap.put("uImage", userInfo.get("image"));
		String monthAndYear=Month.of(month).name()+" "+year;
		valueMap.put("monthAndYear",monthAndYear);
		valueMap.put("userId",userId);
		valueMap.put("currentYear", LocalDate.now().getYear());
		String subject="Payslip - "+monthAndYear;
		Payroll payroll=payrollRepository.findAllByMonthAndUserIdAndYear(month, userId, year);
		Payslip payslip=payslipRepository.findAllByUserIdAndPayslipMonthAndPayslipYearAndPayrollId(userId, month, year, payroll.getId());
		mailService.sendScheduleHtmlMail(userInfo.get("email").toString(), subject, valueMap,"monthly-payslip-template");
		payslip.setPayslipStatus(PayslipStatus.SENT);
		UserModel currentUser=validator.tokenbValidate(accessToken);
		payslip.setModifiedBy(currentUser.getUserId());
		payslip=payslipRepository.save(payslip);
		if(payslip!=null)
			return true;
		else 
			return false;
	}

	@Override
	public Map<String,Object> changePayrollStatusOnExport(String accessToken, List<Integer> payrollIds) {
		Map<String,Object> exportData=new HashMap<>();
		List<Payroll> verifiedPayrolls = new ArrayList<>();
		List<Payroll> exportedPayrolls = new ArrayList<>();
		payrollIds.forEach(payrollId -> {
			Payroll payroll = payrollRepository.findAllById(payrollId);
			if (payroll != null && payroll.getPayRollStatus().equals(PayRollStatus.VERIFIED)) {
				payroll.setPayRollStatus(PayRollStatus.FILEPROCESSED);
				payroll = payrollRepository.save(payroll);
				verifiedPayrolls.add(payroll);
			}
			else if(payroll != null && payroll.getPayRollStatus().equals(PayRollStatus.FILEPROCESSED)) {
				exportedPayrolls.add(payroll);
			}
		});
		exportData.put("alreadyExported", exportedPayrolls);
		exportData.put("newlyExported", exportedPayrolls);
		return exportData;
	}
	
}
