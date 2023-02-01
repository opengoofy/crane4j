package cn.createsequence.crane4j.core.container;

import cn.createsequence.crane4j.core.annotation.MappingType;
import cn.createsequence.crane4j.core.support.MethodInvoker;
import cn.createsequence.crane4j.core.util.CollectionUtils;
import cn.hutool.core.lang.Assert;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>方法数据源容器，指定任意无参或首个参数为{@link Collection}类型的实例或静态方法，
 * 将其适配为一个数据源容器。<br />
 * 该容器一般并不直接由用户创建，而是配合{@link MethodContainerFactory}用于在框架中根据注解或特定配置，
 * 大批量扫描不确定数量的特定上下文对象中的方法，将其自动适配为数据源容器并注册。<br />
 * 若仅有少量已知的方法需要作为数据源，可以直接使用{@link LambdaContainer}。
 *
 * @author huangchengxing
 * @see MethodContainerFactory
 * @see MappingType
 */
public class MethodInvokerContainer implements Container<Object> {

    @Getter
    private final String namespace;
    private final MethodInvoker methodInvoker;
    private final Object methodSource;
    private final KeyExtractor keyExtractor;
    private final MappingType mappingType;

    /**
     * 构建一个方法数据源容器
     *
     * @param namespace 命名空间
     * @param methodInvoker 要调用的方法
     * @param methodSource 方法调用对象，若要调用的是静态方法，则可以为空
     * @param keyExtractor 数据源对象的key值提取方法，若{@code mappingType}为{@code MAPPED}则可以为空
     * @param mappingType 返回值类型
     */
    public MethodInvokerContainer(
        String namespace,
        MethodInvoker methodInvoker, Object methodSource,
        KeyExtractor keyExtractor, MappingType mappingType) {
        this.namespace = Objects.requireNonNull(namespace);
        this.methodInvoker = Objects.requireNonNull(methodInvoker);
        this.methodSource = methodSource;

        // 若返回值不为Map，则key提取器为必填项
        this.keyExtractor = keyExtractor;
        this.mappingType = Objects.requireNonNull(mappingType);
        Assert.isTrue(
            mappingType == MappingType.MAPPED || Objects.nonNull(keyExtractor),
            "keyExtractor must not null"
        );
    }

    /**
     * 输入一批key值，返回按key值分组的数据源对象
     *
     * @param keys keys
     * @return 按key值分组的数据源对象
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<Object, ?> get(Collection<Object> keys) {
        Object invokeResult = methodInvoker.invoke(methodSource, keys);
        if (Objects.isNull(invokeResult)) {
            return Collections.emptyMap();
        }
        if (mappingType == MappingType.MAPPED) {
            return (Map<Object, ?>)invokeResult;
        }
        // 按类型对返回值进行分组
        Collection<?> invokeResults = CollectionUtils.adaptObjectToCollection(invokeResult);
        // 一对一
        if (mappingType == MappingType.ONE_TO_ONE) {
            return invokeResults.stream().collect(Collectors.toMap(keyExtractor::getKey, Function.identity()));
        }
        // 一对多
        return invokeResults.stream().collect(Collectors.groupingBy(keyExtractor::getKey));
    }

    /**
     * key值提取器，用于从数据源对象获取key值
     */
    @FunctionalInterface
    interface KeyExtractor {

        /**
         * 获取Key值
         *
         * @param source 数据源对象
         * @return key值
         */
        Object getKey(Object source);
    }

}
