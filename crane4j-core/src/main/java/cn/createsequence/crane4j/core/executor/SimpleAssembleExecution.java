package cn.createsequence.crane4j.core.executor;

import cn.createsequence.crane4j.core.parser.AssembleOperation;
import cn.createsequence.crane4j.core.parser.BeanOperations;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

/**
 * {@link AssembleExecution}的简单实现
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
