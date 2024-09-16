package com.krishna.util;

import java.util.Properties;

/**
 * Message Utility Class for reading message
 */

public class MessageUtility {

	private static Properties message = new Properties();

	public MessageUtility() {
	}

	/**
	 * Initialize Message from Properties File
	 */

	public void initialze() {
		loadMessage();
	}

	/**
	 * Return value corresponding to key if not found return null
	 * 
	 * @param key
	 * @return
	 */
	public static String getMessage(String key) {
		return (String) message.get(key);
	}

	private void loadMessage() {
		Properties propertiesObj = null;
		propertiesObj = TicketUtilities.propertiesFileReader(UrlMappings.MESSAGE_FILE_PATH);
		if (!TicketUtilities.isNull(propertiesObj)) {
			message = propertiesObj;
		}
	}

}
