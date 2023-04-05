package cn.crane4j.spring.boot.config.main;

import cn.crane4j.core.cache.CacheManager;
import cn.crane4j.core.container.CacheableContainer;
import cn.crane4j.core.container.ConstantContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.BeanOperationsResolver;
import cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
import cn.crane4j.extension.spring.Crane4jApplicationContext;
import cn.crane4j.spring.boot.config.Crane4jAutoConfiguration;
import cn.hutool.core.util.ReflectUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * test for {@link Crane4jAutoConfiguration.Properties}
 *
 * @author huangchengxing
 */
@SpringBootApplication
@TestPropertySource(properties = "spring.config.location = classpath:test.yml")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Crane4jAutoConfiguration.class)
public class Crane4jInitializerTest {

    @Autowired
    private BeanOperationParser beanOperationParser;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private Crane4jAutoConfiguration.Properties properties;

    @SuppressWarnings("unchecked")
    @Test
    public void test() {
        Assert.assertTrue(properties.isEnableAsmReflect());
        Assert.assertTrue(properties.isOnlyLoadAnnotatedEnum());

        // 启用方法返回值自动填充
        Assert.assertTrue(properties.isEnableMethodResultAutoOperate());
        //Assert.assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(MethodResultAutoOperateAspect.class));

        // 启用方法参数自动填充
        Assert.assertTrue(properties.isEnableMethodArgumentAutoOperate());
        //Assert.assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(MethodArgumentAutoOperateAspect.class));

        // 配置解析器
        if (beanOperationParser instanceof TypeHierarchyBeanOperationParser) {
            Set<BeanOperationsResolver> resolvers = (Set<BeanOperationsResolver>)ReflectUtil.getFieldValue(beanOperationParser, "beanOperationsResolvers");
            Assert.assertEquals(1, resolvers.size());
        }

        // 注册枚举容器
        Crane4jApplicationContext context = applicationContext.getBean(Crane4jApplicationContext.class);
        Assert.assertEquals(
            Collections.singleton("cn.crane4j.spring.boot.config.main.*"),
            properties.getContainerEnumPackages()
        );
        Assert.assertTrue(context.getContainer("test1") instanceof ConstantContainer);
        Container<?> container = context.getContainer("test2");
        Assert.assertTrue(container instanceof CacheableContainer);
        Assert.assertTrue(((CacheableContainer<?>)container).getContainer() instanceof ConstantContainer);
        Assert.assertSame(
            applicationContext.getBean(CacheManager.class),
            ((CacheableContainer<?>)container).getCacheManager()
        );
        Assert.assertEquals(
            "shared-cache", ((CacheableContainer<?>)container).getCacheName()
        );

        // 注册常量类容器
        Assert.assertEquals(
            Collections.singleton("cn.crane4j.spring.boot.config.main.*"),
            properties.getContainerConstantPackages()
        );
        Container<?> constant = context.getContainer("constant");
        Assert.assertTrue(constant instanceof ConstantContainer);
        Map<?, ?> constantData = constant.get(null);
        Assert.assertEquals("one", constantData.get("one"));
        Assert.assertEquals("two", constantData.get("two"));
        Assert.assertFalse(constantData.containsKey("THREE"));

        // 预加载实体类操作配置
        Assert.assertEquals(
            Collections.singleton("cn.crane4j.spring.boot.config.main.*"),
            properties.getOperateEntityPackages()
        );
        BeanOperationParser parser = applicationContext.getBean(TypeHierarchyBeanOperationParser.class);
        Map<Class<?>, BeanOperations> parsedBeanOperations = (Map<Class<?>, BeanOperations>)ReflectUtil.getFieldValue(parser, "resolvedTypes");
        Assert.assertTrue(parsedBeanOperations.containsKey(TestBean1.class));
        Assert.assertTrue(parsedBeanOperations.containsKey(TestBean2.class));
    }
}
