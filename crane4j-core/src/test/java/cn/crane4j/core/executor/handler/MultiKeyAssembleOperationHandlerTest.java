package cn.crane4j.core.executor.handler;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.ConstantContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.BaseExecutorTest;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.DisorderedBeanOperationExecutor;
import cn.crane4j.core.parser.BeanOperations;
import cn.crane4j.core.support.reflect.MapAccessiblePropertyOperator;
import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectPropertyOperator;
import cn.hutool.core.collection.CollUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * test for {@link MultiKeyAssembleOperationHandler}
 *
 * @author huangchengxing
 */
public class MultiKeyAssembleOperationHandlerTest extends BaseExecutorTest {

    private BeanOperationExecutor executor;

    @Before
    public void init() {
        PropertyOperator operator = new MapAccessiblePropertyOperator(new ReflectPropertyOperator());
        MultiKeyAssembleOperationHandler handler = new MultiKeyAssembleOperationHandler(operator);
        configuration.getAssembleOperationHandlerMap().put(handler.getClass().getName(), handler);

        executor = new DisorderedBeanOperationExecutor();

        Map<String, Map<String, String>> sources = new HashMap<>();
        for (int i = 0; i < 6; i++) {
            Map<String, String> source = new HashMap<>();
            source.put("value", "key" + i);
            source.put("name", "id" + i);
            sources.put(String.valueOf(i), source);
        }
        Container<String> container = ConstantContainer.forMap("test", sources);
        configuration.getContainerMap().put("test", container);
    }

    @Test
    public void process() {
        BeanOperations operations = parseOperations(Bean.class);
        List<Bean> beanList = Arrays.asList(
            new Bean("1, 2, 3", Arrays.asList("1", "2", "3"), new String[]{"1", "2", "3"}),
            new Bean("4, 5", Arrays.asList("4", "5"), new String[]{"4", "5"})
        );

        executor.execute(beanList, operations);
        Bean bean1 = CollUtil.get(beanList, 0);
        checkBean(bean1, "1", "2", "3");
        Bean bean2 = CollUtil.get(beanList, 1);
        checkBean(bean2, "4", "5");
    }

    private void checkBean(Bean bean, String... ids) {
        Assert.assertEquals(Arrays.stream(ids).map(id -> "id" + id).collect(Collectors.toList()), bean.getNames());
        Assert.assertEquals(Arrays.stream(ids).map(id -> "key" + id).collect(Collectors.toSet()), bean.getValues());
        List<Object> items = bean.getItems();
        Assert.assertEquals(ids.length, items.size());
        for (Object item : items) {
            Assert.assertTrue(item instanceof Map);
        }
    }

    @RequiredArgsConstructor
    @Data
    private static class Bean {
        @Assemble(
            container = "test", props = @Mapping(src = "name", ref = "names"),
            handler = MultiKeyAssembleOperationHandler.class
        )
        private final String ids;
        private List<String> names;

        @Assemble(
            container = "test", props = @Mapping(src = "value", ref = "values"),
            handler = MultiKeyAssembleOperationHandler.class
        )
        private final List<String> keys;
        private Set<String> values;

        @Assemble(
            container = "test", props = @Mapping(ref = "items"),
            handler = MultiKeyAssembleOperationHandler.class
        )
        private final String[] code;
        private List<Object> items;
    }
}
