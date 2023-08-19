package cn.crane4j.spring.boot.annotation;

import cn.crane4j.extension.spring.annotation.ContainerConstantScan;
import cn.crane4j.extension.spring.annotation.ContainerEnumScan;
import cn.crane4j.extension.spring.annotation.OperatorScan;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable crane4j components and other extension plugins.
 *
 * @author huangchengxing
 * @see EnableCrane4jFramework
 * @see EnableCrane4jMybatisPlusExtension
 * @see OperatorScan
 * @see ContainerConstantScan
 * @see ContainerEnumScan
 */
@OperatorScan
@ContainerConstantScan
@ContainerEnumScan
@EnableCrane4jFramework
@EnableCrane4jMybatisPlusExtension
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableCrane4j {

    /**
     * The package path which has the operator interface.
     *
     * @return package path
     * @see OperatorScan#includePackages
     */
    @AliasFor(annotation = OperatorScan.class, attribute = "includePackages")
    String[] operatorPackages() default {};

    /**
     * The package path which has the container constant.
     *
     * @return package path
     * @see ContainerConstantScan#includePackages
     */
    @AliasFor(annotation = ContainerConstantScan.class, attribute = "includePackages")
    String[] constantPackages() default {};

    /**
     * The package path which has the enum container.
     *
     * @return package path
     * @see ContainerEnumScan#includePackages
     */
    @AliasFor(annotation = ContainerEnumScan.class, attribute = "includePackages")
    String[] enumPackages() default {};
}
