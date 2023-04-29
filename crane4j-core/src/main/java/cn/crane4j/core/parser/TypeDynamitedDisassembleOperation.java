package cn.crane4j.core.parser;

import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.support.TypeResolver;
import cn.crane4j.core.util.Asserts;
import lombok.Getter;
import lombok.NonNull;

/**
 * <p>The simple implementation of {@link DisassembleOperation}.<br />
 * It's used for the disassembly configuration of nested objects
 * of uncertain types, such as the field type to be disassembled is generic.
 *
 * @author huangchengxing
 * @see TypeResolver
 */
public class TypeDynamitedDisassembleOperation extends SimpleKeyTriggerOperation implements DisassembleOperation {

    @Getter
    private final Class<?> sourceType;
    @Getter
    private final DisassembleOperationHandler disassembleOperationHandler;
    private final BeanOperationParser beanOperationParser;
    private final TypeResolver typeResolver;

    public TypeDynamitedDisassembleOperation(
        String key, int sort, Class<?> sourceType,
        DisassembleOperationHandler disassembleOperationHandler,
        BeanOperationParser beanOperationParser, TypeResolver typeResolver) {
        super(key, sort);
        this.sourceType = sourceType;
        this.disassembleOperationHandler = disassembleOperationHandler;
        this.beanOperationParser = beanOperationParser;
        this.typeResolver = typeResolver;
    }

    public TypeDynamitedDisassembleOperation(
        String key, Class<?> sourceType,
        DisassembleOperationHandler disassembleOperationHandler,
        BeanOperationParser beanOperationParser, TypeResolver typeResolver) {
        this(key, Integer.MAX_VALUE, sourceType, disassembleOperationHandler, beanOperationParser, typeResolver);
    }

    /**
     * <p>Get the operation configuration of the nested object,
     * and use {@link #typeResolver} to dynamically infer the type.
     *
     * @param internalBean internal bean
     * @return operation
     */
    @NonNull
    @Override
    public BeanOperations getInternalBeanOperations(Object internalBean) {
        Class<?> internalType = typeResolver.resolve(internalBean);
        Asserts.isNotNull(internalType, "cannot resolve type for object: [{}]", internalBean);
        return beanOperationParser.parse(internalType);
    }
}
