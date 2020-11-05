package uk.gov.companieshouse.efs.web.security.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionFormApi;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.session.Session;
import uk.gov.companieshouse.session.handler.SessionHandler;
import uk.gov.companieshouse.session.model.SignInInfo;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidatorResourceProviderTest {

    private static final String EFS_SUBMISSION_WITH_COMPANY = "/efs-submission/{0}/company/{1}";
    public static final String FORM_TYPE = "FORM_TYPE";

    @Mock
    HttpServletRequest request;

    @Mock
    SubmissionApi submission;

    @Mock
    ApiClientService apiClientService;

    @Mock
    ApiResponse<SubmissionApi> submissionResponse;

    @Mock
    FormTemplateService formTemplateService;

    @Mock
    FormTemplateApi formTemplate;

    @Mock
    SubmissionFormApi submissionForm;

    @Mock
    ApiResponse<FormTemplateApi> formTemplateServiceResponse;

    @Mock
    SignInInfo signInInfo;

    @Mock
    Session session;

    ValidatorResourceProvider testResourceProvider;


    @BeforeEach
    void setUp() {
        testResourceProvider = spy(new ValidatorResourceProvider(apiClientService, formTemplateService));
        testResourceProvider.setInput(request);
    }

    @Test
    void usesExistingSessionIfExists() {
        ReflectionTestUtils.setField(testResourceProvider,
                "submission", submission);

        testResourceProvider.getSubmission();
        verify(testResourceProvider, never()).getRequestPathMatcher();
    }

    @Test
    void getsSubmissionWhenNoExistingSubmission() {
        String submissionId = "5f8422b326e7b618e25684da";
        String companyNumber = "12345678";
        String url = MessageFormat.format(EFS_SUBMISSION_WITH_COMPANY,
                submissionId, companyNumber);
        when(request.getRequestURI()).thenReturn(url);

        when(apiClientService.getSubmission(submissionId)).thenReturn(submissionResponse);
        when(submissionResponse.getData()).thenReturn(submission);

        testResourceProvider.getSubmission();
        verify(testResourceProvider).getRequestPathMatcher();
        verify(apiClientService).getSubmission(submissionId);

        SubmissionApi submission = (SubmissionApi) ReflectionTestUtils.getField(
                testResourceProvider, "submission");
        assertTrue(Objects.nonNull(submission));
    }

    @Test
    void getSubmissionWhenNoExistingSubmissionButUrlInvalid() {
        when(request.getRequestURI()).thenReturn("");


        testResourceProvider.getSubmission();
        verify(testResourceProvider).getRequestPathMatcher();
        verify(apiClientService, never()).getSubmission(anyString());
    }

    @Test
    void getRequest() {
        testResourceProvider =
                spy(new ValidatorResourceProvider(null, null));
        testResourceProvider.setInput(request);

        HttpServletRequest gotRequest = testResourceProvider.getInput();
        assertThat(request, sameInstance(gotRequest));
    }

    @Test
    void getFormWhenNoExistingForm() {
        ReflectionTestUtils.setField(testResourceProvider, "submission", submission);

        when(submission.getSubmissionForm()).thenReturn(submissionForm);
        when(submissionForm.getFormType()).thenReturn(FORM_TYPE);
        when(formTemplateService.getFormTemplate(FORM_TYPE)).thenReturn(formTemplateServiceResponse);
        when(formTemplateServiceResponse.getData()).thenReturn(formTemplate);

        testResourceProvider.getForm();

        verify(testResourceProvider).getSubmission();

        // Ensure form is cached for subsequent calls
        FormTemplateApi cachedForm = (FormTemplateApi) ReflectionTestUtils.getField(
                testResourceProvider, "form");
        assertTrue(Objects.nonNull(cachedForm));
    }

    @Test
    void getFormWhenExistingForm() {
        ReflectionTestUtils.setField(testResourceProvider, "form", formTemplate);

        Optional<FormTemplateApi> form = testResourceProvider.getForm();
        assertTrue(form.isPresent());
        assertThat(form.get(), sameInstance(formTemplate));

        verify(testResourceProvider, never()).getSubmission();
        verify(formTemplateService, never()).getFormTemplate(FORM_TYPE);
    }

    @Test
    void getFormWhenSubmissionNotPresent() {
        // Matcher doesn't find and getSubmission returns empty
        when(request.getRequestURI()).thenReturn("");

        Optional<FormTemplateApi> form = testResourceProvider.getForm();
        assertFalse(form.isPresent());
        verify(formTemplateService, never()).getFormTemplate(null);
    }


    @Test
    void getSignInInfoWhenExistingSignInInfo() {
        ReflectionTestUtils.setField(testResourceProvider, "signInInfo", signInInfo);

        testResourceProvider.getSignInInfo();

        verify(request, never()).getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY);
    }

    @Test
    void getSignInInfoWhenNoExistingSignInInfo() {
        when(request.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY))
                .thenReturn(session);
        when(session.getSignInInfo()).thenReturn(signInInfo);

        Optional<SignInInfo> maybeSignInInfo = testResourceProvider.getSignInInfo();
        assertTrue(maybeSignInInfo.isPresent());
        assertThat(maybeSignInInfo.get(), sameInstance(signInInfo));
        verify(session).getSignInInfo();


        // Ensure value is cached
        SignInInfo cachedSignInInfo = (SignInInfo) ReflectionTestUtils.getField(
                testResourceProvider, "signInInfo");
        assertTrue(Objects.nonNull(cachedSignInInfo));
    }

    @Test
    void getSignInInfoWhenSessionNull() {
        Optional<SignInInfo> maybeSignInInfo = testResourceProvider.getSignInInfo();
        assertFalse(maybeSignInInfo.isPresent());
        verify(session, never()).getSignInInfo();
        verifyNoMoreInteractions(session);
    }

    @Test
    void getCompanyNumberValidUrl() {
        String companyNumber = "12345678";
        String validUrl = MessageFormat.format(EFS_SUBMISSION_WITH_COMPANY,
                "5f8422b326e7b618e25684da", companyNumber);
        when(request.getRequestURI()).thenReturn(validUrl);

        Optional<String> maybeCompanyNumber = testResourceProvider.getCompanyNumber();

        assertTrue(maybeCompanyNumber.isPresent());
        assertEquals(maybeCompanyNumber.get(), companyNumber);
    }

    @Test
    void getCompanyNoUrlMatch() {
        String invalidUrl = "";
        when(request.getRequestURI()).thenReturn(invalidUrl);

        Optional<String> maybeCompanyNumber = testResourceProvider.getCompanyNumber();
        assertFalse(maybeCompanyNumber.isPresent());

    }

    @Test
    void getSession() {
        when(request.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY)).thenReturn(session);

        Optional<Session> gotSession = testResourceProvider.getChsSession();
        assertTrue(gotSession.isPresent());
        assertThat(gotSession.get(), sameInstance(session));
    }

    @Test
    void getSessionWhenSessionNull() {
        when(request.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY)).thenReturn(null);

        Optional<Session> gotSession = testResourceProvider.getChsSession();
        assertFalse(gotSession.isPresent());
    }

    @Test
    void setInputWhenInputIsNull() {
        ReflectionTestUtils.setField(testResourceProvider, "signInInfo", new SignInInfo());
        testResourceProvider.setInput(null);

        SignInInfo signInInfo = (SignInInfo) ReflectionTestUtils.getField(testResourceProvider,
                "signInInfo");
        assertThat(signInInfo, is(nullValue()));
    }
}