package cn.createsequence.crane4j.core.container;

import cn.createsequence.crane4j.core.annotation.ContainerConstant;
import cn.createsequence.crane4j.core.annotation.ContainerEnum;
import cn.createsequence.crane4j.core.support.AnnotationFinder;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>简单的键值对常量数据源容器，用于支持常量/枚举类型的数据源，
 * 允许通过内置的{@code forXXX}方法基于特定场景快速构建数据源。<br />
 * 出于性能考虑，通过{@link #get}方法总是从容器中获取全量——而不是仅与输入的key对应——的数据。
 *
 * @author huangchengxing
 * @see ContainerEnum
 */
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ConstantContainer<K> implements Container<K> {

    /**
     * 命名空间
     */
    @Getter
    private final String namespace;

    /**
     * 按key值分组的数据源对象
     */
    private final Map<K, ?> data;

    /**
     * 根据指定类型枚举创建一个键值对容器，key值为{@code keyGetter}所获得的枚举属性值
     *
     * @param namespace 命名空间
     * @param enumType 枚举类型
     * @param keyGetter 从枚举实例获取对应key值的方法
     * @param <K> key类型
     * @param <T> 枚举类型
     * @return 数据源容器
     */
    @Nonnull
    public static <K, T extends Enum<?>> ConstantContainer<K> forEnum(
        String namespace, Class<T> enumType, Function<? super T, ? extends K> keyGetter) {
        Objects.requireNonNull(enumType);
        Objects.requireNonNull(keyGetter);
        Map<K, T> enumMap = Stream.of(enumType.getEnumConstants())
            .collect(Collectors.toMap(keyGetter, Function.identity()));
        return forMap(namespace, enumMap);
    }

    /**
     * 根据指定类型枚举创建一个键值对容器，key值与namespace皆通过对应的{@link ContainerEnum}注解获取
     *
     * @param enumType 枚举类型
     * @param annotationFinder 注解查找器
     * @param <K> key类型
     * @param <T> 枚举类型
     * @return 枚举容器, 若枚举类型不存在注解则返回空
     * @see ContainerEnum
     */
    @SuppressWarnings("unchecked")
    public static <K, T extends Enum<?>> ConstantContainer<K> forEnum(
        Class<T> enumType, AnnotationFinder annotationFinder) {
        Objects.requireNonNull(enumType);
        Objects.requireNonNull(annotationFinder);
        // 枚举未被注解
        ContainerEnum annotation = annotationFinder.findAnnotation(enumType, ContainerEnum.class);
        if (Objects.isNull(annotation)) {
            return (ConstantContainer<K>)forEnum(enumType.getSimpleName(), enumType, Enum::name);
        }

        // 若命名空间为空，则默认为类名称本身
        String namespace = CharSequenceUtil.emptyToDefault(annotation.namespace(), enumType.getSimpleName());
        // 若key字段为空，则默认为枚举名称
        Function<? super T, ? extends K> keyMapper = CharSequenceUtil.isEmpty(annotation.key()) ?
            e -> (K)e.name() : e ->(K)ReflectUtil.getFieldValue(e, annotation.key());
        // 若value字段为空，则默认为枚举项本身
        Function<? super T, ?> valueMapper = CharSequenceUtil.isEmpty(annotation.value()) ?
            Function.identity() : e -> (K)ReflectUtil.getFieldValue(e, annotation.value());
        Map<K, Object> data = Stream.of(enumType.getEnumConstants()).collect(Collectors.toMap(keyMapper, valueMapper));
        return forMap(namespace, data);
    }

    /**
     * 根据指定类型枚举创建一个键值对容器，key值为{@code keyGetter}所获得的枚举属性值
     *
     * @param namespace 命名空间
     * @param data 按key值分组的数据源对象
     * @param <K> key类型
     * @return 数据源容器
     */
    public static <K> ConstantContainer<K> forMap(String namespace, Map<K, ?> data) {
        Objects.requireNonNull(namespace);
        Assert.notEmpty(data, "data must not empty");
        return new ConstantContainer<>(namespace, data);
    }

    /**
     * 将指定的常量类中的公共静态常量转为容器
     *
     * @param constantClass 常量类
     * @return 数据源容器
     * @see ContainerConstant
     */
    public static ConstantContainer<String> forConstantClass(
        Class<?> constantClass, AnnotationFinder annotationFinder) {
        Objects.requireNonNull(constantClass);
        ContainerConstant annotation = annotationFinder.findAnnotation(constantClass, ContainerConstant.class);
        Assert.notNull(annotation, "cannot find @ContainerConstant from [{}]", constantClass);
        boolean onlyPublic = annotation.onlyPublic();
        boolean onlyExplicitlyIncluded = annotation.onlyExplicitlyIncluded();
        // 获取属性
        Field[] fields = ReflectUtil.getFields(constantClass);
        Map<String, Object> data = new LinkedHashMap<>();
        Stream.of(fields)
            .filter(field -> Modifier.isStatic(field.getModifiers()))
            .filter(field -> !onlyPublic || Modifier.isPublic(field.getModifiers()))
            .filter(field -> !onlyExplicitlyIncluded || annotationFinder.hasAnnotation(field, ContainerConstant.Include.class))
            .filter(field -> !annotationFinder.hasAnnotation(field, ContainerConstant.Exclude.class))
            .forEach(field -> {
                Object value = ReflectUtil.getStaticFieldValue(field);
                ContainerConstant.Name name = annotationFinder.findAnnotation(field, ContainerConstant.Name.class);
                String key = Objects.isNull(name) ? field.getName() : name.value();
                data.put(key, value);
            });
        // 构建容器
        String namespace = CharSequenceUtil.emptyToDefault(annotation.namespace(), constantClass.getSimpleName());
        return forMap(namespace, data);
    }

    /**
     * 输入一批key值，返回按key值分组的数据源对象
     *
     * @param keys keys
     * @return 按key值分组的数据源对象
     */
    @Override
    public Map<K, ?> get(Collection<K> keys) {
        return data;
    }
}
