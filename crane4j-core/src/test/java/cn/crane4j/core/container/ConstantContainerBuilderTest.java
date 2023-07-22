package cn.crane4j.core.container;

import cn.crane4j.annotation.ContainerConstant;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * test for {@link ConstantContainerBuilder}
 *
 * @author tangcent
 */
public class ConstantContainerBuilderTest {

    @Test
    public void nonAnnotatedConstantDefault() {
        Container<?> container = ConstantContainerBuilder.of(FooConstant.class)
            .annotationFinder(new SimpleAnnotationFinder())
            .build();
        Assert.assertEquals(FooConstant.class.getSimpleName(), container.getNamespace());
        Map<?, ?> sources = container.get(null);
        Assert.assertTrue(sources.containsKey("ONE"));
        Assert.assertEquals("one", sources.get("ONE"));
        Assert.assertTrue(sources.containsKey("TWO"));
        Assert.assertEquals("two", sources.get("TWO"));
    }

    @Test
    public void annotatedConstantDefault() {
        Container<?> container = ConstantContainerBuilder.of(AnnotatedConstant.class)
            .annotationFinder(new SimpleAnnotationFinder())
            .build();
        Assert.assertEquals(AnnotatedConstant.class.getSimpleName(), container.getNamespace());
        Map<?, ?> sources = container.get(null);
        Assert.assertTrue(sources.containsKey("one"));
        Assert.assertEquals("ONE", sources.get("one"));
        Assert.assertTrue(sources.containsKey("three"));
        Assert.assertEquals("THREE", sources.get("three"));
        Assert.assertFalse(sources.containsKey("two"));
    }

    @Test
    public void nonAnnotatedConstant() {
        Container<?> container = ConstantContainerBuilder.of(FooConstant.class)
            .annotationFinder(new SimpleAnnotationFinder())
            .namespace("test")
            .onlyPublic(false)
            .reverse(true)
            .build();
        Assert.assertEquals("test", container.getNamespace());
        Map<?, ?> sources = container.get(null);
        Assert.assertTrue(sources.containsKey("one"));
        Assert.assertEquals("ONE", sources.get("one"));
        Assert.assertTrue(sources.containsKey("two"));
        Assert.assertEquals("TWO", sources.get("two"));
        Assert.assertTrue(sources.containsKey("three"));
        Assert.assertEquals("SAN", sources.get("three"));
    }

    @Test
    public void annotatedConstant() {
        Container<?> container = ConstantContainerBuilder.of(AnnotatedConstant.class)
            .annotationFinder(new SimpleAnnotationFinder())
            .namespace("test")
            .onlyPublic(true)
            .onlyExplicitlyIncluded(false)
            .reverse(false)
            .build();
        Assert.assertEquals("test", container.getNamespace());
        Map<?, ?> sources = container.get(null);
        Assert.assertTrue(sources.containsKey("ONE"));
        Assert.assertEquals("one", sources.get("ONE"));
        Assert.assertTrue(sources.containsKey("TWO"));
        Assert.assertEquals("two", sources.get("TWO"));
        Assert.assertFalse(sources.containsKey("THREE"));
    }

    @SuppressWarnings("unused")
    public static class FooConstant {
        public static final String ONE = "one";
        public static final String TWO = "two";

        private static final String SAN = "three";
    }

    @SuppressWarnings("unused")
    @ContainerConstant(onlyExplicitlyIncluded = true, onlyPublic = false, reverse = true)
    public static class AnnotatedConstant {
        @ContainerConstant.Include
        public static final String ONE = "one";
        public static final String TWO = "two";
        @ContainerConstant.Include
        @ContainerConstant.Name("THREE")
        private static final String SAN = "three";
    }
}
