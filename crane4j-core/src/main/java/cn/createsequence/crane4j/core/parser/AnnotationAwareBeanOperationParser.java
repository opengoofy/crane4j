package cn.createsequence.crane4j.core.parser;

import cn.createsequence.crane4j.core.annotation.Assemble;
import cn.createsequence.crane4j.core.annotation.Disassemble;
import cn.createsequence.crane4j.core.annotation.MappingTemplate;
import cn.createsequence.crane4j.core.annotation.Operations;
import cn.createsequence.crane4j.core.container.Container;
import cn.createsequence.crane4j.core.exception.CraneException;
import cn.createsequence.crane4j.core.executor.AssembleOperationHandler;
import cn.createsequence.crane4j.core.executor.BeanOperationExecutor;
import cn.createsequence.crane4j.core.executor.DisassembleOperationHandler;
import cn.createsequence.crane4j.core.support.AnnotationFinder;
import cn.createsequence.crane4j.core.support.Crane4jGlobalConfiguration;
import cn.createsequence.crane4j.core.support.Sorted;
import cn.createsequence.crane4j.core.util.CollectionUtils;
import cn.createsequence.crane4j.core.util.ReflectUtils;
import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>基于注解的{@link BeanOperationParser}实现。
 * 支持通过解析类和属性上的基于{@link Assemble}和{@link Disassemble}
 * 以及{@link Operations}注解来构建操作配置。<br />
 * 除了支持解析直接存在的与类及其字段上注释，还支持解析父类或接口、以及父类属性上的注解。
 *
 * <p>解析器将会按配置的排序值保证得到的{@link BeanOperations}中装配和拆卸操作之间的排序，
 * 该顺序可以通过在构造器调用时指定比较器来改变。<br />
 * 需要注意的是，该顺序不代表最终操作被执行的顺序，该顺序由执行器{@link BeanOperationExecutor}保证。
 *
 * @author huangchengxing
 * @see Assemble
 * @see Disassemble
 * @see Operations
 */
@Slf4j
public class AnnotationAwareBeanOperationParser implements BeanOperationParser {

    private static final String ANNOTATION_KEY_ATTRIBUTE = "key";
    private static final Map<Class<?>, MetaDataCache> META_DATA_CACHES = CollectionUtils.newWeakConcurrentMap();

    private final AnnotationFinder annotationFinder;
    private final Crane4jGlobalConfiguration globalConfiguration;
    private final Comparator<KeyTriggerOperation> operationComparator;
    private final Map<Class<?>, BeanOperations> parsedBeanOperations = new ConcurrentHashMap<>(32);

    /**
     * 创建一个支持注解配置的操作解析器
     *
     * @param annotationFinder 注解查找器
     * @param globalConfiguration 全局配置
     * @param operationComparator 操作排序比较器
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
     * 创建一个支持注解配置的操作解析器，操作配置之间的顺序按{@link Sorted#getSort}由小到大
     *
     * @param annotationFinder 注解查找器
     * @param globalConfiguration 全局配置
     */
    public AnnotationAwareBeanOperationParser(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration) {
        this(annotationFinder, globalConfiguration, Comparator.comparing(KeyTriggerOperation::getSort));
    }

    /**
     * 解析类及类属性信息，生成对应的{@link BeanOperations}实例
     *
     * @param beanType 类
     * @return {@link BeanOperations}实例，该实例可能存在缓存
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
     * 创建操作配置实例
     *
     * @param beanType 正在解析的类
     * @return {@link BeanOperations}
     */
    protected BeanOperations createBeanOperations(Class<?> beanType) {
        return new SimpleBeanOperations(beanType);
    }

    /**
     * 解析类中的{@link Assemble}注解
     *
     * @param beanType 正在解析的类
     * @return {@link Assemble}
     */
    protected List<Assemble> parseAssembleAnnotations(Class<?> beanType) {
        return parseAnnotationForDeclaredFields(beanType, Assemble.class);
    }

    /**
     * 解析类中的{@link Disassemble}注解
     *
     * @param beanType 正在解析的类
     * @return {@link Disassemble}
     */
    protected List<Disassemble> parseDisassembleAnnotations(Class<?> beanType) {
        return parseAnnotationForDeclaredFields(beanType, Disassemble.class);
    }

    /**
     * 解析类上的{@link Operations}
     *
     * @param beanType 正在解析的类
     * @return {@link Operations}
     */
    protected Collection<Operations> parseOperationsAnnotations(Class<?> beanType) {
        return annotationFinder.findAllAnnotations(beanType, Operations.class);
    }

    private void doParse(Class<?> beanType, BeanOperations beanOperations) {
        Set<Class<?>> accessed = new HashSet<>();
        Deque<Class<?>> typeQueue = new LinkedList<>();
        typeQueue.add(beanType);

        List<AssembleOperation> assembleOperations = new ArrayList<>();
        List<DisassembleOperation> disassembleOperations = new ArrayList<>();
        while (!typeQueue.isEmpty()) {
            Class<?> type = typeQueue.removeFirst();
            if (Objects.equals(Object.class, type) || accessed.contains(type)) {
                continue;
            }
            accessed.add(type);

            // 已有元数据缓存？
            MetaDataCache cache = MapUtil.computeIfAbsent(
                META_DATA_CACHES, type, this::createMetaDataCache
            );
            cache.append(assembleOperations, disassembleOperations);

            // 接着查找其父类和接口
            typeQueue.add(type.getSuperclass());
            CollUtil.addAll(typeQueue, type.getInterfaces());
        }

        // 在注册操作前，先根据排序值对其进行排序
        assembleOperations.sort(operationComparator);
        assembleOperations.forEach(beanOperations::putAssembleOperations);
        disassembleOperations.sort(operationComparator);
        disassembleOperations.forEach(beanOperations::putDisassembleOperations);
    }

