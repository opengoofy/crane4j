package cn.createsequence.crane4j.core.container;

import cn.createsequence.crane4j.core.annotation.ContainerConstant;
import cn.createsequence.crane4j.core.annotation.ContainerEnum;
import cn.createsequence.crane4j.core.support.SimpleAnnotationFinder;
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
        // 有注解
        Container<String> container = ConstantContainer.forEnum(AnnotatedEnum.class, new SimpleAnnotationFinder());
        Assert.assertEquals(AnnotatedEnum.class.getSimpleName(), container.getNamespace());
        Map<?, ?> data = container.get(null);
        Assert.assertEquals(AnnotatedEnum.ONE.getValue(), data.get(AnnotatedEnum.ONE.getKey()));
        Assert.assertEquals(AnnotatedEnum.TWO.getValue(), data.get(AnnotatedEnum.TWO.getKey()));

        // 没注解
        container = ConstantContainer.forEnum(FooEnum.class, new SimpleAnnotationFinder());
        Assert.assertEquals(FooEnum.class.getSimpleName(), container.getNamespace());
        data = container.get(null);
        Assert.assertEquals(FooEnum.ONE, data.get(FooEnum.ONE.name()));
        Assert.assertEquals(FooEnum.TWO, data.get(FooEnum.TWO.name()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void forAnnotatedEnumWhenDefault() {
        Container<String> container = ConstantContainer.forEnum(DefaultAnnotatedEnum.class, new SimpleAnnotationFinder());
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
        ConstantContainer<String> container1 = ConstantContainer.forConstantClass(
            FooConstant1.class, new SimpleAnnotationFinder()
        );
        Assert.assertEquals("foo", container1.getNamespace());
        Map<String, ?> sources1 = container1.get(null);
        Assert.assertTrue(sources1.containsKey("ONE"));
        Assert.assertEquals("one", sources1.get("ONE"));
        Assert.assertTrue(sources1.containsKey("THREE"));
        Assert.assertEquals("three", sources1.get("THREE"));
        Assert.assertFalse(sources1.containsKey("two"));

        ConstantContainer<String> container2 = ConstantContainer.forConstantClass(
            FooConstant2.class, new SimpleAnnotationFinder()
        );
        Assert.assertEquals(FooConstant2.class.getSimpleName(), container2.getNamespace());
        Map<String, ?> sources2 = container2.get(null);
        Assert.assertTrue(sources2.containsKey("ONE"));
        Assert.assertEquals("one", sources1.get("ONE"));
        Assert.assertTrue(sources2.containsKey("THREE"));
        Assert.assertEquals("three", sources1.get("THREE"));
        Assert.assertFalse(sources2.containsKey("two"));
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
}
