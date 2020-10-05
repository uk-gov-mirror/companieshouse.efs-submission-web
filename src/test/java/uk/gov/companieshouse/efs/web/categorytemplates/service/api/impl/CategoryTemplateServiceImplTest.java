package uk.gov.companieshouse.efs.web.categorytemplates.service.api.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTemplateControllerImplTest.CAT1_SUB_LEVEL1;
import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTemplateControllerImplTest.CAT2_SUB_LEVEL1;
import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTemplateControllerImplTest.CAT_TOP_LEVEL;
import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTemplateControllerImplTest.INSOLVENCY;
import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTemplateControllerImplTest.INS_SUB_LEVEL1;
import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTemplateControllerImplTest.INS_SUB_LEVEL2;
import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTemplateControllerImplTest.ROOT_LEVEL;
import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants.OTHER;
import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants.RESOLUTIONS;

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
import uk.gov.companieshouse.api.handler.efs.categorytemplates.PrivateEfsCategoryTemplatesResourceHandler;
import uk.gov.companieshouse.api.handler.efs.categorytemplates.request.PrivateCategoryTemplateGet;
import uk.gov.companieshouse.api.handler.efs.categorytemplates.request.PrivateCategoryTemplateListGet;
import uk.gov.companieshouse.api.handler.efs.categorytemplates.request.PrivateCategoryTemplateListGetByParent;
import uk.gov.companieshouse.api.handler.efs.categorytemplates.request.PrivateEfsCategoryTemplateGetResourceHandler;
import uk.gov.companieshouse.api.handler.efs.categorytemplates.request.PrivateEfsCategoryTemplatesGetByParentResourceHandler;
import uk.gov.companieshouse.api.handler.efs.categorytemplates.request.PrivateEfsCategoryTemplatesGetResourceHandler;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateListApi;
import uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class CategoryTemplateServiceImplTest {

    private CategoryTemplateService testService;

    @Mock
    private ApiClientService apiClientService;
    @Mock
    private Logger logger;
    @Mock
    private InternalApiClient privateApiClient;
    @Mock
    private PrivateEfsResourceHandler resourceHandler;
    @Mock
    private PrivateEfsCategoryTemplatesResourceHandler categoryTemplatesResourceHandler;
    @Mock
    private PrivateEfsCategoryTemplatesGetResourceHandler categoryTemplatesGetResourceHandler;
    @Mock
    private PrivateCategoryTemplateListGet categoryTemplatesGet;
    @Mock
    private PrivateEfsCategoryTemplateGetResourceHandler categoryTemplateGetResourceHandler;
    @Mock
    private PrivateCategoryTemplateGet categoryTemplateGet;
    @Mock
    private PrivateEfsCategoryTemplatesGetByParentResourceHandler categoryTemplatesGetByParentResourceHandler;
    @Mock
    private PrivateCategoryTemplateListGetByParent categoryTemplatesGetByParent;

    @BeforeEach
    void setUp() {
        testService = new CategoryTemplateServiceImpl(apiClientService, logger);

        when(apiClientService.getApiClient()).thenReturn(privateApiClient);
        when(privateApiClient.privateEfsResourceHandler()).thenReturn(resourceHandler);
    }

    @Test
    void getCategoryTemplates() throws ApiErrorResponseException, URIValidationException {
        final ApiResponse<CategoryTemplateListApi> expected = buildApiResponseOK(
            new CategoryTemplateListApi(
                Arrays.asList(CAT_TOP_LEVEL, CAT1_SUB_LEVEL1, CAT2_SUB_LEVEL1)));

        expectGetCategoryTemplates();
        when(categoryTemplatesGet.execute()).thenReturn(expected);

        final ApiResponse<CategoryTemplateListApi> result = testService.getCategoryTemplates();

        assertThat(result, is(expected));
    }

    @Test
    void getCategoryTemplate() throws ApiErrorResponseException, URIValidationException {
        final ApiResponse<CategoryTemplateApi> expected = buildApiResponseOK(CAT1_SUB_LEVEL1);

        expectGetCategoryTemplate();
        when(categoryTemplateGet.execute()).thenReturn(expected);

        final ApiResponse<CategoryTemplateApi> result = testService.getCategoryTemplate(
            CAT1_SUB_LEVEL1.getCategoryType());

        assertThat(result, is(expected));
    }

    @Test
    void getCategoryTemplatesByParent() throws ApiErrorResponseException, URIValidationException {
        final ApiResponse<CategoryTemplateListApi> expected = buildApiResponseOK(
            new CategoryTemplateListApi(Arrays.asList(CAT1_SUB_LEVEL1, CAT2_SUB_LEVEL1)));

        expectGetCategoryTemplateByParent();
        when(categoryTemplatesGetByParent.execute()).thenReturn(expected);

        final ApiResponse<CategoryTemplateListApi> result =
            testService.getCategoryTemplatesByParent(CAT_TOP_LEVEL.getCategoryType());

        assertThat(result, is(expected));
    }

    @Test
    void getTopLevelCategoryGivenRootCategory() throws ApiErrorResponseException, URIValidationException {
        final CategoryTemplateApi category = ROOT_LEVEL;

        expectGetCategoryTemplate();
        when(categoryTemplateGet.execute()).thenReturn(buildApiResponseOK(category));

        final CategoryTypeConstants topLevelCategory = testService.getTopLevelCategory(category.getCategoryType());

        assertThat(topLevelCategory.getValue(), is(ROOT_LEVEL.getCategoryType()));
    }

    @Test
    void getTopLevelCategoryGivenTopLevelNonInsolvencyCategory() throws ApiErrorResponseException, URIValidationException {
        final CategoryTemplateApi category = CAT_TOP_LEVEL;

        expectGetCategoryTemplate();
        when(categoryTemplateGet.execute()).thenReturn(buildApiResponseOK(category));

        final CategoryTypeConstants topLevelCategory = testService.getTopLevelCategory(category.getCategoryType());

        assertThat(topLevelCategory.getValue(), is(OTHER.getValue()));
    }

    @Test
    void getTopLevelCategoryGivenLevel1SubCategory() throws ApiErrorResponseException, URIValidationException {
        expectGetCategoryTemplate();
        when(categoryTemplateGet.execute()).thenReturn(buildApiResponseOK(CAT1_SUB_LEVEL1))
                .thenReturn(buildApiResponseOK(CAT_TOP_LEVEL));

        final CategoryTypeConstants topLevelCategory =
                testService.getTopLevelCategory(CAT1_SUB_LEVEL1.getCategoryType());

        assertThat(topLevelCategory.getValue(), is(OTHER.getValue()));
    }

    @Test
    void getTopLevelCategoryGivenResolutionsCategory() throws ApiErrorResponseException, URIValidationException {
        final CategoryTemplateApi category = new CategoryTemplateApi("RESOLUTIONS",
                "RESOLUTIONS", "", null);;

        expectGetCategoryTemplate();
        when(categoryTemplateGet.execute()).thenReturn(buildApiResponseOK(category));

        final CategoryTypeConstants topLevelCategory =
                testService.getTopLevelCategory(category.getCategoryType());

        assertThat(topLevelCategory.getValue(), is(RESOLUTIONS.getValue()));
    }

    @Test
    void getTopLevelCategoryGivenInsolvencyCategory() throws ApiErrorResponseException, URIValidationException {
        final CategoryTemplateApi category = INSOLVENCY;

        expectGetCategoryTemplate();
        when(categoryTemplateGet.execute()).thenReturn(buildApiResponseOK(category));

        final CategoryTypeConstants topLevelCategory =
                testService.getTopLevelCategory(category.getCategoryType());

        assertThat(topLevelCategory.getValue(), is(INSOLVENCY.getCategoryType()));
    }

    @Test
    void getTopLevelCategoryGivenInsolvencySubCategory() throws ApiErrorResponseException, URIValidationException {
        expectGetCategoryTemplate();
        when(categoryTemplateGet.execute()).thenReturn(buildApiResponseOK(INS_SUB_LEVEL1)).thenReturn(
                buildApiResponseOK(INSOLVENCY));

        final CategoryTypeConstants topLevelCategory =
                testService.getTopLevelCategory(INS_SUB_LEVEL1.getCategoryType());

        assertThat(topLevelCategory.getValue(), is(INSOLVENCY.getCategoryType()));
    }

    @Test
    void getTopLevelCategoryGivenInsolvencySubSubCategory() throws ApiErrorResponseException, URIValidationException {
        expectGetCategoryTemplate();
        when(categoryTemplateGet.execute()).thenReturn(buildApiResponseOK(INS_SUB_LEVEL2))
                .thenReturn(buildApiResponseOK(INS_SUB_LEVEL1)).thenReturn(
                buildApiResponseOK(INSOLVENCY));

        final CategoryTypeConstants topLevelCategory =
                testService.getTopLevelCategory(INS_SUB_LEVEL2.getCategoryType());

        assertThat(topLevelCategory.getValue(), is(INSOLVENCY.getCategoryType()));
    }

    private <T> ApiResponse<T> buildApiResponseOK(final T data) {
        return new ApiResponse<T>(HttpStatus.OK.value(), Collections.emptyMap(), data);
    }

    private void expectGetCategoryTemplate(
            final CategoryTemplateApi category) throws URIValidationException, ApiErrorResponseException {

        final ApiResponse<CategoryTemplateApi> expected = buildApiResponseOK(category);

        expectGetCategoryTemplate();
        when(categoryTemplateGet.execute()).thenReturn(expected);
    }

    private void expectGetCategoryTemplates() {
        when(resourceHandler.categoryTemplates()).thenReturn(categoryTemplatesResourceHandler);
        when(categoryTemplatesResourceHandler.categoryTemplates()).thenReturn(categoryTemplatesGetResourceHandler);
        when(categoryTemplatesGetResourceHandler.get(anyString())).thenReturn(categoryTemplatesGet);
    }

    private void expectGetCategoryTemplate() {
        when(resourceHandler.categoryTemplates()).thenReturn(categoryTemplatesResourceHandler);
        when(categoryTemplatesResourceHandler.categoryTemplate()).thenReturn(categoryTemplateGetResourceHandler);
        when(categoryTemplateGetResourceHandler.get(anyString())).thenReturn(categoryTemplateGet);
    }

    private void expectGetCategoryTemplateByParent() {
        when(resourceHandler.categoryTemplates()).thenReturn(categoryTemplatesResourceHandler);
        when(categoryTemplatesResourceHandler.categoryTemplatesByParent()).thenReturn(
                categoryTemplatesGetByParentResourceHandler);
        when(categoryTemplatesGetByParentResourceHandler.get(anyString())).thenReturn(categoryTemplatesGetByParent);
    }
}