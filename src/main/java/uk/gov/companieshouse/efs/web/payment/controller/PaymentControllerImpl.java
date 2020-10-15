package uk.gov.companieshouse.efs.web.payment.controller;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.paymentsession.SessionApi;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
import uk.gov.companieshouse.efs.web.controller.BaseControllerImpl;
import uk.gov.companieshouse.efs.web.controller.ViewConstants;
import uk.gov.companieshouse.efs.web.exception.ServiceException;
import uk.gov.companieshouse.efs.web.payment.service.NonceService;
import uk.gov.companieshouse.efs.web.payment.service.PaymentService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.efs.web.service.api.impl.BaseApiClientServiceImpl;
import uk.gov.companieshouse.logging.Logger;

@Controller
public class PaymentControllerImpl extends BaseControllerImpl implements PaymentController {
    private static final Pattern PAYMENT_SESSION_URL_REGEX = Pattern.compile("/payments/([A-Za-z0-9]{15}+)/pay$");

    private PaymentService paymentService;
    private NonceService nonceService;

    /**
     * Constructor sets the service bean dependencies.
     *
     * @param paymentService the payment service bean
     * @param nonceService   the nonce service bean
     */
    @Autowired
    public PaymentControllerImpl(final ApiClientService apiClientService, final PaymentService paymentService, final NonceService nonceService, final Logger logger) {
        super(logger, null, apiClientService);
        this.paymentService = paymentService;
        this.nonceService = nonceService;
    }

    @Override
    public String payment(@PathVariable String id, final String companyNumber, HttpServletRequest request) {
        final String sessionState = nonceService.generateBase64();
        final String callback = UrlBasedViewResolver.REDIRECT_URL_PREFIX + paymentService
            .createPaymentSession(id, companyNumber, sessionState);

        logger.infoContext(id, "Redirect callback=" + callback, null);
        if (createPaymentSession(id, callback, sessionState)) {
            logger.infoContext(id, "Payment session saved ", null);
        } else {
            final ServiceException sessionServiceException = new ServiceException("Invalid payment session ID");
            logger.errorContext(id, "Payment session not saved due to invalid payment session ID " + sessionState,
                sessionServiceException, null);
        }

        return callback;
    }

    @Override
    public String paymentCallback(final HttpServletRequest request, @PathVariable String id,
        @PathVariable String companyNumber, @RequestParam(name = "status") String status,
        @RequestParam(name = "ref") String ref, @RequestParam(name = "state") String state, ServletRequest servletRequest) {
        ApiResponse<SubmissionApi> response = apiClientService.getSubmission(id);

        logApiResponse(response, id, MessageFormat.format("GET " + BaseApiClientServiceImpl.SUB_URI + "{0}", id));

        SessionListApi paymentSessions = Objects.requireNonNull(response.getData()).getPaymentSessions();

        if (StringUtils.equals(status, "paid")) {
            if (paymentSessions.stream().anyMatch(s -> StringUtils.equals(s.getSessionState(), state))) {
                return ViewConstants.CONFIRMATION.asRedirectUri(chsUrl, id, companyNumber);
            } else {
                final ServiceException stateServiceException = new ServiceException("State does not match");
                logger.errorContext(id, null, stateServiceException, null);
                throw stateServiceException;
            }
        } else {
            return ViewConstants.CHECK_DETAILS.asRedirectUri(chsUrl, id, companyNumber);
        }
    }

    private boolean createPaymentSession(final String submissionId, String callback, final String sessionState) {
        boolean created = false;

        Matcher matcher = PAYMENT_SESSION_URL_REGEX.matcher(callback);
        if (matcher.find()) {

            SessionApi paymentSession = new SessionApi(matcher.group(1), sessionState);
            SessionListApi paymentSessions = new SessionListApi();

            Optional.ofNullable(getSubmission(submissionId).getPaymentSessions()).ifPresent(paymentSessions::addAll);
            paymentSessions.add(paymentSession);

            final ApiResponse<SubmissionResponseApi> response =
                apiClientService.putPaymentSessions(submissionId, new SessionListApi(paymentSessions));

            logApiResponse(response, submissionId,
                MessageFormat.format("PUT /efs-submission-api/submission/{0}/payment-sessions", submissionId));
            created = true;
        }

        return created;
    }


}
