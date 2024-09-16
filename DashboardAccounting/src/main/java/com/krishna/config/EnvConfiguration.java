package com.krishna.config;

/**
 * The Interface EnvConfiguration.
 */
public interface EnvConfiguration {
		
		/**
		 * Gets the data base name.
		 *
		 * @return the data base name
		 */
		String getDataBaseName();

		
		/**
		 * Gets the data base driver.
		 *
		 * @return the data base driver
		 */
		String getDataBaseDriver();

		
		/**
		 * Gets the data base port.
		 *
		 * @return the data base port
		 */
		String getDataBasePort();

		
		/**
		 * Ge data base ip address.
		 *
		 * @return the string
		 */
		String getDataBaseIpAddress();

		
		/**
		 * Gets the data base user name.
		 *
		 * @return the data base user name
		 */
		String getDataBaseUserName();

		
		/**
		 * Gets the data base password.
		 *
		 * @return the data base password
		 */
		String getDataBasePassword();
		
		String getSecretKey();
}
