package de.brianp.controller;

import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import de.brianp.service.GoogleCalendarService;
import de.brianp.service.GoogleOAuth2Service;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(required = false) String calendarId,
            @AuthenticationPrincipal OAuth2AuthenticationToken authentication) {

        if (!oAuth2Service.isUserAuthenticated(authentication)) {
            return ResponseEntity.status(401).body("User not authenticated with Google");
        }

        try {
            List<Event> events = googleCalendarService.getEvents(startDate, endDate, calendarId, authentication);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to retrieve calendar events: " + e.getMessage());
        }
    }

    /**
     * Get calendar events for current month (datastar compatible)
     */
    @GetMapping("/events/current-month")
    public void getCurrentMonthEvents(
            @RequestParam(required = false) String calendarId,
            HttpServletResponse response) throws IOException {

        response.setContentType("text/event-stream");

        // Send loading state
        sendSseEvent(response, "datastar-merge-signals", "{\"loading\": true, \"error\": null}");

        try {
            LocalDate today = LocalDate.now();
            LocalDate startOfMonth = today.withDayOfMonth(1);
            LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

            // Try to get real events, fallback to mock if authentication fails
            List<Event> events;
            try {
                events = googleCalendarService.getEvents(startOfMonth, endOfMonth, calendarId, null);
            } catch (Exception e) {
                // Use mock events if not authenticated
                events = getMockEvents();
            }

            // Generate HTML for events
            String eventsHtml = generateEventsHtml(events);

            sendSseEvent(response, "datastar-merge-fragments",
                "{\"fragments\":[{\"selector\":\"#content\",\"html\":\"" + escapeJson(eventsHtml) + "\"}]}");

            // Update signals with current month/year
            Map<String, Object> monthSignals = new HashMap<>();
            monthSignals.put("currentMonth", today.getMonth().toString());
            monthSignals.put("currentYear", today.getYear());

            sendSseEvent(response, "datastar-merge-signals", monthSignals.toString());

            // Clear loading state
            sendSseEvent(response, "datastar-merge-signals", "{\"loading\": false}");

        } catch (Exception e) {
            // Send error state
            sendSseEvent(response, "datastar-merge-signals",
                "{\"loading\": false, \"error\": \"Failed to load calendar events: " + e.getMessage() + "\"}");
        }
    }

    private void sendSseEvent(HttpServletResponse response, String event, String data) throws IOException {
        response.getWriter().write("event: " + event + "\n");
        response.getWriter().write("data: " + data + "\n\n");
        response.getWriter().flush();
    }

    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }

    private List<Event> getMockEvents() {
        // This would need to be implemented to return mock Event objects
        // For now, return empty list
        return List.of();
    }

    private String generateEventsHtml(List<Event> events) {
        StringBuilder html = new StringBuilder();

        if (events.isEmpty()) {
            html.append("""
                <div class="no-events">
                    <h3>No events found</h3>
                    <p>You don't have any calendar events for this month.</p>
                </div>
                """);
        } else {
            for (Event event : events) {
                String startTime = event.getStart().getDateTime() != null
                    ? event.getStart().getDateTime().toString()
                    : event.getStart().getDate().toString();

                html.append(String.format("""
                    <div class="event-card">
                        <div class="event-title">%s</div>
                        <div class="event-time">📅 %s</div>
                        %s
                        %s
                    </div>
                    """,
                    event.getSummary() != null ? event.getSummary() : "No title",
                    startTime,
                    event.getDescription() != null ? "<div class=\"event-description\">" + event.getDescription() + "</div>" : "",
                    event.getLocation() != null ? "<div class=\"event-time\">📍 " + event.getLocation() + "</div>" : ""
                ));
            }
        }

        return html.toString();
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
