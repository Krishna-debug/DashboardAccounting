package com.krishna.config;

import org.springframework.beans.factory.annotation.Value;



/**
 * The Class DevelopmentEnv.
 */
public class LocalDevelopmentEnv implements EnvConfiguration {
	
	/** The data base name. */
	@Value("${com.oodles.dataBaseName.localDevelopment}")
	private String dataBaseName;
	
	/** The driver name. */
	@Value("${com.oodles.driverName}")
	private String driverName;
	
	/** The data base port. */
	@Value("${com.oodles.dataBasePort.localDevelopment}")
	private String dataBasePort;
	
	/** The data base ip address. */
	@Value("${com.oodles.dataBaseIpAddress.localDevelopment}")
	private String dataBaseIpAddress;
	
	/** The data base user name. */
	@Value("${com.oodles.dataBaseUserName.localDevelopment}")
	private String dataBaseUserName;
	
	/** The data base password. */
	@Value("${com.oodles.dataBasePassword.localDevelopment}")
	private String dataBasePassword;
	
	@Value("${jwt.secret.key}")
	private String secretKey;
	
	/* (non-Javadoc)
	 * @see com.oodles.config.EnvConfiguration#getDataBaseName()
	 */
	@Override
	public String getDataBaseName() {
		return dataBaseName;
	}
	
	/* (non-Javadoc)
	 * @see com.oodles.config.EnvConfiguration#getDataBaseDriver()
	 */
	@Override
	public String getDataBaseDriver() {
		return driverName;
	}
	
	/* (non-Javadoc)
	 * @see com.oodles.config.EnvConfiguration#getDataBasePort()
	 */
	@Override
	public String getDataBasePort() {
		return dataBasePort;
	}
	
	/* (non-Javadoc)
	 * @see com.oodles.config.EnvConfiguration#geDataBaseIpAddress()
	 */
	@Override
	public String getDataBaseIpAddress() {
		return dataBaseIpAddress;
	}
	
	/* (non-Javadoc)
	 * @see com.oodles.config.EnvConfiguration#getDataBaseUserName()
	 */
	@Override
	public String getDataBaseUserName() {
		return dataBaseUserName;
	}
	
	/* (non-Javadoc)
	 * @see com.oodles.config.EnvConfiguration#getDataBasePassword()
	 */
	@Override
	public String getDataBasePassword() {
		return dataBasePassword;
	}
	
	@Override
	public String getSecretKey() {
		return secretKey;
	}
	
}
