package uk.gov.companieshouse.efs.web.categorytemplates.service.api.impl;

import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants.OTHER;
import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants.ROOT;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateListApi;
import uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.configuration.DataCacheConfig;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.efs.web.service.api.impl.ApiClientServiceImpl;
import uk.gov.companieshouse.efs.web.service.api.impl.BaseApiClientServiceImpl;
import uk.gov.companieshouse.logging.Logger;

/**
 * Service sends and receives secure REST messages to the api.
 */
@Service
public class CategoryTemplateServiceImpl extends BaseApiClientServiceImpl
        implements CategoryTemplateService {

    public static final String CATEGORY_TEMPLATES_FRAGMENT = "/category-templates";
    public static final String CATEGORY_TEMPLATE_FRAGMENT = "/category-template";
    private static final String FAMILY_TEMPLATE = "family={family}";
    private static final String PARENT_TEMPLATE = "parent={parent}";

    private ApiClientService apiClientService;

    /**
     * Construct an {@link ApiClientServiceImpl}.
     *
     * @param logger the CH logger
     */
    @Autowired
    public CategoryTemplateServiceImpl(ApiClientService apiClientService, Logger logger) {
        super(logger);
        this.apiClientService = apiClientService;
    }

    @Override
    public InternalApiClient getApiClient() {
        return apiClientService.getApiClient();
    }

    @Override
    @Cacheable(value = DataCacheConfig.ALL_CATEGORIES, sync = true)
    public ApiResponse<CategoryTemplateListApi> getCategoryTemplates() {

        final String uri = BaseApiClientServiceImpl.ROOT_URI + CATEGORY_TEMPLATES_FRAGMENT;

        return executeOp("getCategoryTemplates", uri,
                getApiClient().privateEfsResourceHandler().categoryTemplates().categoryTemplates()
                        .get(uri));
    }

    @Override
    @Cacheable(value = DataCacheConfig.CATEGORY_BY_ID, sync = true)
    public ApiResponse<CategoryTemplateApi> getCategoryTemplate(String id) {

        final String uri =
                BaseApiClientServiceImpl.ROOT_URI + CATEGORY_TEMPLATE_FRAGMENT + "/" + id;

        return executeOp("getCategoryTemplate", uri,
            getApiClient().privateEfsResourceHandler().categoryTemplates().categoryTemplate().get(uri));
    }

    @Override
    @Cacheable(value = DataCacheConfig.CATEGORY_BY_FAMILY, sync = true)
    public ApiResponse<CategoryTemplateListApi> getCategoryTemplatesByFamily(final String id) {

        final String path = BaseApiClientServiceImpl.ROOT_URI + CATEGORY_TEMPLATES_FRAGMENT;
        final UriComponents components = UriComponentsBuilder.fromPath(path).query(FAMILY_TEMPLATE)
                .buildAndExpand(id).encode();

        final String uri = components.toUriString();

        return executeOp("getCategoryTemplateByFamily", uri,
                getApiClient().privateEfsResourceHandler().categoryTemplates()
                        .categoryTemplatesByFamily().get(uri));
    }

    @Override
    @Cacheable(value = DataCacheConfig.CATEGORY_BY_PARENT, sync = true)
    public ApiResponse<CategoryTemplateListApi> getCategoryTemplatesByParent(final String id) {

        final String path = BaseApiClientServiceImpl.ROOT_URI + CATEGORY_TEMPLATES_FRAGMENT;
        final UriComponents components = UriComponentsBuilder.fromPath(path).query(PARENT_TEMPLATE)
                .buildAndExpand(id).encode();

        final String uri = components.toUriString();

        return executeOp("getCategoryTemplateByParent", uri,
                getApiClient().privateEfsResourceHandler().categoryTemplates()
                        .categoryTemplatesByParent().get(uri));
    }

    @Override
    @Cacheable(value = DataCacheConfig.TOP_LEVEL_CATEGORY, sync = true)
    public CategoryTypeConstants getTopLevelCategory(final String category) {
        CategoryTypeConstants result = CategoryTypeConstants.nameOf(category).orElse(OTHER);
        String currentCategory = category;

        while (true) {
            final ApiResponse<CategoryTemplateApi> categoryTemplateResponse = getCategoryTemplate(currentCategory);
            final String parent = categoryTemplateResponse.getData().getParent();

            if (StringUtils.isBlank(parent)) {
                return result;
            }

            final CategoryTypeConstants parentCategory = CategoryTypeConstants.nameOf(parent).orElse(OTHER);

            if (parentCategory == ROOT) {
                return result;
            } else {
                result = parentCategory;
                currentCategory = parent;
            }
        }
    }
}
