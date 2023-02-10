package cn.createsequence.crane4j.core.support;

import cn.createsequence.crane4j.core.container.Container;
import cn.createsequence.crane4j.core.executor.handler.ReflectAssembleOperationHandler;
import cn.createsequence.crane4j.core.executor.handler.ReflectDisassembleOperationHandler;
import cn.createsequence.crane4j.core.parser.AnnotationAwareBeanOperationParser;
import cn.createsequence.crane4j.core.support.reflect.AsmReflectPropertyOperator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * test for {@link SimpleCrane4jGlobalConfiguration}
 *
 * @author huangchengxing
 */
public class SimpleCrane4jGlobalConfigurationTest {

    private final SimpleCrane4jGlobalConfiguration configuration = new SimpleCrane4jGlobalConfiguration();

    @Before
    public void init() {
        configuration.getContainerMap().put("test", Container.empty());
        configuration.setTypeResolver(new SimpleTypeResolver());
        configuration.getBeanOperationParserMap().put(
            AnnotationAwareBeanOperationParser.class,
            new AnnotationAwareBeanOperationParser(new SimpleAnnotationFinder(), configuration)
        );
        configuration.getAssembleOperationHandlerMap().put(
            ReflectAssembleOperationHandler.class,
            new ReflectAssembleOperationHandler(new AsmReflectPropertyOperator())
        );
        configuration.getDisassembleOperationHandlerMap().put(
            ReflectDisassembleOperationHandler.class,
            new ReflectDisassembleOperationHandler(new AsmReflectPropertyOperator())
        );
    }

    @Test
    public void getTypeResolver() {
        Assert.assertNotNull(configuration.getTypeResolver());
    }

    @Test
    public void getContainer() {
        Assert.assertNotNull(configuration.getContainer("test"));
    }

    @Test
    public void getBeanOperationsParser() {
        Assert.assertNotNull(configuration.getBeanOperationsParser(AnnotationAwareBeanOperationParser.class));
    }

    @Test
    public void getAssembleOperationHandler() {
        Assert.assertNotNull(configuration.getAssembleOperationHandler(ReflectAssembleOperationHandler.class));
    }

    @Test
    public void getDisassembleOperationHandler() {
        Assert.assertNotNull(configuration.getDisassembleOperationHandler(ReflectDisassembleOperationHandler.class));
    }
}
