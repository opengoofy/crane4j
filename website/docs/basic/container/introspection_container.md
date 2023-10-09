# 对象内省

当你未指定填充使用的数据源容器时，crane4j 将会直接将待处理对象本身作为数据源，换而言之，这是当容器为空时的一种特殊处理。

例如：

```java
public class Foo {
    @Assemble(props = @Mapping(ref = "alias"))
    private String name;
    private String alias;
}
```

在上述示例中，通过对象内省的方式，将对象的 `name` 字段映射到 `alias` 字段上。

这个功能通常用于同步冗余的别名字段，可以通过对象内省自动将一个字段的值设置到另一个字段上，从而实现字段值的同步更新。