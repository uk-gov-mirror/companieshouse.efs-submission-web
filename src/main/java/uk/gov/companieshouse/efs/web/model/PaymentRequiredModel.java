package uk.gov.companieshouse.efs.web.model;

import java.math.BigDecimal;
import java.util.Objects;
import javax.validation.constraints.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class PaymentRequiredModel {

    private String submissionId;
    private String companyNumber;
    private BigDecimal feeAmount;
    private String paymentReference;

    public PaymentRequiredModel() {
    }

    /**
     * Constructor sets the application id and required value from the {@link PaymentRequiredModel}.
     *
     * @param original the {@link PaymentRequiredModel}
     */
    public PaymentRequiredModel(final PaymentRequiredModel original) {
        this.submissionId = original.getSubmissionId();
        this.paymentReference = original.getPaymentReference();
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(final String submissionId) {
        this.submissionId = submissionId;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(final String companyNumber) {
        this.companyNumber = companyNumber;
    }

    // Length = 10, uppercase alphanumeric, allowing optional whitespace separators anywhere
    @Pattern(regexp = "\\s*(?:[A-Z0-9]\\s*){10}$")
    public String getPaymentReference() {
        return StringUtils.deleteWhitespace(paymentReference);
    }

    public void setPaymentReference(final String paymentReference) {
        this.paymentReference = paymentReference;
    }

    @NumberFormat(style = NumberFormat.Style.CURRENCY, pattern = "####") // decimal digits only if needed
    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(final BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PaymentRequiredModel that = (PaymentRequiredModel) o;
        return Objects.equals(getSubmissionId(), that.getSubmissionId()) && Objects.equals(getPaymentReference(),
            that.getPaymentReference());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubmissionId(), getPaymentReference());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("submissionId", submissionId).append(
            "paymentReference", paymentReference).toString();
    }

}
