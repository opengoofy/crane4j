package cn.crane4j.spring.boot.annotation;

import java.lang.annotation.*;

/**
 * Enable crane4j components and other extension plugins.
 *
 * @author huangchengxing
 * @see EnableCrane4jFramework
 * @see EnableCrane4jMybatisPlusExtension
 */
@EnableCrane4jFramework
@EnableCrane4jMybatisPlusExtension
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableCrane4j {
}
