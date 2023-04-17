package cn.crane4j.core.support.operator;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.annotation.Operator;
import cn.crane4j.annotation.ProvideData;
import cn.crane4j.core.container.SharedContextContainerProvider;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.support.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * test for {@link SharedContextProxyMethodFactory}
 *
 * @author huangchengxing
 */
public class SharedContextProxyMethodFactoryTest {

    private OperatorProxyFactory operatorProxyFactory;
    private SharedContextProxyMethodFactory sharedContextProxyMethodFactory;
    private SharedContextContainerProvider sharedContextContainerProvider;

    @Before
    public void init() {
        Crane4jGlobalConfiguration globalConfiguration = SimpleCrane4jGlobalConfiguration.create(Collections.emptyMap());
        AnnotationFinder annotationFinder = new SimpleAnnotationFinder();
        this.sharedContextContainerProvider = globalConfiguration.getContainerProvider(SharedContextContainerProvider.class);
        this.sharedContextProxyMethodFactory = new SharedContextProxyMethodFactory(
            new SimpleAnnotationFinder(), new SimpleParameterNameFinder(), sharedContextContainerProvider, true
        );
        this.operatorProxyFactory = new OperatorProxyFactory(
            globalConfiguration, annotationFinder, Collections.singletonList(sharedContextProxyMethodFactory)
        );
    }

    @Test
    public void order() {
        Assert.assertEquals(SharedContextProxyMethodFactory.ORDER, sharedContextProxyMethodFactory.getSort());
    }

    @Test
    public void get() {
        Assert.assertThrows(Crane4jException.class, () -> operatorProxyFactory.get(OperatorInterface2.class));
        OperatorInterface1 operator = operatorProxyFactory.get(OperatorInterface1.class);

        Map<String, Object> target = new HashMap<>();
        target.put("id", 1);
        Collection<Map<String, Object>> targets = Collections.singletonList(target);

        Map<Integer, Object> source = new HashMap<>();
        source.put(1, "name1");
        operator.fill(targets, source);
        Assert.assertEquals("name1", target.get("name"));

        source.put(1, "name2");
        operator.fill(targets, source);
        Assert.assertEquals("name2", target.get("name"));

        sharedContextContainerProvider.setDataProvider("test", ids -> ids.stream().collect(Collectors.toMap(Function.identity(), id -> id + "name")));
        operator.fill(targets, null);
        Assert.assertEquals("1name", target.get("name"));

        operator.fill(null, null);

        OperatorInterface3 operator3 = operatorProxyFactory.get(OperatorInterface3.class);
        operator3.fill(targets, ids -> ids.stream().collect(Collectors.toMap(Function.identity(), id -> id + "name" + id)));
        Assert.assertEquals("1name1", target.get("name"));
    }

    @Operator
    private interface OperatorInterface1 {
        @Assemble(
            key = "id", container = "test", props = @Mapping(ref = "name"),
            containerProvider = SharedContextContainerProvider.class
        )
        void fill(Collection<Map<String, Object>> targets, @ProvideData("test") Map<?, ?> map);
    }

    @Operator
    private interface OperatorInterface2 {
        @Assemble(key = "id", container = "test", props = @Mapping(ref = "name"), containerProvider = SharedContextContainerProvider.class)
        void fill(Collection<Map<String, Object>> targets, @ProvideData("test") Object map);
    }

    @Operator
    private interface OperatorInterface3 {
        @Assemble(
            key = "id", container = "test", props = @Mapping(ref = "name"),
            containerProvider = SharedContextContainerProvider.class
        )
        void fill(Collection<Map<String, Object>> targets, @ProvideData("test") DataProvider<Integer, ?> provider);
    }
}
