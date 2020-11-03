package uk.gov.companieshouse.efs.web.security;


import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;


public class FormTemplateValidator extends ValidatorImpl
        implements Validator {

    public FormTemplateValidator(ValidatorResourceProvider resourceProvider) {
        super(resourceProvider);
    }

    @Override
    public boolean isValid() {
        return isAuthRequired();
    }

    private boolean isAuthRequired() {
        return resourceProvider.getForm()
                .map(FormTemplateApi::isAuthenticationRequired)
                .orElse(false);    // If the user hasn't progressed far enough to have a form then
                                         // this returns false
    }
}
