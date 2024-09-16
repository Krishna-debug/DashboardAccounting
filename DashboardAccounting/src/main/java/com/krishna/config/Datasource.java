package com.krishna.config;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

/**
 * The Class Datasource.
 */
@Component
public class Datasource {
	
	/** The configuration. */
	@Autowired
	EnvConfiguration configuration;


	/**
	 * Data source.
	 *
	 * @return the data source
	 */
	@Bean
	public DataSource dataSource() {
		
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(configuration.getDataBaseDriver());
		dataSource.setUrl("jdbc:mysql://" + configuration.getDataBaseIpAddress() + ":" + configuration.getDataBasePort() + "/"
				+ configuration.getDataBaseName()+"?autoReconnect=true&useSSL=false");
		dataSource.setUsername(configuration.getDataBaseUserName().trim());
		dataSource.setPassword(configuration.getDataBasePassword().trim());
		return dataSource;
	}

}
