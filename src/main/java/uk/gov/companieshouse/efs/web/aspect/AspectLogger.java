package uk.gov.companieshouse.efs.web.aspect;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.efs.web.formtemplates.model.FormTemplateModel;
import uk.gov.companieshouse.logging.Logger;

@Aspect
@Component
public class AspectLogger {
    private final Logger logger;
    private static final List<String> fieldsToLog = Arrays.asList(
            "id", "companyNumber");

    @Autowired
    public AspectLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Logs all exceptions thrown within any endpoints.
     * Logs at error level with the application id as context. Includes the stack trace and
     * attempts to fetch some fields to pass to the structured logger.
     * <p>
     * Exceptions thrown in controllers are caught and handled in the exception handlers in the
     * GlobalExceptionHandler controller. However, they are not logged. That is the job is this
     * aspect.
     * </p>
     *
     * @param jp the point in execution just after the exception is thrown.
     * @param ex the exception that was thrown.
     */
    @AfterThrowing(value = "@within(org.springframework.stereotype.Controller)", throwing = "ex")
    void onExceptionHandler(JoinPoint jp, Exception ex) {
        JoinPointHelper helper = new JoinPointHelper(jp);
        String id = helper.getArgument("id", String.class)
                .orElse("");

        Map<String, Object> argsPassedToEndpoint = helper.getArguments(fieldsToLog::contains);

        logger.errorContext(id, ex.getMessage(), ex, argsPassedToEndpoint);
    }

    @Pointcut("within(uk.gov.companieshouse.efs.web.formtemplates.controller."
             + "FormTemplateControllerImpl)")
    void withinFormTemplateController() {
        // Pointcut for methods in the form template controller controller
    }

    /**
     * Logs at info level when a form is selected.
     *
     * @param beforeCall the point in execution just before the postFrmTemplate endpoint is called.
     */
    @After("withinFormTemplateController() && execution(* postFormTemplate(..))")
    void formSelected(JoinPoint beforeCall) {
        JoinPointHelper helper = new JoinPointHelper(beforeCall);
        String id = helper.getArgument("id", String.class)
                .orElse("");

        String selectedForm = helper.getArgument(
                "formTemplateAttribute", FormTemplateModel.class)
                .map(FormTemplateModel::getFormName)
                .orElse("Unable to get form name");

        Map<String, Object> loggedData = new HashMap<>();
        loggedData.put("formSelected", selectedForm);

        logger.infoContext(id, "Form selected",
                loggedData);
    }

    @Pointcut("within(uk.gov.companieshouse.efs.web.controller.ConfirmationControllerImpl)")
    void withinConfirmation() {
        // Pointcut for methods in the confirmation controller
    }

    /**
     * Logs at info level when a submission is completed.
     *
     * @param joinPoint the point in execution just before the endpoint is called.
     */
    @After("withinConfirmation() && execution(* getConfirmation(String, String, ..))")
    void submissionCompleted(JoinPoint joinPoint) {
        JoinPointHelper helper = new JoinPointHelper(joinPoint);
        Map<String, Object> logFields = helper.getArguments(fieldsToLog::contains);

        helper.getArgument(
                "formTemplateAttribute", FormTemplateModel.class)
                .map(FormTemplateModel::getFormName)
                .ifPresent(formName -> logFields.put("formName", formName));

        String id = Optional.ofNullable(logFields.get("id"))
                .map(obj -> (String) obj)
                .orElse("");

        logger.infoContext(id, "Submission completed", logFields);
    }
}
