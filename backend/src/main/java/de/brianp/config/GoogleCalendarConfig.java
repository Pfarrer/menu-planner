package de.brianp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "google.calendar")
@Data
public class GoogleCalendarConfig {
    
    private String calendarId = "primary";
}
