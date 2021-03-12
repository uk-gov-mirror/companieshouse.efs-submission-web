package uk.gov.companieshouse.efs.web.formtemplates.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateListApi;
import uk.gov.companieshouse.api.model.efs.submissions.FormTypeApi;
import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants;
import uk.gov.companieshouse.efs.web.controller.BaseControllerImplTest;
import uk.gov.companieshouse.efs.web.controller.ViewConstants;

@ExtendWith(MockitoExtension.class)
class FormTemplateControllerTest extends BaseControllerImplTest {

    /*
     *                     ROOT_CATEGORY
     *                    /           \
     *                   /             \
     *                  /               \
     *                CAT_TOP_LEVEL   INSOLVENCY
     *                   /        \
     *                  /          \
     *           CAT1_SUB_LEVEL1   CAT2_SUB_LEVEL1
     *            /           \
     *           /             \
     *     CAT1_SUB_LEVEL2   CAT2_SUB_LEVEL2
     */
    public static final CategoryTemplateApi INSOLVENCY = new CategoryTemplateApi("INS",
            "Insolvency", "", null);
    public static final CategoryTemplateApi CAT1_SUB_LEVEL1 = new CategoryTemplateApi(
            "CAT1_SUB_LEVEL1", "Dummy category 1, subcategory level 1", "CAT_TOP_LEVEL", null);
    public static final CategoryTemplateApi CAT2_SUB_LEVEL1 = new CategoryTemplateApi(
            "CAT2_SUB_LEVEL1", "Dummy category 2, subcategory level 1", "CAT_TOP_LEVEL", null);

    public static final CategoryTemplateApi INS_SUB_LEVEL1 = new CategoryTemplateApi(
            "INS_SUB_LEVEL1", "Dummy ins category 1, subcategory level 1", "INS", null);

    public static final FormTemplateApi FORM_TEMPLATE_1 = new FormTemplateApi("CC01", "Test01", "CAT1_SUB_LEVEL1",
            "100", true, true, null);
    public static final FormTemplateApi FORM_TEMPLATE_2 = new FormTemplateApi("CC02", "Test02", "CAT1_SUB_LEVEL1",
            "100", true, true, null);
    public static final FormTemplateApi FORM_TEMPLATE_3 = new FormTemplateApi("CC03", "Test03", "CAT1_SUB_LEVEL1",                          "100", true, true, null);
    public static final FormTemplateApi INS_TEMPLATE_1 = new FormTemplateApi("INS", "InsTest04", "INS_SUB_LEVEL1",
            "100", true, true, null);
    public static final List<FormTemplateApi> FORM_TEMPLATE_LIST = Arrays.asList(FORM_TEMPLATE_1, FORM_TEMPLATE_2, FORM_TEMPLATE_3);

    private FormTemplateController testController;

    final static String PRESENTER_EMAIL = "test@email.com";
    final static String SCOTTISH_COMPANY_NUMBER_1 = "SC000000";
    final static String SCOTTISH_COMPANY_NUMBER_2 = "SF000000";
    final static String SCOTTISH_COMPANY_NUMBER_3 = "SO000000";

    @BeforeEach
    protected void setUp() {
        setUpHeaders();
        testController = new FormTemplateControllerImpl(categoryTemplateService,
                formTemplateService, apiClientService, sessionService, logger,
                formTemplateAttribute);
        ((FormTemplateControllerImpl) testController).setChsUrl(CHS_URL);
    }

    @Test
    void getFormTemplateAttribute() {
        assertThat(((FormTemplateControllerImpl) testController).getFormTemplateAttribute(),
                is(sameInstance(formTemplateAttribute)));
    }

    @Test
    void getViewName() {
        assertThat(((FormTemplateControllerImpl) testController).getViewName(),
                is(ViewConstants.DOCUMENT_SELECTION.asView()));
    }

    @Test
    void formTemplateWhenSubmissionNotOpen() {

        String category = "CC01";

        final SubmissionApi submission = createSubmission(SubmissionStatus.SUBMITTED);

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
                getSubmissionOkResponse(submission));

