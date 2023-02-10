package cn.createsequence.crane4j.springboot.annotation;

import cn.createsequence.crane4j.springboot.config.Crane4jAutoConfiguration;
import cn.createsequence.crane4j.springboot.config.Crane4jInitializer;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用Crane4j配置
 *
 * @author huangchengxing
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ImportAutoConfiguration({Crane4jAutoConfiguration.class, Crane4jInitializer.class})
public @interface EnableCrane4j {
}
