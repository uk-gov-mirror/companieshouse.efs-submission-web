package uk.gov.companieshouse.efs.web.service.api.impl;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.handler.efs.submissions.request.PrivateSubmissionGet;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class BaseApiClientServiceImplTest {
    private class TestBaseApiClientServiceImpl extends BaseApiClientServiceImpl {

        public TestBaseApiClientServiceImpl(final Logger logger) {
            super(logger);
        }
    }

    private TestBaseApiClientServiceImpl testService;

    @Mock
    private Logger logger;
    @Mock
    private PrivateSubmissionGet submissionGet;

    @BeforeEach
    void setUp() {
        testService = new TestBaseApiClientServiceImpl(logger);
    }

    @Test
    void executeOp() {
        final String uri = "/test-service/test-endpoint";

        testService.executeOp("testOperation", uri, submissionGet);

        verify(logger, Mockito.atLeastOnce()).debugContext(anyString(), anyString(),
                anyMap());
    }
}