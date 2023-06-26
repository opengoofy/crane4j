package cn.crane4j.core.support;

import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.container.DefaultContainerManager;
import cn.crane4j.core.container.lifecycle.ContainerRegisterLogger;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.executor.OrderedBeanOperationExecutor;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.executor.handler.ManyToManyAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToManyAssembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToOneAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectiveDisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.TypeHierarchyBeanOperationParser;
import cn.crane4j.core.parser.handler.AssembleAnnotationHandler;
import cn.crane4j.core.parser.handler.AssembleEnumAnnotationHandler;
import cn.crane4j.core.parser.handler.DisassembleAnnotationHandler;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.converter.HutoolConverterManager;
import cn.crane4j.core.support.reflect.ChainAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.MapAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import cn.crane4j.core.util.Asserts;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic implementation of {@link Crane4jGlobalConfiguration}.
 *
 * @author huangchengxing
 */
@Getter
public class SimpleCrane4jGlobalConfiguration
    extends DefaultContainerManager implements Crane4jGlobalConfiguration {

    @Setter
    private TypeResolver typeResolver;
    @Setter
    private PropertyOperator propertyOperator;
    @Setter
    private ConverterManager converterManager;
    private final Map<String, BeanOperationParser> beanOperationParserMap = new HashMap<>(16);
    private final Map<String, AssembleOperationHandler> assembleOperationHandlerMap = new HashMap<>(4);
    private final Map<String, DisassembleOperationHandler> disassembleOperationHandlerMap = new HashMap<>(4);
    private final Map<String, BeanOperationExecutor> beanOperationExecutorMap = new HashMap<>(4);

    /**
     * Create a {@link SimpleCrane4jGlobalConfiguration} using the default configuration.
     *
     * @return configuration
     */
    public static SimpleCrane4jGlobalConfiguration create() {
        SimpleCrane4jGlobalConfiguration configuration = new SimpleCrane4jGlobalConfiguration();
        // basic components
        ConverterManager converter = new HutoolConverterManager();
        configuration.setConverterManager(converter);
        PropertyOperator operator = new ReflectivePropertyOperator(converter);
        operator = new MapAccessiblePropertyOperator(operator);
        operator = new ChainAccessiblePropertyOperator(operator);
        configuration.setPropertyOperator(operator);
        configuration.setTypeResolver(new SimpleTypeResolver());

        // container container lifecycle lifecycle
        Logger logger = LoggerFactory.getLogger(ContainerRegisterLogger.class);
        configuration.registerContainerLifecycleProcessor(new ContainerRegisterLogger(logger::info));

        // operation parser
        AnnotationFinder annotationFinder = new SimpleAnnotationFinder();
        BeanOperationParser beanOperationParser = new TypeHierarchyBeanOperationParser(Arrays.asList(
            new AssembleAnnotationHandler(annotationFinder, configuration),
            new DisassembleAnnotationHandler(annotationFinder, configuration),
            new AssembleEnumAnnotationHandler(annotationFinder, configuration, operator, configuration)
        ));
        configuration.getBeanOperationParserMap().put(BeanOperationParser.class.getSimpleName(), beanOperationParser);
        configuration.getBeanOperationParserMap().put(beanOperationParser.getClass().getSimpleName(), beanOperationParser);

        // operation executor
        DisorderedBeanOperationExecutor disorderedBeanOperationExecutor = new DisorderedBeanOperationExecutor(configuration);
        configuration.getBeanOperationExecutorMap().put(BeanOperationExecutor.class.getSimpleName(), disorderedBeanOperationExecutor);
        configuration.getBeanOperationExecutorMap().put(disorderedBeanOperationExecutor.getClass().getSimpleName(), disorderedBeanOperationExecutor);
        OrderedBeanOperationExecutor orderedBeanOperationExecutor = new OrderedBeanOperationExecutor(configuration, Crane4jGlobalSorter.instance());
        configuration.getBeanOperationExecutorMap().put(orderedBeanOperationExecutor.getClass().getSimpleName(), orderedBeanOperationExecutor);

        // operation handlers
        OneToOneAssembleOperationHandler oneToOneReflexAssembleOperationHandler = new OneToOneAssembleOperationHandler(operator);
        configuration.getAssembleOperationHandlerMap().put(AssembleOperationHandler.class.getSimpleName(), oneToOneReflexAssembleOperationHandler);
        configuration.getAssembleOperationHandlerMap().put(oneToOneReflexAssembleOperationHandler.getClass().getSimpleName(), oneToOneReflexAssembleOperationHandler);
        OneToManyAssembleOperationHandler oneToManyReflexAssembleOperationHandler = new OneToManyAssembleOperationHandler(operator);
        configuration.getAssembleOperationHandlerMap().put(oneToManyReflexAssembleOperationHandler.getClass().getSimpleName(), oneToManyReflexAssembleOperationHandler);
        ManyToManyAssembleOperationHandler manyToManyReflexAssembleOperationHandler = new ManyToManyAssembleOperationHandler(operator);
        configuration.getAssembleOperationHandlerMap().put(manyToManyReflexAssembleOperationHandler.getClass().getSimpleName(), manyToManyReflexAssembleOperationHandler);
        ReflectiveDisassembleOperationHandler reflectiveDisassembleOperationHandler = new ReflectiveDisassembleOperationHandler(operator);
        configuration.getDisassembleOperationHandlerMap().put(DisassembleOperationHandler.class.getSimpleName(), reflectiveDisassembleOperationHandler);
        configuration.getDisassembleOperationHandlerMap().put(reflectiveDisassembleOperationHandler.getClass().getSimpleName(), reflectiveDisassembleOperationHandler);

        // container providers
        configuration.registerContainerProvider(configuration.getClass().getSimpleName(), configuration);
        configuration.registerContainerProvider(ContainerProvider.class.getSimpleName(), configuration);

        return configuration;
    }

    /**
     * Get bean operation executor.
     *
     * @param executorName executor name
     * @return executor
     */
    @Override
    public BeanOperationExecutor getBeanOperationExecutor(String executorName) {
        BeanOperationExecutor executor = beanOperationExecutorMap.get(executorName);
        Asserts.isNotNull(executor, "cannot find executor [{}]", executorName);
        return executor;
    }

    /**
     * Get bean operation parser.
     *
     * @param parserName parser name
     * @return parser
     */
    @Override
    public BeanOperationParser getBeanOperationsParser(String parserName) {
        BeanOperationParser parser = beanOperationParserMap.get(parserName);
        Asserts.isNotNull(parser, "cannot find parser [{}]", parserName);
        return parser;
    }

    /**
     * Get assemble operation handler.
     *
     * @param  handlerName handler name
     * @return handler
     */
    @Override
    public AssembleOperationHandler getAssembleOperationHandler(String handlerName) {
        AssembleOperationHandler handler = assembleOperationHandlerMap.get(handlerName);
        Asserts.isNotNull(handler, "cannot find assemble handler [{}]", handlerName);
        return handler;
    }

    /**
     * Get disassemble operation handler.
     *
     * @param handlerName handler name
     * @return handler
     */
    @Override
    public DisassembleOperationHandler getDisassembleOperationHandler(String handlerName) {
        DisassembleOperationHandler handler = disassembleOperationHandlerMap.get(handlerName);
        Asserts.isNotNull(handler, "cannot find disassemble handler [{}]", handlerName);
        return handler;
    }
}
