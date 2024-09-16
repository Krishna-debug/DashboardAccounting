package com.krishna.repository.invoice;
import java.io.Serializable;


import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.krishna.domain.invoice.InvoiceBank;
import com.krishna.domain.invoice.ProjectInvoice;

@Repository
public interface ProjectInvoiceRepository extends JpaRepository <ProjectInvoice,  Serializable>{
	
	@Cacheable("findById")
	ProjectInvoice findById(Long id); 
	
	@Cacheable("findByIsDeletedAndInvoiceCycleIdAndIsInternal")
	List<ProjectInvoice>  findByIsDeletedAndInvoiceCycleIdAndIsInternal(boolean b,Long id,boolean isInternal);
	
	@Cacheable("findByIsDeletedAndPaymentTermsIdAndIsInternal")
	List<ProjectInvoice> findByIsDeletedAndPaymentTermsIdAndIsInternal(boolean b,Long id,boolean isInternal);
	
	@Cacheable("findByIsDeletedAndModeOfPaymentIdAndIsInternal")
	List<ProjectInvoice>  findByIsDeletedAndModeOfPaymentIdAndIsInternal(boolean b,Long id,boolean isInternal);

	
	@Cacheable("findByProjectAndMonthAndYearAndIsDeletedAndIsInternal")
	List<ProjectInvoice> findByProjectAndMonthAndYearAndIsDeletedAndIsInternal(String name,String month,String year,boolean b,boolean isInternal);
	
	@Cacheable("findAllByMonthAndYearAndIsDeletedAndInvoiceStatusAndIsInternal")
	List<ProjectInvoice> findAllByMonthAndYearAndIsDeletedAndInvoiceStatusAndIsInternal(String month,String year,boolean b,Long id,boolean isInternal);
	@Cacheable("findByProjectAndYearAndIsDeletedAndIsInternal")
	List<ProjectInvoice> findByProjectAndYearAndIsDeletedAndIsInternal(String name,String year,boolean b,boolean isInternal);
	
	@Cacheable("findByMonthAndInvoiceStatusAndYearAndIsDeletedAndIsInternal")
	List<ProjectInvoice>  findByMonthAndInvoiceStatusAndYearAndIsDeletedAndIsInternal(String month,Long id,String year,boolean b,boolean isInternal);
	@Cacheable("findByMonthAndYearAndIsDeletedAndIsInternal")
	List<ProjectInvoice> findByMonthAndYearAndIsDeletedAndIsInternal(String month,String year,boolean b,boolean isInternal);
	@Cacheable("findByBillingDateBetween")
	List<ProjectInvoice> findByBillingDateBetween(Date d1,Date d2);
	@Cacheable("findByDueDateBetween")
	List<ProjectInvoice> findByDueDateBetween(Date d1,Date d2);
	@Cacheable("findByReceivedOnBetween")
	List<ProjectInvoice> findByReceivedOnBetween(Date d1,Date d2);
	
	@Cacheable("findByProjectIdAndMonthAndYearAndIsDeletedAndIsInternal")
	List<ProjectInvoice> findByProjectIdAndMonthAndYearAndIsDeletedAndIsInternal(long id,String month,String year,boolean b,boolean isInternal);
	@Cacheable("findByProjectIdAndIsDeletedAndIsInternal")
	List<ProjectInvoice>  findByProjectIdAndIsDeletedAndIsInternal(long id,boolean b,boolean isInternal);
	@Cacheable("findAllByIsDeletedAndIsInternal")
	List<ProjectInvoice> findAllByIsDeletedAndIsInternal(boolean b,boolean isInternal);

	@Cacheable("findAllByMonthAndYearAndIsDeletedAndProjectIdAndIsInternal")
	List<ProjectInvoice> findAllByMonthAndYearAndIsDeletedAndProjectIdAndIsInternal(String monthName, String year, boolean b,
			long parseLong,boolean isInternal);
	
