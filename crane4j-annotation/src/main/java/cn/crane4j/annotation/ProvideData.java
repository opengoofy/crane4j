package cn.crane4j.annotation;

import java.lang.annotation.*;

/**
 * Marker a parameter as temporary container data in shared context of current thread when execute operation.
 *
 * @author huangchengxing
 * @see cn.crane4j.core.support.operator.SharedContextProxyMethodFactory
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProvideData {

    /**
     * Container namespace of container what based on parameter.
     *
     * @return namespace
     */
    String value() default "";
}
