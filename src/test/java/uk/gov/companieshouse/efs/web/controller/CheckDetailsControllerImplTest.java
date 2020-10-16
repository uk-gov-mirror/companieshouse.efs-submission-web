package uk.gov.companieshouse.efs.web.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.ConfirmAuthorisedApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileDetailApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileDetailListApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionFormApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
import uk.gov.companieshouse.efs.web.model.CheckDetailsModel;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.efs.web.validation.ConfirmAuthorisedValidator;

@ExtendWith(MockitoExtension.class)
class CheckDetailsControllerImplTest extends BaseControllerImplTest {

    private CheckDetailsController testController;
    private static final String FORM_TYPE = "AM01";
    @Mock
    private ApiClientService apiClientService;
    @Mock
    private CheckDetailsModel checkDetailsAttribute;
    @Mock
    private ConfirmAuthorisedValidator confirmAuthorisedValidator;
    @Mock
    private SessionListApi paymentSessions;

    @BeforeEach
    protected void setUp() {
        super.setUp();
        testController = new CheckDetailsControllerImpl(logger, sessionService, apiClientService, formTemplateService,
            categoryTemplateService, checkDetailsAttribute, confirmAuthorisedValidator);
        ((CheckDetailsControllerImpl) testController).setChsUrl(CHS_URL);
    }

    @Test
    void getViewName() {
        assertThat(((CheckDetailsControllerImpl) testController).getViewName(),
            is(ViewConstants.CHECK_DETAILS.asView()));
    }

    @Test
    void checkDetails() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
            new ApiResponse<>(200, getHeaders(), submission));
        when(formTemplateService.getFormTemplate(FORM_TYPE)).thenReturn(
            new ApiResponse<FormTemplateApi>(200, getHeaders(), new FormTemplateApi()));
        final String result = testController.checkDetails(SUBMISSION_ID, COMPANY_NUMBER, checkDetailsAttribute, model, request, session, sessionStatus);

        assertThat(result, is(ViewConstants.CHECK_DETAILS.asView()));
    }

    @Test
    void checkDetailsWhenSubmissionNotOpen() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.SUBMITTED);
        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
            new ApiResponse<>(200, headers, submission));

        final String result = testController.checkDetails(SUBMISSION_ID, COMPANY_NUMBER, checkDetailsAttribute, model, request, session, sessionStatus);

        assertThat(result, is(ViewConstants.GONE.asView()));
    }

    @Test
    void postCheckDetailsWhenValidFeeForm() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
            new ApiResponse<>(200, getHeaders(), submission));
        when(bindingResult.hasErrors()).thenReturn(false);
        when(apiClientService.putConfirmAuthorised(SUBMISSION_ID, new ConfirmAuthorisedApi(false))).thenReturn(
            new ApiResponse<>(200, getHeaders(), new SubmissionResponseApi(SUBMISSION_ID)));
        when(checkDetailsAttribute.getPaymentCharge()).thenReturn("99");

        final String result = testController.postCheckDetails(SUBMISSION_ID, COMPANY_NUMBER, checkDetailsAttribute, bindingResult, model, request);

        assertThat(result, is(ViewConstants.PAYMENT.asRedirectUri(CHS_URL, SUBMISSION_ID, COMPANY_NUMBER)));
    }

    @Test
    void postCheckDetailsWhenValidNonFeeForm() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
            new ApiResponse<>(200, getHeaders(), submission));
        when(bindingResult.hasErrors()).thenReturn(false);
        when(apiClientService.putConfirmAuthorised(SUBMISSION_ID, new ConfirmAuthorisedApi(false))).thenReturn(
            new ApiResponse<>(200, getHeaders(), new SubmissionResponseApi(SUBMISSION_ID)));
        when(checkDetailsAttribute.getPaymentCharge()).thenReturn(null);

        final String result = testController.postCheckDetails(SUBMISSION_ID, COMPANY_NUMBER, checkDetailsAttribute, bindingResult, model, request);

        assertThat(result, is(ViewConstants.CONFIRMATION.asRedirectUri(CHS_URL, SUBMISSION_ID, COMPANY_NUMBER)));
    }

    @Test
    void postCheckDetailsWhenNotValid() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
            new ApiResponse<>(200, getHeaders(), submission));
        when(bindingResult.hasErrors()).thenReturn(true);
        when(formTemplateService.getFormTemplate(FORM_TYPE))
            .thenReturn(new ApiResponse<>(200, getHeaders(), new FormTemplateApi()));

        final String result = testController.postCheckDetails(SUBMISSION_ID, COMPANY_NUMBER, checkDetailsAttribute, bindingResult, model, request);

        assertThat(result, is(ViewConstants.CHECK_DETAILS.asView()));
    }

    protected SubmissionApi createSubmission(final SubmissionStatus submitted) {
        final SubmissionApi submission = super.createSubmission(submitted);
        submission.setSubmissionForm(new SubmissionFormApi());
        submission.getSubmissionForm().setFormType(FORM_TYPE);
        submission.setCompany(new CompanyApi(COMPANY_NUMBER, COMPANY_NAME));
        submission.getSubmissionForm().setFileDetails(new FileDetailListApi());
        submission.getSubmissionForm().getFileDetails().add(new FileDetailApi());
        submission.setPaymentSessions(paymentSessions);

        return submission;
    }
}