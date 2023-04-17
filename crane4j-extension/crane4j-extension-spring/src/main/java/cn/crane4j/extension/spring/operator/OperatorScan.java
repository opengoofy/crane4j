package cn.crane4j.extension.spring.operator;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Scan Operator interface for the specified package path.
 *
 * @author huangchengxing
 * @since 1.3.0
 */
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(OperatorBeanDefinitionRegistrar.class)
public @interface OperatorScan {

    /**
     * Base packages to scan for annotated operator interfaces.
     *
     * @return base packages to scan
     */
    String[] scan() default "";

    /**
     * Include type of operator.
     *
     * @return type of operator
     */
    Class<?>[] includes() default {};

    /**
     * Exclude type of operator.
     *
     * @return type of operator
     */
    Class<?>[] excludes() default {};
}
