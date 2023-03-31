package cn.crane4j.spring.boot.example;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.LambdaContainer;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.OperateTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 演示如何使用多对多装配处理器：
 * 1.准备一个数据源容器，向该容器的输入一个key值将会获得对应的一个数据源对象;
 * 2.在类为拼接字符串、集合或数组类型的key字段上配置装配操作；
 * 3.指定装配处理器为ManyToManyReflexAssembleOperationHandler;
 * 4.指定字段映射，获取数据源集合中的每一个指定字段值，转为集合后赋值给目标对象；
 *
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Crane4jSpringBootStarterExampleApplication.class})
public class ManyToManyAssembleExample {

    @Autowired
    private Crane4jGlobalConfiguration configuration;
    @Autowired
    private OperateTemplate operateTemplate;

    @Before
    public void init() {
        Container<Integer> container = LambdaContainer.forLambda(
            "foo", ids -> ids.stream().map(id -> new Foo(id, "name" + id))
                .collect(Collectors.toMap(Foo::getId, Function.identity()))
        );
        configuration.registerContainer(container);
    }

    @Test
    public void test() {
        List<FooVO> vos = IntStream.rangeClosed(1, 5)
            .mapToObj(i -> IntStream.rangeClosed(0, i).boxed().collect(Collectors.toList()))
            .map(FooVO::new)
            .collect(Collectors.toList());
        operateTemplate.execute(vos);

        for (FooVO vo : vos) {
            List<String> names = vo.getNames();
            List<Integer> ids = vo.getIds();
            for (int i = 0; i < vo.getIds().size(); i++) {
                Assert.assertEquals("name" + ids.get(i), names.get(i));
            }
        }
    }

    @RequiredArgsConstructor
    @Data
    private static class FooVO {
        @Assemble(
            container = "foo",
            props = @Mapping(src = "name", ref = "names"),
            handlerName = "manyToManyReflexAssembleOperationHandler"
        )
        private final List<Integer> ids;
        private List<String> names;
    }

    @AllArgsConstructor
    @Data
    private static class Foo {
        private final Integer id;
        private String name;
    }
}
