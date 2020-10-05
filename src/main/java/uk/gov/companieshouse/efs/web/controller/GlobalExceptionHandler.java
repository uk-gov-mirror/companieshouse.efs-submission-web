package uk.gov.companieshouse.efs.web.controller;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String SERVICE_PROBLEM_PAGE = ViewConstants.ERROR.asView();

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500
    @ExceptionHandler(ResponseStatusException.class)
    public String handleStatusErrorResponse(final ResponseStatusException ex) {
        return SERVICE_PROBLEM_PAGE;
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
