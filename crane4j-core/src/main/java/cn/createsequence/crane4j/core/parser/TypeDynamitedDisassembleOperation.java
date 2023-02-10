package cn.createsequence.crane4j.core.parser;

import cn.createsequence.crane4j.core.exception.CraneException;
import cn.createsequence.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.createsequence.crane4j.core.support.TypeResolver;
import cn.hutool.core.lang.Assert;
import lombok.Getter;
import lombok.NonNull;

/**
 * {@link DisassembleOperation}的简单实现，用于不确定类型的嵌套对象拆卸配置，比如需要拆卸的字段类型为泛型
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
     * 获取嵌套对象的操作配置，使用{@link #typeResolver}动态的推断类型
     *
     * @param internalBean 带解析的嵌套对象
     * @return 嵌套对象的操作配置
     */
    @NonNull
    @Override
    public BeanOperations getInternalBeanOperations(Object internalBean) {
        Class<?> internalType = typeResolver.resolve(internalBean);
        Assert.notNull(internalType, () -> new CraneException("cannot resolve type for object: [{}]", internalBean));
        return beanOperationParser.parse(internalType);
    }
}
