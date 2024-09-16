package com.krishna.Interfaces;

import java.util.List;
import java.util.Map;

public interface IInvoicePipelineService {

	List<Object> getInvoicePipeline(int month, int year, String projectType, String businessVerticals);

	List<Object> getActualHoursForInvoicePipeline(int i, int year, String authorization);

    void flushActualHoursForInvoicePipeline();

}
