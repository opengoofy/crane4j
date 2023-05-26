package cn.crane4j.core.parser.operation;

import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.TypeResolver;
import cn.crane4j.core.util.Asserts;
import lombok.Getter;
import lombok.NonNull;

/**
 * <p>The {@link DisassembleOperation} implementation that
 * express the disassemble operation of data from dynamic type.<br />
 * It is usually used to process the disassemble operation for unknown types or generic types.
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
