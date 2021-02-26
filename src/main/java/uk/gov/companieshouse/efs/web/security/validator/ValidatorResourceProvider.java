package uk.gov.companieshouse.efs.web.security.validator;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionFormApi;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.session.Session;
import uk.gov.companieshouse.session.handler.SessionHandler;
import uk.gov.companieshouse.session.model.SignInInfo;

/**
 * To validate whether a request requires authorisation information about that request must be gathered
 * For example, the submission attached to the request, or the form.
 * This can be expensive to compute (requires an api call)
 * So in cases where information is required by more than one validator that information can
 * be saved to make subsequent accesses quicker.
 * <p>
 * ValidationResourceProvider wraps a request and takes in some dependencies, providing methods to
 * access these resources.
 * One they have been computed, their value is saved.
 */
public class ValidatorResourceProvider {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ValidatorResourceProvider.class.getCanonicalName());
    private static final Pattern EFS_SUBMISSION_WITH_COMPANY = Pattern.compile(
            "^/efs-submission/(?<submissionId>[a-fA-F\\d]{24}+)/company/(?<companyNumber>[a-zA-Z\\d]{8}+)[^a-zA-Z\\d]?+");

    private final FormTemplateService formTemplateService;
    private final ApiClientService apiClientService;
    private HttpServletRequest request;
    private SubmissionApi submission;
    private FormTemplateApi form;
    private SignInInfo signInInfo;
    private static int getSubmissionCounter;

    public ValidatorResourceProvider(ApiClientService apiClientService,
                                     FormTemplateService formTemplateService) {

        this.formTemplateService = formTemplateService;
        this.apiClientService = apiClientService;
    }

    /**
     * Gets the submission of request by extracting the submissionID from the URL and
     * querying the API for a submission with that ID.
     *
     * @return an optional containing the submission. Empty if there is no submission with that ID.
     */
    private Optional<SubmissionApi> getSubmissionFromRequest() {
        Matcher urlMatcher = getRequestPathMatcher();
        if (urlMatcher.find()) {
            String submissionId = urlMatcher.group(1);
            Optional<SubmissionApi> maybeSubmission = Optional.ofNullable(apiClientService
                    .getSubmission(submissionId))
                    .map(ApiResponse::getData);

            logGetSubmission();
            maybeSubmission.ifPresent(submissionApi -> submission = submissionApi);

            return maybeSubmission;
        }

        return Optional.empty();
    }

    private static synchronized void logGetSubmission() {
        LOGGER.debug(String.format("%s getSubmission() count: % 3d",
                ValidatorResourceProvider.class.getSimpleName(), ++getSubmissionCounter));
    }

    /**
     * Gets the already computed submission if there is one.
     * Else it will compute it from the request
     *
     * @return an optional containing a submission. It will be empty if there is no submission
     * with the submissionID retrieved from the URL
     */
    Optional<SubmissionApi> getSubmission() {
        if (submission != null) {
            return Optional.of(submission);
        }
        
        return getSubmissionFromRequest();
    }

    /**
     * Gets the request that is being used to compute resources
     *
     * @return the input request
     */
    public HttpServletRequest getInput() {
        return request;
    }

    /**
     * Sets the request used to compute resources.
     *
     * @param request the request to use
     */
    void setInput(HttpServletRequest request) {
        if (request == null || !request.equals(this.request)) {
            // Pre-computes resources are only valid for a specific request
            invalidate();
        }

        this.request = request;
    }

    /**
     * Removes all pre computed resources.
     */
    private void invalidate() {
        submission = null;
        form = null;
        signInInfo = null;
    }

    /**
     * Gets a regular expression that can match against a URL to efs that has a submissionID
     * and company number. It has groups allowing that information to be extracted.
     *
     * @return a regex matcher for the request URL and EFS_SUBMISSION_WITH_COMPANY
     */
    Matcher getRequestPathMatcher() {
        return EFS_SUBMISSION_WITH_COMPANY.matcher(request.getRequestURI());
    }

    /**
     * Computes the submission form for the requests submission.
     * If the submission is not present or the submission doesn't have a form then the return will
     * be empty.
     * <p>
     * If the form is present it will be cached for future access.
     *
     * @return an optional containing the form
     */
    private Optional<FormTemplateApi> getFormFromSubmission() {
        Optional<FormTemplateApi> maybeForm = getSubmission()
                .map(SubmissionApi::getSubmissionForm)
                .map(SubmissionFormApi::getFormType)
                .map(formTemplateService::getFormTemplate)
                .map(ApiResponse::getData);

        maybeForm.ifPresent(formTemplateApi -> form = formTemplateApi);

        return maybeForm;
    }

    /**
     * Gets the submission form for the request.
     * <p>
     * If it hasn't already been computed, it will compute it from the request.
     *
     * @return an optional containing the form.
     */
    Optional<FormTemplateApi> getForm() {
        if (form != null) {
            return Optional.of(form);
        }

        return getFormFromSubmission();
    }

    /**
     * Computes the chs session from the request
     *
     * @return an optional containing the session. Empty is there is no session.
     */
    public Optional<Session> getChsSession() {
        return Optional.ofNullable((Session) request.getAttribute(SessionHandler
                .CHS_SESSION_REQUEST_ATT_KEY));
    }

    /**
     * Computes the SignInInfo for user that made the request.
     * Caches the result if present.
     *
     * @return an optional containing the signininfo. Empty if no session.
     */
    private Optional<SignInInfo> getSignInInfoFromRequest() {
        Optional<SignInInfo> maybeSignInInfo = getChsSession()
                .map(Session::getSignInInfo);

        maybeSignInInfo.ifPresent(signInInfo1 -> signInInfo = signInInfo1);

        return maybeSignInInfo;
    }


    /**
     * Gets the sign in info for a request.
     * <p>
     * Computes it if not present.
     *
     * @return an optional containing the siginininfo.
     */
    Optional<SignInInfo> getSignInInfo() {
        if (signInInfo != null) {
            return Optional.of(signInInfo);
        }

        return getSignInInfoFromRequest();
    }

    ApiClientService getApiClientService() {
        return apiClientService;
    }

    /**
     * Gets the companyNumber from the request url
     *
     * @return the company is it was found.
     */
    public Optional<String> getCompanyNumber() {
        Matcher urlPathMatcher = getRequestPathMatcher();
        if (!urlPathMatcher.find()) {
            return Optional.empty();
        }

        return Optional.ofNullable(urlPathMatcher.group("companyNumber"));
    }

}
