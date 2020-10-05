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
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;

@ExtendWith(MockitoExtension.class)
class ConfirmationControllerImplTest extends BaseControllerImplTest {

    private ConfirmationController testController;

    @BeforeEach
    protected void setup() {
        super.setUp();
        testController = new ConfirmationControllerImpl(logger, sessionService, apiClientService);
    }

    @Test
    void getViewName() {
        assertThat(((ConfirmationControllerImpl) testController).getViewName(),
            is(ViewConstants.CONFIRMATION.asView()));
    }

    @Test
    void getConfirmation() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
            new ApiResponse<>(200, headers, submission));

        final String result = testController.getConfirmation(SUBMISSION_ID, COMPANY_NUMBER, formTemplateAttribute, model, request, session, sessionStatus);

        assertThat(result, is(ViewConstants.CONFIRMATION.asView()));
    }

    @Test
    void getConfirmationWhenSubmissionNotOpen() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.SUBMITTED);
        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
            new ApiResponse<>(200, headers, submission));

        final String result = testController.getConfirmation(SUBMISSION_ID, COMPANY_NUMBER, formTemplateAttribute, model, request, session, sessionStatus);

        assertThat(result, is(ViewConstants.GONE.asView()));
    }

    @Test
    void getConfirmationWhenPaymentRequired() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
            new ApiResponse<>(200, headers, submission));
        when(formTemplateAttribute.getFee()).thenReturn("2");

        final String result = testController.getConfirmation(SUBMISSION_ID, COMPANY_NUMBER, formTemplateAttribute, model, request, session, sessionStatus);

        assertThat(result, is(ViewConstants.MISSING.asView()));
    }

}