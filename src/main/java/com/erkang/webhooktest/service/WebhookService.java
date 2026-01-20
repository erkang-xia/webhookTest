package com.erkang.webhooktest.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.erkang.webhooktest.model.WebhookHistoryEntry;
import com.erkang.webhooktest.model.WebhookRequest;
import com.erkang.webhooktest.model.WebhookResponse;

@Service
public class WebhookService {

	private static final Duration HISTORY_TTL = Duration.ofDays(7);

	private final List<WebhookHistoryEntry> history = Collections.synchronizedList(new ArrayList<>());
	private final Clock clock;

	public WebhookService(Clock clock) {
		this.clock = clock;
	}

	public WebhookResponse process(WebhookRequest request) {
		int statusCode = request.resolvedStatusCode();
		validateStatusCode(statusCode);

		return new WebhookResponse(statusCode, request.resolvedMessage(), Instant.now(clock));
	}

	public void recordHistory(String rawBody, WebhookRequest request, WebhookResponse response) {
		Instant now = Instant.now(clock);
		pruneOldHistory(now);
		history.add(new WebhookHistoryEntry(rawBody, request.statusCode(), request.message(), response, now));
	}

	public List<WebhookHistoryEntry> history() {
		synchronized (history) {
			pruneOldHistory(Instant.now(clock));
			return List.copyOf(history);
		}
	}

	public void clearHistory() {
		history.clear();
	}

	private void pruneOldHistory(Instant now) {
		Instant cutoff = now.minus(HISTORY_TTL);
		synchronized (history) {
			for (Iterator<WebhookHistoryEntry> it = history.iterator(); it.hasNext();) {
				WebhookHistoryEntry entry = it.next();
				if (entry.recordedAt().isBefore(cutoff)) {
					it.remove();
				}
			}
		}
	}

	private void validateStatusCode(int statusCode) {
		if (statusCode < 100 || statusCode > 599) {
			throw new IllegalArgumentException("Status code must be between 100 and 599.");
		}
	}
}
