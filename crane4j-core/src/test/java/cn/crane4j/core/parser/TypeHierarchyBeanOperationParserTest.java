package cn.crane4j.core.parser;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Disassemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.annotation.MappingTemplate;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.executor.handler.OneToOneAssembleOperationHandler;
import cn.crane4j.core.executor.handler.ReflectiveDisassembleOperationHandler;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.parser.operation.DisassembleOperation;
import cn.crane4j.core.parser.operation.TypeDynamitedDisassembleOperation;
import cn.crane4j.core.parser.operation.TypeFixedDisassembleOperation;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ReflectUtils;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * test for {@link TypeHierarchyBeanOperationParser}
 *
 * @author huangchengxing
 */
public class TypeHierarchyBeanOperationParserTest {

    private static final String GROUP = "test";
    private static final String CONTAINER_NAME = "test";
    private static final Container<Object> CONTAINER = LambdaContainer.forLambda(CONTAINER_NAME, keys -> Collections.emptyMap());
    private static final int SUB_SORT = Integer.MIN_VALUE;
    private static final int SUP_SORT = Integer.MAX_VALUE;

    private BeanOperationParser parser;
    private Crane4jGlobalConfiguration configuration;

    @Before
    public void init() {
        configuration = SimpleCrane4jGlobalConfiguration.create();
        configuration.registerContainer(CONTAINER);
        parser = configuration.getBeanOperationsParser(null, BeanOperationParser.class);
        ((TypeHierarchyBeanOperationParser)parser).setEnableHierarchyCache(true);
        Assert.assertFalse(((TypeHierarchyBeanOperationParser)parser).getOperationAnnotationHandlers().isEmpty());
    }

    @Test
    public void parseOther() {
        Method checkNestedBean = ReflectUtils.getDeclaredMethod(this.getClass(), "checkNestedBean", BeanOperations.class, BeanOperations.class);
        Assert.assertNotNull(checkNestedBean);
        Parameter[] parameters = checkNestedBean.getParameters();
        BeanOperations beanOperations = parser.parse(parameters[0]);
        Assert.assertTrue(beanOperations.isEmpty());
    }

    @Test
    public void parse() {
        BeanOperations beanOperations = parser.parse(Bean.class);

        // assemble : key -> value
        Assert.assertEquals(2, beanOperations.getAssembleOperations().size());
        Collection<AssembleOperation> assembles = beanOperations.getAssembleOperations();
        AssembleOperation keyValue = CollectionUtils.get(assembles, 0);
        checkAssembleOperation(keyValue, "key", SUB_SORT);

        // assemble: id -> name
        AssembleOperation idName = CollectionUtils.get(assembles, 1);
        checkAssembleOperation(idName, "id", SUP_SORT);

        // disassemble: nestedBean
        Collection<DisassembleOperation> disassembles = beanOperations.getDisassembleOperations();
        DisassembleOperation nestedBean = CollectionUtils.get(disassembles, 0);
        Assert.assertTrue(nestedBean instanceof TypeFixedDisassembleOperation);
        Assert.assertEquals("nestedBean", nestedBean.getKey());
        Assert.assertEquals(Bean.class, nestedBean.getSourceType());
        checkGroups(nestedBean.getGroups(), GROUP);
        Assert.assertEquals(configuration.getDisassembleOperationHandler(ReflectiveDisassembleOperationHandler.class), nestedBean.getDisassembleOperationHandler());

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
        Assert.assertEquals(2, assembles.size());

        // assemble : key -> value
        AssembleOperation keyValue = CollectionUtils.get(assembles, 0);
        checkAssembleOperation(keyValue, "key", SUB_SORT);

        // assemble: id -> name
        AssembleOperation idName = CollectionUtils.get(assembles, 1);
        checkAssembleOperation(idName, "id", SUP_SORT);

        // disassemble: bean
        Collection<DisassembleOperation> disassembles = nestedBeanOperations.getDisassembleOperations();
        DisassembleOperation bean = CollectionUtils.get(disassembles, 0);
        Assert.assertNotNull(bean);
        if (!(bean instanceof TypeFixedDisassembleOperation)) {
            System.out.println(bean);
            System.out.println(bean.getKey());
            System.out.println(disassembles);
        }
        Assert.assertEquals(bean.getId(), bean.getKey());
        Assert.assertTrue(bean instanceof TypeFixedDisassembleOperation);
        Assert.assertEquals("bean", bean.getKey());
        Assert.assertEquals(NestedBean.class, bean.getSourceType());
        checkGroups(bean.getGroups(), GROUP);
        Assert.assertEquals(configuration.getDisassembleOperationHandler(ReflectiveDisassembleOperationHandler.class), bean.getDisassembleOperationHandler());
        Assert.assertSame(beanOperations, bean.getInternalBeanOperations(null));

        // disassemble: dynamicBean
        DisassembleOperation dynamicBean = CollectionUtils.get(disassembles, 1);
        Assert.assertTrue(dynamicBean instanceof TypeDynamitedDisassembleOperation);
        Assert.assertEquals("dynamicBean", dynamicBean.getKey());
        Assert.assertEquals(NestedBean.class, dynamicBean.getSourceType());
        checkGroups(dynamicBean.getGroups(), GROUP);
        Assert.assertEquals(configuration.getDisassembleOperationHandler(DisassembleOperationHandler.class), dynamicBean.getDisassembleOperationHandler());

        // 运行时再解析类型
        Assert.assertSame(beanOperations, dynamicBean.getInternalBeanOperations(new Bean()));
        Assert.assertSame(nestedBeanOperations, dynamicBean.getInternalBeanOperations(new NestedBean<>()));
    }

