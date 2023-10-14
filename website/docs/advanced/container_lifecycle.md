# 容器的生命周期

为了便于对在容器进行初始化和销毁阶段进行一些特殊的处理，crane4j 为容器设计了一套类似 Spring 的生命周期回调机制。

它包含容器生命周期处理器 `ContainerLifecycleProcessor` 与生命周期接口 `Container.Lifecycle` 两种，前者用于针对全局的容器进行处理器，而后者则用于处理特定类型的容器。

## 1.生命周期处理器

生命周期处理器 `ContainerLifecycleProcessor` 是用于感知并干涉容器生命周期的特色组件，它提个三个抽象方法：

- `whenRegistered`：容器注册前回调，通过该方法获得要注册的容器示例或工厂方法；
- `whenCreated`：容器创建后，通过该方法可以获取创建后即将加入缓存的容器实例；
- `whenDestroyed`：容器销毁后回调，通过该方法可以获得被替换或者删除的容器；

`crane4j` 基于这套机制已经提供了三种实现：

- `CacheableContainerProcessor`：在用户的容器注册前，将会把要注册的容器对象包装并替换具备缓存功能的增强容器；
- `ContainerRegisterLogger`：在容器生命周期的三个关键节点输出相关信息；
- `ContainerInstanceLifecycleProcessor`：用于支持 `Container.Lifecycle` 的 `init` 和 `destroy` 方法。

当你创建了一个处理器时，你可以通过全局配置 `Crane4jGlobalConfiguration` 的 `registerContainerLifecycleProcessor` 方法注册它。

而如果在 Spring 环境，你只需将其交给 Spring 管理即可，在启动后 crane4j 会自动注册。

## 2.Lifecycle回调接口

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