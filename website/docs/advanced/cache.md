# 缓存

在 `crane4j` 中，缓存功能由缓存管理器 `CacheManager` 和具体的缓存对象 `Cache` 共同完成。

缓存管理器 `CacheManager` 负责管理缓存对象 `CacheObject` 的创建和销毁，而缓存对象 `CacheObject` 提供对缓存数据的具体的增删改查操作。

![缓存结构](https://img.xiajibagao.top/image-20230225011748030.png)

**对于所有的数据源容器来说，缓存的粒度都是 key 级别，即第一次查询 a、b，则会对 a、b 进行查询并缓存。第二次查询 a、b、c 时，只会查询 c 并将其增量添加到缓存中，而 a、b 则直接从缓存中获取。**

## 1.使用

### 1.1.通过注解配置

你可以在类或者方法上添加 `@ContainerCache` 注解，以便快速的为数据源容器配置缓存。

比如，你可以直接**在实现了 `Container` 接口的类上添加注解**，表示这个数据源容器需要应用缓存：

~~~java
@ContainerCache(
    expirationTime = 1000L, // 配置过期时间
    timeUnit = TimeUnit.SECONDS, // 指定过期时间单位
)
private static class TestContainer implements Container<Object> {
    @Getter
    private final String namespace = "test";
    @Override
    public Map<Object, ?> get(Collection<Object> keys) {
        return null;
    }
}
~~~

此外，你也可以**在带有 `@ContainerMethod` 注解的方法上添加注解**，表示这个方法容器也需要应用缓存：

~~~java
@ContainerCache(
    expirationTime = 1000L, // 配置过期时间
    timeUnit = TimeUnit.SECONDS, // 指定过期时间单位
)
@ContainerMethod(namespace = "annotatedMethod", resultType = Foo.class)
public List<Foo> annotatedMethod(List<String> args) {
    return args.stream().map(key -> new Foo(key, key)).collect(Collectors.toList());
}
~~~

:::tip

- 关于 `@ContainerMethod` 的使用方法，请参见 [方法数据源容器](./../basic/container/method_container.md) 一节。

:::

此外，在 2.6.0 及以上版本，当你在 Spring 环境使用时，你也可以直接将注解加在配置类里的工厂方法上，它同样能够生效：

~~~java
@Configuration
public class Configuration {
    
    @ContainerCache(
        expirationTime = 1000L,
        timeUnit = TimeUnit.SECONDS,
    )
    @Bean
    public Container<String> customContainer() {
        // create custom container
    }
}
~~~

### 1.2.使用配置文件配置

除注解外，你也可以通过配置文件配置要对哪些容器应用缓存：

~~~yml
crane4j:
  caches:
  	  # 容器的命名空间
    - namespace: test1
      # 过期时间
      expire-time: 60
      # 时间单位
      time-unit: SECONDS
      # 要使用的缓存管理器
	  container-manager: GuavaCacheManager
    - namespace: test2
      expire-time: 600
      time-unit: MILLISECONDS
~~~

其中，`namespace` 即为要应用缓存的数据源容器的 `namespace`。

### 3.3.手动配置

除通过上述方式自动配置缓存外，你也可以通过获取 `CacheManager` 手动的创建具备缓存功能的数据源容器，比如：

~~~java
// 准备一个数据源容器
Container<String> container = Containers.forMap(Collections.singletonMap("test", 1));

// 从全局配置中获取 CacheManager
CacheManager cacheManager = configuration.getCacheManager("MapCacheManager");
// 定义缓存配置
CacheDefinition def = new CacheDefinition.Impl(
    container.getNamespace(), "MapCacheManager", 
    100L, TimeUnit.MILLISECONDS
);
// 使用 CacheableContainer 进行包装
CacheableContainer<String> cacheableContainer = new CacheableContainer<>(container, def, cacheManager);
~~~

当执行时，`CacheableContainer` 将会自动从 `CacheManager` 中获取缓存。

## 2.缓存管理器

`crane4j` 默认提供了两种类型的缓存管理器：

- **本地缓存**：本地缓存管理器 `MapCacheManager` 是基于 `Map` 集合实现的本地缓存管理器，默认使用 `WeakConcurrentMap` 实现，不能设置超时时间，当 JVM 触发 GC 时回收。
- **Guava 缓存**：Guava 缓存管理器 `GuavaCacheManager` 是基于 `Guava` 的 `Cache` 实现的缓存对象，它支持配置过期时间和并发等级等各种功能；

此外，你也可以引入 crane4j 的 Redis 扩展插件，它允许你使用基于 Redis 的缓存管理器，具体可以参见：[Redis 扩展](./../extension/redis_extension.md)。

### 2.1.指定缓存管理器

在使用 `@ContainerCache` 注解时，你可以通过 `cacheManager` 属性指定要使用的管理器：

```java
@ContainerCache(cacheManager = "GuavaCacheManager") // 指定缓存管理器
@ContainerMethod(namespace = "annotatedMethod", resultType = Foo.class)
public List<Foo> annotatedMethod(List<String> args) {
    return args.stream().map(key -> new Foo(key, key)).collect(Collectors.toList());
}
```

此外，你也可以注册自己的缓存管理器：

- 在 Spring 环境，你仅需要将其交给 Spring 托管即可，项目启动后 Crane4j 将会自动获取并注册它，此后你可以通过它的 beanName 获取它。
- 在非 Spring 环境，你需要在创建/获取 `SimpleCrane4jGlobalConfiguration` 后，获取 `cacheManagerMap` 属性并注册你的管理器。

### 2.2.刷新缓存

一般情况下，缓存会根据你设置的过期时间自动过期，不过在某些时候，你可能需要手动的刷新缓存。此时，你可以选择直接**通过缓存管理器移除这个缓存**：

~~~java
// 创建一个缓存 test
CacheManager cacheManager = new GuavaCacheManager();
CacheObject<String> cache = cacheManager.createCache("test", -1L, TimeUnit.MILLISECONDS);

// 移除缓存 test
cacheManager.removeCache("test");
// 此时 test 缓存将会被清空，并且标记为过期
cache.isInvalid(); // = true
~~~

当缓存被管理器移除后，它将会被标记为“失效”。当数据源容器尝试操作它时，若发现它已经失效，则会从管理器重新创建并获取一个缓存对象。

当然，除直接移除缓存外，你也可以通过**管理器获取具体的缓存对象，然后根据你的需要移除某些键值**：

~~~java
// 创建一个缓存 test
CacheManager cacheManager = new GuavaCacheManager();
CacheObject<String> cache = cacheManager.createCache("test", -1L, TimeUnit.MILLISECONDS);

// 获取缓存，此时与上文创建的缓存为同一个对象
CacheObject<String> cache = cacheManager.getCache("test");
cache.remove("something");
cache.clear();
~~~

**在默认情况下，你总是可以通过容器的 `namespace` 获取它所持有的缓存对象。**

### 2.3.自定义缓存

要接入自定义的缓存，你需要实现 `CacheManager` 接口，提供一个自定义的缓存管理器。

基于已有的 `AbstractCacheManager` 模板类可以大大减少工作量，具体来说，假如希望我们想要实现一个基于 HashMap 的自定义缓存，那么需要两步：

- 定义一个缓存实现类 `CustomCacheObject`，让它继承 `AbstractCacheManager.AbstractCacheObject`，并实现各种增删改查方法；
- 定义一个缓存管理器实现类 `CustomCacheManager`，然后让它继承 `AbstractCacheManager`，并且实现 `doCreateCache` 方法，让它返回一个 `CustomCacheObject` 实例；

具体代码如下：

~~~java
public class CustomCacheManager extends AbstractCacheManager {
    
    @Override
    @NonNull
    protected <K> MapCacheObject<K> doCreateCache(
        String name, Long expireTime, TimeUnit timeUnit) {
        // 实现抽象方法，返回一个自定义缓存对象
        return new MapCacheObject<>(name, new HashMap<>());
    }
    
    // 基于 AbstractCacheObject 实现自己的自定义缓存对象
    protected static class CustomCacheObject<K> extends AbstractCacheObject<K> {
        private final Map<K, Object> map;
        public CustomCacheObject(String name, Map<K, Object> map) {
            super(name);
            this.map = map;
        }
        // 实现自己的增删改查方法
        @Nullable
        @Override
        public Object get(K key) {
            return map.get(key);
        }
        @Override
        public void put(K key, Object value) {
            map.put(key, value);
        }
        @Override
        public void putIfAbsent(K key, Object value) {
            map.putIfAbsent(key, value);
        }
        @Override
        public void remove(K key) {
            map.remove(key);
        }
        @Override
        public void clear() {
            map.clear();
        }
    }
}
~~~

