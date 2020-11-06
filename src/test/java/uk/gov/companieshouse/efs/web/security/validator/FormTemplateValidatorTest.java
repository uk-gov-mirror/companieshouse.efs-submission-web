package uk.gov.companieshouse.efs.web.security.validator;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormTemplateValidatorTest {

    FormTemplateRequiredValidator testFormTemplateValidator;

    @Mock
    ValidatorResourceProvider resourceProvider;

    @Mock
    HttpServletRequest request;

    @Mock
    Validator<HttpServletRequest> nextValidator;

    @Mock
    FormTemplateApi formTemplate;

    @BeforeEach
    void setUp() {
        testFormTemplateValidator = spy(new FormTemplateRequiredValidator(
                resourceProvider));
    }

    @Test
    void falseWhenNoForm() {
        when(resourceProvider.getForm()).thenReturn(Optional.empty());

        testFormTemplateValidator.validate(request);
        verify(nextValidator, never()).validate(request);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void requiresAuthWhenFormRequiresAuth(boolean formRequiresAuth) {
        when(resourceProvider.getForm()).thenReturn(Optional.of(formTemplate));
        when(formTemplate.isAuthenticationRequired()).thenReturn(formRequiresAuth);

        boolean requiresAuth = testFormTemplateValidator.validate(request);
        assertEquals(formRequiresAuth, requiresAuth);
    }
}