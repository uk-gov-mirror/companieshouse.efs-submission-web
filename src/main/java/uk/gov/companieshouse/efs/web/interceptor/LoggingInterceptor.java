package uk.gov.companieshouse.efs.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.RequestLogger;

/**
 * Logs out information about the {@link HttpServletRequest} and {@link HttpServletResponse}.
 */
@Component
public class LoggingInterceptor extends HandlerInterceptorAdapter implements RequestLogger {

    @SuppressWarnings("squid:S1312") // Loggers should be private static final; here logger is singleton bean
    private Logger logger;

    /**
     * Constructor for the LoggingInterceptor.
     *
     * @param logger used to log out the request at the start and end of processing
     */
    @Autowired
    public LoggingInterceptor(Logger logger) {
        this.logger = logger;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        logStartRequestProcessing(request, logger);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mav) {
        logEndRequestProcessing(request, response, logger);
    }

}
