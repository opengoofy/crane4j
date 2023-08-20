package cn.crane4j.core.parser.handler;

import cn.crane4j.annotation.AssembleEnum;
import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.PropertyMapping;
import cn.crane4j.core.parser.SimpleBeanOperations;
import cn.crane4j.core.parser.handler.strategy.OverwriteMappingStrategy;
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
 * test for {@link AssembleEnumAnnotationHandler}.
 *
 * @author huangchengxing
 */
public class AssembleEnumAnnotationHandlerTest {

    private AssembleEnumAnnotationHandler annotationHandler;
    private Crane4jGlobalConfiguration configuration;

    @Before
    public void init() {
        configuration = SimpleCrane4jGlobalConfiguration.create();
        this.annotationHandler = new AssembleEnumAnnotationHandler(
            new SimpleAnnotationFinder(), configuration, configuration.getPropertyOperator(), configuration
        );
        this.annotationHandler.addPropertyMappingStrategy(OverwriteMappingStrategy.INSTANCE);
    }

    @Test
    public void resolve() {
        BeanOperations beanOperations = new SimpleBeanOperations(Foo.class);
        annotationHandler.resolve(null, beanOperations);

        Collection<AssembleOperation> operations = beanOperations.getAssembleOperations();
        Assert.assertEquals(2, operations.size());

        AssembleOperation operationOfCnName = CollectionUtils.get(operations, 0);
        Assert.assertEquals("id", operationOfCnName.getKey());
        Set<PropertyMapping> mappings = operationOfCnName.getPropertyMappings();
        Assert.assertEquals(1, mappings.size());
        PropertyMapping mapping = CollectionUtils.get(mappings, 0);
        Assert.assertFalse(mapping.hasSource());
        Assert.assertEquals("cnName", mapping.getReference());
        Container<Integer> container = configuration.getContainer(operationOfCnName.getContainer());
        Map<Integer, ?> sources = container.get(Arrays.asList(0, 1));
        Assert.assertEquals("女", sources.get(0));
        Assert.assertEquals("男", sources.get(1));

        AssembleOperation operationOfEnName = CollectionUtils.get(operations, 1);
        Assert.assertEquals("id", operationOfEnName.getKey());
        mappings = operationOfEnName.getPropertyMappings();
        Assert.assertEquals(1, mappings.size());
        mapping = CollectionUtils.get(mappings, 0);
        Assert.assertFalse(mapping.hasSource());
        Assert.assertEquals("enName", mapping.getReference());
        container = configuration.getContainer(operationOfEnName.getContainer());
        sources = container.get(Arrays.asList(0, 1));
        Assert.assertEquals("female", sources.get(0));
        Assert.assertEquals("male", sources.get(1));
    }

    @RequiredArgsConstructor
    @Data
    private static class Foo {
        @AssembleEnum(type = Gender.class, useContainerEnum = false, enumKey = "code", enumValue = "cnName", ref = "cnName", sort = 1)
        @AssembleEnum(type = Gender.class, ref = "enName", sort = 2)
        private final Integer id;
        private String cnName;
        private String enName;
    }

    @ContainerEnum(namespace = "gender", key = "code", value = "enName")
    @Getter
    @RequiredArgsConstructor
    private enum Gender {
        FEMALE(0, "女", "female"), MALE(1, "男", "male");
        private final Integer code;
        private final String cnName;
        private final String enName;
    }
}
