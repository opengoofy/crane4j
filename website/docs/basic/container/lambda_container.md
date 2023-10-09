# Lambda 容器

Lambda 容器指以 Lambda 表达式作为数据源的数据源容器，相对集合、常量和枚举，它使用起来会更加灵活。

你可以将任何**输入`Collection`集合并返回`Map`集合**的 lambda 表达式定义为容器：

```java
// 定义Lambda容器，接受key值，并返回按key分组的数据源对象
Container<String> container = Containers.forLambda(
    namespace, keys -> keys.stream().collect(Collectors.toMap(Function.identity(), Function.identity()))
);

// 获取全局上下文并注册容器
Crane4jGlobalConfiguration configuration = SpringUtils.getBean(Crane4jGlobalConfiguration.class);
configuration.registerContainer(container);
```

其中，方法数据源的函数式接口 `DataProvider` 也提供了一些便捷的工厂方法：

```java
// 固定返回某个集合
DataProvider<Integer, ?> provider1 = DataProvider.fixed(Collections.emptyMap());
Containers.forLambda(namespace, provider1);

// 总是返回空集合
DataProvider<Integer, ?> provider2 = DataProvider.empty();
Containers.forLambda(namespace, provider2);
```
