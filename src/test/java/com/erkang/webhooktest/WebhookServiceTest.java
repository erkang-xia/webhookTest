package com.erkang.webhooktest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.Clock;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.erkang.webhooktest.model.WebhookHistoryEntry;
import com.erkang.webhooktest.model.WebhookRequest;
import com.erkang.webhooktest.model.WebhookResponse;
import com.erkang.webhooktest.service.WebhookService;

class WebhookServiceTest {

	@Test
	void prunesHistoryOlderThanSevenDays() {
		MutableClock clock = new MutableClock(Instant.parse("2025-01-01T00:00:00Z"), ZoneId.of("UTC"));
		WebhookService service = new WebhookService(clock);

		service.recordHistory("{\"statusCode\":200}", new WebhookRequest(200, "old"),
				new WebhookResponse(200, "old", Instant.now(clock)));

		clock.advance(Duration.ofDays(8));

		service.recordHistory("{\"statusCode\":201}", new WebhookRequest(201, "newer"),
				new WebhookResponse(201, "newer", Instant.now(clock)));

		List<WebhookHistoryEntry> history = service.history();
		assertEquals(1, history.size());
		assertEquals(201, history.get(0).requestedStatusCode());
		assertEquals("newer", history.get(0).requestedMessage());
	}

	private static class MutableClock extends Clock {
		private Instant instant;
		private final ZoneId zone;

		MutableClock(Instant instant, ZoneId zone) {
			this.instant = instant;
			this.zone = zone;
		}

		@Override
		public ZoneId getZone() {
			return zone;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return new MutableClock(instant, zone);
		}

		@Override
		public Instant instant() {
			return instant;
		}

		void advance(Duration duration) {
			instant = instant.plus(duration);
		}
	}
}
