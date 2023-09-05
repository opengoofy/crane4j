package cn.crane4j.extension.jackson;

import cn.crane4j.annotation.AutoOperate;
import cn.crane4j.core.exception.OperationExecuteException;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.aop.AutoOperateAnnotatedElement;
import cn.crane4j.core.support.aop.AutoOperateAnnotatedElementResolver;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * <p>Serializer modifier to reset the serializer for classes annotated by {@link AutoOperate}
 * to support filling according to the operation configuration during serialization.
 *
 * @author huangchengxing
 * @see AutoOperateAnnotatedElementResolver
 * @see AutoOperateSerializer
 * @see AutoOperate
 */
@RequiredArgsConstructor
public class JsonNodeAutoOperateSerializerModifier extends BeanSerializerModifier {

    private final AutoOperateSerializeContext context = new AutoOperateSerializeContext();
    private final AutoOperateAnnotatedElementResolver elementResolver;
    private final ObjectMapper objectMapper;
    private final AnnotationFinder annotationFinder;

    @SuppressWarnings("unchecked")
    @Override
    public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        Class<?> targetType = beanDesc.getType().getRawClass();
        AutoOperate autoOperate = annotationFinder.findAnnotation(targetType, AutoOperate.class);
        if (Objects.isNull(autoOperate)) {
            return serializer;
        }
        AutoOperateAnnotatedElement element = elementResolver.resolve(targetType, autoOperate);
        if (Objects.isNull(element.getBeanOperations()) || element.getBeanOperations().isEmpty()) {
            return serializer;
        }
        return new AutoOperateSerializer((Class<Object>)targetType, element, (JsonSerializer<Object>)serializer);
    }

    /**
     *  Auto operate serialize context.
     *
     * @author huangchengxing
     */
    private static class AutoOperateSerializeContext {

        /**
         * threadLocal
         */
        private final ThreadLocal<Set<Integer>> threadLocal = new ThreadLocal<>();

        /**
         * Record the objects currently being processed.
         *
         * @param target target
         * @return is the current object already being processed
         */
        public boolean process(Object target) {
            Set<Integer> set = threadLocal.get();
            if (Objects.isNull(set)) {
                set = new HashSet<>();
                threadLocal.set(set);
            }
            return set.add(target.hashCode());
        }

        /**
         *  Remove the record for objects currently being processed.
         *
         * @param target target
         */
        public void processed(Object target) {
            Set<Integer> set = threadLocal.get();
            if (Objects.nonNull(set)) {
                set.remove(target.hashCode());
                if (set.isEmpty()) {
                    threadLocal.remove();
                }
            }
        }
    }

    /**
     * A serializer that supports the ability to turn objects into JsonNode trees
     * and autofill according to operation configuration when serializing objects.
     *
     * @author huangchengxing
     */
    public class AutoOperateSerializer extends StdSerializer<Object> {

        /**
         * Auto operate element which resolve from target type
         */
        private final transient AutoOperateAnnotatedElement autoOperateType;

        /**
         * Default serializer
         */
        private final transient JsonSerializer<Object> serializer;

        /**
         * Constructor.
         *
         * @param targetType target type
         * @param autoOperateType auto operate element for target type
         */
        protected AutoOperateSerializer(Class<Object> targetType, AutoOperateAnnotatedElement autoOperateType, JsonSerializer<Object> serializer) {
            super(targetType);
            this.autoOperateType = autoOperateType;
            this.serializer = serializer;
        }

        @SuppressWarnings("all")
        @Override
        public void serialize(Object target, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (Objects.isNull(target)) {
                return;
            }
            // target already processed?
            if (!context.process(target)) {
                serializer.serialize(target, jsonGenerator, serializerProvider);
                return;
            }
            try {
                JsonNode jsonNode = objectMapper.valueToTree(target);
                autoOperateType.execute(Collections.singletonList(jsonNode));
                jsonGenerator.writeTree(jsonNode);
            } catch (Throwable ex) {
                throw new OperationExecuteException(ex);
            } finally {
                // anyway, the record needs to be removed after the operation is completed
                context.processed(target);
            }
        }
    }
}
