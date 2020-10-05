package uk.gov.companieshouse.efs.web.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.isA;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.RequestLogger;

@ExtendWith(MockitoExtension.class)
public class LoggingConfigTest {
    private LoggingConfig testConfig;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        testConfig = new LoggingConfig();
    }

    @Test
    void structuredLoggerBean() {
        assertThat(testConfig.logger(), isA(Logger.class));
    }

    @Test
    void loggingInterceptorBean() {
        assertThat(testConfig.loggingInterceptor(logger), isA(RequestLogger.class));
    }
}
