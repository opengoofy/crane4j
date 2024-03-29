package cn.crane4j.core.executor;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Disassemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.annotation.condition.ConditionOnProperty;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.Containers;
import cn.crane4j.core.parser.BeanOperations;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * test for {@link DisorderedBeanOperationExecutor}
 *
 * @author huangchengxing
 */
public class DisorderedBeanOperationExecutorTest extends BaseExecutorTest {

    private DisorderedBeanOperationExecutor executor;

    @Before
    public void init() {
        executor = new DisorderedBeanOperationExecutor(configuration);

        Map<Integer, Object> sources = new HashMap<>();
        sources.put(1, new Source(1, "one"));
        sources.put(2, new Source(2, "two"));
        Container<Integer> container = Containers.forMap("test", sources);
        configuration.registerContainer(container);
    }

    @SuppressWarnings("all")
    @Test
    public void execute() {
        Bean bean1 = new Bean().setId(1).setNestedBean(new NestedBean().setType(2));
        Bean bean2 = new Bean().setId(2).setNestedBean(new NestedBean().setType(1));
        List<Bean> beanList = Arrays.asList(bean1, bean2);

        BeanOperations beanOperations = parseOperations(Bean.class);
        executor.execute(beanList, beanOperations);

        Assert.assertEquals("one", bean1.getName());
        Assert.assertEquals("two", bean1.getNestedBean().getTypeName());
        Assert.assertEquals("two", bean2.getName());
        Assert.assertEquals("one", bean2.getNestedBean().getTypeName());
    }

    @Test
    public void executeWithCondition() {
        ConditionalBean bean1 = new ConditionalBean().setId(1);
        ConditionalBean bean2 = new ConditionalBean().setId(2);
        BeanOperations beanOperations = parseOperations(ConditionalBean.class);
        executor.execute(Arrays.asList(bean1, bean2), beanOperations);
        Assert.assertEquals("one", bean1.getName());
        Assert.assertNull(bean2.getName());
    }

    @Test
    public void executeMethodBasedOperation() {
        MethodBasedOperationBean bean1 = new MethodBasedOperationBean(1, 1);

        BeanOperations beanOperations = parseOperations(MethodBasedOperationBean.class);
        executor.execute(Collections.singleton(bean1), beanOperations);
        Assert.assertEquals("one", bean1.getName1());
        Assert.assertEquals("one", bean1.getName2());
    }

    @Getter
    @RequiredArgsConstructor
    private static class Source {
        private final Integer key;
        private final String value;
    }

    @Accessors(chain = true)
    @Data
    private static class Bean {
        @Assemble(container = "test", props = @Mapping(ref = "name", src = "value"))
        private Integer id;
        private String name;
        @Disassemble(type = NestedBean.class)
        private NestedBean nestedBean;
    }

    @Accessors(chain = true)
    @Data
    private static class NestedBean {
        @Assemble(container = "test", props = @Mapping(ref = "typeName", src = "value"))
        private Integer type;
        private String typeName;
    }

    @Accessors(chain = true)
    @Data
    private static class ConditionalBean {
        @ConditionOnProperty(value = "1", valueType = Integer.class)
        @Assemble(container = "test", props = @Mapping(ref = "name", src = "value"))
        private Integer id;
        private String name;
    }


    @Assemble(key = "getField1", container = "test", props = @Mapping(ref = "name1", src = "value"))
    @RequiredArgsConstructor
    private static class MethodBasedOperationBean {
        @Getter
        private final Integer field1;
        @Getter
        @Setter
        private String name1;

        private final Integer field2;
        @Getter
        @Setter
        private String name2;

        @Assemble(container = "test", props = @Mapping(ref = "name2", src = "value"))
        public Integer getId() {
            return field1;
        }
    }
}
