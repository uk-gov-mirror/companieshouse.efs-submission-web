package uk.gov.companieshouse.efs.web.model.company;

import java.time.LocalDate;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class CompanyDetail {

    private String submissionId;
    private String companyName;
    private String companyNumber;
    private String registeredOfficeAddress;
    private LocalDate incorporationDate;

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(final String submissionId) {
        this.submissionId = submissionId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getRegisteredOfficeAddress() {
        return registeredOfficeAddress;
    }

    public void setRegisteredOfficeAddress(String registeredOfficeAddress) {
        this.registeredOfficeAddress = registeredOfficeAddress;
    }

    public LocalDate getIncorporationDate() {
        return incorporationDate;
    }

    public void setIncorporationDate(LocalDate incorporationDate) {
        this.incorporationDate = incorporationDate;
    }

    /**
     * Clear out attribute values.
     */
    public void clear() {
        submissionId = null;
        companyName = null;
        companyNumber = null;
        registeredOfficeAddress = null;
        incorporationDate = null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CompanyDetail that = (CompanyDetail) o;
        return Objects.equals(getSubmissionId(), that.getSubmissionId()) && Objects
            .equals(getCompanyName(), that.getCompanyName()) && Objects
            .equals(getCompanyNumber(), that.getCompanyNumber()) && Objects
            .equals(getRegisteredOfficeAddress(), that.getRegisteredOfficeAddress()) && Objects
            .equals(getIncorporationDate(), that.getIncorporationDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubmissionId(), getCompanyName(), getCompanyNumber(), getRegisteredOfficeAddress(),
            getIncorporationDate());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("submissionId", submissionId)
            .append("companyName", companyName).append("companyNumber", companyNumber)
            .append("registeredOfficeAddress", registeredOfficeAddress).append("incorporationDate", incorporationDate)
            .toString();
    }
}
