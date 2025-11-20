package de.brianp.controller;

import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import de.brianp.service.GoogleCalendarService;
import de.brianp.service.GoogleOAuth2Service;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final GoogleCalendarService googleCalendarService;
    private final GoogleOAuth2Service oAuth2Service;

    /**
     * Get calendar events for a date range
     */
    @GetMapping("/events")
    public ResponseEntity<?> getEvents(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal OAuth2AuthenticationToken authentication) {

        if (!oAuth2Service.isUserAuthenticated(authentication)) {
            return ResponseEntity.status(401).body("User not authenticated with Google");
        }

        try {
            List<Event> events = googleCalendarService.getEvents(startDate, endDate, authentication);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to retrieve calendar events: " + e.getMessage());
        }
    }

    /**
     * Get calendar events for current month
     */
    @GetMapping("/events/current-month")
    public ResponseEntity<?> getCurrentMonthEvents(@AuthenticationPrincipal OAuth2User principal) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate startOfMonth = today.withDayOfMonth(1);
            LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

            List<Event> events = googleCalendarService.getEvents(startOfMonth, endOfMonth, null /* TODO */);
            return ResponseEntity.ok(new CurrentMonthResponse(today.getMonth().toString(), today.getYear(), events));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to retrieve calendar events: " + e.getMessage());
        }
    }

    /**
     * Get all calendars the user has access to
     */
    @GetMapping("/list")
    public ResponseEntity<?> getAllCalendars(@AuthenticationPrincipal OAuth2AuthenticationToken authentication) {
        try {
            List<CalendarListEntry> calendars = googleCalendarService.getAllCalendars(authentication);
            return ResponseEntity.ok(calendars);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to retrieve calendar list: " + e.getMessage());
        }
    }

    /**
     * Check if Google Calendar is configured
     */
    @GetMapping("/status")
    public ResponseEntity<?> getCalendarStatus(@AuthenticationPrincipal OAuth2AuthenticationToken authentication) {
        boolean isAuthenticated = oAuth2Service.isUserAuthenticated(authentication);
        return ResponseEntity.ok(new CalendarStatus(isAuthenticated));
    }

    /**
     * Simple status response class
     */
    @Data
    @AllArgsConstructor
    public static class CalendarStatus {
        private boolean configured;
    }

    /**
     * Response class for current month events
     */
    @Data
    @AllArgsConstructor
    public static class CurrentMonthResponse {
        private String month;
        private int year;
        private List<Event> events;
    }
}