    private AssembleOperation parseAssembleOperation(Assemble annotation) {
        // 根据注解构建操作配置
        Assert.isTrue(CharSequenceUtil.isNotBlank(annotation.key()), throwException("the key of assemble operation must not blank"));
        Container<?> container = CharSequenceUtil.isNotEmpty(annotation.namespace()) ?
            globalConfiguration.getContainer(annotation.namespace()) : Container.empty();
        Assert.notNull(container, throwException("container [{}] not found", annotation.namespace()));
        AssembleOperationHandler assembleOperationHandler = globalConfiguration.getAssembleOperationHandler(annotation.handler());
        Assert.notNull(assembleOperationHandler, throwException("assemble operation handler [{}] not found", annotation.handler()));
        Set<PropertyMapping> propertyMappings = Stream.of(annotation.props())
            .map(m -> new SimplePropertyMapping(m.src(), CharSequenceUtil.isEmpty(m.ref()) ? annotation.key() : m.ref()))
            .collect(Collectors.toSet());
        // 解析映射配置模板
        List<PropertyMapping> templateMappings = parsePropTemplate(annotation);
        if (CollUtil.isNotEmpty(templateMappings)) {
            propertyMappings.addAll(templateMappings);
        }
        AssembleOperation operation = new SimpleAssembleOperation(
            annotation.key(), annotation.sort(),
            propertyMappings, container, assembleOperationHandler
        );
        operation.getGroups().addAll(Arrays.asList(annotation.groups()));
        return operation;
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

    private DisassembleOperation parseDisassembleOperation(Class<?> sourceType, Disassemble annotation) {
        // 获取配置解析器与操作处理器
        Assert.isTrue(CharSequenceUtil.isNotBlank(annotation.key()), throwException("the key of disassemble operation must not blank"));
        BeanOperationParser parser = globalConfiguration.getBeanOperationsParser(annotation.parser());
        Assert.notNull(parser, throwException("bean operations parser [{}] not found", annotation.parser()));
        DisassembleOperationHandler disassembleOperationHandler = globalConfiguration.getDisassembleOperationHandler(annotation.handler());
        Assert.notNull(disassembleOperationHandler, throwException("disassemble handler [{}] not found", annotation.handler()));
        // 若未在注解中指定嵌套对象的类型，则等到实际处理数据时再动态解析其类型
        DisassembleOperation operation;
        if (Objects.equals(Object.class, annotation.type()) || Objects.equals(Void.TYPE, annotation.type())) {
            operation = new TypeDynamitedDisassembleOperation(
                annotation.key(), annotation.sort(),
                sourceType, disassembleOperationHandler, parser,
                globalConfiguration.getTypeResolver()
            );
        }
        // 已经在注解中指定了嵌套对象的属性，则照顾完成操作配置的解析
        else {
            BeanOperations operations = parser.parse(annotation.type());
            operation = new TypeFixedDisassembleOperation(
                annotation.key(), annotation.sort(),
                sourceType, operations, disassembleOperationHandler
            );
        }
        // 设置组别
        operation.getGroups().addAll(Arrays.asList(annotation.groups()));
        return operation;
    }

    private <T extends Annotation> List<T> parseAnnotationForDeclaredFields(Class<?> beanType, Class<T> annotationType) {
        Field[] fields = ReflectUtils.getDeclaredFields(beanType);
        List<T> results = new ArrayList<>(fields.length);
        for (Field field : fields) {
            Set<T> annotation = annotationFinder.findAllAnnotations(field, annotationType);
            if (CollUtil.isEmpty(annotation)) {
                continue;
            }
            for (T t : annotation) {
                // 如果注解在属性上，则强制将value设置为被注解的属性名称
                AnnotationUtil.setValue(t, ANNOTATION_KEY_ATTRIBUTE, field.getName());
                results.add(t);
            }
        }
        return results;
    }

    private MetaDataCache createMetaDataCache(Class<?> type) {
        // 解析类中的注解配置
        Collection<Assemble> assembles = parseAssembleAnnotations(type);
        Collection<Disassemble> disassembles = parseDisassembleAnnotations(type);
        Collection<Operations> operations = parseOperationsAnnotations(type);
        operations.forEach(op -> {
            CollUtil.addAll(assembles, op.assembles());
            CollUtil.addAll(disassembles, op.disassembles());
        });
        // 将装配/拆卸配置添加到操作配置中
        List<AssembleOperation> assembleOperations = assembles.stream()
            .map(this::parseAssembleOperation)
            .sorted(operationComparator)
            .collect(Collectors.toList());
        List<DisassembleOperation> disassembleOperations = disassembles.stream()
            .map(annotation -> parseDisassembleOperation(type, annotation))
            .sorted(operationComparator)
            .collect(Collectors.toList());
        return new MetaDataCache(assembleOperations, disassembleOperations);
    }

    private static Supplier<CraneException> throwException(String errTemp, Object... args) {
        return () -> new CraneException(errTemp, args);
    }

    /**
     * 类上的注解元数据缓存
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
