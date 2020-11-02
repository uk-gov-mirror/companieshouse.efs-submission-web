package uk.gov.companieshouse.efs.web.security;


public abstract class ValidatorImpl implements Validator {
    private Validator nextValidator;
    protected ValidatorResourceProvider resourceProvider;

    public ValidatorImpl(ValidatorResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    @Override
    public Validator setNext(Validator nextValidator) {
        this.nextValidator = nextValidator;

        return nextValidator;
    }

    @Override
    public boolean validate() {
        if (isValid() && nextValidator != null) {
            return nextValidator.validate();
        }

        return false;
    }

    public abstract boolean isValid();
}
