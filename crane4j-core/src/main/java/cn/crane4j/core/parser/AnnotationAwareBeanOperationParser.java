package cn.crane4j.core.parser;

import cn.crane4j.annotation.Assemble;
import cn.crane4j.annotation.Disassemble;
import cn.crane4j.annotation.MappingTemplate;
import cn.crane4j.annotation.Operations;
import cn.crane4j.core.container.Container;
import cn.crane4j.core.container.ContainerProvider;
import cn.crane4j.core.exception.Crane4jException;
import cn.crane4j.core.executor.BeanOperationExecutor;
import cn.crane4j.core.executor.handler.AssembleOperationHandler;
import cn.crane4j.core.executor.handler.DisassembleOperationHandler;
import cn.crane4j.core.support.AnnotationFinder;
import cn.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.crane4j.core.support.Sorted;
import cn.crane4j.core.util.CollectionUtils;
import cn.crane4j.core.util.ConfigurationUtil;
import cn.crane4j.core.util.ReflectUtils;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Annotation-based {@link BeanOperationParser} implementation.<br />
 * Support the construction of operation configuration
 * by resolving annotations based on {@link Assemble}, {@link Disassemble}
 * and {@link Operations} on classes and attributes.<br />
 *
 * <p>The parser will ensure the sorting between assembly and disassembly operations
 * in {@link BeanOperations} according to the configured sorting value,
 * which can be changed by specifying a comparator when the constructor calls.<br />
 * It should be noted that this order does not represent the order in which the final operation will be executed.
 * This order is guaranteed by the executor {@link BeanOperationExecutor}.
 *
 * @author huangchengxing
 * @see Assemble
 * @see Disassemble
 * @see Operations
 */
@Slf4j
public class AnnotationAwareBeanOperationParser implements BeanOperationParser {

    protected static final String ANNOTATION_KEY_ATTRIBUTE = "key";
    private static final Map<Class<?>, MetaDataCache> META_DATA_CACHES = CollectionUtils.newWeakConcurrentMap();

    protected final AnnotationFinder annotationFinder;
    protected final Crane4jGlobalConfiguration globalConfiguration;
    private final Comparator<KeyTriggerOperation> operationComparator;
    private final Map<Class<?>, BeanOperations> parsedBeanOperations = new ConcurrentHashMap<>(32);

    /**
     * Create an operation parser that supports annotation configuration.
     *
     * @param annotationFinder annotation finder
     * @param globalConfiguration global configuration
     * @param operationComparator operation comparator
     */
    public AnnotationAwareBeanOperationParser(
        AnnotationFinder annotationFinder,
        Crane4jGlobalConfiguration globalConfiguration,
        Comparator<KeyTriggerOperation> operationComparator) {
        this.annotationFinder = annotationFinder;
        this.globalConfiguration = globalConfiguration;
        this.operationComparator = operationComparator;
    }

    /**
     * <p>Create an operation parser that supports annotation configuration.<br />
     * The order of operation configurations is {@link Sorted#getSort} from small to large.
     *
     * @param annotationFinder annotation finder
     * @param globalConfiguration global configuration
     */
    public AnnotationAwareBeanOperationParser(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration) {
        this(annotationFinder, globalConfiguration, Sorted.comparator());
    }

    /**
     * <p>Parse the class and class attribute information,
     * and generate the corresponding {@link BeanOperations} instance.<br />
     * If there is a cache, it will be obtained from the cache first.
     *
     * @param beanType bean type
     * @return {@link BeanOperations}
     */
    @NonNull
    @Override
    public BeanOperations parse(Class<?> beanType) {
        Objects.requireNonNull(beanType);
        return MapUtil.computeIfAbsent(parsedBeanOperations, beanType, type -> {
            BeanOperations beanOperations = createBeanOperations(type);
            beanOperations.setActive(false);
            parsedBeanOperations.put(type, beanOperations);
            doParse(type, beanOperations);
            beanOperations.setActive(true);
            return beanOperations;
        });
    }

    /**
     * Create {@link BeanOperations} instance
     *
     * @param beanType bean type
     * @return {@link BeanOperations}
     */
    protected BeanOperations createBeanOperations(Class<?> beanType) {
        return new SimpleBeanOperations(beanType);
    }

    /**
     * Parse {@link Assemble} annotations for class.
     *
     * @param beanType bean type
     * @return {@link Assemble}
     * @see #parseAnnotationForDeclaredFields
     */
    protected List<Assemble> parseAssembleAnnotations(Class<?> beanType) {
        return parseAnnotationForDeclaredFields(beanType, Assemble.class, (a, f) -> {
            // force value to be set to the annotated attribute name
            ReflectUtils.setAttributeValue(a, ANNOTATION_KEY_ATTRIBUTE, f.getName());
            return a;
        });
    }

