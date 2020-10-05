package uk.gov.companieshouse.efs.web.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ViewConstantsTest {
    private static final String CHS_URL = "http://web.chs-dev:4000";
    private static final String SUB_ID = "4480ac41f5c0b596";
    private static final String NEW_SUBMISSION = "new-submission";
    private static final String EFS_ID_PAGE = "/efs-submission/" + SUB_ID;
    private static final String EFS_PAGE = "/efs-submission/" + NEW_SUBMISSION;
    private static final String REDIRECT_ID_PAGE = "redirect:" + CHS_URL + EFS_ID_PAGE;
    private static final String REDIRECT_PAGE = "redirect:" + CHS_URL + EFS_PAGE;
    private static final String COMPANY_NUMBER = "11111111";
    private static final String CATEGORY_SEQ = "INS,IA1986";

    @ParameterizedTest
    @CsvSource({"COMPANY_DETAIL, companyDetail", "ACCESSIBILITY, accessibilityStatement"})
    void asView(String constantName, String viewName) {
        assertThat(ViewConstants.valueOf(constantName).asView(), is(viewName));
    }

    @ParameterizedTest
    @CsvSource({"COMPANY_DETAIL, details", "ACCESSIBILITY, accessibilityStatement"})
    void asRedirectUri(String constantName, String uri) {
        assertThat(ViewConstants.valueOf(constantName).asRedirectUri(CHS_URL, SUB_ID),
            is(REDIRECT_ID_PAGE + "/" + uri));
    }

    @ParameterizedTest
    @CsvSource({"COMPANY_DETAIL, details", "ACCESSIBILITY, accessibilityStatement"})
    void asUri(String constantName, String uri) {
        assertThat(ViewConstants.valueOf(constantName).asUri(SUB_ID, CHS_URL), is(CHS_URL + EFS_ID_PAGE + "/" + uri));
    }

    @Test
    void asRedirectUriWithoutId() {
        assertThat(ViewConstants.NEW_SUBMISSION.asRedirectUri(CHS_URL), is(REDIRECT_PAGE));
    }

    @ParameterizedTest
    @CsvSource({"CATEGORY_SELECTION, category-selection", "DOCUMENT_SELECTION, document-selection"})
    void asRedirectUriForCategory(String constantName, String uri) {
        assertThat(ViewConstants.valueOf(constantName).asRedirectUri(CHS_URL, SUB_ID, COMPANY_NUMBER, CATEGORY_SEQ),
            is(String.format("%s/company/%s/%s?category=%s", REDIRECT_ID_PAGE, COMPANY_NUMBER, uri, CATEGORY_SEQ)));
    }

    @Test
    void asUriForCompany() {
        assertThat(ViewConstants.NEW_SUBMISSION.asUriForCompany(CHS_URL, COMPANY_NUMBER),
            is(String.format("%s/efs-submission/company/%s/%s", CHS_URL, COMPANY_NUMBER, NEW_SUBMISSION)));
    }

}