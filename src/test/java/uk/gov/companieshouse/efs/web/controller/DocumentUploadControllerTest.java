package uk.gov.companieshouse.efs.web.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.efs.web.controller.DocumentUploadControllerImpl.FILE_UPLOADS_ALLOWED_FOR_FES_ENABLED_FORMS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileDetailListApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileListApi;
import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionFormApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.efs.web.configuration.FileUploadConfiguration;
import uk.gov.companieshouse.efs.web.model.DocumentUploadModel;
import uk.gov.companieshouse.efs.web.transfer.FileTransferApiClientResponse;
import uk.gov.companieshouse.efs.web.validation.DocumentUploadValidator;

@ExtendWith(MockitoExtension.class)
class DocumentUploadControllerTest extends BaseControllerImplTest {

    private static final String CHS_URL = "chs-url";
    private static final String SIGNED_IN_USER = "test@ch.gov.uk";
    private static final String FORM_TYPE_CODE = "RP06";
    private static final String CC_REMINDER = "showCcReminder";

    @Mock
    private FileUploadConfiguration fileUploadConfiguration;

    @Mock
    private DocumentUploadValidator documentUploadValidator;

    @Mock
    private DocumentUploadModel documentUploadAttribute;

    @Mock
    private ResourceBundle resourceBundle;

    private ModelMap attributes;

    @InjectMocks
    private DocumentUploadControllerImpl toTest;

    @BeforeEach
    private void start() {
        super.setUp();

        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(toTest, "chsUrl", CHS_URL);

        attributes = new ModelMap();
    }

    @AfterEach
    private void finish() {

    }

    private SubmissionApi createValidSubmissionApi(final Integer filesToCreate) {
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

        FileDetailListApi fileDetailListApi = new FileDetailListApi();
        submissionFormApi.setFileDetails(fileDetailListApi);

        return submissionApi;
    }

    @Test
    void getDefaultViewName() {
        final String viewName = toTest.getViewName();
        assertThat(viewName, is(ViewConstants.DOCUMENT_UPLOAD.asView()));
    }

    @Test
    void testPrepareNonFesEnabledForm() {
        SubmissionApi submissionApi = createValidSubmissionApi(0);

        String submissionID = submissionApi.getId();
        String companyNumber = submissionApi.getCompany().getCompanyNumber();

        ApiResponse<SubmissionApi> apiSubmissionResponse = mock(ApiResponse.class);
        when(apiSubmissionResponse.getData()).thenReturn(submissionApi);
        when(apiSubmissionResponse.getStatusCode()).thenReturn(200);

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(apiSubmissionResponse);

        FormTemplateApi formTemplateApi = mock(FormTemplateApi.class);
        when(formTemplateApi.isFesEnabled()).thenReturn(Boolean.FALSE);
        when(formTemplateApi.getFormCategory()).thenReturn("RP");

        ApiResponse<FormTemplateApi> apiFormTypeResponse = mock(ApiResponse.class);
        when(apiFormTypeResponse.getData()).thenReturn(formTemplateApi);

        when(formTemplateService.getFormTemplate(FORM_TYPE_CODE)).thenReturn(apiFormTypeResponse);

        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, SUBMISSION_ID);

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn(SIGNED_IN_USER);

        when(fileUploadConfiguration.getMaximumFilesAllowed()).thenReturn(10);

        when(documentUploadAttribute.getAttributes()).thenReturn(attributes);

        String viewName = toTest.prepare(submissionID, companyNumber, documentUploadAttribute, model, servletRequest, httpSession);

