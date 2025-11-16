package de.brianp.controller;

import com.google.api.services.calendar.model.Event;
import de.brianp.service.GoogleCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final GoogleCalendarService googleCalendarService;

    @Autowired
    public CalendarController(GoogleCalendarService googleCalendarService) {
        this.googleCalendarService = googleCalendarService;
    }

    /**
     * Get calendar events for a date range
     */
    @GetMapping("/events")
    public ResponseEntity<?> getEvents(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (!googleCalendarService.isConfigured()) {
            return ResponseEntity.badRequest().body("Google Calendar is not configured");
        }

        try {
            List<Event> events = googleCalendarService.getEvents(startDate, endDate);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to retrieve calendar events: " + e.getMessage());
        }
    }

    /**
     * Check if Google Calendar is configured
     */
    @GetMapping("/status")
    public ResponseEntity<?> getCalendarStatus() {
        return ResponseEntity.ok(new CalendarStatus(googleCalendarService.isConfigured()));
    }

    /**
     * Simple status response class
     */
    public static class CalendarStatus {
        private final boolean configured;

        public CalendarStatus(boolean configured) {
            this.configured = configured;
        }

        public boolean isConfigured() {
            return configured;
        }
    }
}
