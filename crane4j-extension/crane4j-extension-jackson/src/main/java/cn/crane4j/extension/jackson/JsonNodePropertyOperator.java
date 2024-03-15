package cn.crane4j.extension.jackson;

import cn.crane4j.core.support.MethodInvoker;
import cn.crane4j.core.support.reflect.PropDesc;
import cn.crane4j.core.support.reflect.PropertyOperator;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The wrapper class of {@link PropertyOperator} that
 * make operator supports reading and writing properties of {@link JsonNode}.
 *
 * @author huangchengxing
 * @since 2.2.0
 */
public class JsonNodePropertyOperator extends JsonNodeHandler implements PropertyOperator {

    private final JsonNodePropDesc jsonNodePropDesc = new JsonNodePropDesc();
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
     * Get property descriptor.
     *
     * @param targetType target type
     * @return property descriptor
     * @since 2.7.0
     */
    @Override
    public @NonNull PropDesc getPropertyDescriptor(Class<?> targetType) {
        return TreeNode.class.isAssignableFrom(targetType) ?
            jsonNodePropDesc : propertyOperator.getPropertyDescriptor(targetType);
    }

    /**
     * The property descriptor that supports reading and writing properties of {@link JsonNode}.
     *
     * @author huangchengxing
     * @since 2.7.0
     */
    private class JsonNodePropDesc implements PropDesc {

        /**
         * Get the bean type.
         *
         * @return bean type
         */
        @Override
        public Class<?> getBeanType() {
            return TreeNode.class;
        }

        /**
         * Get the getter method.
         *
         * @param propertyName property name
         * @return property getter
         */
        @Override
        public @Nullable MethodInvoker getGetter(String propertyName) {
            return (t, args) -> read(t, propertyName);
        }

        /**
         * Get the setter method.
         *
         * @param propertyName property name
         * @return property setter
         */
        @Override
        public @Nullable MethodInvoker getSetter(String propertyName) {
            return (t, args) -> write(t, propertyName, args[0]);
        }
    }
}
