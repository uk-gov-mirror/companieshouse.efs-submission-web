package uk.gov.companieshouse.efs.web.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.model.efs.submissions.FileApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileListApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionFormApi;
import uk.gov.companieshouse.efs.web.configuration.FileUploadConfiguration;

@Component
@SessionScope
public class DocumentUploadModel {

    private FileUploadConfiguration config;

    private String submissionId;

    private Integer maximumUploadsAllowed;
    private Boolean maximumUploadLimitReached;
    private String maximumFileSize;
    private String companyNumber;
    private String companyName;
    private ModelMap attributes;

    private List<MultipartFile> selectedFiles;
    private FileListApi details;

    /**
     * Constructor.
     *
     * @param fileUploadConfiguration   dependency
     */
    @Autowired
    public DocumentUploadModel(final FileUploadConfiguration fileUploadConfiguration) {
        this.config = fileUploadConfiguration;
        this.selectedFiles = new ArrayList<>();
        this.attributes = new ModelMap();
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(final String submissionId) {
        this.submissionId = submissionId;
    }

    public Integer getMaximumUploadsAllowed() {
        return maximumUploadsAllowed;
    }

    public void setMaximumUploadsAllowed(Integer maximumUploadsAllowed) {
        this.maximumUploadsAllowed = maximumUploadsAllowed;
    }

    public Boolean getMaximumUploadLimitReached() {
        return maximumUploadLimitReached;
    }

    public void setMaximumUploadLimitReached(Boolean maximumUploadLimitReached) {
        this.maximumUploadLimitReached = maximumUploadLimitReached;
    }

    public String getMaximumFileSize() {
        return maximumFileSize;
    }

    public void setMaximumFileSize(final String maximumFileSize) {
        this.maximumFileSize = maximumFileSize;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(final String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(final String companyName) {
        this.companyName = companyName;
    }

    public List<MultipartFile> getSelectedFiles() {
        return selectedFiles;
    }

    public void setSelectedFiles(List<MultipartFile> selectedFiles) {
        this.selectedFiles = selectedFiles;
    }

    public FileListApi getDetails() {
        return details;
    }

    public void setDetails(final FileListApi details) {
        this.details = details;
    }

    /**
     * Adds file details to model.
     *
     * @param submissionApi submission details
     */
    public void addDetails(final SubmissionApi submissionApi) {
        final SubmissionFormApi submissionForm = submissionApi.getSubmissionForm();

        // Assign an empty list as a default for the file uploads.
        List<FileApi> files = new ArrayList<>();

        if (submissionForm != null && submissionForm.getFileDetails() != null) {
            files = submissionForm.getFileDetails().stream().map(
                    d -> new FileApi(d.getFileId(), d.getFileName(), d.getFileSize())
            ).collect(Collectors.toList());
        }

        setDetails(new FileListApi(files));
    }

    public Object getAttribute(final String key) {
        return attributes.getAttribute(key);
    }

    public ModelMap getAttributes() {
        return attributes;
    }

    /**
     * Assign attributes field (used for testing only).
     *
     * @param attributes the ModelMap to assign
     */
    void setAttributes(final ModelMap attributes) {
        this.attributes = attributes;
    }


    public void addAttribute(final String key, final Object value) {
        attributes.addAttribute(key, value);
    }

    /**
     * Retrieves and formats list of file extensions allowed in file-upload.properties.
     *
     * @return String with list separated by commas and 'or' at end
     */
    public String getAllowedFileExtensions() {
        final List<String> extensions = config.getDistinctExtensions().stream().sorted().collect(Collectors.toList());
        return extensions.stream().collect(Collectors.collectingAndThen(Collectors.toList(),
            joiningLastDelimiter(", ", " or ")));
    }

    private static Function<List<String>, String> joiningLastDelimiter(
        String delimiter, String lastDelimiter) {
        return list -> {
            int last = list.size() - 1;
            if (last < 1) return String.join(delimiter, list);
            return String.join(lastDelimiter,
                String.join(delimiter, list.subList(0, last)),
                list.get(last));
        };
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DocumentUploadModel that = (DocumentUploadModel) o;
        return Objects.equals(config, that.config) && Objects.equals(getSubmissionId(), that.getSubmissionId())
            && Objects.equals(getMaximumUploadsAllowed(), that.getMaximumUploadsAllowed()) && Objects
            .equals(getMaximumUploadLimitReached(), that.getMaximumUploadLimitReached()) && Objects
            .equals(getMaximumFileSize(), that.getMaximumFileSize()) && Objects
            .equals(getCompanyNumber(), that.getCompanyNumber()) && Objects
            .equals(getCompanyName(), that.getCompanyName()) && Objects.equals(getAttributes(), that.getAttributes())
            && Objects.equals(getSelectedFiles(), that.getSelectedFiles()) && Objects
            .equals(getDetails(), that.getDetails());
    }

    @Override
    public int hashCode() {
        return Objects.hash(config, getSubmissionId(), getMaximumUploadsAllowed(), getMaximumUploadLimitReached(),
            getMaximumFileSize(), getCompanyNumber(), getCompanyName(), getAttributes(), getSelectedFiles(), getDetails());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("config", config).append(
            "submissionId", submissionId).append("selectedFiles", selectedFiles).append("details", details).toString();
    }

    /**
     * Method to assist Thymeleaf in the rendering of filesize as a human readable string.
     *
     * @param file The DocumentApi containing the file metadata.
     * @return The human-readable string representation of the files size.
     */
    public static String displayFileSizeAsHuman(final FileApi file) {
        final int UNIT = 1024;
        if (file.getFileSize() < UNIT) {
            return file.getFileSize() + " B";
        }
        int exp = (int) (Math.log(file.getFileSize()) / Math.log(UNIT));
        char pre = ("KMGTPE").charAt(exp - 1);
        return String.format("%.1f %cB", file.getFileSize() / Math.pow(UNIT, exp), pre);
    }

}
