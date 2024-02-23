# 对象容器

在默认情况下，`Container` 总是用于根据指定的 key 值查询对应的数据源，但是在有些情况下，我们可能需要同时**根据多个 key 值，或一些复杂的自定义条件**确认要从数据源中获取哪些数据，并如何对 key 对应。

为此，在配置填充操作时，你可以**不指定 key 值**，此时，crane4j 会直接将待填充的对象作为 key 值传入容器，由用户自行决定要如何返回数据源，此类容器称为“**对象容器**”。

比如，我们现有待填充对象 `Foo`，我们需要同时根据 `Foo` 的 `id` 和 `code` 去确认一个对应的数据。

因此，我们在类上添加 `@Assemble` 注解，但是不指定任何的 key 值：

~~~java
@Assemble(container = "foo_info", props = @Mapping(ref = "name")) // 直接以当前的 Foo 对象作为 key，去数据源容器中查询
@Data
public class Foo {
    private Integer id;
    private String code;
    private String name;
}
~~~

此时，我们声明一个容器，该容器**入参为待填充的 `Foo` 集合本身，并返回按 `Foo` 对象分组的数据集**：

~~~java
Container<Foo> objectContainer = Containers.forLambda("foo_info", fooList -> {
    fooList.stream().collect(Collectors.toMap(
    	foo -> foo, foo -> foo.getId() + "#" + foo.getCode();
    ))
});
~~~

:::warning

`Container` 返回的数据必须按**入参的对象实例本身**分组，如果重写了 `equals` 或者 `hashCode` 需要格外注意。

:::

:::tip

如果你的目的仅仅是为了对目标对象做一些处理，而不是真的需要以目标对象本身为数据源进行填充，那么你可以直接让目标类实现 `OperationAwareBean` 或 `SmartOperationAwareBean` 接口，在回调方法中实现你想要的效果。

具体可以参见 [组件的回调接口](./../../advanced/callback_of_component.md) 一节中的 “对象的回调接口” 这一小节。

:::