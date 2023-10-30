package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Marker a parameter as dynamic container in execution.<br />
 * support following types of parameter:
 * <ul>
 *     <li>{@link cn.crane4j.core.container.Container}</li>
 *     <li>{@link java.util.Map}</li>
 *     <li>{@link cn.crane4j.core.support.DataProvider}</li>
 * </ul>
 *
 * @author huangchengxing
 * @see cn.crane4j.core.support.operator.DynamicContainerOperatorProxyMethodFactory
 * @see cn.crane4j.core.support.ContainerAdapterRegister
 * @since 1.3.0
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContainerParam {

    /**
     * Container namespace of container what based on parameter.
     *
     * @return namespace
     */
    String value() default "";
}
