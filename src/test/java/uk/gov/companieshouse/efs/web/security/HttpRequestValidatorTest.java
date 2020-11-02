package uk.gov.companieshouse.efs.web.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpRequestValidatorTest {
    private HttpRequestValidator testValidator;

    @Mock
    Validator nextValidator;

    @Mock
    HttpServletRequest request;

    ValidatorResourceProvider resourceProvider;

    @BeforeEach
    void setUp() {
        resourceProvider = new ValidatorResourceProvider(request, null, null);
    }

    @ParameterizedTest
    @MethodSource("provideValidConditions")
    void passingValidator(String method, String path) {
        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURI()).thenReturn(path);
        when(nextValidator.validate()).thenReturn(true);


        testValidator = new HttpRequestValidator(resourceProvider);
        testValidator.setNext(nextValidator);

        testValidator.validate();

        // validate must be true to call the next validator in the chain
        verify(nextValidator).validate();
    }

    private static Stream<Arguments> provideValidConditions() {
        return Stream.of(
                Arguments.of("GET", "/efs-submission/0123456789/company/11223344"),
                Arguments.of("GET", "/efs-submission/8374g58974g5/company/hgdfoiusbdfg"),
                Arguments.of("gEt", "/efs-submission/8374g58974g5/company/hgdfoiusbdfg"));
    }

    @ParameterizedTest
    @MethodSource("provideInValidConditions")
    void failingValidator(String method, String path) {
        when(request.getMethod()).thenReturn(method);
        if (method.equalsIgnoreCase("GET")) {
            when(request.getRequestURI()).thenReturn(path);
        }


        testValidator = new HttpRequestValidator(resourceProvider);
        testValidator.setNext(nextValidator);

        boolean needsAuth = testValidator.validate();
        assertFalse(needsAuth);

        // validate must be true to call the next validator in the chain
        verifyNoMoreInteractions(nextValidator);
    }

    private static Stream<Arguments> provideInValidConditions() {
        return Stream.of(
                Arguments.of("POST", "/efs-submission/0123456789/company/11223344"),
                Arguments.of("GET", "/efs-submission/0123456789/company"),
                Arguments.of("GET", " /efs-submission/0123456789/company/11223344"));
    }

    @Test
    void nullInput() {
        testValidator = new HttpRequestValidator(new ValidatorResourceProvider(null, null, null));

        boolean needsAuth = testValidator.validate();
        assertFalse(needsAuth);

        // validate must be true to call the next validator in the chain
        verifyNoMoreInteractions(nextValidator);
    }


    @Test
    void nullResourceProvider() {
        testValidator = new HttpRequestValidator(null);
        boolean needsAuth = testValidator.validate();
        assertFalse(needsAuth);

        verifyNoMoreInteractions(nextValidator);
    }
}