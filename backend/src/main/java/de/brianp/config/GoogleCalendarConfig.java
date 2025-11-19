package de.brianp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "google.calendar")
public record GoogleCalendarConfig(String calendarId) {
}
