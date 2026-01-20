package com.erkang.webhooktest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.erkang.webhooktest.service.WebhookService;

@SpringBootTest
@AutoConfigureMockMvc
class WebhookControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private WebhookService webhookService;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void clearHistory() {
		webhookService.clearHistory();
	}

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
	void choosesRandomStatusFromList() throws Exception {
		Set<Integer> options = Set.of(201, 202, 204);
		MvcResult result = mockMvc.perform(post("/webhook/test")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"payload":{"statusCode":[201,202,204],"message":"List"}}
								"""))
				.andReturn();

		JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
		int chosen = root.get("statusCode").asInt();
		assertTrue(options.contains(chosen), "Returned statusCode should be from provided list");
		assertEquals(result.getResponse().getStatus(), chosen, "HTTP status matches chosen status code");
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
	void handlesNestedStatusCode() throws Exception {
		mockMvc.perform(post("/webhook/test")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"payload":{"statusCode":"405","message":"Nested"}}
								"""))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(jsonPath("$.statusCode").value(405))
				.andExpect(jsonPath("$.message").value("Nested"));
	}

	@Test
	void returnsHistoryWithRequestsAndResponses() throws Exception {
		mockMvc.perform(post("/webhook/test")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"statusCode":201,"message":"first"}
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/webhook/test")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"payload":{"statusCode":202,"message":"second"}}
								"""))
				.andExpect(status().isAccepted());

		mockMvc.perform(get("/webhook/history"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].response.statusCode").value(201))
				.andExpect(jsonPath("$[0].requestedMessage").value("first"))
				.andExpect(jsonPath("$[1].response.statusCode").value(202))
				.andExpect(jsonPath("$[1].requestedMessage").value("second"));
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
