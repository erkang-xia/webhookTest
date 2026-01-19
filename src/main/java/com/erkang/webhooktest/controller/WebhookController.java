package com.erkang.webhooktest.controller;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erkang.webhooktest.model.WebhookRequest;
import com.erkang.webhooktest.model.WebhookResponse;
import com.erkang.webhooktest.service.WebhookService;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

	private final WebhookService webhookService;
	private final ObjectMapper objectMapper;

	public WebhookController(WebhookService webhookService, ObjectMapper objectMapper) {
		this.webhookService = webhookService;
		this.objectMapper = objectMapper;
	}

	@PostMapping(value = "/test", consumes = MediaType.ALL_VALUE)
	public ResponseEntity<WebhookResponse> respondWithStatus(@RequestBody(required = false) byte[] body) throws JsonProcessingException {
		WebhookRequest request = parseRequest(body);
		WebhookResponse response = webhookService.process(request);
		return ResponseEntity.status(response.statusCode()).body(response);
	}

	@GetMapping("/health")
	public ResponseEntity<Map<String, Object>> health() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", "ok");
		body.put("timestamp", Instant.now());
		return ResponseEntity.ok(body);
	}

	private WebhookRequest parseRequest(byte[] rawBody) throws JsonProcessingException {
		if (rawBody == null || rawBody.length == 0) {
			return new WebhookRequest(null, null);
		}
		String asString = new String(rawBody, StandardCharsets.UTF_8);
		return objectMapper.readValue(asString, WebhookRequest.class);
	}
}
