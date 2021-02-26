package uk.gov.companieshouse.efs.web.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.github.stefanbirkner.systemlambda.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import uk.gov.companieshouse.auth.filter.HijackFilter;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.session.handler.SessionHandler;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebApplicationSecurityTest {
    @Mock
    private ApiClientService apiClientService;
    @Mock
    private FormTemplateService formTemplateService;
    @Mock
    private CategoryTemplateService categoryTemplateService;
    @Mock
    private EnvironmentReader environmentReader;
    @Mock
    private HttpSecurity httpSecurity;

    static final String randomEncryptionKey = "3T3L6iAEFscijkJZnOK0bYu/pH9jZeJqC1j59ZROKu8=";

    @Test
    void rootLevelConfig() {
        final WebApplicationSecurity.RootLevelSecurityConfig testConfig =
            new WebApplicationSecurity.RootLevelSecurityConfig();

        testConfig.configure(httpSecurity);

        verify(httpSecurity).antMatcher("/efs-submission");
    }

    @Test
    void startPageConfig() {
        final WebApplicationSecurity.StartPageSecurityConfig testConfig =
            new WebApplicationSecurity.StartPageSecurityConfig("start page");

        testConfig.configure(httpSecurity);

        verify(httpSecurity).antMatcher("start page");
    }

    @Test
    void accessibilityPageConfig() {
        final WebApplicationSecurity.AccessibilityStatementPageSecurityConfig testConfig =
            new WebApplicationSecurity.AccessibilityStatementPageSecurityConfig("access page");

        testConfig.configure(httpSecurity);

        verify(httpSecurity).antMatcher("access page");
    }

    @Test
    void guidancePageConfig() {
        final WebApplicationSecurity.GuidancePageSecurityConfig testConfig =
            new WebApplicationSecurity.GuidancePageSecurityConfig("guidance page");

        testConfig.configure(httpSecurity);

        verify(httpSecurity).antMatcher("guidance page");
    }

    @Test
    void insolvencyGuidancePageConfig() {
        final WebApplicationSecurity.InsolvencyGuidancePageSecurityConfig testConfig =
            new WebApplicationSecurity.InsolvencyGuidancePageSecurityConfig("insolvency page");

        testConfig.configure(httpSecurity);

        verify(httpSecurity).antMatcher("insolvency page");
    }

    @Test
    void companyAuthFilterSecurityConfigTest() {

        final WebApplicationSecurity webApplicationSecurity = new WebApplicationSecurity(
                apiClientService, formTemplateService, categoryTemplateService, environmentReader);

        final WebApplicationSecurity.CompanyAuthFilterSecurityConfig testConfig =
                webApplicationSecurity.new CompanyAuthFilterSecurityConfig();

        when(httpSecurity.antMatcher(anyString())).thenReturn(httpSecurity);
        when(httpSecurity.addFilterBefore(any(), any())).thenReturn(httpSecurity);

        withLoggingAuthFilterEnvironment(() ->
                testConfig.configure(httpSecurity));

        verify(httpSecurity).antMatcher("/efs-submission/*/company/**");
        verify(httpSecurity)
                .addFilterBefore(any(SessionHandler.class), eq(BasicAuthenticationFilter.class));
        verify(httpSecurity)
                .addFilterBefore(any(HijackFilter.class), eq(BasicAuthenticationFilter.class));
        verify(httpSecurity)
                .addFilterBefore(any(LoggingAuthFilter.class), eq(BasicAuthenticationFilter.class));
        verify(httpSecurity)
                .addFilterBefore(any(CompanyAuthFilter.class), eq(BasicAuthenticationFilter.class));
    }

    void withLoggingAuthFilterEnvironment(Statement callback) {
        try {
            withEnvironmentVariable("OAUTH2_REQUEST_KEY", randomEncryptionKey)
                    .and("OAUTH2_AUTH_URI", "oauth2_auth_uri")
                    .and("OAUTH2_CLIENT_ID", "oauth_client_id")
                    .and("OAUTH2_REDIRECT_URI", "oauth2_redirect_uri")
                    .and("COOKIE_SECRET", "cookie_secret")
                    .and("USE_FINE_GRAIN_SCOPES_MODEL", "user_fine_grained_scope")
                    .and("COOKIE_NAME", "a")
                    .and("COOKIE_DOMAIN", "a")
                    .and("COOKIE_SECURE_ONLY", "a")
                    .execute(callback);
        } catch (Exception e) {
            throw new RuntimeException("Exception while creating logging auth filter environment", e);
        }
    }
}