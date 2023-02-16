package cn.crane4j.core.container;

import cn.crane4j.core.annotation.MappingType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * test for {@link MethodInvokerContainer}
 *
 * @author huangchengxing
 */
public class MethodInvokerContainerTest {

    private static final Service service = new Service();
    private static final Foo foo1 = new Foo("1", "foo");
    private static final Foo foo2 = new Foo("2", "foo");

    @SuppressWarnings("unchecked")
    @Test
    public void getWhenMapped() {
        MethodInvokerContainer container = new MethodInvokerContainer(
            MethodInvokerContainer.class.getSimpleName(),
            (t, arg) -> service.mappedMethod((Collection<String>)arg[0]),
            service, null, MappingType.MAPPED
        );
        Assert.assertEquals(MethodInvokerContainer.class.getSimpleName(), container.getNamespace());

        Map<Object, ?> data = container.get(Collections.singletonList(foo1.key));
        Assert.assertEquals(foo1, data.get(foo1.key));

        data = container.get(null);
        Assert.assertTrue(data.isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getWhenOneToOne() {
        MethodInvokerContainer container = new MethodInvokerContainer(
            MethodInvokerContainer.class.getSimpleName(),
            (t, arg) -> service.noneMappedMethod((Collection<String>)arg[0]),
            service, t -> ((Foo) t).key, MappingType.ONE_TO_ONE
        );
        Assert.assertEquals(MethodInvokerContainer.class.getSimpleName(), container.getNamespace());

        Map<Object, ?> data = container.get(Collections.singletonList(foo1.key));
        Assert.assertEquals(foo1, data.get(foo1.key));

        data = container.get(null);
        Assert.assertTrue(data.isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getWhenOneToMany() {
        MethodInvokerContainer container = new MethodInvokerContainer(
            MethodInvokerContainer.class.getSimpleName(),
            (t, arg) -> service.noneMappedMethod((Collection<String>)arg[0]),
            service, t -> ((Foo) t).name, MappingType.ONE_TO_MANY
        );
        Assert.assertEquals(MethodInvokerContainer.class.getSimpleName(), container.getNamespace());

        Map<Object, ?> data = container.get(Collections.singletonList(foo1.name));
        Assert.assertEquals(Arrays.asList(foo1, foo2), data.get(foo1.name));

        data = container.get(null);
        Assert.assertTrue(data.isEmpty());
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    private static class Foo {
        private String key;
        private String name;
    }

    private static class Service {
        public Map<String, Foo> mappedMethod(Collection<String> key) {
            return Objects.isNull(key) ? null : Stream.of(foo1, foo2).collect(Collectors.toMap(Foo::getKey, Function.identity()));
        }
        public List<Foo> noneMappedMethod(Collection<String> key) {
            return Objects.isNull(key) ? null : Arrays.asList(foo1, foo2);
        }
    }
}
