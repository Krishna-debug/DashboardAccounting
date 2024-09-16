package com.krishna.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

	@Bean
	public JwtAuthorizationFilter authenticationTokenFilterBean() {
		return new JwtAuthorizationFilter();
	}

	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests()
				.antMatchers("/users/login", "/accounts/isRunning", "/accounts/isConnected","/monitoring/**","/api/v1/olap/syncInvoiceData","/api/v1/olap/syncSalaryData").permitAll()
				.antMatchers("/actuator/**").permitAll()
				.antMatchers("/v2/api-docs", "/configuration/ui", "/images/**", "/static/**", "/swagger-resources",
						"/configuration/security", "/swagger-ui.html", "/webjars/**", "/swagger.json",
						"/swagger-resources/configuration/ui", "/new.html","/myAccounts/**")
				.permitAll().anyRequest().authenticated().and()
				.addFilterBefore(authenticationTokenFilterBean() ,UsernamePasswordAuthenticationFilter.class)
				// this disables session creation on Spring Security
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("api/v1/login/filter");
	}

	
}
