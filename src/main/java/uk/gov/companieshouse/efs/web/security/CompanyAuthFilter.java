package uk.gov.companieshouse.efs.web.security;

import uk.gov.companieshouse.auth.filter.AuthFilter;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.session.Session;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * CompanyAuthFilter
 */
public class CompanyAuthFilter extends AuthFilter {

    private final ApiClientService apiClientService;

    private final FormTemplateService formTemplateService;

    private final CategoryTemplateService categoryTemplateService;

    /**
     * Constructor.
     *
     * @param environmentReader       dependency
     * @param apiClientService        dependency
     * @param formTemplateService     dependency
     * @param categoryTemplateService dependency
     */
    public CompanyAuthFilter(final EnvironmentReader environmentReader,
                             final ApiClientService apiClientService,
                             final FormTemplateService formTemplateService,
                             CategoryTemplateService categoryTemplateService) {
        super(environmentReader);
        this.apiClientService = apiClientService;
        this.formTemplateService = formTemplateService;
        this.categoryTemplateService = categoryTemplateService;
    }

    @Override
    public void doFilter(
            ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        ValidatorResourceProvider resourceProvider = new ValidatorResourceProvider(httpServletRequest, apiClientService, formTemplateService);
        Validator authChecker = new HttpRequestValidator(resourceProvider)
                .setNext(new FormTemplateValidator(resourceProvider))
                .setNext(new UserValidator(resourceProvider, categoryTemplateService));

        if(authChecker.validate()) {
            String companyNumber = resourceProvider.getCompanyNumber().orElse("");
            Session chsSession = resourceProvider.getChsSession().orElse(null);
            redirectForAuth(chsSession,
                    httpServletRequest,
                    (HttpServletResponse) response,
                    companyNumber,
                    false);
        }

        chain.doFilter(request, response);
    }
}
