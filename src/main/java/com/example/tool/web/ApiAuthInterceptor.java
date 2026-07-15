package com.example.tool.web;

import com.example.tool.config.AuthProperties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiAuthInterceptor implements HandlerInterceptor {

	private final AuthProperties properties;

	public ApiAuthInterceptor(AuthProperties properties) {
		this.properties = properties;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (!properties.isEnabled()) {
			return true;
		}
		String uri = request.getRequestURI();
		if (uri.startsWith("/api/auth/") || "/api/health".equals(uri)) {
			return true;
		}
		if (AuthSession.authenticated(request.getSession(false))) {
			return true;
		}
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write("{\"error\":\"Ban can dang nhap.\"}");
		return false;
	}
}