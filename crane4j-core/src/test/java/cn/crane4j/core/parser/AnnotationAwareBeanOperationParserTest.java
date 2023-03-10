package cn.crane4j.core.parser;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Disassemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.annotation.MappingTemplate;
import cn.crane4j.annotation.Operations;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectDisassembleOperationHandler;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleTypeResolver;
import cn.crane4j.core.support.reflect.ReflectPropertyOperator;
import cn.hutool.core.collection.CollUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * test for {@link AnnotationAwareBeanOperationParser}
 *
 * @author huangchengxing
 */
public class AnnotationAwareBeanOperationParserTest {

    private static final String GROUP = "test";
    private static final String CONTAINER_NAME = "test";
    private static final Container<Object> CONTAINER = LambdaContainer.forLambda(CONTAINER_NAME, keys -> Collections.emptyMap());
    private static final int SUB_SORT = Integer.MIN_VALUE;
    private static final int SUP_SORT = Integer.MAX_VALUE;
    private static final AssembleOperationHandler ASSEMBLE_OPERATION_HANDLER = new ReflectAssembleOperationHandler(new ReflectPropertyOperator());
    private static final DisassembleOperationHandler DISASSEMBLE_OPERATION_HANDLER = new ReflectDisassembleOperationHandler(
        new ReflectPropertyOperator()
    );

    private AnnotationAwareBeanOperationParser parser;

    @Before
    public void init() {
        SimpleCrane4jGlobalConfiguration configuration = new SimpleCrane4jGlobalConfiguration();
        parser = new AnnotationAwareBeanOperationParser(
            new SimpleAnnotationFinder(), configuration
        );
        configuration.setTypeResolver(new SimpleTypeResolver());
        configuration.getContainerMap().put(CONTAINER.getNamespace(), CONTAINER);

        configuration.getBeanOperationParserMap().put(parser.getClass().getName(), parser);
        configuration.getBeanOperationParserMap().put(BeanOperationParser.class.getName(), parser);

        configuration.getAssembleOperationHandlerMap().put(ASSEMBLE_OPERATION_HANDLER.getClass().getName(), ASSEMBLE_OPERATION_HANDLER);
        configuration.getAssembleOperationHandlerMap().put(AssembleOperationHandler.class.getName(), ASSEMBLE_OPERATION_HANDLER);

        configuration.getDisassembleOperationHandlerMap().put(DISASSEMBLE_OPERATION_HANDLER.getClass().getName(), DISASSEMBLE_OPERATION_HANDLER);
        configuration.getDisassembleOperationHandlerMap().put(DisassembleOperationHandler.class.getName(), DISASSEMBLE_OPERATION_HANDLER);
    }

    @Test
    public void parse() {
        BeanOperations beanOperations = parser.parse(Bean.class);

        // assemble : key -> value
        Assert.assertEquals(2, beanOperations.getAssembleOperations().size());
        Collection<AssembleOperation> assembles = beanOperations.getAssembleOperations();
        AssembleOperation keyValue = CollUtil.get(assembles, 0);
        checkAssembleOperation(keyValue, "key", SUB_SORT);

        // assemble: id -> name
        AssembleOperation idName = CollUtil.get(assembles, 1);
        checkAssembleOperation(idName, "id", SUP_SORT);

        // disassemble: nestedBean
        Collection<DisassembleOperation> disassembles = beanOperations.getDisassembleOperations();
        DisassembleOperation nestedBean = CollUtil.get(disassembles, 0);
        Assert.assertTrue(nestedBean instanceof TypeFixedDisassembleOperation);
        Assert.assertEquals("nestedBean", nestedBean.getKey());
        Assert.assertEquals(Bean.class, nestedBean.getSourceType());
        checkGroups(nestedBean.getGroups(), GROUP);
        Assert.assertEquals(DISASSEMBLE_OPERATION_HANDLER, nestedBean.getDisassembleOperationHandler());

        // ??????NestedBean????????????
        BeanOperations nestedBeanOperations = nestedBean.getInternalBeanOperations(null);
        checkNestedBean(nestedBeanOperations, beanOperations);
    }

