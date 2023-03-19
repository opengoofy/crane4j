package cn.crane4j.core.executor.handler;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.executor.BaseExecutorTest;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectPropertyOperator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * test for {@link OneToOneReflexAssembleOperationHandler}.
 *
 * @author huangchengxing
 */
public class OneToOneReflexAssembleOperationHandlerTest extends BaseExecutorTest {

    private BeanOperationExecutor executor;

    @Before
    public void init() {
        PropertyOperator operator = new ReflectPropertyOperator();
        OneToOneReflexAssembleOperationHandler handler = new OneToOneReflexAssembleOperationHandler(operator);
        configuration.getAssembleOperationHandlerMap().put(handler.getClass().getName(), handler);
        executor = new DisorderedBeanOperationExecutor();
        Container<Integer> container = LambdaContainer.forLambda(
            "test", ids -> ids.stream().collect(Collectors.toMap(
                Function.identity(), id -> new Bean(id, "name" + id)
            ))
        );
        configuration.getContainerMap().put("test", container);
    }

    @Test
    public void process() {
        BeanOperations operations = parseOperations(Bean.class);
        List<Bean> beanList = Arrays.asList(new Bean(1), new Bean(2), new Bean(3));
        executor.execute(beanList, operations);
        for (int i = 0; i < beanList.size(); i++) {
            Assert.assertEquals("name" + (i + 1), beanList.get(i).getName());
        }
    }

    @RequiredArgsConstructor
    @AllArgsConstructor
    @Data
    private static class Bean {
        @Assemble(
            container = "test", props = @Mapping(src = "name", ref = "name"),
            handler = OneToOneReflexAssembleOperationHandler.class
        )
        private final Integer id;
        private String name;
    }
}
