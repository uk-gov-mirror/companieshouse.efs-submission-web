package uk.gov.companieshouse.efs.web.service.api;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.ConfirmAuthorisedApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileListApi;
import uk.gov.companieshouse.api.model.efs.submissions.FormTypeApi;
import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;

/**
 * The {@code ApiClientService} interface provides an abstraction that can be
 * used when testing {@code ApiClientManager} static methods, without imposing
 * the use of a test framework that supports mocking of static methods.
 */
public interface ApiClientService {

    InternalApiClient getApiClient();

    /**
     * Create a submission.
     *
     * @param presenter the presenter details
     * @return the submission id of the created object
     */
    ApiResponse<SubmissionResponseApi> createSubmission(final PresenterApi presenter);

    /**
     * Get a submission.
     *
     * @param submissionId the submission ID
     * @return the model for the submission json response
     */
    ApiResponse<SubmissionApi> getSubmission(final String submissionId);

    /**
     * Update company details.
     *
     * @param submissionId the submission ID
     * @param company the company details
     * @return the api response
     */
    ApiResponse<SubmissionResponseApi> putCompany(final String submissionId, final CompanyApi company);

    /**
     * Update form type.
     *
     * @param submissionId the submission ID
     * @param formType the form type
     * @return the api response
     */
    ApiResponse<SubmissionResponseApi> putFormType(final String submissionId, final FormTypeApi formType);

    /**
     * Update file details.
     *
     * @param submissionId the submission ID
     * @param fileList the file details list
     * @return the api response
     */
    ApiResponse<SubmissionResponseApi> putFileList(final String submissionId, final FileListApi fileList);

    /**
     * Update payment sessions.
     *
     * @param submissionId the submission ID
     * @param paymentSessions the complete payment session collection
     * @return the api response
     */
    ApiResponse<SubmissionResponseApi> putPaymentSessions(final String submissionId,
        final SessionListApi paymentSessions);

    /**
     * Update confirm authorised.
     *
     * @param submissionId the submission ID
     * @param confirmAuthorised the authorised confirmation
     * @return the api response
     */
    ApiResponse<SubmissionResponseApi> putConfirmAuthorised(final String submissionId, final ConfirmAuthorisedApi confirmAuthorised);

    /**
     * Update submission status; SUBMITTED.
     *
     * @param submissionId the submission ID
     * @return the api response
     */
    ApiResponse<SubmissionResponseApi> putSubmissionSubmitted(final String submissionId);

    /**
     * Checks if an email address is on the allow list.
     *
     * @param emailAddress the email address to be checked
     * @return the api response - true or false
     */
    ApiResponse<Boolean> isOnAllowList(final String emailAddress);

}