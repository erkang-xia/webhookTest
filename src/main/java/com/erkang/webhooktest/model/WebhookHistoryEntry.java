package com.erkang.webhooktest.model;

import java.time.Instant;

public record WebhookHistoryEntry(String requestBody, Integer requestedStatusCode, String requestedMessage,
		WebhookResponse response, Instant recordedAt) {
}
