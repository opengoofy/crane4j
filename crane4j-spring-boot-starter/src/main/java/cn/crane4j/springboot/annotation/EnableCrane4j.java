package cn.crane4j.springboot.annotation;

import cn.crane4j.springboot.config.Crane4jAutoConfiguration;
import cn.crane4j.springboot.config.Crane4jInitializer;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

import java.lang.annotation.*;

/**
 * enable crane4j configuration.
 *
 * @author huangchengxing
 * @see Crane4jAutoConfiguration
 * @see Crane4jInitializer
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ImportAutoConfiguration({Crane4jAutoConfiguration.class, Crane4jInitializer.class})
public @interface EnableCrane4j {
}
