package cn.crane4j.core.executor.key;

import cn.crane4j.core.parser.operation.AssembleOperation;

/**
 * <p>Key resolver, which is used to resolve the key of the operation.
 *
 * @author huangchengxing
 * @since 2.7.0
 */
public interface KeyResolver extends KeyResolverProvider {

    /**
     * Get the resolver of the operation.
     *
     * @param operation operation
     * @return resolver
     */
    @Override
    default KeyResolver getResolver(AssembleOperation operation) {
        return this;
    }

    /**
     * Resolve the key of the operation.
     *
     * @param target    target
     * @param operation operation
     * @return key
     */
    Object resolve(Object target, AssembleOperation operation);
}
