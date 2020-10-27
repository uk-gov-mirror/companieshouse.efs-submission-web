package uk.gov.companieshouse.efs.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import uk.gov.companieshouse.auth.filter.HijackFilter;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.session.handler.SessionHandler;

/**
 * Customises web security.
 */
@EnableWebSecurity
public class WebApplicationSecurity {
    @Value("${start.page.url}")
    private String startPageUrl;
    @Value("${guidance.page.url}")
    private String guidancePageUrl;
    @Value("${accessibility.statement.page.url}")
    private String accessibilityStatementPageUrl;
    @Value("${chs.signout.redirect.path}")
    private String signoutRedirectPath;
    private ApiClientService apiClientService;
    private FormTemplateService formTemplateService;
    private CategoryTemplateService categoryTemplateService;
    private EnvironmentReader environmentReader;

    /**
     * Constructor.
     *
     * @param apiClientService              apiClient service
     * @param formTemplateService           formTemplate service
     * @param categoryTemplateService       categoryTemplate service
     */
    @Autowired
    public WebApplicationSecurity(
        final ApiClientService apiClientService, FormTemplateService formTemplateService,
        final CategoryTemplateService categoryTemplateService, final EnvironmentReader environmentReader) {
        this.apiClientService = apiClientService;
        this.formTemplateService = formTemplateService;
        this.categoryTemplateService = categoryTemplateService;
        this.environmentReader = environmentReader;
    }

    /**
     * static nested class for root level security.
     */
    @Configuration
    @Order(1)
    public class RootLevelSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(final HttpSecurity http) {
            http.antMatcher("/efs-submission");
        }
    }

    /**
     * static nested class for start page security.
     */
    @Configuration
    @Order(2)
    public class StartPageSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(final HttpSecurity http) {
            http.antMatcher(startPageUrl);
        }
    }

    /**
     * static nested class for accessibility statement page security.
     */
    @Configuration
    @Order(3)
    public class AccessibilityStatementPageSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(final HttpSecurity http) {
            http.antMatcher(accessibilityStatementPageUrl);
        }
    }

    /**
     * static nested class for guidance page security.
     */
    @Configuration
    @Order(4)
    public class GuidancePageSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(final HttpSecurity http) {
            http.antMatcher(guidancePageUrl);
        }
    }

    @Configuration
    @Order(5)
    public class CompanyAuthFilterSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            final LoggingAuthFilter authFilter = new LoggingAuthFilter(signoutRedirectPath);
            final CompanyAuthFilter companyAuthFilter =
                new CompanyAuthFilter(environmentReader, apiClientService, formTemplateService,
                    categoryTemplateService);

            http.antMatcher("/efs-submission/*/company/**")
                .addFilterBefore(new SessionHandler(), BasicAuthenticationFilter.class)
                .addFilterBefore(new HijackFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(authFilter, BasicAuthenticationFilter.class)
                .addFilterBefore(companyAuthFilter, BasicAuthenticationFilter.class);
        }
    }

    /**
     * static nested class for resource level security.
     */
    @Configuration
    @Order(6)
    public class EfsWebResourceFilterConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(final HttpSecurity http) {
            final LoggingAuthFilter authFilter = new LoggingAuthFilter(signoutRedirectPath);

            http.antMatcher("/efs-submission/**")
                .addFilterBefore(new SessionHandler(), BasicAuthenticationFilter.class)
                .addFilterBefore(new HijackFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(authFilter, BasicAuthenticationFilter.class);
        }
    }
}
