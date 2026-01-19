package com.erkang.webhooktest.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.erkang.webhooktest.model.WebhookRequest;
import com.erkang.webhooktest.model.WebhookResponse;

@Service
public class WebhookService {

	public WebhookResponse process(WebhookRequest request) {
		int statusCode = request.resolvedStatusCode();
		validateStatusCode(statusCode);

		return new WebhookResponse(statusCode, request.resolvedMessage(), Instant.now());
	}

	private void validateStatusCode(int statusCode) {
		if (statusCode < 100 || statusCode > 599) {
			throw new IllegalArgumentException("Status code must be between 100 and 599.");
		}
	}
}
