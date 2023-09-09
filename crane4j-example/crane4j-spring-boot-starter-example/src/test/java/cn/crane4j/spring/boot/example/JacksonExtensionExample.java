package cn.crane4j.spring.boot.example;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.annotation.Mapping;
import cn.crane4j.core.container.Containers;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author huangchengxing
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    Crane4jSpringBootStarterExampleApplication.class, ObjectMapper.class
})
public class JacksonExtensionExample {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Crane4jGlobalConfiguration configuration;

    @SneakyThrows
    @Test
    public void test() {
        int num = 5;

        // init datasource
        Map<String, Foo> datasource = new HashMap<>();
        IntStream.range(0, num)
            .mapToObj(idx -> new Foo(idx, "name" + idx))
            .forEach(foo -> datasource.put(foo.getId().toString(), foo));
        configuration.registerContainer(Containers.forMap("test", datasource));

        // prepare targets
        List<Foo> targets = IntStream.range(0, num)
            .mapToObj(Foo::new)
            .collect(Collectors.toList());

        String json = objectMapper.writeValueAsString(targets);
        System.out.println(json);
        List<Map<String, Object>> beans = objectMapper.readValue(
            json, new TypeReference<List<Map<String, Object>>>() {}
        );
        Assert.assertEquals(num, beans.size());

        for (int i = 0; i < beans.size(); i++) {
            Map<String, Object> target = beans.get(i);
            Assert.assertEquals("name" + i, target.get("name"));
            Assert.assertEquals("name" + i, target.get("fooName"));
        }
    }

    @AutoOperate(type = Foo.class)
    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    private static class Foo {
        @Assemble(container = "test", props = {
            @Mapping("name"),
            @Mapping(src = "name", ref = "fooName")
        })
        private final Integer id;
        private String name;
    }
}
