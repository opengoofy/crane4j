package cn.createsequence.crane4j.core.container;

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
        Container<String> container = ConstantContainer.forAnnotatedEnum(AnnotatedEnum.class, new SimpleAnnotationFinder());
        Assert.assertEquals(AnnotatedEnum.class.getSimpleName(), container.getNamespace());
        Map<?, ?> data = container.get(null);
        Assert.assertEquals(AnnotatedEnum.ONE.getValue(), data.get(AnnotatedEnum.ONE.getKey()));
        Assert.assertEquals(AnnotatedEnum.TWO.getValue(), data.get(AnnotatedEnum.TWO.getKey()));

        // 没注解
        container = ConstantContainer.forAnnotatedEnum(FooEnum.class, new SimpleAnnotationFinder());
        Assert.assertEquals(FooEnum.class.getSimpleName(), container.getNamespace());
        data = container.get(null);
        Assert.assertEquals(FooEnum.ONE, data.get(FooEnum.ONE.name()));
        Assert.assertEquals(FooEnum.TWO, data.get(FooEnum.TWO.name()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void forAnnotatedEnumWhenDefault() {
        Container<String> container = ConstantContainer.forAnnotatedEnum(DefaultAnnotatedEnum.class, new SimpleAnnotationFinder());
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
}
