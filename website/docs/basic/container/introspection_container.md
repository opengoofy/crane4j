# 对象内省

## 1.对象内省

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

这个功能通常用于同步冗余的别名字段，可以通过对象内省自动**将一个字段的值设置到另一个字段上，从而实现字段值的同步更新**。

## 2.键值映射

在 2.6.0 及以上版本，crane4j 提供了选项式配置风格的键值映射功能，它实际上是基于键值的内省。

它可以用于在某些场景下仅针对键值进行转换，比如字段脱敏，或者字段加解密。

### 2.1.配置

在使用前，你需要向 `AssembleKeyAnnotationHandler` 注册指定的值映射策略，这里我们以手机号脱敏为例：

~~~java
// 从 spring 容器获取 AssembleKeyAnnotationHandler
AssembleKeyAnnotationHandler handler = SpringUtil.getBean(AssembleKeyAnnotationHandler.class);
handler.registerValueMapperProvider("phone_number_desensitization", element -> 
	key -> { // 将手机号中间四位替换为 *
        String phone = (String)key;
        return phone.substring(0, 3) + "****" + phone.substring(7)
    }
);
~~~

在 Spring 环境中，你也可以直接实现 `AssembleKeyAnnotationHandler.ValueMapperProvider` 接口，并将其交给 Spring 容器管理，crane4j 在启动后将会自动注册。

### 2.2.使用

接着，在你要转换的字段——这里是手机号字段——上添加 `@AssembleKey` 注解即可：

```java
private static class Foo {
    @AssembleKey(mapper = "phone_number_desensitization", sort = 1)
    private String phone;
}
```

### 2.3.获取注解元素

我们会注意到，`registerValueMapperProvider` 方法的入参是一个函数式接口：

~~~java
@FunctionalInterface
public interface ValueMapperProvider {
    @NonNull
    UnaryOperator<Object> get(AnnotatedElement element);
}
~~~

这里的 `element` 实际上就是你声明的 `@AssembleKey` 注解所在的元素，它可以是类、方法或属性，通过获取它，你可以基于注解完成一些自定义的逻辑。

比如，手机号可能前面会带有国家代码，因此对于 "0086-xxxxxxxx" 格式的手机号，你需要跳过前五位 “0086-”，那么你可以这么做：

~~~java
private static class Foo {
    @Skip(5) // 跳过前四位国家编码
    @AssembleKey(mapper = "phone_number_desensitization", sort = 1)
    private String phone;
}

AssembleKeyAnnotationHandler handler = SpringUtil.getBean(AssembleKeyAnnotationHandler.class);
handler.registerValueMapperProvider("phone_number_desensitization", element -> {
    Skip skip = element.getAnnotation(Skip.class);
    return key -> { 
        String phone = (String)key;
        // 如果是 0086-10012341234 格式，那么跳过 ‘0086-’前五个字符
        if (Objects.nonNull(skip)) {
            phone = phone.substring(skip);
        }
        return phone.substring(0, 3) + "****" + phone.substring(7) // 将手机号中间四位替换为 *
    }	
});
~~~

