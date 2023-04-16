package cn.crane4j.core.support;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.annotation.Operator;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.exception.Crane4jException;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * test for {@link OperatorProxyFactory}.
 *
 * @author huangchengxing
 */
public class OperatorProxyFactoryTest {

    private OperatorProxyFactory operatorProxyFactory;

    @Before
    public void init() {
        Crane4jGlobalConfiguration globalConfiguration = SimpleCrane4jGlobalConfiguration.create(Collections.emptyMap());
        AnnotationFinder annotationFinder = new SimpleAnnotationFinder();
        this.operatorProxyFactory = new OperatorProxyFactory(globalConfiguration, annotationFinder);

        LambdaContainer<Integer> container = LambdaContainer.forLambda(
            "test", ids -> ids.stream().collect(Collectors.toMap(id -> id, id -> "name" + id))
        );
        globalConfiguration.registerContainer(container);
    }

    @Test
    public void checkInvalidOperator() {
        Assert.assertThrows(Crane4jException.class, () -> operatorProxyFactory.get(OperatorInterface2.class));
        Assert.assertThrows(Crane4jException.class, () -> operatorProxyFactory.get(OperatorInterface3.class));
        Assert.assertNull(operatorProxyFactory.get(OperatorInterface4.class));
    }

    @Test
    public void get() {
        OperatorInterface operator = operatorProxyFactory.get(OperatorInterface.class);
        Assert.assertNotNull(operator);

        Collection<Map<String, Object>> targets = IntStream.rangeClosed(0, 5).mapToObj(id -> {
            Map<String, Object> target = new HashMap<>();
            target.put("id", id);
            return target;
        }).collect(Collectors.toList());

        operator.fill(null);
        operator.fill(targets);
        targets.forEach(target -> assertEquals("name" + target.get("id"), target.get("name")));
    }

    @Operator
    private interface OperatorInterface {
        @Assemble(key = "id", container = "test", props = @Mapping(ref = "name"))
        void fill(Collection<Map<String, Object>> targets);
    }

    @Operator
    private interface OperatorInterface2 extends OperatorInterface {
        void noneAnnotatedMethod(Collection<Map<String, Object>> targets);
    }

    @Operator
    private interface OperatorInterface3 extends OperatorInterface {
        @Assemble(key = "id", container = "test", props = @Mapping(ref = "name"))
        void noneArgMethod();
    }

    private interface OperatorInterface4 extends OperatorInterface {
    }
}
