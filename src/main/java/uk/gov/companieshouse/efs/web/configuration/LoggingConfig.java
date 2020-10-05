package uk.gov.companieshouse.efs.web.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import uk.gov.companieshouse.efs.web.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * Configuration class for logging.
 */
@Configuration
@PropertySource("classpath:logger.properties")
public class LoggingConfig {

    @Value("${logger.namespace}")
    private String loggerNamespace;

    /**
     * Creates a logger with specified namespace.
     *
     * @return the {@link LoggerFactory} for the specified namespace
     */
    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger(loggerNamespace);
    }

    /**
     * Creates a loggingInterceptor with specified logger.
     *
     * @param logger sets a structured logging logger
     * @return new {@link LoggingInterceptor} to enable logging of the request
     */
    @Bean("loggingInterceptorBean")
    public LoggingInterceptor loggingInterceptor(Logger logger) {
        return new LoggingInterceptor(logger);
    }
}