    /**
     * Parse {@link Disassemble} annotations for class.
     *
     * @param beanType bean type
     * @return {@link Disassemble}
     * @see #parseAnnotationForDeclaredFields
     */
    protected List<Disassemble> parseDisassembleAnnotations(Class<?> beanType) {
        return parseAnnotationForDeclaredFields(beanType, Disassemble.class, (a, f) -> {
            // force value to be set to the annotated attribute name
            ReflectUtils.setAttributeValue(a, ANNOTATION_KEY_ATTRIBUTE, f.getName());
            return a;
        });
    }

    /**
     * Parse {@link Operations} annotations for class.
     *
     * @param beanType bean type
     * @return {@link Operations}
     */
    protected Operations parseOperationsAnnotation(Class<?> beanType) {
        return annotationFinder.findAnnotation(beanType, Operations.class);
    }

    private void doParse(Class<?> beanType, BeanOperations beanOperations) {
        Set<Class<?>> accessed = new HashSet<>();
        Deque<Class<?>> typeQueue = new LinkedList<>();
        typeQueue.add(beanType);

        List<AssembleOperation> assembleOperations = new ArrayList<>();
        List<DisassembleOperation> disassembleOperations = new ArrayList<>();
        while (!typeQueue.isEmpty()) {
            Class<?> type = typeQueue.removeFirst();
            accessed.add(type);

            // Existing metadata cache?
            MetaDataCache cache = MapUtil.computeIfAbsent(
                META_DATA_CACHES, type, this::createMetaDataCache
            );
            cache.append(assembleOperations, disassembleOperations);

            // Then find superclass and interfaces
            Class<?> superclass = type.getSuperclass();
            if (Objects.nonNull(superclass) && !Objects.equals(superclass, Object.class) && !accessed.contains(superclass)) {
                typeQueue.add(superclass);
            }
            CollUtil.addAll(typeQueue, type.getInterfaces());
        }

        // before registering an operation, sort it according to the sort value
        assembleOperations.sort(operationComparator);
        assembleOperations.forEach(beanOperations::putAssembleOperations);
        disassembleOperations.sort(operationComparator);
        disassembleOperations.forEach(beanOperations::putDisassembleOperations);
    }

    /**
     * Create {@link AssembleOperation} instance from annotation.
     *
     * @param type type
     * @param annotation annotation
     * @return {@link AssembleOperation}
     */
    protected AssembleOperation parseAssembleOperation(Class<?> type, Assemble annotation) {
        Assert.isTrue(CharSequenceUtil.isNotBlank(annotation.key()), throwException("the key of assemble operation must not blank"));
        // get operation handler
        AssembleOperationHandler assembleOperationHandler = ConfigurationUtil.getAssembleOperationHandler(
            globalConfiguration, annotation.handlerName(), annotation.handler()
        );
        Assert.notNull(assembleOperationHandler, throwException("assemble operation handler [{}]({}) not found", annotation.handlerName(), annotation.handler()));

        // resolved property mapping from annotation and template
        Set<PropertyMapping> propertyMappings = Stream.of(annotation.props())
            .map(m -> new SimplePropertyMapping(m.src(), CharSequenceUtil.isEmpty(m.ref()) ? annotation.key() : m.ref()))
            .collect(Collectors.toSet());
        List<PropertyMapping> templateMappings = parsePropTemplate(annotation);
        if (CollUtil.isNotEmpty(templateMappings)) {
            propertyMappings.addAll(templateMappings);
        }

        // get container
        Container<?> container = getContainer(annotation);

        // create operation
        AssembleOperation operation = new SimpleAssembleOperation(
            annotation.key(), annotation.sort(),
            propertyMappings, container, assembleOperationHandler
        );
        operation.getGroups().addAll(Arrays.asList(annotation.groups()));
        return operation;
    }

    /**
     * Get container.
     *
     * @param annotation annotation
     * @return container
     * @throws IllegalArgumentException thrown when the container is null
     */
    protected Container<?> getContainer(Assemble annotation) {
        // determine provider
        ContainerProvider provider = ConfigurationUtil.getContainerProvider(
            globalConfiguration, annotation.containerProviderName(), annotation.containerProvider()
        );
        provider = ObjectUtil.defaultIfNull(provider, globalConfiguration);
        // get from provider
        Container<?> container = CharSequenceUtil.isNotEmpty(annotation.container()) ?
            provider.getContainer(annotation.container()) : Container.empty();
        Assert.notNull(
            container, throwException("cannot find container [{}] from provider [{}]", annotation.container(), provider.getClass())
        );
        return container;
    }

