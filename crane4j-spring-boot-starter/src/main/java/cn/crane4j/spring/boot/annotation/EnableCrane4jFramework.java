package cn.crane4j.spring.boot.annotation;

import cn.crane4j.spring.boot.config.Crane4jAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable crane4j components
 *
 * @author huangchengxing
 */
@Deprecated
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(Crane4jAutoConfiguration.class)
public @interface EnableCrane4jFramework {
}
