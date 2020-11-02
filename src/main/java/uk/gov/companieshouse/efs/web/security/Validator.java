package uk.gov.companieshouse.efs.web.security;

public interface Validator {
    Validator setNext(Validator nextValidator);
    boolean validate();
}
