package com.krishna.security;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.krishna.Auditing.AuditorDetails;
import com.krishna.config.EnvConfiguration;
import com.krishna.domain.UserModel;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;


public class JwtAuthorizationFilter extends OncePerRequestFilter{

	public static final Logger logger = LoggerFactory.getLogger(JwtValidator.class);
	
	@Autowired
	EnvConfiguration environment;
	


	

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		String header = req.getHeader("Authorization");
		
		if (header == null) {
			chain.doFilter(req, res);
			return;
		}

		UsernamePasswordAuthenticationToken authentication = getAuthentication(req);

		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(req, res);
	}

	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		String token = header.replace("Bearer ", "");
		if (token != null) {
			UserModel jwtUser = JwtParserValidation(token);
			try {
				if (jwtUser != null) {
					logger.debug("valid token");
					String[] roles = jwtUser.getRoles().toArray(new String[0]);
					List < GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(roles);
					return new UsernamePasswordAuthenticationToken(jwtUser.getEmail(), null,authorities);
				} else {
					logger.debug("invalid token");
				}
			} catch (Exception e) {
				logger.error(" Exception :: {} ",e.getMessage());
				return null;
			}
		}
		return null;
	}
	
	
	public UserModel JwtParserValidation(String token) {
		try {

			JWT jwt = JWTParser.parse(token);
			byte[] secret = environment.getSecretKey().getBytes();
			SignedJWT signedJwt = (SignedJWT) jwt;

			Timestamp ts1 = new Timestamp(jwt.getJWTClaimsSet().getExpirationTimeClaim() * 1000);
			Timestamp ts2 = new Timestamp(System.currentTimeMillis());
			logger.debug("expire--" + ts1);
			logger.debug("current time--" + ts2);

			if (ts2.before(ts1)) {
				
			} else {
				
				return null;
			}

			Map<String, Object> roles = signedJwt.getJWTClaimsSet().getCustomClaims();

            String email = signedJwt.getJWTClaimsSet().getSubjectClaim();
			
			String[] a= email.split("\\.");
			String firstName = a[0].replace(a[0].charAt(0), a[0].toUpperCase().charAt(0));
			String lastName = a[1].split("@")[0].replace(a[1].split("@")[0].charAt(0), a[1].split("@")[0].toUpperCase().charAt(0));
			
			UserModel jwtUser = new UserModel();
			Long userId = (Long)signedJwt.getJWTClaimsSet().getCustomClaims().get("id");
			jwtUser.setEmail(signedJwt.getJWTClaimsSet().getSubjectClaim());
			jwtUser.setRoles((ArrayList<String>) roles.get("roles"));
			jwtUser.setEmpName(firstName + " " +lastName);
			jwtUser.setUserId(userId);
			AuditorDetails.auditorId = jwtUser.getUserId();
			AuditorDetails.auditorName = firstName + " " +lastName;	
			if (!signedJwt.verify(new MACVerifier(secret))) {
				return null;
			}

			return jwtUser;

		} catch (Exception ex) {

			logger.error(" Exception :: {} ",ex.getMessage());
			return null;
		}
	}
	
	/**
	 * Sets the user details in UserModel Domain from domain 
	 * 
	 * @param signedJwt
	 * @return
	 * @throws ParseException
	 */
	public UserModel setUserDetails(SignedJWT signedJwt) throws ParseException {
		logger.info("Inside setUserDetails method");
		Map<String, Object> roles = signedJwt.getJWTClaimsSet().getCustomClaims();
		String email = signedJwt.getJWTClaimsSet().getSubjectClaim();
		
		String[] a= email.split("\\.");
		String firstName = a[0].replace(a[0].charAt(0), a[0].toUpperCase().charAt(0));
		String lastName = a[1].split("@")[0].replace(a[1].split("@")[0].charAt(0), a[1].split("@")[0].toUpperCase().charAt(0));
		
		UserModel jwtUser = new UserModel();
		Long userId = (Long)signedJwt.getJWTClaimsSet().getCustomClaims().get("id");
		jwtUser.setEmail(signedJwt.getJWTClaimsSet().getSubjectClaim());
		jwtUser.setRoles((ArrayList<String>) roles.get("roles"));
		jwtUser.setEmpName(firstName + " " +lastName);
		jwtUser.setUserId(userId);
		return jwtUser;	
	}
}
