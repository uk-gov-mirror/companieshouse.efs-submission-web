package uk.gov.companieshouse.efs.web.service.company;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.efs.web.model.company.CompanyDetail;

public interface CompanyService {

    CompanyProfileApi getCompanyProfile(String companyNumber);

    void getCompanyDetail(final CompanyDetail companyDetailAttribute, String companyNumber);

}
