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
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionFormApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants;

@ExtendWith(MockitoExtension.class)
class ConfirmationControllerImplTest extends BaseControllerImplTest {

    public static final String TEST_FORM = "TestForm";
    private ConfirmationController testController;

    @Mock
    private ApiResponse<FormTemplateApi> formTemplateApiResponse;

    @Mock
    private FormTemplateApi formTemplateApi;

    @BeforeEach
    protected void setup() {
        setUpHeaders();
        testController = new ConfirmationControllerImpl(logger, sessionService,
                apiClientService, formTemplateService, categoryTemplateService);
    }

    @Test
    void getViewName() {
        assertThat(((ConfirmationControllerImpl) testController).getViewName(),
            is(ViewConstants.CONFIRMATION.asView()));
    }

    @Test
    void getConfirmationRP() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        submission.setSubmissionForm(new SubmissionFormApi(null, TEST_FORM, null));
        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
            new ApiResponse<>(200, headers, submission));

        when(formTemplateService.getFormTemplate(TEST_FORM)).thenReturn(formTemplateApiResponse);
        when(formTemplateApiResponse.getData()).thenReturn(formTemplateApi);
        when(formTemplateApi.getFormCategory()).thenReturn("RP");
        when(categoryTemplateService.getTopLevelCategory("RP")).thenReturn(CategoryTypeConstants.REGISTRAR_POWERS);

        final String result = testController.getConfirmation(SUBMISSION_ID, COMPANY_NUMBER,
                model, request, session, sessionStatus);

        assertThat(result, is(ViewConstants.CONFIRMATION.asView()));
    }

    @Test
    void getConfirmationNonRP() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        submission.setSubmissionForm(new SubmissionFormApi(null, TEST_FORM, null));
        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
                new ApiResponse<>(200, headers, submission));

        when(formTemplateService.getFormTemplate(TEST_FORM)).thenReturn(formTemplateApiResponse);
        when(formTemplateApiResponse.getData()).thenReturn(formTemplateApi);
        when(formTemplateApi.getFormCategory()).thenReturn("MA");
        when(categoryTemplateService.getTopLevelCategory("MA")).thenReturn(CategoryTypeConstants.ARTICLES);

        final String result = testController.getConfirmation(SUBMISSION_ID, COMPANY_NUMBER,
                model, request, session, sessionStatus);

        assertThat(result, is(ViewConstants.CONFIRMATION.asView()));
    }

    @Test
    void getConfirmationWhenSubmissionNotOpen() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.SUBMITTED);
        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
            new ApiResponse<>(200, headers, submission));

        final String result = testController.getConfirmation(SUBMISSION_ID, COMPANY_NUMBER,
                model, request, session, sessionStatus);

        assertThat(result, is(ViewConstants.GONE.asView()));
    }

}