    /**
     * ?????? Bean ?????? nestedBean ???????????????
     */
    private void checkNestedBean(BeanOperations nestedBeanOperations, BeanOperations beanOperations) {
        Assert.assertNotNull(nestedBeanOperations);
        Collection<AssembleOperation> assembles = nestedBeanOperations.getAssembleOperations();
        Assert.assertEquals(2, beanOperations.getAssembleOperations().size());

        // assemble : key -> value
        AssembleOperation keyValue = CollUtil.get(assembles, 0);
        checkAssembleOperation(keyValue, "key", SUB_SORT);

        // assemble: id -> name
        AssembleOperation idName = CollUtil.get(assembles, 1);
        checkAssembleOperation(idName, "id", SUP_SORT);

        // disassemble: bean
        Collection<DisassembleOperation> disassembles = nestedBeanOperations.getDisassembleOperations();
        DisassembleOperation bean = CollUtil.get(disassembles, 0);
        Assert.assertTrue(bean instanceof TypeFixedDisassembleOperation);
        Assert.assertEquals("bean", bean.getKey());
        Assert.assertEquals(NestedBean.class, bean.getSourceType());
        checkGroups(bean.getGroups(), GROUP);
        Assert.assertEquals(DISASSEMBLE_OPERATION_HANDLER, bean.getDisassembleOperationHandler());
        Assert.assertSame(beanOperations, bean.getInternalBeanOperations(null));

        // disassemble: dynamicBean
        DisassembleOperation dynamicBean = CollUtil.get(disassembles, 1);
        Assert.assertTrue(dynamicBean instanceof TypeDynamitedDisassembleOperation);
        Assert.assertEquals("dynamicBean", dynamicBean.getKey());
        Assert.assertEquals(NestedBean.class, dynamicBean.getSourceType());
        checkGroups(dynamicBean.getGroups(), GROUP);
        Assert.assertEquals(DISASSEMBLE_OPERATION_HANDLER, dynamicBean.getDisassembleOperationHandler());

        // ????????????????????????
        Assert.assertSame(beanOperations, dynamicBean.getInternalBeanOperations(new Bean()));
        Assert.assertSame(nestedBeanOperations, dynamicBean.getInternalBeanOperations(new NestedBean<>()));
    }

    private void checkAssembleOperation(AssembleOperation assembleOperation, String id, int supSort) {
        Assert.assertNotNull(assembleOperation);
        Assert.assertEquals(id, assembleOperation.getKey());
        Assert.assertSame(ASSEMBLE_OPERATION_HANDLER, assembleOperation.getAssembleOperationHandler());
        Assert.assertSame(CONTAINER, assembleOperation.getContainer());
        Assert.assertEquals(supSort, assembleOperation.getSort());
        checkGroups(assembleOperation.getGroups(), GROUP);
        checkPropertyMappings(assembleOperation.getPropertyMappings(), "name", "name");
    }

    private void checkGroups(Set<String> groups, String... expectedGroups) {
        Set<String> expected = Stream.of(expectedGroups).collect(Collectors.toCollection(LinkedHashSet::new));
        Assert.assertEquals(groups, expected);
    }

    private void checkPropertyMappings(Set<PropertyMapping> mappings, String ref, String src) {
        Assert.assertEquals(1, mappings.size());
        PropertyMapping mapping = CollUtil.getFirst(mappings);
        Assert.assertEquals(ref, mapping.getReference());
        Assert.assertEquals(src, mapping.getSource());
    }

    /**
     * ??????????????????????????????????????????
     */
    private static class BaseBean {
        @Assemble(
            container = CONTAINER_NAME,
            groups = GROUP, sort = SUP_SORT,
            props = @Mapping(src = "name", ref = "name")
        )
        private Integer id;
        private String name;
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????
     * ?????????@Assemble?????????????????????????????????@Assemble???????????????
     * ???????????????????????????????????????
     */
    private static class Bean extends BaseBean {
        // ????????????
        @Assemble(
            container = CONTAINER_NAME, sort = SUB_SORT,
            groups = GROUP,
            props = @Mapping(src = "name", ref = "name")
        )
        private String key;
        private String value;

        // ????????????????????????????????????NestedBean
        @Disassemble(sort = SUB_SORT, type = NestedBean.class, groups = GROUP)
        private NestedBean<?> nestedBean;
    }

    /**
     * ??????????????????????????????????????????@Operations???????????????
     * ?????????????????????????????????????????????????????????
     * ?????????@Assemble?????????????????????????????????@Assemble???????????????
     * ???????????????????????????????????????
     */
    @Operations(
        assembles = @Assemble(
            key = "key",
            container = CONTAINER_NAME, sort = SUB_SORT,
            groups = GROUP,
            propTemplates = MappingTemp.class
        ),

        // ????????????
        disassembles = {
            // ??????????????????????????????????????????????????????????????????
            @Disassemble(key = "bean", type = Bean.class, sort = SUB_SORT, groups = GROUP),
            // ????????????????????????????????????????????????????????????????????????????????????????????????
            @Disassemble(key = "dynamicBean", sort = SUB_SORT, groups = GROUP)
        }
    )
    private static class NestedBean<T> extends BaseBean {
        private String key;
        private String value;
        private Bean bean;
        private T dynamicBean;
    }

    /**
     * ??????????????????????????????@Assemble????????????
     * ???????????????????????????@Assemble?????????
     */
    @MappingTemplate(@Mapping(src = "name", ref = "name"))
    private static class MappingTemp {}
}
