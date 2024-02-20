package cn.crane4j.core.parser;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.condition.ConditionOnExpression;
import cn.crane4j.annotation.condition.ConditionOnProperty;
import cn.crane4j.annotation.condition.ConditionType;
import cn.crane4j.core.condition.Condition;
import cn.crane4j.core.condition.ConditionOnExpressionParser;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.parser.operation.AssembleOperation;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.SimpleAnnotationFinder;
import cn.crane4j.core.support.SimpleCrane4jGlobalConfiguration;
import cn.crane4j.core.support.expression.OgnlExpressionContext;
import cn.crane4j.core.support.expression.OgnlExpressionEvaluator;
import cn.crane4j.core.util.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

/**
 * test for {@link ConditionalTypeHierarchyBeanOperationParser}
 *
 * @author huangchengxing
 */
public class ConditionalTypeHierarchyBeanOperationParserTest {

    private static final String CONTAINER_NAME = "test";
    private static final Container<Object> CONTAINER = LambdaContainer.forLambda(CONTAINER_NAME, keys -> Collections.emptyMap());

    private ConditionalTypeHierarchyBeanOperationParser parser;

    @Before
    public void init() {
        Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
        configuration.registerContainer(CONTAINER);
        parser = (ConditionalTypeHierarchyBeanOperationParser)configuration
            .getBeanOperationsParser(null, ConditionalTypeHierarchyBeanOperationParser.class);
        parser.registerConditionParser(new ConditionOnExpressionParser(
            SimpleAnnotationFinder.INSTANCE, new OgnlExpressionEvaluator(), (t, op) -> new OgnlExpressionContext(t)
        ));
    }

    @Test
    public void test() {
        BeanOperations beanOperations = parser.parse(Foo.class);
        Collection<AssembleOperation> assembleOperations = beanOperations.getAssembleOperations();
        Assert.assertEquals(3, assembleOperations.size());

        AssembleOperation operationOfId = CollectionUtils.get(assembleOperations, 0);
        Assert.assertNotNull(operationOfId);
        Condition conditionOfId = operationOfId.getCondition();
        Assert.assertNotNull(conditionOfId);
        Assert.assertTrue(conditionOfId.test(new Foo(1, null, null), operationOfId));
        Assert.assertFalse(conditionOfId.test(new Foo(2, null, null), operationOfId));
        Assert.assertTrue(conditionOfId.test(new Foo(null, null, null), operationOfId));

        AssembleOperation operationOfKey = CollectionUtils.get(assembleOperations, 1);
        Assert.assertNotNull(operationOfKey);
        Condition conditionOfKey = operationOfKey.getCondition();
        Assert.assertNotNull(conditionOfKey);
        Assert.assertTrue(conditionOfKey.test(new Foo(null, 2, null), operationOfKey));
        Assert.assertFalse(conditionOfKey.test(new Foo(null, 3, null), operationOfKey));

        AssembleOperation operationOfCode = CollectionUtils.get(assembleOperations, 2);
        Assert.assertNotNull(operationOfCode);
        Condition conditionOfCode = operationOfCode.getCondition();
        Assert.assertNotNull(conditionOfCode);
        Assert.assertTrue(conditionOfCode.test(new Foo(null, null, 6), operationOfCode));
        Assert.assertFalse(conditionOfCode.test(new Foo(null, null, 7), operationOfCode));
    }

    /**
     * 父类的装配操作将会传递到子类
     */
    @Getter
    @SuppressWarnings("unused")
    @AllArgsConstructor
    private static class Foo {

        @ConditionOnProperty(value = "1", valueType = Integer.class, enableNull = true)
        @Assemble(container = CONTAINER_NAME, sort = 1)
        private Integer id;

        @ConditionOnExpression("key % 2 == 0")
        @Assemble(container = CONTAINER_NAME, sort = 2)
        private Integer key;

        @ConditionOnExpression(value = "#target.code % 3 == 0", sort = 1)
        @ConditionOnExpression(value = "#target.code % 2 == 0", sort = 2, type = ConditionType.AND)
        @Assemble(container = CONTAINER_NAME, sort = 3)
        private Integer code;
    }
}
