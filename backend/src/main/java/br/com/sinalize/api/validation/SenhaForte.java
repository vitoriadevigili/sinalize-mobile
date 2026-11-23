package br.com.sinalize.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = {})
@Size(min = 6, max = 20)
@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[\\d\\W]).*$")
@ReportAsSingleViolation
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface SenhaForte {

    String message() default "A senha deve ter de 6 a 20 caracteres, com pelo menos 1 letra maiúscula, "
            + "1 minúscula e 1 número ou símbolo.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
