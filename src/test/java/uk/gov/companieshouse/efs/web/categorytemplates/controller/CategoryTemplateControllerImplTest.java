package uk.gov.companieshouse.efs.web.categorytemplates.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTemplateControllerImpl.TEMP_COMPANY_NUMBER;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateListApi;
import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.web.categorytemplates.model.CategoryTemplateModel;
import uk.gov.companieshouse.efs.web.controller.BaseControllerImplTest;
import uk.gov.companieshouse.efs.web.controller.ViewConstants;

@ExtendWith(MockitoExtension.class)
public class CategoryTemplateControllerImplTest extends BaseControllerImplTest {
    /*
     *   //////////////
     *  // FAMILY_1 //
     * //////////////
     *                     ROOT_CATEGORY
     *                      /          \
     *                     /            \
     *                    /              \
     *                   CAT_TOP_LEVEL   INSOLVENCY
     *                   /        \        \ _ _ _ _ _ _
     *                  /          \                    \
     *           CAT1_SUB_LEVEL1   CAT2_SUB_LEVEL1  INS_SUB_LEVEL1
     *               /        \                          \
     *              /          \                          \
     *     CAT1_SUB_LEVEL2   CAT2_SUB_LEVEL2          INS_SUB_LEVEL2
     *
     */
    public static final CategoryTemplateApi FAMILY_1_ROOT_LEVEL =
        new CategoryTemplateApi("", "FAMILY_1", "Dummy ROOT level category", "", null);
    public static final CategoryTemplateApi CAT_TOP_LEVEL =
        new CategoryTemplateApi("CAT_TOP_LEVEL", "FAMILY_1", "Dummy top level category", "", null);
    public static final CategoryTemplateApi INSOLVENCY =
        new CategoryTemplateApi("INS", "FAMILY_1", "INSOLVENCY", "", null);
    public static final CategoryTemplateApi INS_SUB_LEVEL1 =
        new CategoryTemplateApi("INS_SUB_LEVEL1", null,
            "Dummy insolvency category 1, subcategory level 1", "INS", null);
    public static final CategoryTemplateApi INS_SUB_LEVEL2 =
        new CategoryTemplateApi("INS_SUB_LEVEL2", null,
            "Dummy insolvency category 1, subcategory level 2", "INS_SUB_LEVEL1", null);
    public static final CategoryTemplateApi CAT1_SUB_LEVEL1 =
        new CategoryTemplateApi("CAT1_SUB_LEVEL1", null,
            "Dummy category 1, subcategory level 1", "CAT_TOP_LEVEL", null);
    public static final CategoryTemplateApi CAT2_SUB_LEVEL1 =
        new CategoryTemplateApi("CAT2_SUB_LEVEL1", null,
            "Dummy category 2, subcategory level 1", "CAT_TOP_LEVEL", null);
    public static final CategoryTemplateApi CAT1_SUB_LEVEL2 =
        new CategoryTemplateApi("CAT1_SUB_LEVEL2", null, "Dummy category1, subcategory level 2",
            "CAT1_SUB_LEVEL1", null);
    public static final CategoryTemplateApi CAT2_SUB_LEVEL2 =
        new CategoryTemplateApi("CAT2_SUB_LEVEL2", null,
            "Dummy category 2, subcategory level 2", "CAT1_SUB_LEVEL1", null);
    public static final List<CategoryTemplateApi> FAMILY_1_CATEGORIES =
        Arrays.asList(CAT_TOP_LEVEL, INSOLVENCY, CAT1_SUB_LEVEL1, CAT2_SUB_LEVEL1, CAT1_SUB_LEVEL2,
            CAT2_SUB_LEVEL2);

