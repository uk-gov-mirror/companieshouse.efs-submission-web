package uk.gov.companieshouse.efs.web.validation;

import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants.INSOLVENCY;

import java.util.Locale;
import java.util.ResourceBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.model.CheckDetailsModel;

/**
 * Class representing the customer bean validator for confirm authorised.
 */
@Component
public class ConfirmAuthorisedValidator {

    private FormTemplateService formTemplateService;
    private CategoryTemplateService categoryTemplateService;
    private ResourceBundle bundle;

    /**
     * Constructor.
     *
     * @param formTemplateService       dependency
     * @param categoryTemplateService   dependency
     */
    @Autowired
    public ConfirmAuthorisedValidator(final FormTemplateService formTemplateService,
        CategoryTemplateService categoryTemplateService) {
        this.bundle = ResourceBundle.getBundle("messages", Locale.UK);
        this.formTemplateService = formTemplateService;
        this.categoryTemplateService = categoryTemplateService;
    }

    /**
     * Checks that confirm authorised selected for Insolvency category forms.
     *
     * @param submission            submission details
     * @param checkDetailsModel     check Details Model
     * @param binding               binding for error
     */
    public void isValid(final SubmissionApi submission, final CheckDetailsModel checkDetailsModel, final BindingResult binding) {

        final String documentType = submission.getSubmissionForm().getFormType();
        final ApiResponse<FormTemplateApi> formResponse = formTemplateService.getFormTemplate(documentType);
        final String documentCategory = formResponse.getData().getFormCategory();
        final CategoryTypeConstants topLevelCategory = categoryTemplateService.getTopLevelCategory(documentCategory);

        if (INSOLVENCY.equals(topLevelCategory) && (checkDetailsModel.getConfirmAuthorised() == null || !checkDetailsModel.getConfirmAuthorised().equals(true))) {
            String errorText = bundle.getString("checkDetails.confirmAuthorised.required");
            binding.rejectValue("confirmAuthorised","confirmAuthorised", errorText);
        }
    }

}
