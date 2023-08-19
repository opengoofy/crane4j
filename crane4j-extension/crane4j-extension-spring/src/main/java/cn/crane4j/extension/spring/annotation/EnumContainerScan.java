package cn.crane4j.extension.spring.annotation;

import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.extension.spring.scanner.ScannedContainerRegister;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Scan enum containers for the specified package path.
 *
 * @author huangchengxing
 * @see ContainerEnum
 * @since 2.1.0
 */
@ConstantContainerScan
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(ScannedContainerRegister.class)
public @interface EnumContainerScan {

    /**
     * Whether to only load the enum which is annotated by {@link ContainerEnum}.
     *
     * @return boolean
     */
    boolean isOnlyLoadAnnotatedEnum() default true;

    /**
     * The package path which will be scanned.
     *
     * @return package path
     */
    @AliasFor(value = "includePackages", annotation = ComponentTypeScan.class)
    String[] includePackages() default {};

    /**
     * The class type which will be scanned.
     *
     * @return class type
     */
    @AliasFor(value = "includeClasses", annotation = ComponentTypeScan.class)
    Class<?>[] includeClasses() default {};

    /**
     * The class type which will not be scanned,
     * it will exclude the class type which is specified in {@link #includeClasses()}.
     *
     * @return class type
     */
    @AliasFor(value = "excludeClasses", annotation = ComponentTypeScan.class)
    Class<?>[] excludeClasses() default {};
}