    /*
     *   //////////////
     *  // FAMILY_2 //
     * //////////////
     *                     ROOT_CATEGORY
     *                      /          \
     *                     /            \
     *                    /              \
     *             CAT_TOP_LEVEL_1   CAT_TOP_LEVEL_2
     */
    public static final CategoryTemplateApi CAT_TOP_LEVEL_1 =
        new CategoryTemplateApi("CAT_TOP_LEVEL_1", "FAMILY_2", "Dummy top level category", "",
            null);
    public static final CategoryTemplateApi CAT_TOP_LEVEL_2 =
        new CategoryTemplateApi("CAT_TOP_LEVEL_2", "FAMILY_2", "Dummy top level category", "",
            null);
    public static final List<CategoryTemplateApi> FAMILY_2_CATEGORIES =
        Arrays.asList(CAT_TOP_LEVEL_1, CAT_TOP_LEVEL_2);

    public static final List<CategoryTemplateApi> ALL_CATEGORIES =
        Stream.of(FAMILY_1_CATEGORIES, FAMILY_2_CATEGORIES)
            .flatMap(List::stream)
            .collect(Collectors.toList());

    private CategoryTemplateController testController;

    @BeforeEach
    protected void setUp() {
        setUpHeaders();
        testController =
            new CategoryTemplateControllerImpl(categoryTemplateService, apiClientService,
                sessionService, formTemplateService, logger, categoryTemplateAttribute);
        ((CategoryTemplateControllerImpl) testController).setChsUrl(CHS_URL);
    }

    @Test
    void getCategoryTemplateAttribute() {
        assertThat(((CategoryTemplateControllerImpl) testController).getCategoryTemplateAttribute(),
                is(sameInstance(categoryTemplateAttribute)));
    }

    @Test
    void getViewName() {
        assertThat(((CategoryTemplateControllerImpl) testController).getViewName(),
                is(ViewConstants.CATEGORY_SELECTION.asView()));
    }

