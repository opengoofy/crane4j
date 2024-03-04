package cn.crane4j.core.executor.key;

import cn.crane4j.core.parser.operation.SimpleAssembleOperation;
import cn.crane4j.core.support.converter.SimpleConverterManager;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * test for {@link ReflectivePropertyKeyResolverProvider}
 *
 * @author huangchengxing
 */
public class ReflectivePropertyKeyResolverProviderTest {

    private ReflectivePropertyKeyResolverProvider provider;

    @Before
    public void inti() {
        this.provider = new ReflectivePropertyKeyResolverProvider(
            new ReflectivePropertyOperator(), SimpleConverterManager.INSTANCE
        );
    }

    @Test
    public void test() {
        SimpleAssembleOperation operation = SimpleAssembleOperation.builder()
            .key("key")
            .build();
        Foo foo = new Foo("test");
        KeyResolver resolver = provider.getResolver(operation);
        Assert.assertNotNull(resolver);
        Object key = resolver.resolve(foo, operation);
        Assert.assertEquals(foo.getKey(), key);

        operation = SimpleAssembleOperation.builder()
            .key("key")
            .keyType(String.class)
            .build();
        resolver = provider.getResolver(operation);
        Assert.assertNotNull(resolver);
        key = resolver.resolve(foo, operation);
        Assert.assertEquals(foo.getKey(), key);
    }

    @AllArgsConstructor
    @Data
    private static class Foo {
        private String key;
    }
}
