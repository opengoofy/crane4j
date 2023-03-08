package cn.crane4j.core.parser;

import cn.crane4j.core.exception.OperationParseException;
import cn.crane4j.core.executor.BeanOperationExecutor;

/**
 * The configuration parser of {@link BeanOperations},
 * it used to obtain all assembly and handling configurations
 * for a specific type according to the type.
 *
 * @author huangchengxing
 * @see AnnotationAwareBeanOperationParser
 * @see BeanOperationExecutor
 */
public interface BeanOperationParser {

    /**
     * Parse the class and class attribute information, and generate the corresponding {@link BeanOperations} instance.
     *
     * @param beanType bean type
     * @return {@link BeanOperations}
     * @throws OperationParseException thrown when configuration resolution exception
     */
    BeanOperations parse(Class<?> beanType) throws OperationParseException;
}
