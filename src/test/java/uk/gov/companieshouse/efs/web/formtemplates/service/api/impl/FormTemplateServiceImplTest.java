package uk.gov.companieshouse.efs.web.formtemplates.service.api.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTemplateTestFixture.CAT_TOP_LEVEL;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.efs.PrivateEfsResourceHandler;
import uk.gov.companieshouse.api.handler.efs.formtemplates.PrivateEfsFormTemplatesResourceHandler;
import uk.gov.companieshouse.api.handler.efs.formtemplates.request.PrivateEfsFormTemplateGetResourceHandler;
import uk.gov.companieshouse.api.handler.efs.formtemplates.request.PrivateEfsFormTemplatesGetByCategoryResourceHandler;
import uk.gov.companieshouse.api.handler.efs.formtemplates.request.PrivateEfsFormTemplatesGetResourceHandler;
import uk.gov.companieshouse.api.handler.efs.formtemplates.request.PrivateFormTemplateGet;
import uk.gov.companieshouse.api.handler.efs.formtemplates.request.PrivateFormTemplateListGet;
import uk.gov.companieshouse.api.handler.efs.formtemplates.request.PrivateFormTemplateListGetByCategory;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateListApi;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.efs.web.service.api.impl.BaseApiClientServiceImpl;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class FormTemplateServiceImplTest {

    private FormTemplateService testService;

    @Mock
    private ApiClientService apiClientService;
    @Mock
    private Logger logger;
    @Mock
    private InternalApiClient privateApiClient;
    @Mock
    private PrivateEfsResourceHandler resourceHandler;
    @Mock
    private PrivateEfsFormTemplatesResourceHandler formTemplatesResourceHandler;
    @Mock
    private PrivateEfsFormTemplatesGetResourceHandler formTemplatesGetResourceHandler;
    @Mock
    private PrivateFormTemplateListGet formTemplatesGet;
    @Mock
    private PrivateEfsFormTemplateGetResourceHandler formTemplateGetResourceHandler;
    @Mock
    private PrivateFormTemplateGet formTemplateGet;
    @Mock
    private PrivateEfsFormTemplatesGetByCategoryResourceHandler formTemplatesGetByCategoryResourceHandler;
    @Mock
    private PrivateFormTemplateListGetByCategory formTemplatesGetByCategory;

    FormTemplateApi formTemplateApi = new FormTemplateApi("CC01", "Test01", "CC03", "CC01", true, true, null);
    FormTemplateApi formTemplateApi2 = new FormTemplateApi("O/C STAY", "O/C STAY", "LIQ", "", true, true, null);
    FormTemplateApi formTemplateApi3 = new FormTemplateApi("O&C STAY", "O&C STAY", "LIQ", "", true, true, null);

    @BeforeEach
    void setUp() {
        testService = new FormTemplateServiceImpl(apiClientService, logger);

        when(apiClientService.getApiClient()).thenReturn(privateApiClient);
        when(privateApiClient.privateEfsResourceHandler()).thenReturn(resourceHandler);
    }

    @Test
    void getFormTemplates() throws ApiErrorResponseException, URIValidationException {
        final ApiResponse<FormTemplateListApi> expected = buildApiResponseOK(
                new FormTemplateListApi(Arrays.asList(formTemplateApi)));

        when(resourceHandler.formTemplates()).thenReturn(formTemplatesResourceHandler);
        when(formTemplatesResourceHandler.formTemplates()).thenReturn(
                formTemplatesGetResourceHandler);
        when(formTemplatesGetResourceHandler.get(BaseApiClientServiceImpl.ROOT_URI
                + FormTemplateServiceImpl.FORM_TEMPLATES_FRAGMENT)).thenReturn(
                formTemplatesGet);
        when(formTemplatesGet.execute()).thenReturn(expected);

        final ApiResponse<FormTemplateListApi> result = testService.getFormTemplates();

        assertThat(result, is(expected));
    }

    @Test
    void getFormTemplate() throws ApiErrorResponseException, URIValidationException {

        final ApiResponse<FormTemplateApi> expected = buildApiResponseOK(
                formTemplateApi);

        when(resourceHandler.formTemplates()).thenReturn(formTemplatesResourceHandler);
        when(formTemplatesResourceHandler.formTemplate()).thenReturn(
                formTemplateGetResourceHandler);
        when(formTemplateGetResourceHandler.get(
            BaseApiClientServiceImpl.ROOT_URI + FormTemplateServiceImpl.FORM_TEMPLATE_FRAGMENT + "?type="
                + formTemplateApi.getFormType())).thenReturn(formTemplateGet);
        when(formTemplateGet.execute()).thenReturn(expected);

        final ApiResponse<FormTemplateApi> result = testService.getFormTemplate(formTemplateApi.getFormType());

        assertThat(result, is(expected));
    }

    @Test
    void getFormTemplateContainingSlash() throws ApiErrorResponseException, URIValidationException {

        final ApiResponse<FormTemplateApi> expected = buildApiResponseOK(formTemplateApi2);

        when(resourceHandler.formTemplates()).thenReturn(formTemplatesResourceHandler);
        when(formTemplatesResourceHandler.formTemplate()).thenReturn(formTemplateGetResourceHandler);
        when(formTemplateGetResourceHandler.get(
            BaseApiClientServiceImpl.ROOT_URI + FormTemplateServiceImpl.FORM_TEMPLATE_FRAGMENT + "?type=O/C%20STAY"))
            .thenReturn(formTemplateGet);
        when(formTemplateGet.execute()).thenReturn(expected);

        final ApiResponse<FormTemplateApi> result = testService.getFormTemplate(formTemplateApi2.getFormType());

        assertThat(result, is(expected));
    }

    @Test
    void getFormTemplateContainingAmpersand() throws ApiErrorResponseException, URIValidationException {

        final ApiResponse<FormTemplateApi> expected = buildApiResponseOK(formTemplateApi3);

        when(resourceHandler.formTemplates()).thenReturn(formTemplatesResourceHandler);
        when(formTemplatesResourceHandler.formTemplate()).thenReturn(formTemplateGetResourceHandler);
        when(formTemplateGetResourceHandler.get(
            BaseApiClientServiceImpl.ROOT_URI + FormTemplateServiceImpl.FORM_TEMPLATE_FRAGMENT + "?type=O%26C%20STAY"))
            .thenReturn(formTemplateGet);
        when(formTemplateGet.execute()).thenReturn(expected);

        final ApiResponse<FormTemplateApi> result = testService.getFormTemplate(formTemplateApi3.getFormType());

        assertThat(result, is(expected));
    }

    @Test
    void getFormTemplatesByCategory() throws ApiErrorResponseException, URIValidationException {
        final ApiResponse<FormTemplateListApi> expected =
            buildApiResponseOK(new FormTemplateListApi(Arrays.asList(formTemplateApi, formTemplateApi2)));

        when(resourceHandler.formTemplates()).thenReturn(formTemplatesResourceHandler);
        when(formTemplatesResourceHandler.formTemplatesByCategory())
            .thenReturn(formTemplatesGetByCategoryResourceHandler);
        when(formTemplatesGetByCategoryResourceHandler.get(BaseApiClientServiceImpl.ROOT_URI
                + FormTemplateServiceImpl.FORM_TEMPLATES_FRAGMENT
                + "?category="
                + CAT_TOP_LEVEL.getCategoryType())).thenReturn(formTemplatesGetByCategory);
        when(formTemplatesGetByCategory.execute()).thenReturn(expected);

        final ApiResponse<FormTemplateListApi> result =
                testService.getFormTemplatesByCategory(CAT_TOP_LEVEL.getCategoryType());

        assertThat(result, is(expected));
    }

    private <T> ApiResponse<T> buildApiResponseOK(final T data) {
        return new ApiResponse<T>(HttpStatus.OK.value(), Collections.emptyMap(), data);
    }
}