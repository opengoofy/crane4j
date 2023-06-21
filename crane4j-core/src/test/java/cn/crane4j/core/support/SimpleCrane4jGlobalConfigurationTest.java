package cn.crane4j.core.support;

import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.container.lifecycle.ContainerLifecycleProcessor;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.executor.handler.ManyToManyAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectiveDisassembleOperationHandler;
import cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

/**
 * test for {@link SimpleCrane4jGlobalConfiguration}
 *
 * @author huangchengxing
 */
public class SimpleCrane4jGlobalConfigurationTest {

    private SimpleCrane4jGlobalConfiguration configuration;

    @Before
    public void init() {
        configuration =  SimpleCrane4jGlobalConfiguration.create();
    }

    @Test
    public void registerContainerLifecycleProcessor() {
        Collection<ContainerLifecycleProcessor> awareList = configuration.getContainerLifecycleProcessors();
        int size = awareList.size();
        ContainerLifecycleProcessor processor = new ContainerLifecycleProcessor() { };
        configuration.registerContainerLifecycleProcessor(processor);
        Assert.assertEquals(size + 1, awareList.size());
        configuration.registerContainerLifecycleProcessor(processor);
        Assert.assertEquals(size + 2, awareList.size());
    }

    @Test
    public void getContainerProvider() {
        Assert.assertSame(configuration, configuration.getContainerProvider(configuration.getClass().getSimpleName()));
    }

    @Test
    public void getPropertyOperator() {
        Assert.assertNotNull(configuration.getPropertyOperator());
    }

    @Test
    public void getTypeResolver() {
        Assert.assertNotNull(configuration.getTypeResolver());
    }

    @Test
    public void getContainer() {
        Object old = configuration.registerContainer(LambdaContainer.forLambda("test", DataProvider.empty()));
        Assert.assertNull(old);
        Assert.assertNotNull(configuration.getContainer("test"));
    }

    @Test
    public void getBeanOperationsParser() {
        Assert.assertNotNull(configuration.getBeanOperationsParser(TypeHierarchyBeanOperationParser.class.getSimpleName()));
    }

    @Test
    public void getAssembleOperationHandler() {
        Assert.assertNotNull(configuration.getAssembleOperationHandler(ManyToManyAssembleOperationHandler.class.getSimpleName()));
    }

    @Test
    public void getGetBeanOperationExecutor() {
        Assert.assertNotNull(configuration.getBeanOperationExecutor(DisorderedBeanOperationExecutor.class.getSimpleName()));
    }

    @Test
    public void getDisassembleOperationHandler() {
        Assert.assertNotNull(configuration.getDisassembleOperationHandler(ReflectiveDisassembleOperationHandler.class.getSimpleName()));
    }
}
