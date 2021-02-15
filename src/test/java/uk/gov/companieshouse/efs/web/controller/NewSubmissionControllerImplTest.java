package uk.gov.companieshouse.efs.web.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.web.model.company.CompanyDetail;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
    void newSubmissionWhenRuntimeError() throws Exception {
        doThrow(new RuntimeException("dummy exception")).when(sessionService).getUserEmail();

        mockMvc = MockMvcBuilders.standaloneSetup(testController)
                .setControllerAdvice(new GlobalExceptionHandler(logger))
                .build();

        String newSubmissionUrl = "/efs-submission/new-submission/";
        mockMvc.perform(get(newSubmissionUrl).flashAttr("companyDetail", companyDetail))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name(ViewConstants.ERROR.asView()))
                .andReturn();
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

    @ParameterizedTest
    @MethodSource("non200StatusCodes")
    void testLogNon200ErrorCodes(HttpStatus status) throws Exception {

        mockMvc = MockMvcBuilders.standaloneSetup(testController)
                .setControllerAdvice(new GlobalExceptionHandler(logger))
                .build();

        when(apiClientService.createSubmission(any()))
                .thenThrow(new ResponseStatusException(status));

        String newSubmissionUrl = "/efs-submission/new-submission/";
        mockMvc.perform(get(newSubmissionUrl).flashAttr("companyDetail", companyDetail))
                .andReturn();

        ArgumentCaptor<Map<String, Object>> logDetailsCapture = ArgumentCaptor.forClass(Map.class);
        verify(logger).errorContext(eq(""),
                contains("Received non 200 series response from API"),
                any(ResponseStatusException.class), logDetailsCapture.capture());

        assertThat(logDetailsCapture.getValue(), hasEntry("statusCode", status.value()));
    }


    private static Stream<Arguments> non200StatusCodes() {
        Predicate<HttpStatus> isNon200 = status ->
                status.series() != HttpStatus.Series.SUCCESSFUL;

        return Arrays.stream(HttpStatus.values())
                .filter(isNon200)
                .map(Arguments::of);
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