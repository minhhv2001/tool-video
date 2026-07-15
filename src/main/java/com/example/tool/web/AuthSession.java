package com.example.tool.web;

import javax.servlet.http.HttpSession;

public final class AuthSession {

	public static final String USERNAME = "AUTH_USERNAME";
	public static final String DISPLAY_NAME = "AUTH_DISPLAY_NAME";
	public static final String IS_ADMIN = "AUTH_IS_ADMIN";

	private AuthSession() {
	}

	public static String username(HttpSession session) {
		Object value = session == null ? null : session.getAttribute(USERNAME);
		return value == null ? null : value.toString();
	}

	public static String displayName(HttpSession session) {
		Object value = session == null ? null : session.getAttribute(DISPLAY_NAME);
		return value == null ? username(session) : value.toString();
	}

	public static boolean admin(HttpSession session) {
		Object value = session == null ? null : session.getAttribute(IS_ADMIN);
		return Boolean.TRUE.equals(value);
	}

	public static boolean authenticated(HttpSession session) {
		String username = username(session);
		return username != null && !username.isBlank();
	}
}