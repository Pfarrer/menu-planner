package de.brianp.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/datastar")
public class DatastarController {

    /**
     * SSE endpoint for real-time menu plan updates
     */
    @GetMapping(value = "/menu-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMenuUpdates() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        try {
            // Send initial signals
            sendSseEvent(emitter, "datastar-merge-signals", "{\"loading\": false, \"menuCount\": 0}");
            
            // Simulate real-time updates
            CompletableFuture.runAsync(() -> {
                try {
                    for (int i = 0; i < 5; i++) {
                        Thread.sleep(2000);
                        
                        Map<String, Object> signals = new HashMap<>();
                        signals.put("menuCount", i + 1);
                        signals.put("lastUpdate", LocalDate.now().toString());
                        
                        sendSseEvent(emitter, "datastar-merge-signals", signals.toString());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
        
        return emitter;
    }

    /**
     * Get current menu plan with datastar formatting
     */
    @GetMapping("/menu/current")
    public void getCurrentMenuPlan(org.springframework.web.context.request.WebRequest request, 
                                 jakarta.servlet.http.HttpServletResponse response) throws IOException {
        response.setContentType("text/event-stream");
        
        // Mock menu plan data
        String menuHtml = generateMenuPlanHtml();
        
        sendSseEvent(response, "datastar-merge-fragments", 
            "{\"fragments\":[{\"selector\":\"#menu-display\",\"html\":\"" + escapeJson(menuHtml) + "\"}]}");
    }

    /**
     * Generate new menu plan with datastar formatting
     */
    @PostMapping("/menu/plan")
    public void generateMenuPlan(@RequestBody(required = false) Map<String, Object> request, 
                               jakarta.servlet.http.HttpServletResponse response) throws IOException {
        response.setContentType("text/event-stream");
        
        // Send loading state
        sendSseEvent(response, "datastar-merge-signals", "{\"loading\": true}");
        
        // Simulate processing time
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Send completed menu plan
        String menuHtml = generateMenuPlanHtml();
        
        sendSseEvent(response, "datastar-merge-fragments", 
            "{\"fragments\":[{\"selector\":\"#menu-display\",\"html\":\"" + escapeJson(menuHtml) + "\"}]}");
        
        // Update loading state
        sendSseEvent(response, "datastar-merge-signals", "{\"loading\": false}");
    }

    /**
     * Get calendar events with datastar formatting
     */
    @GetMapping("/calendar/events")
    public void getCalendarEvents(@RequestParam(required = false) String calendarId, 
                                jakarta.servlet.http.HttpServletResponse response) throws IOException {
        response.setContentType("text/event-stream");
        
        // Send loading state
        sendSseEvent(response, "datastar-merge-signals", "{\"loading\": true, \"error\": null}");
        
        try {
            // Mock calendar events data
            String eventsHtml = generateCalendarEventsHtml(calendarId);
            
            sendSseEvent(response, "datastar-merge-fragments", 
                "{\"fragments\":[{\"selector\":\"#content\",\"html\":\"" + escapeJson(eventsHtml) + "\"}]}");
            
            // Update signals with current month/year
            Map<String, Object> monthSignals = new HashMap<>();
            monthSignals.put("currentMonth", LocalDate.now().getMonth().toString());
            monthSignals.put("currentYear", LocalDate.now().getYear());
            
            sendSseEvent(response, "datastar-merge-signals", monthSignals.toString());
            
            // Clear loading state
            sendSseEvent(response, "datastar-merge-signals", "{\"loading\": false}");
            
        } catch (Exception e) {
            // Send error state
            sendSseEvent(response, "datastar-merge-signals", 
                "{\"loading\": false, \"error\": \"Failed to load calendar events: " + e.getMessage() + "\"}");
        }
    }

    private void sendSseEvent(SseEmitter emitter, String event, String data) {
        try {
            emitter.send(SseEmitter.event()
                .name(event)
                .data(data));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    private void sendSseEvent(jakarta.servlet.http.HttpServletResponse response, String event, String data) throws IOException {
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

    private String generateMenuPlanHtml() {
        return """
            <div style="background: white; padding: 20px; border-radius: 8px; margin-top: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                <h3 style="color: #333; margin-bottom: 15px;">🍽️ Generated Menu Plan</h3>
                <div style="display: grid; gap: 10px;">
                    <div style="padding: 10px; background: #f8f9fa; border-left: 4px solid #4285f4; border-radius: 4px;">
                        <strong>Monday:</strong> Spaghetti Carbonara
                    </div>
                    <div style="padding: 10px; background: #f8f9fa; border-left: 4px solid #34a853; border-radius: 4px;">
                        <strong>Tuesday:</strong> Grilled Chicken Salad
                    </div>
                    <div style="padding: 10px; background: #f8f9fa; border-left: 4px solid #fbbc04; border-radius: 4px;">
                        <strong>Wednesday:</strong> Vegetable Stir Fry
                    </div>
                    <div style="padding: 10px; background: #f8f9fa; border-left: 4px solid #ea4335; border-radius: 4px;">
                        <strong>Thursday:</strong> Beef Tacos
                    </div>
                    <div style="padding: 10px; background: #f8f9fa; border-left: 4px solid #9333ea; border-radius: 4px;">
                        <strong>Friday:</strong> Homemade Pizza
                    </div>
                </div>
                <p style="margin-top: 15px; color: #666; font-size: 14px;">
                    Generated on %s with OptaPlanner optimization
                </p>
            </div>
            """.formatted(LocalDate.now());
    }

    private String generateCalendarEventsHtml(String calendarId) {
        return """
            <div data-show="!loading && !error && events.length === 0">
                <h3>No events found</h3>
                <p>You don't have any calendar events for the selected period.</p>
            </div>
            <div data-show="!loading && !error && events.length > 0">
                <div class="event-card">
                    <div class="event-title">Team Meeting</div>
                    <div class="event-time">📅 Mon, Nov 25 ⏰ 10:00 AM - 11:00 AM</div>
                    <div class="event-description">Weekly team sync and planning session</div>
                </div>
                <div class="event-card">
                    <div class="event-title">Lunch with Client</div>
                    <div class="event-time">📅 Tue, Nov 26 ⏰ 12:30 PM - 2:00 PM</div>
                    <div class="event-time">📍 Downtown Restaurant</div>
                </div>
                <div class="event-card">
                    <div class="event-title">Project Deadline</div>
                    <div class="event-time">📅 Wed, Nov 27 (All day)</div>
                    <div class="event-description">Final submission for Q4 project</div>
                </div>
            </div>
            """;
    }
}