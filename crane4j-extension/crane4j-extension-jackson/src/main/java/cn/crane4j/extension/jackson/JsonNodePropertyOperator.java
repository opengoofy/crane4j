package cn.crane4j.extension.jackson;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.reflect.PropertyOperator;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The wrapper class of {@link PropertyOperator} that
 * make operator supports reading and writing properties of {@link JsonNode}.
 *
 * @author huangchengxing
 * @since 2.2.0
 */
public class JsonNodePropertyOperator extends JsonNodeHandler implements PropertyOperator {

    private final PropertyOperator propertyOperator;

    /**
     * Constructor.
     *
     * @param jsonNodeAssistant json node assistant
     * @param propertyOperator property operator
     */
    public JsonNodePropertyOperator(
        JsonNodeAssistant<JsonNode> jsonNodeAssistant, PropertyOperator propertyOperator) {
        super(jsonNodeAssistant);
        this.propertyOperator = propertyOperator;
    }

    /**
     * Get getter method.
     *
     * @param targetType   target type
     * @param propertyName property name
     * @return getter method
     */
    @Nullable
    @Override
    public MethodInvoker findGetter(Class<?> targetType, String propertyName) {
        if (!(TreeNode.class.isAssignableFrom(targetType))) {
            return propertyOperator.findGetter(targetType, propertyName);
        }
        return (t, args) -> read(t, propertyName);
    }

    /**
     * Get setter method.
     *
     * @param targetType   target type
     * @param propertyName property name
     * @return setter method
     */
    @Nullable
    @Override
    public MethodInvoker findSetter(Class<?> targetType, String propertyName) {
        if (!(TreeNode.class.isAssignableFrom(targetType))) {
            return propertyOperator.findSetter(targetType, propertyName);
        }
        return JsonNode.class.isAssignableFrom(targetType) ?
            (t, args) -> write(t, propertyName, args[0]) : null;
    }
}
