package cn.crane4j.core.executor.handler;

import cn.crane4j.core.parser.operation.DisassembleOperation;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.util.Asserts;
import cn.crane4j.core.util.CollectionUtils;
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
        if (CollectionUtils.isEmpty(targets)) {
            return Collections.emptyList();
        }
        MethodInvoker getter = propertyOperator.findGetter(operation.getSourceType(), operation.getKey());
        Asserts.isNotNull(getter, "cannot find getter for [{}] on [{}]", operation.getKey(), operation.getSourceType());
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
