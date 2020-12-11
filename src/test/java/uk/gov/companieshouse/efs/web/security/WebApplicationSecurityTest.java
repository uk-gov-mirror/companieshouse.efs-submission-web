package uk.gov.companieshouse.efs.web.security;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.environment.EnvironmentReader;

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

}