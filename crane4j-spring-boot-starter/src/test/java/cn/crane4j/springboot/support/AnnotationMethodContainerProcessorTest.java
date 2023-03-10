package cn.crane4j.springboot.support;

import cn.crane4j.annotation.Bind;
import cn.crane4j.annotation.ContainerCache;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.annotation.MappingType;
import cn.crane4j.core.container.CacheableContainer;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.MethodInvokerContainer;
import cn.crane4j.springboot.config.Crane4jAutoConfiguration;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * test for {@link AnnotationMethodContainerProcessor}
 *
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Crane4jAutoConfiguration.class, AnnotationMethodContainerProcessorTest.Service.class})
public class AnnotationMethodContainerProcessorTest {

    @Autowired
    private Crane4jApplicationContext context;
    @Autowired
    private AnnotationMethodContainerProcessor annotationMethodContainerProcessor;

    @Test
    public void test() {
        Map<String, Container<?>> containerMap = context.getRegisteredContainers();
        Assert.assertEquals(3, containerMap.size());
        Assert.assertFalse(containerMap.containsKey("noneResultMethod"));

        // mappedMethod
        Container<?> mappedMethod = containerMap.get("mappedMethod");
        Assert.assertTrue(mappedMethod instanceof MethodInvokerContainer);
        Assert.assertEquals("mappedMethod", mappedMethod.getNamespace());

        // onoToOneMethod
        Container<?> onoToOneMethod = containerMap.get("onoToOneMethod");
        Assert.assertTrue(onoToOneMethod instanceof MethodInvokerContainer);
        Assert.assertEquals("onoToOneMethod", onoToOneMethod.getNamespace());

        // oneToManyMethod
        Container<?> oneToManyMethod = containerMap.get("oneToManyMethod");
        Assert.assertTrue(oneToManyMethod instanceof CacheableContainer);
        Assert.assertEquals("oneToManyMethod", oneToManyMethod.getNamespace());

        annotationMethodContainerProcessor.destroy();
    }

    protected static class BaseService {
        public void noneResultMethod() { }
        public Map<String, Foo> mappedMethod(List<String> args) {
            return args.stream()
                .map(key -> new Foo(key, key))
                .collect(Collectors.toMap(Foo::getId, Function.identity()));
        }
    }

    // ????????????bind?????????????????????????????????
    @ContainerMethod(namespace = "noneResultMethod", type = MappingType.MAPPED, resultType = Foo.class)
    // ?????????????????????????????????????????????
    @ContainerMethod(
        namespace = "noneResultMethod", type = MappingType.MAPPED, resultType = Foo.class,
        bind = @Bind("noneResultMethod")
    )
    @ContainerMethod(
        namespace = "mappedMethod", type = MappingType.MAPPED, resultType = Foo.class,
        bind = @Bind(value = "mappedMethod", paramTypes = List.class)
    )
    protected static class Service extends BaseService {
        @ContainerMethod(namespace = "onoToOneMethod", type = MappingType.ONE_TO_ONE, resultType = Foo.class)
        public Set<Foo> onoToOneMethod(List<String> args) {
            return args.stream().map(key -> new Foo(key, key)).collect(Collectors.toSet());
        }
        // ???????????????????????????????????????
        @ContainerCache
        @ContainerMethod(namespace = "oneToManyMethod", type = MappingType.ONE_TO_MANY, resultType = Foo.class, resultKey = "name")
        public List<Foo> oneToManyMethod(List<String> args) {
            return args.stream().map(key -> new Foo(key, key)).collect(Collectors.toList());
        }
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    private static class Foo {
        private String id;
        private String name;
    }
}
