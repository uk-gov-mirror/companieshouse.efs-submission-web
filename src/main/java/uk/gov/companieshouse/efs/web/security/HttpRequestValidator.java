package uk.gov.companieshouse.efs.web.security;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.regex.Matcher;

public class HttpRequestValidator extends ValidatorImpl
        implements Validator {

    public HttpRequestValidator(ValidatorResourceProvider resourceProvider) {
        super(resourceProvider);
    }

    @Override
    public boolean isValid() {
        return isGetRequest() && isEfsSubmissionWithCompany();
    }

    private boolean isGetRequest() {
        return Optional.ofNullable(resourceProvider)
                .map(ValidatorResourceProvider::getInput)
                .map(HttpServletRequest::getMethod)
                .map("GET"::equalsIgnoreCase)
                .orElse(false);
    }

    private boolean isEfsSubmissionWithCompany() {
        return Optional.ofNullable(resourceProvider)
                .map(ValidatorResourceProvider::getRequestPathMatcher)
                .map(Matcher::find)
                .orElse(false);
    }
}
