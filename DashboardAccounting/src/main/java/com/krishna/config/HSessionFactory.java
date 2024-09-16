/**
 * 
 */
package com.krishna.config;



import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author ved
 *
 */
@Component
public class HSessionFactory {
	
	@Autowired
	EntityManagerFactory entityManagerFactory;

	/*
	 * @Bean public SessionFactory getSessionFactory() { if
	 * (entityManagerFactory.unwrap(SessionFactory.class) == null) { throw new
	 * NullPointerException("factory is not a hibernate factory"); } return
	 * entityManagerFactory.unwrap(SessionFactory.class); }
	 */

}
