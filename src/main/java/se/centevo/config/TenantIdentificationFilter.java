package se.centevo.config;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class TenantIdentificationFilter extends OncePerRequestFilter {
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return request.getRequestURI().startsWith("/login");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
    	String tenantId = tryGetTentantIdFromSession(request.getSession());
    	if(missingTenantIdInSession(tenantId)) {
    		tenantId = tryGetTenantFromAuthentication();
    	}
    	TenantContext.setTenantId(tenantId);
        
    	try {
    		filterChain.doFilter(request, response);
    	} finally {
    		TenantContext.clear();
    	}
    }

	private boolean missingTenantIdInSession(String tenantId) {
		return tenantId == null;
	}

	private String tryGetTentantIdFromSession(HttpSession session) {
		Object tentantId = session.getAttribute("tenantId");
		return tentantId != null ? (String)tentantId : null;
	}
    
    private String tryGetTenantFromAuthentication() {
    	String tentantId = null;
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	if(authentication instanceof UsernamePasswordAuthenticationToken) {
    		UserDetails principal = (UserDetails) authentication.getPrincipal();
    		tentantId = principal.getUsername();
    	}
    	return tentantId;
	}
}