    @Test
    void categoryTemplateWhenSubmissionNotOpen() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.SUBMITTED);

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
                getSubmissionOkResponse(submission));

        final String result = testController.categoryTemplate(SUBMISSION_ID, COMPANY_NUMBER,
                Collections.emptyList(), categoryTemplateAttribute, model, servletRequest);

        verifyNoInteractions(categoryTemplateService, categoryTemplateAttribute, model);
        assertThat(result, is(ViewConstants.GONE.asView()));
    }

    @Test
    void categoryTemplateTopLevelWhenCategorySequenceListNull() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        final CategoryTemplateListApi expectedCategoryList =
            new CategoryTemplateListApi(FAMILY_1_CATEGORIES);

        expectGetFamilyCategories(CategoryFamilyConstants.FILE, expectedCategoryList);
        expectInteractionsForGet(submission, true, expectedCategoryList,
            CategoryTemplateModel.ROOT_CATEGORY_ID, null);

        final String result = testController.categoryTemplate(SUBMISSION_ID, COMPANY_NUMBER, null,
            categoryTemplateAttribute, model, servletRequest);

        verifyOutcomeForFamilyCategories(CategoryFamilyConstants.FILE);
        verifyOutcomeForAttributes(CategoryTemplateModel.ROOT_CATEGORY,
            expectedCategoryList);
        assertThat(result, is(ViewConstants.CATEGORY_SELECTION.asView()));
    }

    @Test
    void categoryTemplateTopLevelWhenTempCompanyNumber() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        final CategoryTemplateListApi expectedCategoryList =
            new CategoryTemplateListApi(FAMILY_2_CATEGORIES);

        expectGetFamilyCategories(CategoryFamilyConstants.INC, expectedCategoryList);
        expectInteractionsForGet(submission, true, expectedCategoryList,
            CategoryTemplateModel.ROOT_CATEGORY_ID, null);

        final String result = testController.categoryTemplate(SUBMISSION_ID, TEMP_COMPANY_NUMBER, null,
            categoryTemplateAttribute, model, servletRequest);

        verifyOutcomeForFamilyCategories(CategoryFamilyConstants.INC);
        verifyOutcomeForAttributes(CategoryTemplateModel.ROOT_CATEGORY,
            expectedCategoryList);
        assertThat(result, is(ViewConstants.CATEGORY_SELECTION.asView()));
    }

    @Test
    void categoryTemplateTopLevelWhenCategorySequenceListContainsInsolvencyAndEmailAllowed() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        final CategoryTemplateListApi expectedCategoryList =
            new CategoryTemplateListApi(Arrays.asList(CAT_TOP_LEVEL, INSOLVENCY));
        final CategoryTemplateListApi expectedChildCategoryList =
            new CategoryTemplateListApi(Collections.singletonList(INS_SUB_LEVEL1));
        

        expectGetChildCategories(INSOLVENCY, expectedChildCategoryList);
        expectInteractionsForGet(submission, true, expectedCategoryList,
            INSOLVENCY.getCategoryType(), null);

        final String result = testController.categoryTemplate(SUBMISSION_ID, COMPANY_NUMBER,
            Collections.singletonList(INSOLVENCY.getCategoryType()), categoryTemplateAttribute,
            model, servletRequest);

        verifyOutcomeForChildCategories(INSOLVENCY);
        verifyOutcomeForAttributes(CategoryTemplateModel.ROOT_CATEGORY, expectedChildCategoryList);
        assertThat(result, is(ViewConstants.CATEGORY_SELECTION.asView()));
    }

    @Test
    void categoryTemplateTopLevelWhenCategorySequenceListContainsInsolvencyAndEmailNotAllowed() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
            getSubmissionOkResponse(submission));
        when(categoryTemplateService.getCategoryTemplates()).thenReturn(
            new ApiResponse<>(200, getHeaders(), new CategoryTemplateListApi(ALL_CATEGORIES)));
        when(apiClientService.isOnAllowList(anyString())).thenReturn(
            new ApiResponse<>(200, getHeaders(), false));

        final String result = testController.categoryTemplate(SUBMISSION_ID, COMPANY_NUMBER,
            Collections.singletonList(INSOLVENCY.getCategoryType()), categoryTemplateAttribute,
            model, servletRequest);

        verifyNoInteractions(model);
        assertThat(result, is(ViewConstants.MISSING.asView()));
    }

    @Test
    void categoryTemplateTopLevelWhenCategorySequenceListContainsInvalidCategoryAndEmailNotAllowed() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
            getSubmissionOkResponse(submission));
        when(categoryTemplateService.getCategoryTemplates()).thenReturn(
            new ApiResponse<>(200, getHeaders(), new CategoryTemplateListApi(ALL_CATEGORIES)));
        when(apiClientService.isOnAllowList(anyString())).thenReturn(
            new ApiResponse<>(200, getHeaders(), false));

        final String result = testController.categoryTemplate(SUBMISSION_ID, COMPANY_NUMBER,
            Collections.singletonList("INVALID"), categoryTemplateAttribute, model, servletRequest);

        verifyNoInteractions(model);
        assertThat(result, is(ViewConstants.MISSING.asView()));
    }

    @Test
    void categoryTemplateTopLevelWhenEmailAllowed() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        final CategoryTemplateListApi expectedCategoryList =
            new CategoryTemplateListApi(Arrays.asList(CAT_TOP_LEVEL, INSOLVENCY));

        expectGetFamilyCategories(CategoryFamilyConstants.FILE, expectedCategoryList);
        expectInteractionsForGet(submission, true, expectedCategoryList,
            CategoryTemplateModel.ROOT_CATEGORY_ID, null);

        final List<String> categorySequenceList = Collections.emptyList();
        final String result =
            testController.categoryTemplate(SUBMISSION_ID, COMPANY_NUMBER, categorySequenceList,
                categoryTemplateAttribute, model, servletRequest);

        verifyOutcomeForFamilyCategories(CategoryFamilyConstants.FILE);
        verifyOutcomeForAttributes(CategoryTemplateModel.ROOT_CATEGORY,
            expectedCategoryList);
        assertThat(result, is(ViewConstants.CATEGORY_SELECTION.asView()));
    }

    @Test
    void categoryTemplateTopLevelWhenEmailNotAllowed() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        final CategoryTemplateListApi expectedCategoryList =
            new CategoryTemplateListApi(Collections.singletonList(CAT_TOP_LEVEL));

        expectGetFamilyCategories(CategoryFamilyConstants.FILE, expectedCategoryList);
        expectInteractionsForGet(submission, false, expectedCategoryList,
            CategoryTemplateModel.ROOT_CATEGORY_ID, null);
        when(apiClientService.isOnAllowList(anyString())).thenReturn(
            new ApiResponse<>(200, getHeaders(), Boolean.FALSE));

        final List<String> categorySequenceList = Collections.emptyList();
        final String result =
            testController.categoryTemplate(SUBMISSION_ID, COMPANY_NUMBER, categorySequenceList,
                categoryTemplateAttribute, model, servletRequest);

        verifyOutcomeForFamilyCategories(CategoryFamilyConstants.FILE);
        verifyOutcomeForAttributes(CategoryTemplateModel.ROOT_CATEGORY,
            expectedCategoryList);
        assertThat(result, is(ViewConstants.CATEGORY_SELECTION.asView()));
    }

    @Test
    void categoryTemplateSubLevel1WhenEmailAllowed() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        final CategoryTemplateListApi expectedCategoryList =
            new CategoryTemplateListApi(Collections.singletonList(CAT1_SUB_LEVEL1));

        expectGetChildCategories(CAT_TOP_LEVEL, expectedCategoryList);
        expectInteractionsForGet(submission, true, expectedCategoryList,
            CAT_TOP_LEVEL.getCategoryType(), CAT_TOP_LEVEL);

        final String result = testController.categoryTemplate(SUBMISSION_ID, COMPANY_NUMBER,
            Collections.singletonList(CAT_TOP_LEVEL.getCategoryType()), categoryTemplateAttribute,
            model, servletRequest);

        verifyOutcomeForChildCategories(CAT_TOP_LEVEL);
        verifyOutcomeForAttributes(CAT_TOP_LEVEL, expectedCategoryList);
        assertThat(result, is(ViewConstants.CATEGORY_SELECTION.asView()));
    }

    @Test
    void categoryTemplateSubLevel1WhenEmailNotAllowed() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        final CategoryTemplateListApi expectedCategoryList =
            new CategoryTemplateListApi(Collections.singletonList(CAT1_SUB_LEVEL1));

        expectGetChildCategories(CAT_TOP_LEVEL, expectedCategoryList);
        expectInteractionsForGet(submission, false, expectedCategoryList,
            CAT_TOP_LEVEL.getCategoryType(), CAT_TOP_LEVEL);

        final String result = testController.categoryTemplate(SUBMISSION_ID, COMPANY_NUMBER,
            Collections.singletonList(CAT_TOP_LEVEL.getCategoryType()), categoryTemplateAttribute,
            model, servletRequest);

        verifyOutcomeForChildCategories(CAT_TOP_LEVEL);
        verifyOutcomeForAttributes(CAT_TOP_LEVEL, expectedCategoryList);
        assertThat(result, is(ViewConstants.CATEGORY_SELECTION.asView()));
    }

    @Test
    void categoryTemplateWhenSubLevel2() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);
        final CategoryTemplateListApi expectedCategoryList =
            new CategoryTemplateListApi(Arrays.asList(CAT1_SUB_LEVEL2, CAT2_SUB_LEVEL2));

        expectGetChildCategories(CAT1_SUB_LEVEL1, expectedCategoryList);
        expectInteractionsForGet(submission, true, expectedCategoryList,
            CAT1_SUB_LEVEL1.getCategoryType(), CAT1_SUB_LEVEL1);

        final String result = testController.categoryTemplate(SUBMISSION_ID, COMPANY_NUMBER,
            Arrays.asList(CAT_TOP_LEVEL.getCategoryType(), CAT1_SUB_LEVEL1.getCategoryType()),
            categoryTemplateAttribute, model, servletRequest);

        verifyOutcomeForChildCategories(CAT1_SUB_LEVEL1);
        verifyOutcomeForAttributes(CAT1_SUB_LEVEL1, expectedCategoryList);
        assertThat(result, is(ViewConstants.CATEGORY_SELECTION.asView()));
    }

    @Test
    void postCategoryTemplateWhenCategorySelectedHasSubCategories() {
        final CategoryTemplateApi details = new CategoryTemplateApi();
        final CategoryTemplateListApi subcategories = new CategoryTemplateListApi(
                Arrays.asList(CAT1_SUB_LEVEL2, CAT2_SUB_LEVEL2));

        details.setCategoryType(CAT1_SUB_LEVEL1.getCategoryType());
        expectInteractionsForPost(details, subcategories,
                Arrays.asList(CAT1_SUB_LEVEL1, CAT2_SUB_LEVEL1), CAT1_SUB_LEVEL1);

        final String catSequence =
                CAT1_SUB_LEVEL1.getCategoryType() + "," + CAT1_SUB_LEVEL2.getCategoryType();

        when(categoryTemplateAttribute.getCategorySequence()).thenReturn(catSequence);

        final String result = testController.postCategoryTemplate(SUBMISSION_ID, COMPANY_NUMBER,
                categoryTemplateAttribute, bindingResult, model, servletRequest);

        verifyOutcomeForPost(CAT1_SUB_LEVEL1);
        assertThat(details.getCategoryName(), is(CAT1_SUB_LEVEL1.getCategoryName()));
        assertThat(result, is(ViewConstants.CATEGORY_SELECTION
                .asRedirectUri(CHS_URL, SUBMISSION_ID, COMPANY_NUMBER, catSequence)));
    }

    @Test
    void postCategoryTemplateWhenCategorySelectedHasNoSubCategories() {
        final CategoryTemplateApi details = new CategoryTemplateApi();
        final CategoryTemplateListApi subcategories = new CategoryTemplateListApi();

        details.setCategoryType(CAT2_SUB_LEVEL1.getCategoryType());
        expectInteractionsForPost(details, subcategories,
                Collections.singletonList(CAT2_SUB_LEVEL1), CAT2_SUB_LEVEL1);


        final String result = testController.postCategoryTemplate(SUBMISSION_ID, COMPANY_NUMBER,
                categoryTemplateAttribute, bindingResult, model, servletRequest);

        final String catSequence = CAT2_SUB_LEVEL1.getCategoryType();

        verifyOutcomeForPost(CAT2_SUB_LEVEL1);
        assertThat(details.getCategoryName(), is(CAT2_SUB_LEVEL1.getCategoryName()));
        assertThat(result, is(ViewConstants.DOCUMENT_SELECTION
                .asRedirectUri(CHS_URL, SUBMISSION_ID, COMPANY_NUMBER, catSequence)));
    }

    @Test
    void postCategoryTemplateWhenSelectionMissing() {
        when(bindingResult.hasErrors()).thenReturn(true);

        final String result = testController.postCategoryTemplate(SUBMISSION_ID, COMPANY_NUMBER,
                categoryTemplateAttribute, bindingResult, model, servletRequest);

        verify(model).addAttribute(TEMPLATE_NAME, ViewConstants.CATEGORY_SELECTION.asView());
        assertThat(result, is(ViewConstants.CATEGORY_SELECTION.asView()));
    }

    @Test
    void postCategoryTemplateWhenAttributeSelectionNull() {
        final CategoryTemplateApi details = new CategoryTemplateApi();
        final CategoryTemplateListApi categoryList = new CategoryTemplateListApi(
                Arrays.asList(CAT1_SUB_LEVEL2, CAT2_SUB_LEVEL2));

        when(categoryTemplateAttribute.getDetails()).thenReturn(details);
        when(categoryTemplateService.getCategoryTemplatesByParent(null)).thenReturn(
                new ApiResponse<>(200, getHeaders(), categoryList));

        final String result = testController.postCategoryTemplate(SUBMISSION_ID, COMPANY_NUMBER,
                categoryTemplateAttribute, bindingResult, model, servletRequest);

        assertThat(result, is(ViewConstants.CATEGORY_SELECTION
                .asRedirectUri(CHS_URL, SUBMISSION_ID, COMPANY_NUMBER, "")));
    }

    protected SubmissionApi createSubmission(final SubmissionStatus submitted) {
        final SubmissionApi submission = super.createSubmission(submitted);
        submission.setPresenter(new PresenterApi(USER_EMAIL));

        return submission;
    }

    private void expectInteractionsForGet(final SubmissionApi submission, final Boolean isAllowed,
        final CategoryTemplateListApi categoryList,
        final String leafCategoryId, final CategoryTemplateApi removedCategoryId) {
        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
            getSubmissionOkResponse(submission));
        when(categoryTemplateService.getCategoryTemplates()).thenReturn(
            new ApiResponse<>(200, getHeaders(), new CategoryTemplateListApi(ALL_CATEGORIES)));
        when(categoryTemplateAttribute.rewindCategoryStack(leafCategoryId)).thenReturn(
            removedCategoryId);
        when(apiClientService.isOnAllowList(anyString())).thenReturn(
            new ApiResponse<>(200, getHeaders(), isAllowed));
    }

    private void expectGetFamilyCategories(final CategoryFamilyConstants family,
        final CategoryTemplateListApi categoryList) {
        when(categoryTemplateService.getCategoryTemplatesByFamily(family.name())).thenReturn(
            new ApiResponse<>(200, getHeaders(), categoryList));

    }
    private void expectGetChildCategories(final CategoryTemplateApi parent, 
        final CategoryTemplateListApi categoryList) {
        when(categoryTemplateService.getCategoryTemplatesByParent(
            parent.getCategoryType())).thenReturn(
            new ApiResponse<>(200, getHeaders(), categoryList));
    }

    private void expectInteractionsForPost(final CategoryTemplateApi details,
        final CategoryTemplateListApi subcategories,
        final List<CategoryTemplateApi> categoryTemplateList,
        final CategoryTemplateApi selectedCategory) {
        when(categoryTemplateAttribute.getDetails()).thenReturn(details);
        when(categoryTemplateAttribute.getCategoryTemplateList()).thenReturn(categoryTemplateList);
        when(categoryTemplateService.getCategoryTemplatesByParent(
            selectedCategory.getCategoryType())).thenReturn(
            new ApiResponse<>(200, getHeaders(), subcategories));
    }

    private void verifyOutcomeForAttributes(final CategoryTemplateApi detailsCategory,
        final CategoryTemplateListApi categoryList) {
        verify(categoryTemplateAttribute).setSubmissionId(SUBMISSION_ID);
        verify(categoryTemplateAttribute).setDetails(new CategoryTemplateApi(detailsCategory));
        verify(categoryTemplateAttribute).setCategoryTemplateList(categoryList);
        verify(model).addAttribute(TEMPLATE_NAME, ViewConstants.CATEGORY_SELECTION.asView());
    }

    private void verifyOutcomeForFamilyCategories(final CategoryFamilyConstants family) {
        verify(categoryTemplateService).getCategoryTemplatesByFamily(family.name());
    }

    private void verifyOutcomeForChildCategories(final CategoryTemplateApi parent) {
        verify(categoryTemplateService).getCategoryTemplatesByParent(parent.getCategoryType());
    }

    private void verifyOutcomeForPost(final CategoryTemplateApi pushedCategory) {
        verify(bindingResult).hasErrors();
        verifyNoMoreInteractions(bindingResult, apiClientService);
        verify(categoryTemplateAttribute).pushCategory(pushedCategory);
    }
}