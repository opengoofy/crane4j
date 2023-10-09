# 枚举容器

枚举容器指以枚举作为数据源的数据源容器，在 2.2 及以上版本，它支持通过 `@AssembleEnum` 注解，以选项式风格配置使用。

## 1.创建容器

我们可以使用 `Containers.forEnum` 方法基于枚举类快速配置一个枚举数据源：

```java
@Getter
@RequiredArgsConstructor
private enum Num {
    ONE(1, "one"),
    TWO(2, "two");
    private final int code;
    private final String value;
}

// 使用 Containers.forEnum 方法构建容器
// 容器缓存的数据为： {1 = ONE}, {2 = TWO}
Container<String> container = Containers.forEnum("num", Num.class, Enum::getCode); // 指定 key 值为 code
```

使用该容器后，可以通过键值（code）从容器中获取对应的枚举实例。

或者，也可以通过建造者构建一个枚举容器：

~~~java
Container<Object> container = EnumContainerBuilder.of(AnnotatedEnum.class)
    .namespace("test") // 指定命名空间
    .key("value") // 指定提供key值的属性
    .value("key") // 指定提供value值的属性
    .build();
~~~

相比起 `Containers` 的静态工厂方法，建造者具备更多的可配置项。

## 2.可配置项

除了普通枚举外，我们还可以在枚举类上添加 `@ContainerEnum` 注解来进一步定义容器的具体信息：

| API                 | 作用                          | 类型                     | 默认值                                            |
| ------------------- | ----------------------------- | ------------------------ | ------------------------------------------------- |
| `namespace`         | 定义枚举容器的命名空间        | 任意字符串               | 枚举类的 `Class#SimpleName`                       |
| `key`               | 数据源的 key 值               | 枚举类的属性值           | 枚举对象的 `Enum#name`                            |
| `value`             | 数据源的 value 值             | 枚举类的属性值           | 枚举对象本身                                      |
| `duplicateStrategy` | 当 key 出现重复值时的处理策略 | `DuplicateStrategy` 枚举 | `DuplicateStrategy.ALERT`，出现重复值时直接抛异常 |

举个例子：

```java
@ContainerEnum(namespace = "AnnotatedEnum", key = "key", value = "value")
@Getter
@RequiredArgsConstructor
private enum Num {
    ONE(1, "one"),
    TWO(2, "two");
    private final int key;
    private final String value;
}

// 使用 Containers.forEnum 方法构建容器
// 容器缓存的数据为： {1 = "one"}, {2 = "two"}
Container<String> container = Containers.forEnum(Num.class, new SimpleAnnotationFinder());
```

:::tip

在 springboot 中，我们也可以在配置文件中直接扫描路径下的所有枚举类，然后将其注册为容器。

:::

## 3.批量扫描

在 spring 环境中，你可以通过**配置文件**或者 **`@EnableCrane4j` 注解**配置扫描路径，批量的扫描并注册枚举。

**通过配置文件**

在 2.0 及以上版本，你可以通过在配置文件中配置枚举包路径，以便批量的将扫描到的枚举类注册为枚举容器：

~~~yml
crane4j:
 # 扫描枚举包路径
 container-enum-packages: cn.demo.constant.enums
 # 是否只加载被@ContainerEnum注解的枚举
 only-load-annotated-enum: true
~~~

**通过注解配置**

在 2.1.0 及以上版本，你可以在 spring 的启动类或配置类上，通过注解配置要扫描的枚举包路径：

~~~java
@EnableCrane4j(
    enumPackages = "com.example.demo" // 要扫描的枚举包路径
)
@SpringBootApplication
public class Demo3Application implements ApplicationRunner {
    public static void main(String[] args) {
        SpringApplication.run(Demo3Application.class, args);
    }
}
~~~

你也直接可以使用 `@ContainerEnumScan` 注解进行更细粒度的配置，效果与 `@EnableCrane4j` 一致：

~~~java
@ContainerEnumScan(
    includePackages = "com.example.demo",  // 要扫描的包路径
    excludeClasses = { NoScanEnum.class }, // 排除指定的类
    includeClasses = { NeedScanEnum.class } // 包含指定的类
)
@SpringBootApplication
public class Demo3Application implements ApplicationRunner {
    public static void main(String[] args) {
        SpringApplication.run(Demo3Application.class, args);
    }
}
~~~

## 4.选项式配置

在 2.1 及以上版本，你可以使用 `@AssembleEnum` 注解进行选项式风格的配置。

比如：

```java
@RequiredArgsConstructor
@Data
private static class Foo {
    @AssembleEnum(
        type = Gender.class, enumKey = "code", 
        props = @Mapping(ref = "cnName")
    )
    private final Integer id;
    private String cnName;
    private Gender gender;
}

@Getter
@RequiredArgsConstructor
private enum Gender {
    FEMALE(0, "女", "female"), MALE(1, "男", "male");
    private final Integer code;
    private final String cnName;
    private final String enName;
}
```

`@AssembleEnum` 注解提供了一些与 `@ContainerEnum` 相同的可选项：

| API                | 作用                                     | 类型                                                         | 默认值                       |
| ------------------ | ---------------------------------------- | ------------------------------------------------------------ | ---------------------------- |
| `type`             | 指定枚举类型                             | 枚举类                                                       | 无，与 `typeName` 二选一必填 |
| `typeName`         | 指定枚举类型                             | 枚举类的全限定名<br />一般在枚举类与实体类不在同一包中时使用 | 无，与 `type` 二选一必填     |
| `enumKey`          | 数据源的 key 值                          | 枚举类的属性值，同 `@ContainerEnum#key`                      | 枚举对象的 `Enum#name`       |
| `enumValue`        | 数据源的 value 值                        | 枚举类的属性值，同 `@ContainerEnum#value`                    | 枚举对象本身                 |
| `useContainerEnum` | 是否遵循枚举类上的 `@ContainerEnum` 注解 | 为 true 时，若枚举类上存在 `@ContainerEnum` 注解，则优先使该注解的配置 | true                         |
| `ref`              | 指定填充字段                             | 等效于 `props = @Mapping(ref = "xxx")`                       | 无                           |