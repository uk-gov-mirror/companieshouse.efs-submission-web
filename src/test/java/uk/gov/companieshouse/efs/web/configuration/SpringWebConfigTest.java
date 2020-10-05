package uk.gov.companieshouse.efs.web.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.isA;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class SpringWebConfigTest {

    @Spy
    private SpringWebConfig testConfig = new SpringWebConfig();

    @Mock
    private Logger logger;

    @Mock
    private EnvironmentReader environmentReader;

    @Test
    void environmentReader() {
        assertThat(testConfig.environmentReader(), isA(EnvironmentReader.class));
    }

}
