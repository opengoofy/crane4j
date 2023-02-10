package cn.createsequence.crane4j.core.executor;

import cn.createsequence.crane4j.core.executor.handler.ReflectAssembleOperationHandler;
import cn.createsequence.crane4j.core.executor.handler.ReflectDisassembleOperationHandler;
import cn.createsequence.crane4j.core.parser.AnnotationAwareBeanOperationParser;
import cn.createsequence.crane4j.core.parser.BeanOperations;
import cn.createsequence.crane4j.core.support.SimpleAnnotationFinder;
import cn.createsequence.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.createsequence.crane4j.core.support.reflect.ReflectPropertyOperator;
import org.junit.Before;

/**
 * @author huangchengxing
 */
public class BaseExecutorTest {

    protected SimpleCrane4jGlobalConfiguration configuration;
    private AnnotationAwareBeanOperationParser parser;

    @Before
    public void initParser() {
        configuration = new SimpleCrane4jGlobalConfiguration();
        parser = new AnnotationAwareBeanOperationParser(new SimpleAnnotationFinder(), configuration);
        configuration.getBeanOperationParserMap().put(parser.getClass(), parser);
        configuration.getAssembleOperationHandlerMap().put(
            ReflectAssembleOperationHandler.class, new ReflectAssembleOperationHandler(new ReflectPropertyOperator())
        );
        configuration.getDisassembleOperationHandlerMap().put(
            ReflectDisassembleOperationHandler.class, new ReflectDisassembleOperationHandler(new ReflectPropertyOperator())
        );
    }

    protected BeanOperations parseOperations(Class<?> type) {
        return parser.parse(type);
    }
}
