package uk.gov.companieshouse.efs.web.service.api.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.efs.PrivateEfsResourceHandler;
import uk.gov.companieshouse.api.handler.efs.companyauthallowlist.PrivateEfsCompanyAuthAllowListResourceHandler;
import uk.gov.companieshouse.api.handler.efs.companyauthallowlist.request.PrivateCompanyAuthAllowListGet;
import uk.gov.companieshouse.api.handler.efs.companyauthallowlist.request.PrivateEfsCompanyAuthAllowListGetResourceHandler;
import uk.gov.companieshouse.api.handler.efs.submissions.PrivateEfsSubmissionsResourceHandler;
import uk.gov.companieshouse.api.handler.efs.submissions.request.PrivateEfsSubmissionGetResourceHandler;
import uk.gov.companieshouse.api.handler.efs.submissions.request.PrivateEfsSubmissionsCreateResourceHandler;
import uk.gov.companieshouse.api.handler.efs.submissions.request.PrivateEfsSubmissionsUpsertResourceHandler;
import uk.gov.companieshouse.api.handler.efs.submissions.request.PrivateModelCreate;
import uk.gov.companieshouse.api.handler.efs.submissions.request.PrivateModelUpsert;
import uk.gov.companieshouse.api.handler.efs.submissions.request.PrivateSubmissionGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.ConfirmAuthorisedApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileListApi;
import uk.gov.companieshouse.api.model.efs.submissions.FormTypeApi;
import uk.gov.companieshouse.api.model.efs.submissions.PaymentReferenceApi;
import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class ApiClientServiceImplTest {

    private static final String SUBMISSION_ID = "1111111111111111";

    private static final String SUBMISSION_URI = ApiClientServiceImpl.SUB_URI + SUBMISSION_ID;

    private static final String ROOT_URI = ApiClientServiceImpl.ROOT_URI;

    private static final ApiResponse<Void> EMPTY_OK_RESPONSE = new ApiResponse<>(HttpStatus.OK.value(),
        Collections.emptyMap());

    private static final String EMAIL_ADDRESS = "demo@email.com";

    private ApiClientService apiClientService;

    @Mock
    private SubmissionResponseApi submissionResponse;

    @Mock
    private SubmissionApi submission;

    @Mock
    private PresenterApi presenter;

    @Mock
    private CompanyApi company;

    @Mock
    private FormTypeApi formType;

    @Mock
    private FileListApi fileList;

    @Mock
    private PaymentReferenceApi paymentReference;

    @Mock
    private ConfirmAuthorisedApi confirmAuthorised;

    @Mock
    private Logger logger;

    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private PrivateEfsResourceHandler resourceHandler;

    @Mock
    private PrivateEfsSubmissionsResourceHandler submissionsResourceHandler;

    @Mock
    private PrivateEfsSubmissionsCreateResourceHandler createResourceHandler;

    @Mock
    private PrivateEfsSubmissionGetResourceHandler getResourceHandler;

    @Mock
    private PrivateSubmissionGet submissionGet;

    @Mock
    private PrivateModelCreate modelCreate;

    @Mock
    private PrivateEfsSubmissionsUpsertResourceHandler upsertResourceHandler;

    @Mock
    private PrivateModelUpsert modelUpsert;

    @Mock
    private PrivateEfsCompanyAuthAllowListResourceHandler companyAuthAllowListResourceHandler;

    @Mock
    private PrivateEfsCompanyAuthAllowListGetResourceHandler companyAuthAllowListGetResourceHandler;

    @Mock
    private PrivateCompanyAuthAllowListGet companyAuthAllowListGet;

    @BeforeEach
    void setUp() {
        apiClientService = Mockito.spy(new ApiClientServiceImpl(logger));
        stubApiClientStatic();
    }

    @Test
    void createSubmission() throws ApiErrorResponseException, URIValidationException {
        final ApiResponse<SubmissionResponseApi> expected = new ApiResponse<>(
            HttpStatus.OK.value(), Collections.emptyMap(), submissionResponse);

        when(resourceHandler.submissions()).thenReturn(submissionsResourceHandler);
        when(submissionsResourceHandler.newSubmission()).thenReturn(createResourceHandler);
        when(createResourceHandler.create(
            ApiClientServiceImpl.ROOT_URI + "/submissions/new", presenter)).thenReturn(modelCreate);
        when(modelCreate.execute()).thenReturn(expected);

        final ApiResponse<SubmissionResponseApi> response = apiClientService.createSubmission(presenter);

        assertThat(response, is(expected));
    }

    @Test
    void getSubmission() throws ApiErrorResponseException, URIValidationException {
        final ApiResponse<SubmissionApi> expected = new ApiResponse<>(
            HttpStatus.OK.value(), Collections.emptyMap(), submission);

        when(resourceHandler.submissions()).thenReturn(submissionsResourceHandler);
        when(submissionsResourceHandler.getSubmission()).thenReturn(getResourceHandler);
        when(getResourceHandler.get(SUBMISSION_URI)).thenReturn(submissionGet);
        when(submissionGet.execute()).thenReturn(expected);

        final ApiResponse<SubmissionApi> response = apiClientService.getSubmission(SUBMISSION_ID);

        assertThat(response, is(expected));
    }

    @Test
    void getSubmissionWhenBadUrl() throws ApiErrorResponseException, URIValidationException {
        when(resourceHandler.submissions()).thenReturn(submissionsResourceHandler);
        when(submissionsResourceHandler.getSubmission()).thenReturn(getResourceHandler);
        when(getResourceHandler.get(SUBMISSION_URI)).thenReturn(submissionGet);
        when(submissionGet.execute()).thenThrow(new URIValidationException("expected"));

        final ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> apiClientService.getSubmission(SUBMISSION_ID));

        assertThat(exception.getStatus(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    void getSubmissionWhenBadRequest() throws ApiErrorResponseException, URIValidationException {
        final HttpResponseException httpResponseException = new HttpResponseException.Builder(
            HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), new HttpHeaders()).build();

        when(resourceHandler.submissions()).thenReturn(submissionsResourceHandler);
        when(submissionsResourceHandler.getSubmission()).thenReturn(getResourceHandler);
        when(getResourceHandler.get(SUBMISSION_URI)).thenReturn(submissionGet);
        when(submissionGet.execute()).thenThrow(ApiErrorResponseException.
            fromHttpResponseException(httpResponseException));

        final ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> apiClientService.getSubmission(SUBMISSION_ID));

        assertThat(exception.getStatus(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    void getSubmissionWhenNotFound() throws ApiErrorResponseException, URIValidationException {
        final HttpResponseException httpResponseException = new HttpResponseException.Builder(
            HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), new HttpHeaders()).build();

        when(resourceHandler.submissions()).thenReturn(submissionsResourceHandler);
        when(submissionsResourceHandler.getSubmission()).thenReturn(getResourceHandler);
        when(getResourceHandler.get(SUBMISSION_URI)).thenReturn(submissionGet);
        when(submissionGet.execute()).thenThrow(ApiErrorResponseException.
            fromHttpResponseException(httpResponseException));

        final ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> apiClientService.getSubmission(SUBMISSION_ID));

        assertThat(exception.getStatus(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    void getSubmissionWhenServiceError() throws ApiErrorResponseException, URIValidationException {
        final HttpResponseException httpResponseException = new HttpResponseException.Builder(
            HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            new HttpHeaders()).build();

        when(resourceHandler.submissions()).thenReturn(submissionsResourceHandler);
        when(submissionsResourceHandler.getSubmission()).thenReturn(getResourceHandler);
        when(getResourceHandler.get(SUBMISSION_URI)).thenReturn(submissionGet);
        when(submissionGet.execute()).thenThrow(ApiErrorResponseException.fromHttpResponseException(
            httpResponseException));

        final ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> apiClientService.getSubmission(SUBMISSION_ID));

        assertThat(exception.getStatus(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void putCompany() throws ApiErrorResponseException, URIValidationException {
        when(resourceHandler.submissions()).thenReturn(submissionsResourceHandler);
        when(submissionsResourceHandler.company()).thenReturn(upsertResourceHandler);
        when(upsertResourceHandler.upsert(SUBMISSION_URI + "/company", company)).thenReturn(modelUpsert);
        when(modelUpsert.execute()).thenReturn(EMPTY_OK_RESPONSE);

        final ApiResponse<SubmissionResponseApi> response = apiClientService.putCompany(SUBMISSION_ID, company);

        assertThat(response, is(EMPTY_OK_RESPONSE));
    }

    @Test
    void putFormType() throws ApiErrorResponseException, URIValidationException {
        when(resourceHandler.submissions()).thenReturn(submissionsResourceHandler);
        when(submissionsResourceHandler.form()).thenReturn(upsertResourceHandler);
        when(upsertResourceHandler.upsert(SUBMISSION_URI + "/form", formType)).thenReturn(modelUpsert);
        when(modelUpsert.execute()).thenReturn(EMPTY_OK_RESPONSE);

        final ApiResponse<SubmissionResponseApi> response = apiClientService.putFormType(SUBMISSION_ID, formType);

        assertThat(response, is(EMPTY_OK_RESPONSE));
    }

    @Test
    void putFileList() throws ApiErrorResponseException, URIValidationException {
        when(resourceHandler.submissions()).thenReturn(submissionsResourceHandler);
        when(submissionsResourceHandler.file()).thenReturn(upsertResourceHandler);
        when(upsertResourceHandler.upsert(SUBMISSION_URI + "/files", fileList)).thenReturn(modelUpsert);
        when(modelUpsert.execute()).thenReturn(EMPTY_OK_RESPONSE);

        final ApiResponse<SubmissionResponseApi> response = apiClientService.putFileList(SUBMISSION_ID, fileList);

        assertThat(response, is(EMPTY_OK_RESPONSE));
    }

    @Test
    void putPayment() throws ApiErrorResponseException, URIValidationException {
        when(resourceHandler.submissions()).thenReturn(submissionsResourceHandler);
        when(submissionsResourceHandler.payment()).thenReturn(upsertResourceHandler);
        when(upsertResourceHandler.upsert(SUBMISSION_URI + "/payment", paymentReference)).thenReturn(modelUpsert);
        when(modelUpsert.execute()).thenReturn(EMPTY_OK_RESPONSE);

        final ApiResponse<SubmissionResponseApi> response = apiClientService.putPayment(SUBMISSION_ID, paymentReference);

        assertThat(response, is(EMPTY_OK_RESPONSE));
    }

    @Test
    void putConfirmAuthorised() throws ApiErrorResponseException, URIValidationException {
        when(resourceHandler.submissions()).thenReturn(submissionsResourceHandler);
        when(submissionsResourceHandler.confirmAuthorised()).thenReturn(upsertResourceHandler);
        when(upsertResourceHandler.upsert(SUBMISSION_URI + "/confirmAuthorised", confirmAuthorised))
            .thenReturn(modelUpsert);
        when(modelUpsert.execute()).thenReturn(EMPTY_OK_RESPONSE);

        final ApiResponse<SubmissionResponseApi> response = apiClientService.putConfirmAuthorised(
            SUBMISSION_ID, confirmAuthorised);

        assertThat(response, is(EMPTY_OK_RESPONSE));
    }

    @Test
    void putSubmissionSubmitted() throws ApiErrorResponseException, URIValidationException {
        when(resourceHandler.submissions()).thenReturn(submissionsResourceHandler);
        when(submissionsResourceHandler.submit()).thenReturn(upsertResourceHandler);
        when(upsertResourceHandler.upsert(SUBMISSION_URI, SubmissionStatus.SUBMITTED)).thenReturn(modelUpsert);
        when(modelUpsert.execute()).thenReturn(EMPTY_OK_RESPONSE);

        final ApiResponse<SubmissionResponseApi> response = apiClientService.putSubmissionSubmitted(SUBMISSION_ID);

        assertThat(response, is(EMPTY_OK_RESPONSE));
    }

    @Test
    void isOnAllowList() throws ApiErrorResponseException, URIValidationException,
        UnsupportedEncodingException {
        final ApiResponse<Boolean> expected = new ApiResponse<>(
            HttpStatus.OK.value(), Collections.emptyMap(), true);

        expectCompanyAuthAllowListResourceHandler();
        when(companyAuthAllowListGet.execute()).thenReturn(expected);

        final ApiResponse<Boolean> response = apiClientService.isOnAllowList(EMAIL_ADDRESS);

        assertThat(response, is(expected));
    }

    @Test
    void isOnAllowListWhenBadUrl() throws ApiErrorResponseException, URIValidationException,
        UnsupportedEncodingException {
        expectCompanyAuthAllowListResourceHandler();
        when(companyAuthAllowListGet.execute()).thenThrow(new URIValidationException("expected"));

        final ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> apiClientService.isOnAllowList(EMAIL_ADDRESS));

        assertThat(exception.getStatus(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    void isOnAllowListWhenBadRequest() throws ApiErrorResponseException, URIValidationException,
        UnsupportedEncodingException {
        final HttpResponseException httpResponseException = new HttpResponseException.Builder(
            HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), new HttpHeaders()).build();

        expectCompanyAuthAllowListResourceHandler();
        when(companyAuthAllowListGet.execute()).thenThrow(ApiErrorResponseException.fromHttpResponseException(
            httpResponseException));

        final ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> apiClientService.isOnAllowList(EMAIL_ADDRESS));

        assertThat(exception.getStatus(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    void isOnAllowListWhenNotFound() throws ApiErrorResponseException, URIValidationException,
        UnsupportedEncodingException {
        final HttpResponseException httpResponseException = new HttpResponseException.Builder(
            HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), new HttpHeaders()).build();

        expectCompanyAuthAllowListResourceHandler();
        when(companyAuthAllowListGet.execute()).thenThrow(ApiErrorResponseException.fromHttpResponseException(
            httpResponseException));

        final ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> apiClientService.isOnAllowList(EMAIL_ADDRESS));

        assertThat(exception.getStatus(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    void isOnAllowListWhenServiceError() throws ApiErrorResponseException, URIValidationException,
        UnsupportedEncodingException {
        final HttpResponseException httpResponseException = new HttpResponseException.Builder(
            HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            new HttpHeaders()).build();

        expectCompanyAuthAllowListResourceHandler();
        when(companyAuthAllowListGet.execute()).thenThrow(ApiErrorResponseException.fromHttpResponseException(
            httpResponseException));

        final ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> apiClientService.isOnAllowList(EMAIL_ADDRESS));

        assertThat(exception.getStatus(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private void stubApiClientStatic() {
        doReturn(internalApiClient).when(apiClientService).getApiClient();

        when(internalApiClient.privateEfsResourceHandler()).thenReturn(resourceHandler);
    }

    private void expectCompanyAuthAllowListResourceHandler() throws UnsupportedEncodingException {
        when(resourceHandler.companyAuthAllowList()).thenReturn(companyAuthAllowListResourceHandler);
        when(companyAuthAllowListResourceHandler.isOnAllowList()).thenReturn(
            companyAuthAllowListGetResourceHandler);
        when(companyAuthAllowListGetResourceHandler
            .get(ROOT_URI + "/company-authentication/allow-list/" + URLEncoder.encode(EMAIL_ADDRESS, "UTF-8")))
            .thenReturn(companyAuthAllowListGet);
    }
}