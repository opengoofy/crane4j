package cn.crane4j.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When {@link ContainerMethod} annotation is on the class,
 * specify the method to bind through the current annotation.
 *
 * @author huangchengxing
 * @see ContainerMethod#bind()
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Bind {

    /**
     * Method name
     *
     * @return method name
     */
    String value();

    /**
     * Method parameter types
     *
     * @return parameter types
     */
    Class<?>[] paramTypes() default {};
}
