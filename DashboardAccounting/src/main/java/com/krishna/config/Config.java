package com.krishna.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import feign.okhttp.OkHttpClient;

@Configuration
public class Config {
	
	@Value("${com.oodles.sesFromAddress}")
	private String mailFromSes;
	
	@Value("${com.oodles.mail.sesRegion}")
	private String sesRegion;
	
	@Value("${com.oodles.mail.sesPort}")
	private Integer sesPort;
	
	@Value("${com.oodles.mail.protocol}")
	private String protocol;
	
	@Value("${com.oodles.mail.username}")
	private String sesUsername;
	
	@Value("${com.oodles.mail.password}")
	private String sesPassword;

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	@Bean
	public ObjectMapper getObjectMapper() {
		return new ObjectMapper();
	}
	
	@Bean
    public OkHttpClient client() {
        return new OkHttpClient();
    }
	
	/*
	 * JavaMailSender with the amazon SES configuration
	 */
	@Bean
	public JavaMailSender javaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(sesRegion);
		mailSender.setPort(sesPort);
		mailSender.setUsername(sesUsername);
		mailSender.setPassword(sesPassword);
		mailSender.setProtocol(protocol);
		return mailSender;
	}
	
}
