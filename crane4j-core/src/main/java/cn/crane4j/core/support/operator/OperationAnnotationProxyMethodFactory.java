package cn.crane4j.core.support.operator;

import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.converter.ParameterConvertibleMethodInvoker;
import cn.crane4j.core.util.ArrayUtils;
import cn.crane4j.core.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * <p>A proxy method factory that supports filling parameters
 * according to operation annotation which is annotated on method.
 * <p>for example:
 * <pre>{@code
 * @Assemble(
 *  container = "dept", key = "deptId",
 *  props = @Mapping(ref = "deptName")
 * )
 * @Assemble(
 *  container = "user", key = "id",
 *  props = @Mapping(ref = "name")
 * )
 * void operateMethod(Foo foo);
 * }</pre>
 *
 * @author huangchengxing
 * @since  1.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class OperationAnnotationProxyMethodFactory implements OperatorProxyMethodFactory {

    private final ConverterManager converterManager;

    /**
     * <p>Gets the sorting value.<br />
     * The smaller the value, the higher the priority of the object.
     *
     * @return sorting value
     */
    @Override
    public int getSort() {
        return OPERATION_ANNOTATION_PROXY_METHOD_FACTORY_ORDER;
    }

    /**
     * Get operator proxy method.
     *
     * @param beanOperations        bean operations
     * @param method with at least one parameter
     * @param beanOperationExecutor bean operation executor
     * @return operator proxy method if supported, null otherwise
     */
    @Nullable
    @Override
    public MethodInvoker get(BeanOperations beanOperations, Method method, BeanOperationExecutor beanOperationExecutor) {
        if (beanOperations.isEmpty()) {
            return null;
        }
        MethodInvoker invoker = new ProxyMethod(beanOperations, beanOperationExecutor);
        log.info("create operation annotation proxy method for method: {}", method);
        return ParameterConvertibleMethodInvoker.create(invoker, converterManager, method.getParameterTypes());
    }

    /**
     * Standard operator method.
     *
     * @author huangchengxing
     */
    @RequiredArgsConstructor
    private static class ProxyMethod implements MethodInvoker {

        private final BeanOperations operations;
        private final BeanOperationExecutor beanOperationExecutor;

        @Override
        public Object invoke(Object target, Object... args) {
            if (ArrayUtils.isNotEmpty(args)) {
                for (Object arg : args) {
                    if (Objects.nonNull(arg)) {
                        beanOperationExecutor.execute(CollectionUtils.adaptObjectToCollection(arg), operations);
                    }
                }
            }
            return null;
        }
    }
}
