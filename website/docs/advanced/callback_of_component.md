# 组件的回调接口

为了便于用户对在 crane4j 各个组件的特定阶段插入自定义逻辑，crane4j 提供了一套类似 Spring 的回调机制。

## 1.容器生命周期处理器

容器生命周期处理器 `ContainerLifecycleProcessor` 是用于感知并干涉容器生命周期的特色组件，它提个三个抽象方法：

- `whenRegistered`：容器注册前回调，通过该方法获得要注册的容器示例或工厂方法；
- `whenCreated`：容器创建后，通过该方法可以获取创建后即将加入缓存的容器实例；
- `whenDestroyed`：容器销毁后回调，通过该方法可以获得被替换或者删除的容器；

`crane4j` 基于这套机制已经提供了三种实现：

- `CacheableContainerProcessor`：在用户的容器注册前，将会把要注册的容器对象包装并替换具备缓存功能的增强容器；
- `ContainerRegisterLogger`：在容器生命周期的三个关键节点输出相关信息；
- `ContainerInstanceLifecycleProcessor`：用于支持 `Container.Lifecycle` 的 `init` 和 `destroy` 方法。

当你创建了一个处理器时，你可以通过全局配置 `Crane4jGlobalConfiguration` 的 `registerContainerLifecycleProcessor` 方法注册它。

而如果在 Spring 环境，你只需将其交给 Spring 管理即可，在启动后 crane4j 会自动注册。

## 2.容器生命周期接口

除针对全局容器的生命周期处理器外，crane4j 也提供了容器自身生命周期的回调接口 `Container.Lifecycle`，当一个容器类实现了 `Container.Lifecycle` 接口后，相关的回调方法会在这个容器的特定生命周期节点被调用。

比如：

```java
@Component
@Getter
@RequiredArgsConstructor
public class Foo implements Container<String>, Container.Lifecycle {
    private final String namespace = "AllDictContainer";
    private final DictService dictService;
    private Map<String, DictDO> allDicts = new HashMap<>();
    
    @Override
    public void init() {
        // 初始化方法
        allDicts = dictService.findAll()
            .stream().collect(Collectors.toMap(DictDO::getId, Function.identity()));
    }
    
    @Override
    public void destroy() {
        // 销毁方法
        allDicts.clear();
    }
    
    @Override
    public Map<String, ?> get(Collection<String> keys) {
        return allDicts;
    }
}
```

在上述示例中，我们创建了一个缓存全部字典项的容器 `Foo`，它实现了 `Container.Lifecycle` 接口。

在容器被使用时，会调用 `init()` 方法进行初始化，该方法会查询并缓存字典值。在容器被销毁时，会调用 `destroy()` 方法进行销毁，该方法会清空缓存。

## 3.对象回调接口

从 2.5.0 开始，Crane4j 提供了两个操作感知接口，当你让**被填充的对象**实现上述接口后，该对象中实现的回调方法会在特定的处理阶段被触发：

- `OperationAwareBean`：在执行装配操作前和完成装配操作后，允许你自定义一些逻辑；
- `SmartOperationAwareBean`：前者的增强版本，能够获取到更多的参数；

结合一个简单的例子，你可以感受一下上述回调接口的使用场景：

~~~java
@Data
private static class Bean implements OperationAwareBean {

    @Assemble(container = "user", props = @Mapping("name"))
    private Integer id;
    private String name;
    @Assemble(container = "entry", props = @Mapping("value"))
    private Integer key;
    private String value;
    @Disassemble(type = NestedBean.class)
    private NestedBean nestedBean;
    private String remark;

    @Override
    public boolean supportOperation(String key) {
        // 若 nestedBean 为空，则不进行针对 nestedBean 属性的递归填充
        return !Objects.equals("nestedBean", key) 
            || Objects.nonNull(this.nestedBean)
    }

    @Override
    public void beforeAssembleOperation() {
        // 在填充开始前，若 id 为空，则为其设置一个默认值
        if (Objects.isNull(this.id)) {
            this.id = 0; 
        }
    }

    @Override
    public void afterOperationsCompletion() {
        // 在完成填充后，对属性值做一些处理
        this.name = "尊敬的：" + this.name;
    }
}
~~~

通过该回调接口，你可以更灵活的添加一些自定义的逻辑。