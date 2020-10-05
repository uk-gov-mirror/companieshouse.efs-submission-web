package uk.gov.companieshouse.efs.web.categorytemplates.controller;

import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTemplateControllerImpl.ATTRIBUTE_NAME;
import static uk.gov.companieshouse.efs.web.categorytemplates.controller.CategoryTypeConstants.INSOLVENCY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateListApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.web.categorytemplates.model.CategoryTemplateModel;
import uk.gov.companieshouse.efs.web.categorytemplates.service.api.CategoryTemplateService;
import uk.gov.companieshouse.efs.web.controller.BaseControllerImpl;
import uk.gov.companieshouse.efs.web.controller.ViewConstants;
import uk.gov.companieshouse.efs.web.formtemplates.service.api.FormTemplateService;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.efs.web.service.session.SessionService;
import uk.gov.companieshouse.logging.Logger;

@Controller
@SessionAttributes(ATTRIBUTE_NAME)
@SuppressWarnings("squid:S3753")
/* S3753: "@Controller" classes that use "@SessionAttributes" must call "setComplete" on their "SessionStatus" objects
 *
 * The nature of the web journey across several controllers means it's not appropriate to do this. However,
 * setComplete() is properly called in ConfirmationControllerImpl at the end of the submission journey.
 */
public class CategoryTemplateControllerImpl extends BaseControllerImpl implements CategoryTemplateController {

    /**
     * Define the model name for this action.
     */
    public static final String ATTRIBUTE_NAME = "categoryTemplate";

    private CategoryTemplateModel categoryTemplateAttribute;

    /**
     * Constructor.
     *
     * @param categoryTemplateService   dependency
     * @param apiClientService          dependency
     * @param sessionService            dependency
     * @param formTemplateService       dependency
     * @param logger                    dependency
     * @param categoryTemplateAttribute details of the selected category template
     */
    @Autowired
    public CategoryTemplateControllerImpl(final CategoryTemplateService categoryTemplateService,
        final ApiClientService apiClientService, final SessionService sessionService,
        final FormTemplateService formTemplateService, final Logger logger,
        final CategoryTemplateModel categoryTemplateAttribute) {

        super(logger, sessionService, apiClientService, formTemplateService, categoryTemplateService);
        this.categoryTemplateAttribute = categoryTemplateAttribute;
    }

    @ModelAttribute(ATTRIBUTE_NAME)
    public CategoryTemplateModel getCategoryTemplateAttribute() {
        return categoryTemplateAttribute;
    }

    @Override
    public String getViewName() {
        return ViewConstants.CATEGORY_SELECTION.asView();
    }

    @Override
    public String categoryTemplate(@PathVariable String id, @PathVariable String companyNumber,
        @RequestParam(value = "category", required = false) List<String> categorySequenceList,
        @ModelAttribute(ATTRIBUTE_NAME) CategoryTemplateModel categoryTemplateAttribute, final Model model,
        HttpServletRequest servletRequest) {

        final SubmissionApi submissionApi = Objects.requireNonNull(getSubmission(id));

        if (submissionApi.getStatus() != SubmissionStatus.OPEN) {
            return ViewConstants.GONE.asView();
        }

        final Boolean isEmailAllowed = apiClientService.isOnAllowList(
                submissionApi.getPresenter().getEmail()).getData();
        final boolean sequenceHasInsolvency =
                categorySequenceList != null && categorySequenceList.contains(
                        INSOLVENCY.getValue());
        final CategoryTemplateListApi allCategoryTemplates =
                categoryTemplateService.getCategoryTemplates().getData();
        final List<String> categoryTypesList = Optional.ofNullable(allCategoryTemplates.getList())
                .orElseGet(ArrayList::new).stream().map(CategoryTemplateApi::getCategoryType)
                .collect(Collectors.toList());
        final boolean sequenceValid = categorySequenceList == null || categoryTypesList.containsAll(
                categorySequenceList);

        if (!sequenceValid || (sequenceHasInsolvency && !isEmailAllowed)) {
            return ViewConstants.MISSING.asView();
        }

        // find last entry in categorySequenceList
        final String parentCategoryId = Optional.ofNullable(categorySequenceList).map(
                Collection::stream).orElseGet(Stream::empty).reduce((first, second) -> second)
                .orElse(CategoryTemplateModel.ROOT_CATEGORY_ID);

        final CategoryTemplateApi childTemplate = categoryTemplateAttribute.rewindCategoryStack(
                parentCategoryId);

        categoryTemplateAttribute.setSubmissionId(submissionApi.getId());
        categoryTemplateAttribute.setDetails(childTemplate == null
                ? new CategoryTemplateApi(CategoryTemplateModel.ROOT_CATEGORY)
                : childTemplate);
        categoryTemplateAttribute.setCategoryTemplateList(
                getChildCategoryTemplateList(parentCategoryId,
                        submissionApi.getPresenter().getEmail()));
        addTrackingAttributeToModel(model);

        return ViewConstants.CATEGORY_SELECTION.asView();
    }

