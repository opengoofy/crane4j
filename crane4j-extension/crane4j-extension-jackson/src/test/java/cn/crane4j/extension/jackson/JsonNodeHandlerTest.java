package cn.crane4j.extension.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Test;

/**
 * test for {@link JsonNodeHandler}.
 *
 * @author huangchengxing
 */
public class JsonNodeHandlerTest {

    @Test
    public void test() {
        ObjectMapper objectMapper = new ObjectMapper();
        JacksonJsonNodeAssistant assistant = new JacksonJsonNodeAssistant(objectMapper);
        JsonNodeHandler handler = new JsonNodeHandler(assistant);

        // read
        Foo foo = new Foo(1, "test");
        ObjectNode objectNode = objectMapper.valueToTree(foo);
        Assert.assertEquals("test", handler.read(objectNode, "name"));

        // write
        handler.write(objectNode, "gender", "male");
        Assert.assertNotNull(objectNode.get("gender"));
    }

    @Data
    @RequiredArgsConstructor
    private static class Foo {
        private final Integer id;
        private final String name;
    }
}
