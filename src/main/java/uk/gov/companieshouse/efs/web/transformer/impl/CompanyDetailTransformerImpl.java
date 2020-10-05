package uk.gov.companieshouse.efs.web.transformer.impl;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.company.RegisteredOfficeAddressApi;
import uk.gov.companieshouse.efs.web.model.company.CompanyDetail;
import uk.gov.companieshouse.efs.web.transformer.CompanyDetailTransformer;

@Component
public class CompanyDetailTransformerImpl implements CompanyDetailTransformer {

    @Override
    public void getCompanyDetail(final CompanyDetail companyDetailAttribute, CompanyProfileApi companyProfile) {

        companyDetailAttribute.setCompanyName(companyProfile.getCompanyName());
        companyDetailAttribute.setCompanyNumber(companyProfile.getCompanyNumber());

        RegisteredOfficeAddressApi registeredOfficeAddress = companyProfile.getRegisteredOfficeAddress();

        if (registeredOfficeAddress != null) {

            companyDetailAttribute.setRegisteredOfficeAddress(Stream.of(registeredOfficeAddress.getAddressLine1(),
                registeredOfficeAddress.getAddressLine2(), registeredOfficeAddress.getPostalCode())
                .filter(Objects::nonNull).collect(Collectors.joining("\n")));
        }
        companyDetailAttribute.setIncorporationDate(companyProfile.getDateOfCreation());
    }
}
