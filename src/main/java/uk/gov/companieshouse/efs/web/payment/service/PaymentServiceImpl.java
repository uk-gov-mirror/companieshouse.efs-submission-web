package uk.gov.companieshouse.efs.web.payment.service;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.api.model.payment.PaymentSessionApi;
import uk.gov.companieshouse.efs.web.controller.ViewConstants;
import uk.gov.companieshouse.efs.web.exception.ServiceException;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.efs.web.service.api.impl.BaseApiClientServiceImpl;
import uk.gov.companieshouse.logging.Logger;

/**
 * Create the information required for a payment session
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    private Logger logger;

    private ApiClientService apiClientService;

    private String apiServiceUrl;
    private String webServiceUrl;

    /**
     * Constructor sets the {@link ApiClientService}, logger and environment variables
     *
     * @param apiClientService the {@link ApiClientService} required for payment
     * @param logger           sets the logger
     */
    @Autowired
    public PaymentServiceImpl(ApiClientService apiClientService, @Value("${api.server.url}") String apiServiceUrl,
        @Value("${web.server.url}") String webServiceUrl, Logger logger) {
        this.apiClientService = apiClientService;
        this.apiServiceUrl = apiServiceUrl;
        this.webServiceUrl = webServiceUrl;
        setLogger(logger);
    }

    private void setLogger(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public String createPaymentSession(String submissionId, String companyNumber, String sessionState) {
        final String result;

        PaymentSessionApi paymentSessionApi = new PaymentSessionApi();
        paymentSessionApi
            .setRedirectUri(ViewConstants.PAYMENT_COMPLETE.asUri(webServiceUrl, submissionId, companyNumber));
//MessageFormat.format("http://internalapi.chs-dev.internal:4001/efs-submission-api/submission/{0}/payment", submissionId)
        paymentSessionApi
            .setResource(apiServiceUrl + getUrlWithId(BaseApiClientServiceImpl.SUB_URI + "{0}/payment", submissionId));
        paymentSessionApi.setReference(submissionId);
        paymentSessionApi.setState(sessionState);
        logPaymentSessionDetails(submissionId, paymentSessionApi);

        final ApiClient apiClient = apiClientService.getApiClient();

        try {
            final ApiResponse<PaymentApi> apiResponse =
                apiClient.payment().create("/payments", paymentSessionApi).execute();
            String sessionUrl = null;

            if (apiResponse.hasErrors()) {
                logger.infoContext(submissionId, "Error(s) creating payment session: " + apiResponse.getErrors(), null);
            } else {
                sessionUrl = apiResponse.getData().getLinks().get("journey");
            }
            result = sessionUrl;
        } catch (ApiErrorResponseException e) {
            throw new ServiceException("Error creating payment session", e);
        } catch (URIValidationException e) {
            throw new ServiceException("Invalid URI for payment resource", e);
        }

        return result;
    }

    private void logPaymentSessionDetails(final String submissionId, final PaymentSessionApi paymentSessionApi) {
        Map<String, Object> map = new HashMap<>();

        map.put("redirect-uri=", paymentSessionApi.getRedirectUri());
        map.put("resource-uri=", paymentSessionApi.getResource());
        map.put("reference=", paymentSessionApi.getReference());
        map.put("state=", paymentSessionApi.getState());

        logger.infoContext(submissionId, "Create payment session:", map);
    }

    private String getUrlWithId(final String template, final String id) {
        return MessageFormat.format(template, id);
    }

}
