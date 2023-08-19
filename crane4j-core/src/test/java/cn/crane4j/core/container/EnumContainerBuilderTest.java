package cn.crane4j.core.container;

import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.converter.HutoolConverterManager;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * test for {@link EnumContainerBuilder}
 *
 * @author tangcent
 */
public class EnumContainerBuilderTest {

    @SuppressWarnings("unchecked")
    @Test
    public void nonAnnotatedEnumDefault() {
        Container<Object> container = EnumContainerBuilder.of(FooEnum.class)
            .build();
        Assert.assertEquals(FooEnum.class.getSimpleName(), container.getNamespace());

        Map<Object, FooEnum> data = (Map<Object, FooEnum>) container.get(null);
        Assert.assertEquals(FooEnum.ONE, data.get(FooEnum.ONE.name()));
        Assert.assertEquals(FooEnum.TWO, data.get(FooEnum.TWO.name()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void nonAnnotatedEnum() {
        EnumContainerBuilder<?, FooEnum> builder = EnumContainerBuilder.of(FooEnum.class);
        Assert.assertThrows(Crane4jException.class, () -> builder.key(""));
        Assert.assertThrows(Crane4jException.class, () -> builder.value(""));

        Container<Integer> container = EnumContainerBuilder.of(FooEnum.class)
            .namespace("test")
            .keyGetter(FooEnum::ordinal)
            .valueGetter(FooEnum::name)
            .build();
        Assert.assertEquals("test", container.getNamespace());

        Map<Integer, String> data = (Map<Integer, String>) container.get(null);
        Assert.assertEquals(FooEnum.ONE.name(), data.get(0));
        Assert.assertEquals(FooEnum.TWO.name(), data.get(1));
    }

    @Test
    public void annotatedEnumDefault() {
        // annotated
        Container<Object> container = EnumContainerBuilder.of(AnnotatedEnum.class)
            .enableContainerEnumAnnotation(true)
            .annotationFinder(new SimpleAnnotationFinder())
            .propertyOperator(new ReflectivePropertyOperator(new HutoolConverterManager()))
            .build();
        Assert.assertEquals(AnnotatedEnum.class.getSimpleName(), container.getNamespace());
        Map<?, ?> data = container.get(null);
        Assert.assertEquals(AnnotatedEnum.ONE.getValue(), data.get(AnnotatedEnum.ONE.getKey()));
        Assert.assertEquals(AnnotatedEnum.TWO.getValue(), data.get(AnnotatedEnum.TWO.getKey()));
    }

    @Test
    public void annotatedEnum() {
        // annotated
        Container<String> container = EnumContainerBuilder.of(AnnotatedEnum.class)
            .namespace("test")
            .annotationFinder(new SimpleAnnotationFinder())
            .propertyOperator(new ReflectivePropertyOperator(new HutoolConverterManager()))
            .keyGetter(AnnotatedEnum::getValue)
            .valueGetter(AnnotatedEnum::getKey)
            .build();

        Assert.assertEquals("test", container.getNamespace());
        Map<?, ?> data = container.get(null);
        Assert.assertEquals(AnnotatedEnum.ONE.getKey(), data.get(AnnotatedEnum.ONE.getValue()));
        Assert.assertEquals(AnnotatedEnum.TWO.getKey(), data.get(AnnotatedEnum.TWO.getValue()));
    }

    @Test
    public void annotatedEnumWithSpecialKeyValue() {
        // annotated
        Container<Object> container = EnumContainerBuilder.of(AnnotatedEnum.class)
            .namespace("test")
            .annotationFinder(new SimpleAnnotationFinder())
            .propertyOperator(new ReflectivePropertyOperator(new HutoolConverterManager()))
            .key("value")
            .value("key")
            .build();

        Assert.assertEquals("test", container.getNamespace());
        Map<?, ?> data = container.get(null);
        Assert.assertEquals(AnnotatedEnum.ONE.getKey(), data.get(AnnotatedEnum.ONE.getValue()));
        Assert.assertEquals(AnnotatedEnum.TWO.getKey(), data.get(AnnotatedEnum.TWO.getValue()));
    }


    @Getter
    private enum FooEnum {
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
}
