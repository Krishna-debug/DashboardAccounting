package com.krishna.Auditing;

import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.hibernate.envers.RevisionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.krishna.domain.RevInfo;
import com.krishna.domain.UserModel;
import com.krishna.security.JwtValidator;

/**
 * @author Shivangi
 */
public class ConferenceRevisionListener implements RevisionListener {
	
	@Autowired
	JwtValidator validator;

	@Override
	public void newRevision(Object revisionEntity) {
		RevInfo entity = (RevInfo) revisionEntity;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserModel model= null;
        String token= ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization");
        if(token!=null)
        	model=validator.tokenbValidate(token);
		entity.setAuditorEmail(auth.getPrincipal().toString());
		if(model!=null) {
			AuditorDetails.auditorId = model.getUserId();
			AuditorDetails.auditorName = model.getEmpName();
			entity.setAuditorId(AuditorDetails.auditorId);
			entity.setAuditorName(AuditorDetails.auditorName);
		}
		
	}

}
