package cn.crane4j.core.executor.handler;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.parser.DisassembleOperation;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 基于{@link PropertyOperator}实现的拆卸操作处理器
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class ReflectDisassembleOperationHandler implements DisassembleOperationHandler {

    private final PropertyOperator propertyOperator;

    /**
     * 根据拆卸配置，提取出对象属性中的嵌套对象
     *
     * @param operation 要执行的拆卸操作
     * @param targets 待处理的目标对象，类型应当与{@code targetType}一致
     */
    @Override
    public Collection<?> process(DisassembleOperation operation, Collection<?> targets) {
        if (CollUtil.isEmpty(targets)) {
            return Collections.emptyList();
        }
        MethodInvoker getter = propertyOperator.findGetter(operation.getSourceType(), operation.getKey());
        Assert.notNull(getter, () -> new Crane4jException(
            "cannot find getter for [{}] on [{}]", operation.getKey(), operation.getSourceType()
        ));
        Deque<Object> deque = targets.stream()
            .filter(Objects::nonNull)
            .map(getter::invoke)
            .collect(Collectors.toCollection(LinkedList::new));

        List<Object> result = new ArrayList<>();
        while (!deque.isEmpty()) {
            Object item = deque.removeFirst();
            if (Objects.isNull(item)) {
                continue;
            }
            // 若依然是集合，则继续拆卸
            if (item instanceof Collection || item.getClass().isArray()) {
                deque.addAll(CollectionUtils.adaptObjectToCollection(item));
            } else {
                result.add(item);
            }
        }
        return result;
    }
}
