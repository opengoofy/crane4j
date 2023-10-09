# 声明一个装配操作

在 crane4j 中，装配操作指一个 “根据 A 的 key 值拿到 B，再把 B 的属性映射到 A” 的操作，在代码上，它对应一个在类或类属性上声明的 `@Assemble` 注解。

一个装配操作包含三个必要的属性：

+ key 字段：被注解的属性，或 `@Assemble#key` 属性指向的属性；
+ 数据源容器：即 `@Assemble#container` 属性指向的数据源容器；
+ 字段映射：即通过 `@Assemble#props` 属性对应的字段映射配置；

具体内容可以参考 “[基本概念](./../user_guide/basic_concept.md)” 一节。

## 1.声明装配操作

所有的 `@Assemble` 注解都支持在类或类的属性上使用（其实也支持在方法使用，后续会介绍）。

### 在属性上声明

你可以使用 `@Assemble` 在某个 key 字段上声明一个装配操作：

```java
public class Student {
    @Assemble(container = "student", props = @Mapping(src = "studentName", ref = "name"))
    private Integer id;
    private String name;
}
```

上述配置表示：

1. 使用 `id` 字段的值；
2. 从命名空间为 `student` 的数据源容器中获取数据源对象；
3. 获取与 `id` 字段值对应的数据，将数据源对象中的 `studentName` 字段值映射到 `name` 字段上；

### 在类上声明

不过，你也可以在类上声明，此时你需要显式的指定该操作要绑定的 key 字段。比如：

```java
// 直接声明
@Assemble(
    key = "id", // 手动指定绑定到 id 字段上
    container = "student", 
    props = @Mapping(src = "studentName", ref = "name")
)
public class Student {
    private Integer id;
    private String name;
}
```

在无法直接修改父类的情况下，可以在类上添加注解来声明装配操作。

此外，在父类或父类属性中声明的操作，也会被子类所继承。

:::tip

`key` 属性默认支持使用 `xx.xx.xx` 格式的链式操作符访问嵌套对象的属性；

:::

### 重复声明

你可以在同一个键上声明多次装配操作：

```java
public class UserVO {
    @Assemble(container = "user_role", props = @Mapping(src = "role", ref = "role"))
    @Assemble(container = "user", props = @Mapping(src = "name", ref = "name"))
    private Integer id;
    private String name;
    private String role;
}
```

无需担心多次查询数据源的问题，一般情况下，crane4j 会自动帮你合并在同一次操作中对相同数据源容器的调用。

## 2.显式指定key类型

在一些情况下，你的数据源容器接受的 key 类型，可能与你实际的 key 类型并不相同。

在 2.2 及以上版本，你可以在注解中通过 `keyType`  强制将 key 的类型转为指定类型，从而保证它与数据源接受的参数类型一致。比如：

~~~java
// 容器对应的key类型为Integer集合
Containers.<Integer>forLambda("foo", ids -> ids.stream().collector(Collectors.toMap(Function::identify, Function::identify)));

// 带填充对象的key类型为String
public class Foo {
    @Assemble(
        container = "foo", props = @Mapping(src = "a", ref = "b"),
        keyType = Integer.class // 指定 key 类型强制转为 Integer
    )
    private String id;
    private String b;
}
~~~

在上述代码中，我们的数据源容器 `foo` 仅接受 `Integer` 类型的参数，而 `foo.id` 的类型却是 `String`，因此通过 `keyType` 指定其需要在执行时将 key 转为 `Integer`。

:::tip

从 2.2 版本开始，`cranej4` 提供了用于在序列化过程中进行填充的 `jackson` 插件，`jackson` 插件默认从 JsonNode 中拿到的所有 key 值都为 `String` 类型，因此当使用此插件时，若你的数据源容器接受的参数类型不是 `String`，则必须手动显式的在注解配置中指定 key 的类型。

:::

## 3.属性映射策略

在默认情况下，当我们指定要将数据源对象的 a 属性映射到目标对象的 b 属性时，仅当 a 的属性值不为 `null` 才会对 b 属性进行赋值。在 `2.1.0` 及以上版本， 该行为可以通过在指定属性映射策略改变。

### 指定策略

比如，如果我们希望仅当目标对象的 b 属性值为 `null` 时，才允许将数据源对象的 a 属性值映射过来：

~~~java
public class Foo {
    @Assemble(
        container = "foo", props = @Mapping(src = "a", ref = "b"),
        propertyMappingStrategy = "ReferenceMappingStrategy" // 指定属性映射策略
    )
    private String id;
    private String b;
}
~~~

默认提供了三种策略：

+ `OverwriteMappingStrategy`：覆写策略，不论 `src` 对应的属性值是不是 `null` ，都强制覆写 `ref` 对应的目标属性；

+ `OverwriteNotNullMappingStrategy`：非空时覆写策略，仅当 `src` 对应的属性值不为 `null` 时，强制覆写 `ref` 对应的目标属性。

	当不指定策略时，将默认使用它作为实际的映射策略；

+ `ReferenceMappingStrategy`：空值引用策略，仅当 `ref` 对应的目标属性为 `null` 时，才获取 `src` 的属性值；

### 自定义策略

你可以通过实现 `PropertyMappingStrategy` 接口创建自定义策略。

在 spring 环境中，你只需要将自定义策略交由 spring 容器管理即可，项目启动后 cranej4 会自动注册。

而在非 spring 环境中，你可以直接将其注册到 `SimpleCrane4jGlobalConfiguration` 中：

~~~java
SimpleCrane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
PropertyMappingStrategy customStrategy = CustomPropertyMappingStrategy();
configuration.addPropertyMappingStrategy(customStrategy);
~~~

## 4.表达式支持

在 spring 环境中，你可以在 `@Assemble` 注解的 `container` 属性里引用一些配置文件属性：

```java
public class Foo {
    @Assemble(container = "${custom.namespace}")
    private String name;
    private String alias;
}
```

或者使用 SpEL 表达式：：

```java
public class Foo {
    @Assemble(container = "${custom.namespace} + '$$defaultProvider'")
    private String name;
    private String alias;
}
```

用法与 spring 提供的 `@Value` 一致。