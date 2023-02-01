package cn.createsequence.crane4j.core.parser;

import cn.createsequence.crane4j.core.annotation.Assemble;
import cn.createsequence.crane4j.core.annotation.Disassemble;
import cn.createsequence.crane4j.core.annotation.Mapping;
import cn.createsequence.crane4j.core.annotation.MappingTemplate;
import cn.createsequence.crane4j.core.annotation.Operations;
import cn.createsequence.crane4j.core.container.Container;
import cn.createsequence.crane4j.core.container.LambdaContainer;
import cn.createsequence.crane4j.core.executor.AssembleOperationHandler;
import cn.createsequence.crane4j.core.executor.DisassembleOperationHandler;
import cn.createsequence.crane4j.core.executor.ReflectAssembleOperationHandler;
import cn.createsequence.crane4j.core.executor.ReflectDisassembleOperationHandler;
import cn.createsequence.crane4j.core.support.SimpleAnnotationFinder;
import cn.createsequence.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.createsequence.crane4j.core.support.SimpleTypeResolver;
import cn.createsequence.crane4j.core.support.reflect.ReflectPropertyOperator;
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
        configuration.getBeanOperationParserMap().put(parser.getClass(), parser);
        configuration.getAssembleOperationHandlerMap().put(ASSEMBLE_OPERATION_HANDLER.getClass(), ASSEMBLE_OPERATION_HANDLER);
        configuration.getDisassembleOperationHandlerMap().put(DISASSEMBLE_OPERATION_HANDLER.getClass(), DISASSEMBLE_OPERATION_HANDLER);
    }

    @Test
    public void parse() {
        BeanOperations beanOperations = parser.parse(Bean.class);

        // assemble : key -> value
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

        // 获取NestedBean操作配置
        BeanOperations nestedBeanOperations = nestedBean.getInternalBeanOperations(null);
        checkNestedBean(nestedBeanOperations, beanOperations);
    }

    /**
     * 校验 Bean 中的 nestedBean 属性的配置
     */
    private void checkNestedBean(BeanOperations nestedBeanOperations, BeanOperations beanOperations) {
        Assert.assertNotNull(nestedBeanOperations);
        Collection<AssembleOperation> assembles = nestedBeanOperations.getAssembleOperations();

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

        // 运行时再解析类型
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
     * 父类的装配操作将会传递到子类
     */
    private static class BaseBean {
        @Assemble(
            namespace = CONTAINER_NAME,
            groups = GROUP, sort = SUP_SORT,
            props = @Mapping(src = "name", ref = "name")
        )
        private Integer id;
        private String name;
    }

    /**
     * 普通对象，通过在方法上指定一组装配操作和一组拆卸操作，
     * 由于在@Assemble中指定排序值小于父类中@Assemble的排序值，
     * 因此子类的操作会更优先执行
     */
    private static class Bean extends BaseBean {
        // 装配操作
        @Assemble(
            namespace = CONTAINER_NAME, sort = SUB_SORT,
            groups = GROUP,
            props = @Mapping(src = "name", ref = "name")
        )
        private String key;
        private String value;

        // 拆卸操作，直接指明类型为NestedBean
        @Disassemble(sort = SUB_SORT, type = NestedBean.class, groups = GROUP)
        private NestedBean<?> nestedBean;
    }

    /**
     * 普通对象，可以在类上直接通过@Operations声明操作，
     * 该配置方式完全等同于直接在属性上配置。
     * 由于在@Assemble中指定排序值小于父类中@Assemble的排序值，
     * 因此子类的操作会更优先执行
     */
    @Operations(
        assembles = @Assemble(
            key = "key",
            namespace = CONTAINER_NAME, sort = SUB_SORT,
            groups = GROUP,
            propTemplates = MappingTemp.class
        ),

        // 拆卸操作
        disassembles = {
            // 此处嵌套对象类型为确定类型，并且构成循环引用
            @Disassemble(key = "bean", type = Bean.class, sort = SUB_SORT, groups = GROUP),
            // 此处嵌套对象类型为无法确定的泛型，故不指定类型而等到执行时再推断
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
     * 属性映射模板，用于在@Assemble中引用，
     * 效果等同于直接写在@Assemble注解中
     */
    @MappingTemplate(@Mapping(src = "name", ref = "name"))
    private static class MappingTemp {}
}