        verifyDocumentAttribute(submissionApi, submissionID, 10, false);
        verify(model).mergeAttributes(attributes);
        verify(model).addAttribute(TEMPLATE_NAME, "documentUpload");
        assertThat(viewName, is(ViewConstants.DOCUMENT_UPLOAD.asView()));
    }

    @Test
    void testPrepareFesEnabledForm() {
        SubmissionApi submissionApi = createValidSubmissionApi(0);

        String submissionID = submissionApi.getId();
        String companyNumber = submissionApi.getCompany().getCompanyNumber();

        ApiResponse<SubmissionApi> apiSubmissionResponse = mock(ApiResponse.class);
        when(apiSubmissionResponse.getData()).thenReturn(submissionApi);
        when(apiSubmissionResponse.getStatusCode()).thenReturn(200);

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(apiSubmissionResponse);

        FormTemplateApi formTemplateApi = mock(FormTemplateApi.class);
        when(formTemplateApi.isFesEnabled()).thenReturn(Boolean.TRUE);

        ApiResponse<FormTemplateApi> apiFormTypeResponse = mock(ApiResponse.class);
        when(apiFormTypeResponse.getData()).thenReturn(formTemplateApi);

        when(formTemplateService.getFormTemplate(FORM_TYPE_CODE)).thenReturn(apiFormTypeResponse);

        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, SUBMISSION_ID);

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn(SIGNED_IN_USER);

        when(documentUploadAttribute.getAttributes()).thenReturn(attributes);

        String viewName = toTest.prepare(submissionID, companyNumber, documentUploadAttribute, model, servletRequest, httpSession);

        verifyDocumentAttribute(submissionApi, submissionID, FILE_UPLOADS_ALLOWED_FOR_FES_ENABLED_FORMS, false);
        verify(model).mergeAttributes(attributes);
        verify(model).addAttribute(TEMPLATE_NAME, "documentUpload");
        assertThat(viewName, is(ViewConstants.DOCUMENT_UPLOAD.asView()));
    }

    @Test
    void testPrepareFesEnabledCcForm() {
        SubmissionApi submissionApi = createValidSubmissionApi(0);

        String submissionID = submissionApi.getId();
        String companyNumber = submissionApi.getCompany().getCompanyNumber();

        ApiResponse<SubmissionApi> apiSubmissionResponse = mock(ApiResponse.class);
        when(apiSubmissionResponse.getData()).thenReturn(submissionApi);
        when(apiSubmissionResponse.getStatusCode()).thenReturn(200);

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(apiSubmissionResponse);

        FormTemplateApi formTemplateApi = mock(FormTemplateApi.class);
        when(formTemplateApi.isFesEnabled()).thenReturn(Boolean.TRUE);
        when(formTemplateApi.getFormCategory()).thenReturn("CC");

        ApiResponse<FormTemplateApi> apiFormTypeResponse = mock(ApiResponse.class);
        when(apiFormTypeResponse.getData()).thenReturn(formTemplateApi);

        when(formTemplateService.getFormTemplate(FORM_TYPE_CODE)).thenReturn(apiFormTypeResponse);

        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, SUBMISSION_ID);

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn(SIGNED_IN_USER);

        when(documentUploadAttribute.getAttributes()).thenReturn(attributes);

        String viewName = toTest.prepare(submissionID, companyNumber, documentUploadAttribute, model, servletRequest, httpSession);

        verifyDocumentAttribute(submissionApi, submissionID, FILE_UPLOADS_ALLOWED_FOR_FES_ENABLED_FORMS, true);
        verify(model).mergeAttributes(attributes);
        verify(model).addAttribute(TEMPLATE_NAME, "documentUpload");
        assertThat(viewName, is(ViewConstants.DOCUMENT_UPLOAD.asView()));
    }


    @Test
    void testPrepareVerifySubmissionWithDifferentSubmissionID() {
        SubmissionApi submissionApi = createValidSubmissionApi(0);

        String submissionID = submissionApi.getId();
        String companyNumber = submissionApi.getCompany().getCompanyNumber();

        ApiResponse<SubmissionApi> apiSubmissionResponse = mock(ApiResponse.class);
        when(apiSubmissionResponse.getData()).thenReturn(submissionApi);
        when(apiSubmissionResponse.getStatusCode()).thenReturn(200);

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(apiSubmissionResponse);

        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, "wrong-submission-id");

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn(SIGNED_IN_USER);

        String viewName = toTest.prepare(submissionID, companyNumber, documentUploadAttribute, model, servletRequest, httpSession);

        assertThat(viewName, is(ViewConstants.ERROR.asView()));
    }

    @Test
    void testPrepareVerifySubmissionWithDifferentSignedInUser() {
        SubmissionApi submissionApi = createValidSubmissionApi(0);

        String submissionID = submissionApi.getId();
        String companyNumber = submissionApi.getCompany().getCompanyNumber();

        ApiResponse<SubmissionApi> apiSubmissionResponse = mock(ApiResponse.class);
        when(apiSubmissionResponse.getData()).thenReturn(submissionApi);
        when(apiSubmissionResponse.getStatusCode()).thenReturn(200);

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(apiSubmissionResponse);

        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, SUBMISSION_ID);

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn("wrong.user@ch.gov.uk");

        String viewName = toTest.prepare(submissionID, companyNumber, documentUploadAttribute, model, servletRequest, httpSession);

        assertThat(viewName, is(ViewConstants.ERROR.asView()));
    }

    @Test
    void testPrepareWithIncorrectSubmissionStatus() {
        SubmissionApi submissionApi = createValidSubmissionApi(0);

        // Modify the submission status.
        submissionApi.setStatus(SubmissionStatus.ACCEPTED);

        String submissionID = submissionApi.getId();
        String companyNumber = submissionApi.getCompany().getCompanyNumber();

        ApiResponse<SubmissionApi> apiSubmissionResponse = mock(ApiResponse.class);
        when(apiSubmissionResponse.getData()).thenReturn(submissionApi);
        when(apiSubmissionResponse.getStatusCode()).thenReturn(200);

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(apiSubmissionResponse);

        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, SUBMISSION_ID);

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn(SIGNED_IN_USER);

        String viewName = toTest.prepare(submissionID, companyNumber, documentUploadAttribute, model, servletRequest, httpSession);

        assertThat(viewName, is(ViewConstants.GONE.asView()));
    }

    @Test
    void testProcessVerifySubmissionWithDifferentSubmissionID() {
        SubmissionApi submissionApi = createValidSubmissionApi(0);

        String submissionID = submissionApi.getId();
        String companyNumber = submissionApi.getCompany().getCompanyNumber();

        ApiResponse<SubmissionApi> apiSubmissionResponse = mock(ApiResponse.class);
        when(apiSubmissionResponse.getData()).thenReturn(submissionApi);
        when(apiSubmissionResponse.getStatusCode()).thenReturn(200);

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(apiSubmissionResponse);

        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, "wrong-submission-id");

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn(SIGNED_IN_USER);

        BindingResult binding = mock(BindingResult.class);

        String viewName = toTest.process(submissionID, companyNumber, documentUploadAttribute, binding, model, servletRequest, httpSession);

        assertThat(viewName, is(ViewConstants.ERROR.asView()));
    }

    @Test
    void testProcessVerifySubmissionWithDifferentSignedInUser() {
        SubmissionApi submissionApi = createValidSubmissionApi(0);

        String submissionID = submissionApi.getId();
        String companyNumber = submissionApi.getCompany().getCompanyNumber();

        ApiResponse<SubmissionApi> apiSubmissionResponse = mock(ApiResponse.class);
        when(apiSubmissionResponse.getData()).thenReturn(submissionApi);
        when(apiSubmissionResponse.getStatusCode()).thenReturn(200);

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(apiSubmissionResponse);

        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, SUBMISSION_ID);

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn("wrong.user@ch.gov.uk");

        BindingResult binding = mock(BindingResult.class);

        String viewName = toTest.process(submissionID, companyNumber, documentUploadAttribute, binding, model, servletRequest, httpSession);

        assertThat(viewName, is(ViewConstants.ERROR.asView()));
    }

    @Test
    void testProcessFormWithNoFileSelected() {
        SubmissionApi submissionApi = createValidSubmissionApi(0);

        String submissionID = submissionApi.getId();
        String companyNumber = submissionApi.getCompany().getCompanyNumber();

        ApiResponse<SubmissionApi> apiSubmissionResponse = mock(ApiResponse.class);
        when(apiSubmissionResponse.getData()).thenReturn(submissionApi);
        when(apiSubmissionResponse.getStatusCode()).thenReturn(200);

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(apiSubmissionResponse);

        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, SUBMISSION_ID);

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn(SIGNED_IN_USER);

        BindingResult binding = mock(BindingResult.class);
        when(binding.hasErrors()).thenReturn(Boolean.TRUE);

        when(documentUploadValidator.apply(documentUploadAttribute, binding)).thenReturn(new ArrayList<>());

        String viewName = toTest.process(submissionID, companyNumber, documentUploadAttribute, binding, model, servletRequest, httpSession);

        assertThat(viewName, is(ViewConstants.DOCUMENT_UPLOAD.asView()));
        assertThat(binding.hasErrors(), is(true));
    }

    @Test
    void testProcessFormWithSingleFileSelected() {
        SubmissionApi submissionApi = createValidSubmissionApi(0);

        String submissionID = submissionApi.getId();
        String companyNumber = submissionApi.getCompany().getCompanyNumber();

        ApiResponse<SubmissionApi> apiSubmissionResponse = mock(ApiResponse.class);
        when(apiSubmissionResponse.getData()).thenReturn(submissionApi);
        when(apiSubmissionResponse.getStatusCode()).thenReturn(200);

        when(apiClientService.getSubmission(SUBMISSION_ID)).thenReturn(apiSubmissionResponse);

        Map<String, Object> sessionContextData = new HashMap<>();
        sessionContextData.put(ORIGINAL_SUBMISSION_ID, SUBMISSION_ID);

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionContextData);
        when(sessionService.getUserEmail()).thenReturn(SIGNED_IN_USER);

        BindingResult binding = mock(BindingResult.class);
        when(binding.hasErrors()).thenReturn(Boolean.FALSE);

        List<MultipartFile> uploadedFiles = new ArrayList<>();
        uploadedFiles.add(new MockMultipartFile("data", "testfile.txt", "text/plain", "some text".getBytes()));

        documentUploadAttribute.getSelectedFiles().addAll(uploadedFiles);

        FileTransferApiClientResponse fileUploadResponse = mock(FileTransferApiClientResponse.class);
        when(fileUploadResponse.getFileId()).thenReturn("my-file-upload-response-guid");

        when(documentUploadValidator.apply(documentUploadAttribute, binding)).thenReturn(uploadedFiles);
        when(fileTransferApiClient.upload(any())).thenReturn(fileUploadResponse);

        SubmissionResponseApi submissionResponseApi = mock(SubmissionResponseApi.class);
        when(submissionResponseApi.getId()).thenReturn("submission-response-id");

        ApiResponse<SubmissionResponseApi> putFileResponse = mock(ApiResponse.class);
        when(putFileResponse.getStatusCode()).thenReturn(200);
        when(putFileResponse.getData()).thenReturn(submissionResponseApi);

        List<FileApi> fileList = new ArrayList<>();
        fileList.add(new FileApi("my-file-upload-response-guid", "testfile.txt", 9L));

        FileListApi fileListApi = new FileListApi(fileList);

        when(apiClientService.putFileList(submissionApi.getId(), fileListApi)).thenReturn(putFileResponse);

        String viewName = toTest.process(submissionID, companyNumber, documentUploadAttribute, binding, model, servletRequest, httpSession);

        String expectedView = String.format("redirect:%s/efs-submission/%s/company/%s/document-upload",
                CHS_URL, submissionApi.getId(), companyNumber);

        assertThat(viewName, is(expectedView));
    }

    @Test
    void testFinishDocumentUploadSuccess() {
        SubmissionApi submissionApi = createValidSubmissionApi(1);

        String submissionID = submissionApi.getId();
        String companyNumber = submissionApi.getCompany().getCompanyNumber();

        List<FileApi> fileList = new ArrayList<>();
        fileList.add(new FileApi("my-file-upload-response-guid", "testfile.txt", 9L));

        when(documentUploadAttribute.getDetails()).thenReturn(new FileListApi(fileList));

        BindingResult binding = mock(BindingResult.class);

        String viewName = toTest.finish(submissionID, companyNumber, documentUploadAttribute, binding, model, servletRequest, httpSession);

        assertThat(viewName, is(ViewConstants.CHECK_DETAILS.asRedirectUri(CHS_URL, submissionApi.getId(), companyNumber)));
    }


    @Test
    void testFinishDocumentUploadFailure() {
        SubmissionApi submissionApi = createValidSubmissionApi(1);

        String submissionID = submissionApi.getId();
        String companyNumber = submissionApi.getCompany().getCompanyNumber();

        when(documentUploadAttribute.getDetails()).thenReturn(new FileListApi());

        BindingResult binding = mock(BindingResult.class);
        when(binding.hasErrors()).thenReturn(Boolean.TRUE);

        String viewName = toTest.finish(submissionID, companyNumber, documentUploadAttribute, binding, model, servletRequest, httpSession);

        assertThat(viewName, is(ViewConstants.DOCUMENT_UPLOAD.asView()));
        assertThat(binding.hasErrors(), is(Boolean.TRUE));
    }

    private void verifyDocumentAttribute(final SubmissionApi submissionApi, final String submissionID,
        final Integer fileUploadsAllowedForFesEnabledForms, final boolean b) {
        verify(documentUploadAttribute).setSubmissionId(submissionID);
        verify(documentUploadAttribute).setCompanyName(submissionApi.getCompany().getCompanyName());
        verify(documentUploadAttribute).setMaximumUploadsAllowed(fileUploadsAllowedForFesEnabledForms);
        verify(documentUploadAttribute).setMaximumUploadLimitReached(false);
        verify(documentUploadAttribute).setDetails(new FileListApi());
        verify(documentUploadAttribute).addAttribute(CC_REMINDER, b);
    }
}
