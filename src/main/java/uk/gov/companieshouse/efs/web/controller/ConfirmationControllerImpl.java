package uk.gov.companieshouse.efs.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.web.formtemplates.controller.FormTemplateControllerImpl;
import uk.gov.companieshouse.efs.web.formtemplates.model.FormTemplateModel;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.efs.web.service.session.SessionService;
import uk.gov.companieshouse.logging.Logger;

@Controller
@SessionAttributes(FormTemplateControllerImpl.ATTRIBUTE_NAME)
public class ConfirmationControllerImpl extends BaseControllerImpl implements ConfirmationController {

    /**
     * Constructor used by child controllers.
     *
     * @param logger the CH logger
     */
    @Autowired
    public ConfirmationControllerImpl(final Logger logger, SessionService sessionService,
        ApiClientService apiClientService) {
        super(logger, sessionService, apiClientService);
    }

    @Override
    public String getViewName() {
        return ViewConstants.CONFIRMATION.asView();
    }

    @Override
    public String getConfirmation(@PathVariable String id, @PathVariable String companyNumber,
        @ModelAttribute(FormTemplateControllerImpl.ATTRIBUTE_NAME) final FormTemplateModel formTemplateAttribute,
        Model model, HttpServletRequest request, HttpSession session, SessionStatus sessionStatus) {

        final SubmissionApi submission = getSubmission(id);

        if (submission.getStatus() != SubmissionStatus.OPEN) {
            return ViewConstants.GONE.asView();
        }

        final ApiResponse<SubmissionResponseApi> response = apiClientService.putSubmissionSubmitted(id);

        logApiResponse(response, id, "PUT /efs-submission-api/submission/" + id);
        model.addAttribute("confirmationRef", submission.getConfirmationReference());
        model.addAttribute("companyName", submission.getCompany().getCompanyName());
        model.addAttribute("newSubmissionUri",
            ViewConstants.NEW_SUBMISSION.asUriForCompany(chsUrl, submission.getCompany().getCompanyNumber()));

        addTrackingAttributeToModel(model);
        sessionStatus.setComplete();

        return ViewConstants.CONFIRMATION.asView();
    }
}
