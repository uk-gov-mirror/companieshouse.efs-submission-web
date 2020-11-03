package uk.gov.companieshouse.efs.web.security;

public abstract class ValidatorImpl implements Validator {
    protected ValidatorResourceProvider resourceProvider;
    private Validator nextValidator;
    private Validator lastValidator;

    public ValidatorImpl(ValidatorResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    @Override
    public Validator setNext(Validator nextValidator) {
        if (this.nextValidator == null) {
            this.nextValidator = nextValidator;
        } else {
            lastValidator.setNext(nextValidator);
        }

        lastValidator = nextValidator;

        return this;
    }

    @Override
    public boolean validate() {
        if (nextValidator == null) {
            return isValid();
        }

        return isValid() && nextValidator.validate();
    }

    public boolean requiresAuth() {
        return validate();
    }

    protected abstract boolean isValid();
}
