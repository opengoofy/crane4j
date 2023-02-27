package cn.crane4j.springboot.support.aop;

import cn.crane4j.annotation.ArgAutoOperate;
import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.annotation.Disassemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.annotation.MappingType;
import cn.crane4j.springboot.config.Crane4jAutoConfiguration;
import cn.hutool.core.collection.CollUtil;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * test for {@link MethodArgumentAutoOperateAspect}
 *
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    MethodArgumentAutoOperateAspectTest.SourceService.class,
    MethodArgumentAutoOperateAspectTest.TargetService.class,
    Crane4jAutoConfiguration.class
})
public class MethodArgumentAutoOperateAspectTest {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private MethodArgumentAutoOperateAspect methodArgumentAutoOperateAspect;

    @Test
    public void test() {
        TargetService service = applicationContext.getBean(TargetService.class);
        List<Foo> list = service.getFooList(
            new Foo("1", null, new NestedFoo("2", null)),
            new Foo("2", null, new NestedFoo("1", null)),
            null
        );

        Foo foo1 = CollUtil.get(list, 0);
        Assert.assertEquals(foo1.getId(), foo1.getName());
        NestedFoo nestedFoo1 = (NestedFoo)foo1.getNestedFoo();
        Assert.assertEquals(nestedFoo1.getId(), nestedFoo1.getName());

        Foo foo2 = CollUtil.get(list, 1);
        Assert.assertEquals(foo2.getId(), foo2.getName());
        NestedFoo nestedFoo2 = (NestedFoo)foo1.getNestedFoo();
        Assert.assertEquals(nestedFoo2.getId(), nestedFoo2.getName());

        methodArgumentAutoOperateAspect.destroy();
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
        @ArgAutoOperate(
            @AutoOperate(value = "foo1", type = Foo.class)
        )
        public List<Foo> getFooList(Foo foo1, @AutoOperate(type = Foo.class) Foo foo2, Foo foo3) {
            return Arrays.asList(foo1, foo2, foo3);
        }
        @ArgAutoOperate
        public List<Foo> noneArg() {
            return Collections.emptyList();
        }
    }

    @Assemble(container = "onoToOneMethod", props = @Mapping(src = "value", ref = "name"))
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
