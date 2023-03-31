package cn.crane4j.core.support.container;

import cn.crane4j.annotation.ContainerMethod;
import cn.crane4j.core.support.AnnotationFinder;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * {@link AbstractMethodContainerAnnotationProcessor} implementation based on {@link AnnotationFinder}
 *
 * @author huangchengxing
 */
public class AnnotationFinderMethodContainerPostProcessor extends AbstractMethodContainerAnnotationProcessor {

    /**
     * annotation finder
     */
    protected final AnnotationFinder annotationFinder;

    /**
     * Create a {@link AbstractMethodContainerAnnotationProcessor} instance.
     *
     * @param methodContainerFactories method container factories
     * @param annotationFinder annotation finder
     */
    public AnnotationFinderMethodContainerPostProcessor(
        Collection<MethodContainerFactory> methodContainerFactories, AnnotationFinder annotationFinder) {
        super(methodContainerFactories);
        this.annotationFinder = annotationFinder;
    }

    /**
     * Resolve annotations for class.
     *
     * @param type type
     * @return annotations
     */
    @Override
    protected Collection<ContainerMethod> resolveAnnotationsForClass(Class<?> type) {
        return annotationFinder.findAllAnnotations(type, ContainerMethod.class);
    }

    /**
     * Resolve annotations for class.
     *
     * @param method method
     * @return annotations
     */
    @Override
    protected Collection<ContainerMethod> resolveAnnotationsForMethod(Method method) {
        return annotationFinder.findAllAnnotations(method, ContainerMethod.class);
    }
}
