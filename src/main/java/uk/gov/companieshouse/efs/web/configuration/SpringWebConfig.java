package uk.gov.companieshouse.efs.web.configuration;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.ResourceBundle;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;

/**
 * Provides configuration for the web application.
 */
@SpringBootConfiguration
public class SpringWebConfig implements WebMvcConfigurer {

    @Value("${rng.algorithm}")
    private String algorithm;
    @Value("${rng.provider}")
    private String provider;

    /**
     * Manage the messages bundle required by models.
     *
     * @return the ResourceBundleMessageSource bean.
     */
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(false);
        messageSource.setFallbackToSystemLocale(true);

        return messageSource;
    }

    /**
     * Create a local validator factory bean.
     *
     * @return the LocalValidatorFactoryBean bean.
     */
    @Override
    @Bean
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource());
        return bean;
    }

    /**
     * Returns the environmentReader which contains the EFS web configuration.
     *
     * @return the EnvironmentReaderImpl() singleton bean
     */
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public EnvironmentReader environmentReader() {
        return new EnvironmentReaderImpl();
    }

    @Bean("algorithm")
    String algorithm() {
        return algorithm;
    }

    @Bean("provider")
    String provider() {
        return provider;
    }

    @Bean
    SecureRandom secureRandom(@Qualifier("algorithm") final String algorithm,
        @Qualifier("provider") final String provider) throws NoSuchProviderException, NoSuchAlgorithmException {
        return SecureRandom.getInstance(algorithm, provider);
    }

    @Bean
    ResponseErrorHandler responseErrorHandler() {
        return new DefaultResponseErrorHandler();
    }

    @Bean
    RestTemplate restTemplate(final ResponseErrorHandler handler) {
        final RestTemplate template = new RestTemplateBuilder().build();
        template.setErrorHandler(handler);

        return template;
    }

    @Bean
    ResourceBundle resourceBundle() {
        return ResourceBundle.getBundle("messages", Locale.UK);
    }

}
