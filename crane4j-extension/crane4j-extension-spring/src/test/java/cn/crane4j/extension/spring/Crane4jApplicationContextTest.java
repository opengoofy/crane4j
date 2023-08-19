package cn.crane4j.extension.spring;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.container.Containers;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.executor.handler.OneToOneAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectiveDisassembleOperationHandler;
import cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.util.ReflectUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Map;

/**
 * test for {@link Crane4jApplicationContext}
 *
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {DefaultCrane4jSpringConfiguration.class, Crane4jApplicationContextTest.TestConfig.class})
public class Crane4jApplicationContextTest {

    @Autowired
    private Crane4jApplicationContext context;
    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void test() {
        Assert.assertNotNull(context.getTypeResolver());
        Assert.assertNotNull(context.getPropertyOperator());
        Assert.assertNotNull(context.getBeanOperationsParser(TypeHierarchyBeanOperationParser.class));
        Assert.assertNotNull(context.getBeanOperationsParser("typeHierarchyBeanOperationParser"));
        Assert.assertNotNull(context.getBeanOperationExecutor(DisorderedBeanOperationExecutor.class));
        Assert.assertNotNull(context.getBeanOperationExecutor("disorderedBeanOperationExecutor"));
        Assert.assertNotNull(context.getAssembleOperationHandler(OneToOneAssembleOperationHandler.class));
        Assert.assertNotNull(context.getAssembleOperationHandler("oneToOneAssembleOperationHandler"));
        Assert.assertNotNull(context.getDisassembleOperationHandler(ReflectiveDisassembleOperationHandler.class));
        Assert.assertNotNull(context.getDisassembleOperationHandler("reflectiveDisassembleOperationHandler"));
        Assert.assertNotNull(context.getContainer("test"));
        Assert.assertNotNull(context.getContainer("testBean"));

        int size = context.getContainerLifecycleProcessors().size();
        ContainerLifecycleProcessor processor = new ContainerLifecycleProcessor() {
        };
        context.registerContainerLifecycleProcessor(processor);
        Assert.assertEquals(size + 1, context.getContainerLifecycleProcessors().size());
        context.registerContainerLifecycleProcessor(processor);
        Assert.assertEquals(size + 2, context.getContainerLifecycleProcessors().size());

        Map<String, Container<?>> containerMap = ReflectUtils.getFieldValue(context, "containerMap");
        Assert.assertNotNull(containerMap);
        Assert.assertFalse(containerMap.isEmpty());
        context.destroy();
        Assert.assertTrue(containerMap.isEmpty());

        // get by bean name
        Assert.assertEquals(
            applicationContext.getBean("testBean"), context.getContainer("testBean")
        );
        Assert.assertEquals(
            applicationContext.getBean("testProvider"), context.getContainerProvider("testProvider")
        );
        Assert.assertEquals(
            applicationContext.getBean(ConverterManager.class), context.getConverterManager()
        );
    }

    @Test
    public void replaceContainer() {
        Object old1 = context.registerContainer(LambdaContainer.forLambda("replaceContainer", ids -> Collections.emptyMap()));
        Assert.assertNull(old1);
        Container<Object> container1 = context.getContainer("replaceContainer");
        Assert.assertSame(container1, context.getContainer("replaceContainer"));

        Object old2 = context.registerContainer(LambdaContainer.forLambda("replaceContainer", ids -> Collections.emptyMap()));
        Assert.assertSame(container1, old2);
        Assert.assertNotSame(container1, context.getContainer("replaceContainer"));
    }

    protected static class TestConfig {
        @Bean("testBean")
        public Container<String> container() {
            return Containers.forMap("test", Collections.singletonMap("key", "value"));
        }

        @Bean("testProvider")
        public ContainerProvider testContainerProvider() {
            return new ContainerProvider() {
                @Override
                public @Nullable <K> Container<K> getContainer(String namespace) {
                    return null;
                }
            };
        }
    }
}
