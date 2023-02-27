package cn.crane4j.core.executor;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.ConstantContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.parser.AssembleOperation;
import cn.crane4j.core.parser.BeanOperations;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * test for {@link OrderedBeanOperationExecutor}
 *
 * @author huangchengxing
 */
public class OrderedBeanOperationExecutorTest extends BaseExecutorTest {

    private OrderedBeanOperationExecutor executor;

    @Before
    public void init() {
        executor = new OrderedBeanOperationExecutor(Comparator.comparing(AssembleOperation::getSort));
        Map<Object, Object> sources = new HashMap<>();
        sources.put(1, "two");
        sources.put("two", "three");
        Container<Object> container = ConstantContainer.forMap("test", sources);
        configuration.getContainerMap().put("test", container);
    }

    @Test
    public void execute() {
        Bean bean = new Bean().setId(1);
        BeanOperations beanOperations = parseOperations(Bean.class);
        executor.execute(Collections.singleton(bean), beanOperations);
        Assert.assertEquals("two", bean.getCode());
        Assert.assertEquals("three", bean.getName());
        Assert.assertEquals("three", bean.getType());
    }

    @Accessors(chain = true)
    @Data
    private static class Bean {
        @Assemble(container = "test", props = @Mapping(ref = "name"), sort = 1)
        private String code;
        @Assemble(container = "test", props = @Mapping(ref = "code"), sort = 0)
        private Integer id;
        private String name;
        @Assemble(props = @Mapping(src = "name"), sort = 2)
        private String type;
    }
}