    private void checkAssembleOperation(AssembleOperation assembleOperation, String key, int supSort) {
        Assert.assertNotNull(assembleOperation);
        Assert.assertEquals(key, assembleOperation.getId());
        Assert.assertNotNull(assembleOperation);
        Assert.assertEquals(key, assembleOperation.getKey());
        Assert.assertSame(configuration.getAssembleOperationHandler(OneToOneAssembleOperationHandler.class), assembleOperation.getAssembleOperationHandler());
        Assert.assertEquals(CONTAINER.getNamespace(), assembleOperation.getContainer());
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
        PropertyMapping mapping = CollectionUtils.get(mappings, 0);
        Assert.assertNotNull(mapping);
        Assert.assertEquals(ref, mapping.getReference());
        Assert.assertEquals(src, mapping.getSource());
    }

    /**
     * 父类的装配操作将会传递到子类
     */
    @SuppressWarnings("unused")
    private static class BaseBean {
        @Assemble(
            id = "id",
            container = CONTAINER_NAME,
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
    @SuppressWarnings("unused")
    private static class Bean extends BaseBean {
        // 装配操作
        @Assemble(
            id = "key",
            container = CONTAINER_NAME, sort = SUB_SORT,
            groups = GROUP,
            props = @Mapping(src = "name", ref = "name")
        )
        private String key;
        private String value;

        // 拆卸操作，直接指明类型为NestedBean
        @Disassemble(id = "nestedBean", sort = SUB_SORT, type = NestedBean.class, groups = GROUP)
        private NestedBean<?> nestedBean;
    }

    /**
     * 普通对象，可以在类上直接通过@Operations声明操作，
     * 该配置方式完全等同于直接在属性上配置。
     * 由于在@Assemble中指定排序值小于父类中@Assemble的排序值，
     * 因此子类的操作会更优先执行
     */
    @Assemble(
        id = "key",
        key = "key",
        container = CONTAINER_NAME, sort = SUB_SORT,
        groups = GROUP,
        propTemplates = MappingTemp.class
    )
    // 此处嵌套对象类型为确定类型，并且构成循环引用
    @Disassemble(id = "bean", key = "bean", type = Bean.class, sort = SUB_SORT, groups = GROUP)
    // 此处嵌套对象类型为无法确定的泛型，故不指定类型而等到执行时再推断
    @Disassemble(id = "dynamicBean", key = "dynamicBean", groups = GROUP)
    @SuppressWarnings("unused")
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

    // =========== method based ===========

    @Test
    public void testParseMethodBasedOperation() {
        BeanOperations beanOperations = parser.parse(MethodBasedOperationBean.class);
        Collection<AssembleOperation> operations = beanOperations.getAssembleOperations();
        Assert.assertEquals(2, operations.size());

        AssembleOperation op1 = CollectionUtils.get(operations, 0);
        Assert.assertNotNull(op1);
        Assert.assertEquals("getId", op1.getId());
        Assert.assertEquals("getId", op1.getKey());
        Assert.assertEquals("test1", op1.getContainer());

        AssembleOperation op2 = CollectionUtils.get(operations, 1);
        Assert.assertNotNull(op2);
        Assert.assertEquals("takeId", op2.getId());
        Assert.assertEquals("takeId", op2.getKey());
        Assert.assertEquals("test2", op2.getContainer());
    }

    @Assemble(key = "getId", container = "test1", sort = 0)
    @RequiredArgsConstructor
    private static class MethodBasedOperationBean {
        private final Integer i;
        public Integer getId() {
            return i;
        }
        @Assemble(container = "test2", sort = 1)
        public Integer takeId() {
            return i;
        }
    }
}
