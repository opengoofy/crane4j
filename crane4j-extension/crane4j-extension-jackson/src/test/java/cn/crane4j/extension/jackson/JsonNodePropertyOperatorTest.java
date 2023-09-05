package cn.crane4j.extension.jackson;

import cn.crane4j.core.support.reflect.PropertyOperator;
import cn.crane4j.core.support.reflect.ReflectivePropertyOperator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link JsonNodePropertyOperator}.
 *
 * @author huangchengxing
 */
public class JsonNodePropertyOperatorTest {

    @Test
    public void test() {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNodeAssistant<JsonNode> assistant = new JacksonJsonNodeAssistant(objectMapper);
        PropertyOperator propertyOperator = new ReflectivePropertyOperator();
        propertyOperator = new JsonNodePropertyOperator(assistant, propertyOperator);

        Foo foo = new Foo(1, "test");
        Assert.assertEquals("test", propertyOperator.readProperty(foo.getClass(), foo, "name"));
        JsonNode fooNode = objectMapper.valueToTree(foo);
        Assert.assertEquals("test", propertyOperator.readProperty(fooNode.getClass(), fooNode, "name"));

        propertyOperator.writeProperty(foo.getClass(), foo, "name", "test2");
        Assert.assertEquals("test2", foo.getName());
        propertyOperator.writeProperty(fooNode.getClass(), fooNode, "name", "test3");
        Assert.assertEquals(objectMapper.valueToTree("test3"), fooNode.get("name"));
        propertyOperator.writeProperty(fooNode.getClass(), fooNode, "gender", "male");
        Assert.assertEquals(objectMapper.valueToTree("male"), fooNode.get("gender"));
    }

    @Data
    @RequiredArgsConstructor
    private static class Foo {
        private final Integer id;
        private final String name;
    }
}
