package uk.gov.companieshouse.efs.web.transformer;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.efs.web.model.company.CompanyDetail;

public interface CompanyDetailTransformer {
    void getCompanyDetail(final CompanyDetail companyDetailAttribute, CompanyProfileApi companyProfile);
}
