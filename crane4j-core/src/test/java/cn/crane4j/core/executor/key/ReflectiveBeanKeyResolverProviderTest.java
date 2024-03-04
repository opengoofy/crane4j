package cn.crane4j.core.executor.key;

import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.parser.operation.SimpleAssembleOperation;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * test for {@link ReflectiveBeanKeyResolverProvider}
 *
 * @author huangchengxing
 */
public class ReflectiveBeanKeyResolverProviderTest {

    private ReflectiveBeanKeyResolverProvider provider;

    @Before
    public void init() {
        this.provider = new ReflectiveBeanKeyResolverProvider(new ReflectivePropertyOperator());
    }

    @Test
    public void testWhenKeyTypeCannotInstantiate() {
        AssembleOperation operation = SimpleAssembleOperation.builder()
            .keyType(Source.class)
            .build();
        Assert.assertThrows(Crane4jException.class, () -> provider.getResolver(operation));
    }

    @Test
    public void testWhenNotSpecifiedMapping() {
        AssembleOperation operation = SimpleAssembleOperation.builder()
            .keyType(Target.class)
            .build();

        KeyResolver keyResolver = provider.getResolver(operation);
        Assert.assertNotNull(keyResolver);
        Source source = new Source(1, "test", null, null);
        Object key = keyResolver.resolve(source, operation);
        Assert.assertNotNull(key);
        Assert.assertTrue(key instanceof Target);
        Assert.assertEquals(source.getId(), ((Target)key).getId());
        Assert.assertEquals(source.getName(), ((Target)key).getName());
    }

    @Test
    public void testWhenSpecifiedMapping() {
        AssembleOperation operation = SimpleAssembleOperation.builder()
            .keyType(Target.class)
            .keyDescription("id, name")
            .build();

        KeyResolver keyResolver = provider.getResolver(operation);
        Assert.assertNotNull(keyResolver);
        Source source = new Source(1, "test", null, null);
        Object key = keyResolver.resolve(source, operation);
        Assert.assertNotNull(key);
        Assert.assertTrue(key instanceof Target);
        Assert.assertEquals(source.getId(), ((Target)key).getId());
        Assert.assertEquals(source.getName(), ((Target)key).getName());
    }

    @Test
    public void testWhenSpecifiedDiffMapping() {
        AssembleOperation operation = SimpleAssembleOperation.builder()
            .keyType(Target.class)
            .keyDescription("prop1:id, prop2:name")
            .build();

        KeyResolver keyResolver = provider.getResolver(operation);
        Assert.assertNotNull(keyResolver);
        Source source = new Source(null, null, 1, "test");
        Object key = keyResolver.resolve(source, operation);
        Assert.assertNotNull(key);
        Assert.assertTrue(key instanceof Target);
        Assert.assertEquals(source.getProp1(), ((Target)key).getId());
        Assert.assertEquals(source.getProp2(), ((Target)key).getName());
    }

    @AllArgsConstructor
    @Data
    private static class Source {
        private Integer id;
        private String name;
        private Integer prop1;
        private String prop2;
    }

    @Data
    private static class Target {
        private Integer id;
        private String name;
    }
}