	@Cacheable("findAllByInvoiceStatusAndMonthAndYearAndIsDeletedAndIsInternal")
	List<ProjectInvoice> findAllByInvoiceStatusAndMonthAndYearAndIsDeletedAndIsInternal(Long status,String month,String year,boolean delete ,boolean isInternal);
	@Cacheable("countByIsDeleted")
	Long countByIsDeleted(boolean b);
	@Cacheable("countByInvoiceStatusAndIsDeletedAndIsInternal")
	Long countByInvoiceStatusAndIsDeletedAndIsInternal(Long status,boolean b,boolean isInternal);
	@Cacheable("findByProjectIdAndIsDeletedAndIsInternalOrderByIdAsc")
	List<ProjectInvoice> findByProjectIdAndIsDeletedAndIsInternalOrderByIdAsc(Long projectId, boolean b,boolean isInternal);
	@Cacheable("findAllByMonthAndYearAndProjectIdAndIsDeletedAndIsInternal")
	List<ProjectInvoice> findAllByMonthAndYearAndProjectIdAndIsDeletedAndIsInternal(String monthName, String year,Long projectId, boolean b,boolean isInternal);
	// @Cacheable("findAllByProjectIdAndIsDeletedAndIsInternal")
	// List<ProjectInvoice> findAllByProjectIdAndIsDeletedAndIsInternal(Long projectId,boolean isDeleted,boolean isInternal);
	@Cacheable("findAllByProjectIdAndMonthAndIsDeletedAndIsInternal")
	List<ProjectInvoice> findAllByProjectIdAndMonthAndIsDeletedAndIsInternal(Long projectId,String month,boolean isDeleted,boolean isInternal);
	@Cacheable("findByProjectIdAndYearAndIsDeletedAndIsInternal")
	List<ProjectInvoice> findByProjectIdAndYearAndIsDeletedAndIsInternal(long projectId, String year, boolean b,boolean isInternal);

	@Cacheable("findAllByProjectIdAndIsDeletedAndIsInternal")
	List<ProjectInvoice> findAllByProjectIdAndIsDeletedAndIsInternal(long projectId, boolean isDeleted,boolean isInternal);
	@Cacheable("findAllByYearAndInvoiceStatusAndIsDeletedAndIsInternal")
	Optional<List<ProjectInvoice>> findAllByYearAndInvoiceStatusAndIsDeletedAndIsInternal(String year, long invoiceStatus , 
			boolean isDeleted,boolean isInternal);
	@Cacheable("findAllByProjectIdAndInvoiceStatusAndIsDeletedAndIsInternal")
	List<ProjectInvoice> findAllByProjectIdAndInvoiceStatusAndIsDeletedAndIsInternal(long projectId, long invoiceStatus,
			boolean isDeleted,boolean isInternal);

	@Cacheable("findAllByProjectIdAndInvoiceStatusAndIsDeletedAndYearAndIsInternal")
	Optional<List<ProjectInvoice>> findAllByProjectIdAndInvoiceStatusAndIsDeletedAndYearAndIsInternal(long projectId, Long i, boolean b, String year,boolean isInternal);
	
	@Cacheable("findAllByProjectIdAndInvoiceStatusAndYearAndIsDeletedAndIsInternal")
	List<ProjectInvoice> findAllByProjectIdAndInvoiceStatusAndYearAndIsDeletedAndIsInternal(long projectId, Long i, String year, boolean b,boolean isInternal);

	@Cacheable("findAllByYearAndIsDeletedAndIsInternal")
	List<ProjectInvoice> findAllByYearAndIsDeletedAndIsInternal(String year, boolean b, boolean isInternal);
	
// 		List<ProjectInvoice> findAllByYearAndIsDeletedAndIsInternalAndInvoiceStatusNot(String year, boolean b, boolean isInternal, long l);

	@Cacheable("findAllByMonthAndYearAndIsDeletedAndIsInternal")
	List<ProjectInvoice> findAllByMonthAndYearAndIsDeletedAndIsInternal(String month, String year, boolean b,
			boolean c);

	@Cacheable("findAllByRaisedToBuAndMonthAndYearAndIsDeletedAndIsInternal")
	List<ProjectInvoice> findAllByRaisedToBuAndMonthAndYearAndIsDeletedAndIsInternal(Long buId, String month, String year,
			boolean b,boolean isInternal);

	@Cacheable("findAllByRaisedFromBuAndMonthAndYearAndIsDeletedAndIsInternal")
	List<ProjectInvoice> findAllByRaisedFromBuAndMonthAndYearAndIsDeletedAndIsInternal(Long buId, String month, String year,
			boolean b,boolean isInternal);

	@Cacheable("findAllByMonthAndYearAndModeOfPaymentIdAndIsDeleted")
	List<ProjectInvoice> findAllByMonthAndYearAndModeOfPaymentIdAndIsDeleted(String month, String year, Long modeOfPaymentId,Boolean deleted);

//	List<ProjectInvoice> findAllByMonthAndYear(String month, String year);
	
