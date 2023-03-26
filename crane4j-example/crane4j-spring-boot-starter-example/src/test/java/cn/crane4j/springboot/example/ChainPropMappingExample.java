package cn.crane4j.springboot.example;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.extension.support.OperateTemplate;
import cn.crane4j.springboot.support.Crane4jApplicationContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 如何以链式操作符设置和访问嵌套属性
 *
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Crane4jSpringBootStarterExampleApplication.class})
public class ChainPropMappingExample {

    @Autowired
    private Crane4jApplicationContext context;
    @Autowired
    private OperateTemplate operateTemplate;

    @Before
    public void init() {
        Container<Integer> oneToOne = LambdaContainer.forLambda(
            "OneToOne", ids -> ids.stream().collect(Collectors.toMap(
                Function.identity(), id -> new Foo(id, null, new Foo(null, "name" + id, null))
            ))
        );
        context.registerContainer(oneToOne);
        Container<Integer> oneToMany = LambdaContainer.forLambda(
            "OneToMany", ids -> ids.stream().collect(Collectors.toMap(
                Function.identity(), id -> Arrays.asList(
                    new Foo(id, null, new Foo(null, "name" + id, null)), new Foo(id, null, new Foo(null, "name" + id, null))
                )
            ))
        );
        context.registerContainer(oneToMany);
    }

    @Test
    public void testOneToOne() {
        List<FooVO> fooList = IntStream.rangeClosed(0, 3)
            .mapToObj(id -> new FooVO(id, new Foo(null, null, null), null))
            .collect(Collectors.toList());
        operateTemplate.executeIfMatchAnyGroups(fooList, "testOneToOne");
        for (FooVO fooVO : fooList) {
            Foo foo = fooVO.getNested();
            Assert.assertEquals("name" + fooVO.getId(), foo.getName());
        }
    }

    @Test
    public void testOneToMany() {
        List<FooVO> fooList = IntStream.rangeClosed(0, 4)
            .mapToObj(id -> new FooVO(id, new Foo(null, null, null), null))
            .collect(Collectors.toList());

        operateTemplate.executeIfMatchAnyGroups(fooList, "testOneToMany");
        for (FooVO fooVO : fooList) {
            fooVO.getNames().forEach(name -> Assert.assertEquals("name" + fooVO.getId(), name));
        }
    }

    @Data
    @AllArgsConstructor
    private static class FooVO {
        // 一对一装配
        @Assemble(
            container = "OneToOne", groups = "testOneToOne",
            props = @Mapping(src = "foo.name", ref = "nested.name")
        )
        // 一对多装配，批量映射
        @Assemble(
            container = "OneToMany", groups = "testOneToMany",
            props = @Mapping(src = "foo.name", ref = "names"),
            handlerName = "oneToManyReflexAssembleOperationHandler"
        )
        private Integer id;
        private Foo nested;
        private List<String> names;
    }

    @Data
    @AllArgsConstructor
    private static class Foo {
        private Integer id;
        private String name;
        private Foo foo;
    }
}
