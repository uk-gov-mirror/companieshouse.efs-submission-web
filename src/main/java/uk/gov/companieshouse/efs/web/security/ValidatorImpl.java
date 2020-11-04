package uk.gov.companieshouse.efs.web.security;

/**
 * Implementation of the validator interface.
 * A validator can be created by extending this class and implementing the "isValid" method
 *
 * @param <T> the type to be validated
 */
public abstract class ValidatorImpl<T> implements Validator<T> {
    protected Validator<T> nextValidator;
    private Validator<T> lastValidator;

    /**
     * Sets the next validator in the chain and returns the first validator in the chain.
     * It returns the first validator so that the return value of a chain of setNext calls
     * is the beginning of the chain and validate can be called to run the chain.
     * <p>
     * usage:
     * Validator<Integer> validator = new Validator1()
     * .setNext(new Validator2())
     * .setNext(mew Validator3());
     *
     * @param nextValidator the validator to add to the chain
     * @return the first validator in the chain
     */
    @Override
    public Validator<T> setNext(Validator<T> nextValidator) {
        if (this.nextValidator == null) {
            this.nextValidator = nextValidator;
        } else {
            lastValidator.setNext(nextValidator);
        }

        lastValidator = nextValidator;

        return this;
    }

    /**
     * Validates if the the chain of validators is true
     *
     * @param input the input to validate
     * @return true if all validators in the chain are true, false if any are not.
     */
    @Override
    public boolean validate(T input) {
        if (nextValidator == null) {
            return isValid();
        }

        return isValid() && nextValidator.validate(input);
    }

    protected abstract boolean isValid();
}
