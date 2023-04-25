package cn.crane4j.core.util;

import cn.crane4j.core.support.ParameterNameFinder;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleParameterNameFinder;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * test for {@link ReflectUtils}
 *
 * @author huangchengxing
 */
public class ReflectUtilsTest {

    @Test
    public void resolveParameterNames() {
        ParameterNameFinder finder = new SimpleParameterNameFinder();

        Method method1 = ReflectUtil.getMethod(ReflectUtilsTest.class, "method1");
        Assert.assertNotNull(method1);
        Map<String, Parameter> parameterMap1 = ReflectUtils.resolveParameterNames(finder, method1);
        Assert.assertTrue(parameterMap1.isEmpty());

        Method method2 = ReflectUtil.getMethod(ReflectUtilsTest.class, "method2", String.class, String.class);
        Assert.assertNotNull(method2);
        Map<String, Parameter> parameterMap2 = ReflectUtils.resolveParameterNames(finder, method2);
        Assert.assertEquals(2, parameterMap2.size());

        Map.Entry<String, Parameter> arg1 = CollUtil.get(parameterMap2.entrySet(), 0);
        Assert.assertEquals("arg0", arg1.getKey());
        Assert.assertEquals("arg0", arg1.getValue().getName());

        Map.Entry<String, Parameter> arg2 = CollUtil.get(parameterMap2.entrySet(), 1);
        Assert.assertEquals("arg1", arg2.getKey());
        Assert.assertEquals("arg1", arg2.getValue().getName());
    }

    @Test
    public void getAnnotationAttributes() {
        Annotation annotation = Foo.class.getAnnotation(Annotation.class);
        Assert.assertNotNull(annotation);
        Map<String, Object> attributes = ReflectUtils.getAnnotationAttributes(annotation);
        Assert.assertEquals(1, attributes.size());
        Assert.assertTrue(attributes.containsKey("value"));
    }

    @Test
    public void isJdkElement() throws NoSuchMethodException {
        Assert.assertTrue(ReflectUtils.isJdkElement(Object.class));
        Assert.assertTrue(ReflectUtils.isJdkElement(String.class));
        Assert.assertTrue(ReflectUtils.isJdkElement(Integer.class));
        Assert.assertTrue(ReflectUtils.isJdkElement(int.class));
        Assert.assertFalse(ReflectUtils.isJdkElement(ReflectUtils.class));
        Assert.assertFalse(ReflectUtils.isJdkElement(ReflectUtilsTest.class));
        Assert.assertTrue(ReflectUtils.isJdkElement(Object.class.getMethod("toString")));
    }

    @Test
    public void getDeclaredMethods() {
        Method[] methods = ReflectUtils.getDeclaredMethods(Foo.class);
        Assert.assertEquals(6, methods.length);
        Assert.assertSame(methods, ReflectUtils.getDeclaredMethods(Foo.class));
    }

    @Test
    public void getDeclaredMethod() {
        Method method = ReflectUtils.getDeclaredMethod(Foo.class, "getStandard");
        Assert.assertNotNull(method);
        Assert.assertEquals("getStandard", method.getName());
        Assert.assertNull(ReflectUtils.getDeclaredMethod(Foo.class, "noneMethod"));
    }

    @Test
    public void getDeclaredSuperClassWithInterface() {
        Set<Class<?>> classes = ReflectUtils.getDeclaredSuperClassWithInterface(Foo.class);
        Assert.assertEquals(2, classes.size());
        Assert.assertTrue(classes.contains(Super.class));
        Assert.assertTrue(classes.contains(Interface.class));
    }

    @Test
    public void traverseTypeHierarchy() {
        List<Class<?>> classList = new ArrayList<>();
        ReflectUtils.traverseTypeHierarchy(Foo.class, classList::add);
        Assert.assertEquals(3, classList.size());
        Assert.assertEquals(Foo.class, classList.get(0));
        Assert.assertEquals(Super.class, classList.get(1));
        Assert.assertEquals(Interface.class, classList.get(2));
    }

    @Test
    public void parseAnnotationForDeclaredFields() {
        List<Annotation> annotationList = ReflectUtils.parseAnnotationForDeclaredFields(
            new SimpleAnnotationFinder(), Foo.class, Annotation.class, (a, f) -> a
        );
        Assert.assertEquals(2, annotationList.size());
    }

