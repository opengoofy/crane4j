# 容器提供者

容器提供者 `ContainerProvider` 是用于获取数据源容器的组件，类似于 Spring 中的 `FactoryBean`，全局配置类 `Crane4jGlobalConfiguration` 本身就是一个 `ContainerProvider`。

它被设计用于接入第基于三方框架实现的容器，比如 `MybatisPlusQueryContainerProvider`，我们可以通过它获取基于 `BaseMapper#selectList` 方法构建的特殊方法容器，当调用时等同于调用`BaseMapper#selectList` 方法。

![image-20230311184930927](https://img.xiajibagao.top/image-20230311184930927.png)

## 1.创建并注册

crane4j 默认提供了 `PartitionContainerProvider` 作为常用实现类，它可以满足绝大部分的需求，或者你也可以实现 `ContainerProvider` 接口，自己定义一个提供者。

当你创建了一个实例后，若你在非 Spring 环境中，你需要将其手动注册到全局配置中：

~~~java
SimpleCrane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
configuration.registerContainerProviderput("fooContainerProvider", xxxContainerProvider);
~~~

如果你是在 Spring 环境，那么你直接将其交给 Spring 管理即可，在项目启动后它会自动注册。

## 2.在配置中引用

你可以在 `@Assemble` 注解的 `containerProvider` 属性中，指定你需要的容器要从哪个提供者获取。比如：

~~~java
public class UserVO {
    @Assemble(
        container = "user", containerProvider = "fooContainerProvider",
        props = @Mapping(src = "name", ref = "name")
    )
    private Integer id;
    private String name;
}
~~~

当配置解析时，`crane4j` 将从用户指定的 `fooContainerProvider` 获取 `namespace` 为 `user` 的数据源容器。

当然，你也可以像 Spring 从 `FactoryBean` 获取 `bean` 那样，通过 `$$` 连接符拼接两者，然后将其作为 `namesapce`，比如上述配置可以改写为：

~~~java
public class UserVO {
    @Assemble(
        container = "fooContainerProvider&&user",
        props = @Mapping(src = "name", ref = "name")
    )
    private Integer id;
    private String name;
}
~~~

两种方式效果一致，不过通常还是更推荐使用第一种方式，它会更直观一些。