package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker a parameter as temporary container data in shared context of current thread when execute operation.
 *
 * @author huangchengxing
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SharedContextContainer {

    /**
     * Container namespace of container what based on parameter.
     *
     * @return namespace
     */
    String value() default "";
}
