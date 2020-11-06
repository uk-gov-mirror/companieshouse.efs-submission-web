package uk.gov.companieshouse.efs.web.security.validator;

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
    private HttpRequestRequiredValidator testValidator;

    @Mock
    Validator<HttpServletRequest> nextValidator;

    @Mock
    HttpServletRequest request;

    ValidatorResourceProvider resourceProvider;
    public static final String SUBMISSION_ID = "5f8422b326e7b618e25684da";
    public static final String COMPANY_NUMBER = "12345678";

    @BeforeEach
    void setUp() {
        resourceProvider = new ValidatorResourceProvider(null, null);
    }

    @ParameterizedTest
    @MethodSource("provideValidConditions")
    void passingValidator(String method, String path) {
        expectMethod(method);
        when(request.getRequestURI()).thenReturn(path);
        when(nextValidator.validate(request)).thenReturn(true);


        testValidator = new HttpRequestRequiredValidator(resourceProvider);
        testValidator.setNext(nextValidator);

        testValidator.validate(request);

        // validate must be true to call the next validator in the chain
        verify(nextValidator).validate(request);
    }

    private static Stream<Arguments> provideValidConditions() {
        return Stream.of(
                createConditions("GET", SUBMISSION_ID, COMPANY_NUMBER),
                createConditions("gEt", SUBMISSION_ID, COMPANY_NUMBER));
    }

    @ParameterizedTest
    @MethodSource(value = "provideInValidConditions")
    void failingValidator(String method, String path) {
        expectMethod(method);
        if (method.equalsIgnoreCase("GET")) {
            when(request.getRequestURI()).thenReturn(path);
        }


        testValidator = new HttpRequestRequiredValidator(resourceProvider);
        testValidator.setNext(nextValidator);

        boolean needsAuth = testValidator.validate(request);
        assertFalse(needsAuth);

        // validate must be true to call the next validator in the chain
        verifyNoMoreInteractions(nextValidator);
    }

    private static Stream<Arguments> provideInValidConditions() {
        return Stream.of(
                createConditions("POST",SUBMISSION_ID, COMPANY_NUMBER),
                createConditions("GET", SUBMISSION_ID, ""),
                createConditions("GET", String.format(" /efs-submission/%s/company/%s", SUBMISSION_ID, COMPANY_NUMBER)),
                createConditions("GET", SUBMISSION_ID.replace('5', 'x'), COMPANY_NUMBER),
                createConditions("GET", SUBMISSION_ID + "0", COMPANY_NUMBER),
                createConditions("GET", SUBMISSION_ID, COMPANY_NUMBER.substring(1)),
                createConditions("GET", SUBMISSION_ID, COMPANY_NUMBER.replace("1", "-")));
    }

    @Test
    void nullInput() {
        testValidator = new HttpRequestRequiredValidator(new ValidatorResourceProvider(null, null));

        boolean needsAuth = testValidator.validate(null);
        assertFalse(needsAuth);

        // validate must be true to call the next validator in the chain
        verifyNoMoreInteractions(nextValidator);
    }


    @Test
    void nullResourceProvider() {
        testValidator = new HttpRequestRequiredValidator(null);
        boolean needsAuth = testValidator.validate(request);
        assertFalse(needsAuth);

        verifyNoMoreInteractions(nextValidator);
    }

    private void expectMethod(String method) {
        when(request.getMethod()).thenReturn(method);
    }

    private static Arguments createConditions(String method, String url) {
        return Arguments.of(
                method,
                url);
    }

    private static Arguments createConditions(String method, String submissionId, String companyNumber) {
        return createConditions(
            method,
            String.format("/efs-submission/%s/company/%s", submissionId, companyNumber));
    }
}