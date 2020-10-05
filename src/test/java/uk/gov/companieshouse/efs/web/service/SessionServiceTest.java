package uk.gov.companieshouse.efs.web.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.web.service.session.SessionService;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {

    @Mock
    private SessionService sessionService;

    @Test
    void testSessionService() {
        // Arrange:
        final Map<String, Object> sessionMap = new HashMap<>();
        sessionMap.put("key", "my-session-key");

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionMap);

        // Act:
        final Map<String, Object> sessionDataFromContext = sessionService.getSessionDataFromContext();

        // Assert:
        assertThat(sessionDataFromContext.size(), is(1));
        assertThat(sessionDataFromContext.get("key"), is("my-session-key"));
    }
}
