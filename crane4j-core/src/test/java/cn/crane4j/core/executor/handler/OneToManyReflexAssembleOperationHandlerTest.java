package cn.crane4j.core.executor.handler;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.ConstantContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.BaseExecutorTest;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.converter.HutoolConverterManager;
import cn.crane4j.core.support.reflect.MapAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectPropertyOperator;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * test for {@link OneToManyAssembleOperationHandler}.
 *
 * @author huangchengxing
 */
public class OneToManyReflexAssembleOperationHandlerTest extends BaseExecutorTest {

    private BeanOperationExecutor executor;

    @Before
    public void init() {
        PropertyOperator operator = new MapAccessiblePropertyOperator(new ReflectPropertyOperator(new HutoolConverterManager()));
        OneToManyAssembleOperationHandler handler = new OneToManyAssembleOperationHandler(operator);
        configuration.getAssembleOperationHandlerMap().put(handler.getClass().getName(), handler);

        executor = new DisorderedBeanOperationExecutor(configuration);

        Map<Integer, List<Bean>> sources = new HashMap<>();
        for (int i = 0; i < 6; i++) {
            List<Bean> beanList = new ArrayList<>();
            for (int j = 0; j < i + 1; j++) {
                beanList.add(new Bean(j, "name" + j));
            }
            sources.put(i, beanList);
        }
        Container<Integer> container = ConstantContainer.forMap("test", sources);
        configuration.registerContainer(container);
    }

    @Test
    public void process() {
        List<Bean> beanList = Arrays.asList(new Bean(0, null), new Bean(1, null), new Bean(2, null));
        BeanOperations operations = parseOperations(Bean.class);
        executor.execute(beanList, operations);

        for (int i = 0; i < beanList.size(); i++) {
            Bean bean = beanList.get(i);
            for (int j = 0; j < i + 1; j++) {
                Assert.assertEquals("name" + j, bean.getNames().get(j));
            }
        }
    }

    @RequiredArgsConstructor
    @Data
    private static class Bean {
        @Assemble(
            container = "test", props = @Mapping(src = "name", ref = "names"),
            handler = "OneToManyAssembleOperationHandler"
        )
        private final Integer id;
        private final String name;
        private List<String> names;
    }
}