	@Cacheable("findAllByInvoiceStatusAndIsDeletedAndIsInternal")
	List<ProjectInvoice> findAllByInvoiceStatusAndIsDeletedAndIsInternal(Long invoiceStatusId,boolean isDeleted,boolean isInternal);

	@Cacheable("findAllByBankAndIsDeleted")
	List<ProjectInvoice> findAllByBankAndIsDeleted(InvoiceBank bank, boolean b);

	@Cacheable("findAllByMonthAndYearAndIsDeletedAndIsInternalAndInvoiceStatusNot")
	List<ProjectInvoice> findAllByMonthAndYearAndIsDeletedAndIsInternalAndInvoiceStatusNot(String monthName,
			String string, boolean b, boolean c, long l);

	@Cacheable("findAllByMonthAndYearAndIsDeletedAndProjectIdAndInvoiceStatusNot")
	List<ProjectInvoice> findAllByMonthAndYearAndIsDeletedAndProjectIdAndInvoiceStatusNot(String string, String string2,
			boolean b, Long projectId, long l);

	@Cacheable("findAllByMonthAndYearAndIsDeletedAndProjectId")
	List<ProjectInvoice> findAllByMonthAndYearAndIsDeletedAndProjectId(String monthName, String string, boolean b,
			Long projectId);

	@Cacheable("findAllByMonthAndYearAndBankAndIsDeleted")
	List<ProjectInvoice> findAllByMonthAndYearAndBankAndIsDeleted(String month, String year, InvoiceBank bank,Boolean deleted);

	@Cacheable("findAllByYearAndBankAndIsDeleted")
	List<ProjectInvoice> findAllByYearAndBankAndIsDeleted(String year, InvoiceBank bank,Boolean deleted);

	@Cacheable("findAllByYearAndModeOfPaymentIdAndIsDeleted")
	List<ProjectInvoice> findAllByYearAndModeOfPaymentIdAndIsDeleted(String year, Long id,Boolean deleted);

	@Cacheable("findAllByMonthAndYearAndIsDeleted")
	List<ProjectInvoice> findAllByMonthAndYearAndIsDeleted(String month, String year, boolean b);

	@Cacheable("findAllByYearAndIsDeleted")
	List<ProjectInvoice> findAllByYearAndIsDeleted(String year, boolean b);

	@Cacheable("findByProjectIdAndMonthAndYearAndIsDeleted")
	List<ProjectInvoice> findByProjectIdAndMonthAndYearAndIsDeleted(Long projectId, String month, String year,
			boolean b);

	@Cacheable("findAllByProjectIdAndInvoiceStatusAndIsDeleted")
	List<ProjectInvoice> findAllByProjectIdAndInvoiceStatusAndIsDeleted(Long projectId, long l, boolean b);

	@Cacheable("findAllByProjectIdAndYearAndIsDeleted")
	List<ProjectInvoice> findAllByProjectIdAndYearAndIsDeleted(long projectId, String year, boolean isDeleted);

	@Cacheable("findAllByYearAndInvoiceStatusAndIsDeletedAndIsInternalAndProjectIdIn")
	Optional<List<ProjectInvoice>> findAllByYearAndInvoiceStatusAndIsDeletedAndIsInternalAndProjectIdIn(String valueOf,
			Long invoiceStatus, boolean b, boolean c, List<Long> projectIds);

	@Cacheable("findByProjectIdInAndMonthAndYearAndIsDeletedAndIsInternal")
	List<ProjectInvoice> findByProjectIdInAndMonthAndYearAndIsDeletedAndIsInternal(List<Long> projectIds, String month,
			String year, boolean isDeleted, boolean isInternal);
	@Cacheable("findAllByMonthAndYearAndIsDeletedAndIsInternalAndProjectIdIn")
	List<ProjectInvoice> findAllByMonthAndYearAndIsDeletedAndIsInternalAndProjectIdIn(String monthName, String year,
			boolean b, boolean c, List<Long> projectIds);
	
	@Cacheable("findAllByMonthAndYearAndIsDeletedAndProjectIdIn")
	List<ProjectInvoice> findAllByMonthAndYearAndIsDeletedAndProjectIdIn(String monthName, String year,
			boolean b, List<Long> projectIds);

	@Cacheable("findAllByProjectId")
	List<ProjectInvoice> findAllByProjectId(Long projectId);

