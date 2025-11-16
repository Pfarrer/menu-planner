package de.brianp.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import de.brianp.domain.MenuItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "Menu Planner";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    @Value("${google.calendar.api-key:}")
    private String apiKey;

    @Value("${google.calendar.calendar-id:primary}")
    private String calendarId;

    /**
     * Creates an authorized Calendar client.
     */
    private Calendar getCalendar() throws IOException, GeneralSecurityException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Calendar.Builder(httpTransport, JSON_FACTORY, null).setApplicationName(APPLICATION_NAME).build();
    }

    /**
     * Get events from Google Calendar for a specific date range
     */
    public List<Event> getEvents(LocalDate startDate, LocalDate endDate) {
        try {
            Calendar calendar = getCalendar();

            ZonedDateTime startZdt = startDate.atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime endZdt = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault());

            Events events = calendar.events().list(calendarId)
                    .setTimeMin(new DateTime(startZdt.toInstant().toEpochMilli()))
                    .setTimeMax(new DateTime(endZdt.toInstant().toEpochMilli())).setSingleEvents(true)
                    .setOrderBy("startTime").execute();

            return events.getItems();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to retrieve calendar events", e);
        }
    }

    /**
     * Create a calendar event for a menu item
     */
    public Event createMenuEvent(MenuItem menuItem) {
        try {
            Calendar calendar = getCalendar();

            Event event = new Event()
                    .setSummary("Meal: "
                            + (menuItem.getRecipe() != null ? menuItem.getRecipe().getName() : "No recipe assigned"))
                    .setDescription("Menu planning: " + menuItem.getDay() + " " + menuItem.getMealType());

            // Parse the day and create date time
            LocalDate mealDate = LocalDate.parse(menuItem.getDay());
            ZonedDateTime startDateTime = mealDate.atTime(18, 0) // Default to 6 PM
                    .atZone(ZoneId.systemDefault());
            ZonedDateTime endDateTime = mealDate.atTime(19, 0) // Default to 7 PM
                    .atZone(ZoneId.systemDefault());

            EventDateTime start = new EventDateTime()
                    .setDateTime(new DateTime(startDateTime.toInstant().toEpochMilli()))
                    .setTimeZone(ZoneId.systemDefault().getId());

            EventDateTime end = new EventDateTime().setDateTime(new DateTime(endDateTime.toInstant().toEpochMilli()))
                    .setTimeZone(ZoneId.systemDefault().getId());

            event.setStart(start);
            event.setEnd(end);

            return calendar.events().insert(calendarId, event).execute();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to create calendar event", e);
        }
    }

    /**
     * Create multiple calendar events for menu items
     */
    public void createMenuEvents(List<MenuItem> menuItems) {
        for (MenuItem menuItem : menuItems) {
            if (menuItem.getRecipe() != null) {
                createMenuEvent(menuItem);
            }
        }
    }

    /**
     * Delete a calendar event
     */
    public void deleteEvent(String eventId) {
        try {
            Calendar calendar = getCalendar();
            calendar.events().delete(calendarId, eventId).execute();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to delete calendar event", e);
        }
    }

    /**
     * Check if Google Calendar integration is configured
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
}
