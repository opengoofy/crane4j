package cn.crane4j.spring.boot.annotation;

import cn.crane4j.spring.boot.config.Crane4jMybatisPlusAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable crane4j mybatis plus extension.
 *
 * @author huangchengxing
 */
@Deprecated
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(Crane4jMybatisPlusAutoConfiguration.class)
public @interface EnableCrane4jMybatisPlusExtension {
}
