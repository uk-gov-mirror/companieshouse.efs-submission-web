package uk.gov.companieshouse.efs.web.categorytemplates.service.api;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateListApi;
import uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants;

/**
 * The {@code CategoryTemplateService} interface provides an abstraction that can be
 * used when testing {@code CategoryTemplateService} static methods, without imposing
 * the use of a test framework that supports mocking of static methods.
 */
public interface CategoryTemplateService {

    InternalApiClient getApiClient();

    /**
     * Category template list response object.
     *
     * @return the list of form category models for the json response
     */
    ApiResponse<CategoryTemplateListApi> getCategoryTemplates();

    /**
     * Category template response object.
     *
     * @param id the form category
     * @return the form category template for the json response
     */
    ApiResponse<CategoryTemplateApi> getCategoryTemplate(String id);

    /**
     * Category template list response object.
     *
     * @param id the form category
     * @return the form category list for the json response
     */
    ApiResponse<CategoryTemplateListApi> getCategoryTemplatesByParent(String id);

    /**
     * Given a form category below the top level, work out its top level ancestor.
     *
     * @param category the form category
     * @return top level category or root if it already is a top level category or the root category
     */
    CategoryTypeConstants getTopLevelCategory(String category);
}
