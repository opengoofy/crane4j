package cn.createsequence.crane4j.core.parser;

import cn.createsequence.crane4j.core.executor.BeanOperationExecutor;

/**
 * {@link BeanOperations}的配置解析器，用于根据类型获得针对特定类型的全部装配以及装卸配置。
 *
 * @author huangchengxing
 * @see AnnotationAwareBeanOperationParser
 * @see BeanOperationExecutor
 */
public interface BeanOperationParser {

    /**
     * 解析类及类属性信息，生成对应的{@link BeanOperations}实例
     *
     * @param beanType 类
     * @return {@link BeanOperations}实例，该实例可能存在缓存
     */
    BeanOperations parse(Class<?> beanType);

}
