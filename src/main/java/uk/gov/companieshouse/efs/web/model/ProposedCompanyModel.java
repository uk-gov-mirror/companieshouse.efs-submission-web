package uk.gov.companieshouse.efs.web.model;

import java.util.Objects;
import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class ProposedCompanyModel {

    private String name;

    public ProposedCompanyModel() {
        super();

        this.name = "";
    }

    /**
     * Constructor sets the required value from the {@link ProposedCompanyModel}
     *
     * @param original the {@link ProposedCompanyModel}
     */
    public ProposedCompanyModel(final ProposedCompanyModel original) {
        this.name = original.getName();
    }

    @NotBlank(message = "{proposedCompany.error}")
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ProposedCompanyModel that = (ProposedCompanyModel) o;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("name", name)
            .toString();
    }

}
