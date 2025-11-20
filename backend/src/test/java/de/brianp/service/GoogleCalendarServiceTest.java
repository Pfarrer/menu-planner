package de.brianp.service;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleCalendarServiceTest {

    @Mock
    private GoogleOAuth2Service mockOAuth2Service;

    @Mock
    private Calendar mockCalendar;

    @Mock
    private Calendar.CalendarList mockCalendarList;

    @Mock
    private Calendar.CalendarList.List mockListRequest;

    @Mock
    private com.google.api.services.calendar.model.CalendarListEntry mockCalendarEntry;

    @Mock
    private OAuth2AuthenticationToken mockAuthentication;

    private GoogleCalendarService googleCalendarService;

    @BeforeEach
    void setUp() throws IOException, java.security.GeneralSecurityException {
        lenient().when(mockOAuth2Service.getCalendarService()).thenReturn(mockCalendar);
        lenient().when(mockCalendar.calendarList()).thenReturn(mockCalendarList);
        lenient().when(mockCalendarList.list()).thenReturn(mockListRequest);
        
        googleCalendarService = new GoogleCalendarService(mockOAuth2Service);
    }

    @Test
    void getAllCalendars_Success() throws IOException, java.security.GeneralSecurityException {
        // Arrange
        CalendarList calendarList = new CalendarList();
        calendarList.setItems(List.of(mockCalendarEntry));
        when(mockListRequest.execute()).thenReturn(calendarList);

        // Act
        List<com.google.api.services.calendar.model.CalendarListEntry> result = googleCalendarService.getAllCalendars(mockAuthentication);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mockOAuth2Service).getCalendarService();
        verify(mockCalendarList).list();
        verify(mockListRequest).execute();
    }

    @Test
    void getAllCalendars_EmptyList() throws IOException, java.security.GeneralSecurityException {
        // Arrange
        CalendarList calendarList = new CalendarList();
        calendarList.setItems(List.of());
        when(mockListRequest.execute()).thenReturn(calendarList);

        // Act
        List<com.google.api.services.calendar.model.CalendarListEntry> result = googleCalendarService.getAllCalendars(mockAuthentication);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllCalendars_ThrowsRuntimeException() throws IOException, java.security.GeneralSecurityException {
        // Arrange
        when(mockOAuth2Service.getCalendarService()).thenThrow(new IOException("API Error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> googleCalendarService.getAllCalendars(mockAuthentication));
    }
}