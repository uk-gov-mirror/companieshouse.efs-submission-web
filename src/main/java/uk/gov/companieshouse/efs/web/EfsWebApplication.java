package uk.gov.companieshouse.efs.web;

import static org.springframework.boot.SpringApplication.run;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.efs.web.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.efs.web.interceptor.UserDetailsInterceptor;

@SpringBootApplication
public class EfsWebApplication implements WebMvcConfigurer {

    private UserDetailsInterceptor userDetailsInterceptor;
    private LoggingInterceptor loggingInterceptor;
    private final String startPageUrl;
    private final String guidancePageUrl;
    private final String insolvencyGuidancePageUrl;
    private final String accessibilityStatementPageUrl;

    /**
     * Constructor for EfsWebApplication.
     *
     * @param  userDetailsInterceptor responsible for validating a user is authenticated
     * @param loggingInterceptor responsible for logging the start and end of the requests
     */
    @Autowired
    public EfsWebApplication(UserDetailsInterceptor userDetailsInterceptor, LoggingInterceptor loggingInterceptor,
        @Value("${start.page.url}") final String startPageUrl,
        @Value("${guidance.page.url}") final String guidancePageUrl,
        @Value("${insolvency.guidance.page.url}") final String insolvencyGuidancePageUrl,
        @Value("${accessibility.statement.page.url}") final String accessibilityStatementPageUrl) {
        this.userDetailsInterceptor = userDetailsInterceptor;
        this.loggingInterceptor = loggingInterceptor;
        this.startPageUrl = startPageUrl;
        this.guidancePageUrl = guidancePageUrl;
        this.insolvencyGuidancePageUrl = insolvencyGuidancePageUrl;
        this.accessibilityStatementPageUrl = accessibilityStatementPageUrl;
    }

    /**
     * Adds interceptors for User Sign in.
     * But exclude initial start / (insolvency) guidance / contact-us pages because they don't need to be and will
     * get move out to gov.uk
     *
     * @param registry the Interceptor registry
     */
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor);
        registry.addInterceptor(userDetailsInterceptor)
                .excludePathPatterns(startPageUrl, guidancePageUrl, insolvencyGuidancePageUrl, accessibilityStatementPageUrl);
    }

    /**
     * Required spring boot application main method.
     *
     * @param args array of String arguments
     */
    public static void main(String[] args) {
        run(EfsWebApplication.class, args);
    }
}
