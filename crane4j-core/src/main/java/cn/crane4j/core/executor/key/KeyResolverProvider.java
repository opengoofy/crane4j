package cn.crane4j.core.executor.key;

import cn.crane4j.core.parser.operation.AssembleOperation;

/**
 * <p>Key resolver provider
 *
 * @author huangchengxing
 * @since 2.7.0
 */
public interface KeyResolverProvider {

    /**
     * Get the resolver of the operation.
     *
     * @param operation operation
     * @return resolver
     */
    KeyResolver getResolver(AssembleOperation operation);
}
