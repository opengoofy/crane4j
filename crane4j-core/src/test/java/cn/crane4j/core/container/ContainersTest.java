package cn.crane4j.core.container;

import cn.crane4j.annotation.ContainerConstant;
import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.converter.HutoolConverterManager;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * test for {@link Containers}
 *
 * @author huangchengxing
 */
public class ContainersTest {
    @Test
    public void empty() {
        Container<Object> container = Containers.empty();
        Assert.assertSame(container, Container.empty());
        Assert.assertEquals(Container.EMPTY_CONTAINER_NAMESPACE, container.getNamespace());
        Assert.assertTrue(container.get(null).isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void forEnum() {
        Container<String> container = Containers.forEnum(
            FooEnum.class.getSimpleName(), FooEnum.class, Enum::name
        );
        Assert.assertEquals(FooEnum.class.getSimpleName(), container.getNamespace());

        Map<String, FooEnum> data = (Map<String, FooEnum>) container.get(null);
        Assert.assertEquals(FooEnum.ONE, data.get(FooEnum.ONE.name()));
        Assert.assertEquals(FooEnum.TWO, data.get(FooEnum.TWO.name()));
    }

    @Test
    public void forAnnotatedEnum() {
        // annotated
        Container<String> container = Containers.forEnum(AnnotatedEnum.class, new SimpleAnnotationFinder(), new ReflectivePropertyOperator(new HutoolConverterManager()));
        Assert.assertEquals(AnnotatedEnum.class.getSimpleName(), container.getNamespace());
        Map<?, ?> data = container.get(null);
        Assert.assertEquals(AnnotatedEnum.ONE.getValue(), data.get(AnnotatedEnum.ONE.getKey()));
        Assert.assertEquals(AnnotatedEnum.TWO.getValue(), data.get(AnnotatedEnum.TWO.getKey()));

        // no annotated
        PropertyOperator propertyOperator = new ReflectivePropertyOperator(new HutoolConverterManager());
        container = Containers.forEnum(FooEnum.class, new SimpleAnnotationFinder(), propertyOperator);
        Assert.assertEquals(FooEnum.class.getSimpleName(), container.getNamespace());
        data = container.get(null);
        Assert.assertEquals(FooEnum.ONE, data.get(FooEnum.ONE.name()));
        Assert.assertEquals(FooEnum.TWO, data.get(FooEnum.TWO.name()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void forAnnotatedEnumWhenDefault() {
        Container<String> container = Containers.forEnum(DefaultAnnotatedEnum.class, new SimpleAnnotationFinder(), new ReflectivePropertyOperator(new HutoolConverterManager()));
        Assert.assertEquals(DefaultAnnotatedEnum.class.getSimpleName(), container.getNamespace());

        Map<String, DefaultAnnotatedEnum> data = (Map<String, DefaultAnnotatedEnum>) container.get(null);
        Assert.assertEquals(DefaultAnnotatedEnum.ONE, data.get(DefaultAnnotatedEnum.ONE.name()));
        Assert.assertEquals(DefaultAnnotatedEnum.TWO, data.get(DefaultAnnotatedEnum.TWO.name()));
    }

    @Test
    public void forMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("1", new Object());
        map.put("2", 2);
        String namespace = "map";
        Container<String> container = Containers.forMap(namespace, map);
        Assert.assertEquals(namespace, container.getNamespace());
        Assert.assertSame(map, container.get(null));
    }

    @Test
    public void forConstantClass() {
        Container<?> container1 = Containers.forConstantClass(
            FooConstant1.class, new SimpleAnnotationFinder()
        );
        Assert.assertEquals("foo", container1.getNamespace());
        Map<?, ?> sources1 = container1.get(null);
        Assert.assertTrue(sources1.containsKey("ONE"));
        Assert.assertEquals("one", sources1.get("ONE"));
        Assert.assertTrue(sources1.containsKey("THREE"));
        Assert.assertEquals("three", sources1.get("THREE"));
        Assert.assertFalse(sources1.containsKey("two"));

        Container<?> container2 = Containers.forConstantClass(
            FooConstant2.class, new SimpleAnnotationFinder()
        );
        Assert.assertEquals(FooConstant2.class.getSimpleName(), container2.getNamespace());
        Map<?, ?> sources2 = container2.get(null);
        Assert.assertTrue(sources2.containsKey("ONE"));
        Assert.assertEquals("one", sources2.get("ONE"));
        Assert.assertTrue(sources2.containsKey("THREE"));
        Assert.assertEquals("three", sources2.get("THREE"));
        Assert.assertFalse(sources2.containsKey("two"));

        Container<?> container3 = Containers.forConstantClass(
            FooConstant3.class, new SimpleAnnotationFinder()
        );
        Assert.assertEquals(FooConstant3.class.getSimpleName(), container3.getNamespace());
        Map<?, ?> sources3 = container3.get(null);
        Assert.assertTrue(sources3.containsKey("one"));
        Assert.assertEquals("ONE", sources3.get("one"));
        Assert.assertTrue(sources3.containsKey("three"));
        Assert.assertEquals("THREE", sources3.get("three"));
        Assert.assertFalse(sources3.containsKey("two"));
    }

    @Test
    public void forLambda() {
        String namespace = "lambda";
        Container<String> container = Containers.forLambda(namespace, ContainersTest::getData);
        Assert.assertEquals(namespace, container.getNamespace());
        Map<String, ?> data = container.get(Arrays.asList("1", "2"));
        Assert.assertEquals("1", data.get("1"));
        Assert.assertEquals("2", data.get("2"));
    }

    private static Map<String, Object> getData(Collection<String> keys) {
        return keys.stream().collect(Collectors.toMap(Function.identity(), Function.identity()));
    }

    @Getter
    private enum FooEnum {
        ONE, TWO;
    }

    @ContainerEnum
    @Getter
    private enum DefaultAnnotatedEnum {
        ONE, TWO;
    }

    @ContainerEnum(namespace = "AnnotatedEnum", key = "key", value = "value")
    @Getter
    @RequiredArgsConstructor
    private enum AnnotatedEnum {
        ONE(1, "one"),
        TWO(2, "two");
        private final int key;
        private final String value;
    }

    @SuppressWarnings("unused")
    @ContainerConstant(namespace = "foo")
    public static class FooConstant1 {
        @ContainerConstant.Include
        public static final String ONE = "one";
        @ContainerConstant.Exclude
        public static final String TWO = "two";
        @ContainerConstant.Name("THREE")
        public static final String SAN = "three";
    }

    @SuppressWarnings("unused")
    @ContainerConstant(onlyExplicitlyIncluded = true, onlyPublic = false)
    public static class FooConstant2 {
        @ContainerConstant.Include
        public static final String ONE = "one";
        public static final String TWO = "two";
        @ContainerConstant.Include
        @ContainerConstant.Name("THREE")
        private static final String SAN = "three";
    }

    @SuppressWarnings("unused")
    @ContainerConstant(onlyExplicitlyIncluded = true, onlyPublic = false, reverse = true)
    public static class FooConstant3 {
        @ContainerConstant.Include
        public static final String ONE = "one";
        public static final String TWO = "two";
        @ContainerConstant.Include
        @ContainerConstant.Name("THREE")
        private static final String SAN = "three";
    }
}
