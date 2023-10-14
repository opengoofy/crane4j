# 操作者接口

操作者接口类似 `MapStruct` 的 `Mapper` 接口，你可以通过直接在接口抽象方法上添加 `@Assemble` 注解，然后通过调用该接口的代理对象的方法对任意类型的对象进行填充，而不必要求填充对象必须有一个对应的实体类。比如：

~~~java
// 声明操作者接口
@Operator
private interface OperatorInterface {
    @Assemble(key = "id", container = "user", props = @Mapping("name"))
    void fill(Collection<Map<String, Object>> targets);
}

// 生成对应代理对象，并通过方法进行填充
@Autowried
private OperatorInterface operator;
List<Foo> fooList = new ArrayList<>();
operator.fill(fooList); // 填充 foo 对象
~~~

它可以处理某些无法直接在类上配置注解的业务场景，比如：

- 用于填充的对象是 `JSONObject` / `Map`，就没有对应的 Java 类，因此也无法在类上或类的属性上添加注解配置；
- 填充的对象的字段非常像，但是它们确实不是一个类，也不存在提取公共父类的可能，又不想要每个类都重复配置；

## 1.配置操作者接口

首先，在一个**接口**上添加 `@Operator` 注解，将其声明为操作者。

然后，在抽象方法上使用 `@Assemble` 注解配置装配操作，就像在类或类属性上进行配置一样。比如：

```java
@Operator
private interface OperatorInterface {
    
    // 所有传入的 map 对象，都会根据 id 对应的值进行填充
    @Assemble(key = "id", container = "user", props = @Mapping("name"))
    void operate(Collection<Object> targets);
}
```

接着，你需要为接口生成代理类，然后才能使用代理类进行填充。

:::warning

注意，此时需要显式的指定 key 字段

:::

### 在 Spring 环境

在 Spring 环境中，你需要在启动类或配置类添加 `@OperatorScan` 注解指定扫描路径，就像 Mybatis 的 `MapperScan` 一样：

~~~java
@OperatorScan(
    includePackages = {"cn.crane4j.example.operators", "cn.crane4j.spring.example.operators"}, // 指定扫描路径
    includeClasses = FooOperator.class,  // 直接指定接口
    excludes = ExcludeOpeator.class // 排除特定的接口
)
@Configuration
protected class ExampleApplication {
}
~~~

在项目启动后，将自动为接口创建 `BeanDefinition`，并在 Spring 容器中创建对应的 Bean。

因此，你可以像使用 MyBatis 的 Mapper 一样，通过依赖注入来使用操作者接口：

```java
@Component
public class FooService {
    @Autowired
    private OperatorInterface operator; // 注入操作者接口
}
```

### 在非 Spring 环境

在非 Spring 环境，你需要手动的创建代理工厂，然后才能基于代理工厂为接口创建代理对象：

~~~java
// 创建操作者接口的代理工厂
Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
OperatorProxyFactory operatorProxyFactory = new OperatorProxyFactory(configuration, SimpleAnnotationFinder.INSTANCE);
operatorProxyFactory.addProxyMethodFactory(new DefaultOperatorProxyMethodFactory(configuration.getConverterManager()));

// 通过代理工厂创建代理对象
OperatorInterface operator = operatorProxyFactory.get(OperatorInterface.class);
~~~

## 2.使用接口填充

无论如何，当你获得操作接口对应的代理对象后，你就可以基于方法填充任意类型的对象了。

~~~java
List<Foo> fooList = new ArrayList<>();
operator.fill(fooList); // 填充 foo 对象
~~~

在调用 `fill` 方法后，我们的输入参数 `targets` 将根据 `operate` 方法上的配置进行填充。

## 3.动态数据源容器

有些时候，我们会希望动态的替换一次填充操作中的特定数据源容器，比如：

~~~java
@Operator
private interface OperatorInterface {
    
    // 指定入参 testContainer 需要适配为动态容器 user
    @Assemble(key = "id", container = "user", props = @Mapping("name"))
    void fill(Collection<Object> targets, @ContainerParam("user") Map<Integer, User> users);
}
~~~

在上述代码中，当执行时，入参的 `testContainer` 将会被适配为一个 `MapContainer` 容器，并在这次填充中替换原本通过命名空间引用的的 `user` 容器：

~~~java
List<Foo> fooList = fooService.listByIds(1, 2, 3);
Map<Integer, User> users = userService.listInternalUserByIds(
	fooList.stream().map(Foo::getUserId)
    	.collect(Collectors.toMap(User::getId, u -> u))
);
// 填充时，不再使用注册到全局配置中的 user 容器，而是直接从上述的 Map 集合中查询
operator.operate(targets, users);
~~~

### 自定义扩展

该功能基于 `OperatorProxyFactory` 实现，你也可以扩展自己的方法工厂，并注册到操作者接口代理工厂 `OperatorProxyMethodFactory` 中，从而实现一些自定义的逻辑。

### 参数适配

处了可以将 Map 集合适配为容器外，也支持直接传入 `Container`，或 `DataProvider` 类型的参数：

~~~java
@Operator
private interface OperatorInterface {

    // 参数类型为 Container
    @Assemble(key = "id", container = "user", props = @Mapping("name"))
    void operate(Collection<Object> targets, @ContainerParam("user") Container<Integer> users);

    // 参数类型为 DataProvider
    @Assemble(key = "id", container = "user", props = @Mapping("name"))
    void operate(Collection<Object> targets, @ContainerParam("user") DataProvider<Integer, User> users);
}
~~~

你也可以通过 `DynamicContainerOperatorProxyMethodFactory` 的 `addAdaptorProvider` 方法添加其他类型的参数适配器：

```java
@Autowried
private DynamicContainerOperatorProxyMethodFactory methodFactory;

// 将 LinkedHashMap 类型的参数适配为容器
DynamicContainerOperatorProxyMethodFactory factory = SpringUtil.getBean(DynamicContainerOperatorProxyMethodFactory.class);
factory.addAdaptorProvider(LinkedHashMap.class, (name, parameter) ->
	arg -> Containers.forMap(name, (Map<Object, ?>) arg)
));
```

## 4.指定执行器和解析器

类似于 `@AutoOperate`，`@Operator` 接口也可以指定用于执行操作的执行器和配置解析器。

比如：

~~~java
@Operator(
    executorType = OrderedBeanOperationExecutor.class,
    parserType = TypeHierarchyBeanOperationParser.class
)
private interface OperatorInterface {
    @Assemble(key = "id", container = "test", props = @Mapping(ref = "name"))
    void operate(Collection<Map<String, Object>> targets);
}
~~~

## 5.方法工厂

与 Spring 中将被 `@EventListener` 注解的方法适配为监听器机制类似，操作者接口中抽象方法的解析也是基于策略模式。

操作者的方法工厂 `OperatorProxyMethodFactory` 默认提供了两种实现：

- `DefaultProxyMethodFactory`：默认的代理方法工厂，支持处理所有有参方法；
- `DynamicSourceProxyMethodFactory`：动态数据源方法工厂，用于支持有不止一个参数的方法；

接口中的一个抽象方法仅会使用最匹配的工厂去生成代理方法。因此若有必要，用户也可以自行实现接口并提高工厂的优先级以替换默认策略。

在 Spring 环境中，只需将自定义工厂类声明为 Spring Bean，即可自动注册。在非 Spring 环境中，用户需要在创建代理工厂 `OperatorProxyFactory` 时将所需的方法工厂作为参数传入。