package cn.crane4j.core.container;

import cn.crane4j.annotation.ContainerConstant;
import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.converter.HutoolConverterManager;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectPropertyOperator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * test for {@link ConstantContainer}
 *
 * @author huangchengxing
 */
public class ConstantContainerTest {

    @SuppressWarnings("unchecked")
    @Test
    public void forEnum() {
        Container<String> container = ConstantContainer.forEnum(
            FooEnum.class.getSimpleName(), FooEnum.class, Enum::name
        );
        Assert.assertEquals(FooEnum.class.getSimpleName(), container.getNamespace());

        Map<String, FooEnum> data = (Map<String, FooEnum>)container.get(null);
        Assert.assertEquals(FooEnum.ONE, data.get(FooEnum.ONE.name()));
        Assert.assertEquals(FooEnum.TWO, data.get(FooEnum.TWO.name()));
    }

    @Test
    public void forAnnotatedEnum() {
        // annotated
        Container<String> container = ConstantContainer.forEnum(AnnotatedEnum.class, new SimpleAnnotationFinder(), new ReflectPropertyOperator(new HutoolConverterManager()));
        Assert.assertEquals(AnnotatedEnum.class.getSimpleName(), container.getNamespace());
        Map<?, ?> data = container.get(null);
        Assert.assertEquals(AnnotatedEnum.ONE.getValue(), data.get(AnnotatedEnum.ONE.getKey()));
        Assert.assertEquals(AnnotatedEnum.TWO.getValue(), data.get(AnnotatedEnum.TWO.getKey()));

        // no annotated
        PropertyOperator propertyOperator = new ReflectPropertyOperator(new HutoolConverterManager());
        container = ConstantContainer.forEnum(FooEnum.class, new SimpleAnnotationFinder(), propertyOperator);
        Assert.assertEquals(FooEnum.class.getSimpleName(), container.getNamespace());
        data = container.get(null);
        Assert.assertEquals(FooEnum.ONE, data.get(FooEnum.ONE.name()));
        Assert.assertEquals(FooEnum.TWO, data.get(FooEnum.TWO.name()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void forAnnotatedEnumWhenDefault() {
        Container<String> container = ConstantContainer.forEnum(DefaultAnnotatedEnum.class, new SimpleAnnotationFinder(), new ReflectPropertyOperator(new HutoolConverterManager()));
        Assert.assertEquals(DefaultAnnotatedEnum.class.getSimpleName(), container.getNamespace());

        Map<String, DefaultAnnotatedEnum> data = (Map<String, DefaultAnnotatedEnum>)container.get(null);
        Assert.assertEquals(DefaultAnnotatedEnum.ONE, data.get(DefaultAnnotatedEnum.ONE.name()));
        Assert.assertEquals(DefaultAnnotatedEnum.TWO, data.get(DefaultAnnotatedEnum.TWO.name()));
    }

    @Test
    public void forMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("1", new Object());
        map.put("2", 2);
        String namespace = "map";
        Container<String> container = ConstantContainer.forMap(namespace, map);
        Assert.assertEquals(namespace, container.getNamespace());
        Assert.assertSame(map, container.get(null));
    }

    @Test
    public void forConstantClass() {
        ConstantContainer<?> container1 = ConstantContainer.forConstantClass(
            FooConstant1.class, new SimpleAnnotationFinder()
        );
        Assert.assertEquals("foo", container1.getNamespace());
        Map<?, ?> sources1 = container1.get(null);
        Assert.assertTrue(sources1.containsKey("ONE"));
        Assert.assertEquals("one", sources1.get("ONE"));
        Assert.assertTrue(sources1.containsKey("THREE"));
        Assert.assertEquals("three", sources1.get("THREE"));
        Assert.assertFalse(sources1.containsKey("two"));

        ConstantContainer<?> container2 = ConstantContainer.forConstantClass(
            FooConstant2.class, new SimpleAnnotationFinder()
        );
        Assert.assertEquals(FooConstant2.class.getSimpleName(), container2.getNamespace());
        Map<?, ?> sources2 = container2.get(null);
        Assert.assertTrue(sources2.containsKey("ONE"));
        Assert.assertEquals("one", sources2.get("ONE"));
        Assert.assertTrue(sources2.containsKey("THREE"));
        Assert.assertEquals("three", sources2.get("THREE"));
        Assert.assertFalse(sources2.containsKey("two"));

        ConstantContainer<?> container3 = ConstantContainer.forConstantClass(
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

    @ContainerConstant(namespace = "foo")
    public static class FooConstant1 {
        @ContainerConstant.Include
        public static final String ONE = "one";
        @ContainerConstant.Exclude
        public static final String TWO = "two";
        @ContainerConstant.Name("THREE")
        public static final String SAN = "three";
    }

    @ContainerConstant(onlyExplicitlyIncluded = true, onlyPublic = false)
    public static class FooConstant2 {
        @ContainerConstant.Include
        public static final String ONE = "one";
        public static final String TWO = "two";
        @ContainerConstant.Include
        @ContainerConstant.Name("THREE")
        private static final String SAN = "three";
    }

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
