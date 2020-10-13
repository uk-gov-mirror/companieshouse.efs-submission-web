package uk.gov.companieshouse.efs.web.formtemplates.model;

import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateListApi;
import uk.gov.companieshouse.efs.web.formtemplates.validator.NotBlankFormTemplate;

/**
 * Model class representing on-screen fields. Delegates to {@link FormTemplateApi} for details.
 */
@Component
@SessionScope
public class FormTemplateModel {

    private FormTemplateApi details;
    private String submissionId;
    private String companyNumber;
    private FormTemplateListApi formTemplateList;


    public FormTemplateModel() {
        this(new FormTemplateApi());
    }

    /**
     * Constructor that sets the {@link FormTemplateApi} on the {@link FormTemplateModel}.
     *
     * @param details the {@link FormTemplateApi}
     */
    public FormTemplateModel(FormTemplateApi details) {
        this.details = details;
        this.formTemplateList = new FormTemplateListApi();
    }

    @NotBlankFormTemplate
    public FormTemplateApi getDetails() {
        return details;
    }

    public void setDetails(FormTemplateApi details) {
        this.details = details;
    }

    public List<FormTemplateApi> getFormTemplateList() {
        return formTemplateList.getList();
    }

    public void setFormTemplateList(final List<FormTemplateApi> formTemplateList) {
        this.formTemplateList = new FormTemplateListApi(formTemplateList);
    }

    public String getFormType() {
        return details.getFormType();
    }

    public void setFormType(String formType) {
        details.setFormType(formType);
    }

    public String getFormName() {
        return details.getFormName();
    }

    public void setFormName(String formName) {
        details.setFormName(formName);
    }

    public String getFormCategory() {
        return details.getFormCategory();
    }

    public void setFormCategory(String formCategory) {
        details.setFormCategory(formCategory);
    }

    public String getPaymentCharge() {
        return details.getPaymentCharge();
    }

    public void setPaymentCharge(String paymentCharge) {
        details.setPaymentCharge(paymentCharge);
    }

    public boolean isAuthenticationRequired() {
        return details.isAuthenticationRequired();
    }

    public boolean isFesEnabled() {
        return details.isFesEnabled();
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FormTemplateModel that = (FormTemplateModel) o;
        return Objects.equals(getDetails(), that.getDetails()) && Objects.equals(getSubmissionId(),
            that.getSubmissionId()) && Objects.equals(getCompanyNumber(), that.getCompanyNumber()) && Objects.equals(
            getFormTemplateList(), that.getFormTemplateList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDetails(), getSubmissionId(), getCompanyNumber(), getFormTemplateList());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, new RecursiveToStringStyle()).append("details", details).append("submissionId",
            submissionId).append("companyNumber", companyNumber).append("formTemplateList", formTemplateList)
            .toString();
    }
}
