package uk.gov.companieshouse.efs.web.security;

/**
 * Chain of responsibility pattern implementation
 *
 * @param <T> the type to be validated
 */
public interface Validator<T> {
    // Sets the next validator in the chain and returns the original validator
    Validator<T> setNext(Validator<T> nextValidator);

    // Runs the chain and returns true if all validators in the chain are true
    boolean validate(T input);
}
