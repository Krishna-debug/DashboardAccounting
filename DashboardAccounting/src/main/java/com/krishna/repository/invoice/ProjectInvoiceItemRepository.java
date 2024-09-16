package com.krishna.repository.invoice;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.invoice.ProjectInvoice;
import com.krishna.domain.invoice.ProjectInvoiceItem;

public interface ProjectInvoiceItemRepository extends JpaRepository<ProjectInvoiceItem, Long>{

	Optional<ProjectInvoiceItem> findByUserIdAndProjectInvoiceIdAndIsDeleted(Long userId,Long invoiceId,Boolean b);

	List<ProjectInvoiceItem> findAllByProjectInvoiceIdAndIsDeleted(Long id, boolean b);

	Optional<ProjectInvoiceItem> findByUnitDescriptionAndIsDeletedAndProjectInvoiceId(String unitDescription, boolean b,
			Long projectInvoiceId);

	List<ProjectInvoiceItem> findAllByProjectInvoiceIdAndIsDeletedAndIsIfsd(long id, boolean b, boolean c);

	Optional<ProjectInvoiceItem> findByUserNameAndIsDeletedAndProjectInvoiceId(String unitDescription, boolean b,
			Long projectInvoiceId);
}
