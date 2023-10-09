# 数据源容器

<img src="https://img.xiajibagao.top/image-20230210133633050.png" alt="container" style="zoom: 33%;" />

每个填充操作都需要对应一个**数据源**，我们通常会通过外键从数据源中得到对应的数据——可能是单个对象，也可能是对象集合——用于后续填充。在 cranej4 中，一个数据源对应一个**数据源容器** (`Container`)，而每个容器都具备全局唯一的**命名空间** (`namespace`)。

你可以基于任何类型的数据源创建容器，并且在将其注册到全局配置对象 `Crane4jGlobalConfiguration` 后，你就可以在 `@Assemble` 注解中通过命名空间引入了：

~~~java
// 基于 Map 集合创建容器，然后注册
Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
Container<Integer> mapContainer = Containers.forMap("map_container", new HashMap<Integer, Object>());
configuration.registerContainer(mapContainer);

// 在注解中通过命名空间引用容器
public class Student {
    @Assemble(container = "map_container", props = @Mapping(ref = "name"))
    private Integer id;
    private String name;
}
~~~

数据源容器是填充中最重要的一环，阅读后续内容，进一步了解该如何使用 crane4j 中的各种数据源容器。
