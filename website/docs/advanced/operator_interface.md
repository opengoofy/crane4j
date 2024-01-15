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

## 1.统一填充方法参数

### 1.1.声明装配操作

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

关于如何配置一个装配操作，你可以直接参见文档：[声明装配操作](./../basic/declare_assemble_operation.md)。

:::warning

注意，在方法上声明装配操作时，注解上需要显式的指定 key 字段。比如上文就显式的指定 key 字段为 “id”。

:::

### 1.2.创建代理对象

**在 Spring 环境**

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

在项目启动后，将自动为接口创建 `BeanDefinition`，并在 Spring 容器中创建对应的 Bean。此时，你可以通过依赖注入来获得操作者接口：

```java
@Component
public class FooService {
    @Autowired
    private OperatorInterface operator; // 注入操作者接口
}
```

**在非 Spring 环境**

在非 Spring 环境，你需要手动的创建代理工厂，然后才能基于代理工厂为接口创建代理对象：

~~~java
// 创建操作者接口的代理工厂
Crane4jGlobalConfiguration configuration = SimpleCrane4jGlobalConfiguration.create();
OperatorProxyFactory proxyFactory = ConfigurationUtil.createOperatorProxyFactory(configuration);
// 通过代理工厂创建代理对象
OperatorInterface operator = proxyFactory.get(OperatorInterface.class);
~~~

### 1.3.对入参进行填充

无论如何，当你获得对应的代理对象后，你就可以基于方法填充任意类型的对象了。

~~~java
List<Foo> fooList = new ArrayList<>();
operator.fill(fooList); // 填充 foo 对象
~~~

在调用代理对象的 `fill` 方法后，我们的输入参数 `targets` 将根据 `operate` 方法上的配置进行填充。

### 1.4.指定执行器和解析器

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

### 1.5.将其它参数作为数据源

当抽象方法具备复数参数时，我们可以将第二个及后面的几个参数作为临时数据源。

比如：

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

该功能基于 `DynamicContainerOperatorProxyMethodFactory` 实现。

**适配不同的参数类型**

除了可以将 Map 集合适配为容器外，也支持直接传入 `Container`，或 `DataProvider` 类型的参数：

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

你也可以通过 `ContainerAdapterRegister` 的 `registerAdapter` 方法添加其他类型的参数适配器：

```java
@Autowried
private ContainerAdapterRegister adapterRegister;

// 将 LinkedHashMap 类型的参数适配为容器
register.registerAdapter(
    LinkedHashMap.class, (namespace, parameter) -> Containers.forMap(namespace, (Map<Object, ?>) parameter)
);
```

## 2.分别填充方法参数

当你直接在方法上通过 `@AssembleXXX` 注解声明装配操作时，crane4j 将会统一按照该配置对参数进行填充。

不过，你也可以选择不在方法上加任何注解。在这种情况下，crane4j 将会**分别解析每个参数的类型，然后分别按照每个参数类型对应的操作配置对参数进行填充**。

### 2.1.简单使用

比如，当你需要在很多地方填充某个特定类型的对象时，你可以直接在操作者接口中定义一个抽象方法：

~~~java
@Operator
private interface OperatorInterface {
    void fill(Foo1 foo1, Foo2 foo2);
}
~~~

在这种情况下，你可以通过 `operateMethod` 去填充 `Foo1` 和 `Foo2` 两个类型的对象：

~~~java
@Component
public class Example {
    @Autowired
    private OperatorInterface operator;
    
    public Tuple<Foo1, Foo2> doSomething() {
        Foo1 foo1 = new Foo1(1);
        Foo2 foo2 = new Foo2(2);
        operator.fill(foo1, foo2); // 填充 foo1 和 foo2
        return Tuple.of(new)
    }
}
~~~

这种做法等效于使用 `OperateTemplate` 进行手动填充：

~~~java
@Component
public class Example {
    @Autowired
    private OperateTemplate operateTemplate;
    
    public Tuple<Foo1, Foo2> doSomething() {
        Foo1 foo1 = new Foo1(1);
        Foo2 foo2 = new Foo2(2);
        operateTemplate.execute(foo1); // 填充 foo1
        operateTemplate.execute(foo2); // 填充 foo2
        return Tuple.of(new)
    }
}
~~~

两者差别不大，不过这种写法可以减少一次类型判断从而轻微的提高性能，并且更容易集中管理配置，你可以根据情况选择。

此外，在这种模式下，将会使用 `@Operator` 注解上的指定的执行器和解析器。

:::tip

关于 `OperateTemplate` 与手动填充，请参见：[触发填充-手动填充](./../basic/trigger_operation.md)。

:::

### 2.2.使用 @AutoOperate 注解

除上述情况外，你也可以使用 `@AutoOperate` 注解来进一步指定如何填充参数：

~~~java
@Operator
private interface OperatorInterface {
    
    @ArgAutoOperate
    void fill(
        @AutoOperate(type = Foo1.class, condition = "#foo1.id != null") Foo1 foo1, 
        @AutoOperate(type = Foo1.class, condition = "#foo2.id != null") Foo2 foo2);
}
~~~

在此处，`@AutoOperate` 注解的使用方式与自动填充中完全一致，具体内容请参见文档：[触发填充-自动填充](./../basic/trigger_operation.md)。

## 3.方法工厂

与 Spring 中基于 `@EventListener` 注解的声明式监听器机制类似，操作者接口中抽象方法的适配基于不同的方法工厂 `OperatorProxyMethodFactory` 实现，默认的四种实现调用顺序如下：

- `OperationAnnotationProxyMethodFactory`：用于统一填充方法参数；
- `DynamicSourceProxyMethodFactory`：若方法具备带有 `@ContainerParam` 注解的参数，则使用该参数作为临时数据源对首个参数进行填充；
- `ParametersFillProxyMethodFactory`：用于分别对每一个参数进行填充；
- `ArgAutoOperateProxyMethodFactory`：若方法带有 `@ArgAutoOperate` 注解，则分别对方法中每一个带有 `@AutoOperate` 注解的参数进行填充；

当有多个方法工厂时，将会使用首个匹配的工厂去生成代理方法。因此，若有必要，用户也可以自行实现接口并提高工厂的优先级以替换默认策略。

在 Spring 环境中，只需将自定义工厂类声明为 Spring Bean，即可自动注册。在非 Spring 环境中，用户需要在创建代理工厂 `OperatorProxyFactory` 时将所需的方法工厂作为参数传入。