package uk.gov.companieshouse.efs.web.categorytemplates.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.efs.categorytemplates.CategoryTemplateApi;

/**
 * Custom validator to validate a blank category template  {@link CategoryTemplateApi}.
 */
@Component
public class NotBlankCategoryTemplateValidator implements ConstraintValidator<NotBlankCategoryTemplate, CategoryTemplateApi> {

    @Override
    public boolean isValid(final CategoryTemplateApi categoryTemplate,
                           final ConstraintValidatorContext constraintValidatorContext) {

        return categoryTemplate == null || (StringUtils.isNotBlank(categoryTemplate.getCategoryType()));
    }
}
