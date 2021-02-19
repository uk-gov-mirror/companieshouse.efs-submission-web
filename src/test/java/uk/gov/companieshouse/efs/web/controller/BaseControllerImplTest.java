package uk.gov.companieshouse.efs.web.controller;

import org.codehaus.plexus.util.cli.Arg;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.SessionStatus;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.web.categorytemplates.model.CategoryTemplateModel;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.formtemplates.model.FormTemplateModel;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.efs.web.service.session.SessionService;
import uk.gov.companieshouse.efs.web.transfer.FileTransferApiClient;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.session.Session;
import uk.gov.companieshouse.session.SessionImpl;
import uk.gov.companieshouse.session.handler.SessionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public abstract class BaseControllerImplTest {

    protected static final String CHS_URL = "http://web.chs-dev:4000";
    protected static final String SUBMISSION_ID = "aaaaaaaaaaaaaaaaaaaaaaaa";
    protected static final String FILE_ID = "1234567890";
    protected static final String CONFIRMATION_REF = "m6mo orcu mwgs c5pw";
    protected static final String COMPANY_NUMBER = "11111111";
    protected static final String COMPANY_NAME = "TEST COMPANY LTD";
    protected static final String USER_EMAIL = "tester@email.com";
    protected static final String SESSION_ID = "sess12345678";
    protected static final Instant FIXED_NOW = Instant.parse("2020-03-15T09:44:08.108Z");
    protected static final String NOT_FOUND_PAGE = ViewConstants.MISSING.asView();
    protected static final String SERVICE_PROBLEM_PAGE = ViewConstants.ERROR.asView();
    protected static final String TEMPLATE_NAME = "templateName";
    protected static final String ORIGINAL_SUBMISSION_ID = "originalSubmissionId";

    private static final String USER_EMAIL2 = "tester2@email.com";
    protected static final String SUBMISSION_ID2 = "bbbbbbbbbbbbbbbbbbbbbbbb";

    protected static String chsSessionId = SESSION_ID;

    @Mock
    protected Logger logger;
    @Mock
    protected HttpServletRequest request;
    @Mock
    protected HttpSession session;
    @Mock
    protected BindingResult bindingResult;
    @Mock
    protected SessionStatus sessionStatus;
    @Mock
    protected Model model;
    @Mock
    protected HttpServletRequest servletRequest;
    @Mock
    protected HttpSession httpSession;
    @Mock
    protected FormTemplateModel formTemplateAttribute;
    @Mock
    protected CategoryTemplateModel categoryTemplateAttribute;
    @Mock
    protected CategoryTemplateService categoryTemplateService;
    @Mock
    protected ApiClientService apiClientService;
    @Mock
    protected SessionService sessionService;
    @Mock
    protected FormTemplateService formTemplateService;
    @Mock
    protected FileTransferApiClient fileTransferApiClient;
    @Mock
    private ApiResponse<String> apiResponse;

    protected MockMvc mockMvc;

    protected Map<String, Object> headers;

    @InjectMocks
    private BaseControllerTestClass baseController;

    @Mock
    PresenterApi presenter;

    @Mock
    private SubmissionApi submission;

    protected void setUp() {
        headers = new HashMap<>();
    }

    protected Map<String, Object> getHeaders() {
        return headers;
    }

    protected void stubGetChsSession() {
        Session session = new SessionImpl();
        session.setCookieId(chsSessionId);
        when(servletRequest.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY)).thenReturn(session);
    }

    protected String getUrlWithId(final String template, final String id) {
        return MessageFormat.format(template, id);
    }

    protected FieldError buildFieldError(final String object, final String field, final String code,
                                         final String rejectedValue, final String defaultMessage, final Object... args) {
        return new FieldError(object, field, rejectedValue, false, new String[]{code}, args, defaultMessage);
    }

    protected ApiResponse<Void> putOkResponse() {
        return new ApiResponse<>(HttpStatus.OK.value(), headers);
    }

    protected ApiResponse<SubmissionApi> getSubmissionOkResponse(final SubmissionApi submission) {
        return new ApiResponse<>(HttpStatus.OK.value(), headers, submission);
    }

    protected SubmissionApi createSubmission(final SubmissionStatus submitted) {
        final SubmissionApi submission = new SubmissionApi();

        submission.setId(SUBMISSION_ID);
        submission.setStatus(submitted);

        CompanyApi company = new CompanyApi();
        company.setCompanyNumber(COMPANY_NAME);
        company.setCompanyName(COMPANY_NUMBER);

        submission.setCompany(company);

        return submission;
    }

    @ParameterizedTest
    @MethodSource("verifySubmissionTestCases")
    void verifySubmissionLogsOnFailure(String reqSubmissionID, String sessionSubmissionID,
                                       String requestUserEmail, String sessionUserEmail,
                                       boolean isSameForm, boolean isSameUser) {

        setupForVerifySubmission(reqSubmissionID, sessionSubmissionID,
                requestUserEmail, sessionUserEmail);


        boolean resp = baseController.verifySubmission(submission);
        if (isSameForm && isSameUser) {
            assertTrue(resp);
        } else {
            String failedLogMessage = "Verify submission failed.";
            verify(logger).errorContext(eq(reqSubmissionID), contains(failedLogMessage),
                    isNull(), anyMap());

            if (!isSameUser) {
                String emailNotMatchedLogMessage = "Session user email does not match request user email.";
                verify(logger).errorContext(any(), contains(emailNotMatchedLogMessage),
                        isNull(), anyMap());
            }

            if (!isSameForm) {
                String sessionNotMatchedLogMessage = "Session submissionID doesn't match request submissionID.";
                verify(logger).errorContext(any(), contains(sessionNotMatchedLogMessage),
                        isNull(), anyMap());
            }
        }
    }

    private void setupForVerifySubmission(String reqSubmissionID, String sessionSubmissionID,
                                          String requestUserEmail, String sessionUserEmail) {

        when(submission.getId()).thenReturn(reqSubmissionID);

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("originalSubmissionId", sessionSubmissionID);
        when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);

        when(submission.getPresenter()).thenReturn(presenter);
        when(presenter.getEmail()).thenReturn(requestUserEmail);
        when(sessionService.getUserEmail()).thenReturn(sessionUserEmail);
    }

    private static Stream<Arguments> verifySubmissionTestCases() {
        return Stream.of(
                Arguments.of(SUBMISSION_ID, SUBMISSION_ID, USER_EMAIL, USER_EMAIL, true, true),
                Arguments.of(SUBMISSION_ID, SUBMISSION_ID2, USER_EMAIL, USER_EMAIL, false, true),
                Arguments.of(SUBMISSION_ID, SUBMISSION_ID, USER_EMAIL, USER_EMAIL2, true, false),
                Arguments.of(SUBMISSION_ID, SUBMISSION_ID2, USER_EMAIL, USER_EMAIL2, false, false)
        );
    }

    // Needed to instantiate a class for testing
    private static class BaseControllerTestClass extends BaseControllerImpl {
    }

    @Test
    void testGetViewName() {
        String viewName = baseController.getViewName();
        assertNull("Base controllers view should be null", viewName);
    }

    @ParameterizedTest
    @MethodSource("errorStatusCodes")
    void testLogOnApiResponse(HttpStatus status) {
        setUpApiResponse(status);

        baseController.logApiResponse(apiResponse, SUBMISSION_ID, "");

        verify(logger).errorContext(eq(SUBMISSION_ID), contains("API response"), isNull(), isNull());
    }

    private static Stream<Arguments> errorStatusCodes() {
        return Arrays.stream(HttpStatus.values())
                .filter(HttpStatus::isError)
                .map(Arguments::of);
    }

    private void setUpApiResponse(HttpStatus status, List<ApiError> errors) {
        when(apiResponse.getStatusCode()).thenReturn(status.value());
    }

    private void setUpApiResponse(HttpStatus status) {
        setUpApiResponse(status, Collections.emptyList());
    }

    private void setUpApiResponse(List<ApiError> errors) {
        setUpApiResponse(HttpStatus.OK, errors);
        if (!errors.isEmpty()) {
            when(apiResponse.hasErrors()).thenReturn(true);
            when(apiResponse.getErrors()).thenReturn(errors);
        } else {
            when(apiResponse.hasErrors()).thenReturn(false);
        }
    }

    @ParameterizedTest
    @MethodSource("apiErrors")
    void testLogApiResponseErrors(List<ApiError> apiErrors) {
        setUpApiResponse(apiErrors);

        baseController.logApiResponse(apiResponse, SUBMISSION_ID, "");

        verify(logger, times(apiErrors.size()))
                .errorContext(eq(SUBMISSION_ID), contains("error="), isNull(), isNull());
    }

    private static Stream<Arguments> apiErrors() {
        return Stream.of(
                Arguments.of(Collections.emptyList()),
                Arguments.of(Collections.singletonList(new ApiError())),
                Arguments.of(Arrays.asList(
                        new ApiError(),
                        new ApiError()
                ))
        );
    }

    @Test
    void testGetChsSessionId() {
        Session session = mock(Session.class);
        when(request.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY))
                .thenReturn(session);
        baseController.getChsSessionId(request);
        verify(session).getCookieId();

        when(request.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY))
                .thenReturn(null);
        assertNull(baseController.getChsSessionId(request));
    }

    @Test
    void testAddAnyErrorsFromResponse() {
        String fieldName = "testAnyErrorsFromResponse";
        String location = String.format("a.b.%s", fieldName);

        BindingResult bindingResult = mock(BindingResult.class);
        ApiError apiError = mock(ApiError.class);

        when(apiResponse.getErrors()).thenReturn(Collections.singletonList(apiError));
        when(apiError.getLocation()).thenReturn(location);
        when(apiError.getErrorValues()).thenReturn(null);

        baseController.addAnyErrorsFromResponse(bindingResult, apiResponse, x -> true);

        verify(bindingResult).rejectValue(eq(fieldName), isNull(), isNull(), anyString());
    }
}
