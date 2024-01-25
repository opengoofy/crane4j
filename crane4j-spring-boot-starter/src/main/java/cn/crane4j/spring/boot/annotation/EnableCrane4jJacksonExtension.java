package cn.crane4j.spring.boot.annotation;

import cn.crane4j.spring.boot.config.Crane4jJacksonConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Enable crane4j jackson extension.
 *
 * <p><b>NOTE</b>: This annotation is deprecated,
 * because since version 2.4.0 crane4j has been integrated into spring boot autoconfig.<br />
 * About package scan config in this annotation, you can replace it with {@link Crane4jComponentScan}.
 *
 * @author huangchengxing
 */
@Deprecated
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(Crane4jJacksonConfiguration.class)
public @interface EnableCrane4jJacksonExtension {
}
