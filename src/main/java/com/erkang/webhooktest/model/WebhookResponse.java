package com.erkang.webhooktest.model;

import java.time.Instant;

public record WebhookResponse(int statusCode, String message, Instant receivedAt) {
}
