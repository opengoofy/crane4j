package cn.crane4j.extension.spring.operator;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.annotation.Operator;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.extension.spring.DefaultCrane4jSpringConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * test for {@link OperatorBeanDefinitionRegistrar}
 *
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {DefaultCrane4jSpringConfiguration.class, OperatorBeanDefinitionRegistrarTest.Config.class})
public class OperatorBeanDefinitionRegistrarTest {

    @Autowired
    private Crane4jGlobalConfiguration globalConfiguration;
    @Autowired
    private ApplicationContext applicationContext;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private OperatorInterface operatorInterface;

    @Before
    public void init() {
        globalConfiguration.registerContainer(LambdaContainer.forLambda(
            "test", ids -> ids.stream().collect(Collectors.toMap(id -> id, id -> "name" + id))
        ));
    }

    @Test
    public void test() {
        OperatorInterface operator = applicationContext.getBean(OperatorInterface.class);
        Assert.assertSame(operatorInterface, operator);

        Collection<Map<String, Object>> targets = IntStream.rangeClosed(0, 5).mapToObj(id -> {
            Map<String, Object> target = new HashMap<>();
            target.put("id", id);
            return target;
        }).collect(Collectors.toList());

        operator.fill(null);
        operator.fill(targets);
    }


    @OperatorScan(includes = {OperatorBeanDefinitionRegistrarTest.OperatorInterface.class})
    @Configuration
    protected static class Config {
    }

    @Operator
    protected interface OperatorInterface {
        @Assemble(key = "id", container = "test", props = @Mapping(ref = "name"))
        void fill(Collection<Map<String, Object>> targets);
    }
}
