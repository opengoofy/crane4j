package cn.crane4j.mybatis.plus;

import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.expression.ExpressionEvaluator;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.springboot.config.Crane4jAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Crane4j mybatis plus configuration.
 *
 * @author huangchengxing
 */
@EnableConfigurationProperties(Crane4jMybatisPlusProperties.class)
@ConditionalOnClass(GlobalConfig.class)
@AutoConfigureAfter({MybatisPlusAutoConfiguration.class, Crane4jAutoConfiguration.class})
public class Crane4jMybatisPlusConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MpBaseMapperContainerRegister mpBaseMapperContainerRegister(
        ApplicationContext applicationContext, PropertyOperator propertyOperator, Crane4jGlobalConfiguration globalConfiguration) {
        return new MpBaseMapperContainerRegister(applicationContext, globalConfiguration, propertyOperator);
    }


    @ConditionalOnProperty(
        prefix = Crane4jMybatisPlusProperties.CRANE4J_MP_EXTENSION_PREFIX,
        name = "auto-register-mapper",
        havingValue = "true", matchIfMissing = true
    )
    @Bean
    @ConditionalOnMissingBean
    public MpBaseMapperContainerAutoRegistrar mpBaseMapperContainerAutoRegistrar(
        ApplicationContext applicationContext, Crane4jMybatisPlusProperties crane4jMybatisPlusProperties) {
        return new MpBaseMapperContainerAutoRegistrar(applicationContext, crane4jMybatisPlusProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public MpMethodContainerProvider mpMethodContainerProvider(
        ApplicationContext applicationContext, ExpressionEvaluator evaluator,
        MpBaseMapperContainerRegister mpBaseMapperContainerRegister) {
        return new MpMethodContainerProvider(
            applicationContext, mpBaseMapperContainerRegister, evaluator
        );
    }
}
