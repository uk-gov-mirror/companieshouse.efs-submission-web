package uk.gov.companieshouse.efs.web.payment.service;

/**
 * Payment service interface used to create the information required for a payment session
 */
public interface PaymentService {
    /**
     * Creates a payment session for customer to submit payment.
     *
     * @param submissionId The ID of the submission
     * @param companyNumber The company number
     * @param  sessionState The state nonce used for verification after payment journey completes
     * @return A URL to redirect customer's browser to the start of the journey
     */
    String createPaymentSession(final String submissionId, final String companyNumber, final String sessionState);
}
