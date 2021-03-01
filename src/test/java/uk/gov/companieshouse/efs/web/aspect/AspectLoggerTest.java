package uk.gov.companieshouse.efs.web.aspect;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.efs.web.formtemplates.model.FormTemplateModel;
import uk.gov.companieshouse.logging.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AspectLoggerTest {
    public static final String TEST_EXCEPTION_MESSAGE = "Test Exception";
    public static final String SUBMISSION_ID = "submissionId";
    @Mock
    Logger logger;

    @Mock
    JoinPoint joinPoint;

    @Mock
    FormTemplateModel mockFormTemplate;

    @Spy
    @InjectMocks
    AspectLogger testLogger;

    @Mock
    CodeSignature sig;

    @Captor
    ArgumentCaptor<Map<String, Object>> debugMapCaptor;

    void setUpJoinPointArgs(Map<String, Object> args) {
        when(joinPoint.getSignature()).thenReturn(sig);

        when(sig.getParameterNames()).thenReturn(args.keySet().toArray(new String[0]));
        when(joinPoint.getArgs()).thenReturn(args.values().toArray(new Object[0]));
    }

    @Test
    void testLogExceptions() {
        setUpJoinPointArgs(new HashMap<>());

        testLogger.onExceptionHandler(joinPoint, new RuntimeException(TEST_EXCEPTION_MESSAGE));

        verify(logger).errorContext(eq(""), eq(TEST_EXCEPTION_MESSAGE),
                any(RuntimeException.class), any());
    }

    @Test
    void testFormSelected() {
        Map<String, Object> args = new HashMap<>();
        args.put("formTemplateAttribute", mockFormTemplate);
        args.put("id", SUBMISSION_ID);
        setUpJoinPointArgs(args);

        testLogger.formSelected(joinPoint);

        verify(logger).infoContext(eq(SUBMISSION_ID), eq("Form selected"),
                debugMapCaptor.capture());

        Map<String, Object> debugMap = debugMapCaptor.getValue();
        assertThat(debugMap, hasKey("formSelected"));
    }

    @Test
    void testSubmissionCompletedLog() {
        Map<String, Object> args = new HashMap<>();
        args.put("formTemplateAttribute", mockFormTemplate);
        args.put("id", SUBMISSION_ID);
        setUpJoinPointArgs(args);

        when(mockFormTemplate.getFormName()).thenReturn("");

        testLogger.submissionCompleted(joinPoint);

        verify(logger).infoContext(eq(SUBMISSION_ID), eq("Submission completed"),
                debugMapCaptor.capture());

        Map<String, Object> debugMap = debugMapCaptor.getValue();
        assertThat(debugMap, hasKey("formName"));
    }
}
