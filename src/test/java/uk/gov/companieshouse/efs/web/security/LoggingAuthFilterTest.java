package uk.gov.companieshouse.efs.web.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.session.Session;
import uk.gov.companieshouse.session.model.SignInInfo;
import uk.gov.companieshouse.session.model.UserProfile;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingAuthFilterTest {
    LoggingAuthFilter testFilter;

    @Mock
    Session session;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    EnvironmentReader environmentReader;

    @Mock
    SignInInfo signInInfo;

    @Mock
    UserProfile userProfile;

    @Captor
    ArgumentCaptor<String> stringArgumentCaptor;

    final static String USER_EMAIL = "tester@test.com";


    static final String signOutRedirectPath = "https://chs.local/efs-submission/start";
    static final String originalRequestUrl = "http://chs.local/efs-submission/start";
    static final String randomEncryptionKey = "3T3L6iAEFscijkJZnOK0bYu/pH9jZeJqC1j59ZROKu8=";

    @BeforeEach
    void setUp() {
        when(environmentReader.getMandatoryString(anyString())).thenReturn("");
        when(environmentReader.getMandatoryString("OAUTH2_REQUEST_KEY"))
                .thenReturn(randomEncryptionKey);
        testFilter = new LoggingAuthFilter(environmentReader, signOutRedirectPath);
    }

    void setupUserProfile() {
        when(session.getSignInInfo()).thenReturn(signInInfo);
        when(signInInfo.getUserProfile()).thenReturn(userProfile);
        when(userProfile.getEmail()).thenReturn(USER_EMAIL);
    }

    @Test
    void testRedirectForAuth() throws IOException {
        setupRequest();
        setupUserProfile();

        testFilter.redirectForAuth(session, request, response, "11111111", true);

        verify(response).sendRedirect(contains("scope="));
    }

    private void setupRequest() {
        when(request.getRequestURL()).thenReturn(
                new StringBuffer(LoggingAuthFilterTest.originalRequestUrl));
    }
}