package cn.crane4j.core.support.operator;

import cn.crane4j.annotation.ContainerParam;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.ContainerAdapterRegister;
import cn.crane4j.core.support.Grouped;
import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.ParameterNameFinder;
import cn.crane4j.core.support.container.DefaultMethodContainerFactory;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.converter.ParameterConvertibleMethodInvoker;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ReflectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An operator proxy method factory that
 * supports dynamic setting container before operations executed.
 *
 * @author huangchengxing
 * @see ContainerParam
 * @see BeanOperationExecutor.Options
 * @since 2.0.0
 */
@Slf4j
public class DynamicContainerOperatorProxyMethodFactory implements OperatorProxyMethodFactory {

    public static final int ORDER = DefaultMethodContainerFactory.ORDER - 1;
    private final ConverterManager converterManager;
    private final ParameterNameFinder parameterNameFinder;
    private final AnnotationFinder annotationFinder;
    private final ContainerAdapterRegister containerAdapterRegister;

    /**
     * Create a {@link DynamicContainerOperatorProxyMethodFactory} instance.
     *
     *
     * @param converterManager    converter manager
     * @param parameterNameFinder parameter name finder
     * @param annotationFinder    annotation finder
     * @param containerAdapterRegister container adapter register
     */
    public DynamicContainerOperatorProxyMethodFactory(
        ConverterManager converterManager, ParameterNameFinder parameterNameFinder,
        AnnotationFinder annotationFinder, ContainerAdapterRegister containerAdapterRegister) {
        this.converterManager = converterManager;
        this.parameterNameFinder = parameterNameFinder;
        this.annotationFinder = annotationFinder;
        this.containerAdapterRegister = containerAdapterRegister;
    }

    /**
     * <p>Gets the sorting value.<br />
     * The smaller the value, the higher the priority of the object.
     *
     * @return sorting value
     */
    @Override
    public int getSort() {
        return ORDER;
    }

    /**
     * Get operator proxy method.
     *
     * @param beanOperations        bean operations
     * @param method                with at least one parameter
     * @param beanOperationExecutor bean operation executor
     * @return operator proxy method if supported, null otherwise
     */
    @Nullable
    @Override
    public MethodInvoker get(BeanOperations beanOperations, Method method, BeanOperationExecutor beanOperationExecutor) {
        Map<String, Parameter> parameterNameMap = ReflectUtils.resolveParameterNames(parameterNameFinder, method);
        // not any other argument need adapt as container
        if (parameterNameMap.size() == 1) {
            return null;
        }
        // has other argument but no adapters were found
        ContainerParameterAdapter[] adaptors = resolveContainerParameterAdaptors(method, parameterNameMap);
        if (Arrays.stream(adaptors).allMatch(Objects::isNull)) {
            return null;
        }
        MethodInvoker invoker = new DynamicContainerMethodInvoker(beanOperations, beanOperationExecutor, adaptors);
        return ParameterConvertibleMethodInvoker.create(invoker, converterManager, method.getParameterTypes());
    }

    @NonNull
    private ContainerParameterAdapter[] resolveContainerParameterAdaptors(
        Method method, Map<String, Parameter> parameterNameMap) {
        // resolve parameter adaptors
        ContainerParameterAdapter[] adaptors = new ContainerParameterAdapter[parameterNameMap.size()];
        AtomicInteger index = new AtomicInteger(0);
        parameterNameMap.forEach((n, p) -> {
            int curr = index.getAndIncrement();
            // first argument must is target which need operate
            if (curr == 0) {
                return;
            }
            // adapt as container
            String namespace = Optional.ofNullable(annotationFinder.getAnnotation(p, ContainerParam.class))
                .map(ContainerParam::value)
                .orElse(n);
            // this parameter may not need to be adapted to a container when provider return null
            adaptors[curr] = findAdaptor(namespace, n, p, method);
        });
        return adaptors;
    }

    @Nullable
    private ContainerParameterAdapter findAdaptor(
        String namespace, String parameterName, Parameter parameter, Method method) {
        Class<?> parameterType = parameter.getType();
        ContainerAdapterRegister.Adapter adapter = containerAdapterRegister.getAdapter(parameterType);
        if (Objects.isNull(adapter)) {
            log.warn("cannot find adaptor provider for type [{}] of param [{}] in method [{}]", parameterType, parameterName, method);
            return null;
        }
        return new ContainerParameterAdapter(namespace, adapter);
    }

    /**
     * Dynamic container method invoker.
     *
     * @author huangchengxing
     */
    @RequiredArgsConstructor
    protected static class DynamicContainerMethodInvoker implements MethodInvoker {

        private final BeanOperations operations;
        private final BeanOperationExecutor beanOperationExecutor;
        private final ContainerParameterAdapter[] adaptors;

        /**
         * Invoke method.
         *
         * @param target target
         * @param args   args
         * @return result of invoke
         */
        @Override
        public Object invoke(Object target, Object... args) {
            Object bean = args[0];
            Collection<?> targets = CollectionUtils.adaptObjectToCollection(bean);
            if (targets.isEmpty()) {
                return bean;
            }
            // not other argument need to adapt
            if (args.length == 1) {
                beanOperationExecutor.execute(targets, operations);
                return bean;
            }
            // has any temporary containers
            Map<String, Container<Object>> temporaryContainers = IntStream.rangeClosed(0, args.length - 1)
                .filter(i -> Objects.nonNull(args[i]) && Objects.nonNull(adaptors[i]))
                .mapToObj(i -> adaptors[i].wrap(args[i]))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Container::getNamespace, Function.identity()));
            beanOperationExecutor.execute(
                targets, operations, new BeanOperationExecutor.Options.DynamicContainerOption(Grouped.alwaysMatch(), temporaryContainers)
            );
            return bean;
        }
    }

    /**
     * Adapter for adapt invoke argument to container.
     *
     * @author huangchengxing
     * @since 2.2.0
     */
    @RequiredArgsConstructor
    protected static class ContainerParameterAdapter {
        private final String namespace;
        private final ContainerAdapterRegister.Adapter adapter;
        public Container<Object> wrap(Object target) {
            return adapter.wrapIfPossible(namespace, target);
        }
    }
}
