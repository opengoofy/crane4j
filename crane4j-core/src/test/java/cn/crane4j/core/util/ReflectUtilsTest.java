package cn.crane4j.core.util;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.ParameterNameFinder;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleParameterNameFinder;
import cn.hutool.core.util.ReflectUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
import java.util.Arrays;
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

        Method method1 = ReflectUtils.getMethod(ReflectUtilsTest.class, "method1");
        Assert.assertNotNull(method1);
        Map<String, Parameter> parameterMap1 = ReflectUtils.resolveParameterNames(finder, method1);
        Assert.assertTrue(parameterMap1.isEmpty());

        Method method2 = ReflectUtils.getMethod(ReflectUtilsTest.class, "method2", String.class, String.class);
        Assert.assertNotNull(method2);
        Map<String, Parameter> parameterMap2 = ReflectUtils.resolveParameterNames(finder, method2);
        Assert.assertEquals(2, parameterMap2.size());

        Map.Entry<String, Parameter> arg1 = CollectionUtils.get(parameterMap2.entrySet(), 0);
        Assert.assertNotNull(arg1);
        Assert.assertEquals("arg0", arg1.getKey());
        Assert.assertEquals("arg0", arg1.getValue().getName());

        Map.Entry<String, Parameter> arg2 = CollectionUtils.get(parameterMap2.entrySet(), 1);
        Assert.assertNotNull(arg2);
        Assert.assertEquals("arg1", arg2.getKey());
        Assert.assertEquals("arg1", arg2.getValue().getName());
    }

    @Test
    public void resolveMethodInvocationArguments() {
        Method method = ReflectUtil.getMethod(ReflectUtilsTest.class, "method2", String.class, String.class);
        Assert.assertNotNull(method);
        Object[] args = new Object[]{"arg0", "arg1"};

        Object[] actualArgs = ReflectUtils.resolveMethodInvocationArguments(method, args);
        Assert.assertArrayEquals(args, actualArgs);

        args = new Object[]{"arg0"};
        actualArgs = ReflectUtils.resolveMethodInvocationArguments(method, args);
        Assert.assertArrayEquals(new Object[]{"arg0", null}, actualArgs);

        args = new Object[]{};
        actualArgs = ReflectUtils.resolveMethodInvocationArguments(method, args);
        Assert.assertArrayEquals(new Object[]{null, null}, actualArgs);

        // if method not have parameter
        method = ReflectUtil.getMethod(ReflectUtilsTest.class, "method1");
        Assert.assertNotNull(method);

        args = new Object[]{};
        actualArgs = ReflectUtils.resolveMethodInvocationArguments(method, args);
        Assert.assertArrayEquals(new Object[]{}, actualArgs);

        args = new Object[]{"arg0"};
        actualArgs = ReflectUtils.resolveMethodInvocationArguments(method, args);
        Assert.assertArrayEquals(new Object[]{}, actualArgs);
    }

    @Test
    public void invoke() {
        Method method = ReflectUtil.getMethod(ReflectUtilsTest.class, "method2", String.class, String.class);
        Assert.assertNotNull(method);

        Object[] args = new Object[]{"arg0", "arg1"};
        Object result = ReflectUtils.invoke(this, method, args);
        Assert.assertEquals("arg0arg1", result);

        args = new Object[]{"arg0"};
        result = ReflectUtils.invoke(this, method, args);
        Assert.assertEquals("arg0null", result);

        args = new Object[]{};
        result = ReflectUtils.invoke(this, method, args);
        Assert.assertEquals("nullnull", result);

        args = new Object[]{"arg0", "arg1", "arg2"};
        result = ReflectUtils.invoke(this, method, args);
        Assert.assertEquals("arg0arg1", result);
    }

    @Test
    public void invokeRaw() {
        Method method = ReflectUtil.getMethod(ReflectUtilsTest.class, "method2", String.class, String.class);
        Assert.assertNotNull(method);

        Object[] args = new Object[]{"arg0", "arg1"};
        Object result = ReflectUtils.invokeRaw(this, method, args);
        Assert.assertEquals("arg0arg1", result);

        Assert.assertThrows(Crane4jException.class, () -> ReflectUtils.invokeRaw(this, method, "arg0"));
        Assert.assertThrows(Crane4jException.class, () -> ReflectUtils.invokeRaw(this, method, 12));
    }

    @Test
    public void getDeclaredMethods() {
        Method[] methods = ReflectUtils.getDeclaredMethods(Foo.class);
        Assert.assertSame(methods, ReflectUtils.getDeclaredMethods(Foo.class));
        Assert.assertEquals(Foo.class.getDeclaredMethods().length, methods.length);
    }

    @Test
    public void getDeclaredMethod() {
        Method method = ReflectUtils.getDeclaredMethod(Foo.class, "getStandard");
        Assert.assertNotNull(method);
        Assert.assertEquals("getStandard", method.getName());
        Assert.assertNull(ReflectUtils.getDeclaredMethod(Foo.class, "noneMethod"));
    }

    @Test
    public void getMethods() {
        Method[] methods = ReflectUtils.getMethods(Foo.class);
        Assert.assertSame(methods, ReflectUtils.getMethods(Foo.class));

        List<Method> allMethods = new ArrayList<>();
        ReflectUtils.traverseTypeHierarchy(Foo.class, type -> allMethods.addAll(Arrays.asList(type.getDeclaredMethods())));
        Assert.assertEquals(allMethods.size(), methods.length);
    }

    @Test
    public void getMethod() {
        Method method = ReflectUtils.getMethod(Foo.class, "getStandard");
        Assert.assertNotNull(method);
        Assert.assertEquals("getStandard", method.getName());
        Assert.assertNull(ReflectUtils.getMethod(Foo.class, "noneMethod"));
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

    @Test
    public void getAnnotationAttributes() {
        Annotation annotation = Foo.class.getAnnotation(Annotation.class);
        Assert.assertNotNull(annotation);
        Map<String, Object> attributes = ReflectUtils.getAnnotationAttributes(annotation);
        Assert.assertEquals(1, attributes.size());
        Assert.assertTrue(attributes.containsKey("value"));
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
    public void getDeclaredFields() {
        Field[] fields = ReflectUtils.getDeclaredFields(Foo.class);
        Assert.assertEquals(3, Stream.of(fields).filter(f -> !Modifier.isStatic(f.getModifiers())).count());
        Assert.assertSame(fields, ReflectUtils.getDeclaredFields(Foo.class));
    }

    @Test
    public void getDeclaredField() {
        Assert.assertNotNull(ReflectUtils.getDeclaredField(Foo.class, "standard"));
        Assert.assertNotNull(ReflectUtils.getDeclaredField(Foo.class, "fluent"));
        Assert.assertNotNull(ReflectUtils.getDeclaredField(Foo.class, "flag"));
        Assert.assertNull(ReflectUtils.getDeclaredField(Foo.class, "notExist"));
    }

    @Test
    public void getFields() {
        Field[] fields = ReflectUtils.getFields(Foo.class);
        Assert.assertSame(fields, ReflectUtils.getFields(Foo.class));

        List<Field> all = new ArrayList<>();
        ReflectUtils.traverseTypeHierarchy(Foo.class, type -> all.addAll(Arrays.asList(type.getDeclaredFields())));
        Assert.assertEquals(all.size(), fields.length);
    }

    @Test
    public void getField() {
        Assert.assertNotNull(ReflectUtils.getField(Foo.class, "standard"));
        Assert.assertNotNull(ReflectUtils.getField(Foo.class, "fluent"));
        Assert.assertNotNull(ReflectUtils.getField(Foo.class, "flag"));
        Assert.assertNull(ReflectUtils.getField(Foo.class, "notExist"));
    }

    @Test
    @SuppressWarnings("all")
    public void getFieldValue() {
        Foo foo = new Foo(1, 2, true);

        // by name
        Assert.assertEquals((Integer)1, ReflectUtils.getFieldValue(foo, "standard"));
        Assert.assertEquals((Integer)2, ReflectUtils.getFieldValue(foo, "fluent"));
        Assert.assertTrue(ReflectUtils.getFieldValue(foo, "flag"));
        Assert.assertNull(ReflectUtils.getFieldValue(foo, "notExist"));

        // by field
        Assert.assertEquals((Integer)1, ReflectUtils.getFieldValue(foo, ReflectUtils.getDeclaredField(Foo.class, "standard")));
        Assert.assertEquals((Integer)2, ReflectUtils.getFieldValue(foo, ReflectUtils.getDeclaredField(Foo.class, "fluent")));
        Assert.assertTrue(ReflectUtils.getFieldValue(foo, ReflectUtils.getDeclaredField(Foo.class, "flag")));
        Assert.assertThrows(Crane4jException.class, () -> ReflectUtils.getFieldValue(null, ReflectUtils.getDeclaredField(Foo.class, "flag")));
        Assert.assertThrows(NullPointerException.class, () -> ReflectUtils.getFieldValue(foo, (Field) null));
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
        public Object invoke(Object proxy, Method method, Object[] args) {
            return valueCache.get(method.getName());
        }
    }

    private static class Super {}

    private interface Interface {}

    @AllArgsConstructor
    @NoArgsConstructor
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
    private static String method2(String param1, String param2) {
        return param1 + param2;
    }

}