	@Cacheable("findAllByMonthAndYearAndIsDeletedAndProjectIdAndIsInternalAndInvoiceStatusNot")
	List<ProjectInvoice> findAllByMonthAndYearAndIsDeletedAndProjectIdAndIsInternalAndInvoiceStatusNot(String monthName,
			String string, boolean b, Long projectId, boolean c, long l);

	@Cacheable("findAllByProjectIdInAndYearAndIsDeletedAndIsInternal")
	List<ProjectInvoice> findAllByProjectIdInAndYearAndIsDeletedAndIsInternal(List<Long> projectIds, String year,
			boolean b, boolean c);
	
	@Cacheable("findAllByMonthAndYearAndIsDeletedAndInvoiceStatusNot")
	List<ProjectInvoice> findAllByMonthAndYearAndIsDeletedAndInvoiceStatusNot(String monthName, String string,
			boolean b, long l);

	@Cacheable("findAllByYearAndIsDeletedAndIsInternalAndProjectIdIn")
	List<ProjectInvoice> findAllByYearAndIsDeletedAndIsInternalAndProjectIdIn(String year, boolean b, boolean c,
			List<Long> projectIds);

	@Cacheable("findAllByIsDeleted")
	List<ProjectInvoice> findAllByIsDeleted(boolean isDeleted);

	@Cacheable("findAllByYearAndIsDeletedAndIsInternalAndInvoiceStatusNot")
	List<ProjectInvoice> findAllByYearAndIsDeletedAndIsInternalAndInvoiceStatusNot(String year, boolean b, boolean c,
			long l);

	@Cacheable("findAllByIsDeletedAndIsInternalAndInvoiceStatusNot")
	List<ProjectInvoice> findAllByIsDeletedAndIsInternalAndInvoiceStatusNot(boolean b, boolean c, long l);
	
	@Cacheable("findAllByMonthInAndYearAndIsDeletedAndIsInternal")
	List<ProjectInvoice> findAllByMonthInAndYearAndIsDeletedAndIsInternal(List<String> month, String year, boolean b,
			boolean c);
	@Cacheable("findAllByMonthInAndYearAndIsDeleted")
	List<ProjectInvoice> findAllByMonthInAndYearAndIsDeleted(List<String> month, String year, boolean b);

	@Cacheable("findAllByProjectIdInAndYearAndMonthInAndIsDeletedAndIsInternal")
	List<ProjectInvoice> findAllByProjectIdInAndYearAndMonthInAndIsDeletedAndIsInternal(List<Long> activeProjects,
			String year, List<String> monthList, boolean b, boolean c);

	@Cacheable("findAllByBankLocationId")
	List<ProjectInvoice> findAllByBankLocationId(long id);

