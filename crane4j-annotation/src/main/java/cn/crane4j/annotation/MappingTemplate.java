package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotations are used to declare a set of field mapping configurations on the class,
 * and are introduced in {@link Assemble#propTemplates()} by specifying the annotated class.<br />
 * This annotation is applicable to the scene where there are too many fields involved
 * in a single assembly to avoid too large a single {@link Assemble} configuration.
 *
 * @author huangchengxing
 * @see Assemble#propTemplates()
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MappingTemplate {

    /**
     * Mapping field required.
     *
     * @return mapping
     */
    Mapping[] value() default {};
}
