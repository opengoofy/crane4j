package cn.crane4j.core.parser;

import cn.crane4j.annotation.AssembleEnum;
import cn.crane4j.annotation.ContainerEnum;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.hutool.core.collection.CollUtil;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * test for {@link AssembleEnumAnnotationResolver}.
 *
 * @author huangchengxing
 */
public class AssembleEnumAnnotationResolverTest {

    private AssembleEnumAnnotationResolver annotationResolver;

    @Before
    public void init() {
        Crane4jGlobalConfiguration globalConfiguration = SimpleCrane4jGlobalConfiguration.create(Collections.emptyMap());
        this.annotationResolver = new AssembleEnumAnnotationResolver(
            new SimpleAnnotationFinder(), globalConfiguration, globalConfiguration.getPropertyOperator(), globalConfiguration
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void resolve() {
        BeanOperations beanOperations = new SimpleBeanOperations(Foo.class);
        annotationResolver.resolve(null, beanOperations);

        Collection<AssembleOperation> operations = beanOperations.getAssembleOperations();
        Assert.assertEquals(2, operations.size());

        AssembleOperation operationOfCnName = CollUtil.get(operations, 0);
        Assert.assertEquals("id", operationOfCnName.getKey());
        Set<PropertyMapping> mappings = operationOfCnName.getPropertyMappings();
        Assert.assertEquals(1, mappings.size());
        PropertyMapping mapping = CollUtil.get(mappings, 0);
        Assert.assertFalse(mapping.hasSource());
        Assert.assertEquals("cnName", mapping.getReference());
        Container<Integer> container = (Container<Integer>)operationOfCnName.getContainer();
        Map<Integer, ?> sources = container.get(Arrays.asList(0, 1));
        Assert.assertEquals("女", sources.get(0));
        Assert.assertEquals("男", sources.get(1));

        AssembleOperation operationOfEnName = CollUtil.get(operations, 1);
        Assert.assertEquals("id", operationOfEnName.getKey());
        mappings = operationOfEnName.getPropertyMappings();
        Assert.assertEquals(1, mappings.size());
        mapping = CollUtil.get(mappings, 0);
        Assert.assertFalse(mapping.hasSource());
        Assert.assertEquals("enName", mapping.getReference());
        container = (Container<Integer>)operationOfEnName.getContainer();
        sources = container.get(Arrays.asList(0, 1));
        Assert.assertEquals("female", sources.get(0));
        Assert.assertEquals("male", sources.get(1));
    }

    @RequiredArgsConstructor
    @Data
    private static class Foo {
        @AssembleEnum(type = Gender.class, enumKey = "code", enumValue = "cnName", ref = "cnName", sort = 1)
        @AssembleEnum(type = Gender.class, useContainerEnum = true, ref = "enName", sort = 2)
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
