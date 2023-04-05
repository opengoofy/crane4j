package cn.crane4j.extension.mybatis.plus;

import cn.crane4j.annotation.AssembleMp;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.parser.AssembleOperation;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.OperationParseContext;
import cn.crane4j.core.parser.SimpleBeanOperations;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.support.reflect.ReflectPropertyOperator;
import cn.hutool.core.collection.CollUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

/**
 * test for {@link MpAnnotationOperationsResolver}
 *
 * @author huangchengxing
 */
public class MpAnnotationOperationsResolverTest extends MpBaseTest {

    private MpAnnotationOperationsResolver operationsResolver;

    @Before
    public void afterInit() {
        AnnotationFinder annotationFinder = new SimpleAnnotationFinder();
        Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create(Collections.emptyMap());
        MpBaseMapperContainerRegister register = new MpBaseMapperContainerRegister(configuration, new ReflectPropertyOperator());
        register.registerMapper("fooMapper", fooMapper);
        operationsResolver = new MpAnnotationOperationsResolver(annotationFinder, register, configuration);
    }

    @Test
    public void resolve() {
        BeanOperations operations = new SimpleBeanOperations(Foo.class);
        OperationParseContext context = new OperationParseContext(operations, t -> operations);
        operationsResolver.resolve(context, Foo.class);

        Collection<AssembleOperation> assembleOperations = operations.getAssembleOperations();
        Assert.assertEquals(2, assembleOperations.size());

        AssembleOperation idOperation = CollUtil.get(assembleOperations, 0);
        Assert.assertEquals("id", idOperation.getKey());
        Assert.assertEquals(1, idOperation.getPropertyMappings().size());
        Container<?> idContainer = idOperation.getContainer();
        Assert.assertTrue(idContainer instanceof MpMethodContainer);

        AssembleOperation keyOperation = CollUtil.get(assembleOperations, 1);
        Assert.assertEquals("key", keyOperation.getKey());
        Assert.assertEquals(1, keyOperation.getPropertyMappings().size());
        Container<?> keyContainer = keyOperation.getContainer();
        Assert.assertTrue(keyContainer instanceof MpMethodContainer);
    }

    @AssembleMp(
        key = "key",
        mapper = "fooMapper", selects = "userAge", where = "id",
        props = @Mapping(src = "userAge", ref = "age"),
        sort = 2
    )
    private static class Foo {
        @AssembleMp(
            mapper = "fooMapper", selects = "userName", where = "id",
            props = @Mapping(src = "userName", ref = "name"),
            sort = 1
        )
        private Integer id;
        private Integer key;
        private String name;
        private String age;
    }
}
