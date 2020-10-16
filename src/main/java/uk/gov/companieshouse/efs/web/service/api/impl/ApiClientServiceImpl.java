package uk.gov.companieshouse.efs.web.service.api.impl;

import static uk.gov.companieshouse.efs.web.configuration.DataCacheConfig.IP_ALLOW_LIST;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.efs.submissions.CompanyApi;
import uk.gov.companieshouse.api.model.efs.submissions.ConfirmAuthorisedApi;
import uk.gov.companieshouse.api.model.efs.submissions.FileListApi;
import uk.gov.companieshouse.api.model.efs.submissions.FormTypeApi;
import uk.gov.companieshouse.api.model.efs.submissions.PresenterApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionResponseApi;
import uk.gov.companieshouse.api.model.efs.submissions.SubmissionStatus;
import uk.gov.companieshouse.api.model.paymentsession.SessionListApi;
import uk.gov.companieshouse.efs.web.exception.UrlEncodingException;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.sdk.manager.ApiClientManager;

/**
 * Service sends and receives secure REST messages to the api.
 */
@Primary
@Service
public class ApiClientServiceImpl extends BaseApiClientServiceImpl implements ApiClientService {

    /**
     * Construct an {@link ApiClientServiceImpl}.
     *
     * @param logger the CH logger
     */
    @Autowired
    public ApiClientServiceImpl(final Logger logger) {
        super(logger);
    }

    @Override
    public InternalApiClient getApiClient() {
        return ApiClientManager.getPrivateSDK();
    }

    @Override
    public ApiResponse<SubmissionResponseApi> createSubmission(final PresenterApi presenter) {
        final String uri = ROOT_URI + "/submissions/new";

        return executeOp("createSubmission", uri,
            getApiClient().privateEfsResourceHandler().submissions().newSubmission().create(uri, presenter));
    }

    @Override
    public ApiResponse<SubmissionApi> getSubmission(final String submissionId) {
        final String uri = SUB_URI + submissionId;

        return executeOp("getSubmission", uri,
            getApiClient().privateEfsResourceHandler().submissions().getSubmission().get(uri));
    }

    @Override
    public ApiResponse<SubmissionResponseApi> putCompany(final String submissionId, final CompanyApi company) {
        final String uri = SUB_URI + submissionId + "/company";

        return executeOp("submitCompany", uri,
            getApiClient().privateEfsResourceHandler().submissions().company().upsert(uri, company));
    }

    @Override
    public ApiResponse<SubmissionResponseApi> putFormType(final String submissionId, final FormTypeApi formType) {
        final String uri = SUB_URI + submissionId + "/form";

        return executeOp("submitFormType", uri,
            getApiClient().privateEfsResourceHandler().submissions().form().upsert(uri, formType));
    }

    @Override
    public ApiResponse<SubmissionResponseApi> putFileList(final String submissionId, final FileListApi fileList) {
        final String uri = SUB_URI + submissionId + "/files";

        return executeOp("submitFiles", uri, getApiClient().privateEfsResourceHandler().submissions().file().upsert(uri, fileList));
    }

    @Override
    public ApiResponse<SubmissionResponseApi> putPaymentSessions(final String submissionId,
        final SessionListApi paymentSessions) {
        final String uri = SUB_URI + submissionId + "/payment-sessions";

        return executeOp("submitPaymentSessions", uri,
            getApiClient().privateEfsResourceHandler().submissions().paymentSessions().upsert(uri, paymentSessions));
    }

    @Override
    public ApiResponse<SubmissionResponseApi> putConfirmAuthorised(final String submissionId, final ConfirmAuthorisedApi confirmAuthorised) {
        final String uri = SUB_URI + submissionId + "/confirmAuthorised";

        return executeOp("confirmAuthorised", uri,
            getApiClient().privateEfsResourceHandler().submissions().confirmAuthorised().upsert(uri, confirmAuthorised));
    }

    @Override
    public ApiResponse<SubmissionResponseApi> putSubmissionSubmitted(final String submissionId) {
        final String uri = SUB_URI + submissionId;

        return executeOp("completeSubmission", uri,
            getApiClient().privateEfsResourceHandler().submissions().submit().upsert(uri, SubmissionStatus.SUBMITTED));
    }

    @Override
    @Cacheable(value = IP_ALLOW_LIST, sync = true)
    public ApiResponse<Boolean> isOnAllowList(final String emailAddress) {

        String encodedEmailAddress;

        try {
            encodedEmailAddress = URLEncoder.encode(emailAddress, "UTF-8");
        }
        catch (UnsupportedEncodingException ex) {
            throw new UrlEncodingException("Error with encoding email address for isOnAllowList. Email address: " + emailAddress, ex);
        }

        final String uri = ROOT_URI + "/company-authentication/allow-list/" + encodedEmailAddress;

        return executeOp("getIsOnAllowList", uri,
            getApiClient().privateEfsResourceHandler().companyAuthAllowList().isOnAllowList().get(uri));
    }
}
