package uk.gov.companieshouse.efs.web.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration class for file uploads.
 */
@Configuration
@PropertySource("classpath:file-upload.properties")
@ConfigurationProperties(prefix = "file-upload")
public class FileUploadConfiguration {

    private String name;
    private List<FileType> allowedTypes;
    private String maximumFileSize;
    private Integer maximumFilesAllowed;

    public FileUploadConfiguration() {
        this(null, new ArrayList<>(), null, null);
    }

    /**
     * Constructor for file upload configuration.
     *
     * @param name configuraton name
     * @param allowedTypes allowed file types
     * @param maximumFileSize maximum file size eg 4MB
     * @param maximumFilesAllowed eg 6
     */
    public FileUploadConfiguration(String name, List<FileType> allowedTypes, String maximumFileSize,
                                   Integer maximumFilesAllowed) {
        setName(name);
        setAllowedTypes(allowedTypes);
        setMaximumFilesize(maximumFileSize);
        setMaximumFilesAllowed(maximumFilesAllowed);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<FileType> getAllowedTypes() {
        return allowedTypes;
    }

    public void setAllowedTypes(final List<FileType> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    public Set<String> getDistinctMimeTypes() {
        return allowedTypes.stream().map(FileType::getMime).collect(Collectors.toSet());
    }

    public Set<String> getDistinctExtensions() {
        return allowedTypes.stream().flatMap(t -> t.getExtensions().stream()).collect(Collectors.toSet());
    }

    public String getMaximumFilesize() {
        return maximumFileSize;
    }

    public void setMaximumFilesize(final String maximumFileSize) {
        this.maximumFileSize = maximumFileSize;
    }

    public Integer getMaximumFilesAllowed() {
        return maximumFilesAllowed;
    }

    public void setMaximumFilesAllowed(final Integer maximumFilesAllowed) {
        this.maximumFilesAllowed = maximumFilesAllowed;
    }

    public static class FileType {

        private String mime;
        private List<String> extensions;

        public FileType() {
            // Default constructor.
        }

        public FileType(final String mimeType, final List<String> extensions) {
            this.mime = mimeType;
            this.extensions = extensions;
        }

        public String getMime() {
            return mime;
        }

        public void setMime(String mime) {
            this.mime = mime;
        }

        public List<String> getExtensions() {
            return extensions;
        }

        public void setExtensions(final List<String> extensions) {
            this.extensions = extensions;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final FileType fileType = (FileType) o;
            return Objects.equals(getMime(), fileType.getMime()) && Objects
                .equals(getExtensions(), fileType.getExtensions());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getMime(), getExtensions());
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("mime", getMime())
                    .append("extensions", getExtensions())
                    .toString();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FileUploadConfiguration that = (FileUploadConfiguration) o;

        return Objects.equals(getName(), that.getName())
            && Objects.equals(getAllowedTypes(), that.getAllowedTypes())
            && Objects.equals(maximumFileSize, that.maximumFileSize)
            && Objects.equals(getMaximumFilesAllowed(), that.getMaximumFilesAllowed());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", name)
                .append("allowedTypes", allowedTypes)
                .append("maximumFileSize", maximumFileSize)
                .append("maximumFilesAllowed", maximumFilesAllowed)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getAllowedTypes(), maximumFileSize, getMaximumFilesAllowed());
    }
}
