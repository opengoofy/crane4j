package cn.crane4j.extension.spring;

import cn.crane4j.core.support.AnnotationFinder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * test for {@link MergedAnnotationFinder}
 *
 * @author huangchengxing
 */
public class MergedAnnotationFinderTest {

    private AnnotationFinder annotationFinder;

    @Before
    public void init() {
        annotationFinder = new MergedAnnotationFinder();
    }

    @Test
    public void findAllAnnotations() {
        Set<Annotation> annotations = annotationFinder.findAllAnnotations(Child.class, Annotation.class);
        Set<String> values = annotations.stream().map(Annotation::value).collect(Collectors.toSet());
        Assert.assertEquals(3, values.size());
        Assert.assertTrue(values.contains("one"));
        Assert.assertTrue(values.contains("two"));
        Assert.assertTrue(values.contains("three"));
    }

    @Test
    public void findAnnotation() {
        Annotation annotation = annotationFinder.findAnnotation(Child.class, Annotation.class);
        Assert.assertEquals("one", annotation.name());
        Assert.assertEquals("one", annotation.value());
    }

    @Test
    public void isAnnotated() {
        Assert.assertTrue(annotationFinder.isAnnotated(Child.class, Annotation.class));
    }

    @Test
    public void getAllAnnotations() {
        Set<Annotation> annotations = annotationFinder.getAllAnnotations(Foo.class, Annotation.class);
        Set<String> values = annotations.stream().map(Annotation::value).collect(Collectors.toSet());
        Assert.assertEquals(3, values.size());
        Assert.assertTrue(values.contains("one"));
        Assert.assertTrue(values.contains("two"));
        Assert.assertTrue(values.contains("three"));
    }

    @Test
    public void getAnnotation() {
        Annotation annotation = annotationFinder.getAnnotation(Foo.class, Annotation.class);
        Assert.assertEquals("one", annotation.name());
        Assert.assertEquals("one", annotation.value());
    }

    @Test
    public void hasAnnotation() {
        Assert.assertTrue(annotationFinder.hasAnnotation(Foo.class, Annotation.class));
    }

    @Annotation("one")
    @Annotations({@Annotation("two"), @Annotation("three")})
    private static class Foo {}

    private static class Child extends Foo {}

    @Repeatable(Annotations.class)
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Annotation {
        @AliasFor("name")
        String value() default "";
        @AliasFor("value")
        String name() default "";
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Annotations {
        Annotation[] value() default {};
    }
}
