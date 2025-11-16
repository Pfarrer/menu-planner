package de.brianp.service;

import com.google.api.services.calendar.model.Event;
import de.brianp.domain.MenuPlan;
import de.brianp.domain.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuCalendarService {

    private final GoogleCalendarService googleCalendarService;

    @Autowired
    public MenuCalendarService(GoogleCalendarService googleCalendarService) {
        this.googleCalendarService = googleCalendarService;
    }

    /**
     * Sync a menu plan to Google Calendar
     */
    public MenuCalendarSyncResult syncMenuToCalendar(MenuPlan menuPlan) {
        if (!googleCalendarService.isConfigured()) {
            return MenuCalendarSyncResult.notConfigured();
        }

        try {
            // Get existing events for the menu plan date range
            LocalDate startDate = getStartDateFromMenuPlan(menuPlan);
            LocalDate endDate = getEndDateFromMenuPlan(menuPlan);

            List<Event> existingEvents = googleCalendarService.getEvents(startDate, endDate);

            // Create new events for menu items
            List<Event> createdEvents = menuPlan.getMenuItems().stream().filter(item -> item.getRecipe() != null)
                    .map(googleCalendarService::createMenuEvent).collect(Collectors.toList());

            return MenuCalendarSyncResult.success(createdEvents.size(), existingEvents.size());

        } catch (Exception e) {
            return MenuCalendarSyncResult.error(e.getMessage());
        }
    }

    /**
     * Get calendar events that might conflict with menu planning
     */
    public List<Event> getConflictingEvents(LocalDate startDate, LocalDate endDate) {
        if (!googleCalendarService.isConfigured()) {
            return List.of();
        }

        return googleCalendarService.getEvents(startDate, endDate);
    }

    /**
     * Extract start date from menu plan
     */
    private LocalDate getStartDateFromMenuPlan(MenuPlan menuPlan) {
        return menuPlan.getMenuItems().stream().map(item -> LocalDate.parse(item.getDay())).min(LocalDate::compareTo)
                .orElse(LocalDate.now());
    }

    /**
     * Extract end date from menu plan
     */
    private LocalDate getEndDateFromMenuPlan(MenuPlan menuPlan) {
        return menuPlan.getMenuItems().stream().map(item -> LocalDate.parse(item.getDay())).max(LocalDate::compareTo)
                .orElse(LocalDate.now());
    }

    /**
     * Result class for calendar sync operations
     */
    public static class MenuCalendarSyncResult {
        private final boolean success;
        private final String message;
        private final int eventsCreated;
        private final int existingEvents;

        private MenuCalendarSyncResult(boolean success, String message, int eventsCreated, int existingEvents) {
            this.success = success;
            this.message = message;
            this.eventsCreated = eventsCreated;
            this.existingEvents = existingEvents;
        }

        public static MenuCalendarSyncResult success(int eventsCreated, int existingEvents) {
            return new MenuCalendarSyncResult(true,
                    String.format("Successfully synced %d events to calendar (found %d existing events)", eventsCreated,
                            existingEvents),
                    eventsCreated, existingEvents);
        }

        public static MenuCalendarSyncResult notConfigured() {
            return new MenuCalendarSyncResult(false, "Google Calendar is not configured", 0, 0);
        }

        public static MenuCalendarSyncResult error(String errorMessage) {
            return new MenuCalendarSyncResult(false, "Failed to sync to calendar: " + errorMessage, 0, 0);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public int getEventsCreated() {
            return eventsCreated;
        }

        public int getExistingEvents() {
            return existingEvents;
        }
    }
}
