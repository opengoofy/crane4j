# 集合容器

集合缓存指以 `Map` 集合作为数据源的容器。它通常用于存放系统启动后加载的配置项或字典项。

通过 `Containers` 的 `forMap` 工厂方法，我们可以快速配置一个数据源容器：

```java
Map<String, ?> map = new HashMap<>();
map.put(key, value);
Container<String> container = Containers.forMap(map);
```

当输入一个键值作为 key 时，该容器将从 `map` 中获取对应的值，并将其作为数据源对象返回。

:::tip

`Containers` 是一个用于创建容器的静态工厂，你可以通过它去创建所有类型的容器实例

:::