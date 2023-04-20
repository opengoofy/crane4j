package cn.crane4j.core.util;

import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.ParameterNameFinder;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * declared method cache
     */
    private static final Map<Class<?>, Method[]> DECLARED_METHOD_CACHE = CollectionUtils.newWeakConcurrentMap();

    /**
     * declared super class with interface
     */
    private static final Map<Class<?>, Set<Class<?>>> DECLARED_SUPER_CLASS_WITH_INTERFACE = CollectionUtils.newWeakConcurrentMap();

    /**
     * Resolve method parameter names.
     *
     * @param finder discoverer
     * @param method method
     * @return collection of parameter name and parameter
     */
    @SuppressWarnings("all")
    public static Map<String, Parameter> resolveParameterNames(ParameterNameFinder finder, Method method) {
        Parameter[] parameters = method.getParameters();
        String[] parameterNames = finder.getParameterNames(method);
        if (ArrayUtil.isEmpty(parameters)) {
            return Collections.emptyMap();
        }
        Map<String, Parameter> parameterMap = new LinkedHashMap<>(parameters.length);
        int nameLength = ArrayUtil.length(parameterNames);
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String parameterName = nameLength <= i ? parameter.getName() : parameterNames[i];
            parameterMap.put(parameterName, parameter);
        }
        return parameterMap;
    }

    /**
     * Get all attribute value of annotation.
     *
     * @param annotation annotation
     * @return all attribute value of annotation
     */
    public static Map<String, Object> getAnnotationAttributes(Annotation annotation) {
        return Stream.of(getDeclaredMethods(annotation.annotationType()))
            .filter(m -> m.getParameterCount() == 0)
            .filter(m -> !Objects.equals(m.getReturnType(), Void.TYPE))
            .collect(Collectors.toMap(Method::getName, m -> ReflectUtil.invoke(annotation, m)));
    }

    /**
     * Whether the {@code element} is from jdk.
     *
     * @param element element
     * @return boolean
     */
    public static boolean isJdkElement(AnnotatedElement element) {
        Class<?> checkedClass = element.getClass();
        if (element instanceof Class) {
            checkedClass = (Class<?>)element;
        } else if (element instanceof Member) {
            checkedClass = ((Member)element).getDeclaringClass();
        }
        return ClassUtil.isJdkClass(checkedClass);
    }

    /**
     * Get declared methods of type.
     *
     * @param type type
     * @return method list
     */
    public static Method[] getDeclaredMethods(Class<?> type) {
        return CollectionUtils.computeIfAbsent(DECLARED_METHOD_CACHE, type, k -> ReflectUtil.getMethods(type));
    }

    /**
     * Get method by name and parameter types.
     *
     * @param type type
     * @param methodName method name
     * @param parameterTypes parameter types
     * @return method if found, otherwise null
     */
    @Nullable
    public static Method getDeclaredMethod(
        Class<?> type, String methodName, Class<?>... parameterTypes) {
        return Stream.of(getDeclaredMethods(type))
            .filter(method -> method.getName().equals(methodName) && Arrays.equals(method.getParameterTypes(), parameterTypes))
            .findFirst()
            .orElse(null);
    }

    /**
     * Get declared super class with interface.
     *
     * @param type type
     * @return declared super class with interface
     */
    public static Set<Class<?>> getDeclaredSuperClassWithInterface(Class<?> type) {
        return CollectionUtils.computeIfAbsent(DECLARED_SUPER_CLASS_WITH_INTERFACE, type, k -> {
            Set<Class<?>> result = new LinkedHashSet<>();
            Class<?> superClass = type.getSuperclass();
            if (superClass != null) {
                result.add(superClass);
            }
            result.addAll(Arrays.asList(type.getInterfaces()));
            return result;
        });
    }

    /**
     * Traverse type hierarchy.
     *
     * @param beanType bean type
     * @param consumer operation for each type
     */
    public static void traverseTypeHierarchy(Class<?> beanType, Consumer<Class<?>> consumer) {
        Set<Class<?>> accessed = new HashSet<>();
        Deque<Class<?>> typeQueue = new LinkedList<>();
        typeQueue.add(beanType);
        while (!typeQueue.isEmpty()) {
            Class<?> type = typeQueue.removeFirst();
            accessed.add(type);
            // do something for current type
            consumer.accept(type);
            // then find superclass and interfaces
            Set<Class<?>> declaredSuperClassWithInterface = getDeclaredSuperClassWithInterface(type);
            declaredSuperClassWithInterface.remove(Object.class);
            declaredSuperClassWithInterface.removeAll(accessed);
            CollUtil.addAll(typeQueue, declaredSuperClassWithInterface);
        }
    }

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
