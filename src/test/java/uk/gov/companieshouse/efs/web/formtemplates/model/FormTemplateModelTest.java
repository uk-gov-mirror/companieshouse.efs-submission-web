package uk.gov.companieshouse.efs.web.formtemplates.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateListApi;

@ExtendWith(MockitoExtension.class)
class FormTemplateModelTest {

    private FormTemplateModel testFormTemplate;

    private FormTemplateApi formTemplate;

    @BeforeEach
    void setUp() {

        formTemplate = new FormTemplateApi("CC01", "Test01", "CC03",
                "CC01", true, true, null);
        testFormTemplate = new FormTemplateModel(formTemplate);
    }

    @Test
    void setGetDetails() {
        FormTemplateApi expected = formTemplate;

        testFormTemplate.setDetails(expected);

        assertThat(testFormTemplate.getDetails(), is(expected));
    }

    @Test
    void setGetFormTemplateList() {
        FormTemplateListApi expected = new FormTemplateListApi(Arrays.asList(formTemplate));

        testFormTemplate.setFormTemplateList(expected);

        assertThat(testFormTemplate.getFormTemplateList(), is(expected));
    }

    @Test
    void setGetFormType() {

        testFormTemplate.setFormType(null);

        assertThat(testFormTemplate.getFormType(), is(nullValue()));
    }

    @Test
    void setGetFormName() {

        testFormTemplate.setFormName(null);

        assertThat(testFormTemplate.getFormName(), is(nullValue()));
    }

    @Test
    void setGetFormCategory() {

        testFormTemplate.setFormCategory(null);

        assertThat(testFormTemplate.getFormCategory(), is(nullValue()));
    }

    @Test
    void setGetFee() {

        testFormTemplate.setPaymentCharge(null);

        assertThat(testFormTemplate.getPaymentCharge(), is(nullValue()));
    }

    @Test
    void isFesEnabled() {

        assertThat(testFormTemplate.isFesEnabled(), is(true));
    }

    @Test
    void isAuthenticationRequired() {

        assertThat(testFormTemplate.isAuthenticationRequired(), is(true));
    }

    @Test
    void setGetSubmissionId() {

        testFormTemplate.setSubmissionId(null);

        assertThat(testFormTemplate.getSubmissionId(), is(nullValue()));
    }

    @Test
    void setGetCompanyNumber() {

        testFormTemplate.setCompanyNumber(null);

        assertThat(testFormTemplate.getCompanyNumber(), is(nullValue()));
    }
}