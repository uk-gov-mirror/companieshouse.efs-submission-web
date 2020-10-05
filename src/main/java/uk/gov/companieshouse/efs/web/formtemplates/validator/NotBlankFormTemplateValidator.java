package uk.gov.companieshouse.efs.web.formtemplates.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.efs.formtemplates.FormTemplateApi;

/**
 * Custom validator to validate a blank form template  {@link FormTemplateApi}.
 */
@Component
public class NotBlankFormTemplateValidator implements ConstraintValidator<NotBlankFormTemplate, FormTemplateApi> {

    @Override
    public boolean isValid(final FormTemplateApi formTemplate,
                           final ConstraintValidatorContext constraintValidatorContext) {

        return formTemplate == null || (StringUtils.isNotBlank(formTemplate.getFormType()));
    }
}
