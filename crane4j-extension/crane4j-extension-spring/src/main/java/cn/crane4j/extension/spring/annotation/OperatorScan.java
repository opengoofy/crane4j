package cn.crane4j.extension.spring.annotation;

import cn.crane4j.extension.spring.scanner.OperatorBeanDefinitionRegistrar;
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
     * The package path which will be scanned.
     *
     * @return package path
     */
    String[] includePackages() default {};

    /**
     * The class type which will be scanned.
     *
     * @return class type
     */
    Class<?>[] includeClasses() default {};

    /**
     * The class type which will not be scanned,
     * it will exclude the class type which is specified in {@link #includeClasses()}.
     *
     * @return class type
     */
    Class<?>[] excludeClasses() default {};
}
