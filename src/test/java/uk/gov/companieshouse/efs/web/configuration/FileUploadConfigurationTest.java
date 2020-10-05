package uk.gov.companieshouse.efs.web.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

public class FileUploadConfigurationTest {

    @Test
    void getDistinctMimeTypes() {
        FileUploadConfiguration classUnderTest = new FileUploadConfiguration();
        FileUploadConfiguration.FileType fileType = new FileUploadConfiguration.FileType("pdf", null);
        List<FileUploadConfiguration.FileType> fileTypes = new ArrayList<>();
        fileTypes.add(fileType);
        classUnderTest.setAllowedTypes(fileTypes);

        Set<String> hSet = new HashSet<String>();
        hSet.add("pdf");

        assertThat(classUnderTest.getDistinctMimeTypes(), is(hSet));

    }


    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(FileUploadConfiguration.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
        // EqualsVerifier does the asserts
    }

    @Test
    void equalsAndHashCodeFileType() {
        EqualsVerifier.forClass(FileUploadConfiguration.FileType.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
        // EqualsVerifier does the asserts
    }

}
