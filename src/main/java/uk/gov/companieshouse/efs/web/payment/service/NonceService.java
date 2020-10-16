package uk.gov.companieshouse.efs.web.payment.service;

import java.security.SecureRandom;

/**
 * Interface to generate a cryptographically strong nonce string.
 * nonce is a random arbitrary character sequence that may only be used once.
 */
public interface NonceService {
    void setSecureRandom(SecureRandom secureRandom);

    /**
     * Creates a random nonce as a character sequence of length 64.
     *
     * @return The nonce.
     */
    String generateBase64();
}
