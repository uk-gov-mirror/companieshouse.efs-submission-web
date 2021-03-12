package uk.gov.companieshouse.efs.web.payment.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.paymentsession.SessionApi;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
import uk.gov.companieshouse.efs.web.controller.BaseControllerImplTest;
import uk.gov.companieshouse.efs.web.controller.ViewConstants;
import uk.gov.companieshouse.efs.web.exception.ServiceException;
import uk.gov.companieshouse.efs.web.payment.service.NonceService;
import uk.gov.companieshouse.efs.web.payment.service.PaymentService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerImplTest extends BaseControllerImplTest {
    private static final String PAYMENT_SESSION_STATE =
        "E_lLgj6SI8cWoEXVtGMsuB81DoEcOiWPPgSJTz4OQ0gVo0y6d_NDFP7waRQfdU1z";
    private static final String PAYMENT_SESSION_ID = "yMxgdNVSdwyk7sN";
    private static final String PAYMENT_SESSION_URL = "http://localhost:5000/payments/" + PAYMENT_SESSION_ID + "/pay";
    private static final String PAYMENT_SESSION_URL_BAD = "http://localhost:5000/bad/" + PAYMENT_SESSION_ID + "/pay";

    private PaymentController testController;

    @Mock
    private ApiClientService apiClientService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private NonceService nonceService;

    @Mock
    private SubmissionApi submission;
    private SessionListApi paymentSessions;
    private SessionApi paySession;

    @BeforeEach
    public void setUp() {
        setUpHeaders();
        testController = new PaymentControllerImpl(apiClientService, paymentService, nonceService, logger);
        ((PaymentControllerImpl) testController).setChsUrl(CHS_URL);
        paymentSessions = new SessionListApi();
        paySession = new SessionApi(PAYMENT_SESSION_ID, PAYMENT_SESSION_STATE);

    }

    @Test
    void paymentWhenSessionUrlMatched() {

        when(nonceService.generateBase64()).thenReturn(PAYMENT_SESSION_STATE);
        when(paymentService.createPaymentSession(SUBMISSION_ID, COMPANY_NUMBER, PAYMENT_SESSION_STATE))
            .thenReturn(PAYMENT_SESSION_URL);
        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(getSubmissionOkResponse(submission));
        when(submission.getPaymentSessions()).thenReturn(paymentSessions);

        final String paymentCallbackUrl = testController.payment(SUBMISSION_ID, COMPANY_NUMBER, request);

        final SessionListApi expectedPaymentSessions = new SessionListApi(Collections.singletonList(paySession));

        verify(apiClientService).putPaymentSessions(SUBMISSION_ID, expectedPaymentSessions);
        assertThat(paymentCallbackUrl, is("redirect:" + PAYMENT_SESSION_URL));
    }

    @Test
    void paymentWhenSessionUrlNotMatched() {
        when(nonceService.generateBase64()).thenReturn(PAYMENT_SESSION_STATE);
        when(paymentService.createPaymentSession(SUBMISSION_ID, COMPANY_NUMBER, PAYMENT_SESSION_STATE))
            .thenReturn(PAYMENT_SESSION_URL_BAD);

        final String paymentCallbackUrl = testController.payment(SUBMISSION_ID, COMPANY_NUMBER, request);

        verifyNoInteractions(apiClientService, session);
        assertThat(paymentCallbackUrl, is("redirect:" + PAYMENT_SESSION_URL_BAD));
    }

    @Test
    void paymentCallbackWhenStatusPaidWithAndStateMatch() {
        paymentSessions.add(paySession);
        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(getSubmissionOkResponse(submission));
        when(submission.getPaymentSessions()).thenReturn(paymentSessions);

        final String result = testController
            .paymentCallback(request, SUBMISSION_ID, COMPANY_NUMBER, "paid", PAYMENT_SESSION_ID, PAYMENT_SESSION_STATE,
                servletRequest);

        assertThat(result, is(ViewConstants.CONFIRMATION.asRedirectUri(CHS_URL, SUBMISSION_ID, COMPANY_NUMBER)));
    }

    @Test
    void paymentCallbackWhenStatusPaidWithAndStateMismatch() {
        paySession.setSessionState("[won't match]");
        paymentSessions.add(paySession);
        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(getSubmissionOkResponse(submission));
        when(submission.getPaymentSessions()).thenReturn(paymentSessions);

        final ServiceException exception = assertThrows(ServiceException.class, () -> testController
            .paymentCallback(request, SUBMISSION_ID, COMPANY_NUMBER, "paid", PAYMENT_SESSION_ID, PAYMENT_SESSION_STATE,
                servletRequest));

        assertThat(exception.getMessage(), is("State does not match"));
    }

    @Test
    void paymentCallbackWhenStatusNotPaid() {
        paymentSessions.add(paySession);
        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(getSubmissionOkResponse(submission));
        when(submission.getPaymentSessions()).thenReturn(paymentSessions);

        final String result = testController
            .paymentCallback(request, SUBMISSION_ID, COMPANY_NUMBER, "failed", PAYMENT_SESSION_ID, PAYMENT_SESSION_STATE,
                servletRequest);

        assertThat(result, is(ViewConstants.CHECK_DETAILS.asRedirectUri(CHS_URL, SUBMISSION_ID, COMPANY_NUMBER)));
    }

}