	@Override
	@CacheEvict(value = {"findById","findByIsDeletedAndInvoiceCycleIdAndIsInternal","findByIsDeletedAndPaymentTermsIdAndIsInternal",
"findByIsDeletedAndModeOfPaymentIdAndIsInternal","findByProjectAndMonthAndYearAndIsDeletedAndIsInternal","findAllByMonthAndYearAndIsDeletedAndInvoiceStatusAndIsInternal",
"findByProjectAndYearAndIsDeletedAndIsInternal","findByMonthAndInvoiceStatusAndYearAndIsDeletedAndIsInternal","findByMonthAndYearAndIsDeletedAndIsInternal",
"findByBillingDateBetween","findByDueDateBetween","findByReceivedOnBetween","findByProjectIdAndMonthAndYearAndIsDeletedAndIsInternal",
"findByProjectIdAndIsDeletedAndIsInternal","findAllByIsDeletedAndIsInternal","findAllByMonthAndYearAndIsDeletedAndProjectIdAndIsInternal","findAllByInvoiceStatusAndMonthAndYearAndIsDeletedAndIsInternal",
"countByIsDeleted","countByInvoiceStatusAndIsDeletedAndIsInternal","findByProjectIdAndIsDeletedAndIsInternalOrderByIdAsc","findAllByMonthAndYearAndProjectIdAndIsDeletedAndIsInternal","findAllByProjectIdAndMonthAndIsDeletedAndIsInternal",
"findByProjectIdAndYearAndIsDeletedAndIsInternal","findAllByProjectIdAndIsDeletedAndIsInternal","findAllByYearAndInvoiceStatusAndIsDeletedAndIsInternal","findAllByProjectIdAndInvoiceStatusAndIsDeletedAndIsInternal","findAllByProjectIdAndInvoiceStatusAndIsDeletedAndYearAndIsInternal",
"findAllByProjectIdAndInvoiceStatusAndYearAndIsDeletedAndIsInternal","findAllByYearAndIsDeletedAndIsInternal","findAllByMonthAndYearAndIsDeletedAndIsInternal","findAllByRaisedToBuAndMonthAndYearAndIsDeletedAndIsInternal","findAllByRaisedFromBuAndMonthAndYearAndIsDeletedAndIsInternal",
"findAllByMonthAndYearAndModeOfPaymentIdAndIsDeleted","findAllByInvoiceStatusAndIsDeletedAndIsInternal","findAllByBankAndIsDeleted","findAllByMonthAndYearAndIsDeletedAndIsInternalAndInvoiceStatusNot","findAllByMonthAndYearAndIsDeletedAndProjectIdAndInvoiceStatusNot","findAllByMonthAndYearAndIsDeletedAndProjectId",
"findAllByMonthAndYearAndBankAndIsDeleted","findAllByYearAndBankAndIsDeleted","findAllByYearAndModeOfPaymentIdAndIsDeleted","findAllByMonthAndYearAndIsDeleted","findAllByYearAndIsDeleted","findByProjectIdAndMonthAndYearAndIsDeleted","findAllByProjectIdAndInvoiceStatusAndIsDeleted","findAllByProjectIdAndYearAndIsDeleted",
"findAllByYearAndInvoiceStatusAndIsDeletedAndIsInternalAndProjectIdIn","findByProjectIdInAndMonthAndYearAndIsDeletedAndIsInternal","findAllByMonthAndYearAndIsDeletedAndIsInternalAndProjectIdIn","findAllByMonthAndYearAndIsDeletedAndProjectIdIn","findAllByProjectId","findAllByMonthAndYearAndIsDeletedAndProjectIdAndIsInternalAndInvoiceStatusNot",
"findAllByProjectIdInAndYearAndIsDeletedAndIsInternal","findAllByMonthAndYearAndIsDeletedAndInvoiceStatusNot","findAllByYearAndIsDeletedAndIsInternalAndProjectIdIn","findAllByIsDeleted","findAllByYearAndIsDeletedAndIsInternalAndInvoiceStatusNot","findAllByIsDeletedAndIsInternalAndInvoiceStatusNot","findAllByMonthInAndYearAndIsDeletedAndIsInternal",
"findAllByMonthInAndYearAndIsDeleted","findAllByProjectIdInAndYearAndMonthInAndIsDeletedAndIsInternal","findAllByBankLocationId","findByProjectAndYearAndIsDeleted","findByProjectAndMonthAndYearAndIsDeleted","findByProjectIdAndYearAndIsDeleted","findAllByMonthAndYearAndProjectIdAndIsDeleted","findAllByYearAndIsDeletedAndInvoiceStatusNot","findAllByYearAndIsDeletedAndInvoiceStatus",
"findAllByProjectIdInAndYearAndIsDeleted","findAllByIsDeletedAndIsInternalAndInvoiceStatusAndYearIn","findAllByIsDeletedAndIsInternalAndInvoiceStatusNotAndYearIn","findAllByIsDeletedAndInvoiceStatusNot"}, allEntries = true)
    <S extends ProjectInvoice> S save(S entity);
	
