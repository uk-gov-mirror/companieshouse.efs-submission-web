package uk.gov.companieshouse.efs.web.security;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormTemplateValidatorTest {

    FormTemplateValidator testFormTemplateValidator;

    @Mock
    CategoryTemplateService categoryTemplateService;

    ValidatorResourceProvider resourceProvider;

    @Mock
    HttpServletRequest request;

    @Mock
    Validator nextValidator;

    @Mock
    FormTemplateService formTemplateService;

    @Mock
    SubmissionApi submission;

    @Mock
    FormTemplateApi formTemplate;

    @BeforeEach
    void setUp() {
        resourceProvider = spy(new ValidatorResourceProvider(request, null, formTemplateService));

        testFormTemplateValidator = spy(new FormTemplateValidator(
                resourceProvider));
    }

    @Test
    void falseWhenNoForm() {
        ReflectionTestUtils.setField(resourceProvider, "submission", submission);
        when(submission.getSubmissionForm()).thenReturn(null);

        testFormTemplateValidator.validate();
        verify(nextValidator, never()).validate();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void requiresAuthWhenFormRequiresAuth(boolean formRequiresAuth) {
        ReflectionTestUtils.setField(resourceProvider, "form", formTemplate);
        when(formTemplate.isAuthenticationRequired()).thenReturn(formRequiresAuth);

        boolean requiresAuth = testFormTemplateValidator.validate();
        assertEquals(formRequiresAuth, requiresAuth);
    }
}