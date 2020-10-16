package uk.gov.companieshouse.efs.web.controller;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.servlet.ServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.efs.web.service.session.SessionService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.session.Session;
import uk.gov.companieshouse.session.handler.SessionHandler;

/**
 * Contains common code for handling the HTTP requests for the web application.
 */
@Controller
@ControllerAdvice
@RequestMapping(BaseControllerImpl.SERVICE_URI)
public abstract class BaseControllerImpl implements BaseController {

    @Value("${chs.url}")
    protected String chsUrl;

    public static final String SERVICE_URI = "/efs-submission";
    static final String TEMPLATE_NAME = "templateName";

    protected static final String CHS_SESSION_ID = "chsSessionId";
    protected static final String ORIGINAL_SUBMISSION_ID = "originalSubmissionId";

    private static final String GET_APPLICATION = "GET {}";

    private static final Pattern FIELD_INDEX_REGEX = Pattern.compile("\\[\\d+\\]");

    protected Logger logger;
    protected SessionService sessionService;
    protected ApiClientService apiClientService;
    protected FormTemplateService formTemplateService;
    protected CategoryTemplateService categoryTemplateService;

    @Override
    public String getViewName() {
        return null;
    }

    /**
     * Retrieves the submission data from the efs-submission-api.
     *
     * @param id the submission id
     * @return Submission data
     */
    @Override
    public SubmissionApi getSubmission(final String id) {
        ApiResponse<SubmissionApi> response = apiClientService.getSubmission(id);

        logApiResponse(response, id, GET_APPLICATION);

        return response.getData();
    }

    /**
     * Required for Web Analytics.
     *
     * @param model the model
     */
    protected final void addTrackingAttributeToModel(Model model) {
        model.addAttribute(TEMPLATE_NAME, getViewName());
    }

    /**
     * Constructor used by child controllers.
     *
     * @param logger              the CH logger
     * @param sessionService      the CHS session service
     * @param apiClientService    the API client service
     * @param formTemplateService the API form template service
     */
    @Autowired
    public BaseControllerImpl(final Logger logger, final SessionService sessionService,
        final ApiClientService apiClientService, final FormTemplateService formTemplateService,
        final CategoryTemplateService categoryTemplateService) {
        this.logger = logger;
        this.sessionService = sessionService;
        this.apiClientService = apiClientService;
        this.formTemplateService = formTemplateService;
        this.categoryTemplateService = categoryTemplateService;
    }

    public BaseControllerImpl(final Logger logger, final SessionService sessionService,
        final ApiClientService apiClientService) {
        this(logger, sessionService, apiClientService, null, null);
    }

    public BaseControllerImpl(final Logger logger) {
        this(logger, null, null, null, null);
    }

    public BaseControllerImpl() {
    }

    protected <T> void logApiResponse(final ApiResponse<T> response, final String applicationId, final String message) {
        if (response != null) {
            final HttpStatus status = HttpStatus.valueOf(response.getStatusCode());
            final HttpStatus.Series series = status.series();

            switch (series) {
                case CLIENT_ERROR:    // fall through
                case SERVER_ERROR:
                    logger.errorContext(applicationId,
                            MessageFormat.format("API response: status={0}, message={1}",
                                    status, message), null, null);
                    break;
                default:
                    logger.infoContext(applicationId,
                            MessageFormat.format("API response: status={0}, message={1}",
                                    status, message) + status, null);
                    break;
            }
            if (response.hasErrors()) {
                response.getErrors().forEach(e -> logger.errorContext(applicationId, "error=" + e, null, null));
            }
        }
    }

    protected String getChsSessionId(final ServletRequest request) {
        Session chSession = (Session) request.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY);
        return chSession == null ? null : chSession.getCookieId();
    }

    protected <T> void addAnyErrorsFromResponse(final BindingResult bindingResult, final ApiResponse<T> response,
        final Predicate<? super ApiError> filter) {
        Optional.ofNullable(response.getErrors()).map(Collection::stream).orElseGet(Stream::empty).filter(filter).map(
                this::toFieldError).forEach(
                f -> bindingResult.rejectValue(f.getField(), f.getCode(), f.getArguments(), f.getDefaultMessage()));
    }

    /**
     * Create {@link FieldError} with resource bundle lookup by Spring. Thymeleaf template error references
     * should be like {@code "${e}"} instead of {@code "#{${e}}"} to avoid a double lookup.
     *
     * @param error the {@link ApiError}
     * @return ApiFieldError with necessary error arguments for resource bundle placeholders, e.g. "{0}"
     */
    @SuppressWarnings("squid:S2589") // false positive: expression always true
    protected FieldError toFieldError(final ApiError error) {
        String code = error.getLocation();
        String[] codeArray = code.split("\\.");
        String codeNoIndex = FIELD_INDEX_REGEX.matcher(code).replaceAll("");
        String[] codeNoIndexArray = codeNoIndex.split(("\\."));
        int objectNameStartIndex = 1; // skip "$" prefix
        int fieldNameStartIndex = objectNameStartIndex + 1;
        String name = codeArray[fieldNameStartIndex];

        while ("list".equals(name) || name.endsWith("]")) {
            // skip name "list" and names ending with regex "\[\d+\], i.e. lists"
            ++fieldNameStartIndex;
            name = codeArray[fieldNameStartIndex];
        }
        if ("ura".equals(name)) {
            ++fieldNameStartIndex;
            // match field codes for address page template; make "ura" part of objectName
        }

        String fieldName = String.join(".",
                Arrays.copyOfRange(codeNoIndexArray, fieldNameStartIndex, codeArray.length));
        String objectName = String.join(".",
                Arrays.copyOfRange(codeNoIndexArray, objectNameStartIndex, fieldNameStartIndex));

        final Map<String, String> errorValues = error.getErrorValues();
        Object[] args = null;
        String rejectedValue = null;

        if (errorValues != null) {
            rejectedValue = errorValues.get("rejected_value");
            switch (error.getError()) {
                case "max_length_exceeded":
                    args = new Object[]{Integer.valueOf(errorValues.get("max_length")), Integer.valueOf(
                            errorValues.get("excess_length"))};
                    break;
                case "value_outside_range":
                    args = new Object[]{Integer.valueOf(errorValues.get("upper")), Integer.valueOf(
                            errorValues.get("lower"))};
                    break;
                case "pattern_not_matched":
                    args = new Object[]{errorValues.get("pattern")};
                    break;
                default:
                    break;
            }
        }

        final String[] fieldErrorCodes = new String[]{error.getError()};
        final String defaultMessage = String.join(".", error.getError(), objectName, fieldName);
        return new FieldError(objectName, fieldName, rejectedValue, false, fieldErrorCodes, args,
                defaultMessage);
    }

    /**
     * Verify the submission form in progress with the journey we started with.
     *
     * @param submissionApi The current submission form being used in the controller.
     * @return True if the submission form and user matches the journey start.
     */
    boolean verifySubmission(final SubmissionApi submissionApi) {
        Map<String, Object> sessionDataFromContext = sessionService.getSessionDataFromContext();
        String originalSubmissionId = sessionDataFromContext.get(ORIGINAL_SUBMISSION_ID).toString();

        boolean isSameForm = StringUtils.equals(originalSubmissionId, submissionApi.getId());
        boolean isSameUser = StringUtils.equals(sessionService.getUserEmail(), submissionApi.getPresenter().getEmail());

        return (isSameForm && isSameUser);
    }

    /**
     * Setter for chsUrl to facilitate testing.
     *
     * @param chsUrl the URL to set
     */
    public void setChsUrl(final String chsUrl) {
        this.chsUrl = chsUrl;
    }
}
