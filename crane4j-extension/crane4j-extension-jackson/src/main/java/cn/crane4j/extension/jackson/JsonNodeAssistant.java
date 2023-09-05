package cn.crane4j.extension.jackson;

/**
 * Assistant for json node.
 *
 * @author huangchengxing
 * @since 2.2.0
 */
public interface JsonNodeAssistant<T> {

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
    String determinePropertyName(String propertyName);

    /**
     * Convert the specified target to json node.
     *
     * @param target target
     * @return json node
     */
    T convertTargetToJsonNode(Object target);
}
