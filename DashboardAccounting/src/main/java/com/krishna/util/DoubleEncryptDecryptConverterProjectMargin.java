package com.krishna.util;

import javax.persistence.Converter;

@Converter(autoApply = false)

	
 public class DoubleEncryptDecryptConverterProjectMargin extends AbstractEncryptDecryptConverterProjectMargin<Double>  {
	
	    public DoubleEncryptDecryptConverterProjectMargin() {
		  this(new CipherMaker());
		 }

		 public DoubleEncryptDecryptConverterProjectMargin(CipherMaker cipherMaker) {
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
