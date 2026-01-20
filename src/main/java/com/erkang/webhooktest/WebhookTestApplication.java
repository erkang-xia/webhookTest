package com.erkang.webhooktest;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WebhookTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebhookTestApplication.class, args);
	}

	@Bean
	public Clock systemClock() {
		return Clock.systemUTC();
	}
}
