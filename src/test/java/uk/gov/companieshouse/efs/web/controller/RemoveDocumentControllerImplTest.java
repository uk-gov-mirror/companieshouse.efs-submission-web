package uk.gov.companieshouse.efs.web.controller;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileDetailApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileDetailListApi;
import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionFormApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.web.model.RemoveDocumentModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveDocumentControllerImplTest extends BaseControllerImplTest {

    private static final String SIGNED_IN_USER = "test@ch.gov.uk";
    private static final String FORM_TYPE_CODE = "RP06";
    private static final String FILE_ID1 = "1234567891";
    private static final String FILE_ID2 = "1234567892";
    private static final String FILE_ID3 = "1234567893";

    private RemoveDocumentController testController;

    @Mock
    private RemoveDocumentModel removeDocumentAttribute;

    @BeforeEach
    protected void setUp() {
        super.setUp();
        testController = new RemoveDocumentControllerImpl(fileTransferApiClient, logger, sessionService,
                apiClientService, removeDocumentAttribute);
    }

    @Test
    void getViewName() {
        Assert.assertThat(((BaseController) testController).getViewName(), is(ViewConstants.REMOVE_DOCUMENT.asView()));
    }

    @Test
    void removeDocumentWhenSubmissionOk() {

        FileDetailApi fileDetailApi = createFileDetailApi("Test.txt",FILE_ID1);
        SubmissionApi submissionApi = createValidSubmissionApi(new FileDetailListApi(Collections.singletonList(fileDetailApi)));

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
                getSubmissionOkResponse(submissionApi));

        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, SUBMISSION_ID);

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn(SIGNED_IN_USER);

        assertThat(testController.prepare(SUBMISSION_ID, COMPANY_NUMBER, FILE_ID1,
                removeDocumentAttribute, model, servletRequest), is("removeDocument"));

    }

    @Test
    void prepareDocumentFromMultipleWhenSubmissionOk() {

        FileDetailApi fileDetailApi = createFileDetailApi("Test.txt",FILE_ID1);
        FileDetailApi fileDetailApi2 = createFileDetailApi("Test2.txt",FILE_ID2);
        FileDetailApi fileDetailApi3 = createFileDetailApi("Test3.txt",FILE_ID3);
        SubmissionApi submissionApi = createValidSubmissionApi(new FileDetailListApi(Arrays.asList(fileDetailApi,fileDetailApi2,fileDetailApi3)));

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
                getSubmissionOkResponse(submissionApi));

        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, SUBMISSION_ID);

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn(SIGNED_IN_USER);

        assertThat(testController.prepare(SUBMISSION_ID, COMPANY_NUMBER, FILE_ID2,
                removeDocumentAttribute, model, servletRequest), is("removeDocument"));

        verify(removeDocumentAttribute).setSubmissionId(SUBMISSION_ID);
        verify(removeDocumentAttribute).setFileName("Test2.txt");
        verify(removeDocumentAttribute).setRequired("");
    }

    @Test
    void removeDocumentWhenNoFilesPresent() {
        SubmissionApi submissionApi = createValidSubmissionApi(new FileDetailListApi());

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(
                getSubmissionOkResponse(submissionApi));

        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, SUBMISSION_ID);

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn(SIGNED_IN_USER);

        assertThat(testController.prepare(SUBMISSION_ID, COMPANY_NUMBER, FILE_ID,
                removeDocumentAttribute, model, servletRequest), is("error"));
    }

    @Test
    void getRemoveDocumentAttribute() {
        RemoveDocumentModel originalModel = (RemoveDocumentModel) ReflectionTestUtils
                .getField(testController, "removeDocumentAttribute");
        RemoveDocumentModel gottenModel = ((RemoveDocumentControllerImpl)testController)
                .getRemoveDocumentAttribute();

        assertThat(gottenModel, sameInstance(originalModel));
    }

    @Test
    void errorWhenNotVerified() {
        SubmissionApi submission = createValidSubmissionApi(new FileDetailListApi());
        when(apiClientService.getSubmission(SUBMISSION_ID))
                .thenReturn(getSubmissionOkResponse(submission));

        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, SUBMISSION_ID);

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn("");

        String viewName = testController.process(SUBMISSION_ID, COMPANY_NUMBER, FILE_ID1, removeDocumentAttribute,
                bindingResult, model, request, session);

        assertThat(viewName, is(ViewConstants.ERROR.asView()));
    }

    @Test
    void returnWithErrorsWhenBindingFails() {
        SubmissionApi submission = createValidSubmissionApi(new FileDetailListApi());
        when(apiClientService.getSubmission(SUBMISSION_ID))
                .thenReturn(getSubmissionOkResponse(submission));

        createValidSession();

        when(bindingResult.hasErrors()).thenReturn(true);

        String viewName = testController.process(SUBMISSION_ID, COMPANY_NUMBER, FILE_ID1, removeDocumentAttribute,
                bindingResult, model, request, session);
        assertThat(viewName, is(ViewConstants.REMOVE_DOCUMENT.asView()));
        verify(model).addAttribute(eq(TEMPLATE_NAME), anyString());
    }

    private void createValidSession() {
        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, SUBMISSION_ID);

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn(SIGNED_IN_USER);
    }

    private FileDetailApi createFileDetailApi(String fileName, String fileId) {
        FileDetailApi fileDetailApi = new FileDetailApi();
        fileDetailApi.setFileName(fileName);
        fileDetailApi.setFileId(fileId);
        return fileDetailApi;
    }

    private SubmissionApi createValidSubmissionApi(final FileDetailListApi filesToCreate) {
        PresenterApi presenterApi = new PresenterApi();
        presenterApi.setEmail(SIGNED_IN_USER);

        CompanyApi companyApi = new CompanyApi();
        companyApi.setCompanyName(COMPANY_NAME);
        companyApi.setCompanyNumber(COMPANY_NUMBER);

        SubmissionFormApi submissionFormApi = new SubmissionFormApi();
        submissionFormApi.setFormType(FORM_TYPE_CODE);

        SubmissionApi submissionApi = new SubmissionApi();
        submissionApi.setId(SUBMISSION_ID);
        submissionApi.setPresenter(presenterApi);
        submissionApi.setCompany(companyApi);
        submissionApi.setStatus(SubmissionStatus.OPEN);
        submissionApi.setSubmissionForm(submissionFormApi);

        submissionFormApi.setFileDetails(filesToCreate);

        return submissionApi;
    }
}