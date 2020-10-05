package uk.gov.companieshouse.efs.web.formtemplates.service.api.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateListApi;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.efs.web.service.api.impl.ApiClientServiceImpl;
import uk.gov.companieshouse.efs.web.service.api.impl.BaseApiClientServiceImpl;
import uk.gov.companieshouse.logging.Logger;

/**
 * Service sends and receives secure REST messages to the api.
 */
@Service
public class FormTemplateServiceImpl extends BaseApiClientServiceImpl
        implements FormTemplateService {

    public static final String FORM_TEMPLATES_FRAGMENT = "/form-templates";
    public static final String FORM_TEMPLATE_FRAGMENT = "/form-template";
    public static final String CATEGORY_TEMPLATE = "category={category}";
    private static final String TYPE_ID_TEMPLATE = "type={id}";

    private ApiClientService apiClientService;

    /**
     * Construct an {@link ApiClientServiceImpl}.
     *
     * @param logger the CH logger
     */
    @Autowired
    public FormTemplateServiceImpl(ApiClientService apiClientService, Logger logger) {
        super(logger);
        this.apiClientService = apiClientService;
    }

    @Override
    public InternalApiClient getApiClient() {
        return apiClientService.getApiClient();
    }

    @Override
    public ApiResponse<FormTemplateListApi> getFormTemplates() {

        final String uri = BaseApiClientServiceImpl.ROOT_URI + FORM_TEMPLATES_FRAGMENT;

        return executeOp("getFormTemplates", uri,
                getApiClient().privateEfsResourceHandler().formTemplates().formTemplates().get(uri));
    }

    @Override
    public ApiResponse<FormTemplateApi> getFormTemplate(String id) {
        final String path = BaseApiClientServiceImpl.ROOT_URI + FORM_TEMPLATE_FRAGMENT;
        final UriComponents components = UriComponentsBuilder.fromPath(path).query(TYPE_ID_TEMPLATE)
            .buildAndExpand(id).encode();

        final String uri = components.toUriString();

        return executeOp("getFormTemplate", uri,
                getApiClient().privateEfsResourceHandler().formTemplates().formTemplate().get(uri));
    }

    @Override
    public ApiResponse<FormTemplateListApi> getFormTemplatesByCategory(String id) {

        final String path = ROOT_URI + FORM_TEMPLATES_FRAGMENT;
        final UriComponents components = UriComponentsBuilder.fromPath(path).query(CATEGORY_TEMPLATE)
            .buildAndExpand(id).encode();

        final String uri = components.toUriString();

        return executeOp("getFormTemplatesByCategory", uri,
            getApiClient().privateEfsResourceHandler().formTemplates().formTemplatesByCategory().get(uri));
    }

}
