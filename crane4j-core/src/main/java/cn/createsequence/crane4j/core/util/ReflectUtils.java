package cn.createsequence.crane4j.core.util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * ReflectUtils
 *
 * @author huangchengxing
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectUtils {

    private static final String SET_PREFIX = "set";
    private static final String IS_PREFIX = "is";
    private static final String GET_PREFIX = "get";

    private static final String JDK_MEMBER_ATTRIBUTE = "memberValues";
    private static final String SPRING_MEMBER_ATTRIBUTE = "valueCache";
    private static final String SPRING_INVOCATION_HANDLER = "SynthesizedMergedAnnotationInvocationHandler";

    /**
     * declared field cache
     */
    private static final Map<Class<?>, Field[]> DECLARED_FIELD_CACHE = CollectionUtils.newWeakConcurrentMap();

    /**
     * 设置注解属性值
     *
     * @param annotation 注解
     * @param attributeName 属性名
     * @param attributeValue 属性值
     */
    @SuppressWarnings("unchecked")
    public static void setAttributeValue(Annotation annotation, String attributeName, Object attributeValue) {
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
        // 由于Spring合成注解使用的代理不同，此处作出区分
        String memberAttributeName = JDK_MEMBER_ATTRIBUTE;
        if (CharSequenceUtil.contains(invocationHandler.getClass().getName(), SPRING_INVOCATION_HANDLER)) {
            memberAttributeName = SPRING_MEMBER_ATTRIBUTE;
        }
        Map<String, Object> memberValues = (Map<String, Object>) ReflectUtil.getFieldValue(invocationHandler, memberAttributeName);
        memberValues.put(attributeName, attributeValue);
    }

    /**
     * 向方法添加注解
     *
     * @param annotation 注解
     * @param method 方法
     */
    @SuppressWarnings("unchecked")
    public static void putAnnotation(Annotation annotation, Method method) {
        Objects.requireNonNull(method);
        Objects.requireNonNull(method.getAnnotations());
        Optional.ofNullable(ReflectUtil.getFieldValue(method, "declaredAnnotations"))
            .map(map -> (Map<Class<? extends Annotation >, Annotation>)map)
            .map(LinkedHashMap::new)
            .ifPresent(map -> {
                map.put(annotation.annotationType(), annotation);
                ReflectUtil.setFieldValue(method, "declaredAnnotations", Collections.unmodifiableMap(map));
            });
    }

    /**
     * Get declared fields.
     *
     * @param type type
     * @return java.lang.reflect.Field[]
     */
    public static Field[] getDeclaredFields(Class<?> type) {
        return DECLARED_FIELD_CACHE.computeIfAbsent(type, Class::getDeclaredFields);
    }

    /**
     * find getter method
     *
     * @param beanType bean's type
     * @param field field
     * @return java.util.Optional<java.lang.reflect.Method>
     */
    public static Optional<Method> findGetterMethod(Class<?> beanType, Field field) {
        // find isXXX method
        Class<?> fieldType = field.getType();
        if (boolean.class.equals(fieldType) || Boolean.class.equals(fieldType)) {
            String booleanGetterName = CharSequenceUtil.upperFirstAndAddPre(field.getName(), IS_PREFIX);
            return Optional.ofNullable(ReflectUtil.getMethod(beanType, booleanGetterName));
        }

        // find getXXX method
        String getterName = CharSequenceUtil.upperFirstAndAddPre(field.getName(), GET_PREFIX);
        Method method = ReflectUtil.getMethod(beanType, getterName);
        if (Objects.nonNull(method)) {
            return Optional.of(method);
        }

        // find fluent method
        getterName = field.getName();
        return Optional.ofNullable(ReflectUtil.getMethod(beanType, getterName));
    }

    /**
     * find getter method
     *
     * @param beanType bean's type
     * @param fieldName field's name
     * @return java.util.Optional<java.lang.reflect.Method>
     */
    public static Optional<Method> findGetterMethod(Class<?> beanType, String fieldName) {
        // find getXXX method
        String getterName = CharSequenceUtil.upperFirstAndAddPre(fieldName, GET_PREFIX);
        Method method = ReflectUtil.getMethod(beanType, getterName);
        if (Objects.nonNull(method)) {
            return Optional.of(method);
        }

        // find fluent method
        Optional<Method> fluentGetter = Optional.ofNullable(ReflectUtil.getMethod(beanType, fieldName));
        if (fluentGetter.isPresent()) {
            return fluentGetter;
        }

        // find isXXX method
        String booleanGetterName = CharSequenceUtil.upperFirstAndAddPre(fieldName, IS_PREFIX);
        return Optional.ofNullable(ReflectUtil.getMethod(beanType, booleanGetterName));
    }

    /**
     * find setter method
     *
     * @param beanType bean's type
     * @param field field
     * @return java.util.Optional<java.lang.reflect.Method>
     */
    public static Optional<Method> findSetterMethod(Class<?> beanType, Field field) {
        // find setXXX method
        Class<?> fieldType = field.getType();
        String setterName = CharSequenceUtil.upperFirstAndAddPre(field.getName(), SET_PREFIX);
        Method method = ReflectUtil.getMethod(beanType, setterName, fieldType);
        if (Objects.nonNull(method)) {
            return Optional.of(method);
        }

        // find fluent method
        setterName = field.getName();
        method = ReflectUtil.getMethod(beanType, setterName, fieldType);
        if (Objects.nonNull(method)) {
            return Optional.of(method);
        }

        // find isXXX method
        String booleanSetterName = CharSequenceUtil.upperFirstAndAddPre(field.getName(), IS_PREFIX);
        return Optional.ofNullable(ReflectUtil.getMethod(beanType, booleanSetterName, fieldType));
    }

}
