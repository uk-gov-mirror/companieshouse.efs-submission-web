package uk.gov.companieshouse.efs.web.payment.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import org.springframework.stereotype.Service;

/**
 * Generator for a cryptographically strong nonce string.
 * A nonce is a random arbitrary character sequence that may only be used once.
 */
@Service
public final class NonceServiceImpl implements NonceService {

    public static final int NONCE_BASE64_LEN = 64;
    public static final int NONCE_BYTE_LEN = 48;

    private SecureRandom secureRandom;

    private void secureInit() {
        // to properly initialize the PRNG it needs to be used once
        final byte[] ar = new byte[NONCE_BASE64_LEN];
        secureRandom.nextBytes(ar);
        Arrays.fill(ar, (byte) 0);
    }

    /**
     * Constructor used by {@link NonceServiceFactoryImpl}
     */
    NonceServiceImpl() {
        // intentionally blank
    }

    @Override
    public void setSecureRandom(final SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
        secureInit();
    }

    /**
     * Creates a random nonce as a character sequence of length 64.
     *
     * @return The nonce.
     */
    @Override
    public String generateBase64() {
        final byte[] ar = new byte[NONCE_BYTE_LEN];
        secureRandom.nextBytes(ar);
        final String nonce =
            new String(java.util.Base64.getUrlEncoder().withoutPadding().encode(ar),
                StandardCharsets.UTF_8);
        Arrays.fill(ar, (byte) 0);
        return nonce;
    }

}