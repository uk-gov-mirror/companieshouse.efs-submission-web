package uk.gov.companieshouse.efs.web.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.efs.web.configuration.FileUploadConfiguration;

class DocumentUploadModelTest {

    private DocumentUploadModel documentUploadModel;
    private FileUploadConfiguration fileUploadConfiguration;
    private FileUploadConfiguration.FileType fileType;

    @Test
    void getAllowedFileExtensionsWhenSingular() {
        fileUploadConfiguration = new FileUploadConfiguration();
        List<String> extensions = Collections.singletonList("PDF");
        setConfig(extensions);

        assertThat(documentUploadModel.getAllowedFileExtensions(), is("PDF"));
    }

    @Test
    void getAllowedFileExtensionsWhenTwo() {
        fileUploadConfiguration = new FileUploadConfiguration();
        List<String> extensions = Arrays.asList("PDF", "PDF2");
        setConfig(extensions);

        assertThat(documentUploadModel.getAllowedFileExtensions(), is("PDF or PDF2"));
    }

    @Test
    void getAllowedFileExtensionsWhenMultiple() {
        fileUploadConfiguration = new FileUploadConfiguration();
        List<String> extensions = Arrays.asList("PDF", "PDF2", "PDF3");
        setConfig(extensions);

        assertThat(documentUploadModel.getAllowedFileExtensions(), is("PDF, PDF2 or PDF3"));
    }

    @Test
    void getAllowedFileExtensionsWhenEmpty() {
        fileUploadConfiguration = new FileUploadConfiguration();
        List<String> extensions = Collections.emptyList();
        setConfig(extensions);

        assertThat(documentUploadModel.getAllowedFileExtensions(), is(""));
    }

    private void setConfig(final List<String> extensions) {
        FileUploadConfiguration.FileType fileType =
            new FileUploadConfiguration.FileType("pdf", extensions);
        List<FileUploadConfiguration.FileType> fileTypes = new ArrayList<>();
        fileTypes.add(fileType);
        fileUploadConfiguration.setAllowedTypes(fileTypes);
        documentUploadModel = new DocumentUploadModel(fileUploadConfiguration);
    }
}