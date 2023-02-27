package cn.crane4j.core.executor.handler;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.parser.DisassembleOperation;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A basic {@link DisassembleOperationHandler} implementation based on {@link PropertyOperator}.
 *
 * @author huangchengxing
 */
@RequiredArgsConstructor
public class ReflectDisassembleOperationHandler implements DisassembleOperationHandler {

    private final PropertyOperator propertyOperator;

    /**
     * Extract nested objects in object attributes according to disassembly configuration.
     *
     * @param operation disassembly operation to be performed
     * @param targets The target object to be processed should be the same type as {@code targetType}
     * @return nested objects
     */
    @Override
    public Collection<Object> process(DisassembleOperation operation, Collection<?> targets) {
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
            // still is collection, continue
            if (item instanceof Collection || item.getClass().isArray()) {
                deque.addAll(CollectionUtils.adaptObjectToCollection(item));
            } else {
                result.add(item);
            }
        }
        return result;
    }
}
