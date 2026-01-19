package com.erkang.webhooktest.model;

public record WebhookRequest(Integer statusCode, String message) {

	public int resolvedStatusCode() {
		return statusCode != null ? statusCode : 200;
	}

	public String resolvedMessage() {
		return message != null && !message.isBlank()
				? message
				: "Webhook processed with status " + resolvedStatusCode();
	}
}
