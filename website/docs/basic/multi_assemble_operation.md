# 一对多&多对多

在一些场景中，一个 key 值会对应多个数据源对象，有时甚至会出现 key 本身就是一个数组或者集合的情况。此时，我们需要更换装配处理器 `AssembleOperateHandler` 来完成这种**一对多/多对多装配**。

**装配处理器** `AssembleOperateHandler` 在整个装配过程中负责实际的属性读写操作，类似于 Jackson 中的序列化器（`Serializer`）和反序列化器（`Deserializer`）。与 Jackson 类似，如果我们需要处理特殊数据结构或具有特殊填充逻辑的 JavaBean，就需要更换不同的装配操作处理器。

crane4j 默认提供了三种处理器：

- `OneToOneAssembleOperationHandler`：一对一装配操作处理器，也是默认的处理器；
- `OneToManyAssembleOperationHandler`：一对多装配操作处理器；
- `ManyToManyAssembleOperationHandler`：多对多装配操作处理器；

## 1.一对一

你可以在 `@Assemble` 注解的 `handler` 属性或 `handlerType` 属性中指定要使用的处理器。比如：

```java
public class Foo {
    @Assemble(container = "foo", handler = "oneToOneAssembleOperationHandler")
    private String name;
    private String alias;
}
```

当你不指定时，默认将会使用一对一装配处理器 `OneToOneAssembleOperationHandler` 执行操作，即一个 `key` 值只对应一个数据源对象。

## 2.一对多

<img src="https://img.xiajibagao.top/image-20230320105459223.png" alt="image-20230320105459223" style="zoom: 33%;" />

在一对多的情况下，一个属性对应多个数据源对象，即从数据源容器中查询数据时，一个 key 可以查出一个集合或数组。比如：

我们有一个名为 `customer` 的容器，可以根据客户 id 查询客户，并返回按客户组别 id 分组的 `Map` 集合：

```java
// 根据ID查询客户，返回的数据按客户组别ID分组
Container<String> customerContainer = LambdaContainer.forLambda(
    "customer", ids -> customerService.listByIds(ids)
        .stream()
        .collect(Collectors.groupBy(CustomerDO::getGroupId))
);
```

现在，我们可以借助一对多装配处理器，批量提取数据源对象的属性，并将其赋值给目标对象的指定属性：

```java
public class CustomerVO {
    @Assemble(
        container = "customer",
        handler = "oneToManyAssembleOperationHandler",
        props = @Mapping(src = "name", ref = "customerNames")
    )
    private Integer id;
    private List<String> customerNames;
}
```

在上面的示例中，我们根据 id 集合获取按客户组别ID分组的客户集合，并提取 `CustomerDO.name` 作为集合，然后赋值给 `CustomerVO.customerNames`。

## 3.多对多

<img src="https://img.xiajibagao.top/image-20230320105521429.png" alt="image-20230320105521429" style="zoom:33%;" />

在多对多的情况下，多个键（key）对应多个数据源对象，实际场景中，比较常见的情况有三种：

- 键字段是按特定分隔符拼接的字符串；
- 建字段是集合类型；
- 建字段是数组类型；

例如，假设存在以下键字段类型：

```java
private String idStr; // 键字段为按分隔符拼接的字符串，例如："a, b, c"
private Set<Integer> idList; // 键字段为集合，例如：[a, b, c]
private Integer[] idArray; // 键字段为数组，例如：[a, b, c]
```

针对多个键字段映射，需要使用特定的装配操作处理器 `ManyToManyAssembleOperationHandler`：

```java
public class StudentVO {
    @Assemble(
        container = "teacher", 
        handler = "manyToManyAssembleOperationHandler",
        props = @Mapping(src = "name", ref = "teacherNames")
    )
    private String teacherIds; // 默认支持 "1, 2, 3" 格式
    private List<String> teacherNames; // 填充的格式默认为 src1, src2, src3
}
```

在上面的示例中，根据 `teacherIds` 字段字符串中按分隔符分割的多个键值，查询关联的多个 `Teacher` 对象，然后将 `Teacher` 集合的 `name` 属性映射为 `List<String>` 并赋值给 `StudentVO.teacherNames` 字段。

这种字段映射遵循普通字段映射的语义，例如对象映射：

```java
public class StudentVO {
    @Assemble(
        container = "teacher", 
        props = @Mapping(ref = "teachers"),
        handlerName = "manyToManyAssembleOperationHandler"
    )
    private String teacherIds; // 默认支持 "1, 2, 3" 格式
    private List<Teacher> teachers;
}
```

在批量映射的情况下，返回的对象可以是数据源对象或数据源对象的属性集合。

**更换分隔符**

默认情况下，若返回值为字符串，`ManyToManyAssembleOperationHandler` 总是尝试将其根据`,`分割为字符串集合，但如果有必要，用户也可以通过 `ManyToManyAssembleOperationHandler.setKeySplitter` 方法设置自己需要的分隔符。

例如，如果希望使用`|`符号作为分隔符，则可以执行以下操作：

~~~java
ManyToManyAssembleOperationHandler handler = SpringUtil.getBean(ManyToManyAssembleOperationHandler.class);
// 按 | 分割
handler.setKeySplitter(k -> new ManyToManyAssembleOperationHandler.DefaultSplitter("|"));
~~~

