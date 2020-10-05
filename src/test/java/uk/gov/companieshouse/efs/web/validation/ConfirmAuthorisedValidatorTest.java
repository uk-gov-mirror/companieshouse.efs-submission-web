package uk.gov.companieshouse.efs.web.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.ResourceBundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionFormApi;
import uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.model.CheckDetailsModel;

@ExtendWith(MockitoExtension.class)
class ConfirmAuthorisedValidatorTest {

    private ConfirmAuthorisedValidator testValidator;
    private CheckDetailsModel checkDetailsModel;
    private static final String FORM_TYPE = "AM01";
    private static final String FORM_CATEGORY = "ADMIN";
    private Map<String, Object> headers;
    @Mock
    private FormTemplateService formTemplateService;
    @Mock
    private CategoryTemplateService categoryTemplateService;
    @Mock
    private ResourceBundle resourceBundle;

    @BeforeEach
    protected void setUp() {
        testValidator = new ConfirmAuthorisedValidator(formTemplateService, categoryTemplateService);
        checkDetailsModel = new CheckDetailsModel();
    }

    @Test
    void isValidWhenConfirmAuthorisedTrue() {
        final SubmissionApi submission = createSubmission();
        checkDetailsModel.setConfirmAuthorised(true);
        BindingResult binding = new BeanPropertyBindingResult(checkDetailsModel, "checkDetails");
        FormTemplateApi formTemplate = new FormTemplateApi();
        formTemplate.setFormCategory(FORM_CATEGORY);
        when(formTemplateService.getFormTemplate(FORM_TYPE)).thenReturn(
            new ApiResponse<FormTemplateApi>(200, headers, formTemplate));
        when(categoryTemplateService.getTopLevelCategory(FORM_CATEGORY)).thenReturn(
            CategoryTypeConstants.ARTICLES);

        testValidator.isValid(submission, checkDetailsModel, binding);

        assertThat(binding.hasErrors(), is(Boolean.FALSE));
    }

    @Test
    void isValidWhenConfirmAuthorisedNull() {
        final SubmissionApi submission = createSubmission();
        checkDetailsModel.setConfirmAuthorised(null);
        BindingResult binding = new BeanPropertyBindingResult(checkDetailsModel, "checkDetails");
        FormTemplateApi formTemplate = new FormTemplateApi();
        formTemplate.setFormCategory(FORM_CATEGORY);
        when(formTemplateService.getFormTemplate(FORM_TYPE)).thenReturn(
            new ApiResponse<FormTemplateApi>(200, headers, formTemplate));
        when(categoryTemplateService.getTopLevelCategory(FORM_CATEGORY)).thenReturn(
            CategoryTypeConstants.INSOLVENCY);

        testValidator.isValid(submission, checkDetailsModel, binding);

        assertThat(binding.hasErrors(), is(Boolean.TRUE));
    }

    protected SubmissionApi createSubmission() {
        final SubmissionApi submission = new SubmissionApi();
        submission.setSubmissionForm(new SubmissionFormApi());
        submission.getSubmissionForm().setFormType(FORM_TYPE);
        return submission;
    }
}