    @Test
    public void putAnnotation() {
        Method method = ReflectUtil.getMethod(ReflectUtilsTest.class, "putAnnotation");
        Assert.assertNotNull(method);
        Annotation annotation = method.getAnnotation(Annotation.class);
        Assert.assertNull(annotation);
        ReflectUtils.putAnnotation(Foo.class.getAnnotation(Annotation.class), method);
        annotation = method.getAnnotation(Annotation.class);
        Assert.assertNotNull(annotation);
    }

    @Test
    public void setAttributeValue() {
        Annotation annotation = Foo.class.getAnnotation(Annotation.class);
        int expected = -1;
        Assert.assertNotEquals(expected, annotation.value());
        ReflectUtils.setAttributeValue(annotation, "value", expected);
        Assert.assertEquals(expected, annotation.value());

        Map<String, Object> memberValues = new HashMap<>();
        InvocationHandler handler = new SynthesizedMergedAnnotationInvocationHandler(memberValues);
        Annotation synthesizedAnnotation = (Annotation) Proxy.newProxyInstance(
            Annotation.class.getClassLoader(), new Class<?>[]{Annotation.class}, handler
        );
        ReflectUtils.setAttributeValue(synthesizedAnnotation, "value", expected);
        Assert.assertEquals(expected, synthesizedAnnotation.value());
    }

    @Test
    public void getDeclaredFields() {
        Field[] fields = ReflectUtils.getDeclaredFields(Foo.class);
        Assert.assertEquals(3, Stream.of(fields).filter(f -> !Modifier.isStatic(f.getModifiers())).count());
        Assert.assertSame(fields, ReflectUtils.getDeclaredFields(Foo.class));
    }

    @Test
    public void findGetterMethodByName() {
        Assert.assertTrue(
            ReflectUtils.findGetterMethod(Foo.class, "standard").isPresent()
        );
        Assert.assertTrue(
            ReflectUtils.findGetterMethod(Foo.class, "fluent").isPresent()
        );
        Assert.assertTrue(
            ReflectUtils.findGetterMethod(Foo.class, "flag").isPresent()
        );
    }

    @SneakyThrows
    @Test
    public void findGetterMethodByField() {
        Field field = Foo.class.getDeclaredField("standard");
        Assert.assertTrue(
            ReflectUtils.findGetterMethod(Foo.class, field).isPresent()
        );

        field = Foo.class.getDeclaredField("fluent");
        Assert.assertTrue(
            ReflectUtils.findGetterMethod(Foo.class, field).isPresent()
        );

        field = Foo.class.getDeclaredField("flag");
        Assert.assertTrue(
            ReflectUtils.findGetterMethod(Foo.class, field).isPresent()
        );
    }

    @SneakyThrows
    @Test
    public void findSetterMethod() {
        Field field = Foo.class.getDeclaredField("standard");
        Assert.assertTrue(
            ReflectUtils.findSetterMethod(Foo.class, field).isPresent()
        );

        field = Foo.class.getDeclaredField("fluent");
        Assert.assertTrue(
            ReflectUtils.findSetterMethod(Foo.class, field).isPresent()
        );

        field = Foo.class.getDeclaredField("flag");
        Assert.assertTrue(
            ReflectUtils.findSetterMethod(Foo.class, field).isPresent()
        );
    }

    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Annotation {
        int value() default 0;
    }

    @RequiredArgsConstructor
    private static class SynthesizedMergedAnnotationInvocationHandler implements InvocationHandler {
        private final Map<String, Object> valueCache;
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return valueCache.get(method.getName());
        }
    }

    private static class Super {}

    private interface Interface {}

    @Annotation
    private static class Foo extends Super implements Interface {

        @Annotation
        private Integer standard;

        @Annotation
        private Integer fluent;

        private boolean flag;

        public Integer getStandard() {
            return standard;
        }
        public void setStandard(Integer standard) {

        }

        public Integer fluent() {
            return fluent;
        }
        public void fluent(Integer id) {

        }

        public boolean isFlag() {
            return flag;
        }
        public void isFlag(boolean flag) {
        }
    }

    private static void method1() {}
    private static void method2(String param1, String param2) {}

}