package cn.crane4j.core.support.operator;

import cn.crane4j.annotation.ContainerParam;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ImmutableMapContainer;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.DataProvider;
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
import java.util.LinkedHashMap;
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
    private final Map<Class<?>, ContainerParameterAdaptorProvider> adaptorProviders;

    /**
     * Create a {@link DynamicContainerOperatorProxyMethodFactory} comparator.
     *
     * @param converterManager    converter manager
     * @param parameterNameFinder parameter name finder
     * @param annotationFinder    annotation finder
     */
    public DynamicContainerOperatorProxyMethodFactory(
        ConverterManager converterManager, ParameterNameFinder parameterNameFinder, AnnotationFinder annotationFinder) {
        this.converterManager = converterManager;
        this.parameterNameFinder = parameterNameFinder;
        this.annotationFinder = annotationFinder;
        this.adaptorProviders = new LinkedHashMap<>();
        initAdaptorProvider();
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

    @SuppressWarnings("unchecked")
    private void initAdaptorProvider() {
        // adapt for map
        adaptorProviders.put(Map.class, (n, p) ->
            arg -> ImmutableMapContainer.forMap(n, (Map<Object, ?>) arg)
        );
        // adapt for container
        adaptorProviders.put(Container.class, (n, p) ->
            arg -> (Container<Object>) arg
        );
        // adapt for data provider
        adaptorProviders.put(DataProvider.class, (n, p) ->
            arg -> LambdaContainer.forLambda(n, (DataProvider<Object, Object>) arg)
        );
    }

    @Nullable
    private Function<Object, Container<Object>> findAdaptor(
        String namespace, String parameterName, Parameter parameter, Method method) {
        Class<?> parameterType = parameter.getType();
        ContainerParameterAdaptorProvider provider = adaptorProviders.get(parameterType);
        if (Objects.nonNull(provider)) {
            return provider.getAdaptor(namespace, parameter);
        }
        Optional<ContainerParameterAdaptorProvider> optional = adaptorProviders.entrySet().stream()
            .filter(e -> e.getKey().isAssignableFrom(parameterType))
            .findFirst()
            .map(Map.Entry::getValue);
        if (!optional.isPresent()) {
            log.warn("cannot find adaptor provider for type [{}] of param [{}] in method [{}]", parameterType, parameterName, method);
            return null;
        }
        return optional.get().getAdaptor(namespace, parameter);
    }

    /**
     * Add adaptor provider for specific type.
     *
     * @param type            type
     * @param adaptorProvider adaptor provider
     */
    public void addAdaptorProvider(
        Class<?> type, ContainerParameterAdaptorProvider adaptorProvider) {
        Objects.requireNonNull(adaptorProvider, "adaptorProvider name must not null");
        adaptorProviders.put(type, adaptorProvider);
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
        Function<Object, Container<Object>>[] adaptors = resolveContainerParameterAdaptors(method, parameterNameMap);
        if (Arrays.stream(adaptors).allMatch(Objects::isNull)) {
            return null;
        }
        MethodInvoker invoker = new DynamicContainerMethodInvoker(beanOperations, beanOperationExecutor, adaptors);
        return ParameterConvertibleMethodInvoker.create(invoker, converterManager, method.getParameterTypes());
    }

    @NonNull
    private Function<Object, Container<Object>>[] resolveContainerParameterAdaptors(
        Method method, Map<String, Parameter> parameterNameMap) {
        // resolve parameter adaptors
        @SuppressWarnings("unchecked")
        Function<Object, Container<Object>>[] adaptors = new Function[parameterNameMap.size()];
        AtomicInteger index = new AtomicInteger(0);
        parameterNameMap.forEach((name, param) -> {
            int curr = index.getAndIncrement();
            // first argument must is target which need operate
            if (curr == 0) {
                return;
            }
            // adapt as container
            String namespace = Optional.ofNullable(annotationFinder.getAnnotation(param, ContainerParam.class))
                .map(ContainerParam::value)
                .orElse(name);
            // this parameter may not need to be adapted to a container when provider return null
            adaptors[curr] = findAdaptor(namespace, name, param, method);
        });
        return adaptors;
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
        private final Function<Object, Container<Object>>[] adaptors;

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
                .mapToObj(i -> adaptors[i].apply(args[i]))
                .collect(Collectors.toMap(Container::getNamespace, Function.identity()));
            beanOperationExecutor.execute(
                targets, operations, new BeanOperationExecutor.Options.DynamicContainerOption(Grouped.alwaysMatch(), temporaryContainers)
            );
            return bean;
        }
    }

    /**
     * Provider of container parameter adaptor.
     *
     * @author huangchengxing
     */
    @FunctionalInterface
    public interface ContainerParameterAdaptorProvider {

        /**
         * Get container parameter adaptor by given namespace and parameter.
         *
         * @param namespace namespace of container
         * @param parameter method parameter
         * @return functional interface for adapting argument to container comparator
         */
        @Nullable
        Function<Object, Container<Object>> getAdaptor(String namespace, Parameter parameter);
    }
}
