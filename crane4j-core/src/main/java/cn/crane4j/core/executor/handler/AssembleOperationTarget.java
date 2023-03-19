package cn.crane4j.core.executor.handler;

import cn.crane4j.core.executor.AssembleExecution;
import lombok.Getter;

/**
 * basic implementation of {@link AbstractAssembleOperationHandler.Target}
 *
 * @author huangchengxing
 */
@Getter
public class AssembleOperationTarget extends AbstractAssembleOperationHandler.Target {

    /**
     * value of key property
     */
    private final Object key;

    /**
     * Create a {@link AssembleOperationTarget} instance.
     *
     * @param execution execution
     * @param target target
     * @param key value of key property
     */
    public AssembleOperationTarget(AssembleExecution execution, Object target, Object key) {
        super(execution, target);
        this.key = key;
    }
}
