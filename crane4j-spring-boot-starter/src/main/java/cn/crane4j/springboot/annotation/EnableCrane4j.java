package cn.crane4j.springboot.annotation;

import cn.crane4j.springboot.config.Crane4jAutoConfiguration;
import cn.crane4j.springboot.config.Crane4jInitializer;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable crane4j auto configuration.
 *
 * @author huangchengxing
 * @see Crane4jAutoConfiguration
 * @see Crane4jInitializer
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({Crane4jAutoConfiguration.class, Crane4jInitializer.class})
public @interface EnableCrane4j {
}
