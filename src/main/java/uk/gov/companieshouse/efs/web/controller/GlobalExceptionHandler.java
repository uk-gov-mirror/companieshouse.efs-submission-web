package uk.gov.companieshouse.efs.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.companieshouse.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Component
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private final Logger structuredLogger;
    private static final Pattern submissionIDPattern = Pattern.compile("[0-9a-fA-F]{24}");

    @Autowired
    public GlobalExceptionHandler(Logger logger) {
        this.structuredLogger = logger;
    }

    private static final String SERVICE_PROBLEM_PAGE = ViewConstants.ERROR.asView();


    @ExceptionHandler(ResponseStatusException.class)
    public String handleStatusErrorResponseWithSubmissionId(HttpServletRequest request,
                                            final ResponseStatusException ex) {

        String submissionID = submissionIDFromURI(request.getRequestURI());
        logResponseStatusException(ex, submissionID);

        return SERVICE_PROBLEM_PAGE;
    }

    private void logResponseStatusException(ResponseStatusException ex, final String submissionID) {
        Map<String, Object> logDetails = new HashMap<>();
        logDetails.put("statusCode", ex.getStatus().value());
        logDetails.put("statusMessage", ex.getStatus().getReasonPhrase());

        structuredLogger.errorContext(submissionID, "Received non 200 series response from API",
                ex, logDetails);
    }

    /**
     * Retrieves the submissionID from the request URI by matching with a regular expression that
     * matches mongo objects which is what is being used for the submission ID's
     *
     * @param uri - The reuest URI
     * @return - The submissionID. It will be an empty string if there was no submissionID
     */
    private static String submissionIDFromURI(final String uri) {
        Matcher submissionIDMatcher = submissionIDPattern.matcher(uri);
        if (!submissionIDMatcher.find()) {
            return "";
        }

        return submissionIDMatcher.group();
    }

    /**
     * Log the exception and go to error page.
     *
     * @param ex the exception
     * @return
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500
    @ExceptionHandler(Exception.class)
    public String handleException(final Exception ex) {
        return SERVICE_PROBLEM_PAGE;
    }
}
