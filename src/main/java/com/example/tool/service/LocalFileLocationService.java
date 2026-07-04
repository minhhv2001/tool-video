package com.example.tool.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class LocalFileLocationService {

	private static final Logger LOGGER = LogManager.getLogger(LocalFileLocationService.class);

	public Path open(Path target) {
		Path normalized = target.toAbsolutePath().normalize();
		if (!Files.exists(normalized)) {
			throw new IllegalArgumentException("Không tìm thấy đường dẫn trên ổ cứng: " + normalized);
		}
		try {
			startOpenCommand(normalized);
			LOGGER.info("Đã mở vị trí file/thư mục trên máy local: {}", normalized);
			return normalized;
		}
		catch (IOException ex) {
			throw new IllegalStateException("Không mở được thư mục trên máy. Đường dẫn: " + normalized, ex);
		}
	}

	private void startOpenCommand(Path target) throws IOException {
		String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
		if (os.contains("win")) {
			if (Files.isDirectory(target)) {
				new ProcessBuilder("explorer.exe", target.toString()).start();
			}
			else {
				new ProcessBuilder("explorer.exe", "/select," + target).start();
			}
			return;
		}
		if (os.contains("mac")) {
			if (Files.isDirectory(target)) {
				new ProcessBuilder("open", target.toString()).start();
			}
			else {
				new ProcessBuilder("open", "-R", target.toString()).start();
			}
			return;
		}
		Path folder = Files.isDirectory(target) ? target : target.getParent();
		new ProcessBuilder("xdg-open", folder.toString()).start();
	}
}
