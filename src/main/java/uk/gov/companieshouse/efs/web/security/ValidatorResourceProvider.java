package uk.gov.companieshouse.efs.web.security;

import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionFormApi;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.session.Session;
import uk.gov.companieshouse.session.handler.SessionHandler;
import uk.gov.companieshouse.session.model.SignInInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorResourceProvider {
    private static final Pattern EFS_SUBMISSION_WITH_COMPANY = Pattern.compile("^/efs-submission/(?<submissionId>.+)/company/(?<companyNumber>[^/]+).*");

    private final FormTemplateService formTemplateService;
    private final ApiClientService apiClientService;
    private final HttpServletRequest request;
    private SubmissionApi submission;
    private FormTemplateApi form;
    private SignInInfo signInInfo;

    public ValidatorResourceProvider(HttpServletRequest request, ApiClientService apiClientService, FormTemplateService formTemplateService) {
        this.formTemplateService = formTemplateService;
        this.request = request;
        this.apiClientService = apiClientService;
    }

    private Optional<SubmissionApi> getSubmissionFromRequest() {
        Matcher urlMatcher = getRequestPathMatcher();
        if (urlMatcher.find()) {
            String submissionId = urlMatcher.group(1);
             return Optional.ofNullable(apiClientService.getSubmission(submissionId))
                    .map(ApiResponse::getData);
        }

        return Optional.empty();
    }

    Optional<SubmissionApi> getSubmission() {
        if (submission != null) {
            return Optional.of(submission);
        }

        return getSubmissionFromRequest();
    }

    public HttpServletRequest getInput() {
        return request;
    }

    Matcher getRequestPathMatcher() {
        return EFS_SUBMISSION_WITH_COMPANY.matcher(request.getRequestURI());
    }

    private Optional<FormTemplateApi> getFormFromSubmission() {
        return getSubmission()
                .map(SubmissionApi::getSubmissionForm)
                .map(SubmissionFormApi::getFormType)
                .map(formTemplateService::getFormTemplate)
                .map(ApiResponse::getData);
    }

    Optional<FormTemplateApi> getForm() {
        if (form != null) {
            return Optional.of(form);
        }

        return getFormFromSubmission();
    }

    private Optional<SignInInfo> getSignInInfoFromRequest() {
        Session session = (Session) request.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY);
        return Optional.ofNullable(session)
                .map(Session::getSignInInfo);
    }

    Optional<SignInInfo> getSignInInfo() {
        if (signInInfo != null) {
            return Optional.of(signInInfo);
        }

        return getSignInInfoFromRequest();
    }


    ApiClientService getApiClientService() {
        return apiClientService;
    }

    Optional<String> getCompanyNumber() {
        Matcher urlPathMatcher = getRequestPathMatcher();
        if (!urlPathMatcher.find()) {
            return Optional.empty();
        }

        return Optional.ofNullable(urlPathMatcher.group("companyNumber"));
    }
}
