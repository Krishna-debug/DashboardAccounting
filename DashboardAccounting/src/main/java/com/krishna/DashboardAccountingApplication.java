package com.krishna;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
// import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

// import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import springfox.documentation.swagger2.annotations.EnableSwagger2;
@EnableScheduling
// @EnableEurekaClient
@EnableAsync
@EnableSwagger2
@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
@EnableJpaRepositories
@EnableCaching
// @EnableDiscoveryClient
public class DashboardAccountingApplication {
	public static void main(String[] args) {
		System.setProperty("javamelody.application-name", "DashboardAccounts");
		SpringApplication.run(DashboardAccountingApplication.class, args);
		
	}

}
