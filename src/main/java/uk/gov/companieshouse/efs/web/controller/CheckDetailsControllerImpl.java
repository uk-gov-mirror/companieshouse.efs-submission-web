package uk.gov.companieshouse.efs.web.controller;

import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants.INSOLVENCY;
import static uk.gov.companieshouse.efs.web.controller.CheckDetailsControllerImpl.ATTRIBUTE_NAME;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.submissions.ConfirmAuthorisedApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.model.CheckDetailsModel;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.efs.web.service.session.SessionService;
import uk.gov.companieshouse.efs.web.validation.ConfirmAuthorisedValidator;
import uk.gov.companieshouse.logging.Logger;

@Controller
@SessionAttributes(ATTRIBUTE_NAME)
@SuppressWarnings("squid:S3753")
/* S3753: "@Controller" classes that use "@SessionAttributes" must call "setComplete" on their "SessionStatus" objects
 *
 * The nature of the web journey across several controllers means it's not appropriate to do this. However,
 * setComplete() is properly called in ConfirmationControllerImpl at the end of the submission journey.
 */
public class CheckDetailsControllerImpl extends BaseControllerImpl implements CheckDetailsController {

    private CheckDetailsModel checkDetailsAttribute;
    private ConfirmAuthorisedValidator confirmAuthorisedValidator;

    /**
     * Define the model name for this action.
     */
    public static final String ATTRIBUTE_NAME = "checkDetails";

    /**
     * Constructor used by child controllers.
     *
     * @param logger the CH logger
     */
    @Autowired
    public CheckDetailsControllerImpl(final Logger logger, SessionService sessionService,
        ApiClientService apiClientService,
        final FormTemplateService formTemplateService,
        final CategoryTemplateService categoryTemplateService,
        CheckDetailsModel checkDetailsAttribute,
        ConfirmAuthorisedValidator confirmAuthorisedValidator) {
        super(logger, sessionService, apiClientService);
        this.formTemplateService = formTemplateService;
        this.categoryTemplateService = categoryTemplateService;
        this.checkDetailsAttribute = checkDetailsAttribute;
        this.confirmAuthorisedValidator = confirmAuthorisedValidator;
    }

    @ModelAttribute(ATTRIBUTE_NAME)
    public CheckDetailsModel getCheckDetailsAttribute() {
        return checkDetailsAttribute;
    }

    @Override
    public String getViewName() {
        return ViewConstants.CHECK_DETAILS.asView();
    }

    @Override
    public String checkDetails(@PathVariable String id, @PathVariable String companyNumber,
        @ModelAttribute(ATTRIBUTE_NAME) final CheckDetailsModel checkDetailsAttribute,
        Model model, HttpServletRequest request, HttpSession session, SessionStatus sessionStatus) {

        final SubmissionApi submission = getSubmission(id);

        if (submission.getStatus() != SubmissionStatus.OPEN) {
            return ViewConstants.GONE.asView();
        }

        addDataToModel(checkDetailsAttribute, model, submission);
        return ViewConstants.CHECK_DETAILS.asView();
    }

    @Override
    public String postCheckDetails(String id, final String companyNumber,
        @ModelAttribute(ATTRIBUTE_NAME) final CheckDetailsModel checkDetailsAttribute,
        BindingResult binding, final Model model, final HttpServletRequest request) {

        final SubmissionApi submission = getSubmission(id);
        confirmAuthorisedValidator.isValid(submission, checkDetailsAttribute, binding);

        if (binding.hasErrors()) {
            addDataToModel(checkDetailsAttribute, model, submission);
            return ViewConstants.CHECK_DETAILS.asView();
        }

        final ApiResponse<SubmissionResponseApi> response = apiClientService.putConfirmAuthorised(id, new ConfirmAuthorisedApi(checkDetailsAttribute.getConfirmAuthorised()));
        logApiResponse(response, id, "PUT /efs-submission-api/submission/" + id + "/confirmAuthorised");

        return ViewConstants.CONFIRMATION.asRedirectUri(chsUrl, id, companyNumber);
    }

    private void addDataToModel(
        @ModelAttribute(ATTRIBUTE_NAME) final CheckDetailsModel checkDetailsAttribute,
        final Model model, final SubmissionApi submission) {

        final String documentType = submission.getSubmissionForm().getFormType();
        final ApiResponse<FormTemplateApi> formResponse = formTemplateService.getFormTemplate(documentType);
        final String documentTypeDescription = formResponse.getData().getFormName();
        final String documentCategory = formResponse.getData().getFormCategory();
        final CategoryTypeConstants topLevelCategory = categoryTemplateService.getTopLevelCategory(documentCategory);

        checkDetailsAttribute.setSubmissionId(submission.getId());
        checkDetailsAttribute.setCompanyName(submission.getCompany().getCompanyName());
        checkDetailsAttribute.setCompanyNumber(submission.getCompany().getCompanyNumber());
        checkDetailsAttribute.setDocumentTypeDescription(documentTypeDescription);
        checkDetailsAttribute.setDocumentUploadedList(submission.getSubmissionForm().getFileDetails().getList());
        checkDetailsAttribute.setPaymentReference(submission.getPaymentReference());
        checkDetailsAttribute.setConfirmAuthorised(submission.getConfirmAuthorised());
        model.addAttribute("showAuthStatement", topLevelCategory == INSOLVENCY);
        addTrackingAttributeToModel(model);
    }
}