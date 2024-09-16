package com.krishna.repository.payroll;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.PayRegister;
import com.krishna.enums.PayRegisterStatus;

/**
 * @author shivangi
 * 
 *         The Interface Payregister Repository
 */
public interface PayRegisterRepository extends JpaRepository<PayRegister, Long> {

	@Cacheable("findAllById")
	PayRegister findAllById(long id);

	@Cacheable("findAllByUserId")
	PayRegister findAllByUserId(long userId);

	@Cacheable("findAllByIsCurrent")
	List<PayRegister> findAllByIsCurrent(boolean b);

	@Cacheable("findByUserId")
	List<PayRegister> findByUserId(long userId);

	@Cacheable("findAllByUserIdAndIsCurrent")
	PayRegister findAllByUserIdAndIsCurrent(long userId, boolean b);

	@Cacheable("findByIsCurrentAndStatus")
	List<PayRegister> findByIsCurrentAndStatus(boolean b, PayRegisterStatus complete);

	@Cacheable("findByIsCurrentAndStatusAndUserId")
	PayRegister findByIsCurrentAndStatusAndUserId(boolean b, PayRegisterStatus complete, Long userId);
	
	@Cacheable("findByUserIdAndIsCurrent")
	PayRegister findByUserIdAndIsCurrent(long userId, boolean isCurrent);

	@Override
	@CacheEvict(value = {"findAllById","findAllByUserId","findAllByIsCurrent","findByUserId","findAllByUserIdAndIsCurrent","findByIsCurrentAndStatus",
"findByIsCurrentAndStatusAndUserId","findByUserIdAndIsCurrent"},allEntries = true)
	<S extends PayRegister> S save(S entity);

	@Override
	@CacheEvict(value = {"findAllById","findAllByUserId","findAllByIsCurrent","findByUserId","findAllByUserIdAndIsCurrent","findByIsCurrentAndStatus",
"findByIsCurrentAndStatusAndUserId","findByUserIdAndIsCurrent"},allEntries = true)
	<S extends PayRegister> S saveAndFlush(S entity);



}
