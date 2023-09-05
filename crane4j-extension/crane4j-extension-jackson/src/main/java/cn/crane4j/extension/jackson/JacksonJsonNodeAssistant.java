package cn.crane4j.extension.jackson;

import cn.crane4j.core.util.ObjectUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

/**
 * Jackson json node assistant.
 *
 * @author huangchengxing
 * @since 2.2.0
 */
public class JacksonJsonNodeAssistant implements JsonNodeAssistant<JsonNode> {

    @NonNull
    private final ObjectMapper objectMapper;
    @Setter
    protected PropertyNamingStrategy namingStrategy;

    /**
     * Constructor.
     *
     * @param objectMapper object mapper
     */
    public JacksonJsonNodeAssistant(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.namingStrategy = ObjectUtils.defaultIfNull(
            objectMapper.getPropertyNamingStrategy(), objectMapper.getSerializationConfig().getPropertyNamingStrategy()
        );
    }

    /**
     * <p>Determine the property name when reading or writing for json node.
     *
     * <p>Sometimes, the property name is not the same as the field name,
     * such as the field name is "userName", but the property name is "user_name" in json node,
     * we can override this method to determine the property name.
     *
     * @param propertyName property name
     * @return property name in json node
     */
    @Override
    public String determinePropertyName(String propertyName) {
        return Objects.isNull(namingStrategy) ?
            propertyName : namingStrategy.nameForField(objectMapper.getSerializationConfig(), null, propertyName);
    }

    /**
     * Convert the specified target to json node.
     *
     * @param target target
     * @return json node
     */
    @Override
    public JsonNode convertTargetToJsonNode(Object target) {
        return target instanceof JsonNode ?
            (JsonNode)target : objectMapper.valueToTree(target);
    }
}
