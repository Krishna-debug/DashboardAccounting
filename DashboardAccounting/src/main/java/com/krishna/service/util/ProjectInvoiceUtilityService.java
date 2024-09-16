
package com.krishna.service.util;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.krishna.Interfaces.util.IProjectInvoiceUtilityService;
import com.krishna.controller.FeignLegacyInterface;
import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.repository.invoice.InvoiceCycleRepository;
import com.krishna.repository.invoice.ProjectInvoiceRepository;
import com.krishna.service.LoginUtiltiyService;
import com.krishna.util.ConstantUtility;

@Service
public class ProjectInvoiceUtilityService implements IProjectInvoiceUtilityService {

	
	@Autowired
	LoginUtiltiyService loginUtiltiyService;

	@Autowired
	ProjectInvoiceRepository projectInvoiceRepository;
	
	@Autowired
	InvoiceCycleRepository invoiceCycleRepository;
	
	@Autowired
	FeignLegacyInterface legacyInterface;

	@Override
	public List<Object> getActualInvoice(String accessToken, int year, int month, String businessVertical) {
		
		List<Object> leads = legacyInterface.getMonthlyStartedProjects(accessToken,month, year, businessVertical);
		List<Object> dataList = new ArrayList<Object>();
		String nextMonthName = new DateFormatSymbols().getMonths()[month].toString();
		if (!leads.isEmpty()) {
			int listSize = leads.size();
			for (int i = 0; i < listSize; i++) {
				Map<String, Object> map = loginUtiltiyService.objectToMapConverter(leads.get(i));
				Long projectId = Long.parseLong(map.get("projectId").toString());
				String invoiceAmount = "NA";
				double invoiceValue = 0;
				double proratedInvoice = 0;
				Double totalInvoiceAmt = 0.0D;
				String billingType="NA";
				if (map.get("projectStartDate") != null) {
					String string = (String) map.get("projectStartDate");
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss z yyyy",Locale.ENGLISH);
					LocalDate date = LocalDate.parse(string, formatter);
					String monthName = new DateFormatSymbols().getMonths()[(date.getMonthValue() - 1)].toString();
					List<ProjectInvoice> projectInvoice = projectInvoiceRepository.findByProjectIdAndMonthAndYearAndIsDeletedAndIsInternal(projectId, monthName, Integer.toString(year),false, false);
					List<ProjectInvoice> allProjectInvoice = projectInvoiceRepository.findAllByProjectIdAndIsDeletedAndIsInternal(projectId, false, false);
					List<ProjectInvoice> totalInvoices = allProjectInvoice.stream().filter(inv->inv.getInvoiceStatus()!=6L).collect(Collectors.toList());
					totalInvoiceAmt = totalInvoiceAmt + totalInvoices.stream().mapToDouble(invoice -> invoice.getAmountInDollar()).sum();
					int leftDays = (int) map.get("workingDays");
					if (!projectInvoice.isEmpty()) {
						for (ProjectInvoice inv : projectInvoice) {
							invoiceValue = invoiceValue + inv.getAmountInDollar();
						}
						billingType=invoiceCycleRepository.findById(projectInvoice.get(projectInvoice.size()-1).getInvoiceCycleId()).getInvoiceCycleType();
					}
					
					double perDayInvoice = invoiceValue / leftDays;
					int totalworkngDays= (int) map.get("totalWorkingDays");
					proratedInvoice = perDayInvoice * totalworkngDays;
					
				}
				invoiceAmount = new DecimalFormat("0.00").format(invoiceValue);
				map.put("actualBill", invoiceAmount);
				map.put("prorataInvoice", new DecimalFormat("0.00").format(proratedInvoice));
				map.put("billingType", billingType);
				map.put("totalInvoiceAmt", new DecimalFormat("0.00").format(totalInvoiceAmt));
				dataList.add(map);
			}
		}
		return dataList;
	}

}
