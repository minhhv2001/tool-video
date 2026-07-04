package com.example.tool.service;

import com.example.tool.config.MediaToolProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Component;

@Component
public class MediaWorkspace {

	private final Path workspace;

	public MediaWorkspace(MediaToolProperties properties) {
		this.workspace = Path.of(properties.getWorkspace()).toAbsolutePath().normalize();
	}

	public Path workspace() {
		return workspace;
	}

	public Path sourcesDirectory() {
		return workspace.resolve("sources");
	}

	public Path outputsDirectory() {
		return workspace.resolve("outputs");
	}

	public Path jobsDirectory() {
		return workspace.resolve("jobs");
	}

	public void ensureBaseDirectories() {
		try {
			Files.createDirectories(sourcesDirectory());
			Files.createDirectories(outputsDirectory());
			Files.createDirectories(jobsDirectory());
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not create media workspace: " + workspace, ex);
		}
	}

	public Path resolveUserPath(String value, Path defaultDirectory) {
		if (value == null || value.isBlank()) {
			return defaultDirectory.toAbsolutePath().normalize();
		}
		Path raw = Path.of(value);
		if (raw.isAbsolute()) {
			return raw.normalize();
		}
		return workspace.resolve(value).toAbsolutePath().normalize();
	}
}
