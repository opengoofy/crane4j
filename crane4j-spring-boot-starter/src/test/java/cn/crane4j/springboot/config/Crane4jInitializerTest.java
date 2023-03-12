package cn.crane4j.springboot.config;

import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.container.CacheableContainer;
import cn.crane4j.core.container.ConstantContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.parser.AnnotationAwareBeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.springboot.support.Crane4jApplicationContext;
import cn.crane4j.springboot.support.aop.MethodArgumentAutoOperateAspect;
import cn.crane4j.springboot.support.aop.MethodResultAutoOperateAspect;
import cn.hutool.core.util.ReflectUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Map;

/**
 * test for {@link Crane4jProperties}
 *
 * @author huangchengxing
 */
@TestPropertySource(properties = "spring.config.location = classpath:test.yml")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Crane4jAutoConfiguration.class, Crane4jInitializer.class})
public class Crane4jInitializerTest {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private Crane4jProperties crane4jProperties;

    @SuppressWarnings("unchecked")
    @Test
    public void test() {
        Assert.assertTrue(crane4jProperties.isEnableAsmReflect());
        Assert.assertTrue(crane4jProperties.isOnlyLoadAnnotatedEnum());

        // 不启用方法返回值自动填充
        Assert.assertFalse(crane4jProperties.isEnableMethodResultAutoOperate());
        Assert.assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(MethodResultAutoOperateAspect.class));

        // 不启用方法参数自动填充
        Assert.assertFalse(crane4jProperties.isEnableMethodArgumentAutoOperate());
        Assert.assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(MethodArgumentAutoOperateAspect.class));

        // 注册枚举容器
        Crane4jApplicationContext context = applicationContext.getBean(Crane4jApplicationContext.class);
        Assert.assertEquals(
            Collections.singleton("cn.crane4j.springboot.config.*"),
            crane4jProperties.getContainerEnumPackages()
        );
        Assert.assertTrue(context.getContainer("test1") instanceof ConstantContainer);
        Container<?> container = context.getContainer("test2");
        Assert.assertTrue(container instanceof CacheableContainer);
        Assert.assertTrue(((CacheableContainer<?>)container).getContainer() instanceof ConstantContainer);
        Assert.assertSame(
            applicationContext.getBean(CacheManager.class).getCache("shared-cache"),
            ((CacheableContainer<?>)container).getCache()
        );

        // 注册常量类容器
        Assert.assertEquals(
            Collections.singleton("cn.crane4j.springboot.config.*"),
            crane4jProperties.getContainerConstantPackages()
        );
        Container<?> constant = context.getContainer("constant");
        Assert.assertTrue(constant instanceof ConstantContainer);
        Map<?, ?> constantData = constant.get(null);
        Assert.assertEquals("one", constantData.get("one"));
        Assert.assertEquals("two", constantData.get("two"));
        Assert.assertFalse(constantData.containsKey("THREE"));

        // 预加载实体类操作配置
        Assert.assertEquals(
            Collections.singleton("cn.crane4j.springboot.config.*"),
            crane4jProperties.getOperateEntityPackages()
        );
        AnnotationAwareBeanOperationParser parser = applicationContext.getBean(AnnotationAwareBeanOperationParser.class);
        Map<Class<?>, BeanOperations> parsedBeanOperations = (Map<Class<?>, BeanOperations>)ReflectUtil.getFieldValue(parser, "parsedBeanOperations");
        Assert.assertTrue(parsedBeanOperations.containsKey(TestBean1.class));
        Assert.assertTrue(parsedBeanOperations.containsKey(TestBean2.class));
    }
}
