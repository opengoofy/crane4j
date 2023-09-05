package cn.crane4j.extension.jackson;

import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.aop.AutoOperateAnnotatedElementResolver;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Json node auto operate module.
 *
 * @author huangchengxing
 * @see JsonNodeAutoOperateSerializerModifier
 */
public class JsonNodeAutoOperateModule extends Module {

    public static final String MODULE_NAME = JsonNodeAutoOperateModule.class.getSimpleName();

    private final AutoOperateAnnotatedElementResolver elementResolver;
    private final ObjectMapper objectMapper;
    private final AnnotationFinder annotationFinder;

    /**
     * Constructor
     *
     * @param elementResolver element resolver
     * @param objectMapper object mapper
     * @param annotationFinder annotation finder
     */
    public JsonNodeAutoOperateModule(
        AutoOperateAnnotatedElementResolver elementResolver, ObjectMapper objectMapper, AnnotationFinder annotationFinder) {
        this.elementResolver = elementResolver;
        this.objectMapper = objectMapper;
        this.annotationFinder = annotationFinder;
    }

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    @Override
    public void setupModule(SetupContext setupContext) {
        setupContext.addBeanSerializerModifier(new JsonNodeAutoOperateSerializerModifier(elementResolver, objectMapper, annotationFinder));
    }

    @Override
    public Object getTypeId() {
        return MODULE_NAME;
    }
}
