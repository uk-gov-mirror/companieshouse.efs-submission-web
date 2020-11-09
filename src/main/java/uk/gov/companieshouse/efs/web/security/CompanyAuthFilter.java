package uk.gov.companieshouse.efs.web.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import uk.gov.companieshouse.auth.filter.AuthFilter;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.security.validator.FormTemplateRequiredValidator;
import uk.gov.companieshouse.efs.web.security.validator.HttpRequestRequiredValidator;
import uk.gov.companieshouse.efs.web.security.validator.UserRequiredValidator;
import uk.gov.companieshouse.efs.web.security.validator.Validator;
import uk.gov.companieshouse.efs.web.security.validator.ValidatorResourceProvider;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.session.Session;


/**
 * CompanyAuthFilter ensures that a request is authorised if it is made to a resource that
 * requires authentication.
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

    /**
     * doFilter does the request validation and, if necessary, redirects the user for authentication
     * before responding.
     * <p>
     * It redirects if:
     * The request is a get request
     * and made to an endpoint with a company number and submissionId
     * and has a form that requires authentication
     * and the user is not authorised for that form or, in the case of Insolvency,
     * on the allow list.
     * <p>
     * The validation for these conditions is implemented as a chain of responsibility
     * with a "link" for the request checking, form checking, and finally user checking.
     *
     * @param request  the request made to the web
     * @param response the response to the user
     * @param chain    the chain of filters for authorisation
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {


        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        ValidatorResourceProvider resourceProvider = new ValidatorResourceProvider(apiClientService,
                formTemplateService);
        Validator<HttpServletRequest> requiresAuth = new HttpRequestRequiredValidator(resourceProvider)
                .setNext(new FormTemplateRequiredValidator(resourceProvider))
                .setNext(new UserRequiredValidator(resourceProvider, categoryTemplateService));

        if (requiresAuth.validate(httpServletRequest)) {
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
