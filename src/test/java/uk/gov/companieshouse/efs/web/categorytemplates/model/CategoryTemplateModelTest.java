package uk.gov.companieshouse.efs.web.categorytemplates.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class CategoryTemplateModelTest {

    private CategoryTemplateModel testCategoryTemplate;

    private CategoryTemplateApi categoryTemplate;

    @BeforeEach
    void setUp() {
        categoryTemplate = new CategoryTemplateApi("CC01", "CategoryC01", "INS", "CC01");
        testCategoryTemplate = new CategoryTemplateModel(categoryTemplate);
    }

    @Test
    void defaultConstructor() {
        testCategoryTemplate = new CategoryTemplateModel();

        assertThat(testCategoryTemplate.getDetails(), is(new CategoryTemplateApi("", "", null, null)));
    }

    @Test
    void setGetDetails() {
        CategoryTemplateApi expected = new CategoryTemplateApi("CC01", "CategoryC01", "INS", "CC01");

        testCategoryTemplate.setDetails(expected);

        assertThat(testCategoryTemplate.getDetails(), is(expected));
    }

    @Test
    void setGetCategoryType() {
        testCategoryTemplate.setCategoryType(null);

        assertThat(testCategoryTemplate.getCategoryType(), is(nullValue()));
    }

    @Test
    void setGetCategoryName() {
        testCategoryTemplate.setCategoryName(null);

        assertThat(testCategoryTemplate.getCategoryName(), is(nullValue()));
    }

    @Test
    void setGetCategoryNumber() {
        testCategoryTemplate.setCompanyNumber(null);

        assertThat(testCategoryTemplate.getCompanyNumber(), is(nullValue()));
    }

    @Test
    void setGetCategorySubmissionId() {
        testCategoryTemplate.setSubmissionId(null);

        assertThat(testCategoryTemplate.getSubmissionId(), is(nullValue()));
    }

    @Test
    void setGetCategoryHint() {
        testCategoryTemplate.setCategoryHint(null);

        assertThat(testCategoryTemplate.getCategoryHint(), is(nullValue()));
    }

    @Test
    void setGetParent() {
        testCategoryTemplate.setParent(null);

        assertThat(testCategoryTemplate.getParent(), is(nullValue()));
    }
}