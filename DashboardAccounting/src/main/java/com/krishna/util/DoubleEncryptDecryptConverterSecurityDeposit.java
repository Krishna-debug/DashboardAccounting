package com.krishna.util;

import javax.persistence.Converter;

@Converter(autoApply = false)

	
 public class DoubleEncryptDecryptConverterSecurityDeposit extends AbstractEncryptDecryptConverterSecurityDeposit<Double>  {
	
	    public DoubleEncryptDecryptConverterSecurityDeposit() {
		  this(new CipherMaker());
		 }

		 public DoubleEncryptDecryptConverterSecurityDeposit(CipherMaker cipherMaker) {
		  super(cipherMaker);
		 }

		 @Override
		 boolean isNotNullOrEmpty(Double attribute) {
		  return attribute != null;
		 }

		 @Override
		 Double convertStringToEntityAttribute(String dbData) {
		  return Double.parseDouble(dbData);
		 }

		 @Override
		 String convertEntityAttributeToString(Double attribute) {
		  return String.valueOf(attribute);
		 }

}
