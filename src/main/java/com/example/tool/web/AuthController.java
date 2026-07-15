package com.example.tool.web;

import com.example.tool.config.AuthProperties;
import javax.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthProperties properties;

	public AuthController(AuthProperties properties) {
		this.properties = properties;
	}

	@GetMapping("/me")
	public AuthUser me(HttpSession session) {
		if (!properties.isEnabled()) {
			return new AuthUser(true, "local", "Local", true);
		}
		return new AuthUser(AuthSession.authenticated(session), AuthSession.username(session), AuthSession.displayName(session), AuthSession.admin(session));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthUser> login(@RequestBody LoginRequest request, HttpSession session) {
		if (!properties.isEnabled()) {
			session.setAttribute(AuthSession.USERNAME, "local");
			session.setAttribute(AuthSession.DISPLAY_NAME, "Local");
			session.setAttribute(AuthSession.IS_ADMIN, true);
			return ResponseEntity.ok(new AuthUser(true, "local", "Local", true));
		}
		AuthProperties.User user = properties.authenticate(request == null ? null : request.getUsername(), request == null ? null : request.getPassword());
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthUser(false, null, null, false));
		}
		session.setAttribute(AuthSession.USERNAME, user.getUsername());
		session.setAttribute(AuthSession.DISPLAY_NAME, user.getDisplayName());
		session.setAttribute(AuthSession.IS_ADMIN, user.isAdmin());
		return ResponseEntity.ok(new AuthUser(true, user.getUsername(), user.getDisplayName(), user.isAdmin()));
	}

	@PostMapping("/logout")
	public AuthUser logout(HttpSession session) {
		session.invalidate();
		return new AuthUser(false, null, null, false);
	}

	public static class LoginRequest {
		private String username;
		private String password;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}

	public static class AuthUser {
		private final boolean authenticated;
		private final String username;
		private final String displayName;
		private final boolean admin;

		public AuthUser(boolean authenticated, String username, String displayName, boolean admin) {
			this.authenticated = authenticated;
			this.username = username;
			this.displayName = displayName;
			this.admin = admin;
		}

		public boolean isAuthenticated() {
			return authenticated;
		}

		public String getUsername() {
			return username;
		}

		public String getDisplayName() {
			return displayName;
		}

		public boolean isAdmin() {
			return admin;
		}
	}
}