    @Override
    @PostMapping(value = {"{id}/company/{companyNumber}/category-selection"}, params = {"action=submit"})
    public String postCategoryTemplate(@PathVariable String id, @PathVariable String companyNumber,
        @Valid @ModelAttribute(ATTRIBUTE_NAME) CategoryTemplateModel categoryTemplateAttribute, BindingResult binding,
        Model model, HttpServletRequest servletRequest) {

        if (binding.hasErrors()) {
            addTrackingAttributeToModel(model);
            return ViewConstants.CATEGORY_SELECTION.asView();
        }

        final String selectedCategoryType = categoryTemplateAttribute.getDetails().getCategoryType();
        final CategoryTemplateApi selectedCategoryTemplate = getSelectedCategoryTemplate(selectedCategoryType);

        if (selectedCategoryTemplate != null) {
            categoryTemplateAttribute.getDetails().setCategoryName(selectedCategoryTemplate.getCategoryName());
            categoryTemplateAttribute.pushCategory(selectedCategoryTemplate);
        }

        // The selected category has belonging sub-categories so redisplay category selection screen for that category
        if (!categoryTemplateService.getCategoryTemplatesByParent(selectedCategoryType).getData().getList().isEmpty()) {
            return ViewConstants.CATEGORY_SELECTION.asRedirectUri(chsUrl, id, companyNumber,
                categoryTemplateAttribute.getCategorySequence());
        }
        else {
            return ViewConstants.DOCUMENT_SELECTION.asRedirectUri(chsUrl, id, companyNumber,
                            selectedCategoryType);
        }
    }

    /**
     * Fetch the list of child categories for a category ID, if any.
     * Filter out INSOLVENCY category if user not on authorised allowlist.
     *
     * @param parentCategoryId the parent category ID
     * @param presenterEmail   email to check against allowlist
     * @return the child category list, may be empty
     */
    private List<CategoryTemplateApi> getChildCategoryTemplateList(final String parentCategoryId,
            final String presenterEmail) {
        ApiResponse<CategoryTemplateListApi> listResponse =
                categoryTemplateService.getCategoryTemplatesByParent(parentCategoryId);
        final Boolean isEmailAllowed = apiClientService.isOnAllowList(presenterEmail)
                .getData();
        final Predicate<CategoryTemplateApi> includeCategory =
                c -> isEmailAllowed || !c.getCategoryType().equals(INSOLVENCY.getValue());

        return listResponse.getData().getList().stream().filter(includeCategory).collect(
                Collectors.toList());
    }

    /**
     * Find user selected category in cached child category list by category ID. Avoids another database lookup.
     *
     * @param selectedCategoryType the selected category ID
     * @return the category template
     */
    private CategoryTemplateApi getSelectedCategoryTemplate(final String selectedCategoryType) {
        return categoryTemplateAttribute.getCategoryTemplateList().stream().filter(
            c -> c.getCategoryType().equals(selectedCategoryType)).findFirst().orElse(null);
    }

}
