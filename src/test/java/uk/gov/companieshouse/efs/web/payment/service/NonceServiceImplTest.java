package uk.gov.companieshouse.efs.web.payment.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import java.security.SecureRandom;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NonceServiceImplTest {
    private NonceServiceImpl testService;

    @Mock
    private SecureRandom secureRandom;

    private byte[] bytes;

    @BeforeEach
    void setUp() {
        testService = new NonceServiceImpl();
        bytes = new byte[NonceServiceImpl.NONCE_BASE64_LEN];
    }

    @Test
    void setSecureRandom() {

        testService.setSecureRandom(secureRandom);

        verify(secureRandom).nextBytes(bytes);
    }

    @Test
    void generateBase64() {
        testService.setSecureRandom(secureRandom);

        final String expected = StringUtils.repeat("QUFB", 16);

        doAnswer(invocation -> {
            byte[] b = invocation.getArgument(0);
            Arrays.fill(b, (byte) 'A');

            return null;
        }).when(secureRandom).nextBytes(any(byte[].class));

        final String nonce = testService.generateBase64();

        verify(secureRandom).nextBytes(bytes);
        assertThat(nonce, is(expected));
    }

}