package uk.gov.companieshouse.efs.web.controller;

import static uk.gov.companieshouse.efs.web.controller.RemoveDocumentControllerImpl.ATTRIBUTE_NAME;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.submissions.FileApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileDetailApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileDetailListApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileListApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionFormApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.web.model.RemoveDocumentModel;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.efs.web.service.session.SessionService;
import uk.gov.companieshouse.efs.web.transfer.FileTransferApiClient;
import uk.gov.companieshouse.efs.web.transfer.FileTransferApiClientResponse;
import uk.gov.companieshouse.logging.Logger;

@Controller
@SessionAttributes(ATTRIBUTE_NAME)
@SuppressWarnings("squid:S3753")
/* S3753: "@Controller" classes that use "@SessionAttributes" must call "setComplete" on their "SessionStatus" objects
 *
 * The nature of the web journey across several controllers means it's not appropriate to do this. However,
 * setComplete() is properly called in ConfirmationControllerImpl at the end of the submission journey.
 */
public class RemoveDocumentControllerImpl extends BaseControllerImpl implements RemoveDocumentController {

    /**
     * Define the model name for this action.
     */
    public static final String ATTRIBUTE_NAME = "removeDocument";

    private FileTransferApiClient fileTransferApiClient;
    private RemoveDocumentModel removeDocumentAttribute;

    /**
     * Constructor used by child controllers.
     *
     * @param logger the CH logger
     */
    @Autowired
    public RemoveDocumentControllerImpl(final FileTransferApiClient fileTransferApiClient, final Logger logger,
        final SessionService sessionService, final ApiClientService apiClientService,
        final RemoveDocumentModel removeDocumentAttribute) {
        super(logger, sessionService, apiClientService);
        this.fileTransferApiClient = fileTransferApiClient;
        this.removeDocumentAttribute = removeDocumentAttribute;
    }

    @ModelAttribute(ATTRIBUTE_NAME)
    public RemoveDocumentModel getRemoveDocumentAttribute() {
        return removeDocumentAttribute;
    }

    @Override
    public String getViewName() {
        return ViewConstants.REMOVE_DOCUMENT.asView();
    }

    @Override
    public String prepare(@PathVariable String id, @PathVariable String companyNumber, @PathVariable String fileId,
        @ModelAttribute(ATTRIBUTE_NAME) RemoveDocumentModel removeDocumentAttribute, Model model,
        HttpServletRequest servletRequest) {

        final SubmissionApi submissionApi = Objects.requireNonNull(getSubmission(id));

        if (!verifySubmission(submissionApi)) {
            return ViewConstants.ERROR.asView();
        }

        FileDetailListApi fileDetailsListApi = Optional.ofNullable(submissionApi.getSubmissionForm().getFileDetails())
            .orElseGet(FileDetailListApi::new);
        Optional<FileDetailApi> fileDetailApi = fileDetailsListApi.getList().stream().filter(
            file -> StringUtils.equals(file.getFileId(), fileId)).findFirst();

        if (!fileDetailApi.isPresent()) {
            return ViewConstants.ERROR.asView();
        }

        removeDocumentAttribute.setSubmissionId(submissionApi.getId());
        removeDocumentAttribute.setFileName(fileDetailApi.get().getFileName());
        removeDocumentAttribute.setRequired("");    // reset as unanswered

        addTrackingAttributeToModel(model);

        return ViewConstants.REMOVE_DOCUMENT.asView();
    }

    @Override
    public String process(@PathVariable String id, @PathVariable String companyNumber, @PathVariable String fileId,
        @Valid @ModelAttribute(ATTRIBUTE_NAME) RemoveDocumentModel removeDocumentAttribute, BindingResult binding,
        Model model, HttpServletRequest servletRequest, HttpSession session) {

        final SubmissionApi submissionApi = Objects.requireNonNull(getSubmission(id));

        if (!verifySubmission(submissionApi)) {
            return ViewConstants.ERROR.asView();
        }
        if (binding.hasErrors()) {
            addTrackingAttributeToModel(model);
            return ViewConstants.REMOVE_DOCUMENT.asView();
        }

        String redirectUri = ViewConstants.DOCUMENT_UPLOAD.asRedirectUri(chsUrl, id, companyNumber);

        if (StringUtils.equals(removeDocumentAttribute.getRequired(), "Y")) {
            redirectUri = performRemoveDocument(submissionApi.getId(), fileId, binding,
                submissionApi.getSubmissionForm());

            if (!binding.hasErrors()) {
                redirectUri = ViewConstants.DOCUMENT_UPLOAD.asRedirectUri(chsUrl, id, companyNumber);

                // TODO: Find a better way of resetting this field.
                removeDocumentAttribute.setRequired("");
            }
        }

        return redirectUri;
    }

    private String performRemoveDocument(@PathVariable final String id, @PathVariable final String fileId,
        final BindingResult binding, final SubmissionFormApi submissionFormApi) {

        final SubmissionApi submissionApi = Objects.requireNonNull(getSubmission(id));

        if (!verifySubmission(submissionApi)) {
            return ViewConstants.ERROR.asView();
        }

        // Locate the evidence that we want to remove.
        Optional<FileDetailApi> fileDetails = submissionFormApi.getFileDetails().getList().stream().filter(
                e -> StringUtils.equals(e.getFileId(), fileId)
        ).findFirst();

        String result = ViewConstants.REMOVE_DOCUMENT.asView();

        if (fileDetails.isPresent()) {
            final String fileToDeleteId = fileDetails.get().getFileId();

            // Attempt to remove the evidence from the S3 bucket.
            final FileTransferApiClientResponse apiClientResponse = fileTransferApiClient.delete(fileToDeleteId);

            // On a failed removal attempt, we need to alert the end user.
            if (apiClientResponse.getHttpStatus() != HttpStatus.NO_CONTENT) {
                result = ViewConstants.ERROR.asView();

            } else {

                // Update the application with the selected document files removed.
                List<FileApi> fileApiList = submissionFormApi.getFileDetails().getList().stream()
                        .filter(file -> !file.getFileId().equals(fileId))
                        .map(file -> new FileApi(file.getFileId(), file.getFileName(), file.getFileSize()))
                        .collect(Collectors.toList());

                ApiResponse<SubmissionResponseApi> response = apiClientService.putFileList(id, new FileListApi(fileApiList));
                logApiResponse(response, id, "PUT /efs-submission-api/submission/" + id + "/files");

                addAnyErrorsFromResponse(binding, response, e -> true);
            }
        }

        return result;
    }
}
