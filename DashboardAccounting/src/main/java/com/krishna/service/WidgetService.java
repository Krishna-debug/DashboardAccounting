package com.krishna.service;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.repository.invoice.ProjectInvoiceRepository;

@Service
public class WidgetService {
	@Autowired
	ProjectInvoiceRepository invoiceRepository;
	
	public Map<String,List<?>> getInvoiceWidgetData(){
		Map<String,List<?>> data = new HashMap<>();
		
		List<Double> recoveredAmountList = new ArrayList<>();
		List<Double> totalAmountList = new ArrayList<>();
		List<String> monthsList = new ArrayList<>();
		
		Date currDate = new Date();
		Calendar c = Calendar.getInstance(); 
		 
		
		for(int i=3; i>=1; i--) {
			double recoveredAmount=0;
			double totalAmount =0;
			c.setTime(currDate);
			c.add(Calendar.MONTH, -i);
			
			String monthName = c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH );
			List<ProjectInvoice> invoiceList = invoiceRepository.findByMonthAndYearAndIsDeletedAndIsInternal(monthName ,String.valueOf(c.get(Calendar.YEAR)),false, false);
			for(ProjectInvoice invoice:invoiceList ) {
				if(invoice.getInvoiceStatus() == 2) {
					recoveredAmount+=invoice.getAmountInDollar();
					totalAmount +=invoice.getAmountInDollar();
				}
				else
					totalAmount +=invoice.getAmountInDollar();
			}
			recoveredAmountList.add(recoveredAmount);
		    totalAmountList.add(totalAmount);
		    monthsList.add(monthName+" "+c.get(Calendar.YEAR));
		}
		data.put("Months", monthsList);
		data.put("recoveredAmount", recoveredAmountList);
		data.put("totalAmount", totalAmountList);
		return data;

	}
}
