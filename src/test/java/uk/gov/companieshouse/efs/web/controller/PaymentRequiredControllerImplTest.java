package uk.gov.companieshouse.efs.web.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.submissions.PaymentReferenceApi;
import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.web.model.PaymentRequiredModel;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
class PaymentRequiredControllerImplTest extends BaseControllerImplTest {

    private static final String SIGNED_IN_USER = "test@ch.gov.uk";

    private PaymentRequiredController paymentRequiredController;

    @Mock
    private PaymentRequiredModel paymentRequiredAttribute;

    @Mock
    private ApiClientService apiClientService;

    @BeforeEach
    protected void setup() {
        super.setUp();
        paymentRequiredController = new PaymentRequiredControllerImpl(logger, sessionService, apiClientService,
                formTemplateService, categoryTemplateService, paymentRequiredAttribute);
        ((PaymentRequiredControllerImpl) paymentRequiredController).setChsUrl(CHS_URL);

        mockMvc = MockMvcBuilders.standaloneSetup(paymentRequiredController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getPaymentRequiredAttribute() {
        assertThat(((PaymentRequiredControllerImpl) paymentRequiredController).getPaymentRequiredAttribute(),
            is(sameInstance(paymentRequiredAttribute)));
    }

    @Test
    void getViewName() {
        Assert.assertThat(((PaymentRequiredControllerImpl) paymentRequiredController).getViewName(),
            is(ViewConstants.PAYMENT_REQUIRED.asView()));
    }

    @Test
    void getPaymentReference() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);

        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, SUBMISSION_ID);

        submission.setPresenter(new PresenterApi(SIGNED_IN_USER));

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
            getSubmissionOkResponse(submission));
        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn(SIGNED_IN_USER);
        when(formTemplateAttribute.getFee()).thenReturn("100");

        final String result = paymentRequiredController.getPaymentReference(SUBMISSION_ID, COMPANY_NUMBER,
            paymentRequiredAttribute, formTemplateAttribute, model, request, session);

        assertThat(result, is(ViewConstants.PAYMENT_REQUIRED.asView()));
    }

    @Test
    void getPaymentReferenceWithNoFee() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);

        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, SUBMISSION_ID);

        submission.setPresenter(new PresenterApi(SIGNED_IN_USER));

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
            getSubmissionOkResponse(submission));
        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn(SIGNED_IN_USER);
        when(formTemplateAttribute.getFee()).thenReturn("");

        final String result = paymentRequiredController.getPaymentReference(SUBMISSION_ID, COMPANY_NUMBER,
            paymentRequiredAttribute, formTemplateAttribute, model, request, session);

        assertThat(result, is(ViewConstants.CHECK_DETAILS.asRedirectUri(CHS_URL, SUBMISSION_ID, COMPANY_NUMBER)));
    }

    @Test
    void getPaymentReferenceWhenFeeIsInvalid() throws Exception {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);

        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, SUBMISSION_ID);

        submission.setPresenter(new PresenterApi(SIGNED_IN_USER));

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
                getSubmissionOkResponse(submission));
        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn(SIGNED_IN_USER);
        when(formTemplateAttribute.getFee()).thenReturn("not a number");

        String url = String.format("/efs-submission/%s/company/%s/payment-required", SUBMISSION_ID, COMPANY_NUMBER);
        mockMvc.perform(get(url).flashAttr("formTemplate", formTemplateAttribute))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name(ViewConstants.ERROR.asView()))
                .andReturn();
    }

    @Test
    void getPaymentReferenceWithDifferentSubmissionId() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);

        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, "incorrect-submission-id");

        submission.setPresenter(new PresenterApi(SIGNED_IN_USER));

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
            getSubmissionOkResponse(submission));
        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn(SIGNED_IN_USER);

        final String result = paymentRequiredController.getPaymentReference(SUBMISSION_ID, COMPANY_NUMBER,
            paymentRequiredAttribute, formTemplateAttribute, model, request, session);

        assertThat(result, is(ViewConstants.ERROR.asView()));
    }

    @Test
    void getPaymentReferenceWithDifferentSignedInUser() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);

        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, SUBMISSION_ID);

        submission.setPresenter(new PresenterApi("incorrect@email.com"));

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
            getSubmissionOkResponse(submission));
        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn(SIGNED_IN_USER);

        final String result = paymentRequiredController.getPaymentReference(SUBMISSION_ID, COMPANY_NUMBER,
            paymentRequiredAttribute, formTemplateAttribute, model, request, session);

        assertThat(result, is(ViewConstants.ERROR.asView()));
    }

    @Test
    void getPaymentReferenceWhenSubmissionIsNotOpen() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.SUBMITTED);

        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, SUBMISSION_ID);

        submission.setPresenter(new PresenterApi(SIGNED_IN_USER));

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
            getSubmissionOkResponse(submission));
        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn(SIGNED_IN_USER);

        final String result = paymentRequiredController.getPaymentReference(SUBMISSION_ID, COMPANY_NUMBER,
            paymentRequiredAttribute, formTemplateAttribute, model, servletRequest, session);

        Assert.assertThat(result, is(ViewConstants.GONE.asView()));
    }


    @Test
    void postPaymentReferenceWhenPaymentReferenceIsMissing() {
        when(bindingResult.hasErrors()).thenReturn(true);

        final String result = paymentRequiredController.postPaymentReference(SUBMISSION_ID, COMPANY_NUMBER,
            paymentRequiredAttribute, bindingResult, model, request, session);

        verify(model).addAttribute(TEMPLATE_NAME, ViewConstants.PAYMENT_REQUIRED.asView());
        assertThat(result, is(ViewConstants.PAYMENT_REQUIRED.asView()));
    }

    @Test
    void postPaymentReference() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(apiClientService.putPayment(SUBMISSION_ID, new PaymentReferenceApi())).thenReturn(
            new ApiResponse<>(200, getHeaders(), new SubmissionResponseApi(SUBMISSION_ID)));

        final String result = paymentRequiredController.postPaymentReference(SUBMISSION_ID, COMPANY_NUMBER,
            paymentRequiredAttribute, bindingResult, model, request, session);

        assertThat(result, is(ViewConstants.CHECK_DETAILS.asRedirectUri(CHS_URL, SUBMISSION_ID, COMPANY_NUMBER)));
    }

    @Test
    void postPaymentReferenceWhenRuntimeException() throws Exception {
        when(apiClientService.putPayment(SUBMISSION_ID, new PaymentReferenceApi())).thenThrow(new RuntimeException());

        String url = String.format("/efs-submission/%s/company/%s/payment-required", SUBMISSION_ID, COMPANY_NUMBER);
        mockMvc.perform(post(url))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name(ViewConstants.ERROR.asView()))
                .andReturn();
    }
}