package uk.gov.companieshouse.efs.web.formtemplates.service.api;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateListApi;

/**
 * The {@code FormTemplateService} interface provides an abstraction that can be
 * used when testing {@code FormTemplateService} static methods, without imposing
 * the use of a test framework that supports mocking of static methods.
 */
public interface FormTemplateService {

    InternalApiClient getApiClient();

    /**
     * Form template list response object.
     *
     * @return the list of form models for the json response
     */
    ApiResponse<FormTemplateListApi> getFormTemplates();

    /**
     * Form template response object.
     *
     * @return the form template for the json response
     */
    ApiResponse<FormTemplateApi> getFormTemplate(String id);

    /**
     * Form template list response object.
     *
     * @return the form template list for the json response
     */
    ApiResponse<FormTemplateListApi> getFormTemplatesByCategory(String id);
}
