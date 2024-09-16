package com.krishna.service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.krishna.Interfaces.PayrollTrendsService;
import com.krishna.controller.FeignLegacyInterface;
import com.krishna.domain.ExpectedBillingRate;
import com.krishna.domain.PayRegister;
import com.krishna.domain.averagebilling.AverageBillingCompliance;
import com.krishna.repository.AverageBillingComplianceRepository;
import com.krishna.repository.ExpectedBillingRepository;
import com.krishna.repository.payroll.PayRegisterRepository;
import com.krishna.service.util.UtilityService;
import com.krishna.util.ConstantUtility;

@Service
public class PayrollTrendsImpl implements PayrollTrendsService {

	private static final Logger log = LoggerFactory.getLogger(PayrollTrendsService.class);

	
	@Autowired
	PayRegisterRepository payregisterRepo;
	@Autowired
	PayRegisterRepository payregister;
	
	@Autowired
	FeignLegacyInterface feignLegacyInterface;

	@Autowired
	ExpectedBillingRepository expectedBillingRepo;

	@Autowired
	UtilityService utilityService;
	
	@Autowired
	AverageBillingComplianceRepository averageBillingComplianceRepo;
	

	@Override
	@SuppressWarnings("unchecked")
	public List<Object> getPayrollTrends(String accessToken, int month, int year, String businessVerticle) {
		List<Object> responseObject = new ArrayList<>();
		List<String> allGrades = getAllGrades(accessToken);

		Map<String, Object> gradeWiseUserData = (Map<String, Object>) feignLegacyInterface.getGradeWiseUserData(accessToken,month, year)
				.get("data");
		allGrades.forEach(grade -> calculateSalaryTrends(responseObject, gradeWiseUserData, grade, businessVerticle,
				month, year));
		return responseObject;
	}

	@SuppressWarnings(value = { "unchecked" })
	private void calculateSalaryTrends(List<Object> responseObject, Map<String, Object> gradeWiseUserData, String grade,
			String businessVerticle, int month, int year) {
		Map<String, Object> salaryTrend = new LinkedHashMap<>();
		Map<String, Object> userListAndCount = (Map<String, Object>) gradeWiseUserData.get(grade);
		List<Map> userList = (List<Map>) userListAndCount.get("userList");
		if (businessVerticle!=null && !businessVerticle.equals("")) {
			userList = userList.stream()
					.filter(currentObject -> currentObject.get("businessVertical").toString().equals(businessVerticle))
					.collect(Collectors.toList());
		}
		List<Object> medianList = new ArrayList<>();
		List<Object> meanList = new ArrayList<>();
		List<Double> salaryData = new ArrayList<>();
		if (!userList.isEmpty()) {
			userList.forEach(userData -> {
				Long userId = Long.parseLong(userData.get("id").toString());
				PayRegister payregister = utilityService.getMonthsalary(userId, month, year);
				if (payregister != null) {
					salaryData.add(payregister.getTotalMonthlyPay());
				} else {
					salaryData.add((double) 0);
				}
			});

			Collections.sort(salaryData);
			Double mean = salaryData.stream().mapToDouble(i -> i).sum() / salaryData.size();
			Double median = calculateMedian(salaryData);
			Double min = salaryData.get(0);
			Double max = salaryData.get(salaryData.size() - 1);
			mean = findMean(salaryData, mean, min, max);
			median = findMedian(salaryData, median, min, max);
			salaryTrend.put("employeeCount", userList.size());
			salaryTrend.put("grade", grade);
			ExpectedBillingRate billingRateObj = expectedBillingRepo.findByMonthAndYearAndGrade(month, year, grade);
			if (billingRateObj != null)
				salaryTrend.put("billingRate", billingRateObj.getBillingRate());
			else
				salaryTrend.put("billingRate", 0);
			salaryTrend.put("min", Math.round(min * 100.00) / 100.00);
			salaryTrend.put("max", Math.round(max * 100.00) / 100.00);
			salaryTrend.put("mean", Math.round(mean * 100.00) / 100.00);
			salaryTrend.put("median", Math.round(median * 100.00) / 100.00);
			salaryTrend.put("medianList", medianList);
			salaryTrend.put("meanList", meanList);
			responseObject.add(salaryTrend);
		}
	}

	public Double calculateMedian(List<Double> salaryData) {

		Double[] set = new Double[salaryData.size()];
		int n = set.length;
		set = salaryData.toArray(set);
		Arrays.sort(set);
		if (n % 2 == 0) {
			double mid = (set[n / 2] + set[(n / 2) - 1]) / 2;
			return mid;
		} else {
			double mid = set[n / 2];
			return mid;
		}
	}

	@SuppressWarnings("unchecked")
	public List<String> getAllGrades(String accessToken) {
		return (List<String>) feignLegacyInterface.getAllBands(accessToken).get("data");
	}

