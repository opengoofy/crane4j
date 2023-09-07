package cn.crane4j.extension.mybatis.plus;

import cn.crane4j.annotation.AssembleMp;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.MethodInvokerContainer;
import cn.crane4j.core.parser.BeanOperationParser;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.parser.SimpleBeanOperations;
import cn.crane4j.core.parser.handler.strategy.SimplePropertyMappingStrategyManager;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.support.container.MethodInvokerContainerCreator;
import cn.crane4j.core.support.converter.ConverterManager;
import cn.crane4j.core.support.converter.HutoolConverterManager;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import cn.crane4j.core.util.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

/**
 * test for {@link AssembleMpAnnotationHandler}
 *
 * @author huangchengxing
 */
public class AssembleMpAnnotationResolverTest extends MpBaseTest {

    private AssembleMpAnnotationHandler operationsResolver;
    private BeanOperationParser beanOperationParser;
    private Crane4jGlobalConfiguration configuration;

    @Before
    public void afterInit() {
        AnnotationFinder annotationFinder = new SimpleAnnotationFinder();
        configuration = SimpleCrane4jGlobalConfiguration.create();
        beanOperationParser = configuration.getBeanOperationsParser(BeanOperationParser.class);
        ConverterManager converterManager = new HutoolConverterManager();
        MybatisPlusQueryContainerProvider register = new MybatisPlusQueryContainerProvider(
            new MethodInvokerContainerCreator(new ReflectivePropertyOperator(new HutoolConverterManager()), converterManager), configuration
        );
        register.registerRepository("fooMapper", fooMapper);
        operationsResolver = new AssembleMpAnnotationHandler(annotationFinder, register, configuration, new SimplePropertyMappingStrategyManager());
    }

    @Test
    public void resolve() {
        BeanOperations operations = new SimpleBeanOperations(Foo.class);
        operationsResolver.resolve(beanOperationParser, operations);

        Collection<AssembleOperation> assembleOperations = operations.getAssembleOperations();
        Assert.assertEquals(2, assembleOperations.size());

        AssembleOperation idOperation = CollectionUtils.get(assembleOperations, 0);
        Assert.assertNotNull(idOperation);
        Assert.assertEquals("id", idOperation.getKey());
        Assert.assertEquals(1, idOperation.getPropertyMappings().size());
        Container<?> idContainer = configuration.getContainer(idOperation.getContainer());
        Assert.assertTrue(idContainer instanceof MethodInvokerContainer);

        AssembleOperation keyOperation = CollectionUtils.get(assembleOperations, 1);
        Assert.assertNotNull(keyOperation);
        Assert.assertEquals("key", keyOperation.getKey());
        Assert.assertEquals(1, keyOperation.getPropertyMappings().size());
        Container<?> keyContainer = configuration.getContainer(keyOperation.getContainer());
        Assert.assertTrue(keyContainer instanceof MethodInvokerContainer);
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
