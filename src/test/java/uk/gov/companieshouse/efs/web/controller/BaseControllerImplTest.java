package uk.gov.companieshouse.efs.web.controller;

import static org.mockito.Mockito.when;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.SessionStatus;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
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

    protected MockMvc mockMvc;

    protected Map<String, Object> headers;

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
}
