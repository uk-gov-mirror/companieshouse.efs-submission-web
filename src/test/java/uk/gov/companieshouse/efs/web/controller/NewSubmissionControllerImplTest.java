package uk.gov.companieshouse.efs.web.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.web.model.company.CompanyDetail;

@ExtendWith(MockitoExtension.class)
class NewSubmissionControllerImplTest extends BaseControllerImplTest {

    private NewSubmissionController testController;

    @Mock
    private CompanyDetail companyDetail;
    @Mock
    private RedirectAttributes attributes;
    private CompanyApi company;

    @BeforeEach
    protected void setUp() {
        super.setUp();
        testController = new NewSubmissionControllerImpl(logger, sessionService, apiClientService);
        ((NewSubmissionControllerImpl) testController).setChsUrl(CHS_URL);
        company = new CompanyApi(COMPANY_NUMBER, COMPANY_NAME);
    }

    @Test
    void getViewName() {
        assertThat(((NewSubmissionControllerImpl) testController).getViewName(),
            is(ViewConstants.NEW_SUBMISSION.asView()));
    }

    @Test
    void newSubmission() {
        expectCreateSubmission();

        final String result = testController.newSubmission(companyDetail, sessionStatus, request, attributes);

        verify(companyDetail).clear();
        verify(attributes)
            .addAttribute("forward", "/efs-submission/" + SUBMISSION_ID + "/company/{companyNumber}/details");
        assertThat(result, is(ViewConstants.COMPANY_LOOKUP.asRedirectUri(CHS_URL, SUBMISSION_ID)));
    }

    @Test
    void newSubmissionWhenRuntimeError() {
        doThrow(new RuntimeException("dummy exception")).when(sessionService).getUserEmail();

        final String result = testController.newSubmission(companyDetail, sessionStatus, request, attributes);

        assertThat(result, is(ViewConstants.ERROR.asView()));
    }

    @Test
    void newSubmissionForCompany() {
        expectCreateSubmission();
        when(companyDetail.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(companyDetail.getCompanyName()).thenReturn(COMPANY_NAME);

        final String result =
            testController.newSubmissionForCompany(COMPANY_NUMBER, companyDetail, sessionStatus, request, attributes);

        verify(apiClientService).putCompany(eq(SUBMISSION_ID), refEq(company));
        assertThat(result, is(ViewConstants.CATEGORY_SELECTION.asRedirectUri(CHS_URL, SUBMISSION_ID, COMPANY_NUMBER)));
    }

    @Test
    void newSubmissionForCompanyWhenNumberMismatch() {
        when(companyDetail.getCompanyNumber()).thenReturn("00000000");

        final String result =
            testController.newSubmissionForCompany(COMPANY_NUMBER, companyDetail, sessionStatus, request, attributes);

        assertThat(result, is(ViewConstants.ERROR.asView()));
    }

    @Test
    void newSubmissionForCompanyWhenRuntimeError() {
        expectCreateSubmission();
        when(companyDetail.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(companyDetail.getCompanyName()).thenReturn(COMPANY_NAME);
        when(apiClientService.putCompany(eq(SUBMISSION_ID), refEq(company)))
            .thenThrow(new RuntimeException("dummy exception"));

        final String result =
            testController.newSubmissionForCompany(COMPANY_NUMBER, companyDetail, sessionStatus, request, attributes);

        assertThat(result, is(ViewConstants.ERROR.asView()));
    }

    private SubmissionResponseApi createSubmissionResponse() {
        final SubmissionResponseApi submissionResponse = new SubmissionResponseApi();

        submissionResponse.setId(SUBMISSION_ID);
        return submissionResponse;
    }

    private ApiResponse<SubmissionResponseApi> createApiResponse(final SubmissionResponseApi submissionResponse) {
        return new ApiResponse<>(200, getHeaders(), submissionResponse);
    }

    private void expectCreateSubmission() {
        PresenterApi presenter = new PresenterApi(USER_EMAIL);
        final SubmissionResponseApi submissionResponse = createSubmissionResponse();
        final ApiResponse<SubmissionResponseApi> apiResponse = createApiResponse(submissionResponse);

        submissionResponse.setId(SUBMISSION_ID);
        when(sessionService.getUserEmail()).thenReturn(USER_EMAIL);
        when(apiClientService.createSubmission(presenter)).thenReturn(apiResponse);
    }
}