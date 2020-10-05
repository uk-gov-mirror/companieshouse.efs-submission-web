package uk.gov.companieshouse.efs.web.service.company.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.efs.web.exception.ServiceException;
import uk.gov.companieshouse.efs.web.model.company.CompanyDetail;
import uk.gov.companieshouse.efs.web.service.api.ApiClientService;
import uk.gov.companieshouse.efs.web.service.company.CompanyService;
import uk.gov.companieshouse.efs.web.transformer.CompanyDetailTransformer;
import uk.gov.companieshouse.logging.Logger;

@Service
public class CompanyServiceImpl implements CompanyService {
    private Logger logger;

    private ApiClientService apiClientService;
    private CompanyDetailTransformer companyDetailTransformer;
    private static final UriTemplate GET_COMPANY_URI = new UriTemplate("/company/{companyNumber}");

    /**
     * Constructor used by child controllers.
     *
     * @param apiClientService calls CompanyProfile API
     * @param companyDetailTransformer sets CompanyDetail model
     * @param logger the CH logger
     */
    public CompanyServiceImpl(ApiClientService apiClientService, CompanyDetailTransformer companyDetailTransformer, final Logger logger) {
        this.apiClientService = apiClientService;
        this.companyDetailTransformer = companyDetailTransformer;
        setLogger(logger);
    }

    private final void setLogger(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public CompanyProfileApi getCompanyProfile(String companyNumber) {

        ApiClient apiClient = apiClientService.getApiClient();

        CompanyProfileApi companyProfileApi;

        String uri = GET_COMPANY_URI.expand(companyNumber).toString();

        try {
            companyProfileApi = apiClient.company().get(uri).execute().getData();
        } catch (ApiErrorResponseException ex) {
            logger.debug("Failed to get company profile from: " + uri);
            throw new ServiceException("Error retrieving company profile", ex);
        } catch (URIValidationException ex) {
            logger.debug("Failed to get company profile from: " + uri);
            throw new ServiceException("Invalid URI for company resource", ex);
        }

        return companyProfileApi;
    }

    @Override
    public void getCompanyDetail(final CompanyDetail companyDetailAttribute, String companyNumber) {

        companyDetailTransformer.getCompanyDetail(companyDetailAttribute, getCompanyProfile(companyNumber));
    }

}
