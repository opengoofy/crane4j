# 方法容器

方法容器指以实例方法或静态方法作为数据源的容器。它是我们在日常中最经常使用的容器之一。

和其他的容器不同，方法容器通常不直接创建使用，而是通过在目标方法上添加注解的方式，将该方法 “声明” 为一个方法容器，它通常由 DI 容器自动扫描并注册——换而言之，**这个功能比较推荐在 Spring 环境中使用**。

crane4j 在设计上参考了 Spring 处理监听器注解 `@EventListener` 的责任链机制，它基于注解处理器 `MethodContainerAnnotationProcessor` 和方法容器工厂链 `MethodContainerFactory` 实现了扫描和适配的功能，你可以通过添加自己的 `MethodContainerFactory` 实现从而扩展这部分功能。

## 1.声明容器

你可以直接在类或方法上添加 `@ContainerMethod` 注解，在 Spring 环境中，当项目启动后，会在后处理阶段扫描该方法，并将其注册为一个方法容器。

**声明在方法上**

```java
@ContainerMethod(
    namespace = "onoToOneMethod",
    resultType = Foo.class, resultKey = "id" // 返回的数据源对象类型为 Foo，并且需要按 id 分组
)
public Set<Foo> onoToOneMethod(List<String> args) {
    // do something
}
```

**声明在类上**

当你在类上声明时，你需要使用 `bindMethod` 和 `bindMethodParamTypes` 属性显式的进行方法绑定：

```java
// 父类
public class SuperClass {
    public Set<Foo> onoToOneMethod(List<String> args) {
        // do something
    }
}

// 子类
@ContainerMethod(
    namespace = "onoToOneMethod",
    resultType = Foo.class, resultKey = "id", // 返回的数据源对象类型为 Foo，并且需要按 id 分组
    bindMethod = "onoToOneMethod" // 指定要绑定的方法名称
    bindMethodParamTypes = List.class // 指定要绑定方法的参数类型
)
public class ChildClass extends SuperClass {}
```

此时，你可以在子类中绑定父类方法作为方法容器。

可以作为方法容器的方法需要**满足下述条件**：

+ **声明类**：不限制，你可以将注解声明在接口或抽象类上，如果声明在类父类或者父接口上，那么子类/实现类同样会获得此方法；
+ **方法类型**：不限制，方法可以是实例方法（包括接口或抽象类中的抽象方法）或静态方法；
+ **返回值类型**：方法必须有返回值，且返回值类型必须为 `Collection` 集合或 `Map` 集合（取决于 `@ContainerMethod#type` 属性）；
+ **参数类型**：可以是无参方法，若是有参方法，则首个参数必须为 `Collection` 类型；

常见的各种 `xxxByIds` 都是非常典型的方法。

## 2.可选配置项

在 `@ContainerMethod` 注解中，提供了一些可选的配置项：

| API                    | 作用                                  | 类型                                             | 默认值                                                |
| ---------------------- | ------------------------------------- | ------------------------------------------------ | ----------------------------------------------------- |
| `namespace`            | 定义枚举容器的命名空间                | 任意字符串                                       | `Method#getName`                                      |
| `type`                 | 映射类型，表示如何对结果集按 key 分组 | `MappingType` 枚举                               | `MappingType.ONE_TO_ONE`，即结果总是与 key 一对一分组 |
| `duplicateStrategy`    | 当 key 出现重复值时的处理策略         | `DuplicateStrategy` 枚举                         | `DuplicateStrategy.ALERT`，出现重复值时直接抛异常     |
| `resultKey`            | 分组的 key 值                         | 方法返回的对象列表的 key                         | `"id"`                                                |
| `resultType`           | 返回值类型                            | 返回值参数类型（如果是集合，则为其中的元素类型） | 无，必填                                              |
| `bindMethod`           | 绑定方法的名称                        | 方法名                                           | 当注解声明在类上时必填，声明在方法上时不填            |
| `bindMethodParamTypes` | 绑定方法的参数类型                    | 方法参数类型                                     | 无，不填时默认获取首个符合条件的同名方法              |

## 3.对结果分组

这里需要强调一下 `@ContainerMethod#type` 属性，它用于指定如何对结果集按 key 分组，它通常与 `resultKey` 与 `resultType` 结合使用。

| 类型                      | 说明                                      | 分组结果                | 场景                                                         |
| ------------------------- | ----------------------------------------- | ----------------------- | ------------------------------------------------------------ |
| `ONE_TO_ONE`              | 按 key 值一对一分组                       | `Map<key, value>`       | 默认                                                         |
| `MappingType.ONE_TO_MANY` | 按 key 值一对多分组                       | `Map<key, List<value>>` | 一个 key 对应多个值<br />比如一个 `classId` 对应多个 `Student` |
| `MappingType.MAPPED`      | 返回值已经是分组后的 `Map` 集合，无需分组 | 原始的方法返回值        | 当返回值已经是 `Map` 时                                      |

## 4.结果缓存

在 2.0 及以上版本，你可以在方法上添加 `@ContainerCache` 注解，使其具备缓存功能：

~~~java
@ContainerCache
@ContainerMethod(
    namespace = "onoToOneMethod",
    resultType = Foo.class, resultKey = "id" // 返回的数据源对象类型为 Foo，并且需要按 id 分组
)
public Set<Foo> onoToOneMethod(List<String> args) {
    // do something
}
~~~

缓存的失效时间取决于你在 `CacheManager` 中设置的时间，目前它是全局的，无法在每个方法上单独设置。

具体内容，可参见后文 “[缓存](./../advanced/cache.md)” 一节。

## 5.手动注册

手动注册一般只在你的目标类未被 Spring 管理，或者干脆项目没有使用 Spring 的时候会使用。

在 Spring 环境中，针对方法容器的扫描和注册是自动完成的。不过你也可以手动完成这个过程：

~~~java
// 从 Spring 容器中获取处理器和全局配置
@Autowried
private MethodContainerAnnotationProcessor processor;
@Autowried
private Crane4jGlobalConfiguration configuration;

// 基于 Foo 的实例方法创建方法容器
Foo foo = new Foo();
Collection<Container<Object>> containers = processor.process(foo, Foo.getClass());
containers.forEach(configuration::registerContainer);
~~~

如果你是在非 Spring 环境中，那么你需要先通过以下代码手动构建 `MethodContainerAnnotationProcessor` 实例：

~~~java
// 构建方法容器处理器
Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
MethodContainerAnnotationProcessor processor = ConfigurationUtil.createContainerMethodAnnotationProcessor(configuration);
~~~
