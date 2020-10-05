package uk.gov.companieshouse.efs.web.service.api.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.http.OAuthHttpClient;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.session.Session;
import uk.gov.companieshouse.session.SessionImpl;
import uk.gov.companieshouse.session.handler.SessionHandler;
import uk.gov.companieshouse.session.store.Store;

@ExtendWith(MockitoExtension.class)
class ApiClientServiceImplIT {
    private static final String API_HOST_URL = "test://host:port";

    private ApiClientService testService;

    @Mock
    private Logger logger;
    @Mock
    private HttpServletRequest request;
    @Mock
    private Store store;

    private Session session;

    @BeforeEach
    void setUp() {
        testService = new ApiClientServiceImpl(logger);
    }

    @Test
    void getApiClient() {
        session = new SessionImpl(store, "", new HashMap<>());
        final ServletRequestAttributes attributes = new ServletRequestAttributes(request);

        RequestContextHolder.setRequestAttributes(attributes);
        createSessionDataSignedInWithOAuth();

        ApiClient client = testService.getApiClient();

        assertOAuthClientAsExpected(client.getHttpClient());
    }
    private void createSessionDataSignedInWithOAuth() {
        Map<String, Object> accessTokenData = new HashMap<>();
        accessTokenData.put("access_token", "access_token_key");
        accessTokenData.put("refresh_token", "refresh_token_key");

        Map<String, Object> signInData = new HashMap<>();
        signInData.put("company_number", "05448736");
        signInData.put("access_token", accessTokenData);
        signInData.put("signed_in", 1);

        addDataToSession(signInData);
    }

    private void addDataToSession(Map<String, Object> signInData) {
        Map<String, Object> sessionData = session.getData();
        sessionData.put("signin_info", signInData);

        when(request.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY)).thenReturn(session);
    }

    private void assertOAuthClientAsExpected(HttpClient httpClient) {
        assertThat(httpClient, is(instanceOf(OAuthHttpClient.class)));

        OAuthHttpClient oAuthHttpClient = (OAuthHttpClient) httpClient;
    }

}
