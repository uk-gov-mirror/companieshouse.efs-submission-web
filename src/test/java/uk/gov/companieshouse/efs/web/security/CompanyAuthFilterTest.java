package uk.gov.companieshouse.efs.web.security;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
class CompanyAuthFilterTest {

    private static final String OAUTH2_REQUEST_KEY = "pXf+qkU6P6SAoY2lKW0FtKMS4PylaNA3pY2sUQxNFDk=";
    private static final String OAUTH2_AUTH_URI = "http://testAuthURI";
    private static final String OAUTH2_CLIENT_ID = "dummyClientId";
    private static final String OAUTH2_REDIRECT_URI = "http://testRedirectURI";
    private static final String COOKIE_SECRET = "iamasecret";

    private static final String EFS_SUBMISSION_WITH_COMPANY = "/efs-submission/{0}/company/{1}";

    private static final String REQUEST_URL = "http://chs.companieshouse.gov.uk/";

    private static final String SUBMISSION_ID = "5f8422b326e7b618e25684da";

    private static final String COMPANY_NUMBER = "12345678";

    private static final String VALID_AUTH_SCOPE = "https://chs.companieshouse.gov.uk/company/" + COMPANY_NUMBER;

    public static final FormTemplateApi INSOLVENCY_WITH_AUTH_REQUIRED_FORM_TEMPLATE =
        new FormTemplateApi("REC01", null, "REC", null, true, false);

    public static final FormTemplateApi AUTH_REQUIRED_FORM_TEMPLATE =
        new FormTemplateApi("CC02", null, "CC", null, true, false);

    public static final FormTemplateApi AUTH_NOT_REQUIRED_FORM_TEMPLATE =
        new FormTemplateApi("CC01", null, "CC", null, false, false);
    private static final String TEST_EMAIL = "testing@test.com";

    public static final String NON_MATCHING_FINE_GRAINED_SCOPE = " /company/12345678/admin.write-full";
    public static final String MATCHING_FINE_GRAINED_SCOPE = "/company/12345678/admin.write-full";
    private static final String OTHER_COMPANY_NUMBER = "11223344";

    private TestCompanyAuthFilter spyFilter;
    private static final String MATCHING_LEGACY_SCOPE = "/company/" + OTHER_COMPANY_NUMBER;
    private static final String NON_MATCHING_LEGACY_SCOPE = MATCHING_LEGACY_SCOPE + " ";

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
    void doFilterWhenUrlIsNotMatched() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("");
        when(request.getMethod()).thenReturn("GET");

        testCompanyAuthFilter.doFilter(request, response, chain);

