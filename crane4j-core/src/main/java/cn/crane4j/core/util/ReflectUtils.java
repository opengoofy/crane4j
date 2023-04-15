package cn.crane4j.core.util;

import cn.crane4j.core.support.AnnotationFinder;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.BiFunction;

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
     * Get annotation for declared fields.
     *
     * @param annotationFinder annotation finder
     * @param beanType bean type
     * @param annotationType annotation type
     * @param mapper mapper
     * @return result list
     */
    public static <T extends Annotation, R> List<R> parseAnnotationForDeclaredFields(
        AnnotationFinder annotationFinder, Class<?> beanType, Class<T> annotationType, BiFunction<T, Field, R> mapper) {
        Field[] fields = ReflectUtils.getDeclaredFields(beanType);
        List<R> results = new ArrayList<>(fields.length);
        for (Field field : fields) {
            Set<T> annotation = annotationFinder.getAllAnnotations(field, annotationType);
            if (CollUtil.isEmpty(annotation)) {
                continue;
            }
            for (T t : annotation) {
                R r = mapper.apply(t, field);
                results.add(r);
            }
        }
        return results;
    }

    /**
     * Set annotation attribute value.
     *
     * @param annotation annotation
     * @param attributeName attribute name
     * @param attributeValue attribute value
     */
    @SuppressWarnings("unchecked")
    public static void setAttributeValue(Annotation annotation, String attributeName, Object attributeValue) {
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
        // adapt to Spring
        String memberAttributeName = JDK_MEMBER_ATTRIBUTE;
        if (CharSequenceUtil.contains(invocationHandler.getClass().getName(), SPRING_INVOCATION_HANDLER)) {
            memberAttributeName = SPRING_MEMBER_ATTRIBUTE;
        }
        Map<String, Object> memberValues = (Map<String, Object>) ReflectUtil.getFieldValue(invocationHandler, memberAttributeName);
        memberValues.put(attributeName, attributeValue);
    }

    /**
     * Add annotation to method.
     *
     * @param annotation annotation
     * @param method method
     * @throws IllegalArgumentException thrown when a method already has annotations of the same type
     */
    @SuppressWarnings("unchecked")
    public static void putAnnotation(Annotation annotation, Method method) {
        Objects.requireNonNull(method);
        Objects.requireNonNull(method.getAnnotations());
        Optional.ofNullable(ReflectUtil.getFieldValue(method, "declaredAnnotations"))
            .map(map -> (Map<Class<? extends Annotation >, Annotation>)map)
            .map(LinkedHashMap::new)
            .ifPresent(map -> {
                Assert.isFalse(
                    map.containsKey(annotation.annotationType()),
                    "method has been annotated by [{}]", annotation.annotationType()
                );
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
