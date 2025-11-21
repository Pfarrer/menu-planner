package de.brianp.controller;

import de.brianp.service.GoogleCalendarService;
import de.brianp.service.GoogleOAuth2Service;
import com.google.api.services.calendar.model.CalendarListEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarControllerTest {

    @Mock
    private GoogleCalendarService mockGoogleCalendarService;

    @Mock
    private GoogleOAuth2Service mockOAuth2Service;

    @Mock
    private CalendarListEntry mockCalendarEntry;

    @Mock
    private OAuth2AuthenticationToken mockAuthentication;

    private CalendarController calendarController;

    @BeforeEach
    void setUp() {
        calendarController = new CalendarController(mockGoogleCalendarService, mockOAuth2Service);
    }

    @Test
    void getAllCalendars_Success() throws IOException {
        // Arrange
        List<CalendarListEntry> expectedCalendars = List.of(mockCalendarEntry);
        when(mockOAuth2Service.isUserAuthenticated(any(OAuth2AuthenticationToken.class))).thenReturn(true);
        when(mockGoogleCalendarService.getAllCalendars(any(OAuth2AuthenticationToken.class))).thenReturn(expectedCalendars);

        // Act
        ResponseEntity<?> response = calendarController.getAllCalendars(mockAuthentication);

        // Assert
        assertEquals(ResponseEntity.ok(expectedCalendars), response);
        verify(mockOAuth2Service).isUserAuthenticated(mockAuthentication);
        verify(mockGoogleCalendarService).getAllCalendars(mockAuthentication);
    }

    @Test
    void getAllCalendars_ThrowsRuntimeException() throws IOException {
        // Arrange
        when(mockOAuth2Service.isUserAuthenticated(any(OAuth2AuthenticationToken.class))).thenReturn(true);
        when(mockGoogleCalendarService.getAllCalendars(any(OAuth2AuthenticationToken.class)))
                .thenThrow(new RuntimeException("API Error"));

        // Act
        ResponseEntity<?> response = calendarController.getAllCalendars(mockAuthentication);

        // Assert
        assertEquals(500, response.getStatusCode().value());
        verify(mockOAuth2Service).isUserAuthenticated(mockAuthentication);
        verify(mockGoogleCalendarService).getAllCalendars(mockAuthentication);
    }
}