package de.brianp.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import de.brianp.domain.MenuItem;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    @Value("${google.calendar.calendar-id}")
    private String calendarId;

    private final GoogleOAuth2Service oAuth2Service;

    /**
     * Creates an authorized Calendar client using OAuth2 credentials.
     */
    private Calendar getCalendar(OAuth2AuthenticationToken authentication)
            throws IOException, GeneralSecurityException {
        return oAuth2Service.getCalendarService();
    }

    /**
     * Get events from Google Calendar for a specific date range
     */
    public List<Event> getEvents(LocalDate startDate, LocalDate endDate, String calendarId, OAuth2AuthenticationToken authentication) {
        try {
            Calendar calendar = getCalendar(authentication);

            ZonedDateTime startZdt = startDate.atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime endZdt = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault());

            // Use provided calendarId or fall back to default
            String targetCalendarId = (calendarId != null && !calendarId.isEmpty()) ? calendarId : this.calendarId;

            Events events = calendar.events().list(targetCalendarId)
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
    public Event createMenuEvent(MenuItem menuItem, OAuth2AuthenticationToken authentication) {
        return createMenuEvent(menuItem, this.calendarId, authentication);
    }

    /**
     * Create a calendar event for a menu item in a specific calendar
     */
    public Event createMenuEvent(MenuItem menuItem, String calendarId, OAuth2AuthenticationToken authentication) {
        try {
            Calendar calendar = getCalendar(authentication);

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

            // Use provided calendarId or fall back to default
            String targetCalendarId = (calendarId != null && !calendarId.isEmpty()) ? calendarId : this.calendarId;

            return calendar.events().insert(targetCalendarId, event).execute();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to create calendar event", e);
        }
    }

    /**
     * Create multiple calendar events for menu items
     */
    public void createMenuEvents(List<MenuItem> menuItems, OAuth2AuthenticationToken authentication) {
        for (MenuItem menuItem : menuItems) {
            if (menuItem.getRecipe() != null) {
                createMenuEvent(menuItem, authentication);
            }
        }
    }

    /**
     * Delete a calendar event
     */
    public void deleteEvent(String eventId, OAuth2AuthenticationToken authentication) {
        deleteEvent(eventId, this.calendarId, authentication);
    }

    /**
     * Delete a calendar event from a specific calendar
     */
    public void deleteEvent(String eventId, String calendarId, OAuth2AuthenticationToken authentication) {
        try {
            Calendar calendar = getCalendar(authentication);

            // Use provided calendarId or fall back to default
            String targetCalendarId = (calendarId != null && !calendarId.isEmpty()) ? calendarId : this.calendarId;

            calendar.events().delete(targetCalendarId, eventId).execute();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to delete calendar event", e);
        }
    }

    /**
     * Get all calendars the user has access to
     */
    public List<CalendarListEntry> getAllCalendars(OAuth2AuthenticationToken authentication) {
        try {
            Calendar calendar = getCalendar(authentication);
            CalendarList calendarList = calendar.calendarList().list().execute();
            return calendarList.getItems();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to retrieve calendar list", e);
        }
    }

    /**
     * Check if Google Calendar integration is configured
     */
    public boolean isConfigured() {
        return true; // OAuth2 is always configured if user is authenticated
    }
}
