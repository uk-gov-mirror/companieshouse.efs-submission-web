package uk.gov.companieshouse.efs.web.security;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.regex.Matcher;

public class HttpRequestRequiredValidator extends AuthRequiredValidator
        implements Validator<HttpServletRequest> {

    public HttpRequestRequiredValidator(ValidatorResourceProvider resourceProvider) {
        super(resourceProvider);
    }

    /**
     * Only get requests require authorisation because the API will handle post requests and
     * get and post request are the only valid methods.
     * <p>
     * All material that requires authorisation is behind a URL with a submission and company so if
     * the url doesn't have a submission and company it doesn't require authorisation.
     *
     * @return true is request requires authorisation
     */
    @Override
    public boolean requiresAuth() {
        return isGetRequest() && isEfsSubmissionWithCompany();
    }

    /**
     * @return true if request method is GET false otherwise
     */
    private boolean isGetRequest() {
        return Optional.ofNullable(resourceProvider)
                .map(ValidatorResourceProvider::getInput)
                .map(HttpServletRequest::getMethod)
                .map("GET"::equalsIgnoreCase)
                .orElse(false);
    }

    /**
     * @return true if the url is an efs submission with a company number.
     */
    private boolean isEfsSubmissionWithCompany() {
        return Optional.ofNullable(resourceProvider)
                .map(ValidatorResourceProvider::getRequestPathMatcher)
                .map(Matcher::find)
                .orElse(false);
    }
}
