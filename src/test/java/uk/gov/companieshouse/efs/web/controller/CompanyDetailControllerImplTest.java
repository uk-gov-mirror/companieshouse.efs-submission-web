package uk.gov.companieshouse.efs.web.controller;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.efs.web.model.company.CompanyDetail;
import uk.gov.companieshouse.efs.web.service.company.CompanyService;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


@ExtendWith(MockitoExtension.class)
class CompanyDetailControllerImplTest extends BaseControllerImplTest {
    private CompanyDetailController testController;

    @Mock
    private CompanyService companyService;
    @Mock
    private CompanyDetail companyDetailAttribute;

    @BeforeEach
    private void setup() {
        super.setUp();
        testController = new CompanyDetailControllerImpl(companyService, sessionService, apiClientService, logger,
                companyDetailAttribute);
        ((CompanyDetailControllerImpl) testController).setChsUrl(CHS_URL);

        mockMvc = MockMvcBuilders.standaloneSetup(testController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getViewName() {
        Assert.assertThat(((CompanyDetailControllerImpl) testController).getViewName(),
            is(ViewConstants.COMPANY_DETAIL.asView()));

    }

    @Test
    void getCompanyDetailAttribute() {
        assertThat(((CompanyDetailControllerImpl) testController).getCompanyDetailAttribute(),
            is(sameInstance(companyDetailAttribute)));
    }

    @Test
    void getCompanyDetail() {
        final String viewName = testController
            .getCompanyDetail(SUBMISSION_ID, COMPANY_NUMBER, companyDetailAttribute, model, servletRequest);

        verify(companyService).getCompanyDetail(companyDetailAttribute, COMPANY_NUMBER);
        assertThat(viewName, is(ViewConstants.COMPANY_DETAIL.asView()));
    }

    @Test
    void getCompanyDetailWhenRuntimeError() throws Exception {
        doThrow(new RuntimeException("dummy exception")).when(companyService)
                .getCompanyDetail(companyDetailAttribute, COMPANY_NUMBER);

        String url = String.format("/efs-submission/%s/company/%s/details", SUBMISSION_ID, COMPANY_NUMBER);
        mockMvc.perform(get(url))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name(ViewConstants.ERROR.asView()))
                .andReturn();
    }

    @Test
    void postCompanyDetail() {
        when(companyDetailAttribute.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(companyDetailAttribute.getCompanyName()).thenReturn(COMPANY_NAME);
        when(apiClientService.putCompany(eq(SUBMISSION_ID), refEq(new CompanyApi(COMPANY_NUMBER, COMPANY_NAME))))
            .thenReturn(new ApiResponse<>(200, getHeaders(), new SubmissionResponseApi()));

        final String viewName = testController
            .postCompanyDetail(SUBMISSION_ID, COMPANY_NUMBER, companyDetailAttribute, model, servletRequest);

        assertThat(viewName,
            is(ViewConstants.CATEGORY_SELECTION.asRedirectUri(CHS_URL, SUBMISSION_ID, COMPANY_NUMBER)));
    }

    @Test
    void postCompanyDetailWhenNumberNull() throws Exception {
        when(apiClientService.putCompany(eq(SUBMISSION_ID), refEq(new CompanyApi())))
                .thenThrow(new RuntimeException("company number is null"));

        String url = String.format("/efs-submission/%s/company/%s/details?action=submit", SUBMISSION_ID, COMPANY_NUMBER);
        mockMvc.perform(post(url))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name(ViewConstants.ERROR.asView()))
                .andReturn();
    }
}