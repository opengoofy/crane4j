package cn.crane4j.core.executor;

import cn.crane4j.core.parser.AssembleOperation;
import cn.crane4j.core.parser.BeanOperations;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

/**
 * Simple implementation of {@link AssembleExecution}.
 *
 * @author huangchengxing
 */
@Getter
@RequiredArgsConstructor
public class SimpleAssembleExecution implements AssembleExecution {
    private final BeanOperations beanOperations;
    private final AssembleOperation operation;
    private final Collection<Object> targets;
}
