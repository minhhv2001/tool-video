package com.example.tool.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

	private boolean enabled = false;
	private List<User> users = new ArrayList<>();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users == null ? new ArrayList<>() : users;
	}

	public User authenticate(String username, String password) {
		String safeUsername = username == null ? "" : username.trim();
		String safePassword = password == null ? "" : password;
		return users.stream()
				.filter(user -> safeUsername.equals(user.getUsername()) && safePassword.equals(user.getPassword()))
				.findFirst()
				.orElse(null);
	}

	public static class User {
		private String username;
		private String password;
		private String displayName;
		private boolean admin;

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

		public String getDisplayName() {
			return displayName == null || displayName.isBlank() ? username : displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public boolean isAdmin() {
			return admin;
		}

		public void setAdmin(boolean admin) {
			this.admin = admin;
		}
	}
}