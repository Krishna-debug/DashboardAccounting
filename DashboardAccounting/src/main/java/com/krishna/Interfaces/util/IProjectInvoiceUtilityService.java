package com.krishna.Interfaces.util;

import java.util.List;

public interface IProjectInvoiceUtilityService{

	List<Object> getActualInvoice(String accessToken, int year, int i, String businessVertical);

}
