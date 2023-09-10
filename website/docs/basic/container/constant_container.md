# 常量容器

常量容器指基于常量类中的静态成员变量创建的数据源容器，它的作用有点类似于 Spring 的 `Contstant` 工具类。

## 1.创建容器

可以使用 `Containers.forConstantClass` 方法将常量类定义为一个数据源容器：

```java
@ContainerConstant
public static class FooConstant {
    public static final String ONE = "one";
    public static final String TWO = "two";
    public static final String THREE = "three";
}

// 使用 Containers.forConstantClass 方法构建容器
// 容器缓存的数据为： {"ONE" = "one"}, {"TWO" = "two"}, {"THREE" = "three"}
Container<String> container = Containers.forConstantClass(FooConstant.class, new SimpleAnnotationFinder());
```

或者，也可以通过建造者构建一个常量容器：

~~~java
Container<?> container = ConstantContainerBuilder.of(FooConstant.class)
    .namespace("test") // 指定容器命名空间
    .onlyPublic(false) // 扫描所有公有和非公有属性
    .reverse(true) // 翻转键值对，即使用属性名作为value，属性值作为key
    .build();
~~~

相比起 `Containers` 的静态工厂方法，建造者具备更多的可配置项。

## 2.可配置项

与常量容器相同，我们还可以在常量类上添加 `@ContainerConstant` 注解来进一步定义容器的具体信息：

```java
@ContainerConstant(
    namespace = "foo", // 指定命名空间
    onlyExplicitlyIncluded = true,  // 是否只保存带有 @Include 注解的属性
    onlyPublic = false // 是否只保存公共变量
)
public static class FooConstant2 {
    @ContainerConstant.Include      // onlyExplicitlyIncluded 为 true 时，仅包含带有该注解的属性
    public static final String ONE = "one";
    @ContainerConstant.Exclude      // 默认情况下排除该属性
    public static final String TWO = "two";
    @ContainerConstant.Name("THREE") // 指定 key 名称为 "THREE"
    private static final String SAN = "three";
}
```

这里提供了一些可选的配置：

| API                          | 作用                                                         | 类型       | 默认值                      |
| ---------------------------- | ------------------------------------------------------------ | ---------- | --------------------------- |
| `namespace`                  | 定义常量容器的命名空间                                       | 任意字符串 | 常量类的 `Class#SimpleName` |
| `onlyExplicitlyIncluded`     | 是否只引用带有 `@Include` 注解的属性                         | 布尔值     | false                       |
| `onlyPublic`                 | 是否只引用被 `public` 修饰的属性                             | 布尔值     | true                        |
| `reverse`                    | 是否需要翻转键值（属性名和属性值）                           | 布尔值     | false                       |
| `@ContainerConstant.Name`    | 注解在属性上，在将常量属性注册到容器后，使用注解指定的名称替代属性名 | 注解       |                             |
| `@ContainerConstant.Include` | 注解在属性上，与 `onlyExplicitlyIncluded` 属性配合使用，声明要保留的常量属性 | 注解       |                             |
| `@ContainerConstant.Exclude` | 注解在属性上，声明要排除的常量属性                           | 注解       |                             |

## 3.批量扫描

在 Spring 环境中，你可以通过**配置文件**或者 **`@EnableCrane4j` 注解**配置扫描路径，批量的扫描并注册常量类。

**通过配置文件**

在 2.0 及以上版本，你可以通过在配置文件中配置常量包路径，以便批量的将扫描到的常量类注册为常量容器：

~~~yml
crane4j:
 # 扫描常量包路径
 container-constant-packages: cn.demo.constant
~~~

**通过注解配置**

在 2.1.0 及以上版本，你可以在 Spring 的启动类或配置类上，通过注解配置要扫描的常量包路径：

~~~java
@EnableCrane4j(
    constantPackages = "com.example.demo", // 要扫描的常量包路径
)
@SpringBootApplication
public class Demo3Application implements ApplicationRunner {
    public static void main(String[] args) {
        SpringApplication.run(Demo3Application.class, args);
    }
}
~~~

你也直接可以使用 `@ContainerConstantScan` 注解进行更细粒度的配置，效果与 `@EnableCrane4j` 一致：

~~~java
@ContainerConstantScan(
    includePackages = "com.example.demo",  // 要扫描的包路径
    excludeClasses = { NoScanConstant.class }, // 排除指定的类
    includeClasses = { NeedScanConstant.class } // 包含指定的类
)
@SpringBootApplication
public class Demo3Application implements ApplicationRunner {
    public static void main(String[] args) {
        SpringApplication.run(Demo3Application.class, args);
    }
}
~~~

## 4.选项式配置

在 2.6 及以上版本，你可以使用 `@AssembleConstant` 注解进行选项式风格的配置。

比如：

```java
@RequiredArgsConstructor
@Data
private static class Foo {
    @AssembleConstant(
        type = Gender.class,
        constant = @ConstantContainer // 如果没有自定义配置可以不配置这个属性
    )
    private String code;
    private String value;
}

@Getter
@RequiredArgsConstructor
private enum Gender {
    public static final String FEMALE = "女";
    public static final String MALE = "男";
}
```

你可以通过 `constant` 设置一个 `@ContainerConstant`，其配置效果与在常量类似添加 `@ContainerConstant` 完全一样。

此外，如果你的常量类已经有 `@ContainerConstant` 注解了，那么你可以通过 `followTypeConfig` 选项决定是否要优先遵循常量类上的注解配置。

`@AssembleEnum` 注解共提供下述可选项：

| API                | 作用                                         | 类型                                                         | 默认值                       |
| ------------------ | -------------------------------------------- | ------------------------------------------------------------ | ---------------------------- |
| `type`             | 指定常量类型                                 | 常量类                                                       | 无，与 `typeName` 二选一必填 |
| `typeName`         | 指定常量类型                                 | 常量类的全限定名<br />一般在常量类与实体类不在同一包中时使用 | 无，与 `type` 二选一必填     |
| `constant`         | 常量容器配置                                 | `@ContainerConstant` 注解                                    | 空                           |
| `followTypeConfig` | 是否遵循常量类上的 `@ContainerConstant` 注解 | 为 true 时，若常量类上存在 `@ContainerConstant` 注解，则优先使该注解的配置 | true                         |
| `ref`              | 指定填充字段                                 | 等效于 `props = @Mapping(ref = "xxx")`                       | 无                           |
