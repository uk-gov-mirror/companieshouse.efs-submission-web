package uk.gov.companieshouse.efs.web.configuration;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ResourceBundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.efs.web.payment.service.NonceService;
import uk.gov.companieshouse.efs.web.payment.service.NonceServiceFactoryImpl;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class SpringWebConfigTest {

    public static final String RNG_ALGORITHM = "SHA1PRNG";
    public static final String RNG_PROVIDER = "SUN";
    @Spy
    private final SpringWebConfig testConfig = new SpringWebConfig();

    @Mock
    private Logger logger;

    @Mock
    private SecureRandom secureRandom;

    @Test
    void environmentReader() {
        assertThat(testConfig.environmentReader(), isA(EnvironmentReader.class));
    }

    @Test
    void nonceServiceFactory() {
        assertThat(testConfig.nonceServiceFactory(), isA(NonceServiceFactoryImpl.class));
    }

    @Test
    void nonceService() throws Exception {
        when(testConfig.algorithm()).thenReturn(RNG_ALGORITHM);
        when(testConfig.provider()).thenReturn(RNG_PROVIDER);
        when(testConfig.secureRandom(RNG_ALGORITHM, RNG_PROVIDER)).thenReturn(secureRandom);

        assertThat(testConfig.nonceService(), isA(NonceService.class));
    }

    @Test
    void secureRandom() throws NoSuchProviderException, NoSuchAlgorithmException {
        final SecureRandom random = testConfig.secureRandom(RNG_ALGORITHM, RNG_PROVIDER);

        assertThat(random.getAlgorithm(), is(RNG_ALGORITHM));
        assertThat(random.getProvider().getName(), startsWith(RNG_PROVIDER));
    }

    @Test
    void messageSource() {
        final ResourceBundleMessageSource messageSource = testConfig.messageSource();

        assertThat(messageSource.getBasenameSet(), contains("messages"));
    }

    @Test
    void resourceBundle() {
        final ResourceBundle bundle = testConfig.resourceBundle();

        assertThat(bundle.getBaseBundleName(), is("messages"));
    }

    @Test
    void getValidator() {
        final LocalValidatorFactoryBean validator = testConfig.getValidator();

        assertThat(validator, isA(LocalValidatorFactoryBean.class));
    }

    @Test
    void responseErrorHandler() {
        assertThat(testConfig.responseErrorHandler(), isA(ResponseErrorHandler.class));
    }

    @Test
    void restTemplate() {
        final DefaultResponseErrorHandler handler = new DefaultResponseErrorHandler();
        final RestTemplate template = testConfig.restTemplate(handler);

        assertThat(template.getErrorHandler(), is(sameInstance(handler)));
    }
}
