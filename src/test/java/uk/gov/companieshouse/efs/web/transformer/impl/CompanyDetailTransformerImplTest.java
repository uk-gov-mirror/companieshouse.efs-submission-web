package uk.gov.companieshouse.efs.web.transformer.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.company.RegisteredOfficeAddressApi;
import uk.gov.companieshouse.efs.web.model.company.CompanyDetail;
import uk.gov.companieshouse.efs.web.transformer.CompanyDetailTransformer;

@ExtendWith(MockitoExtension.class)
class CompanyDetailTransformerImplTest {
    public static final String SUBMISSION_ID = "id";
    public static final String COMPANY_NAME = "name";
    public static final String COMPANY_NUMBER = "number";
    public static final String OFFICE_ADDRESS = String.join("\n", "line1", "line2", "PO code");
    public static final String ADDRESS_LINE_1 = "line1";
    public static final String ADDRESS_LINE_2 = "line2";
    public static final String POSTAL_CODE = "PO code";
    private CompanyDetailTransformer testTransformer;

    @BeforeEach
    void setUp() {
        testTransformer = new CompanyDetailTransformerImpl();
    }

    @Test
    void getCompanyDetailWhenAddressNotNull() {
        final CompanyDetail output = new CompanyDetail();
        final LocalDate incDate = LocalDate.now();
        final RegisteredOfficeAddressApi roa = new RegisteredOfficeAddressApi();

        roa.setAddressLine1(ADDRESS_LINE_1);
        roa.setAddressLine2(ADDRESS_LINE_2);
        roa.setPostalCode(POSTAL_CODE);

        final CompanyProfileApi input = createCompanyProfile(roa, incDate);
        final CompanyDetail expected = createCompanyDetail(OFFICE_ADDRESS, incDate);

        output.setSubmissionId(
                SUBMISSION_ID); // property not modified by getCompanyDetail()
        testTransformer.getCompanyDetail(output, input);

        assertThat(output, samePropertyValuesAs(expected));
    }

    @Test
    void getCompanyDetailWhenAddressNull() {
        final CompanyDetail output = new CompanyDetail();
        final LocalDate incDate = LocalDate.now();

        final CompanyProfileApi input = createCompanyProfile(null, incDate);
        final CompanyDetail expected = createCompanyDetail(null, incDate);

        output.setSubmissionId(
                SUBMISSION_ID); // property not modified by getCompanyDetail()
        testTransformer.getCompanyDetail(output, input);

        assertThat(output, samePropertyValuesAs(expected));
    }

    private CompanyProfileApi createCompanyProfile(final RegisteredOfficeAddressApi roa,
            final LocalDate incDate) {

        final CompanyProfileApi profile = new CompanyProfileApi();

        profile.setCompanyName(COMPANY_NAME);
        profile.setCompanyNumber(COMPANY_NUMBER);
        profile.setRegisteredOfficeAddress(roa);
        profile.setDateOfCreation(incDate);

        return profile;
    }

    private CompanyDetail createCompanyDetail(final String roa, final LocalDate incDate) {
        final CompanyDetail detail = new CompanyDetail();

        detail.setSubmissionId(SUBMISSION_ID);
        detail.setCompanyName(COMPANY_NAME);
        detail.setCompanyNumber(COMPANY_NUMBER);
        detail.setIncorporationDate(incDate);
        detail.setRegisteredOfficeAddress(roa);

        return detail;
    }
}