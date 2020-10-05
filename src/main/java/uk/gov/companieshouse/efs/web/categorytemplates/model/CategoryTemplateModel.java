package uk.gov.companieshouse.efs.web.categorytemplates.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateListApi;
import uk.gov.companieshouse.efs.web.categorytemplates.validator.NotBlankCategoryTemplate;

/**
 * Model class representing on-screen fields. Delegates to {@link CategoryTemplateApi} for details.
 */
@Component
@SessionScope
public class CategoryTemplateModel {
    public static final String ROOT_CATEGORY_ID = "";
    public static final CategoryTemplateApi ROOT_CATEGORY = new CategoryTemplateApi(ROOT_CATEGORY_ID, "", null, null);

    private String submissionId;
    private String companyNumber;
    private CategoryTemplateListApi categoryTemplateList;
    private Deque<CategoryTemplateApi> categoryStack;
    private CategoryTemplateApi details;

    public CategoryTemplateModel() {
        this(new CategoryTemplateApi(ROOT_CATEGORY));
    }

    /**
     * Constructor that sets the {@link CategoryTemplateApi} on the {@link CategoryTemplateModel}.
     *
     * @param details the {@link CategoryTemplateApi}
     */
    public CategoryTemplateModel(CategoryTemplateApi details) {
        this.details = details;
        this.categoryStack = new ArrayDeque<>();
        this.categoryTemplateList = new CategoryTemplateListApi();
    }

    public List<CategoryTemplateApi> getCategoryTemplateList() {
        return categoryTemplateList.getList();
    }

    public void setCategoryTemplateList(final List<CategoryTemplateApi> categoryTemplateList) {
        this.categoryTemplateList = new CategoryTemplateListApi(categoryTemplateList);
    }

    @NotBlankCategoryTemplate
    public CategoryTemplateApi getDetails() {
        return details;
    }

    public void setDetails(final CategoryTemplateApi details) {
        this.details = details;
    }

    public Deque<CategoryTemplateApi> getCategoryStack() {
        return categoryStack;
    }

    /**
     * Pop categories from the stack until the topmost/last entry has the given category ID.
     * Note: If the category ID is not present, all entries will be removed.
     *
     * @param categoryType the category type ID
     * @return the element most recently removed, or null
     */
    public CategoryTemplateApi rewindCategoryStack(final String categoryType) {
        CategoryTemplateApi removed = null;

        while (!categoryStack.isEmpty()) {
            final CategoryTemplateApi top = categoryStack.peekLast();

            if (top != null && top.getCategoryType().equals(categoryType)) {
                return removed;
            }
            else {
                removed = categoryStack.removeLast();
            }
        }

        return removed;
    }

    public void pushCategory(CategoryTemplateApi details) {
        this.categoryStack.add(details);
    }

    public String getCategoryType() {
        return getDetails().getCategoryType();
    }

    public void setCategoryType(String categoryType) {
        getDetails().setCategoryType(categoryType);
    }

    public String getCategoryName() {
        return getDetails().getCategoryName();
    }

    public void setCategoryName(String categoryName) {
        getDetails().setCategoryName(categoryName);
    }

    public String getParent() {
        return getDetails().getParent();
    }

    public void setParent(String parent) {
        getDetails().setParent(parent);
    }

    public String getCategoryHint() {
        return getDetails().getCategoryHint();
    }

    public void setCategoryHint(String hint) {
        getDetails().setCategoryHint(hint);
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(final String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /**
     * Determines parent category based on the stack.
     *
     * @return category type that is the parent category of the view
     */
    public CategoryTemplateApi getParentCategory() {
        return Optional.ofNullable(categoryStack.peekLast()).orElse(new CategoryTemplateApi(ROOT_CATEGORY));
    }

    /**
     * Order of categories chosen by user.
     *
     * @param limit position in order
     * @return
     */
    public String getCategorySequence(final int limit) {
        return categoryStack.stream().filter(c -> c.getCategoryType() != null).filter(
            distinctByKey(CategoryTemplateApi::getCategoryType)).map(CategoryTemplateApi::getCategoryType).limit(limit)
            .collect(Collectors.joining(","));
    }

    public String getCategorySequence() {
        return getCategorySequence(categoryStack.size());
    }

    public String getParentCategorySequence() {
        return getCategorySequence(categoryStack.isEmpty() ? 0 : categoryStack.size() - 1);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CategoryTemplateModel that = (CategoryTemplateModel) o;
        return Objects.equals(categoryStack, that.categoryStack) && Objects.equals(getSubmissionId(),
            that.getSubmissionId()) && Objects.equals(getCompanyNumber(), that.getCompanyNumber()) && Objects.equals(
            getCategoryTemplateList(), that.getCategoryTemplateList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryStack, getSubmissionId(), getCompanyNumber(), getCategoryTemplateList());
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, new RecursiveToStringStyle()).toString();
    }

}
