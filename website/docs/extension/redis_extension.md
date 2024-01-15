## Redis 插件

在 2.4.0 及更高版本，你可以使用引入 `crane4j-redis-extension` 依赖来使用基于 Redis 的缓存管理器

## 1.安装

在开始前，请先确保已经引入必要的 crane4j 配置，然后在此基础上，额外的引入下述依赖：

~~~xml
<!-- 引入 crane4j-extension-mybatis-plus -->
<dependency>
    <groupId>cn.crane4j</groupId>
    <artifactId>crane4j-extension-redis</artifactId>
    <version>${last-version}</version>
</dependency>

<!-- 引入 spring-data-redis 依赖，若已有则可以跳过 -->
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-redis</artifactId>
    <version>${last-version}</version>
</dependency>
~~~

## 2.启用缓存管理器

该扩展提供的 Redis 缓存管理器 `RedisCacheManager` 需要依赖 `RedisTemplate` 使用。因此，在引入依赖后，你需要在自己的配置类中基于 `RedisTemplate` 手动创建并注册 `RedisCacheManager`。

如果你的项目中有 `StringRedisTemplate`，或者任意以 `Spring` 作为 key 的 `RedisTemplate` （`RedisTemplate<String, ?>`），那么你可以基于它创建一个 `StringKeyRedisCacheManager`：

~~~java
@Configuratiopn
public class Crane4jConfig {
    
    @Bean
    public StringKeyRedisCacheManager<String> cacheManager(StringRedisTemplate template) {
        StringKeyRedisCacheManager cacheManger = new StringKeyRedisCacheManager<>(template);
        cacheManger.setGlobalPrefix("cranej4:global:key")
        return cacheManger;
    }
} 
~~~

如果你的 `RedisTemplate` 对应的 key 类型不为 `String`，那么你也可以直接使用 `GeneralRedisCacheManager` 来适应不同的情况：

~~~java
@Configuratiopn
public class Crane4jConfig {
    
    @Bean
    public GeneralRedisCacheManager<Object, Object> cacheManager(
        RedisTemplate<Object, Object> template) {
        return new GeneralRedisCacheManager<>(
            template, 
            (cacheName, key) -> // 获取缓存 key, 
            value -> // 获取缓存值
        );
    }
} 
~~~

关于如何使用缓存管理器，请参见：[缓存](./../advanced/cache.md)。

## 3.键前缀

当你使用 `StringKeyRedisCacheManager` 作为缓存管理器时，容器在读写数据时，都会默认拼接上自己的命名空间作为 key 的前缀。

比如：

~~~java
@ContainerCache(
    expirationTime = 1000L, timeUnit = TimeUnit.MILLISECONDS,
	cacheManager = "stringKeyRedisCacheManager", // 你声明的管理器的 beanName
)
public class CustomContainer implements Container<String> {
    @Getter
    private String namespace = "test";
    @Override
    public Map<String, ?> get(Collection<String> keys) {
        // do something
	}
}
~~~

当你访问 `CustomContainer` 容器时，`CustomContainer` 容器会通过从缓存管理器获取的缓存对象存取数据，此时将会以 “`namespace:key`” 的格式访问 Redis：

~~~java
Container<String> customContainer = containerManager.getContainer("test");
customContainer.get(Arrays.asList("1", "2", "3"));
// redisTemplate.opsForValue().mget(Arrays.asList("test:1", "test:2", "test:3"))
~~~

如果你觉得区分度不够高，那么可以再对 `StringKeyRedisCacheManager` 本身也设置一个全局前缀：

~~~java
CacheManager cacheManager = crane4jGlobalConfiguration.getCacheManager("stringKeyRedisCacheManager");
cacheManager.setGlobalPrefix("crane4j:cache"); // 设置全局前缀

Container<String> customContainer = crane4jGlobalConfiguration.getContainer("test");
customContainer.get(Arrays.asList("1", "2", "3"));
// redisTemplate.opsForValue().mget(Arrays.asList(
//     "crane4j:cache:test:1", "crane4j:cache:test:2", "crane4j:cachetest:3"
// ))
~~~

:::warning

出于性能考虑，所有的操作都会通过 `RedisTemplate` 提供的批量接口（比如 `mget` ）完成，一些无法直接通过批量 API 完成的操作则会通过管道完成。因此，在某些情况下，你可能需要考虑事务对管道操作的一些影响。

:::

## 4.清空缓存

当你调用 `CacheObject.clear` 时，将会清除某个命名空间中的缓存，而调用 `CacheManager.removeCache`、`CacheManager.clear` 时将会清除缓存管理器中的所有缓存。

在默认情况下，`StringKeyRedisCacheManager` 并不会实际上的清除 Redis 中的缓存，不过你可以通过 `enableClearCache` 开启这个功能，在这种情况下，调用上述方法，将会删除所有以特定前缀开头的 key。比如：

~~~java
CacheManager cacheManager = crane4jGlobalConfiguration.getCacheManager("stringKeyRedisCacheManager");
cacheManager.setGlobalPrefix("crane4j:cache"); // 设置全局前缀
cacheManager.setEnableClearCache(true); // 允许从 Redis 删除数据
CacheObject<String> cache = cacheManager.removeCache("test"); // 删除数据
// redisTemplate.remove(redisTemplate.keys("crane4j:cache:test:*"))
~~~

如果你确实需要这个功能（尤其是在没有设置缓存过期时间时），那么你最好确保每一个缓存对象都对应一个独一无二的前缀，避免误删。

