package cn.crane4j.extension.spring;

import cn.crane4j.core.container.ConstantContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerDefinition;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.executor.handler.OneToOneAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectDisassembleOperationHandler;
import cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
import cn.crane4j.core.util.ReflectUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
@ContextConfiguration(classes = {Crane4jSpringTestConfiguration.class, Crane4jApplicationContextTest.TestConfig.class})
public class Crane4jApplicationContextTest {

    @Autowired
    private Crane4jApplicationContext context;

    @Test
    public void test() {
        Assert.assertNotNull(context.getTypeResolver());
        Assert.assertNotNull(context.getPropertyOperator());
        Assert.assertNotNull(context.getBeanOperationsParser(TypeHierarchyBeanOperationParser.class.getSimpleName()));
        Assert.assertNotNull(context.getBeanOperationExecutor(DisorderedBeanOperationExecutor.class.getSimpleName()));
        Assert.assertNotNull(context.getAssembleOperationHandler(OneToOneAssembleOperationHandler.class.getSimpleName()));
        Assert.assertNotNull(context.getDisassembleOperationHandler(ReflectDisassembleOperationHandler.class.getSimpleName()));
        Assert.assertNotNull(context.getContainer("test"));
        Assert.assertNotNull(context.getContainer("testBean"));

        int size = context.getContainerLifecycleProcessors().size();
        ContainerLifecycleProcessor processor = new ContainerLifecycleProcessor() { };
        context.registerContainerLifecycleProcessor(processor);
        Assert.assertEquals(size + 1, context.getContainerLifecycleProcessors().size());
        context.registerContainerLifecycleProcessor(processor);
        Assert.assertEquals(size + 2, context.getContainerLifecycleProcessors().size());

        Map<String, Container<?>> containerMap = ReflectUtils.getFieldValue(context, "containerMap");
        Assert.assertFalse(containerMap.isEmpty());
        context.destroy();
        Assert.assertTrue(containerMap.isEmpty());
    }

    @Test
    public void replaceContainer() {
        ContainerDefinition definition1 = context.registerContainer(LambdaContainer.forLambda("replaceContainer", ids -> Collections.emptyMap()));
        Assert.assertNotNull(definition1);
        Container<Object> container1 = context.getContainer("replaceContainer");
        Assert.assertSame(container1, context.getContainer("replaceContainer"));

        ContainerDefinition container2 = context.registerContainer(LambdaContainer.forLambda("replaceContainer", ids -> Collections.emptyMap()));
        Assert.assertNotSame(definition1, container2);
        Assert.assertNotSame(container1, context.getContainer("replaceContainer"));
    }

    protected static class TestConfig {
        @Bean("testBean")
        public ConstantContainer<String> container() {
            return ConstantContainer.forMap("test", Collections.singletonMap("key", "value"));
        }
    }
}
