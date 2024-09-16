package com.krishna.security;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.krishna.Auditing.AuditorDetails;
import com.krishna.config.EnvConfiguration;
import com.krishna.domain.ClientModel;
import com.krishna.domain.UserModel;
import com.krishna.util.ConstantUtility;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;

@Component
public class JwtValidator {

	public static final Logger logger = LoggerFactory.getLogger(JwtValidator.class);


	@Autowired
	EnvConfiguration environment;
	
	
	public UserModel tokenbValidate(String token) {
		UserModel jwtUser = JwtParserValidation(token);
		try {
			if (jwtUser != null) {
				logger.debug(ConstantUtility.VALID_TOKEN);
			} else {
				logger.debug(ConstantUtility.INVALID_TOKEN);
			}
		} catch (Exception e) {
			logger.error("Error occured", e);
			return null;
		}
		return jwtUser;

	}
	
	public ClientModel clientTokenValidate(String token) {
		ClientModel jwtClient = JwtParserValidationClient(token);
		try {
			if (jwtClient != null) {
				logger.debug(ConstantUtility.VALID_TOKEN);
			} else
				logger.debug(ConstantUtility.INVALID_TOKEN);
		} catch (Exception e) {
			logger.error("Error occured", e);
			return null;
		}
		return jwtClient;

	}
	
 /**
 * @modifiedBy Dharmendra
 * @param header
 * @return
 */
	public UserModel JwtParserValidation(String header) {
		
		try {
			String token = header.replace("Bearer ", "");
			
			JWT jwt = JWTParser.parse(token);

			byte[] secret = environment.getSecretKey().getBytes();
			SignedJWT signedJwt = (SignedJWT) jwt;

			Timestamp ts1 = new Timestamp(jwt.getJWTClaimsSet().getExpirationTimeClaim() * 1000);
			Timestamp ts2 = new Timestamp(System.currentTimeMillis());
			logger.debug("expire--" + ts1);
			logger.debug("current time--" + ts2);

			if (ts2.before(ts1)) {
				logger.debug(ConstantUtility.VALID_TOKEN);
			} else {
				logger.debug(ConstantUtility.INVALID_TOKEN);
				return null;
			}

			Map<String, Object> roles = signedJwt.getJWTClaimsSet().getCustomClaims();

			UserModel jwtUser = new UserModel();

			String email = signedJwt.getJWTClaimsSet().getSubjectClaim();
			String[] a= email.split("\\.");
			
			String[] b=a[0].split("@");
			String firstName=StringUtils.capitalize(b[0]);
			
			String lastName=StringUtils.capitalize(a[1].split("@")[0]);
			jwtUser.setEmail(signedJwt.getJWTClaimsSet().getSubjectClaim());
			jwtUser.setRoles((ArrayList<String>) roles.get("roles"));
			jwtUser.setUserId((Long)signedJwt.getJWTClaimsSet().getCustomClaims().get("id"));
			jwtUser.setGrade((String) signedJwt.getJWTClaimsSet().getCustomClaims().get("grade"));
			jwtUser.setRank((long) signedJwt.getJWTClaimsSet().getCustomClaims().get("rank"));
			if (b.length > 1)
				jwtUser.setEmpName(firstName);
			else
				jwtUser.setEmpName(firstName + " " + lastName);
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

	public ClientModel JwtParserValidationClient(String header) {
		
		try {
			String token = header.replace("Bearer ", "");
			
			JWT jwt = JWTParser.parse(token);

			byte[] secret = environment.getSecretKey().getBytes();
			SignedJWT signedJwt = (SignedJWT) jwt;

			Timestamp ts1 = new Timestamp(jwt.getJWTClaimsSet().getExpirationTimeClaim() * 1000);
			Timestamp ts2 = new Timestamp(System.currentTimeMillis());
			logger.debug("expire--" + ts1);
			logger.debug("current time--" + ts2);

			if (ts2.before(ts1)) {
				logger.debug(ConstantUtility.VALID_TOKEN);
			} else {
				logger.debug(ConstantUtility.INVALID_TOKEN);
				return null;
			}

			Map<String, Object> roles = signedJwt.getJWTClaimsSet().getCustomClaims();
			ClientModel jwtClient = new ClientModel();

			String email = signedJwt.getJWTClaimsSet().getSubjectClaim();

			jwtClient.setEmail(signedJwt.getJWTClaimsSet().getSubjectClaim());
			jwtClient.setRoles((ArrayList<String>) roles.get("roles"));
			jwtClient.setContactId((Long)signedJwt.getJWTClaimsSet().getCustomClaims().get("id"));
			
			if (!signedJwt.verify(new MACVerifier(secret))) {
				return null;
			}

			return jwtClient;

		} catch (Exception ex) {

			logger.error(" Exception :: {} ",ex.getMessage());
			return null;
		}
	}
}
