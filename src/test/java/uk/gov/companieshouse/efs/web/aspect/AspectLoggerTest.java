package uk.gov.companieshouse.efs.web.aspect;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AspectLoggerTest {
    public static final String TEST_EXCEPTION_MESSAGE = "Test Exception";
    @Mock
    Logger logger;

    @Mock
    JoinPoint joinPoint;

    @Spy
    @InjectMocks
    AspectLogger testLogger;

    @Mock
    CodeSignature sig;

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
}
