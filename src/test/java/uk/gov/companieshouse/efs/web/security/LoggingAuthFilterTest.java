package uk.gov.companieshouse.efs.web.security;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.gov.companieshouse.efs.web.exception.ServiceException;
import uk.gov.companieshouse.session.Session;
import uk.gov.companieshouse.session.model.SignInInfo;
import uk.gov.companieshouse.session.model.UserProfile;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;

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
    SignInInfo signInInfo;

    @Mock
    UserProfile userProfile;

    final static String USER_EMAIL = "tester@test.com";


    static final String signOutRedirectPath = "https://chs.local/efs-submission/start";
    static final String originalRequestUrl = "http://chs.local/efs-submission/start";
    static final String randomEncryptionKey = "3T3L6iAEFscijkJZnOK0bYu/pH9jZeJqC1j59ZROKu8=";

    @BeforeEach
    void setUp() {
        try {
            testFilter = createLoggingAuthFilter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    LoggingAuthFilter createLoggingAuthFilter() throws Exception {
        return withEnvironmentVariable("OAUTH2_REQUEST_KEY", randomEncryptionKey)
                .and("OAUTH2_AUTH_URI", "a")
                .and("OAUTH2_CLIENT_ID", "a")
                .and("OAUTH2_REDIRECT_URI", "a")
                .and("COOKIE_SECRET", "a")
                .and("USE_FINE_GRAIN_SCOPES_MODEL", "a")
                .execute(() -> new LoggingAuthFilter(signOutRedirectPath));
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

    @Test
    void testCreateAuthoriseURIInvalidUriSyntax() {
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            testFilter.createAuthoriseURI("^", "", "");
        });

        assertThat(exception.getCause(), isA(URISyntaxException.class));
    }

    @Test
    void equalsAndHash() {
        EqualsVerifier.forClass(LoggingAuthFilter.class)
                .withOnlyTheseFields("signoutRestartPath").verify();
    }


    private void setupRequest() {
        when(request.getRequestURL()).thenReturn(
                new StringBuffer(LoggingAuthFilterTest.originalRequestUrl));
    }
}