	@Override
	@CacheEvict(value = {"findById","findByIsDeletedAndInvoiceCycleIdAndIsInternal","findByIsDeletedAndPaymentTermsIdAndIsInternal",
"findByIsDeletedAndModeOfPaymentIdAndIsInternal","findByProjectAndMonthAndYearAndIsDeletedAndIsInternal","findAllByMonthAndYearAndIsDeletedAndInvoiceStatusAndIsInternal",
"findByProjectAndYearAndIsDeletedAndIsInternal","findByMonthAndInvoiceStatusAndYearAndIsDeletedAndIsInternal","findByMonthAndYearAndIsDeletedAndIsInternal",
"findByBillingDateBetween","findByDueDateBetween","findByReceivedOnBetween","findByProjectIdAndMonthAndYearAndIsDeletedAndIsInternal",
"findByProjectIdAndIsDeletedAndIsInternal","findAllByIsDeletedAndIsInternal","findAllByMonthAndYearAndIsDeletedAndProjectIdAndIsInternal","findAllByInvoiceStatusAndMonthAndYearAndIsDeletedAndIsInternal",
"countByIsDeleted","countByInvoiceStatusAndIsDeletedAndIsInternal","findByProjectIdAndIsDeletedAndIsInternalOrderByIdAsc","findAllByMonthAndYearAndProjectIdAndIsDeletedAndIsInternal","findAllByProjectIdAndMonthAndIsDeletedAndIsInternal",
"findByProjectIdAndYearAndIsDeletedAndIsInternal","findAllByProjectIdAndIsDeletedAndIsInternal","findAllByYearAndInvoiceStatusAndIsDeletedAndIsInternal","findAllByProjectIdAndInvoiceStatusAndIsDeletedAndIsInternal","findAllByProjectIdAndInvoiceStatusAndIsDeletedAndYearAndIsInternal",
"findAllByProjectIdAndInvoiceStatusAndYearAndIsDeletedAndIsInternal","findAllByYearAndIsDeletedAndIsInternal","findAllByMonthAndYearAndIsDeletedAndIsInternal","findAllByRaisedToBuAndMonthAndYearAndIsDeletedAndIsInternal","findAllByRaisedFromBuAndMonthAndYearAndIsDeletedAndIsInternal",
"findAllByMonthAndYearAndModeOfPaymentIdAndIsDeleted","findAllByInvoiceStatusAndIsDeletedAndIsInternal","findAllByBankAndIsDeleted","findAllByMonthAndYearAndIsDeletedAndIsInternalAndInvoiceStatusNot","findAllByMonthAndYearAndIsDeletedAndProjectIdAndInvoiceStatusNot","findAllByMonthAndYearAndIsDeletedAndProjectId",
"findAllByMonthAndYearAndBankAndIsDeleted","findAllByYearAndBankAndIsDeleted","findAllByYearAndModeOfPaymentIdAndIsDeleted","findAllByMonthAndYearAndIsDeleted","findAllByYearAndIsDeleted","findByProjectIdAndMonthAndYearAndIsDeleted","findAllByProjectIdAndInvoiceStatusAndIsDeleted","findAllByProjectIdAndYearAndIsDeleted",
"findAllByYearAndInvoiceStatusAndIsDeletedAndIsInternalAndProjectIdIn","findByProjectIdInAndMonthAndYearAndIsDeletedAndIsInternal","findAllByMonthAndYearAndIsDeletedAndIsInternalAndProjectIdIn","findAllByMonthAndYearAndIsDeletedAndProjectIdIn","findAllByProjectId","findAllByMonthAndYearAndIsDeletedAndProjectIdAndIsInternalAndInvoiceStatusNot",
"findAllByProjectIdInAndYearAndIsDeletedAndIsInternal","findAllByMonthAndYearAndIsDeletedAndInvoiceStatusNot","findAllByYearAndIsDeletedAndIsInternalAndProjectIdIn","findAllByIsDeleted","findAllByYearAndIsDeletedAndIsInternalAndInvoiceStatusNot","findAllByIsDeletedAndIsInternalAndInvoiceStatusNot","findAllByMonthInAndYearAndIsDeletedAndIsInternal",
"findAllByMonthInAndYearAndIsDeleted","findAllByProjectIdInAndYearAndMonthInAndIsDeletedAndIsInternal","findAllByBankLocationId","findByProjectAndYearAndIsDeleted","findByProjectAndMonthAndYearAndIsDeleted","findByProjectIdAndYearAndIsDeleted","findAllByMonthAndYearAndProjectIdAndIsDeleted","findAllByYearAndIsDeletedAndInvoiceStatusNot","findAllByYearAndIsDeletedAndInvoiceStatus",
"findAllByProjectIdInAndYearAndIsDeleted","findAllByIsDeletedAndIsInternalAndInvoiceStatusAndYearIn","findAllByIsDeletedAndIsInternalAndInvoiceStatusNotAndYearIn","findAllByIsDeletedAndInvoiceStatusNot"}, allEntries = true)
    <S extends ProjectInvoice> S saveAndFlush(S entity);

    @Cacheable("findByProjectAndYearAndIsDeleted")
	List<ProjectInvoice> findByProjectAndYearAndIsDeleted(String project, String year, boolean b);

    @Cacheable("findByProjectAndMonthAndYearAndIsDeleted")
	List<ProjectInvoice> findByProjectAndMonthAndYearAndIsDeleted(String project, String month, String year, boolean b);

