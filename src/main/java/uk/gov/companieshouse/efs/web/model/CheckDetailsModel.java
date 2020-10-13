package uk.gov.companieshouse.efs.web.model;

import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import uk.gov.companieshouse.api.model.efs.submissions.FileDetailApi;

@Component
@SessionScope
public class CheckDetailsModel {

    private String submissionId;
    private String companyName;
    private String companyNumber;
    private String documentTypeDescription;
    private List<FileDetailApi> documentUploadedList;
    private Boolean confirmAuthorised;

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(final String submissionId) {
        this.submissionId = submissionId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getDocumentTypeDescription() {
        return documentTypeDescription;
    }

    public void setDocumentTypeDescription(final String documentTypeDescription) {
        this.documentTypeDescription = documentTypeDescription;
    }

    public List<FileDetailApi> getDocumentUploadedList() {
        return documentUploadedList;
    }

    public void setDocumentUploadedList(final List<FileDetailApi> documentUploadedList) {
        this.documentUploadedList = documentUploadedList;
    }

    public Boolean getConfirmAuthorised() {
        return confirmAuthorised;
    }

    public void setConfirmAuthorised(final Boolean confirmAuthorised) {
        this.confirmAuthorised = confirmAuthorised;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CheckDetailsModel that = (CheckDetailsModel) o;
        return Objects.equals(getSubmissionId(), that.getSubmissionId())
               && Objects.equals(getCompanyName(), that.getCompanyName())
               && Objects.equals(getCompanyNumber(), that.getCompanyNumber())
               && Objects.equals(getDocumentTypeDescription(), that.getDocumentTypeDescription())
               && Objects.equals(getDocumentUploadedList(), that.getDocumentUploadedList())
               && Objects.equals(getConfirmAuthorised(), that.getConfirmAuthorised());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubmissionId(), getCompanyName(), getCompanyNumber(), getDocumentTypeDescription(),
            getDocumentUploadedList(), getConfirmAuthorised());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("submissionId", submissionId).append("companyName", companyName)
            .append("companyNumber", companyNumber)
            .append("documentTypeDescription", documentTypeDescription)
            .append("documentUploadedList", documentUploadedList)
            .append("confirmAuthorised", confirmAuthorised).toString();
    }
}
