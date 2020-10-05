package uk.gov.companieshouse.efs.web.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateListApi;
import uk.gov.companieshouse.api.model.efs.submissions.FormTypeApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;

@ExtendWith(MockitoExtension.class)
class ResolutionsInfoControllerImplTest extends BaseControllerImplTest {

    public static final FormTemplateApi RESOLUTIONS_FORM = new FormTemplateApi("RESOLUTIONS", "Resolutions",
        "RESOLUTIONS", "", true, true);

    private ResolutionsInfoController testController;

    @BeforeEach
    private void setup() {
        super.setUp();
        testController = new ResolutionsInfoControllerImpl(logger, sessionService, apiClientService,
            formTemplateService, categoryTemplateService);
        ((ResolutionsInfoControllerImpl) testController).setChsUrl(CHS_URL);
    }

    @Test
    void getViewName() {
        Assert.assertThat(((ResolutionsInfoControllerImpl) testController).getViewName(),
            is(ViewConstants.RESOLUTIONS_INFO.asView()));
    }

    @Test
    void getResolutionsInfo() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.OPEN);

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
            getSubmissionOkResponse(submission));

        final String result = testController.resolutionsInfo(SUBMISSION_ID, COMPANY_NUMBER, categoryTemplateAttribute,
            model, servletRequest);

        assertThat(result, is(ViewConstants.RESOLUTIONS_INFO.asView()));
    }

    @Test
    void getResolutionsInfoWhenSubmissionIsNotOpen() {
        final SubmissionApi submission = createSubmission(SubmissionStatus.SUBMITTED);

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
            getSubmissionOkResponse(submission));

        final String result = testController.resolutionsInfo(SUBMISSION_ID, COMPANY_NUMBER, categoryTemplateAttribute,
            model, servletRequest);

        assertThat(result, is(ViewConstants.GONE.asView()));
    }

    @Test
    void postResolutionsInfo() {
        final CategoryTemplateApi resolutions = new CategoryTemplateApi("RESOLUTIONS",
            "Resolutions", "RESOLUTIONS", null);
        final FormTemplateApi formTemplateApi = new FormTemplateApi();
        final FormTemplateApi resolutionsForm = new FormTemplateApi(RESOLUTIONS_FORM);

        formTemplateApi.setFormType("RESOLUTIONS");

        final String result = testController.postResolutionsInfo(SUBMISSION_ID, COMPANY_NUMBER, categoryTemplateAttribute,
            bindingResult, model, servletRequest, session);

        assertThat(formTemplateApi.getFormType(), is(resolutionsForm.getFormType()));
        assertThat(result, is(ViewConstants.DOCUMENT_UPLOAD.asRedirectUri(CHS_URL, SUBMISSION_ID, COMPANY_NUMBER)));
    }
}