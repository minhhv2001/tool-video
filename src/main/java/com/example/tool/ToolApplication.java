package com.example.tool;

import com.example.tool.config.AuthProperties;
import com.example.tool.config.MediaToolProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableConfigurationProperties({ MediaToolProperties.class, AuthProperties.class })
public class ToolApplication {

	private static final Logger LOGGER = LogManager.getLogger(ToolApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ToolApplication.class, args);
		LOGGER.info("Video Highlight Cutter khởi động thành công.");
		LOGGER.info("Giao diện đã sẵn sàng tại cổng:8080");
	}
}
