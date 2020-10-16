package uk.gov.companieshouse.efs.web.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.model.efs.submissions.FileDetailApi;

class CheckDetailsModelTest {
    private static final String SUBMISSION_ID = "aaaaaaaaaaaaaaaaaaaaaaaa";
    private static final String COMPANY_NAME = "TEST COMPANY LTD";
    private static final String COMPANY_NUMBER = "11111111";

    private CheckDetailsModel checkDetailsModel;

    @BeforeEach
    void setUp() {
        checkDetailsModel = new CheckDetailsModel();
    }

    @Test
    void getSetSubmissionId() {
        assertThat(checkDetailsModel.getSubmissionId(), is(nullValue()));

        final String submissionId = SUBMISSION_ID;
        checkDetailsModel.setSubmissionId(submissionId);
        assertThat(checkDetailsModel.getSubmissionId(), is(submissionId));
        assertThat(checkDetailsModel.getSubmissionId(), sameInstance(submissionId));
    }

    @Test
    void getSetCompanyName() {
        assertThat(checkDetailsModel.getCompanyName(), is(nullValue()));

        final String companyName = COMPANY_NAME;
        checkDetailsModel.setCompanyName(companyName);
        assertThat(checkDetailsModel.getCompanyName(), is(companyName));
        assertThat(checkDetailsModel.getCompanyName(), sameInstance(companyName));
    }

    @Test
    void getSetCompanyNumber() {
        assertThat(checkDetailsModel.getCompanyNumber(), is(nullValue()));

        final String companyNumber = COMPANY_NUMBER;
        checkDetailsModel.setCompanyNumber(companyNumber);
        assertThat(checkDetailsModel.getCompanyNumber(), is(companyNumber));
        assertThat(checkDetailsModel.getCompanyNumber(), sameInstance(companyNumber));
    }

    @Test
    void getSetDocumentTypeDescription() {
        assertThat(checkDetailsModel.getDocumentTypeDescription(), is(nullValue()));

        final String documentTypeDescription = "PDF";
        checkDetailsModel.setDocumentTypeDescription(documentTypeDescription);
        assertThat(checkDetailsModel.getDocumentTypeDescription(), is(documentTypeDescription));
        assertThat(checkDetailsModel.getDocumentTypeDescription(), sameInstance(documentTypeDescription));
    }

    @Test
    void getSetDocumentUploadedList() {
        final List<FileDetailApi> documentsUploaded = Collections.emptyList();
        assertThat(checkDetailsModel.getDocumentUploadedList(), is(nullValue()));

        checkDetailsModel.setDocumentUploadedList(documentsUploaded);
        assertThat(checkDetailsModel.getDocumentUploadedList(), is(documentsUploaded));
        assertThat(checkDetailsModel.getDocumentUploadedList(), sameInstance(documentsUploaded));
    }

    @Test
    void getSetPaymentCharge() {
        assertThat(checkDetailsModel.getPaymentCharge(), is(nullValue()));

        final String paymentCharge = "99";

        checkDetailsModel.setPaymentCharge(paymentCharge);
        assertThat(checkDetailsModel.getPaymentCharge(), is(paymentCharge));
    }

    @Test
    void getSetConfirmAuthorised() {
        assertThat(checkDetailsModel.getConfirmAuthorised(), is(nullValue()));

        Arrays.asList(true, false).forEach(isAuthorised -> {
            checkDetailsModel.setConfirmAuthorised(isAuthorised);
            assertThat(checkDetailsModel.getConfirmAuthorised(), is(isAuthorised));
            assertThat(checkDetailsModel.getConfirmAuthorised(), sameInstance(isAuthorised));
        });
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(CheckDetailsModel.class)
                .usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
        // EqualsVerifier does asserts
    }

    @Test
    void toStringTest() {
        final String stringFormat = "CheckDetailsModel[submissionId=%s,companyName=%s,companyNumber=%s," +
                "documentTypeDescription=%s,documentUploadedList=%s,paymentCharge=%s,confirmAuthorised=%s]";

        assertThat(checkDetailsModel.toString(),
                is(String.format(stringFormat, "<null>", "<null>", "<null>", "<null>", "<null>", "<null>", "<null>", "<null>")));
    }
}