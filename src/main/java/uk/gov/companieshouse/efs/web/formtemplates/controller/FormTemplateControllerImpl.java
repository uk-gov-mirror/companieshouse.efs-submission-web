package uk.gov.companieshouse.efs.web.formtemplates.controller;

import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants.INSOLVENCY;
import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants.OTHER;
import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants.RESOLUTIONS;
import static uk.gov.companieshouse.efs.web.formtemplates.controller.FormTemplateControllerImpl.ATTRIBUTE_NAME;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.ServletRequest;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateListApi;
import uk.gov.companieshouse.api.model.efs.submissions.FormTypeApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTemplateControllerImpl;
import uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants;
import uk.gov.companieshouse.efs.web.categorytemplates.model.CategoryTemplateModel;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.controller.BaseControllerImpl;
import uk.gov.companieshouse.efs.web.controller.ViewConstants;
import uk.gov.companieshouse.efs.web.formtemplates.model.FormTemplateModel;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.efs.web.service.session.SessionService;
import uk.gov.companieshouse.logging.Logger;

@Controller
@SessionAttributes({ATTRIBUTE_NAME, CategoryTemplateControllerImpl.ATTRIBUTE_NAME})
@SuppressWarnings("squid:S3753")
/* S3753: "@Controller" classes that use "@SessionAttributes" must call "setComplete" on their "SessionStatus" objects
 *
 * The nature of the web journey across several controllers means it's not appropriate to do this. However,
 * setComplete() is properly called in ConfirmationControllerImpl at the end of the submission journey.
 */
public class FormTemplateControllerImpl extends BaseControllerImpl implements FormTemplateController {

    private FormTemplateModel formTemplateAttribute;

    /**
     * Define the model name for this action.
     */
    public static final String ATTRIBUTE_NAME = "formTemplate";

    /**
     * Constructor form {@link FormTemplateControllerImpl}.
     *
     * @param formTemplateService the form template service
     * @param logger              the CH logger
     */
    @Autowired
    public FormTemplateControllerImpl(final CategoryTemplateService categoryTemplateService,
        final FormTemplateService formTemplateService, final ApiClientService apiClientService,
        final SessionService sessionService, final Logger logger, final FormTemplateModel formTemplateAttribute) {
        super(logger, sessionService, apiClientService, formTemplateService, categoryTemplateService);
        this.formTemplateAttribute = formTemplateAttribute;
    }

    @ModelAttribute(ATTRIBUTE_NAME)
    public FormTemplateModel getFormTemplateAttribute() {
        return formTemplateAttribute;
    }

    @Override
    public String getViewName() {
        return ViewConstants.DOCUMENT_SELECTION.asView();
    }

    @Override
    public String formTemplate(@PathVariable String id, @PathVariable String companyNumber,
            @RequestParam("category") String category,
            @SessionAttribute(CategoryTemplateControllerImpl.ATTRIBUTE_NAME)
                    CategoryTemplateModel categoryTemplateAttribute,
            @ModelAttribute(ATTRIBUTE_NAME) FormTemplateModel formTemplateAttribute,
            final Model model, ServletRequest servletRequest) {

        final SubmissionApi submissionApi = Objects.requireNonNull(getSubmission(id));

        if (submissionApi.getStatus() != SubmissionStatus.OPEN) {
            return ViewConstants.GONE.asView();
        }

        final CategoryTypeConstants topLevelCategory = categoryTemplateService.getTopLevelCategory(category);
        final Boolean isEmailAllowed = apiClientService.isOnAllowList(
                submissionApi.getPresenter().getEmail()).getData();

        if (topLevelCategory == INSOLVENCY && Boolean.FALSE.equals(isEmailAllowed)) {
            return ViewConstants.MISSING.asView();
        }

        // refresh form template list from repository
        final ApiResponse<FormTemplateListApi> formResponse = formTemplateService.getFormTemplatesByCategory(category);
        final List<FormTemplateApi> formList = formResponse.getData().getList();

        formTemplateAttribute.setFormTemplateList(formList);
        formTemplateAttribute.setSubmissionId(submissionApi.getId());

        final Optional<FormTemplateApi> selectedForm = formTemplateAttribute.getFormTemplateList().stream()
            .filter(f -> f.getFormType().equals(formTemplateAttribute.getFormType()))
            .findFirst();
        // selected form category must match the category otherwise the user hasn't selected
        // a form for the last selected category
        final boolean formNotSelected = !selectedForm.isPresent()
                                        || !StringUtils.equals(selectedForm.get().getFormCategory(),
            categoryTemplateAttribute.getCategoryType());
        if (formNotSelected) {
            formTemplateAttribute.setDetails(new FormTemplateApi());
        }

        model.addAttribute("categoryName", categoryTemplateAttribute.getCategoryName());
        model.addAttribute("isScottishCompany", isScottishCompany(companyNumber));
        addTrackingAttributeToModel(model);

        return ViewConstants.DOCUMENT_SELECTION.asView();
    }

    @Override
    @PostMapping(value = {"{id}/company/{companyNumber}/document-selection"},
            params = {"action=submit"})
    public String postFormTemplate(@PathVariable String id, @PathVariable String companyNumber,
            @SessionAttribute(CategoryTemplateControllerImpl.ATTRIBUTE_NAME)
                    CategoryTemplateModel categoryTemplateAttribute,
            @Valid @ModelAttribute(ATTRIBUTE_NAME) FormTemplateModel formTemplateAttribute,
            BindingResult binding, Model model, ServletRequest servletRequest) {

        if (binding.hasErrors()) {
            model.addAttribute("isScottishCompany", isScottishCompany(companyNumber));
            addTrackingAttributeToModel(model);

            return ViewConstants.DOCUMENT_SELECTION.asView();
        }

        final String selectedFormType = formTemplateAttribute.getFormType();
        final FormTemplateApi selectedFormTemplate = getSelectedFormTemplate(selectedFormType);

        // Call the API layer to persist the form type.
        ApiResponse<SubmissionResponseApi> submissionResponse = apiClientService.putFormType(id,
            new FormTypeApi(selectedFormType));
        logApiResponse(submissionResponse, id, "Form type: " + submissionResponse.getData().getId());

        formTemplateAttribute.setDetails(selectedFormTemplate);

        if (CategoryTypeConstants.nameOf(selectedFormType).orElse(OTHER) == RESOLUTIONS) {
            return ViewConstants.RESOLUTIONS_INFO.asRedirectUri(chsUrl, id, companyNumber);
        } else {
            return ViewConstants.DOCUMENT_UPLOAD.asRedirectUri(chsUrl, id, companyNumber);
        }
    }

    private FormTemplateApi getSelectedFormTemplate(final String selectedFormType) {
        return formTemplateAttribute.getFormTemplateList().stream().filter(
                c -> c.getFormType().equals(selectedFormType)).findFirst().orElse(null);
    }

    boolean isScottishCompany(String companyNumber) {
        return Stream.of("SC","SF","SO").anyMatch(companyNumber::startsWith);
    }
}