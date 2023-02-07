package cn.createsequence.crane4j.springboot.support;

import cn.createsequence.crane4j.core.executor.BeanOperationExecutor;
import cn.createsequence.crane4j.core.parser.BeanOperationParser;
import cn.createsequence.crane4j.core.parser.BeanOperations;
import cn.createsequence.crane4j.core.parser.KeyTriggerOperation;
import cn.createsequence.crane4j.core.support.Grouped;
import cn.createsequence.crane4j.core.support.TypeResolver;
import cn.createsequence.crane4j.core.util.CollectionUtils;
import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 参照Spring提供的XXXTemplate，用于简化填充操作
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class OperateTemplate {

    /**
     * 默认的配置解析器，当未指定解析器时将使用该解析器
     */
    private final BeanOperationParser defaultParser;

    /**
     * 默认的操作执行器，当未指定执行器时将使用该执行器
     */
    private final BeanOperationExecutor defaultExecutor;

    /**
     * 类型解析器，未指定待操作对象类型时通过解析器获得类型
     */
    private final TypeResolver typeResolver;

    /**
     * 若操作属于{@code groups}中的任意分组，则执行操作
     *
     * @param target 待处理的对象
     * @param groups 组别
     * @see Grouped#anyMatch
     */
    public void executeIfMatchAnyGroups(Object target, String... groups) {
        execute(target, Grouped.anyMatch(groups));
    }

    /**
     * 若操作不属于{@code groups}中的任意分组，则执行操作
     *
     * @param target 待处理的对象
     * @param groups 组别
     * @see Grouped#anyMatch 
     */
    public void executeIfNoneMatchAnyGroups(Object target, String... groups) {
        execute(target, Grouped.anyMatch(groups).negate());
    }

    /**
     * 若操作同时属于{@code groups}中的所有分组，则执行操作
     *
     * @param target 待处理的对象
     * @param groups 组别
     * @see Grouped#allMatch 
     */
    public void executeIfMatchAllGroups(Object target, String... groups) {
        execute(target, Grouped.allMatch(groups));
    }

    /**
     * 执行操作
     *
     * @param target 待处理的对象
     */
    public void execute(Object target) {
        execute(
            CollectionUtils.adaptObjectToCollection(target),
            resolveType(target), defaultParser, defaultExecutor, Grouped.alwaysMatch()
        );
    }

    /**
     * 执行操作
     *
     * @param target 待处理的对象
     * @param filter 操作过滤器
     */
    public void execute(Object target, Predicate<? super KeyTriggerOperation> filter) {
        execute(
            CollectionUtils.adaptObjectToCollection(target),
            resolveType(target), defaultParser, defaultExecutor, filter
        );
    }

    /**
     * 执行操作
     *
     * @param target 待处理的对象
     * @param executor 操作执行器
     * @param filter 操作过滤器
     */
    public void execute(
        Object target, BeanOperationExecutor executor, Predicate<? super KeyTriggerOperation> filter) {
        execute(
            CollectionUtils.adaptObjectToCollection(target),
            resolveType(target), defaultParser, executor, filter
        );
    }

    /**
     * 执行操作
     *
     * @param targets 待处理的对象
     * @param targetType 待处理的对象类型
     * @param parser 配置解析器
     * @param executor 操作执行器
     * @param filter 操作过滤器
     * @param <T> 对象类型
     */
    public <T> void execute(
        Collection<T> targets, Class<T> targetType, BeanOperationParser parser,
        BeanOperationExecutor executor, Predicate<? super KeyTriggerOperation> filter) {
        if (CollUtil.isEmpty(targets)) {
            return;
        }
        Objects.requireNonNull(targetType);
        Objects.requireNonNull(parser);
        Objects.requireNonNull(executor);
        BeanOperations beanOperations = parser.parse(targetType);
        executor.execute(targets, beanOperations, filter);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> resolveType(Object target) {
        Class<?> type = typeResolver.resolve(target);
        Objects.requireNonNull(type, "cannot resolve type for targets");
        return (Class<T>)type;
    }
}
