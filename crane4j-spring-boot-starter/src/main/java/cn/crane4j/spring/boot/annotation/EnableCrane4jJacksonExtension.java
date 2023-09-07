package cn.crane4j.spring.boot.annotation;

import cn.crane4j.spring.boot.config.Crane4jJacksonConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable crane4j jackson extension.
 *
 * @author huangchengxing
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(Crane4jJacksonConfiguration.class)
public @interface EnableCrane4jJacksonExtension {
}
