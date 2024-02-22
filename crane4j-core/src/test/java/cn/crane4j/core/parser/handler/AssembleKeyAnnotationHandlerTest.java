package cn.crane4j.core.parser.handler;

import cn.crane4j.annotation.AssembleKey;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.SimpleBeanOperations;
import cn.crane4j.core.parser.handler.strategy.OverwriteMappingStrategy;
import cn.crane4j.core.parser.handler.strategy.PropertyMappingStrategyManager;
import cn.crane4j.core.parser.handler.strategy.SimplePropertyMappingStrategyManager;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.util.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * test for {@link AssembleKeyAnnotationHandler}.
 *
 * @author huangchengxing
 */
public class AssembleKeyAnnotationHandlerTest {

    private AssembleKeyAnnotationHandler annotationHandler;
    private Crane4jGlobalConfiguration configuration;

    @Before
    public void init() {
        configuration = SimpleCrane4jGlobalConfiguration.create();
        PropertyMappingStrategyManager propertyMappingStrategyManager = new SimplePropertyMappingStrategyManager();
        propertyMappingStrategyManager.addPropertyMappingStrategy(OverwriteMappingStrategy.INSTANCE);
        this.annotationHandler = new AssembleKeyAnnotationHandler(
            SimpleAnnotationFinder.INSTANCE, configuration, propertyMappingStrategyManager
        );
        this.annotationHandler.registerValueMapperProvider("test1", e -> kv -> kv + "test1");
        this.annotationHandler.registerValueMapperProvider("test2", e -> kv -> kv + "test2");
    }

    @Test
    public void resolve() {
        BeanOperations beanOperations = new SimpleBeanOperations(Foo.class);
        annotationHandler.resolve(null, beanOperations);

        Collection<AssembleOperation> operations = beanOperations.getAssembleOperations();
        Assert.assertEquals(2, operations.size());

        AssembleOperation operation1 = CollectionUtils.get(operations, 0);
        Assert.assertNotNull(operation1);
        Assert.assertEquals("k1", operation1.getKey());
        Set<PropertyMapping> mappings = operation1.getPropertyMappings();
        Assert.assertEquals(1, mappings.size());
        PropertyMapping mapping = CollectionUtils.get(mappings, 0);
        Assert.assertNotNull(mapping);
        Assert.assertFalse(mapping.hasSource());
        Assert.assertEquals("k1", mapping.getReference());
        Container<String> container = configuration.getContainer(operation1.getContainer());
        Assert.assertNotNull(container);
        Map<String, ?> sources = container.get(Arrays.asList("a", "b"));
        Assert.assertEquals("atest1", sources.get("a"));
        Assert.assertEquals("btest1", sources.get("b"));

        AssembleOperation operation2 = CollectionUtils.get(operations, 1);
        Assert.assertEquals("k1", operation2.getKey());
        Assert.assertNotNull(operation2);
        mappings = operation2.getPropertyMappings();
        Assert.assertEquals(1, mappings.size());
        mapping = CollectionUtils.get(mappings, 0);
        Assert.assertNotNull(mapping);
        Assert.assertFalse(mapping.hasSource());
        Assert.assertEquals("k2", mapping.getReference());
        container = configuration.getContainer(operation2.getContainer());
        Assert.assertNotNull(container);
        sources = container.get(Arrays.asList("a", "b"));
        Assert.assertEquals("atest2", sources.get("a"));
        Assert.assertEquals("btest2", sources.get("b"));
    }

    @AssembleKey(key = "k1", mapper = "test2", sort = 2, ref = "k2")
    @AllArgsConstructor
    @Data
    private static class Foo {
        @AssembleKey(mapper = "test1", sort = 1)
        private String k1;
        private String k2;
    }
}
