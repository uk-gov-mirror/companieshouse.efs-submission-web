package uk.gov.companieshouse.efs.web.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionFormApi;
import uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.session.Session;
import uk.gov.companieshouse.session.SessionImpl;
import uk.gov.companieshouse.session.SessionKeys;
import uk.gov.companieshouse.session.handler.SessionHandler;
import uk.gov.companieshouse.session.model.SignInInfo;
import uk.gov.companieshouse.session.model.UserProfile;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CompanyAuthFilterTest {

    private static final String OAUTH2_REQUEST_KEY = "pXf+qkU6P6SAoY2lKW0FtKMS4PylaNA3pY2sUQxNFDk=";
    private static final String OAUTH2_AUTH_URI = "http://testAuthURI";
    private static final String OAUTH2_CLIENT_ID = "dummyClientId";
    private static final String OAUTH2_REDIRECT_URI = "http://testRedirectURI";
    private static final String COOKIE_SECRET = "iamasecret";

    private static final String EFS_SUBMISSION_WITH_COMPANY = "/efs-submission/{0}/company/{1}";

    private static final String REQUEST_URL = "http://chs.companieshouse.gov.uk/";

    private static final String SUBMISSION_ID = "0123456789";

    private static final String COMPANY_NUMBER = "11223344";

    private static final String VALID_AUTH_SCOPE = "https://chs.companieshouse.gov.uk/company/" + COMPANY_NUMBER;

    public static final FormTemplateApi INSOLVENCY_WITH_AUTH_REQUIRED_FORM_TEMPLATE =
        new FormTemplateApi("REC01", null, "REC", null, true, false);

    public static final FormTemplateApi AUTH_REQUIRED_FORM_TEMPLATE =
        new FormTemplateApi("CC02", null, "CC", null, true, false);

    public static final FormTemplateApi AUTH_NOT_REQUIRED_FORM_TEMPLATE =
        new FormTemplateApi("CC01", null, "CC", null, false, false);
    private static final String TEST_EMAIL = "testing@test.com";

    private TestCompanyAuthFilter spyFilter;

    private static class TestCompanyAuthFilter extends CompanyAuthFilter {
        public TestCompanyAuthFilter(final EnvironmentReader environmentReader, final ApiClientService apiClientService,
            final FormTemplateService formTemplateService, final CategoryTemplateService categoryTemplateService) {
            super(environmentReader, apiClientService, formTemplateService, categoryTemplateService);
        }

        @Override
        public Session createSession() {
            return null;
        }
    }

    private TestCompanyAuthFilter testCompanyAuthFilter;

    @Mock
    private Session session;

    @Mock
    EnvironmentReader environmentReader;

    @Mock
    ApiClientService apiClientService;

    @Mock
    FormTemplateService formTemplateService;

    @Mock
    CategoryTemplateService categoryTemplateService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    private SignInInfo signInInfo;
    private UserProfile userProfile;

    @BeforeEach
    void setUp() {
        expectEnvironmentVariables();
        testCompanyAuthFilter = new TestCompanyAuthFilter(environmentReader, apiClientService, formTemplateService, categoryTemplateService);
        spyFilter = spy(testCompanyAuthFilter);
        userProfile = new UserProfile();
        signInInfo = new SignInInfo();
        signInInfo.setCompanyNumber(COMPANY_NUMBER);
        signInInfo.setUserProfile(userProfile);
    }

    private void expectEnvironmentVariables() {
        when(environmentReader.getMandatoryString("OAUTH2_REQUEST_KEY")).thenReturn(OAUTH2_REQUEST_KEY);
        when(environmentReader.getMandatoryString("OAUTH2_AUTH_URI")).thenReturn(OAUTH2_AUTH_URI);
        when(environmentReader.getMandatoryString("OAUTH2_CLIENT_ID")).thenReturn(OAUTH2_CLIENT_ID);
        when(environmentReader.getMandatoryString("OAUTH2_REDIRECT_URI")).thenReturn(OAUTH2_REDIRECT_URI);
        when(environmentReader.getMandatoryString("COOKIE_SECRET")).thenReturn(COOKIE_SECRET);
    }

    private void expectSession(final Session session) {
        when(request.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY)).thenReturn(session);
        if (session != null) {
            when(session.getSignInInfo()).thenReturn(signInInfo);
        }
    }

    @Test
    public void doFilterWhenUrlIsNotMatched() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("");
        when(request.getMethod()).thenReturn("GET");

        testCompanyAuthFilter.doFilter(request, response, chain);

        verifyCompanyAuthIsSkipped();
    }

    @Test
    public void doFilterWhenMethodIsNotGET() throws IOException, ServletException {
        when(request.getMethod()).thenReturn("POST");

        testCompanyAuthFilter.doFilter(request, response, chain);

        verifyCompanyAuthIsSkipped();
    }

    @Test
    public void doFilterWhenSubmissionFormApiIsNull() throws IOException, ServletException {
        SubmissionApi submission = createSubmission(null);

        ApiResponse<SubmissionApi> submissionApiResponse = new ApiResponse<>(HttpStatus.OK.value(),
                new HashMap<>(),
                submission);

        when(request.getRequestURI()).thenReturn(MessageFormat.format(EFS_SUBMISSION_WITH_COMPANY,
            SUBMISSION_ID, COMPANY_NUMBER));
        when(request.getMethod()).thenReturn("GET");
        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(submissionApiResponse);

        testCompanyAuthFilter.doFilter(request, response, chain);

        verifyCompanyAuthIsSkipped();
    }

    @Test
    public void doFilterWhenCHSessionIsNull() throws IOException, ServletException {
        SubmissionFormApi submissionForm = createSubmissionForm(AUTH_REQUIRED_FORM_TEMPLATE);
        SubmissionApi submission = createSubmission(submissionForm);


        expectSession(null);
        when(spyFilter.createSession()).thenReturn(session);
        when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL));

        expectCategoryAndFormLookup(submission, AUTH_REQUIRED_FORM_TEMPLATE);
        when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL));

        spyFilter.doFilter(request, response, chain);

        verifyCompanyAuthIsNotSkipped();
    }

    @Test
    public void doFilterWhenAuthIsRequiredAndEmailIsNull() throws IOException, ServletException {
        SubmissionFormApi submissionForm = createSubmissionForm(AUTH_REQUIRED_FORM_TEMPLATE);
        SubmissionApi submission = createSubmission(submissionForm);

        expectSession(session);
        expectCategoryAndFormLookup(submission, AUTH_REQUIRED_FORM_TEMPLATE);
        expectRequestUrlLookup();
        when(request.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY)).thenReturn(session);

        testCompanyAuthFilter.doFilter(request, response, chain);

        verifyCompanyAuthIsNotSkipped();
    }

    @Test
    public void doFilterWhenAuthIsNotRequired() throws IOException, ServletException {
        SubmissionFormApi submissionForm = createSubmissionForm(AUTH_NOT_REQUIRED_FORM_TEMPLATE);
        SubmissionApi submission = createSubmission(submissionForm);

        expectCategoryAndFormLookup(submission, AUTH_NOT_REQUIRED_FORM_TEMPLATE);

        testCompanyAuthFilter.doFilter(request, response, chain);

        verifyCompanyAuthIsSkipped();
    }

    @Test
    public void doFilterWhenAuthIsRequiredForInsolvencyAndUserIsOnAllowList() throws IOException, ServletException {
        SubmissionFormApi submissionForm = createSubmissionForm(INSOLVENCY_WITH_AUTH_REQUIRED_FORM_TEMPLATE);
        SubmissionApi submission = createSubmission(submissionForm);

        expectSession(session);
        userProfile.setEmail(TEST_EMAIL);
        when(session.getSignInInfo()).thenReturn(signInInfo);
        expectCategoryAndFormLookup(submission, INSOLVENCY_WITH_AUTH_REQUIRED_FORM_TEMPLATE);
        when(categoryTemplateService.getTopLevelCategory("REC")).thenReturn(CategoryTypeConstants.INSOLVENCY);
        when(request.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY)).thenReturn(session);
        when(apiClientService.isOnAllowList(TEST_EMAIL)).thenReturn(new ApiResponse<>(HttpStatus.OK.value(),
            Collections.emptyMap(), true));

        testCompanyAuthFilter.doFilter(request, response, chain);

        verifyCompanyAuthIsSkipped();
    }

    @Test
    public void doFilterWhenAuthIsRequiredForInsolvencyAndUserProfileIsNull() throws IOException, ServletException {
        SubmissionFormApi submissionForm = createSubmissionForm(INSOLVENCY_WITH_AUTH_REQUIRED_FORM_TEMPLATE);
        SubmissionApi submission = createSubmission(submissionForm);

        expectSession(session);
        signInInfo.setUserProfile(null);
        expectCategoryAndFormLookup(submission, INSOLVENCY_WITH_AUTH_REQUIRED_FORM_TEMPLATE);
        expectRequestUrlLookup();
        when(request.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY)).thenReturn(session);

        testCompanyAuthFilter.doFilter(request, response, chain);

        verifyCompanyAuthIsNotSkipped();
    }

    @Test
    public void doFilterWhenAuthIsRequiredAndCompanyNumberMismatch() throws IOException, ServletException {
        SubmissionFormApi submissionForm = createSubmissionForm(AUTH_REQUIRED_FORM_TEMPLATE);
        SubmissionApi submission = createSubmission(submissionForm);

        expectSession(session);
        signInInfo.setCompanyNumber("11111111");
        when(session.getSignInInfo()).thenReturn(signInInfo);
        expectCategoryAndFormLookup(submission, AUTH_REQUIRED_FORM_TEMPLATE);
        expectRequestUrlLookup();

        when(request.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY)).thenReturn(session);

        testCompanyAuthFilter.doFilter(request, response, chain);

        verifyCompanyAuthIsNotSkipped();
    }

    @Test
    public void doFilterWhenAuthIsRequiredAndCompanyNumberMatches() throws IOException, ServletException {
        SubmissionFormApi submissionForm = createSubmissionForm(INSOLVENCY_WITH_AUTH_REQUIRED_FORM_TEMPLATE);
        SubmissionApi submission = createSubmission(submissionForm);

        expectSession(session);
        signInInfo.getUserProfile().setEmail(TEST_EMAIL);
        expectCategoryAndFormLookup(submission, INSOLVENCY_WITH_AUTH_REQUIRED_FORM_TEMPLATE);
        when(categoryTemplateService.getTopLevelCategory(anyString())).thenReturn(CategoryTypeConstants.INSOLVENCY);
        when(apiClientService.isOnAllowList(TEST_EMAIL)).thenReturn(new ApiResponse<>(HttpStatus.OK.value(),
            Collections.emptyMap(), true));

        testCompanyAuthFilter.doFilter(request, response, chain);

        verifyCompanyAuthIsSkipped();
    }

    private SubmissionApi createSubmission(final SubmissionFormApi submissionForm) {
        SubmissionApi submission = new SubmissionApi();
        submission.setId(SUBMISSION_ID);
        submission.setSubmissionForm(submissionForm);
        return submission;
    }

    private SubmissionFormApi createSubmissionForm(final FormTemplateApi formTemplate) {
        SubmissionFormApi submissionForm = new SubmissionFormApi();
        submissionForm.setFormType(formTemplate.getFormType());
        return submissionForm;
    }

    private Session createSession(final String companyNumber) {
        Session session = new SessionImpl();

        Map<String, Object> userProfileData = createUserProfileData();

        Map<String, Object> signInData = createSignInData(userProfileData, companyNumber);

        Map<String, Object> sessionData = createSessionData(signInData);

        session.setData(sessionData);

        return session;
    }

    private Map<String, Object> createUserProfileData() {
        Map<String, Object> userProfileData = new HashMap<>();
        userProfileData.put(SessionKeys.EMAIL.getKey(), "demo@ch.gov.uk");
        userProfileData.put(SessionKeys.SCOPE.getKey(), VALID_AUTH_SCOPE);
        return userProfileData;
    }

    private Map<String, Object> createSignInData(final Map<String, Object> userProfileData,
        final String companyNumber) {
        Map<String, Object> signInData = new HashMap<>();
        signInData.put(SessionKeys.SIGNED_IN.getKey(), 1);
        signInData.put(SessionKeys.COMPANY_NUMBER.getKey(), companyNumber);
        signInData.put(SessionKeys.USER_PROFILE.getKey(), userProfileData);
        return signInData;
    }

    private Map<String, Object> createSessionData(final Map<String, Object> signInData) {
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put(SessionKeys.SIGN_IN_INFO.getKey(), signInData);
        return sessionData;
    }

    private void expectCategoryAndFormLookup(final SubmissionApi submission, final FormTemplateApi formTemplate) {

        when(request.getRequestURI()).thenReturn(MessageFormat.format(EFS_SUBMISSION_WITH_COMPANY,
            SUBMISSION_ID, COMPANY_NUMBER));
        when(request.getMethod()).thenReturn("GET");
        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(new ApiResponse<>(HttpStatus.OK.value(),
            Collections.emptyMap(), submission));
        when(formTemplateService.getFormTemplate(formTemplate.getFormType())).thenReturn(
            new ApiResponse<>(HttpStatus.OK.value(), Collections.emptyMap(), formTemplate));
    }

    private void expectRequestUrlLookup() {
        when(request.getRequestURL()).thenReturn(new StringBuffer(REQUEST_URL));
        when(request.getQueryString()).thenReturn("");
    }

    private void verifyCompanyAuthIsNotSkipped() throws IOException, ServletException {
        verify(request).getRequestURL();
        verify(response).sendRedirect(anyString());
        verify(chain).doFilter(request, response);
    }

    private void verifyCompanyAuthIsSkipped() throws IOException, ServletException {
        verify(chain).doFilter(request, response);
    }
}