package uk.gov.companieshouse.efs.web.payment.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.payment.PaymentResourceHandler;
import uk.gov.companieshouse.api.handler.payment.request.PaymentCreate;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.api.model.payment.PaymentSessionApi;
import uk.gov.companieshouse.efs.web.controller.BaseControllerImpl;
import uk.gov.companieshouse.efs.web.exception.ServiceException;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.efs.web.service.api.impl.BaseApiClientServiceImpl;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    private static final String SUB_ID = "0000000000";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String PAYMENT_SESSION_STATE =
        "E_lLgj6SI8cWoEXVtGMsuB81DoEcOiWPPgSJTz4OQ0gVo0y6d_NDFP7waRQfdU1z";
    private static final String API_URL = "http://localhost:4444";
    private static final String WEB_URL = "http://localhost:5555";

    private PaymentService testService;

    @Mock
    private Logger logger;
    @Mock
    private ApiClientService apiClientService;
    @Mock
    private InternalApiClient apiClient;
    @Mock
    private PaymentResourceHandler paymentResourceHandler;
    @Mock
    private PaymentCreate paymentCreate;
    @Mock
    private ApiResponse<PaymentApi> response;
    @Mock
    private PaymentApi paymentApi;

    private ArgumentCaptor<PaymentSessionApi> sessionCaptor;

    @BeforeEach
    void setUp() {
        testService = new PaymentServiceImpl(apiClientService, API_URL, WEB_URL, logger);

        when(apiClientService.getApiClient()).thenReturn(apiClient);
        when(apiClient.payment()).thenReturn(paymentResourceHandler);

        when(paymentResourceHandler.create(eq("/payments"), any(PaymentSessionApi.class))).thenReturn(paymentCreate);

        sessionCaptor = ArgumentCaptor.forClass(PaymentSessionApi.class);
    }

    @Test
    void createPaymentSession() throws ServiceException, ApiErrorResponseException, URIValidationException {
        when(paymentCreate.execute()).thenReturn(response);
        when(response.getData()).thenReturn(paymentApi);

        final String paymentSessionUrl = "http://pay.chs-dev.internal:4044";
        final Map<String, String> linksMap = Collections.singletonMap("journey", paymentSessionUrl);

        when(paymentApi.getLinks()).thenReturn(linksMap);

        final String location = testService.createPaymentSession(SUB_ID, COMPANY_NUMBER, PAYMENT_SESSION_STATE);

        assertThat(location, is(paymentSessionUrl));
        verify(paymentResourceHandler).create(eq("/payments"), sessionCaptor.capture());

        final PaymentSessionApi paymentSessionApi = createStubPaymentSessionApi();

        assertThat(sessionCaptor.getValue().getRedirectUri(), is(paymentSessionApi.getRedirectUri()));
        assertThat(sessionCaptor.getValue().getResource(), is(paymentSessionApi.getResource()));
        assertThat(sessionCaptor.getValue().getReference(), is(paymentSessionApi.getReference()));
        assertThat(sessionCaptor.getValue().getState(), is(paymentSessionApi.getState()));
    }

    @Test
    void createPaymentSessionWhenApiError() throws ApiErrorResponseException, URIValidationException {
        when(paymentCreate.execute()).thenThrow(ApiErrorResponseException.fromIOException(new IOException()));

        assertThrows(ServiceException.class, () -> {
            testService.createPaymentSession(SUB_ID, COMPANY_NUMBER, PAYMENT_SESSION_STATE);
        });
    }

    @Test
    void createPaymentSessionWhenUriError() throws ApiErrorResponseException, URIValidationException {
        when(paymentCreate.execute()).thenThrow(new URIValidationException("failed", new RuntimeException()));

        assertThrows(ServiceException.class, () -> {
            testService.createPaymentSession(SUB_ID, COMPANY_NUMBER, PAYMENT_SESSION_STATE);
        });
    }

    @Test
    void createPaymentSessionWhenValidationError() throws ApiErrorResponseException, URIValidationException {
        when(paymentCreate.execute()).thenReturn(new ApiResponse<>(Collections.singletonList(new ApiError())));

        assertThat(testService.createPaymentSession(SUB_ID, COMPANY_NUMBER, PAYMENT_SESSION_STATE), is(nullValue()));
    }

    private PaymentSessionApi createStubPaymentSessionApi() {
        PaymentSessionApi api = new PaymentSessionApi();

        api.setRedirectUri(MessageFormat
            .format("{0}{1}/{2}/company/{3}/payment-complete-callback", WEB_URL, BaseControllerImpl.SERVICE_URI, SUB_ID, COMPANY_NUMBER));
        api.setResource(
            MessageFormat.format("{0}/efs-submission-api/submission/{1}/payment", API_URL, SUB_ID));
        api.setReference(SUB_ID);
        api.setState(PAYMENT_SESSION_STATE);

        return api;
    }
}