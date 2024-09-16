package com.krishna.Interfaces;

import java.util.List;
import java.util.Map;

import com.krishna.domain.Payroll;
import com.krishna.domain.Payslip;
import com.krishna.dto.PayslipDto;

public interface IPayslipService {

	Map<String, Object> getPayslip(long payrollId, int month, int year);

	Payroll savePayslip(String accessToken, long payrollId, int month, int year, PayslipDto payslipDto);

//	Payslip createPayslip(String accessToken, long payrollId, int month, int year);

	List<Payslip> generatePayslip(String accessToken, int month, int year, List<Integer> payrollIds);

	boolean sendPayslip(int month, int year, long userId, String accessToken);

	Map<String,Object> changePayrollStatusOnExport(String accessToken, List<Integer> payrollIds);


}
