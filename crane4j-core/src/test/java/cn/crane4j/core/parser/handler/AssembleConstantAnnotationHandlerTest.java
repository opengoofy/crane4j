package cn.crane4j.core.parser.handler;

import cn.crane4j.annotation.AssembleConstant;
import cn.crane4j.annotation.ContainerConstant;
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
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * test for {@link AssembleConstantAnnotationHandler}.
 *
 * @author huangchengxing
 */
public class AssembleConstantAnnotationHandlerTest {

    private AssembleConstantAnnotationHandler annotationHandler;
    private Crane4jGlobalConfiguration configuration;

    @Before
    public void init() {
        configuration = SimpleCrane4jGlobalConfiguration.create();
        PropertyMappingStrategyManager propertyMappingStrategyManager = new SimplePropertyMappingStrategyManager();
        propertyMappingStrategyManager.addPropertyMappingStrategy(OverwriteMappingStrategy.INSTANCE);
        this.annotationHandler = new AssembleConstantAnnotationHandler(
            new SimpleAnnotationFinder(), configuration, propertyMappingStrategyManager
        );
    }

    @Test
    public void resolve() {
        BeanOperations beanOperations = new SimpleBeanOperations(Foo.class);
        annotationHandler.resolve(null, beanOperations);

        Collection<AssembleOperation> operations = beanOperations.getAssembleOperations();
        Assert.assertEquals(2, operations.size());

        AssembleOperation reversedOperation = CollectionUtils.get(operations, 0);
        Assert.assertEquals("code", reversedOperation.getKey());
        Set<PropertyMapping> mappings = reversedOperation.getPropertyMappings();
        Assert.assertEquals(1, mappings.size());
        PropertyMapping mapping = CollectionUtils.get(mappings, 0);
        Assert.assertFalse(mapping.hasSource());
        Assert.assertEquals("value", mapping.getReference());
        Container<String> container = configuration.getContainer(reversedOperation.getContainer());
        Assert.assertNotNull(container);
        Map<String, ?> sources = container.get(Arrays.asList("女", "男"));
        Assert.assertEquals("FEMALE", sources.get("女"));
        Assert.assertEquals("MALE", sources.get("男"));

        AssembleOperation operation = CollectionUtils.get(operations, 1);
        Assert.assertEquals("code", operation.getKey());
        mappings = operation.getPropertyMappings();
        Assert.assertEquals(1, mappings.size());
        mapping = CollectionUtils.get(mappings, 0);
        Assert.assertFalse(mapping.hasSource());
        Assert.assertEquals("value", mapping.getReference());
        container = configuration.getContainer(operation.getContainer());
        Assert.assertNotNull(container);
        sources = container.get(Arrays.asList("FEMALE", "MALE"));
        Assert.assertEquals("女", sources.get("FEMALE"));
        Assert.assertEquals("男", sources.get("MALE"));
    }

    @RequiredArgsConstructor
    @Data
    private static class Foo {
        @AssembleConstant(
            type = Gender.class, ref = "value",
            followTypeConfig = false, constant = @ContainerConstant(reverse = true),
            sort = 1
        )
        @AssembleConstant(
            typeName = "cn.crane4j.core.parser.handler.AssembleConstantAnnotationHandlerTest$Gender",
            sort = 2, ref = "value"
        )
        private String code;
        private String value;
    }

    @ContainerConstant(namespace = "gender")
    @Getter
    @RequiredArgsConstructor
    private static class Gender {
        public static final String FEMALE = "女";
        public static final String MALE = "男";
    }
}
