package uk.gov.companieshouse.efs.web.security;


import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;

import javax.servlet.http.HttpServletRequest;


/**
 * Validates if the the form attached to the submission requires authorisation.
 */
public class FormTemplateRequiredValidator extends AuthRequiredValidator
        implements Validator<HttpServletRequest> {

    public FormTemplateRequiredValidator(ValidatorResourceProvider resourceProvider) {
        super(resourceProvider);
    }

    /**
     * Only forms require authorisation so if the user hasn't progressed far enough to have a form
     * then it can't require authorisation.
     * If the form is present then it returns whether it requires authorisation.
     *
     * @return true if the form attached to the submission requires authentication
     */
    @Override
    public boolean requiresAuth() {
        return formRequiresAuth();
    }

    /**
     * @return true if submission has form and form requires auth
     */
    private boolean formRequiresAuth() {
        return resourceProvider.getForm()
                .map(FormTemplateApi::isAuthenticationRequired)
                .orElse(false);    // If the user hasn't progressed far enough to have a form then
        // this returns false
    }
}
