package cn.crane4j.extension.spring;

import cn.crane4j.core.container.ConstantContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.support.callback.ContainerRegisterAware;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReflectUtil;
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
        Assert.assertNotNull(context.getBeanOperationsParser(BeanOperationParser.class));
        Assert.assertNotNull(context.getBeanOperationsParser("typeHierarchyBeanOperationParser"));
        Assert.assertNotNull(context.getBeanOperationExecutor(BeanOperationExecutor.class));
        Assert.assertNotNull(context.getBeanOperationExecutor("disorderedBeanOperationExecutor"));
        Assert.assertNotNull(context.getAssembleOperationHandler(AssembleOperationHandler.class));
        Assert.assertNotNull(context.getAssembleOperationHandler("oneToOneReflexAssembleOperationHandler"));
        Assert.assertNotNull(context.getDisassembleOperationHandler(DisassembleOperationHandler.class));
        Assert.assertNotNull(context.getDisassembleOperationHandler("reflectDisassembleOperationHandler"));
        Assert.assertNotNull(context.getContainer("test"));
        Assert.assertNotNull(context.getContainer("testBean"));

        int size = context.getContainerRegisterAwareList().size();
        ContainerRegisterAware aware = new ContainerRegisterAware() { };
        context.addContainerRegisterAware(aware);
        Assert.assertEquals(size + 1, context.getContainerRegisterAwareList().size());
        context.addContainerRegisterAware(aware);
        Assert.assertEquals(size + 1, context.getContainerRegisterAwareList().size());

        @SuppressWarnings("unchecked")
        Map<String, Container<?>> containerMap = (Map<String, Container<?>>)ReflectUtil.getFieldValue(context, "containerMap");
        Assert.assertFalse(containerMap.isEmpty());
        context.destroy();
        Assert.assertTrue(containerMap.isEmpty());
    }

    @Test
    public void containsContainer() {
        Assert.assertTrue(context.containsContainer("test"));
        Assert.assertFalse(context.containsContainer("no registered"));
    }

    @Test
    public void replaceContainer() {
        Assert.assertFalse(context.containsContainer("no registered"));
        Container<?> container1 = context.replaceContainer("no registered", container -> {
            Assert.assertNull(container);
            return LambdaContainer.forLambda("no registered", ids -> Collections.emptyMap());
        });
        Assert.assertNull(container1);
        Assert.assertTrue(context.containsContainer("no registered"));

        Container<?> container2 = context.replaceContainer("no registered", container -> {
            Assert.assertNotNull(container);
            return null;
        });
        Assert.assertNotNull(container2);
        Assert.assertFalse(context.containsContainer("no registered"));
    }

    protected static class TestConfig {
        @Bean("testBean")
        public ConstantContainer<String> container() {
            return ConstantContainer.forMap("test", MapUtil.of("key", "value"));
        }
    }
}