        verifyCompanyAuthIsSkipped();
    }

    @Test
    void doFilterWhenMethodIsNotGET() throws IOException, ServletException {
        when(request.getMethod()).thenReturn("POST");

        testCompanyAuthFilter.doFilter(request, response, chain);

        verifyCompanyAuthIsSkipped();
    }

    @Test
    void doFilterWhenSubmissionFormApiIsNull() throws IOException, ServletException {
        SubmissionApi submission = createSubmission(null);

        ApiResponse<SubmissionApi> submissionApiResponse =
            new ApiResponse<>(HttpStatus.OK.value(), new HashMap<>(), submission);

        when(request.getRequestURI())
            .thenReturn(MessageFormat.format(EFS_SUBMISSION_WITH_COMPANY, SUBMISSION_ID, COMPANY_NUMBER));
        when(request.getMethod()).thenReturn("GET");
        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(submissionApiResponse);

        testCompanyAuthFilter.doFilter(request, response, chain);

        verifyCompanyAuthIsSkipped();
    }

    @Test
    void doFilterWhenCHSessionIsNull() throws IOException, ServletException {
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
    void doFilterWhenAuthIsRequiredAndEmailIsNull() throws IOException, ServletException {
        SubmissionFormApi submissionForm = createSubmissionForm(AUTH_REQUIRED_FORM_TEMPLATE);
        SubmissionApi submission = createSubmission(submissionForm);

        expectSession(session);
        signInInfo.setCompanyNumber(OTHER_COMPANY_NUMBER);
        expectCategoryAndFormLookup(submission, AUTH_REQUIRED_FORM_TEMPLATE);
        expectRequestUrlLookup();
        when(request.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY)).thenReturn(session);

        testCompanyAuthFilter.doFilter(request, response, chain);

        verifyCompanyAuthIsNotSkipped();
    }

    @Test
    void doFilterWhenAuthIsNotRequired() throws IOException, ServletException {
        SubmissionFormApi submissionForm = createSubmissionForm(AUTH_NOT_REQUIRED_FORM_TEMPLATE);
        SubmissionApi submission = createSubmission(submissionForm);

        expectCategoryAndFormLookup(submission, AUTH_NOT_REQUIRED_FORM_TEMPLATE);

        testCompanyAuthFilter.doFilter(request, response, chain);

        verifyCompanyAuthIsSkipped();
    }

    @Test
    void doFilterWhenAuthIsRequiredForInsolvencyAndUserIsOnAllowList() throws IOException, ServletException {
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
    void doFilterWhenAuthIsRequiredForInsolvencyAndUserProfileIsNull() throws IOException, ServletException {
        SubmissionFormApi submissionForm = createSubmissionForm(INSOLVENCY_WITH_AUTH_REQUIRED_FORM_TEMPLATE);
        SubmissionApi submission = createSubmission(submissionForm);

        expectSession(session);
        signInInfo.setUserProfile(null);
        signInInfo.setCompanyNumber(OTHER_COMPANY_NUMBER);
        expectCategoryAndFormLookup(submission, INSOLVENCY_WITH_AUTH_REQUIRED_FORM_TEMPLATE);
        expectRequestUrlLookup();
        when(request.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY)).thenReturn(session);

        testCompanyAuthFilter.doFilter(request, response, chain);

        verifyCompanyAuthIsNotSkipped();
    }

    @Test
    void doFilterWhenAuthIsRequiredForInsolvencyAndUserProfileScopeIsNull() throws IOException, ServletException {
        SubmissionFormApi submissionForm = createSubmissionForm(INSOLVENCY_WITH_AUTH_REQUIRED_FORM_TEMPLATE);
        SubmissionApi submission = createSubmission(submissionForm);

        expectSession(session);
        signInInfo.setUserProfile(userProfile);
        signInInfo.setCompanyNumber(OTHER_COMPANY_NUMBER);
        userProfile.setScope(null);
        expectCategoryAndFormLookup(submission, INSOLVENCY_WITH_AUTH_REQUIRED_FORM_TEMPLATE);
        expectRequestUrlLookup();
        when(request.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY)).thenReturn(session);

        testCompanyAuthFilter.doFilter(request, response, chain);

        verifyCompanyAuthIsNotSkipped();
    }

    @ParameterizedTest(name = "Fine grained scopes={0}")
    @ValueSource(booleans = {false, true})
    void doFilterWhenAuthIsRequiredForInsolvencyAndFineGrainedScopeMatches(final boolean fineGrainedUserScope)
        throws IOException, ServletException {
        SubmissionFormApi submissionForm = createSubmissionForm(INSOLVENCY_WITH_AUTH_REQUIRED_FORM_TEMPLATE);
        SubmissionApi submission = createSubmission(submissionForm);

        expectFineGrainedScope();

        expectSession(session);
        signInInfo.setUserProfile(userProfile);
        signInInfo.setCompanyNumber(OTHER_COMPANY_NUMBER);
        userProfile.setScope(fineGrainedUserScope ? MATCHING_FINE_GRAINED_SCOPE : MATCHING_LEGACY_SCOPE);
        expectCategoryAndFormLookup(submission, INSOLVENCY_WITH_AUTH_REQUIRED_FORM_TEMPLATE);
        expectRequestUrlLookup();
        when(request.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY)).thenReturn(session);

        testCompanyAuthFilter.doFilter(request, response, chain);

        verifyCompanyAuthIsNotSkipped();
    }

    @ParameterizedTest(name = "Fine grained scopes={0}")
    @ValueSource(booleans = {false, true})
    void doFilterWhenAuthIsRequiredForInsolvencyAndFineGrainedScopeMatchesCompanyNumber(
        final boolean fineGrainedUserScope) throws IOException, ServletException {
        SubmissionFormApi submissionForm = createSubmissionForm(INSOLVENCY_WITH_AUTH_REQUIRED_FORM_TEMPLATE);
        SubmissionApi submission = createSubmission(submissionForm);

        if (fineGrainedUserScope) {
            expectFineGrainedScope();
        }

        expectSession(session);
        signInInfo.setUserProfile(userProfile);

        final String scope = fineGrainedUserScope ? MATCHING_FINE_GRAINED_SCOPE : MATCHING_LEGACY_SCOPE;

        userProfile.setScope(scope.replace(OTHER_COMPANY_NUMBER, COMPANY_NUMBER));
        expectCategoryAndFormLookup(submission, INSOLVENCY_WITH_AUTH_REQUIRED_FORM_TEMPLATE);
        when(request.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY)).thenReturn(session);

        testCompanyAuthFilter.doFilter(request, response, chain);

        verifyCompanyAuthIsSkipped();
    }

    @ParameterizedTest(name = "Fine grained scopes={0}")
    @ValueSource(booleans = {false, true})
    void doFilterWhenAuthIsRequiredForInsolvencyAndScopeDoesntMatch(final boolean fineGrainedUserScope)
        throws IOException, ServletException {
        SubmissionFormApi submissionForm = createSubmissionForm(INSOLVENCY_WITH_AUTH_REQUIRED_FORM_TEMPLATE);
        SubmissionApi submission = createSubmission(submissionForm);

        if (fineGrainedUserScope) {
            expectFineGrainedScope();
        }

        expectSession(session);
        signInInfo.setUserProfile(userProfile);
        signInInfo.setCompanyNumber(OTHER_COMPANY_NUMBER);
        userProfile.setScope(fineGrainedUserScope ? NON_MATCHING_FINE_GRAINED_SCOPE : NON_MATCHING_LEGACY_SCOPE);
        expectCategoryAndFormLookup(submission, INSOLVENCY_WITH_AUTH_REQUIRED_FORM_TEMPLATE);
        expectRequestUrlLookup();
        when(request.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY)).thenReturn(session);

        testCompanyAuthFilter.doFilter(request, response, chain);

        verifyCompanyAuthIsNotSkipped();
    }

    @Test
    void doFilterWhenAuthIsRequiredAndCompanyNumberMismatch() throws IOException, ServletException {
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
    void doFilterWhenAuthIsRequiredAndCompanyNumberMatches() throws IOException, ServletException {
        SubmissionFormApi submissionForm = createSubmissionForm(INSOLVENCY_WITH_AUTH_REQUIRED_FORM_TEMPLATE);
        SubmissionApi submission = createSubmission(submissionForm);

        expectSession(session);
        signInInfo.getUserProfile().setEmail(TEST_EMAIL);
        expectCategoryAndFormLookup(submission, INSOLVENCY_WITH_AUTH_REQUIRED_FORM_TEMPLATE);
        when(categoryTemplateService.getTopLevelCategory(anyString())).thenReturn(CategoryTypeConstants.INSOLVENCY);
        when(apiClientService.isOnAllowList(TEST_EMAIL))
            .thenReturn(new ApiResponse<>(HttpStatus.OK.value(), Collections.emptyMap(), true));

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

    private void expectFineGrainedScope() {
        when(environmentReader.getOptionalString("USE_FINE_GRAIN_SCOPES_MODEL")).thenReturn("1");
        testCompanyAuthFilter = new TestCompanyAuthFilter(environmentReader, apiClientService, formTemplateService,
            categoryTemplateService);
        spyFilter = spy(testCompanyAuthFilter);
    }
}