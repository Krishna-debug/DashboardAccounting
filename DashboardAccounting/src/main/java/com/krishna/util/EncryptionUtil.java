package com.krishna.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtil<X> {
	
	private static final Logger logger = LoggerFactory.getLogger(EncryptionUtil.class);
	
	
	static String ENCRYPTION_SECRET;
			
	public EncryptionUtil(@Value("${com.oodles.util.encryptionKey}") String secretKey) {
		EncryptionUtil.ENCRYPTION_SECRET = secretKey;
	}
	
	public Object getDecryptedValue(Object valueToDecrypt) {
		Cipher cipher = null;
		try {
				cipher = new CipherMaker().configureAndGetInstance(Cipher.DECRYPT_MODE, 
						EncryptionUtil.ENCRYPTION_SECRET);
				byte[] bytesToDecrypt = Base64.getDecoder().decode(String.valueOf(valueToDecrypt));
				byte[] decryptedBytes = cipher.doFinal(bytesToDecrypt);
				return new String(decryptedBytes);
				
		} catch(InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException | 
					InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			logger.error("Error Occured while encrypting data {}", e.getLocalizedMessage());
		}
		return null;
	}
	
	public String getEncryptedValue(X valueToEncrypt) {
		Cipher cipher = null;
		try {
				cipher = new CipherMaker().configureAndGetInstance(Cipher.ENCRYPT_MODE, 
						EncryptionUtil.ENCRYPTION_SECRET);
				byte[] bytesToEncrypt = String.valueOf(valueToEncrypt).getBytes();
				byte[] encryptedBytes = cipher.doFinal(bytesToEncrypt);
				return Base64.getEncoder().encodeToString(encryptedBytes);
				
		} catch (InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			logger.error("Error Occured while encrypting data {}", e.getLocalizedMessage());
		}
		return null;
	}}
