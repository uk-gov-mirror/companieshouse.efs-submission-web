package uk.gov.companieshouse.efs.web;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import uk.gov.companieshouse.efs.web.controller.BaseControllerImplTest;
import uk.gov.companieshouse.efs.web.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.efs.web.interceptor.UserDetailsInterceptor;

@ExtendWith(MockitoExtension.class)
class EfsWebApplicationTest {

    private EfsWebApplication testApp;
    @Mock
    private UserDetailsInterceptor userDetailsInterceptor;
    @Mock
    private LoggingInterceptor loggingInterceptor;
    @Mock
    private InterceptorRegistry registry;
    @Mock
    private InterceptorRegistration interceptorRegistration;

    @BeforeEach
    void setup() {
        testApp = new EfsWebApplication(
            userDetailsInterceptor,
            loggingInterceptor,
            "/efs-submission/start",
            "/efs-submission/guidance",
            "/efs-submission/insolvency-guidance",
            "/efs-submission/accessibility-statement"
            );
    }

    @Test
    void testInterceptors() {
        doReturn(interceptorRegistration).when(registry).addInterceptor(userDetailsInterceptor);
        doReturn(interceptorRegistration).when(registry).addInterceptor(loggingInterceptor);

        testApp.addInterceptors(registry);

        verify(registry).addInterceptor(userDetailsInterceptor);
        verify(interceptorRegistration)
            .excludePathPatterns("/efs-submission/start", "/efs-submission/guidance",
                "/efs-submission/insolvency-guidance", "/efs-submission/accessibility-statement");
    }
}