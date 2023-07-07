package cn.crane4j.core.executor.handler;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.executor.BaseExecutorTest;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.converter.HutoolConverterManager;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * test for {@link OneToOneAssembleOperationHandler}.
 *
 * @author huangchengxing
 */
public class OneToOneReflexAssembleOperationHandlerTest extends BaseExecutorTest {

    private BeanOperationExecutor executor;

    @Before
    public void init() {
        PropertyOperator operator = new ReflectivePropertyOperator(new HutoolConverterManager());
        OneToOneAssembleOperationHandler handler = new OneToOneAssembleOperationHandler(operator);
        configuration.getAssembleOperationHandlerMap().put(handler.getClass().getName(), handler);
        executor = new DisorderedBeanOperationExecutor(configuration);
        Container<Integer> container = LambdaContainer.forLambda(
            "test", ids -> ids.stream().filter(id -> id != 0).collect(Collectors.toMap(
                Function.identity(), id -> new Bean(id, "name" + id, null)
            ))
        );
        configuration.registerContainer(container);

        @SuppressWarnings("all")
        Container<Bean> container2 = LambdaContainer.forLambda(
            "identity", beans -> beans.stream()
                .collect(Collectors.toMap(Function.identity(), Function.identity()))
        );
        configuration.registerContainer(container2);
    }

    @Test
    public void process() {
        BeanOperations operations = parseOperations(Bean.class);
        List<Bean> beanList = Arrays.asList(new Bean(1), new Bean(2), new Bean(3));
        executor.execute(beanList, operations);
        for (int i = 0; i < beanList.size(); i++) {
            Assert.assertEquals("name" + (i + 1), beanList.get(i).getName());
            Assert.assertEquals((Integer)(i + 1), beanList.get(i).getOtherId());
        }
        executor.execute(Collections.singletonList(new Bean(0)), operations);
    }

    @Assemble(container = "identity", props = @Mapping(src = "id", ref = "otherId"))
    @RequiredArgsConstructor
    @AllArgsConstructor
    @Data
    private static class Bean {
        @Assemble(
            container = "test", props = @Mapping(src = "name", ref = "name"),
            handler = "OneToOneAssembleOperationHandler"
        )
        private final Integer id;
        private String name;
        private Integer otherId;
    }
}
