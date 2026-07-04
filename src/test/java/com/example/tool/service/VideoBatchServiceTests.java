package com.example.tool.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.tool.model.BatchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class VideoBatchServiceTests {

	@Test
	void failsClearlyWhenFfmpegIsMissing() throws Exception {
		Path temp = Files.createTempDirectory("video-factory-test");
		MediaWorkspace workspace = mock(MediaWorkspace.class);
		FfmpegService ffmpeg = mock(FfmpegService.class);
		when(workspace.sourcesDirectory()).thenReturn(temp.resolve("sources"));
		when(workspace.outputsDirectory()).thenReturn(temp.resolve("outputs"));
		when(workspace.workspace()).thenReturn(temp);
		when(ffmpeg.ffmpegAvailable()).thenReturn(false);
		when(ffmpeg.ffprobeAvailable()).thenReturn(false);

		VideoBatchService service = new VideoBatchService(
				workspace,
				ffmpeg,
				new CreativeBriefService(),
				new ObjectMapper());

		BatchRequest request = new BatchRequest(
				"topic",
				"sources",
				"outputs",
				null,
				1,
				1,
				1,
				2,
				1080,
				1920,
				true,
				false,
				1L);

		assertThatThrownBy(() -> service.createBatch(request))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("ffmpeg/ffprobe is not available");
	}
}