	private double calculateStandardDeviation(List<Double> salaryData) {
		double[] salArray = ArrayUtils.toPrimitive(salaryData.toArray(new Double[salaryData.size()]));
		int length = salArray.length;
		double sum = 0.0, standardDeviation = 0.0;
		for (double num : salArray) {
			sum += num;
		}
		double mean = sum / length;
		for (double num : salArray) {
			standardDeviation += Math.pow(num - mean, 2);
		}
		double root = Math.sqrt(standardDeviation / length);
		return root * 3;
	}

	private double findMean(List<Double> salaryData, double mean, double min, double max) {
		double sigmaValue = calculateStandardDeviation(salaryData);
		Double usl = mean;
		Double lsl = mean;
		for (int j = 0; j < 3; j++) {
			lsl = lsl - sigmaValue;
			usl = usl + sigmaValue;
		}

		if (min >= lsl && max <= usl) {
			return mean;
		} else {
			List<Double> salaries = new ArrayList<>();
			salaries.addAll(salaryData);
			for (Double salary : salaries) {
				if (salary < lsl)
					salaryData.remove(salary);
				if (salary > usl)
					salaryData.remove(salary);
			}
			Collections.sort(salaryData);
			mean = salaryData.stream().mapToDouble(i -> i).sum() / salaryData.size();
			min = salaryData.get(0);
			max = salaryData.get(salaryData.size() - 1);
			findMean(salaryData, mean, min, max);
			return mean;
		}
	}

	public double findMedian(List<Double> salaryData, double median, double min, double max) {
		double sigmaValue = calculateStandardDeviation(salaryData);
		Double usl = median;
		Double lsl = median;
		for (int j = 0; j < 3; j++) {
			lsl = lsl - sigmaValue;
			usl = usl + sigmaValue;
		}
		if (min >= lsl && max <= usl) {
			return median;
		} else {
			List<Double> salaries = new ArrayList<>();
			salaries.addAll(salaryData);
			for (Double salary : salaries) {
				if (salary < lsl)
					salaryData.remove(salary);
				if (salary > usl)
					salaryData.remove(salary);
			}
			Collections.sort(salaryData);
			median = calculateMedian(salaryData);
			min = salaryData.get(0);
			max = salaryData.get(salaryData.size() - 1);
			findMean(salaryData, median, min, max);
			return median;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getMinAndMaxSalaryOfGivenGrade(String grade, String accessToken) {
		Map<String, Object> response = new HashMap<>();
		List<Double> salaryData = new ArrayList<Double>();
		List<Object> userList = (List<Object>) feignLegacyInterface.getUsersByGrade(accessToken, grade);
		
		if (userList != null) {
			for (Object ob : userList) {
				Map<String, Object> userData = (Map<String, Object>) ob;
				long userId = ((Number) userData.get(ConstantUtility.USER_ID)).longValue();
				PayRegister payRegister = payregister.findByUserIdAndIsCurrent(userId, true);
				if (payRegister != null) {
					salaryData.add(payRegister.getAnnualCTC());
				}
			}
			if (salaryData.size() < 1) {
				response.put("minimumPay", 0);
				response.put("maximumPay", 0);
			} else {
				response.put("minimumPay", Collections.min(salaryData));
				response.put("maximumPay", Collections.max(salaryData));
			}
			return response;
		}
		return null;
	}

	@Override
	public ExpectedBillingRate saveExpectedBillingRate(String grade, String token, int month, int year,
			double billingRate) {
		ExpectedBillingRate billingRateObj = expectedBillingRepo.findByMonthAndYearAndGrade(month, year, grade);
		if (billingRateObj == null) {
			billingRateObj = new ExpectedBillingRate();
			billingRateObj.setGrade(grade);
			billingRateObj.setMonth(month);
			billingRateObj.setYear(year);
		}
		billingRateObj.setBillingRate(billingRate);
		expectedBillingRepo.saveAndFlush(billingRateObj);
		return billingRateObj;
	}

	@Override
	public AverageBillingCompliance saveAverageBillingCompliance(int month, int year, String comments, long projectId) {
		Optional<AverageBillingCompliance> commentData = averageBillingComplianceRepo.findByProjectIdAndMonthAndYear(projectId, month, year);
		if(commentData.isPresent()) {
			commentData.get().setComments(comments);
			return averageBillingComplianceRepo.save(commentData.get());
		} else {
			return createNewEntryOfAverageBillingCompliance(month, year, comments, projectId);
		}
	}

	private AverageBillingCompliance createNewEntryOfAverageBillingCompliance(int month, int year, String comments,
			long projectId) {
		AverageBillingCompliance avgBillingRateCompliance = new AverageBillingCompliance(0, month, year, projectId, comments);
		return averageBillingComplianceRepo.save(avgBillingRateCompliance);
	}
}
