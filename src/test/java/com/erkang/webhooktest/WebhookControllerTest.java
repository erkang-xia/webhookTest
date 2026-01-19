package com.erkang.webhooktest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class WebhookControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void returnsDefaultOkWhenStatusNotProvided() throws Exception {
		mockMvc.perform(post("/webhook/test")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.statusCode").value(200));
	}

	@Test
	void returnsCustomStatusWhenProvided() throws Exception {
		mockMvc.perform(post("/webhook/test")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"statusCode":201,"message":"Created by webhook"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.statusCode").value(201))
				.andExpect(jsonPath("$.message").value("Created by webhook"));
	}

	@Test
	void acceptsTextPlainJsonBody() throws Exception {
		mockMvc.perform(post("/webhook/test")
						.contentType(MediaType.TEXT_PLAIN)
						.content("""
								{"statusCode":202,"message":"Queued for processing"}
								"""))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.statusCode").value(202))
				.andExpect(jsonPath("$.message").value("Queued for processing"));
	}

	@Test
	void rejectsInvalidStatusCode() throws Exception {
		mockMvc.perform(post("/webhook/test")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"statusCode":99}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Status code must be between 100 and 599."));
	}
}
