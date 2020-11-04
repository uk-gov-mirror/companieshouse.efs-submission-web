package uk.gov.companieshouse.efs.web.security;

import javax.servlet.http.HttpServletRequest;

/**
 * Contains fields used by all company auth validators
 * <p>
 * Implementation of a chain of responsibility pattern
 */
public abstract class BaseCompanyAuthValidator extends ValidatorImpl<HttpServletRequest> {
    protected ValidatorResourceProvider resourceProvider;

    /**
     * Constructor
     *
     * @param resourceProvider the object to hold resources used by multiple chain links
     */
    public BaseCompanyAuthValidator(ValidatorResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    /**
     * Validates whether the request needs to be redirected for authorisation before continuing
     *
     * @param request the request made to the web
     * @return true if redirect for auth required, false otherwise
     */
    @Override
    public boolean validate(HttpServletRequest request) {
        if (resourceProvider != null && resourceProvider.getInput() == null) {
            resourceProvider.setInput(request);
        }

        return super.validate(request);
    }

    protected abstract boolean requiresAuth();

    @Override
    protected boolean isValid() {
        return requiresAuth();
    }
}
