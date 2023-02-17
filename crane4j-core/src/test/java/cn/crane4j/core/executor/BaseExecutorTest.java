package cn.crane4j.core.executor;

import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectDisassembleOperationHandler;
import cn.crane4j.core.parser.AnnotationAwareBeanOperationParser;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.support.reflect.ReflectPropertyOperator;
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
        configuration.getBeanOperationParserMap().put(parser.getClass().getName(), parser);
        configuration.getBeanOperationParserMap().put(BeanOperationParser.class.getName(), parser);

        ReflectAssembleOperationHandler assembleOperationHandler = new ReflectAssembleOperationHandler(new ReflectPropertyOperator());
        configuration.getAssembleOperationHandlerMap().put(assembleOperationHandler.getClass().getName(), assembleOperationHandler);
        configuration.getAssembleOperationHandlerMap().put(AssembleOperationHandler.class.getName(), assembleOperationHandler);

        ReflectDisassembleOperationHandler disassembleOperationHandler = new ReflectDisassembleOperationHandler(new ReflectPropertyOperator());
        configuration.getDisassembleOperationHandlerMap().put(disassembleOperationHandler.getClass().getName(), disassembleOperationHandler);
        configuration.getDisassembleOperationHandlerMap().put(DisassembleOperationHandler.class.getName(), disassembleOperationHandler);
    }

    protected BeanOperations parseOperations(Class<?> type) {
        return parser.parse(type);
    }
}
