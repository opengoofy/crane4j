package cn.crane4j.core.util;

import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.callback.ContainerRegisterAware;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * ConfigurationUtil
 *
 * @author huangchengxing
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigurationUtil {

    public static void registerContainer(
        Object caller,
        Function<String, Container<?>> containerGetter, Consumer<Container<?>> containerSetter,
        Container<?> container, Collection<ContainerRegisterAware> awareList) {
        String namespace = container.getNamespace();
        // is container already registered?
        Container<?> old = containerGetter.apply(namespace);
        Assert.isNull(old, () -> new Crane4jException("the container [{}] has been registered", namespace));

        // invoke callback for container
        Container<?> actual = invokeBeforeContainerRegister(
            caller, container, awareList
        );
        if (Objects.nonNull(actual)) {
            containerSetter.accept(actual);
            ConfigurationUtil.invokeAfterContainerRegister(caller, container, awareList);
        }
    }

    @Nullable
    public static Container<?> invokeBeforeContainerRegister(
        Object caller, Container<?> container,
        Collection<ContainerRegisterAware> containerRegisterAwareCollection) {
        for (ContainerRegisterAware containerRegisterAware : containerRegisterAwareCollection) {
            if (Objects.isNull(container)) {
                return null;
            }
            container = containerRegisterAware.beforeContainerRegister(caller, container);
        }
        return container;
    }

    public static void invokeAfterContainerRegister(
        Object caller, @Nullable Container<?> container,
        Collection<ContainerRegisterAware> containerRegisterAwareList) {
        if (Objects.nonNull(container)) {
            containerRegisterAwareList.forEach(aware -> aware.afterContainerRegister(caller, container));
        }
    }

    public static ContainerProvider getContainerProvider(
        Crane4jGlobalConfiguration configuration, String name, Class<?> type) {
        return getPlugin(
            configuration, ContainerProvider.class,
            Crane4jGlobalConfiguration::getContainerProvider, type,
            Crane4jGlobalConfiguration::getContainerProvider, name
        );
    }

    public static BeanOperationExecutor getOperationExecutor(
        Crane4jGlobalConfiguration configuration, String name, Class<?> type) {
        return getPlugin(
            configuration, BeanOperationExecutor.class,
            Crane4jGlobalConfiguration::getBeanOperationExecutor, type,
            Crane4jGlobalConfiguration::getBeanOperationExecutor, name
        );
    }

    public static DisassembleOperationHandler getDisassembleOperationHandler(
        Crane4jGlobalConfiguration configuration, String name, Class<?> type) {
        return getPlugin(
            configuration, DisassembleOperationHandler.class,
            Crane4jGlobalConfiguration::getDisassembleOperationHandler, type,
            Crane4jGlobalConfiguration::getDisassembleOperationHandler, name
        );
    }

    public static AssembleOperationHandler getAssembleOperationHandler(
        Crane4jGlobalConfiguration configuration, String name, Class<?> type) {
        return getPlugin(
            configuration, AssembleOperationHandler.class,
            Crane4jGlobalConfiguration::getAssembleOperationHandler, type,
            Crane4jGlobalConfiguration::getAssembleOperationHandler, name
        );
    }

    public static BeanOperationParser getParser(
        Crane4jGlobalConfiguration configuration, String name, Class<?> type) {
        return getPlugin(
            configuration, BeanOperationParser.class,
            Crane4jGlobalConfiguration::getBeanOperationsParser, type,
            Crane4jGlobalConfiguration::getBeanOperationsParser, name
        );
    }

    @SuppressWarnings("unchecked")
    private static <T> T getPlugin(
        Crane4jGlobalConfiguration configuration, Class<T> pluginType,
        BiFunction<Crane4jGlobalConfiguration, Class<T>, T> getByType, Class<?> type,
        BiFunction<Crane4jGlobalConfiguration, String, T> getByName, String name) {
        // find by name
        if (CharSequenceUtil.isNotEmpty(name)) {
            return pluginType.cast(getByName.apply(configuration, name));
        }
        // find by type
        Class<?> targetType = (Objects.equals(Object.class, type) || !pluginType.isAssignableFrom(type)) ? pluginType : type;
        return pluginType.cast(getByType.apply(configuration, (Class<T>)targetType));
    }
}
