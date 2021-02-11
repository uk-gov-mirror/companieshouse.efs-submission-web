package uk.gov.companieshouse.efs.web.model;

import java.util.Objects;
import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class RegisteredCompanyModel {

    private String registered;

    public RegisteredCompanyModel() {
        super();

        this.registered = "";
    }

    /**
     * Constructor sets the required value from the {@link RegisteredCompanyModel}
     *
     * @param original the {@link RegisteredCompanyModel}
     */
    public RegisteredCompanyModel(final RegisteredCompanyModel original) {
        this.registered = original.getRegistered();
    }

    @NotBlank(message = "{registeredCompany.error}")
    public String getRegistered() {
        return registered;
    }

    public void setRegistered(final String registered) {
        this.registered = registered;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RegisteredCompanyModel that = (RegisteredCompanyModel) o;
        return Objects.equals(getRegistered(), that.getRegistered());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRegistered());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("registered", registered)
            .toString();
    }


}