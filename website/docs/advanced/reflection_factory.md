# 反射工厂

在 `crane4j` 中，提供了属性操作器 `PropertyOperator`，它类似于 MyBatis 的反射工厂，用于统一管理框架各处的反射调用操作。

它被用于支持包括装配操作执行器、拆卸操作执行器、方法数据源容器以及自动填充切面中的各种属性操作功能。

`PropertyOperator` 默认提供了以下几个实现：

| 实现                              | 介绍                                                         | 是否默认生效 |
| --------------------------------- | ------------------------------------------------------------ | ------------ |
| `ReflectivePropertyOperator`      | 基于原生反射的普通反射属性操作器，最通用、功能最全的操作器   | √            |
| `MethodHandlePropertyOperator`    | 基于方法句柄 `MethodHandle` 的属性操作器。较前者拥有更高的性能，但是不支持包括虚拟字段在内的一些额外功能 | ×            |
| `AsmReflectivePropertyOperator`   | 基于 `ReflectAsm` 的字节码反射属性操作器，理论上具备最高的性能，但是不具备包括虚拟字段在内的一些额外功能，且在 JDK9 及以上版本无法使用 | ×            |
| `CacheablePropertyOperator`       | 装饰器，在原有功能的基础上支持 `getter` 和 `setter` 缓存     | √            |
| `MapAccessiblePropertyOperator`   | 装饰器，在原有功能的基础上支持读写 `Map` 集合                | √            |
| `ChainAccessiblePropertyOperator` | 装饰器，在原有功能的基础上支持通过链式操作符读写嵌套对象属性 | √            |

如果你想替换默认的 `PropertyOperator` 实现，可以在 Spring 配置类中重新声明一个 `PropertyOperator`，并返回自定义的实现：

```java
@Bean
public PropertyOperator customPropertyOperator() {
    return new CustomPropertyOperator();
}
```

或者，你也可以直接为已有的操作器添加额外的装饰器：

~~~java
DecoratedPropertyOperator decoratedPropertyOperator = (DecoratedPropertyOperator) configuration.getPropertyOperator();
PropertyOperator delegate = decoratedPropertyOperator.getPropertyOperator();
delegate = new CustomPropertyOperator(delegate);
propertyOperatorHolder.setPropertyOperator(delegate);
~~~

:::warning

注意，装饰器是 crane4j 一些独特功能的来源，比如对 `Map` 对象的支持，或者对链式操作符的支持等，不过同时它们也会带来额外的性能消耗。

如果你更在乎性能，且用不到这些额外的功能，那么可以选择性的放弃一些装饰器。

:::