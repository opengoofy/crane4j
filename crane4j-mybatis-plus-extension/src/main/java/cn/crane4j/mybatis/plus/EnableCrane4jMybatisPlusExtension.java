package cn.crane4j.mybatis.plus;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable crane4j mybatis plus extension.
 *
 * @author huangchengxing
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(Crane4jMybatisPlusConfiguration.class)
public @interface EnableCrane4jMybatisPlusExtension {
}
