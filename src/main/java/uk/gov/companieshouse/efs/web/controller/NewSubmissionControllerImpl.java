package uk.gov.companieshouse.efs.web.controller;

import java.text.MessageFormat;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.web.model.company.CompanyDetail;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.efs.web.service.session.SessionService;
import uk.gov.companieshouse.logging.Logger;

@Controller
@SessionAttributes(CompanyDetailControllerImpl.ATTRIBUTE_NAME)
@SuppressWarnings("squid:S3753")
/* S3753: "@Controller" classes that use "@SessionAttributes" must call "setComplete" on their "SessionStatus" objects
 *
 * The nature of the web journey across several controllers means it's not appropriate to do this. However,
 * setComplete() is properly called in ConfirmationControllerImpl at the end of the submission journey.
 */
public class NewSubmissionControllerImpl extends BaseControllerImpl implements NewSubmissionController {

    @Autowired
    public NewSubmissionControllerImpl(final Logger logger, final SessionService sessionService,
        final ApiClientService apiClientService) {
        super(logger, sessionService, apiClientService);
    }

    @Override
    public String getViewName() {
        return ViewConstants.NEW_SUBMISSION.asView();
    }

    @Override
    public String newSubmission(
        @ModelAttribute(CompanyDetailControllerImpl.ATTRIBUTE_NAME) CompanyDetail companyDetailAttribute,
        final SessionStatus sessionStatus, final HttpServletRequest request, RedirectAttributes attributes) {

        String newSubmissionId;

        companyDetailAttribute.clear();

        newSubmissionId = createNewSubmission();
        companyDetailAttribute.setSubmissionId(newSubmissionId);
        storeOriginalSubmissionId(newSubmissionId);

        attributes.addAttribute("forward",
            String.format("/efs-submission/%s/company/{companyNumber}/details", newSubmissionId));

        return ViewConstants.COMPANY_LOOKUP.asRedirectUri(chsUrl, newSubmissionId);
    }

    @Override
    public String newSubmissionForCompany(final String companyNumber, final CompanyDetail companyDetailAttribute,
        final SessionStatus sessionStatus, final HttpServletRequest request, RedirectAttributes attributes) {
        String newSubmissionId;

        if (!StringUtils.equals(companyDetailAttribute.getCompanyNumber(), companyNumber)) {
            logger.errorRequest(request, "Company number in URL does not match companyDetailAttribute.companyNumber");
            return ViewConstants.ERROR.asView();
        }

        newSubmissionId = createNewSubmission();

        ApiResponse<SubmissionResponseApi> response = apiClientService.putCompany(newSubmissionId,
            new CompanyApi(companyDetailAttribute.getCompanyNumber(), companyDetailAttribute.getCompanyName()));

        logApiResponse(response, "",
            MessageFormat.format("PUT /efs-submission-api/submission/{0}/company", newSubmissionId));
        storeOriginalSubmissionId(newSubmissionId);


        return ViewConstants.CATEGORY_SELECTION
            .asRedirectUri(chsUrl, newSubmissionId, companyDetailAttribute.getCompanyNumber());
    }

    private String createNewSubmission() {
        final String newSubmissionId;
        PresenterApi sessionPresenter = new PresenterApi(sessionService.getUserEmail());
        ApiResponse<SubmissionResponseApi> response = apiClientService.createSubmission(sessionPresenter);

        logApiResponse(response, "", "POST /efs-submission-api/submissions/new");
        newSubmissionId = response.getData().getId();

        return newSubmissionId;
    }

    /**
     * Store the original submission that we started the journey with.
     *
     * @param sessionId the id to store
     */
    private void storeOriginalSubmissionId(final String sessionId) {
        sessionService.getSessionDataFromContext().put(ORIGINAL_SUBMISSION_ID, sessionId);
    }
}
