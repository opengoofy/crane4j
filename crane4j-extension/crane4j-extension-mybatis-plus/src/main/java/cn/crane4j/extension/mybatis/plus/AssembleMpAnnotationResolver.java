package cn.crane4j.extension.mybatis.plus;

import cn.crane4j.annotation.AssembleMp;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.parser.AssembleOperation;
import cn.crane4j.core.parser.KeyTriggerOperation;
import cn.crane4j.core.parser.OperationAnnotationResolver;
import cn.crane4j.core.parser.StandardAssembleAnnotationResolver;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Sorted;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Comparator;

/**
 * <p>The implementation of {@link OperationAnnotationResolver}.<br />
 * It's used to scan the {@link AssembleMp} annotations on classes and their attributes,
 * And generate {@link AssembleOperation} for it
 * using {@link MpMethodContainer} as the data source container.
 *
 * @author huangchengxing
 * @see AssembleMp
 * @see MpBaseMapperContainerRegister
 * @since 1.2.0
 */
@Accessors(chain = true)
public class AssembleMpAnnotationResolver extends StandardAssembleAnnotationResolver<AssembleMp> {

    private final MpBaseMapperContainerRegister mapperContainerRegister;

    /**
     * Create a {@link AssembleMpAnnotationResolver} instance.
     *
     * @param annotationFinder annotation finder
     * @param mapperContainerRegister mp method container provider
     * @param globalConfiguration global configuration
     */
    public AssembleMpAnnotationResolver(
        AnnotationFinder annotationFinder,
        MpBaseMapperContainerRegister mapperContainerRegister,
        Crane4jGlobalConfiguration globalConfiguration) {
        this(annotationFinder, Sorted.comparator(), mapperContainerRegister, globalConfiguration);
    }

    /**
     * Create a {@link AssembleMpAnnotationResolver} instance.
     *
     * @param annotationFinder annotation finder
     * @param operationComparator operation comparator
     * @param mapperContainerRegister mp method container provider
     * @param globalConfiguration global configuration
     */
    public AssembleMpAnnotationResolver(
        AnnotationFinder annotationFinder, Comparator<KeyTriggerOperation> operationComparator,
        MpBaseMapperContainerRegister mapperContainerRegister,
        Crane4jGlobalConfiguration globalConfiguration) {
        super(AssembleMp.class, annotationFinder, operationComparator, globalConfiguration);
        this.mapperContainerRegister = mapperContainerRegister;
    }

    /**
     * Get container from given {@code annotation}.
     *
     * @param annotation annotation
     * @return {@link Container} instance
     */
    @Override
    protected Container<?> getContainer(AssembleMp annotation) {
        return mapperContainerRegister.getContainer(
            annotation.mapper(), annotation.where(), Arrays.asList(annotation.selects())
        );
    }
}
