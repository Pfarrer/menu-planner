package de.brianp.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2Service {

    private static final String APPLICATION_NAME = "Menu Planner";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/calendar");

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private final OAuth2AuthorizedClientService authorizedClientService;

    public Calendar getCalendarService() throws IOException, GeneralSecurityException {
        Credential credential = createCredential();
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        return new Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY,
                credential).setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential createCredential() throws IOException, GeneralSecurityException {
        GoogleClientSecrets.Details web = new GoogleClientSecrets.Details().setClientId(clientId)
                .setClientSecret(clientSecret);

        GoogleClientSecrets clientSecrets = new GoogleClientSecrets().setWeb(web);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(new MemoryDataStoreFactory()).setAccessType("offline").build();

        // Create credential with the tokens
        Credential credential = new Credential(flow.getMethod());
        credential.setAccessToken(accessToken());

        return credential;
    }

    public boolean isUserAuthenticated(OAuth2AuthenticationToken authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return false;
        }

        try {
            OAuth2AuthorizedClient authorizedClient = authorizedClientService
                    .loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(), authentication.getName());
            return authorizedClient != null && authorizedClient.getAccessToken() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public String accessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient("google",
                authentication.getName());
        if (authorizedClient == null) {
            // log an error, it can be helpful during debugging
            return null;
        }
        // here we're actually using the OAuth2 client that we configured before
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        return accessToken.getTokenValue();
    }
}
