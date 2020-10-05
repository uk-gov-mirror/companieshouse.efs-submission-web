package uk.gov.companieshouse.efs.web.security;

import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants.INSOLVENCY;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionFormApi;
import uk.gov.companieshouse.auth.filter.AuthFilter;
import uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.session.Session;
import uk.gov.companieshouse.session.handler.SessionHandler;
import uk.gov.companieshouse.session.model.SignInInfo;
import uk.gov.companieshouse.session.model.UserProfile;

public class CompanyAuthFilter extends AuthFilter {

    private static final Pattern EFS_SUBMISSION_WITH_COMPANY = Pattern.compile("^/efs-submission/(.+)/company/([^/]+).*");

    private static final Pattern AUTH_COMPANY_SCOPE = Pattern.compile("/company/([0-9a-zA-Z]*)$");

    private ApiClientService apiClientService;

    private FormTemplateService formTemplateService;

    private CategoryTemplateService categoryTemplateService;

    /**
     * Constructor.
     *
     * @param environmentReader         dependency
     * @param apiClientService          dependency
     * @param formTemplateService       dependency
     * @param categoryTemplateService   dependency
     */
    public CompanyAuthFilter(final EnvironmentReader environmentReader,
        final ApiClientService apiClientService,
        final FormTemplateService formTemplateService,
        CategoryTemplateService categoryTemplateService) {
        super(environmentReader);
        this.apiClientService = apiClientService;
        this.formTemplateService = formTemplateService;
        this.categoryTemplateService = categoryTemplateService;
    }

    @Override
    public void doFilter(
        ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        Matcher matcher = EFS_SUBMISSION_WITH_COMPANY.matcher(httpServletRequest.getRequestURI());

        if ("GET".equalsIgnoreCase(httpServletRequest.getMethod()) && matcher.find()) {

            String efsSubmissionId = matcher.group(1);

            ApiResponse<SubmissionApi> submissionApiResponse = apiClientService.getSubmission(efsSubmissionId);

            SubmissionApi submissionApi = submissionApiResponse.getData();

            SubmissionFormApi submissionFormApi = submissionApi.getSubmissionForm();

            // The form could be null if the user hasn't progressed far enough in the journey to select a form type
            if (submissionFormApi != null) {

                String formType = submissionFormApi.getFormType();

                ApiResponse<FormTemplateApi> formTemplateResponse = formTemplateService.getFormTemplate(formType);

                String formCategory = formTemplateResponse.getData().getFormCategory();

                boolean isAuthenticationRequired = formTemplateResponse.getData().isAuthenticationRequired();

                if (isAuthenticationRequired) {
                    //Get the sign in info from the CH session
                    Session chSession = (Session) request.getAttribute(SessionHandler.CHS_SESSION_REQUEST_ATT_KEY);
                    SignInInfo signInInfo = new SignInInfo();
                    if (chSession != null) {
                        signInInfo = chSession.getSignInInfo();
                    }

                    String companyNumber = matcher.group(2);

                    /*
                        According to the sign in info, check:
                        If the company number is authorised
                        If the email address is on the allow list for insolvency forms
                     */
                    if (!isAuthorisedForCompany(signInInfo, companyNumber)
                        && !isOnAllowList(signInInfo, formCategory)) {

                        //Redirect for company authentication (scope specified)
                        redirectForAuth(chSession, httpServletRequest, httpServletResponse, companyNumber, false);
                    }
                }
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Determines whether a signed in user is authorised to act on behalf of a company.
     * @param signInInfo User's signed in info retrieved from their session
     * @param companyNumber The company to act on behalf of
     * @return true or false
     */
    private boolean isAuthorisedForCompany(SignInInfo signInInfo, String companyNumber) {
        String authorisedCompany = signInInfo.getCompanyNumber();
        return (authorisedCompany != null
            && authorisedCompany.equalsIgnoreCase(companyNumber))
            && hasAuthorisedCompanyScope(signInInfo, companyNumber);
    }

    /**
     * Determines whether a signed in user has authorised scope to act on behalf of a company.
     * @param signInInfo User's signed in info retrieved from their session
     * @param companyNumber The company to act on behalf of
     * @return true or false
     */
    private boolean hasAuthorisedCompanyScope(SignInInfo signInInfo, String companyNumber) {

        if (signInInfo.getUserProfile() != null && signInInfo.getUserProfile().getScope() != null) {

            String[] scopes = signInInfo.getUserProfile().getScope().split(" ");
            for (String scope : scopes) {
                Matcher m = AUTH_COMPANY_SCOPE.matcher(scope);
                if (m.find() && m.group(1).equalsIgnoreCase(companyNumber)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Determines whether a signed in users email address is within the company authentication allow list.
     * If the email address is within the allow list, and an insolvency form has been selected, then
     * the company does not require authentication
     * @param signInInfo User's signed in info retrieved from their session
     * @param formCategory The form category
     * @return true or false
     */
    private boolean isOnAllowList(SignInInfo signInInfo, String formCategory) {
        String userEmail = Optional.ofNullable(signInInfo.getUserProfile()).orElseGet(UserProfile::new).getEmail();

        final CategoryTypeConstants topLevelCategory = categoryTemplateService.getTopLevelCategory(formCategory);

        if (topLevelCategory == INSOLVENCY) {
            return apiClientService.isOnAllowList(userEmail).getData();
        }

        return false;
    }
}
