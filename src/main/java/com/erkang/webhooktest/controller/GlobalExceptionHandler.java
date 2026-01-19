package com.erkang.webhooktest.controller;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("error", ex.getMessage());
		body.put("timestamp", Instant.now());
		return ResponseEntity.badRequest().body(body);
	}

	@ExceptionHandler(JsonProcessingException.class)
	public ResponseEntity<Map<String, Object>> handleJsonProcessing(JsonProcessingException ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("error", "Invalid request body: " + ex.getOriginalMessage());
		body.put("timestamp", Instant.now());
		return ResponseEntity.badRequest().body(body);
	}
}
