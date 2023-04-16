package cn.crane4j.core.parser;

import cn.crane4j.core.exception.OperationParseException;
import cn.crane4j.core.executor.BeanOperationExecutor;

import java.lang.reflect.AnnotatedElement;

/**
 * <p>The configuration parser of {@link BeanOperations},
 * it used to obtain all assembly and handling configurations
 * for a specific type according to the {@link AnnotatedElement}.
 *
 * @author huangchengxing
 * @see TypeHierarchyBeanOperationParser
 * @see BeanOperationExecutor
 */
public interface BeanOperationParser {

    /**
     * Parse the {@link AnnotatedElement} annotation information,
     * and generate the corresponding {@link BeanOperations} instance.
     *
     * @param element element to parse
     * @return {@link BeanOperations}
     * @throws OperationParseException thrown when configuration resolution exception
     */
    BeanOperations parse(AnnotatedElement element) throws OperationParseException;
}
