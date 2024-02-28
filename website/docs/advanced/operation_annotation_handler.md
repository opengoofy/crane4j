# 注解处理器

在`crane4j`中，你可以通过各种注解去声明操作配置，比如 `@Assemble`、`@Disassemble`、`@AssembleEnum` 和 `@AssembleMp`，我们称这用于声明操作的注解为**操作注解**。它们底层实际上依赖于解析器中对应的操作注解处理器 `OperationAnnotationHandler` 实现。

`crane4j` 在这部分功能使用了非常典型的责任链模式。在开始解析配置前，我们向配置解析器 `TypeHierarchyBeanOperationParser` 注册一系列注解处理器，而每个处理器都用于处理某个特定的注解。

当我们将一个需要解析的 `AnnotatedElement` 传递给`Parser`时，`Parser` 将创建一个 `BeanOperations` 配置对象，并驱动它在处理器链上流转。每个解析器根据规则将 `AnnotatedElement` 上的特定注解解析为对应的装配或拆卸配置。

下图展示了解析器的工作流程：

![](http://img.xiajibagao.top/%E6%97%A0%E6%A0%87%E9%A2%98-2023-06-04-1303.png)

## 1.内置处理器

`crane4j`目前提供了七个内置的操作注解处理器：

| 处理器                              | 注解                | 对应操作类型                    |
| ----------------------------------- | ------------------- | ------------------------------- |
| `DisassembleAnnotationHandler`      | `@Disassemble`      | 拆卸操作 `DisassembleOperation` |
| `AssembleAnnotationHandler`         | `@Assemble`         | 装配操作 `AssembleOperation`    |
| `AssembleEnumAnnotationHandler`     | `@AssembleEnum`     | 装配操作 `AssembleOperation`    |
| `AssembleConstantAnnotationHandler` | `@AssembleConstant` | 装配操作 `AssembleOperation`    |
| `AssembleMethodAnnotationHandler`   | `@AssembleMethod`   | 装配操作 `AssembleOperation`    |
| `AssembleMpAnnotationHandler`       | `@AssembleMp`       | 装配操作 `AssembleOperation`    |
| `AssembleKeyAnnotationHandler`      | `@AssembleKey`      | 装配操作 `AssembleOperation`    |

## 2.自定义注解处理器

通常情况下，你直接基于 `OperationAnnotationHandler` 接口定义你自己的注解处理器，不过你也可以根据情况下选择一个模板类来节约一些功夫，它们按抽象程度从高到低分别为：

- `AbstractStandardOperationAnnotationHandler`：标准操作注解处理器，你可以基于它实现支持装配或拆卸操作的注解处理器；
- `AbstractStandardAssembleAnnotationHandler`：标准装配操作注解处理器，你可以基于它实现支持装配操作的注解处理器；
- `InternalProviderAssembleAnnotationHandler`：基于自定义容器的装配操作注解处理器，功能同上，不过你可以在解析注解时自动态的指定数据源容器。它也是最常用的注解处理器。

举个例子，假如我们有一张字典表，它的结构为 “一级分类-二级分类-字典项ID”，我们希望有一个注解可以直接对其进行填充：

~~~java
@Dict(
  category = "一级分类", type = "二级分类", 
  prop = @Mapping(ref = "dictName")
)
private Integer dictId;
private String dictName;
~~~

**定义注解**

你需要根据你的需要定义一个注解，为了兼容 crane4j 默认支持的操作过滤、筛选、条件注解等功能，推荐为你的自定义注解一并加上一些通用属性：

~~~java
@Documented
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Dict {

    String category() default ""; // 一级分类
    String type() default ""; // 二级分类
  
  	// 通用属性
    String id() default "";
    String key() default "";
    int sort() default Integer.MAX_VALUE;
    Mapping[] props() default { };
    Class<?>[] propTemplates() default {};
    String[] groups() default {};
    String propertyMappingStrategy() default "";
}
~~~

**定义注解处理器**

在定义了对应注解后，我们可以基于 `InternalProviderAssembleAnnotationHandler` 实现对应的注解处理器，你只需要实现三个抽象方法即可：

~~~java
@Component
public class DictAnnotationHandler extends InternalProviderAssembleAnnotationHandler<Dict> {

    // 通过依赖注入获取字典服务
    @Autowired
    private DictService dictService;

    public DictAnnotationHandler(
        AnnotationFinder annotationFinder, Crane4jGlobalConfiguration globalConfiguration,
        PropertyMappingStrategyManager propertyMappingStrategyManager) {
        super(
            Dict.class, annotationFinder, Crane4jGlobalSorter.comparator(),
            globalConfiguration, propertyMappingStrategyManager
        );
    }

    @Override
    protected StandardAssembleAnnotation<Dict> getStandardAnnotation(
        BeanOperations beanOperations, AnnotatedElement element, Dict annotation) {
        // 获取注解的通用属性
        return StandardAssembleAnnotationAdapter.<Dict>builder()
            .annotatedElement(element)
            .annotation(annotation)
            .id(annotation.id())
            .key(annotation.key())
            .sort(annotation.sort())
            .groups(annotation.groups())
            .mappingTemplates(annotation.propTemplates())
            .props(annotation.props())
            .propertyMappingStrategy(annotation.propertyMappingStrategy())
            .build();
    }

    @Override
    protected @NonNull Container<Object> createContainer(
        StandardAssembleAnnotation<Dict> standardAnnotation, String namespace) {
        Dict annotation = standardAnnotation.getAnnotation();
        // 创建一个数据源容器，当调用时，会根据字典分类和字典类型从服务接口中获取字典项
        return Containers.forLambda(namespace, dictIds -> {
            // 根据字典分类查询关联的字典项，并按照字典项ID分组
            List<DictDO> dictItems = dictService.listDicts(annotation.category(), annotation.type(), dictIds);
            return dictItems.stream().collect(Collectors.toMap(DictDO::getId, Function.identity()));
        });
    }

    @Override
    protected String determineNamespace(StandardAssembleAnnotation<Dict> standardAnnotation) {
        // 返回字典分类和字典类型组合的字符串作为命名空间
        // 例如：category#type
        // 我们通过这种方式来避免重复创建相同的容器
        return standardAnnotation.getAnnotation().category() + "#" + standardAnnotation.getAnnotation().type();
    }
}
~~~

**注册注解处理器**

在 Spring 环境中，你只需要将该注解处理器交由 Spring 容器管理即可，启动后 crane4j 会自动进行注册。

而在非 spring 环境，你可以直接将其注册到配置解析器 `TypeHierarchyBeanOperationParser` 即可生效：

~~~java
// 获取配置解析器
Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
TypeHierarchyBeanOperationParser parser = configuration.getBeanOperationParser(TypeHierarchyBeanOperationParser.class);

// 注册注解处理器
DictAnnotationHandler handler = new DictAnnotationHandler(
	new SimpleAnnotationFinder(), configuration, configuration
);
parser.addOperationAnnotationHandler(handler);
~~~