    @Cacheable("findByProjectIdAndYearAndIsDeleted")
	List<ProjectInvoice> findByProjectIdAndYearAndIsDeleted(long project, String year, boolean b);

    @Cacheable("findAllByMonthAndYearAndProjectIdAndIsDeleted")
	List<ProjectInvoice> findAllByMonthAndYearAndProjectIdAndIsDeleted(String monthName, String string, Long projectId,
            boolean b);

	@Cacheable("findAllByYearAndIsDeletedAndInvoiceStatusNot")
	List<ProjectInvoice> findAllByYearAndIsDeletedAndInvoiceStatusNot(String year, boolean b, long l);

	@Cacheable("findAllByYearAndIsDeletedAndInvoiceStatus")
	List<ProjectInvoice> findAllByYearAndIsDeletedAndInvoiceStatus(String year, boolean b, long l);

	@Cacheable("findAllByProjectIdInAndYearAndIsDeleted")
	List<ProjectInvoice> findAllByProjectIdInAndYearAndIsDeleted(List<Long> activeProjects, String year, boolean b);

	@Query(value="select project_id, invoice_status, place_of_supply, paying_entity_name, received_on from project_invoice where  is_deleted=:b and is_internal =:isInternal", nativeQuery = true)
	List<Object []> findAllByNetiveIsDeletedAndIsInternal(boolean b,boolean isInternal);

	@Query(value="select project_id, invoice_status, place_of_supply, paying_entity_name, received_on from project_invoice where  project_id in (:projectIds) and  is_deleted=:b and is_internal =:isInternal", nativeQuery = true)
	List<Object []> findAllByNetiveIsDeletedAndIsInternalAndProjectIdIn(boolean b,boolean isInternal, Set<Long> projectIds);
	
	@Cacheable("findAllByIsDeletedAndIsInternalAndInvoiceStatusAndYearIn")
	//@Query(value="select * from project_invoice where year in (:yearList) and is_deleted=:b and is_interal=:b and invoice_status",nativeQuery = true)
	List<ProjectInvoice> findAllByIsDeletedAndIsInternalAndInvoiceStatusAndYearIn(boolean b,
            boolean c, long l,List<String> yearList);

	@Cacheable("findAllByIsDeletedAndIsInternalAndInvoiceStatusNotAndYearIn")
	List<ProjectInvoice> findAllByIsDeletedAndIsInternalAndInvoiceStatusNotAndYearIn(boolean b,
					boolean c, long l,List<String> yearList);
	@Query(value="select * from project_invoice where STR_TO_DATE(CONCAT(year, '-', month, '-01'), '%Y-%M-%d') BETWEEN :from AND :to and is_deleted=:isDeleted" , nativeQuery=true)
	List<ProjectInvoice> findAllByDateRangeFilterIsDeleted(Date from ,Date to, boolean isDeleted);
	
	@Query(value="select * from project_invoice where STR_TO_DATE(CONCAT(year, '-', month, '-01'), '%Y-%M-%d') BETWEEN :from AND :to and is_deleted=:isDeleted and is_Internal=:isInternal" , nativeQuery=true)
	List<ProjectInvoice> findAllByDateRangeFilterIsDeletedAndIsInternal(Date from ,Date to, boolean isDeleted, boolean isInternal);

	@Query(value="select * from project_invoice where project_id in (:projectIds) AND  STR_TO_DATE(CONCAT(year, '-', month, '-01'), '%Y-%M-%d') BETWEEN :from AND :to and is_deleted=:isDeleted" , nativeQuery=true)
	List<ProjectInvoice> findAllByProjectIdInDateRangeFilterIsDeleted(List<Long> projectIds, Date from ,Date to, boolean isDeleted);
	
	List<ProjectInvoice> findAllByYearAndIsDeletedAndProjectIdAndInvoiceStatusNot(String year, boolean b, long projectId, long l );

    @Cacheable("findAllByIsDeletedAndInvoiceStatusNot")
	List<ProjectInvoice> findAllByIsDeletedAndInvoiceStatusNot(boolean b, long l);

	List<ProjectInvoice> findAllBySecurityDepositeIdAndIsDeleted(long id,boolean b);
	
	@Cacheable("findAllByProjectIdAndIsDeletedAndInvoiceStatusNot")
	List<ProjectInvoice> findAllByProjectIdAndIsDeletedAndInvoiceStatusNot(Long projectId, boolean b, long l);
}