        final String result = testController.formTemplate(SUBMISSION_ID, COMPANY_NUMBER, category, categoryTemplateAttribute,
                formTemplateAttribute, model, servletRequest);

        verifyNoInteractions(formTemplateService, formTemplateAttribute, model);
        assertThat(result, is(ViewConstants.GONE.asView()));
    }

    @Test
    void formTemplateWhenSubmissionOpen() {

        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        PresenterApi presenter = new PresenterApi();
        presenter.setEmail(PRESENTER_EMAIL);
        submission.setPresenter(presenter);

        expectOpenSubmission(submission, CAT1_SUB_LEVEL1);

        String template = testController.formTemplate(SUBMISSION_ID, COMPANY_NUMBER, CAT1_SUB_LEVEL1.getCategoryType(),
                categoryTemplateAttribute, formTemplateAttribute, model, servletRequest);

        verify(formTemplateAttribute).setFormTemplateList(FORM_TEMPLATE_LIST);
        verify(formTemplateAttribute).setSubmissionId(SUBMISSION_ID);
        verify(model).addAttribute("categoryName", categoryTemplateAttribute.getCategoryName());
        verify(model).addAttribute(TEMPLATE_NAME, ViewConstants.DOCUMENT_SELECTION.asView());

        assertThat(template, is(ViewConstants.DOCUMENT_SELECTION.asView()));

    }

    @Test
    void formTemplateWhenSubmissionOpenAndFormSelectedSameCategory() {

        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        PresenterApi presenter = new PresenterApi();
        presenter.setEmail(PRESENTER_EMAIL);
        submission.setPresenter(presenter);

        expectOpenSubmission(submission, CAT1_SUB_LEVEL1);

        when(categoryTemplateAttribute.getCategoryType()).thenReturn(CAT1_SUB_LEVEL1.getCategoryType());

        when(formTemplateAttribute.getFormTemplateList()).thenReturn(FORM_TEMPLATE_LIST);

        when(formTemplateAttribute.getFormType()).thenReturn(FORM_TEMPLATE_1.getFormType());

        String template = testController.formTemplate(SUBMISSION_ID, COMPANY_NUMBER, CAT1_SUB_LEVEL1.getCategoryType(),
            categoryTemplateAttribute, formTemplateAttribute, model, servletRequest);

        verify(formTemplateAttribute, never()).setDetails(any(FormTemplateApi.class));
        verify(formTemplateAttribute).setFormTemplateList(FORM_TEMPLATE_LIST);
        verify(formTemplateAttribute).setSubmissionId(SUBMISSION_ID);
        verify(model).addAttribute("categoryName", categoryTemplateAttribute.getCategoryName());
        verify(model).addAttribute(TEMPLATE_NAME, ViewConstants.DOCUMENT_SELECTION.asView());

        assertThat(template, is(ViewConstants.DOCUMENT_SELECTION.asView()));

    }

    @Test
    void formTemplateWhenSubmissionOpenAndFormSelectedDifferentCategory() {

        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        PresenterApi presenter = new PresenterApi();
        presenter.setEmail(PRESENTER_EMAIL);
        submission.setPresenter(presenter);

        expectOpenSubmission(submission, CAT2_SUB_LEVEL1);

        when(categoryTemplateAttribute.getCategoryType()).thenReturn(CAT2_SUB_LEVEL1.getCategoryType());

        when(formTemplateAttribute.getFormTemplateList()).thenReturn(FORM_TEMPLATE_LIST);

        when(formTemplateAttribute.getFormType()).thenReturn(FORM_TEMPLATE_1.getFormType());

        String template = testController.formTemplate(SUBMISSION_ID, COMPANY_NUMBER, CAT2_SUB_LEVEL1.getCategoryType(),
            categoryTemplateAttribute, formTemplateAttribute, model, servletRequest);

        verify(formTemplateAttribute).setDetails(any(FormTemplateApi.class));

        assertThat(template, is(ViewConstants.DOCUMENT_SELECTION.asView()));

    }

    @Test
    void formTemplateWhenSubmissionOpenAndEmailNotAuthorised() {

        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        PresenterApi presenter = new PresenterApi();
        presenter.setEmail(PRESENTER_EMAIL);
        submission.setPresenter(presenter);

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
                getSubmissionOkResponse(submission));

        when(categoryTemplateService.getTopLevelCategory(INS_SUB_LEVEL1.getCategoryType()))
                .thenReturn(CategoryTypeConstants.INSOLVENCY);

        when(apiClientService.isOnAllowList(PRESENTER_EMAIL)).
                thenReturn(new ApiResponse(200, getHeaders(), false));

        String template = testController.formTemplate(SUBMISSION_ID, COMPANY_NUMBER, INS_SUB_LEVEL1.getCategoryType(),
                categoryTemplateAttribute, formTemplateAttribute, model, servletRequest);

        assertThat(template, is(ViewConstants.MISSING.asView()));
    }

    @Test
    void postFormTemplateWhenSelectionMissing() {
        when(bindingResult.hasErrors()).thenReturn(true);

        final String result = testController.postFormTemplate(SUBMISSION_ID, COMPANY_NUMBER,
                categoryTemplateAttribute, formTemplateAttribute, bindingResult, model, servletRequest);

        verify(model).addAttribute(TEMPLATE_NAME, ViewConstants.DOCUMENT_SELECTION.asView());
        assertThat(result, is(ViewConstants.DOCUMENT_SELECTION.asView()));
    }

    @Test
    void postFormTemplate() {

        when(formTemplateAttribute.getFormTemplateList()).thenReturn(FORM_TEMPLATE_LIST);
        when(formTemplateAttribute.getFormType()).thenReturn(FORM_TEMPLATE_1.getFormType());
        when(formTemplateAttribute.getFormTemplateList()).thenReturn(FORM_TEMPLATE_LIST);
        when(apiClientService.putFormType(SUBMISSION_ID, new FormTypeApi(FORM_TEMPLATE_1.getFormType()))).
                thenReturn(new ApiResponse(200, getHeaders(), new SubmissionResponseApi(FORM_TEMPLATE_1.getFormType())));

        final String result = testController.postFormTemplate(SUBMISSION_ID, COMPANY_NUMBER,
                categoryTemplateAttribute, formTemplateAttribute, bindingResult, model, servletRequest);

        verify(bindingResult).hasErrors();
        verifyNoMoreInteractions(bindingResult, apiClientService);

        assertThat(result, is(ViewConstants.DOCUMENT_UPLOAD
                .asRedirectUri(CHS_URL, SUBMISSION_ID, COMPANY_NUMBER)));
    }

    @ParameterizedTest
    @ValueSource(strings = {SCOTTISH_COMPANY_NUMBER_1, SCOTTISH_COMPANY_NUMBER_2, SCOTTISH_COMPANY_NUMBER_3})
    void formTemplateWhenScottishCompany(String companyNumber) {

        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        PresenterApi presenter = new PresenterApi();
        presenter.setEmail(PRESENTER_EMAIL);
        submission.setPresenter(presenter);

        expectOpenSubmission(submission, CAT1_SUB_LEVEL1);

        String template = testController.formTemplate(SUBMISSION_ID, companyNumber, CAT1_SUB_LEVEL1.getCategoryType(),
                categoryTemplateAttribute, formTemplateAttribute, model, servletRequest);

        verify(model).addAttribute("isScottishCompany", Boolean.TRUE);

        assertThat(template, is(ViewConstants.DOCUMENT_SELECTION.asView()));
    }

    private void expectOpenSubmission(final SubmissionApi submission,
        final CategoryTemplateApi categoryTemplate) {
        when(apiClientService.getSubmission(SUBMISSION_ID))
            .thenReturn(getSubmissionOkResponse(submission));

        when(apiClientService.isOnAllowList(PRESENTER_EMAIL)).
            thenReturn(new ApiResponse(200, getHeaders(), true));

        when(formTemplateService.getFormTemplatesByCategory(categoryTemplate.getCategoryType())).
            thenReturn(
                new ApiResponse(200, getHeaders(), new FormTemplateListApi(FORM_TEMPLATE_LIST)));

        when(categoryTemplateAttribute.getCategoryName())
            .thenReturn(categoryTemplate.getCategoryName());
    }
}