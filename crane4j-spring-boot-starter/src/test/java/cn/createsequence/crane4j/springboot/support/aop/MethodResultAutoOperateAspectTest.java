package cn.createsequence.crane4j.springboot.support.aop;

import cn.createsequence.crane4j.core.annotation.Assemble;
import cn.createsequence.crane4j.core.annotation.ContainerMethod;
import cn.createsequence.crane4j.core.annotation.Disassemble;
import cn.createsequence.crane4j.core.annotation.Mapping;
import cn.createsequence.crane4j.core.annotation.MappingType;
import cn.createsequence.crane4j.springboot.annotation.AutoOperate;
import cn.createsequence.crane4j.springboot.config.Crane4jAutoConfiguration;
import cn.hutool.core.collection.CollUtil;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * test for {@link MethodResultAutoOperateAspect}
 *
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    MethodResultAutoOperateAspectTest.SourceService.class,
    MethodResultAutoOperateAspectTest.TargetService.class,
    Crane4jAutoConfiguration.class
})
public class MethodResultAutoOperateAspectTest {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private MethodResultAutoOperateAspect methodResultAutoOperateAspect;

    @Test
    public void test() {
        TargetService service = applicationContext.getBean(TargetService.class);
        List<Foo> list = service.getFooList().getData();

        Foo foo1 = CollUtil.get(list, 0);
        Assert.assertEquals(foo1.getId(), foo1.getName());
        NestedFoo nestedFoo1 = (NestedFoo)foo1.getNestedFoo();
        Assert.assertEquals(nestedFoo1.getId(), nestedFoo1.getName());

        Foo foo2 = CollUtil.get(list, 1);
        Assert.assertEquals(foo2.getId(), foo2.getName());
        NestedFoo nestedFoo2 = (NestedFoo)foo1.getNestedFoo();
        Assert.assertEquals(nestedFoo2.getId(), nestedFoo2.getName());

        methodResultAutoOperateAspect.destroy();
    }

    protected static class SourceService {
        @ContainerMethod(
            namespace = "onoToOneMethod", type = MappingType.ONE_TO_ONE,
            resultType = Source.class, resultKey = "key"
        )
        public Set<Source> onoToOneMethod(List<String> args) {
            return args.stream().map(key -> new Source(key, key)).collect(Collectors.toSet());
        }
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    @Setter
    private static class Source {
        private String key;
        private String value;
    }

    /**
     * @author huangchengxing
     */
    @Component
    protected static class TargetService {
        @AutoOperate(type = Foo.class, on = "data")
        public Result<List<Foo>> getFooList() {
            return new Result<>(
                Arrays.asList(
                    new Foo("1", null, new NestedFoo("2", null)),
                    new Foo("2", null, new NestedFoo("1", null))
                )
            );
        }
    }

    @Getter
    @RequiredArgsConstructor
    private static class Result<T> {
        private final T data;
    }

    @Assemble(namespace = "onoToOneMethod", props = @Mapping(src = "value", ref = "name"))
    @Documented
    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    private @interface AssembleId { }

    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    @Setter
    private static class Foo {
        @AssembleId
        private String id;
        private String name;
        @Disassemble
        private Object nestedFoo;
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    @Setter
    private static class NestedFoo {
        @AssembleId
        private String id;
        private String name;
    }
}
