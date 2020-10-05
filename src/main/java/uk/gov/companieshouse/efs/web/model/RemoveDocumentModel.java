package uk.gov.companieshouse.efs.web.model;

import java.util.Objects;
import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class RemoveDocumentModel {

    private String submissionId;

    private String companyNumber;

    private String fileId;
    private String fileName;

    private String required;

    /**
     * No argument Constructor.
     */
    public RemoveDocumentModel() {
        super();

        this.required = "";
    }

    /**
     * Constructor sets the application id and required value from the {@link RemoveDocumentModel}.
     *
     * @param original the {@link RemoveDocumentModel}
     */
    public RemoveDocumentModel(final RemoveDocumentModel original) {
        this.submissionId = original.getSubmissionId();
        this.fileId = original.getFileId();
        this.fileName = original.getFileName();
        this.required = original.getRequired();
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(final String submissionId) {
        this.submissionId = submissionId;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(final String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @NotBlank(message = "{removeDocument.error}")
    public String getRequired() {
        return required;
    }

    public void setRequired(final String required) {
        this.required = required;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RemoveDocumentModel that = (RemoveDocumentModel) o;
        return Objects.equals(getSubmissionId(), that.getSubmissionId())
                && Objects.equals(getFileId(), that.getFileId())
                && Objects.equals(getFileName(), that.getFileName())
                && Objects.equals(getRequired(), that.getRequired());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubmissionId(), getFileId(), getFileName(), getRequired());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("submissionId", submissionId)
                .append("fileId", fileId)
                .append("fileName", fileName)
                .append("required", required).toString();
    }

}
