package com.example.tool.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CreativeBriefServiceTests {

	@Test
	void buildsBriefWithNormalizedSearchPhrase() {
		CreativeBriefService service = new CreativeBriefService();
		String topic = "\u0110\u1ed3 \u0103n \u0111\u01b0\u1eddng ph\u1ed1!";

		String brief = service.buildBrief(topic, 4);

		assertThat(brief).contains("Topic: " + topic);
		assertThat(brief).contains("Search phrase: do an duong pho");
		assertThat(brief).contains("4 bien the");
	}
}
