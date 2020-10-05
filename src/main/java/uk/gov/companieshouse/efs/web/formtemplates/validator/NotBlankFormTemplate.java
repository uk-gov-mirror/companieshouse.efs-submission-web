package uk.gov.companieshouse.efs.web.formtemplates.validator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Custom validator for a blank template.
 */

@Documented
@Constraint(validatedBy = NotBlankFormTemplateValidator.class)
@Target({METHOD, FIELD, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBlankFormTemplate {

    /**
     * Default error message key.
     * @return the error message key
     */
    String message() default "NotBlankFormTemplate";

    /**
     * The group this constraint belongs to.
     * @return default is empty group
     */
    Class<?>[] groups() default {};

    /**
     * The specified {@link Payload}.
     * @return default is empty {@link Payload} array
     */
    Class<? extends Payload>[] payload() default {};
}