    @NonNull
    private List<PropertyMapping> parsePropTemplate(Assemble annotation) {
        return Stream.of(annotation.propTemplates())
            .map(t -> annotationFinder.findAnnotation(t, MappingTemplate.class))
            .map(MappingTemplate::value)
            .flatMap(Stream::of)
            .map(m -> new SimplePropertyMapping(m.src(), m.ref()))
            .collect(Collectors.toList());
    }

    /**
     * Create {@link DisassembleOperation} instance from annotation.
     *
     * @param type type
     * @param annotation annotation
     * @return {@link DisassembleOperation}
     */
    protected DisassembleOperation parseDisassembleOperation(Class<?> type, Disassemble annotation) {
        // get parser
        Assert.isTrue(CharSequenceUtil.isNotBlank(annotation.key()), throwException("the key of disassemble operation must not blank"));
        BeanOperationParser parser = ConfigurationUtil.getParser(globalConfiguration, annotation.parserName(), annotation.parser());
        Assert.notNull(parser, throwException("bean operations parser [{}]({}) not found", annotation.parserName(), annotation.parser()));

        // get handler
        DisassembleOperationHandler disassembleOperationHandler = ConfigurationUtil.getDisassembleOperationHandler(
            globalConfiguration, annotation.handlerName(), annotation.handler()
        );
        Assert.notNull(disassembleOperationHandler, throwException("disassemble handler [{}]({}) not found", annotation.handlerName(), annotation.parser()));

        // wait until runtime to dynamically determine the actual type if no type is specified
        DisassembleOperation operation;
        if (Objects.equals(Object.class, annotation.type()) || Objects.equals(Void.TYPE, annotation.type())) {
            operation = new TypeDynamitedDisassembleOperation(
                annotation.key(), annotation.sort(),
                type, disassembleOperationHandler, parser,
                globalConfiguration.getTypeResolver()
            );
        }
        // complete the parsing now if the type has been specified in the annotation
        else {
            BeanOperations operations = parser.parse(annotation.type());
            operation = new TypeFixedDisassembleOperation(
                annotation.key(), annotation.sort(),
                type, operations, disassembleOperationHandler
            );
        }

        // set group
        operation.getGroups().addAll(Arrays.asList(annotation.groups()));
        return operation;
    }

    /**
     * Parse annotations for declared fields of {@code beanType}.
     *
     * @param beanType bean type
     * @param annotationType annotation type
     * @param mapper mapper for annotation and field
     * @return annotations
     */
    protected final <T extends Annotation> List<T> parseAnnotationForDeclaredFields(
        Class<?> beanType, Class<T> annotationType, BiFunction<T, Field, T> mapper) {
        Field[] fields = ReflectUtils.getDeclaredFields(beanType);
        List<T> results = new ArrayList<>(fields.length);
        for (Field field : fields) {
            Set<T> annotation = annotationFinder.findAllAnnotations(field, annotationType);
            if (CollUtil.isEmpty(annotation)) {
                continue;
            }
            for (T t : annotation) {
                t = mapper.apply(t, field);
                results.add(t);
            }
        }
        return results;
    }

    private MetaDataCache createMetaDataCache(Class<?> type) {
        Collection<Assemble> assembles = parseAssembleAnnotations(type);
        Collection<Disassemble> disassembles = parseDisassembleAnnotations(type);
        Operations operations = parseOperationsAnnotation(type);
        if (Objects.nonNull(operations)) {
            CollUtil.addAll(assembles, operations.assembles());
            CollUtil.addAll(disassembles, operations.disassembles());
        }
        List<AssembleOperation> assembleOperations = assembles.stream()
            .map(annotation -> parseAssembleOperation(type, annotation))
            .sorted(operationComparator)
            .collect(Collectors.toList());
        List<DisassembleOperation> disassembleOperations = disassembles.stream()
            .map(annotation -> parseDisassembleOperation(type, annotation))
            .sorted(operationComparator)
            .collect(Collectors.toList());
        return new MetaDataCache(assembleOperations, disassembleOperations);
    }

    /**
     * Get supplier of {@link Crane4jException}.
     *
     * @param errTemp errTemp
     * @param args args
     * @return supplier of exception
     */
    protected static Supplier<Crane4jException> throwException(String errTemp, Object... args) {
        return () -> new Crane4jException(errTemp, args);
    }

    /**
     * Annotation metadata cache on class.
     */
    @RequiredArgsConstructor
    private static class MetaDataCache {
        private final List<AssembleOperation> assembleOperations;
        private final List<DisassembleOperation> disassembleOperations;
        public void append(
            List<AssembleOperation> assembleOperations, List<DisassembleOperation> disassembleOperations) {
            assembleOperations.addAll(this.assembleOperations);
            disassembleOperations.addAll(this.disassembleOperations);
        }
    }
}
