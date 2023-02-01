package cn.createsequence.crane4j.core.support;

import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

/**
 * test for {@link SimpleAnnotationFinder}
 *
 * @author huangchengxing
 */
public class SimpleAnnotationFinderTest {

    private final AnnotationFinder finder = new SimpleAnnotationFinder();

    @Test
    public void findAnnotation() {
        Assert.assertNotNull(finder.findAnnotation(Foo.class, Annotation.class));
    }

    @Test
    public void findAllAnnotations() {
        Set<Annotation> annotations = finder.findAllAnnotations(Foo.class, Annotation.class);
        Assert.assertNotNull(annotations);
        Assert.assertEquals(3, annotations.size());
    }

    @AnnotationList({@Annotation(1), @Annotation(2)})
    @Annotation(3)
    private static class Foo {}

    @Repeatable(AnnotationList.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    private @interface Annotation {
        int value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    private @interface AnnotationList {
        Annotation[] value();
    }

}
