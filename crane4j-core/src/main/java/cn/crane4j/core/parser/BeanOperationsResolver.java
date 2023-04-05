package cn.crane4j.core.parser;

import cn.crane4j.core.support.Sorted;

/**
 * Class operation resolver, belonging to {@link TypeHierarchyBeanOperationParser},
 * used to obtain all operation configurations in a single parsing of a class.
 *
 * @author huangchengxing
 * @since 1.2.0
 */
public interface BeanOperationsResolver extends Sorted {

    /**
     * Resolve operations from type
     *
     * @param context context
     * @param type    type
     */
    void resolve(OperationParseContext context, Class<?> type);
}
