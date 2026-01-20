package com.erkang.webhooktest.controller;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
		JsonNode root = objectMapper.readTree(asString);

		Integer statusCode = extractStatusCode(root);
		String message = extractTextField(root, "message");
		return new WebhookRequest(statusCode, message);
	}

	private Integer extractStatusCode(JsonNode root) {
		JsonNode node = findFirstField(root, "statusCode");
		if (node == null || node.isNull()) {
			return null;
		}
		if (node.isNumber()) {
			return node.asInt();
		}
		if (node.isTextual()) {
			try {
				return Integer.parseInt(node.asText());
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("Status code must be numeric.");
			}
		}
		throw new IllegalArgumentException("Status code must be numeric.");
	}

	private String extractTextField(JsonNode root, String fieldName) {
		JsonNode node = findFirstField(root, fieldName);
		if (node == null || node.isNull()) {
			return null;
		}
		return node.asText();
	}

	private JsonNode findFirstField(JsonNode node, String fieldName) {
		if (node == null) {
			return null;
		}
		if (node.has(fieldName)) {
			return node.get(fieldName);
		}
		if (node.isObject()) {
			for (JsonNode child : node) {
				JsonNode found = findFirstField(child, fieldName);
				if (found != null) {
					return found;
				}
			}
		}
		if (node.isArray()) {
			for (JsonNode child : node) {
				JsonNode found = findFirstField(child, fieldName);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}
}
